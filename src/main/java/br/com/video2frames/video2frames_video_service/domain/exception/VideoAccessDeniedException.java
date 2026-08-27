package br.com.video2frames.video2frames_video_service.domain.exception;

public class VideoAccessDeniedException extends RuntimeException {
    public VideoAccessDeniedException() {
        super("Este vídeo pertence a outro usuário");
    }
}
