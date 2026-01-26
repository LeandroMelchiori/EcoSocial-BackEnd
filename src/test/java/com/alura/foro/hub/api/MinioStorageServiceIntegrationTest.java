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
        // ✅ Si MinIO no está disponible -> SKIP del test (no falla)
        Assumptions.assumeTrue(isMinioUp(minioEndpoint, Duration.ofSeconds(2)),
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

    private boolean isMinioUp(String endpoint, Duration timeout) {
        try {
            String base = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;

            // MinIO suele responder en /minio/health/ready (y si no, el GET al root igual te dice si hay algo vivo)
            String healthUrl = base + "/minio/health/ready";

            HttpURLConnection con = (HttpURLConnection) new URL(healthUrl).openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout((int) timeout.toMillis());
            con.setReadTimeout((int) timeout.toMillis());

            int code = con.getResponseCode();
            return code >= 200 && code < 500; // 200-399 OK, 404 también indica "hay server"
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    void saveProductImage_sube_objeto_y_existe_en_bucket() throws Exception {
        var file = new MockMultipartFile(
                "imagenes",
                "a.png",
                "image/png",
                "contenido".getBytes(StandardCharsets.UTF_8)
        );

        Long productoId = 10L;
        int orden = 1;

        String objectKey = storage.saveProductImage(productoId, file, orden);

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

        String url = storage.getUrl(objectKey);
        assertNotNull(url);
        assertTrue(url.startsWith("http"));
    }
}
