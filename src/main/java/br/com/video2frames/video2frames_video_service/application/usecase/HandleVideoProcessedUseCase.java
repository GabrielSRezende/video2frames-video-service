package br.com.video2frames.video2frames_video_service.application.usecase;

import br.com.video2frames.video2frames_video_service.application.dto.VideoProcessedEvent;
import br.com.video2frames.video2frames_video_service.domain.exception.VideoNotFoundException;
import br.com.video2frames.video2frames_video_service.domain.model.Video;
import br.com.video2frames.video2frames_video_service.domain.model.VideoStatusHistoryEntry;
import br.com.video2frames.video2frames_video_service.domain.repository.VideoRepository;
import br.com.video2frames.video2frames_video_service.domain.repository.VideoStatusHistoryRepository;
import org.springframework.stereotype.Component;

@Component
public class HandleVideoProcessedUseCase {

    private final VideoRepository videoRepository;
    private final VideoStatusHistoryRepository historyRepository;

    public HandleVideoProcessedUseCase(VideoRepository videoRepository, VideoStatusHistoryRepository historyRepository) {
        this.videoRepository = videoRepository;
        this.historyRepository = historyRepository;
    }

    public void execute(VideoProcessedEvent event) {
        Video video = videoRepository.findById(event.videoId())
                .orElseThrow(() -> new VideoNotFoundException(event.videoId()));

        Video completed = videoRepository.save(video.markCompleted(event.zipKey(), event.frameCount()));
        historyRepository.save(VideoStatusHistoryEntry.record(completed.getId(), completed.getStatus(), null));
    }
}
