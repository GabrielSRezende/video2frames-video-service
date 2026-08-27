package br.com.video2frames.video2frames_video_service.application.dto;

import java.util.UUID;

public record VideoProcessedEvent(UUID videoId, String zipKey, int frameCount) {
}
