package br.com.video2frames.video2frames_video_service.infrastructure.persistence.adapter;

import br.com.video2frames.video2frames_video_service.domain.model.VideoStatusHistoryEntry;
import br.com.video2frames.video2frames_video_service.domain.repository.VideoStatusHistoryRepository;
import br.com.video2frames.video2frames_video_service.infrastructure.persistence.mapper.VideoStatusHistoryMapper;
import br.com.video2frames.video2frames_video_service.infrastructure.persistence.repository.VideoStatusHistoryJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class VideoStatusHistoryRepositoryAdapter implements VideoStatusHistoryRepository {

    private final VideoStatusHistoryJpaRepository jpaRepository;
    private final VideoStatusHistoryMapper mapper;

    public VideoStatusHistoryRepositoryAdapter(VideoStatusHistoryJpaRepository jpaRepository, VideoStatusHistoryMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public VideoStatusHistoryEntry save(VideoStatusHistoryEntry entry) {
        var saved = jpaRepository.save(mapper.toEntity(entry));
        return mapper.toDomain(saved);
    }
}
