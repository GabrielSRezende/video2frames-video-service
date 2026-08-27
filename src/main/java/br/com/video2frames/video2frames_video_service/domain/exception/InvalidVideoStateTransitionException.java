package br.com.video2frames.video2frames_video_service.domain.exception;

public class InvalidVideoStateTransitionException extends RuntimeException {
    public InvalidVideoStateTransitionException(String message) {
        super(message);
    }
}
