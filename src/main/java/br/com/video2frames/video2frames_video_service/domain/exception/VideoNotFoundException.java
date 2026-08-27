package br.com.video2frames.video2frames_video_service.domain.exception;

import java.util.UUID;

public class VideoNotFoundException extends RuntimeException {
    public VideoNotFoundException(UUID videoId) {
        super("Vídeo não encontrado: " + videoId);
    }
}
