package br.com.video2frames.video2frames_video_service.domain.repository;

import br.com.video2frames.video2frames_video_service.domain.model.VideoStatusHistoryEntry;

public interface VideoStatusHistoryRepository {

    VideoStatusHistoryEntry save(VideoStatusHistoryEntry entry);
}
