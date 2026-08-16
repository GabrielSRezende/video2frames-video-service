package br.com.video2frames.video2frames_video_service.application.dto;

import java.io.InputStream;

public record UploadVideoCommand(
        String ownerEmail,
        String fileName,
        String contentType,
        InputStream content,
        long contentLength) {
}
