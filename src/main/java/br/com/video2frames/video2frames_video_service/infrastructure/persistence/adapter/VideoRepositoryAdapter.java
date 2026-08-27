package br.com.video2frames.video2frames_video_service.infrastructure.persistence.adapter;

import br.com.video2frames.video2frames_video_service.domain.model.Video;
import br.com.video2frames.video2frames_video_service.domain.repository.VideoRepository;
import br.com.video2frames.video2frames_video_service.infrastructure.persistence.mapper.VideoMapper;
import br.com.video2frames.video2frames_video_service.infrastructure.persistence.repository.VideoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class VideoRepositoryAdapter implements VideoRepository {

    private final VideoJpaRepository jpaRepository;
    private final VideoMapper mapper;

    public VideoRepositoryAdapter(VideoJpaRepository jpaRepository, VideoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Video save(Video video) {
        var saved = jpaRepository.save(mapper.toEntity(video));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Video> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Video> findAllByOwnerEmail(String ownerEmail) {
        return jpaRepository.findAllByOwnerEmail(ownerEmail).stream().map(mapper::toDomain).toList();
    }
}
