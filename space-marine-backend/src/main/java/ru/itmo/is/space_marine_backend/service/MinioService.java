package ru.itmo.is.space_marine_backend.service;

import java.io.InputStream;
import java.time.Duration;

public interface MinioService {
    String uploadTemp(String txId, String filename, InputStream data, String contentType, long size);

    void copyToFinal(String sourceKey, String destKey);

    void delete(String key);

    String presignGetUrl(String key, Duration expiry, String filename);
}
