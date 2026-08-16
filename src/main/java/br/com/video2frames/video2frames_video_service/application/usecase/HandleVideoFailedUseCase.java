package br.com.video2frames.video2frames_video_service.application.usecase;

import br.com.video2frames.video2frames_video_service.application.dto.VideoFailedEvent;
import br.com.video2frames.video2frames_video_service.domain.exception.VideoNotFoundException;
import br.com.video2frames.video2frames_video_service.domain.model.Video;
import br.com.video2frames.video2frames_video_service.domain.model.VideoStatusHistoryEntry;
import br.com.video2frames.video2frames_video_service.domain.repository.VideoRepository;
import br.com.video2frames.video2frames_video_service.domain.repository.VideoStatusHistoryRepository;
import org.springframework.stereotype.Component;

@Component
public class HandleVideoFailedUseCase {

    private final VideoRepository videoRepository;
    private final VideoStatusHistoryRepository historyRepository;

    public HandleVideoFailedUseCase(VideoRepository videoRepository, VideoStatusHistoryRepository historyRepository) {
        this.videoRepository = videoRepository;
        this.historyRepository = historyRepository;
    }

    public void execute(VideoFailedEvent event) {
        Video video = videoRepository.findById(event.videoId())
                .orElseThrow(() -> new VideoNotFoundException(event.videoId()));

        Video failed = videoRepository.save(video.markFailed(event.reason()));
        historyRepository.save(VideoStatusHistoryEntry.record(failed.getId(), failed.getStatus(), event.reason()));
    }
}
