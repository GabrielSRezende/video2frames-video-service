package br.com.video2frames.video2frames_video_service.application.usecase;

import br.com.video2frames.video2frames_video_service.application.dto.VideoResult;
import br.com.video2frames.video2frames_video_service.domain.model.Video;
import br.com.video2frames.video2frames_video_service.domain.repository.VideoRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class ListUserVideosUseCase {

    private final VideoRepository videoRepository;

    public ListUserVideosUseCase(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public List<VideoResult> execute(String ownerEmail) {
        return videoRepository.findAllByOwnerEmail(ownerEmail).stream()
                .sorted(Comparator.comparing(Video::getCreatedAt).reversed())
                .map(VideoResult::from)
                .toList();
    }
}
