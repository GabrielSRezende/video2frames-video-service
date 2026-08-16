package br.com.video2frames.video2frames_video_service.domain.exception;

public class UnsupportedVideoFormatException extends RuntimeException {
    public UnsupportedVideoFormatException(String contentType) {
        super("Formato de arquivo não suportado: " + contentType);
    }
}
