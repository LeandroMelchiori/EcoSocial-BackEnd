package com.alura.foro.hub.api.modules.catalogo.config;

import com.alura.foro.hub.api.modules.catalogo.service.StorageService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@TestConfiguration
class TestStorageConfig {
    @Bean
    @Primary
    StorageService storageService() {
        return new StorageService() {
            private final Map<String, byte[]> store = new HashMap<>();

            @Override public String saveProductImage(Long productoId, MultipartFile file, int orden) { throw new UnsupportedOperationException(); }
            @Override public List<String> uploadProductImagesTemp(Long productoId, List<MultipartFile> files, String opId) { throw new UnsupportedOperationException(); }
            @Override public List<String> promoteTempToFinal(Long productoId, String opId, List<String> tempKeys) { throw new UnsupportedOperationException(); }
            @Override public void purgeTemp(String opId) { }

            @Override public String moveProductDirToTrash(Long productoId, String opId) { return ""; }
            @Override public void restoreTrashToProductDir(Long productoId, String trashPrefix) {}
            @Override public void purgeTrash(String trashPrefix) {}

            @Override public void deleteObjects(List<String> objectKeys) {
                if (objectKeys == null) return;
                objectKeys.forEach(store::remove);
            }

            @Override public String getUrl(String objectKey) { return "http://test/" + objectKey; }

            @Override
            public String saveEmprendimientoLogo(Long emprendimientoId, MultipartFile file) {
                String key = "logos/" + emprendimientoId + "/" + UUID.randomUUID() + ".jpg";
                try { store.put(key, file.getBytes()); } catch (Exception ignored) {}
                return key;
            }
            @Override
            public void deleteObject(String key) { store.remove(key); }
        };
    }
}
