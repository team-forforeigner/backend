
INSERT INTO users (id, name, email) VALUES (1, '테스트유저', 'test@example.com');
INSERT INTO choices (id, content) VALUES (1, 'A 선택지');
INSERT INTO choices (id, content) VALUES (2, 'B 선택지');

-- =================================================================
-- '날씨 - 벚꽃'
-- =================================================================

--[카테고리 및 시리즈 생성]--

-- 카테고리 생성
INSERT INTO category (category_title, category_description)
VALUES ('봄', '봄 -  축제 또는 황사에 대한 시리즈를 엮음');
SET @cat_id = LAST_INSERT_ID();

-- 시리즈 생성
INSERT INTO series (category_id, title, series_description)
VALUES (@cat_id,
    '벚꽃 축제 탐방기',
     JSON_ARRAY(
        '한국의 봄은 3월 말부터 4월 초까지 벚꽃이 절정을 이루는 시기야.',
        '한국에 자생하는 대표적인 벚꽃은 <strong>왕벚나무</strong>야.',
        '가장 유명한 벚꽃 축제는 <strong>진해 군항제</strong>와 <strong>여의도 윤중로 벚꽃축제</strong>야.',
        '도시와 자연이 어우러진 여의도, 해군 퍼레이드의 진해까지 다양해.'
        )
);
SET @series_id = LAST_INSERT_ID();

-- -- [에피소드 생성] -- --

-- 에피소드 1 생성
INSERT INTO episode (series_id, order_series, episode_title, episode_content)
VALUES (
    @series_id,
    1,
    '🌸 에피소드 1. 벚꽃 축제를 가보자!',
    JSON_ARRAY(
        '🌸 한국의 봄은 정말 아름다워!',
        '오늘은 벚꽃 축제가 열리는 날이야. 어느 축제에 갈까?',
        '',
        '🌸 여의도 벚꽃축제는 서울에서 열려서 접근성이 좋아.',
        '🌸 진해 군항제는 군악대 퍼레이드로 유명한 대규모 축제야!'
    )
);
SET @ep1_id = LAST_INSERT_ID();

-- 에피소드 2 생성
INSERT INTO episode (series_id, order_series, episode_title, episode_content)
VALUES (
    @series_id,
    2,
    '👍 에피소드 2. 좋아! 이제 축제에 가기 전 장소를 확인하고 준비물을 챙기자',
    JSON_ARRAY(
        '🧭 진해 군항제: 경상남도 창원시 진해구 일대',
        '🧭 여의도 벚꽃축제: 서울 영등포구 여의도 윤중로',
        '',
        '🎒 준비물: 마실 물, 카메라, 돗자리, 지갑 등을 챙기면 편해.',
        '🏪 편의점이나 배달도 쓸 수 있으니까 너무 걱정 마!',
        '',
        '👍 필요한 준비물을 체크하면 준비가 완벽해질 거야.',
        '🤔 귀찮아서 안 챙기면 현장에서 조금 불편할 수도 있어.'
    )
);
SET @ep2_id = LAST_INSERT_ID();

-- 에피소드 3 생성
INSERT INTO episode (series_id, order_series, episode_title, episode_content)
VALUES (
    @series_id,
    3,
    '🎉 에피소드 3. 드디어 축제 도착!',
    JSON_ARRAY(
        '👨‍👩‍👧‍👦 사람들이 진짜 많아! 다들 들떠 있어.',
        '🎺 진해에선 군악대 퍼레이드랑 멋진 야경이 있어.',
        '🚚 여의도에선 푸드트럭 음식이랑 거리 공연이 인기야.',
        '😌 꽃밭 근처에 앉아서 여유롭게 구경하는 것도 좋아.'
    )
);
SET @ep3_id = LAST_INSERT_ID();

-- 에피소드 4 생성
INSERT INTO episode (series_id, order_series, episode_title, episode_content)
VALUES (
    @series_id,
    4,
    '🌸 에피소드 4. 벚꽃의 기억',
    JSON_ARRAY(
        '✨ 아름다운 벚꽃도 구경하고,',
        '👏 축제에 대해 이제 마스터한 너 정말! 수고했어.'
    )
);
SET @ep4_id = LAST_INSERT_ID();

-- -- [선택지 생성 및 에피소드 연결] -- --

-- 에피소드 1의 선택지 (-> 에피소드 2)
INSERT INTO choice (episode_id, next_episode_id, choice_description) VALUES
(@ep1_id, @ep2_id, JSON_OBJECT('text', '여의도 벚꽃축제')),
(@ep1_id, @ep2_id, JSON_OBJECT('text', '진해 군항제'));

-- 에피소드 2의 선택지 (-> 에피소드 3)
INSERT INTO choice (episode_id, next_episode_id, choice_description) VALUES
(@ep2_id, @ep3_id, JSON_OBJECT('text', '필요한 준비물을 체크하기')),
(@ep2_id, @ep3_id, JSON_OBJECT('text', '귀찮으니까 난 가서 다 살래!'));

-- 8. 에피소드 3의 선택지 (-> 에피소드 4로 연결)
INSERT INTO choice (episode_id, next_episode_id, choice_description) VALUES
(@ep3_id, @ep3_id, JSON_OBJECT('text', '진해: 군악대 퍼레이드, 야간 조명')),
(@ep3_id, @ep3_id, JSON_OBJECT('text', '여의도: 푸드트럭, 거리 공연')),
(@ep3_id, @ep3_id, JSON_OBJECT('text', '앉아서 꽃 구경하기'));

-- -- [스크립트 완료] -- --
SELECT '벚꽃 축제 탐방기 시리즈 데이터가 성공적으로 저장되었습니다.' AS message;

-- =================================================================
-- '병원 - '
-- =================================================================

--[카테고리 및 시리즈 생성]--

-- 카테고리 생성
INSERT INTO category (category_title, category_description)
VALUES ('병원', '병원 - 동네/대학병원, 응급상황에 대한 시리즈를 엮음');
SET @cat_id = LAST_INSERT_ID(); --또는 2--

-- 시리즈 생성
INSERT INTO series (category_id, title, series_description)
VALUES (@cat_id, --또는 2--
    '구급차',
     JSON_ARRAY(
        '한국에서는 구급차 이용 자체는 무료야.',
        '병원 소속 구급차나 의료 인력이 동승할 경우 추가 요금이 발생할 수 있어.',
        '구급대원에게 환자 상태를 정확히 말하기',
        '병원까지 조용히 동행하며 협조하기'
        )
);
SET @series_id = LAST_INSERT_ID(); --또는 2--

