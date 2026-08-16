package br.com.video2frames.video2frames_video_service.infrastructure.web.exception;

import br.com.video2frames.video2frames_video_service.domain.exception.InvalidVideoStateTransitionException;
import br.com.video2frames.video2frames_video_service.domain.exception.UnsupportedVideoFormatException;
import br.com.video2frames.video2frames_video_service.domain.exception.VideoAccessDeniedException;
import br.com.video2frames.video2frames_video_service.domain.exception.VideoNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VideoNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(VideoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(VideoAccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(VideoAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(UnsupportedVideoFormatException.class)
    public ResponseEntity<Map<String, String>> handleUnsupportedFormat(UnsupportedVideoFormatException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidVideoStateTransitionException.class)
    public ResponseEntity<Map<String, String>> handleInvalidTransition(InvalidVideoStateTransitionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
    }
}
