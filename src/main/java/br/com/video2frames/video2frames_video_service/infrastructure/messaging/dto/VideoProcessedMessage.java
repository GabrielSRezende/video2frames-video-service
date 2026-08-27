package br.com.video2frames.video2frames_video_service.infrastructure.messaging.dto;

public record VideoProcessedMessage(String videoId, String zipKey, int frameCount) {
}
