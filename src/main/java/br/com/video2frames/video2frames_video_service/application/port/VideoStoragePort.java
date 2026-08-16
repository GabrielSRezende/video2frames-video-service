package br.com.video2frames.video2frames_video_service.application.port;

import java.io.InputStream;
import java.util.UUID;

public interface VideoStoragePort {

    String storeOriginalVideo(UUID videoId, String ownerEmail, String fileName,
                              InputStream content, long contentLength, String contentType);

    InputStream fetchZip(String zipKey);
}
