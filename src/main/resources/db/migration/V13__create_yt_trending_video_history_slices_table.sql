CREATE TABLE IF NOT EXISTS yt_trending_video_history_slices (
    id INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    video_id VARCHAR(128) NOT NULL,
    view_count INTEGER DEFAULT 0 NOT NULL,
    like_count INTEGER DEFAULT 0 NOT NULL,
    comment_count INTEGER DEFAULT 0 NOT NULL,
    subscriber_count INTEGER DEFAULT 0 NOT NULL,
    history_time_stamp BIGINT DEFAULT 0 NOT NULL
);