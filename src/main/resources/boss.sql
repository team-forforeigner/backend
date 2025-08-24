-- =================================================================
-- 보스전 데이터 (수정된 최종본)
-- =================================================================

-- 1. '이순신' 보스 생성 (ID를 1로 직접 지정)
INSERT IGNORE INTO boss_stage (id, boss_name, total_hp, boss_image_url)
VALUES (1, '이순신', 1000, 'https://forforeigner-s3.s3.ap-northeast-2.amazonaws.com/images/boss_lee.png');

-- 2. '이순신' 보스의 1페이즈 생성 (boss_stage_id를 1로 직접 지정)
INSERT IGNORE INTO boss_phase (id, boss_stage_id, phase_number, mission_text, damage_per_quiz, time_limit_seconds, total_questions, required_correct_answers)
VALUES (1, 1, 1, '총 7문제 중 4문제 이상을 맞혀 왜군의 기세를 꺾어라!', 100, 120, 7, 4);

-- 3. '이순신' 보스의 2페이즈 생성 (boss_stage_id를 1로 직접 지정)
INSERT IGNORE INTO boss_phase (id, boss_stage_id, phase_number, mission_text, damage_per_quiz, time_limit_seconds, total_questions, required_correct_answers)
VALUES (2, 1, 2, '총 8문제 중 6문제 이상을 맞혀 최종 승리를 쟁취하라!', 100, 60, 8, 6);


-- 4. 1페이즈용 O/X 퀴즈 7개 생성 (boss_phase_id를 1로 직접 지정)
INSERT IGNORE INTO quiz (boss_phase_id, question, quiz_type, category, explanation, is_active, is_translatable)
VALUES
(1, '거북선은 세계 최초의 철갑선이다.', 'OX', 'HISTORY', '정확히는 철로 덮인 배가 아니라, 철판 대신 철첨(쇠못)을 꽂은 배였습니다.', 1, 1),
(1, '거북선의 머리에서는 연막을 뿜을 수 있었다.', 'OX', 'HISTORY', '용의 머리에서는 유황과 염초를 태운 연기를 뿜어 적을 혼란시켰습니다.', 1, 1),
(1, '거북선은 3층 구조의 배였다.', 'OX', 'HISTORY', '일반적으로 2층 구조로 알려져 있으며, 3층 구조설도 존재합니다.', 1, 1),
(1, '이순신 장군은 거북선을 직접 설계했다.', 'OX', 'HISTORY', '이순신 장군이 직접 설계한 것은 아니며, 기존의 배를 개량하여 전투에 활용했습니다.', 1, 1),
(1, '거북선의 등에는 날카로운 칼이나 송곳이 꽂혀 있었다.', 'OX', 'HISTORY', '맞습니다. 적군이 배 위로 쉽게 오르지 못하게 하기 위함이었습니다.', 1, 1),
(1, '거북선은 임진왜란 때 처음으로 사용되었다.', 'OX', 'HISTORY', '맞습니다. 임진왜란 당시 사천 해전에서 처음으로 실전에 투입되었습니다.', 1, 1),
(1, '거북선 안에는 휴식 공간이 있었다.', 'OX', 'HISTORY', '거북선 내부는 전투 공간으로만 이루어져 있어 따로 휴식 공간은 없었습니다.', 1, 1);

-- 1페이즈 퀴즈에 대한 정답(QuizChoice) 생성
INSERT IGNORE INTO quiz_choice (quiz_id, content, is_answer) VALUES
((SELECT id FROM quiz WHERE question = '거북선은 세계 최초의 철갑선이다.'), 'X', 1),
((SELECT id FROM quiz WHERE question = '거북선의 머리에서는 연막을 뿜을 수 있었다.'), 'O', 1),
((SELECT id FROM quiz WHERE question = '거북선은 3층 구조의 배였다.'), 'O', 1),
((SELECT id FROM quiz WHERE question = '이순신 장군은 거북선을 직접 설계했다.'), 'X', 1),
((SELECT id FROM quiz WHERE question = '거북선의 등에는 날카로운 칼이나 송곳이 꽂혀 있었다.'), 'O', 1),
((SELECT id FROM quiz WHERE question = '거북선은 임진왜란 때 처음으로 사용되었다.'), 'O', 1),
((SELECT id FROM quiz WHERE question = '거북선 안에는 휴식 공간이 있었다.'), 'X', 1);


-- 5. 2페이즈용 O/X 퀴즈 8개 생성 (boss_phase_id를 2로 직접 지정)
INSERT IGNORE INTO quiz (boss_phase_id, question, quiz_type, category, explanation, is_active, is_translatable)
VALUES
(2, '명량해전에서 조선의 배는 13척이었다.', 'OX', 'HISTORY', '난중일기에는 13척으로 기록되어 있습니다.', 1, 1),
(2, '이순신 장군은 명량해전에서 전사했다.', 'OX', 'HISTORY', '이순신 장군은 명량해전이 아닌, 노량해전에서 전사했습니다.', 1, 1),
(2, '명량해전은 임진왜란 3대 대첩 중 하나이다.', 'OX', 'HISTORY', '맞습니다. 한산도 대첩, 행주 대첩, 진주 대첩이 3대 대첩이며 명량해전도 이에 버금가는 위대한 승리입니다.', 1, 1),
(2, '"신에게는 아직 12척의 배가 남아있사옵니다"는 영화 명량의 명대사이다.', 'OX', 'HISTORY', '맞습니다. 이 명대사는 영화에서 큰 감동을 주었습니다.', 1, 1),
(2, '명량해협은 물살이 매우 느린 곳이다.', 'OX', 'HISTORY', '아닙니다. 명량해협은 조류가 매우 빠르기로 유명하며, 이순신 장군은 이를 전술에 활용했습니다.', 1, 1),
(2, '명량해전 당시 일본 수군의 배는 133척이었다.', 'OX', 'HISTORY', '맞습니다. 조선 수군은 13척의 배로 133척의 일본 수군을 맞서 싸웠습니다.', 1, 1),
(2, '명량해전의 승리로 조선은 제해권을 완전히 장악했다.', 'OX', 'HISTORY', '맞습니다. 이 승리로 일본의 보급로를 차단하고 전세를 역전시키는 발판을 마련했습니다.', 1, 1),
(2, '명량해전은 1597년에 일어났다.', 'OX', 'HISTORY', '맞습니다. 정유재란 당시인 1597년에 일어난 해전입니다.', 1, 1);

-- 2페이즈 퀴즈에 대한 정답(QuizChoice) 생성
INSERT IGNORE INTO quiz_choice (quiz_id, content, is_answer) VALUES
((SELECT id FROM quiz WHERE question = '명량해전에서 조선의 배는 13척이었다.'), 'O', 1),
((SELECT id FROM quiz WHERE question = '이순신 장군은 명량해전에서 전사했다.'), 'X', 1),
((SELECT id FROM quiz WHERE question = '명량해전은 임진왜란 3대 대첩 중 하나이다.'), 'O', 1),
((SELECT id FROM quiz WHERE question = '"신에게는 아직 12척의 배가 남아있사옵니다"는 영화 명량의 명대사이다.'), 'O', 1),
((SELECT id FROM quiz WHERE question = '명량해협은 물살이 매우 느린 곳이다.'), 'X', 1),
((SELECT id FROM quiz WHERE question = '명량해전 당시 일본 수군의 배는 133척이었다.'), 'O', 1),
((SELECT id FROM quiz WHERE question = '명량해전의 승리로 조선은 제해권을 완전히 장악했다.'), 'O', 1),
((SELECT id FROM quiz WHERE question = '명량해전은 1597년에 일어났다.'), 'O', 1);
