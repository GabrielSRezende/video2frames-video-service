package br.com.video2frames.video2frames_video_service.infrastructure.messaging.dto;

public record VideoUploadedMessage(String videoId, String ownerEmail, String videoKey) {
}
