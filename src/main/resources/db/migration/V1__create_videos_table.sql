CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE videos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_email VARCHAR(255) NOT NULL,
    file_name VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    video_key VARCHAR(500) NOT NULL,
    zip_key VARCHAR(500),
    frame_count INTEGER,
    error_reason VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_videos_owner_email ON videos (owner_email);
