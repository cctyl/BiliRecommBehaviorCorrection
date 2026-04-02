UPDATE video_detail
SET handle_type = CASE
                      WHEN handle_type = 'THUMB_UP' THEN 'WHITE'
                      WHEN handle_type = 'DISLIKE' THEN 'BLACK'
                      ELSE handle_type
    END
WHERE handle_type IN ('THUMB_UP', 'DISLIKE');