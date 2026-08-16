package br.com.video2frames.video2frames_video_service.application.dto;

import java.util.UUID;

public record VideoFailedEvent(UUID videoId, String reason) {
}
