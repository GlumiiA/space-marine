package ru.itmo.is.space_marine_backend.service.impl;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.itmo.is.space_marine_backend.service.MinioService;

import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class MinioServiceImpl implements MinioService {

    private final MinioClient client;
    private final String bucket;

    public MinioServiceImpl(MinioClient client, @Value("${minio.bucket-name}") String bucket) {
        this.client = client;
        this.bucket = bucket;
    }

    @Override
    public String uploadTemp(String txId, String filename, InputStream data, String contentType, long size) {
        try {
            String key = String.format("tmp/%s/%s", txId, filename);
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(data, size, -1)
                    .contentType(contentType)
                    .build();
            client.putObject(args);
            return key;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload temp object to MinIO", e);
        }
    }

    @Override
    public void copyToFinal(String sourceKey, String destKey) {
        try {
            // Use server-side copy
            CopySource source = CopySource.builder().bucket(bucket).object(sourceKey).build();
            CopyObjectArgs args = CopyObjectArgs.builder()
                    .bucket(bucket)
                    .object(destKey)
                    .source(source)
                    .build();
            client.copyObject(args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy object in MinIO", e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            RemoveObjectArgs args = RemoveObjectArgs.builder().bucket(bucket).object(key).build();
            client.removeObject(args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete object in MinIO", e);
        }
    }

    @Override
    public String presignGetUrl(String key, Duration expiry) {
        try {
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(key)
                    .expiry((int) expiry.getSeconds())
                    .build();
            return client.getPresignedObjectUrl(args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create presigned url", e);
        }
    }

    @Override
    public String presignGetUrl(String key, Duration expiry, String filename) {
        try {
            java.util.Map<String, String> extra = new java.util.HashMap<>();
            // force download with attachment disposition
            extra.put("response-content-disposition", "attachment; filename=\"" + filename + "\"");

            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(key)
                    .expiry((int) expiry.getSeconds())
                    .extraQueryParams(extra)
                    .build();
            return client.getPresignedObjectUrl(args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create presigned url", e);
        }
    }
}
