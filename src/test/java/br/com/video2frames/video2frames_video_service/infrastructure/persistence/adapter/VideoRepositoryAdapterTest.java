package br.com.video2frames.video2frames_video_service.infrastructure.persistence.adapter;

import br.com.video2frames.video2frames_video_service.domain.model.Video;
import br.com.video2frames.video2frames_video_service.domain.model.VideoStatus;
import br.com.video2frames.video2frames_video_service.infrastructure.persistence.entity.VideoJpaEntity;
import br.com.video2frames.video2frames_video_service.infrastructure.persistence.mapper.VideoMapper;
import br.com.video2frames.video2frames_video_service.infrastructure.persistence.repository.VideoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoRepositoryAdapterTest {

    @Mock
    private VideoJpaRepository jpaRepository;

    @Mock
    private VideoMapper mapper;

    private VideoRepositoryAdapter adapter;

    private final UUID id = UUID.randomUUID();
    private final VideoJpaEntity entity = new VideoJpaEntity(
            id, "gabriel@video2frames.com", "meu-video.mp4", VideoStatus.UPLOADED,
            "videos/gabriel/id.mp4", null, null, null, OffsetDateTime.now(), OffsetDateTime.now());
    private final Video domainVideo = Video.upload("gabriel@video2frames.com", "meu-video.mp4", "videos/gabriel/id.mp4")
            .withId(id);

    @BeforeEach
    void setUp() {
        adapter = new VideoRepositoryAdapter(jpaRepository, mapper);
    }

    @Test
    void save_mapeiaParaEntidadeSalvaEDevolveParaODominio() {
        when(mapper.toEntity(domainVideo)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domainVideo);

        Video saved = adapter.save(domainVideo);

        assertThat(saved).isEqualTo(domainVideo);
    }

    @Test
    void findById_quandoEncontrado_retornaVideoMapeado() {
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domainVideo);

        Optional<Video> result = adapter.findById(id);

        assertThat(result).contains(domainVideo);
    }

    @Test
    void findById_quandoNaoEncontrado_retornaOptionalVazio() {
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Video> result = adapter.findById(id);

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByOwnerEmail_retornaTodosOsVideosMapeadosParaODominio() {
        VideoJpaEntity outraEntity = new VideoJpaEntity(
                UUID.randomUUID(), "gabriel@video2frames.com", "outro-video.mp4", VideoStatus.COMPLETED,
                "key2", "zip2", 5, null, OffsetDateTime.now(), OffsetDateTime.now());
        Video outroDomain = Video.upload("gabriel@video2frames.com", "outro-video.mp4", "key2");

        when(jpaRepository.findAllByOwnerEmail("gabriel@video2frames.com"))
                .thenReturn(List.of(entity, outraEntity));
        when(mapper.toDomain(entity)).thenReturn(domainVideo);
        when(mapper.toDomain(outraEntity)).thenReturn(outroDomain);

        List<Video> result = adapter.findAllByOwnerEmail("gabriel@video2frames.com");

        assertThat(result).containsExactly(domainVideo, outroDomain);
    }
}
