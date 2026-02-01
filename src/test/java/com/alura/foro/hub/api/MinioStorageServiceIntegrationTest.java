package com.alura.foro.hub.api;

import com.alura.foro.hub.api.modules.catalogo.service.MinioStorageService;
import io.minio.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Assumptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MinioStorageServiceIntegrationTest {

    @Value("${minio.endpoint}")
    String minioEndpoint;

    @Value("${minio.access-key}")
    String accessKey;

    @Value("${minio.secret-key}")
    String secretKey;

    @Value("${minio.bucket}")
    String bucket;

    MinioClient client;
    MinioStorageService storage;

    @BeforeAll
    void setup() throws Exception {
        Assumptions.assumeTrue(isMinioReady(minioEndpoint, Duration.ofSeconds(2)),
                "MinIO no está disponible en " + minioEndpoint + " (se ignora el test)");

        client = MinioClient.builder()
                .endpoint(minioEndpoint)
                .credentials(accessKey, secretKey)
                .build();

        boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }

        storage = new MinioStorageService(client, bucket);
    }

    private boolean isMinioReady(String endpoint, Duration timeout) {
        try {
            String base = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
            String healthUrl = base + "/minio/health/ready";

            HttpURLConnection con = (HttpURLConnection) new URL(healthUrl).openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout((int) timeout.toMillis());
            con.setReadTimeout((int) timeout.toMillis());

            return con.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    void saveProductImage_sube_objeto_y_se_puede_leer() throws Exception {
        var file = new MockMultipartFile(
                "imagenes",
                "a.png",
                "image/png",
                "contenido".getBytes(StandardCharsets.UTF_8)
        );

        Long productoId = System.currentTimeMillis(); // único
        int orden = 1;

        String objectKey = null;

        try {
            objectKey = storage.saveProductImage(productoId, file, orden);

            assertNotNull(objectKey);
            assertTrue(objectKey.startsWith("productos/" + productoId + "/"));

            var stat = client.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
            assertNotNull(stat);
            assertTrue(stat.size() > 0);

            // ✅ verificar que realmente se puede leer el objeto
            try (var stream = client.getObject(
                    GetObjectArgs.builder().bucket(bucket).object(objectKey).build()
            )) {
                byte[] bytes = stream.readAllBytes();
                assertArrayEquals("contenido".getBytes(StandardCharsets.UTF_8), bytes);
            }

        } finally {
            // ✅ cleanup: no ensuciar el bucket
            if (objectKey != null) {
                client.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .build());
            }
        }
    }
}

