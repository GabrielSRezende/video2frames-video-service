package br.com.video2frames.video2frames_video_service.application.dto;

import java.io.InputStream;

public record VideoDownload(InputStream content, String suggestedFileName) {
}
