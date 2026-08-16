CREATE TABLE video_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    video_id UUID NOT NULL REFERENCES videos (id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL,
    reason VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_video_status_history_video_id ON video_status_history (video_id);
