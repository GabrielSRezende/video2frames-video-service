package br.com.video2frames.video2frames_video_service.application.port;

import br.com.video2frames.video2frames_video_service.application.dto.VideoUploadedEvent;

public interface VideoProcessingQueuePort {

    void publishVideoUploaded(VideoUploadedEvent event);
}
