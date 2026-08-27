package br.com.video2frames.video2frames_video_service.infrastructure.messaging.dto;

public record VideoFailedMessage(String videoId, String reason) {
}
