package com.alura.foro.hub.api.modules.catalogo.service;

import io.minio.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
public class StorageService {

    private final MinioClient minio;
    private final String bucket;

    public StorageService(MinioClient minio,
                          @Value("${minio.bucket}") String bucket) {
        this.minio = minio;
        this.bucket = bucket;
    }

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
                minio.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(objectKey)
                                .stream(in, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            return objectKey;

        } catch (Exception e) {
            throw new RuntimeException("Error al guardar imagen en MinIO", e);
        }
    }


    public void deleteProductDir(Long productoId) {
        deletePrefixOrThrow("productos/" + productoId + "/");
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

    public String moveProductDirToTrash(Long productoId, String opId) {
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
                                .source(
                                        CopySource.builder()
                                                .bucket(bucket)
                                                .object(srcObj)
                                                .build()
                                )
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

    public void restoreTrashToProductDir(Long productoId, String trashPrefix) {
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
        } catch (Exception e) {
            throw new RuntimeException("Error restaurando desde trash. productoId=" + productoId, e);
        }
    }

    public void purgeTrash(String trashPrefix) {
        deletePrefixOrThrow(trashPrefix);
    }

    public void deleteObjects(List<String> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) return;

        try {
            for (String key : objectKeys) {
                if (key == null || key.isBlank()) continue;
                minio.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucket)
                                .object(key)
                                .build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Error borrando objetos en MinIO", e);
        }
    }

    private void ensureBucket() throws IOException {
        try {
            boolean exists = minio.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build()
            );
            if (!exists) {
                minio.makeBucket(
                        MakeBucketArgs.builder().bucket(bucket).build()
                );
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
                            .method(io.minio.http.Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(60 * 60) // 1 hora
                            .build()
            );
        } catch (Exception e) {
            return null;
        }
    }

}
