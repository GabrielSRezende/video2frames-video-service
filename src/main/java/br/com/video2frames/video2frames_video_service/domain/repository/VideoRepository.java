package br.com.video2frames.video2frames_video_service.domain.repository;

import br.com.video2frames.video2frames_video_service.domain.model.Video;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoRepository {

    Video save(Video video);

    Optional<Video> findById(UUID id);

    List<Video> findAllByOwnerEmail(String ownerEmail);
}
