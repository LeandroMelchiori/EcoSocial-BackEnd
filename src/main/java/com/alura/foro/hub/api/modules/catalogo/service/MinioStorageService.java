package com.alura.foro.hub.api.modules.catalogo.service;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(name="catalogo.storage", havingValue="minio")
public class MinioStorageService implements StorageService {
    private final MinioClient minio;
    private final String bucket;

    @PersistenceContext
    private EntityManager em;

    public MinioStorageService(MinioClient minio,
                               @Value("${minio.bucket}") String bucket) {
        this.minio = minio;
        this.bucket = bucket;
    }

    @Override
    public String saveProductImage(Long productoId, MultipartFile file, int orden) {
        try {
            ensureBucket();

            String original = StringUtils.cleanPath(
                    file.getOriginalFilename() == null ? "img" : file.getOriginalFilename()
            );
            String ext = getExtension(original);

            String filename = "img_" + orden + "_" + UUID.randomUUID()
                    + (ext.isBlank() ? "" : "." + ext);

            String objectKey = "productos/" + productoId + "/" + filename;

            try (InputStream in = file.getInputStream()) {
                long partSize = 10L * 1024 * 1024; // 10 MB

                minio.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(objectKey)
                                .stream(in, file.getSize(), partSize)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            return objectKey;

        } catch (Exception e) {
            throw new RuntimeException("Error al guardar imagen en MinIO", e);
        }
    }

    @Override
    public List<String> uploadProductImagesTemp(Long productoId, List<MultipartFile> files, String opId) {
        ensureBucketRuntime();

        String tempPrefix = "temp/" + opId + "/productos/" + productoId + "/";

        List<String> tempKeys = new ArrayList<>();
        int orden = 1;

        try {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;

                String original = StringUtils.cleanPath(
                        file.getOriginalFilename() == null ? "img" : file.getOriginalFilename()
                );
                String ext = getExtension(original);

                String filename = "img_" + orden + "_" + UUID.randomUUID()
                        + (ext.isBlank() ? "" : "." + ext);

                String objectKey = tempPrefix + filename;

                try (InputStream in = file.getInputStream()) {
                    long partSize = 10L * 1024 * 1024;
                    minio.putObject(
                            PutObjectArgs.builder()
                                    .bucket(bucket)
                                    .object(objectKey)
                                    .stream(in, file.getSize(), partSize)
                                    .contentType(file.getContentType())
                                    .build()
                    );
                }

                tempKeys.add(objectKey);
                orden++;
            }
            return tempKeys;

        } catch (Exception e) {
            try { purgeTemp(opId); } catch (Exception ignored) {}
            throw new RuntimeException("Error subiendo imágenes a temp en MinIO", e);
        }
    }

    @Override
    public List<String> promoteTempToFinal(Long productoId, String opId, List<String> tempKeys) {
        ensureBucketRuntime();

        String tempPrefix  = "temp/" + opId + "/productos/" + productoId + "/";
        String finalPrefix = "productos/" + productoId + "/";

        List<String> finalKeys = new ArrayList<>();

        try {
            for (String tempObj : tempKeys) {
                if (tempObj == null || tempObj.isBlank() || !tempObj.startsWith(tempPrefix)) continue;
                String fileName = tempObj.substring(tempPrefix.length());
                String finalObj = finalPrefix + fileName;

                minio.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(bucket)
                                .object(finalObj)
                                .source(CopySource.builder().bucket(bucket).object(tempObj).build())
                                .build()
                );

                finalKeys.add(finalObj);
            }

            return finalKeys;

        } catch (Exception e) {
            try { deleteObjects(finalKeys); } catch (Exception ignored) {}
            throw new RuntimeException("Error promoviendo temp->final en MinIO", e);
        }
    }

    @Override
    public void purgeTemp(String opId) {
        deletePrefixOrThrow("temp/" + opId + "/");
    }

    private void ensureBucketRuntime() {
        try {
            ensureBucket();
        } catch (Exception e) {
            throw new RuntimeException("Error verificando/creando bucket en MinIO: " + bucket, e);
        }
    }

    public void deletePrefixOrThrow(String prefix) {
        try {
            Iterable<Result<Item>> results = minio.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(prefix)
                            .recursive(true)
                            .build()
            );

            for (Result<Item> r : results) {
                String obj = r.get().objectName();
                minio.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(obj).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error borrando objetos en MinIO. prefix=" + prefix, e);
        }
    }

    @Override
    public String moveProductDirToTrash(Long productoId, String opId) {
        ensureBucketRuntime();

        String srcPrefix = "productos/" + productoId + "/";
        String trashPrefix = "trash/" + opId + "/productos/" + productoId + "/";

        try {
            Iterable<Result<Item>> results = minio.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(srcPrefix)
                            .recursive(true)
                            .build()
            );

            for (Result<Item> r : results) {
                String srcObj = r.get().objectName();
                String fileName = srcObj.substring(srcPrefix.length());
                String dstObj = trashPrefix + fileName;

                // copy
                minio.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(bucket)
                                .object(dstObj)
                                .source(CopySource.builder().bucket(bucket).object(srcObj).build())
                                .build()
                );

                // delete original
                minio.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(srcObj).build());
            }

            return trashPrefix;

        } catch (Exception e) {
            throw new RuntimeException("Error moviendo a trash en MinIO. productoId=" + productoId, e);
        }
    }

    @Override
    public void restoreTrashToProductDir(Long productoId, String trashPrefix) {
        ensureBucketRuntime();

        String dstPrefix = "productos/" + productoId + "/";

        try {
            Iterable<Result<Item>> results = minio.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(trashPrefix)
                            .recursive(true)
                            .build()
            );

            for (Result<Item> r : results) {
                String srcObj = r.get().objectName();
                String fileName = srcObj.substring(trashPrefix.length());
                String dstObj = dstPrefix + fileName;

                // copy back
                minio.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(bucket)
                                .object(dstObj)
                                .source(CopySource.builder().bucket(bucket).object(srcObj).build())
                                .build()
                );

                // delete trash copy
                minio.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(srcObj).build());
            }

            // opcional: borrar la carpeta del trash por prolijidad
            // deletePrefixOrThrow(trashPrefix);

        } catch (Exception e) {
            throw new RuntimeException("Error restaurando desde trash. productoId=" + productoId, e);
        }
    }

    @Override
    public void purgeTrash(String trashPrefix) {
        ensureBucketRuntime();
        deletePrefixOrThrow(trashPrefix);
    }

    public void deleteObjects(List<String> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) return;

        try {
            for (String key : objectKeys) {
                if (key == null || key.isBlank()) continue;
                minio.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(key).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error borrando objetos en MinIO", e);
        }
    }

    private void ensureBucket() throws IOException {
        try {
            boolean exists = minio.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minio.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            throw new IOException("Error verificando/creando bucket en MinIO: " + bucket, e);
        }
    }

    private String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot == -1) return "";
        return name.substring(dot + 1).toLowerCase();
    }

    public String getUrl(String objectKey) {
        try {
            return minio.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(60 * 60)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar URL para el objeto: " + objectKey, e);
        }
    }

    @Override
    public String saveEmprendimientoLogo(Long emprendimientoId, MultipartFile file) {
        try {
            ensureBucket();

            String original = StringUtils.cleanPath(
                    file.getOriginalFilename() == null ? "logo" : file.getOriginalFilename()
            );
            String ext = getExtension(original);

            String filename = "logo_" + UUID.randomUUID() + (ext.isBlank() ? "" : "." + ext);
            String objectKey = "emprendimientos/" + emprendimientoId + "/logo/" + filename;

            try (InputStream in = file.getInputStream()) {
                long partSize = 10L * 1024 * 1024;
                minio.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(objectKey)
                                .stream(in, file.getSize(), partSize)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            return objectKey;

        } catch (Exception e) {
            throw new RuntimeException("Error al guardar logo en MinIO", e);
        }
    }

    @Override
    public void deleteObject(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) return;
        try {
            minio.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
        } catch (Exception e) {
            throw new RuntimeException("Error borrando objeto en MinIO: " + objectKey, e);
        }
    }

}
