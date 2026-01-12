package com.alura.foro.hub.api.modules.catalogo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class StorageService {

    private final Path rootDir;

    public StorageService(@Value("${catalogo.upload-dir:uploads}") String uploadDir) {
        this.rootDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String saveProductImage(Long productoId, MultipartFile file, int orden) throws IOException {
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "img" : file.getOriginalFilename());
        String ext = getExtension(original);

        String filename = "img_" + orden + "_" + UUID.randomUUID() + (ext.isBlank() ? "" : "." + ext);

        Path productDir = rootDir.resolve("productos").resolve(productoId.toString());
        Files.createDirectories(productDir);

        Path target = productDir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // URL pública (gracias a static-locations=file:uploads/)
        return "/productos/" + productoId + "/" + filename;
    }

    private String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot == -1) return "";
        return name.substring(dot + 1).toLowerCase();
    }

    public void deleteProductDir(Long productoId) {
        try {
            Path productDir = rootDir.resolve("productos").resolve(productoId.toString());
            if (!Files.exists(productDir)) return;

            // borra recursivo
            Files.walk(productDir)
                    .sorted((a, b) -> b.compareTo(a)) // primero archivos, luego carpetas
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    });
        } catch (IOException ignored) {}
    }

}
