INSERT IGNORE INTO member(
    id, email, nickname, email_verified,
    role, status, level, experience, play_count,
    quiz_set_count, hint_enabled
) VALUES (
    0730, 'test0730@example.com', '테스트유저', true,
    'USER', 'ACTIVE', 1, 0, 0, 0, true
);