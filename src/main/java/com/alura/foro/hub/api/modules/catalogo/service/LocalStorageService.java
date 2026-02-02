package com.alura.foro.hub.api.modules.catalogo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "catalogo.storage", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final Path root;

    public LocalStorageService(@Value("${catalogo.local.root:uploads}") String rootDir) {
        this.root = Paths.get(rootDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.root);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo crear el directorio root de uploads: " + this.root, e);
        }
    }

    @Override
    public String saveProductImage(Long productoId, MultipartFile file, int orden) {
        try {
            Path dir = root.resolve(Paths.get("productos", String.valueOf(productoId)));
            Files.createDirectories(dir);

            String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "img" : file.getOriginalFilename());
            String ext = getExtension(original);

            String filename = "img_" + orden + "_" + UUID.randomUUID() + (ext.isBlank() ? "" : "." + ext);
            Path target = dir.resolve(filename);

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            // devolvemos "objectKey" relativo (igual idea que MinIO)
            return toKey(target);

        } catch (Exception e) {
            throw new RuntimeException("Error al guardar imagen en LocalStorage", e);
        }
    }

    @Override
    public List<String> uploadProductImagesTemp(Long productoId, List<MultipartFile> files, String opId) {
        try {
            Path tempDir = root.resolve(Paths.get("temp", opId, "productos", String.valueOf(productoId)));
            Files.createDirectories(tempDir);

            List<String> tempKeys = new ArrayList<>();
            int orden = 1;

            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;

                String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "img" : file.getOriginalFilename());
                String ext = getExtension(original);

                String filename = "img_" + orden + "_" + UUID.randomUUID() + (ext.isBlank() ? "" : "." + ext);
                Path target = tempDir.resolve(filename);

                try (InputStream in = file.getInputStream()) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }

                tempKeys.add(toKey(target));
                orden++;
            }

            return tempKeys;

        } catch (Exception e) {
            try { purgeTemp(opId); } catch (Exception ignored) {}
            throw new RuntimeException("Error subiendo imágenes a temp (local)", e);
        }
    }

    @Override
    public List<String> promoteTempToFinal(Long productoId, String opId, List<String> tempKeys) {
        try {
            Path finalDir = root.resolve(Paths.get("productos", String.valueOf(productoId)));
            Files.createDirectories(finalDir);

            List<String> finalKeys = new ArrayList<>();
            for (String tempKey : tempKeys) {
                if (tempKey == null || tempKey.isBlank()) continue;

                Path tempPath = fromKey(tempKey);
                String filename = tempPath.getFileName().toString();

                Path finalPath = finalDir.resolve(filename);
                Files.copy(tempPath, finalPath, StandardCopyOption.REPLACE_EXISTING);

                finalKeys.add(toKey(finalPath));
            }

            return finalKeys;

        } catch (Exception e) {
            try { deleteObjects(finalKeysFromTemp(productoId, opId, tempKeys)); } catch (Exception ignored) {}
            throw new RuntimeException("Error promoviendo temp->final (local)", e);
        }
    }

    @Override
    public void purgeTemp(String opId) {
        deletePrefix(Paths.get("temp", opId).toString());
    }

    @Override
    public String moveProductDirToTrash(Long productoId, String opId) {
        try {
            Path src = root.resolve(Paths.get("productos", String.valueOf(productoId)));
            Path trash = root.resolve(Paths.get("trash", opId, "productos", String.valueOf(productoId)));

            if (!Files.exists(src)) {
                // nada para mover, devolvemos igual el "prefix" por consistencia
                return Paths.get("trash", opId, "productos", String.valueOf(productoId)).toString().replace("\\", "/") + "/";
            }

            Files.createDirectories(trash.getParent());
            // mover carpeta completa
            Files.move(src, trash, StandardCopyOption.REPLACE_EXISTING);

            return Paths.get("trash", opId, "productos", String.valueOf(productoId)).toString().replace("\\", "/") + "/";

        } catch (Exception e) {
            throw new RuntimeException("Error moviendo producto a trash (local)", e);
        }
    }

    @Override
    public void restoreTrashToProductDir(Long productoId, String trashPrefix) {
        try {
            Path trashDir = root.resolve(trashPrefix.replace("/", FileSystems.getDefault().getSeparator()));
            Path dst = root.resolve(Paths.get("productos", String.valueOf(productoId)));

            if (!Files.exists(trashDir)) return;

            Files.createDirectories(dst.getParent());
            Files.move(trashDir, dst, StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
            throw new RuntimeException("Error restaurando desde trash (local)", e);
        }
    }

    @Override
    public void purgeTrash(String trashPrefix) {
        deletePrefix(trashPrefix);
    }

    @Override
    public void deleteObjects(List<String> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) return;

        for (String key : objectKeys) {
            if (key == null || key.isBlank()) continue;
            try {
                Files.deleteIfExists(fromKey(key));
            } catch (Exception e) {
                throw new RuntimeException("Error borrando objeto (local): " + key, e);
            }
        }
    }

    @Override
    public String getUrl(String objectKey) {
        String clean = objectKey.startsWith("/") ? objectKey.substring(1) : objectKey;
        return "/uploads/" + clean; // ej: /uploads/productos/1/a.png
    }

    // -----------------
    // helpers
    // -----------------
    private String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot == -1) return "";
        return name.substring(dot + 1).toLowerCase();
    }

    private String toKey(Path absolutePath) {
        // /abs/uploads/productos/1/a.png -> productos/1/a.png
        return root.relativize(absolutePath).toString().replace("\\", "/");
    }

    private Path fromKey(String key) {
        // key tipo "productos/1/a.png"
        String clean = key.startsWith("/") ? key.substring(1) : key;
        return root.resolve(clean).toAbsolutePath().normalize();
    }

    private void deletePrefix(String prefix) {
        try {
            Path dir = root.resolve(prefix.replace("/", FileSystems.getDefault().getSeparator()));
            if (!Files.exists(dir)) return;

            // borrar recursivo
            Files.walk(dir)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                    });

        } catch (Exception e) {
            throw new RuntimeException("Error borrando prefix (local): " + prefix, e);
        }
    }

    private List<String> finalKeysFromTemp(Long productoId, String opId, List<String> tempKeys) {
        // placeholder por si querés limpiar algo al fallar promote
        return List.of();
    }

    @Override
    public String saveEmprendimientoLogo(Long emprendimientoId, MultipartFile file) {
        try {
            Path dir = root.resolve(Paths.get("emprendimientos", String.valueOf(emprendimientoId), "logo"));
            Files.createDirectories(dir);

            String originalFilename = file.getOriginalFilename();
            
            // Validate raw filename first to detect and reject suspicious inputs
            if (originalFilename != null && !originalFilename.isBlank()) {
                if (originalFilename.contains("..")
                        || originalFilename.contains("/")
                        || originalFilename.contains("\\")
                        || originalFilename.contains("\0")) {
                    throw new IllegalArgumentException("Nombre de archivo inválido para el logo del emprendimiento");
                }
            }

            // Use default if null or blank
            if (originalFilename == null || originalFilename.isBlank()) {
                originalFilename = "logo";
            }

            String original = StringUtils.cleanPath(originalFilename);
            if (original.isBlank()) {
                throw new IllegalArgumentException("Nombre de archivo inválido para el logo del emprendimiento");
            }
            String ext = getExtension(original);

            String filename = "logo_" + UUID.randomUUID() + (ext.isBlank() ? "" : "." + ext);
            Path target = dir.resolve(filename);

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            return toKey(target);

        } catch (Exception e) {
            throw new RuntimeException("Error al guardar logo en LocalStorage", e);
        }
    }

    @Override
    public void deleteObject(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) return;
        try {
            Files.deleteIfExists(fromKey(objectKey));
        } catch (Exception e) {
            throw new RuntimeException("Error borrando objeto (local): " + objectKey, e);
        }
    }

}
