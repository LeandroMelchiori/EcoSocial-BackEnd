package com.alura.foro.hub.api.modules.catalogo.config;

import io.minio.MinioClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestMinioConfig {

    @Bean
    MinioClient minioClient() {
        return Mockito.mock(MinioClient.class);
    }
}
