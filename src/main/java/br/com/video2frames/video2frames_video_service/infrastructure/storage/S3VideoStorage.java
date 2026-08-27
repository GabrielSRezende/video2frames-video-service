package br.com.video2frames.video2frames_video_service.infrastructure.storage;

import br.com.video2frames.video2frames_video_service.application.port.VideoStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.UUID;

@Component
public class S3VideoStorage implements VideoStoragePort {

    private final S3Client s3Client;
    private final String bucket;

    public S3VideoStorage(S3Client s3Client, @Value("${aws.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @Override
    public String storeOriginalVideo(
            UUID videoId, String ownerEmail, String fileName,
            InputStream content, long contentLength, String contentType) {

        String extension = extractExtension(fileName);
        String key = "videos/%s/%s%s".formatted(ownerEmail, videoId, extension);

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromInputStream(content, contentLength));

        return key;
    }

    @Override
    public InputStream fetchZip(String zipKey) {
        return s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(zipKey).build());
    }

    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex >= 0 ? fileName.substring(dotIndex) : "";
    }
}
