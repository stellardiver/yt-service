CREATE TABLE IF NOT EXISTS yt_trending_videos (
    id INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    video_id VARCHAR(128) UNIQUE NOT NULL,
    title VARCHAR(512) NOT NULL,
    description VARCHAR(512) NOT NULL,
    channel_name VARCHAR(128) NOT NULL,
    channel_url VARCHAR(128) NOT NULL,
    published_time VARCHAR(128) NOT NULL,
    tab_title VARCHAR(128) NOT NULL,
    view_count VARCHAR(128) NOT NULL,
    view_count_at_start VARCHAR(128) NOT NULL,
    view_count_last VARCHAR(128) NOT NULL,
    video_length VARCHAR(128) NOT NULL,
    thumbnail_url VARCHAR(512) NOT NULL,
    video_url VARCHAR(128) NOT NULL,
    last_updated BIGINT DEFAULT 0 NOT NULL
);