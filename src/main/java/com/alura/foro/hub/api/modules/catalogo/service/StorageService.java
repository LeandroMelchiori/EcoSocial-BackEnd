package com.alura.foro.hub.api.modules.catalogo.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageService {
    String saveProductImage(Long productoId, MultipartFile file, int orden);
    List<String> uploadProductImagesTemp(Long productoId, List<MultipartFile> files, String opId);
    List<String> promoteTempToFinal(Long productoId, String opId, List<String> tempKeys);
    void purgeTemp(String opId);

    String moveProductDirToTrash(Long productoId, String opId);
    void restoreTrashToProductDir(Long productoId, String trashPrefix);
    void purgeTrash(String trashPrefix);

    void deleteObjects(List<String> objectKeys);
    String getUrl(String objectKey);
}
