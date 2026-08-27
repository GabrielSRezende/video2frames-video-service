package br.com.video2frames.video2frames_video_service.infrastructure.persistence.adapter;

import br.com.video2frames.video2frames_video_service.domain.model.VideoStatus;
import br.com.video2frames.video2frames_video_service.domain.model.VideoStatusHistoryEntry;
import br.com.video2frames.video2frames_video_service.infrastructure.persistence.entity.VideoStatusHistoryJpaEntity;
import br.com.video2frames.video2frames_video_service.infrastructure.persistence.mapper.VideoStatusHistoryMapper;
import br.com.video2frames.video2frames_video_service.infrastructure.persistence.repository.VideoStatusHistoryJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoStatusHistoryRepositoryAdapterTest {

    @Mock
    private VideoStatusHistoryJpaRepository jpaRepository;

    @Mock
    private VideoStatusHistoryMapper mapper;

    private VideoStatusHistoryRepositoryAdapter adapter;

    private final UUID videoId = UUID.randomUUID();
    private final VideoStatusHistoryEntry domainEntry = VideoStatusHistoryEntry.record(videoId, VideoStatus.UPLOADED, null);
    private final VideoStatusHistoryJpaEntity entity = new VideoStatusHistoryJpaEntity(
            UUID.randomUUID(), videoId, VideoStatus.UPLOADED, null, OffsetDateTime.now());

    @BeforeEach
    void setUp() {
        adapter = new VideoStatusHistoryRepositoryAdapter(jpaRepository, mapper);
    }

    @Test
    void save_mapeiaParaEntidadeSalvaEDevolveParaODominio() {
        when(mapper.toEntity(domainEntry)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(domainEntry);

        VideoStatusHistoryEntry saved = adapter.save(domainEntry);

        assertThat(saved).isEqualTo(domainEntry);
    }
}
