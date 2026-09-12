package br.com.video2frames.video2frames_video_service.application.usecase;

import br.com.video2frames.video2frames_video_service.application.dto.VideoResult;
import br.com.video2frames.video2frames_video_service.domain.model.Video;
import br.com.video2frames.video2frames_video_service.domain.repository.VideoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class ListUserVideosUseCase {

    private final VideoRepository videoRepository;

    public ListUserVideosUseCase(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @Cacheable(cacheNames = "userVideos", key = "#ownerEmail")
    public List<VideoResult> execute(String ownerEmail) {
        List<VideoResult> videos = videoRepository.findAllByOwnerEmail(ownerEmail).stream()
                .sorted(Comparator.comparing(Video::getCreatedAt).reversed())
                .map(VideoResult::from)
                .toList();

        log.info("Listagem de vídeos de {} retornou {} registro(s)", ownerEmail, videos.size());
        return videos;
    }
}
