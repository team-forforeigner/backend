
INSERT INTO member (id, email, nickname, password, nationality, role, status, title, badge)
VALUES (1, 'test@example.com', '테스트유저', 'password123', 'South Korea', 'USER', 'ACTIVE', '초보 생존자', 'badge_url');

-- =================================================================
-- '날씨 - 벚꽃'
-- =================================================================

--[카테고리 및 시리즈 생성]--

-- 카테고리 생성
INSERT INTO category (category_id, category_title, category_description)
VALUES (1, '봄', '봄 -  축제 또는 황사에 대한 시리즈를 엮음');

-- 시리즈 생성
INSERT INTO series (series_id, category_id, title, series_description)
VALUES (1, 1, '벚꽃 축제 탐방기',
     JSON_ARRAY(
        '한국의 봄은 3월 말부터 4월 초까지 벚꽃이 절정을 이루는 시기야.',
        '한국에 자생하는 대표적인 벚꽃은 <strong>왕벚나무</strong>야.',
        '가장 유명한 벚꽃 축제는 <strong>진해 군항제</strong>와 <strong>여의도 윤중로 벚꽃축제</strong>야.',
        '도시와 자연이 어우러진 여의도, 해군 퍼레이드의 진해까지 다양해.'
        )
);
-- -- [에피소드 생성] -- --

-- 에피소드 1 생성
INSERT INTO episode (episode_id, series_id, order_series, episode_title, episode_content)
VALUES (1, 1, 1, '🌸 에피소드 1. 벚꽃 축제를 가보자!',
    JSON_ARRAY(
        '🌸 한국의 봄은 정말 아름다워!',
        '오늘은 벚꽃 축제가 열리는 날이야. 어느 축제에 갈까?',
        '',
        '🌸 여의도 벚꽃축제는 서울에서 열려서 접근성이 좋아.',
        '🌸 진해 군항제는 군악대 퍼레이드로 유명한 대규모 축제야!'
    )
);

-- 에피소드 2 생성
INSERT INTO episode (episode_id, series_id, order_series, episode_title, episode_content)
VALUES (2, 1, 2, '👍 에피소드 2. 좋아! 이제 축제에 가기 전 장소를 확인하고 준비물을 챙기자',
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

-- 에피소드 3 생성
INSERT INTO episode (episode_id, series_id, order_series, episode_title, episode_content)
VALUES (3, 1, 3, '🎉 에피소드 3. 드디어 축제 도착!',
    JSON_ARRAY(
        '👨‍👩‍👧‍👦 사람들이 진짜 많아! 다들 들떠 있어.',
        '🎺 진해에선 군악대 퍼레이드랑 멋진 야경이 있어.',
        '🚚 여의도에선 푸드트럭 음식이랑 거리 공연이 인기야.',
        '😌 꽃밭 근처에 앉아서 여유롭게 구경하는 것도 좋아.'
    )
);

-- 에피소드 4 생성
INSERT INTO episode (episode_id, series_id, order_series, episode_title, episode_content)
VALUES (4, 1, 4, '🌸 에피소드 4. 벚꽃의 기억',
    JSON_ARRAY(
        '✨ 아름다운 벚꽃도 구경하고,',
        '👏 축제에 대해 이제 마스터한 너 정말! 수고했어.'
    )
);

-- -- [선택지 생성 및 에피소드 연결] -- --

-- 에피소드 1의 선택지 (-> 에피소드 2)
INSERT INTO choice (episode_id, next_episode_id, choice_description) VALUES
(1, 2, JSON_OBJECT('text', '여의도 벚꽃축제')),
(1, 2, JSON_OBJECT('text', '진해 군항제'));

-- 에피소드 2의 선택지 (-> 에피소드 3)
INSERT INTO choice (episode_id, next_episode_id, choice_description) VALUES
(2, 3, JSON_OBJECT('text', '필요한 준비물을 체크하기')),
(2, 3, JSON_OBJECT('text', '귀찮으니까 난 가서 다 살래!'));

-- 8. 에피소드 3의 선택지 (-> 에피소드 4로 연결)
INSERT INTO choice (episode_id, next_episode_id, choice_description) VALUES
(3, 4, JSON_OBJECT('text', '진해: 군악대 퍼레이드, 야간 조명')),
(3, 4, JSON_OBJECT('text', '여의도: 푸드트럭, 거리 공연')),
(3, 4, JSON_OBJECT('text', '앉아서 꽃 구경하기'));

-- -- [스크립트 완료] -- --
-- SELECT '벚꽃 축제 탐방기 시리즈 데이터가 성공적으로 저장되었습니다.' AS message;

-- =================================================================
-- '병원'
-- =================================================================

--[카테고리 및 시리즈 생성]--

-- 카테고리 2번 '병원' 생성
INSERT INTO category (category_id, category_title, category_description)
VALUES (2, '병원', '병원 - 동네/대학병원, 응급상황에 대한 시리즈를 엮음');

-- 카테고리 2 - 시리즈 2번 '구급차' 생성
INSERT INTO series (series_id, category_id, title, series_description)
VALUES (2, 2, '구급차',
     JSON_ARRAY( '한국에서는 구급차 이용 자체는 무료야.','병원 소속 구급차나 의료 인력이 동승할 경우 추가 요금이 발생할 수 있어.', '구급대원에게 환자 상태를 정확히 말하기','병원까지 조용히 동행하며 협조하기')
);

-- 카테고리 2 - 시리즈 3번 '응급상황 - 119' 생성
INSERT INTO series (series_id, category_id, title, series_description)
VALUES (3, 2, '응급상황',
     JSON_ARRAY('신속한 신고가 생명을 구할 수 있어요.', '정확한 위치와 상황을 설명해야 해요.', '침착하게 응급처치를 시도해보세요.')
);

-- 카테고리 2 - 시리즈 4번 '동네병원' 생성
INSERT INTO series (series_id, category_id, title, series_description)
VALUES (4, 2, '동네병원',
     JSON_ARRAY('가까운 병원은 대기 시간이 짧고 접근이 쉬워요.', '건강보험증이나 신분증은 꼭 지참하세요.', '진료 후에는 약국에서 약을 받을 수 있어요.')
);

-- 카테고리 2 - 시리즈 5번 '대학병원' 생성
INSERT INTO series (series_id, category_id, title, series_description)
VALUES (5, 2, '대학병원',
     JSON_ARRAY('대학병원은 전문 진료과가 세분화되어 있어요.', '사전 예약이 필수인 경우가 많아요.', '접수, 대기, 진료, 수납 등 절차를 잘 확인하세요.')
);

-- 시리즈 2번 '구급차'의 에피소드(5, 6, 7, 8) 생성
INSERT INTO episode (episode_id, series_id, order_series, episode_title, episode_content) VALUES
(5, 2, 1, '🚑 에피소드 1. 구급차 호출 완료!', JSON_ARRAY('💨 119에 신고한 뒤, 구급차가 출동 중이야.','👋 도로에 나가서 손을 흔들면 구급차가 빨리 찾을 수 있어.','🚫 전화만 반복하면 혼선을 줄 수 있어.')),
(6, 2, 2, '🗣️ 에피소드 2. 환자 정보 전달!', JSON_ARRAY('🧑‍⚕️ 구급대원이 도착했어. 정확한 정보 전달이 필요해.', '📝 환자의 나이와 증상 그리고 상황을 설명하면 응급 처치에 큰 도움이 돼.',  '🤔 이름만 말하면 부족해.','🚫 아무 말도 하지 않으면 대응이 어려워.')),
(7, 2, 3, '🚑 에피소드 3. 병원 이동 중!', JSON_ARRAY('🏃‍♂️ 구급차 안, 상황이 긴박하지만 차분하게 행동해야 해.','🤫 의료진 요청에 따르며 조용히 동행하는 게 좋아.','❓ 질문이 많으면 처치를 방해할 수 있어.')),
(8, 2, 4, '🏥 에피소드 4. 병원 도착!', JSON_ARRAY('👍 구급차가 무사히 병원에 도착했어.','👏 신속하고 침착한 대응, 정말 잘했어!'));

-- '구급차' 에피소드의 선택지 생성
-- 에피소드 5의 선택지 (-> 에피소드 6)
INSERT INTO choice (episode_id, next_episode_id, choice_description) VALUES
(5, 6, JSON_OBJECT('text', '도로에 나가서 손을 흔든다')),
(5, 6, JSON_OBJECT('text', '전화를 반복하지 않는다'));

-- 에피소드 6의 선택지 (-> 에피소드 7)
INSERT INTO choice (episode_id, next_episode_id, choice_description) VALUES
(6, 7, JSON_OBJECT('text', '환자의 나이와 증상을 설명한다')),
(6, 7, JSON_OBJECT('text', '최대한 환자의 정보를 말한다'));

-- 에피소드 7의 선택지 (-> 에피소드 8)
INSERT INTO choice (episode_id, next_episode_id, choice_description) VALUES
(7, 8, JSON_OBJECT('text', '조용히 동행하며 의료진 요청에 따른다')),
(7, 8, JSON_OBJECT('text', '정말 필요한 질문만 한다'));

-- 시리즈 3번 '응급상황 - 119' 의 에피소드(9,10,11,12) 생성
 INSERT INTO episode (episode_id, series_id, order_series, episode_title, episode_content) VALUES
(9, 3, 1, '🚨 에피소드 1. 긴급 상황 발생!!', JSON_ARRAY('😱 친구가 갑자기 쓰러졌어! 당황하지 말고 침착하게 대처해야 해.','🚑 119에 즉시 신고하면 가장 빠르게 도움을 받을 수 있어.', '⏳ 인터넷 검색은 시간이 오래 걸릴 수 있어.', '🚫 그냥 기다리는 건 매우 위험해!')),
(10, 3, 2, '📞 에피소드 2. 신고 내용 전달!', JSON_ARRAY('☎️ 119에 연결되었어. 상황을 정확히 설명해야 해.', '📍 위치와 증상은 가장 중요한 정보야. (위치를 모른다면 모른다고 해! 위치 추적이 가능해)', '🗣️ 증상을 말하지 않으면 응급치료가 지연될 수 있어.', '💡 구급대원이 전화로 대처법을 말해주면서 출동할거야!')),
(11, 3, 3, '🩹 에피소드 3. 응급처치!', JSON_ARRAY('🤔 구급차가 도착하기 전까지 무엇을 해야 할까?','❤️ 기본적인 응급처치는 생명을 구할 수 있어',  '😨 아무것도 하지 않으면 상태가 악화될 수 있어.', '💬 구급대원에게 응급처치를 물어보자!')),
(12, 3, 4, '✅ 에피소드 4. 구조 완료!', JSON_ARRAY( '🙌 구급차가 무사히 도착했어! 빠른 판단이 큰 도움이 되었어.','👍 긴급 상황에서도 침착하게 잘 대처했네!'));

-- '응급상황 - 119' 에피소드의 선택지 생성
-- 에피소드 9 -->  10
INSERT INTO choice (episode_id, next_episode_id, choice_description) VALUES
(9, 10, JSON_OBJECT('text', '119에 바로 신고한다')),
(9, 10, JSON_OBJECT('text', '주변 사람들에게 119 신고를 부탁한다'));

-- 에피소드 10 --> 11
INSERT INTO choice (episode_id, next_episode_id, choice_description) VALUES
(10, 11, JSON_OBJECT('text', '위치와 증상을 설명한다')),
(10, 11, JSON_OBJECT('text', '상황을 자세히 설명한다')),
(10, 11, JSON_OBJECT('text', '침착하게 구급대원의 지시를 따른다'));

-- 에피소드 11 --> 12
INSERT INTO choice (episode_id, next_episode_id, choice_description) VALUES
(11, 12, JSON_OBJECT('text', '기본 응급처치 시도한다')),
(11, 12, JSON_OBJECT('text', '구급대원에게 응급처치를 물어본다'));


-- 시리즈 4번 '동네병원' 의 에피소드(13, 14, 15, 16) 생성
INSERT INTO episode (episode_id, series_id, order_series, episode_title, episode_content) VALUES
(13, 4, 1, '🤒 에피소드 1. 감기에 걸렸어!', JSON_ARRAY('🤧 콧물과 기침이 계속돼...',  '🏥 가까운 내과를 찾아가면 빠르고 편리한 진료를 받을 수 있어.', '💊 약국에서도 간단한 상담과 약 구입이 가능해.',  '🛌 집에서 푹 쉬는게 좋아.')),
(14, 4, 2, '🏥 에피소드 2. 아플 때는 병원에 가야해! 병원 방문을 예행 연습해보자!', JSON_ARRAY('📄 진료비와 필요한 서류를 준비해야 해.', '💳 여권 또는 외국인 등록증을 지참해야 해.',  '🤔 아무것도 안 챙기면 진료가 어려울 수도 있어.')),
(15, 4, 3, '👩‍⚕️ 에피소드 3. 접수와 진료!', JSON_ARRAY('🏥 병원에 도착했어!', '📝 문진표를 먼저 작성하면 진료가 더 정확해져.',   '💬 접수대에 궁금한 걸 물어보면 도움을 받을 수 있어.')),
(16, 4, 4, '✅ 에피소드 4. 진료 완료!', JSON_ARRAY('💊 진료도 끝나고 약 처방도 받았어!', '😌 이제 안심이 되네. 오늘 너무 수고했어.','❤️ 건강은 지킬수록 소중해!'));

-- '응급상황 - 119' 에피소드의 선택지 생성
-- 에피소드 13 -->  14
INSERT INTO choice (episode_id, next_episode_id, choice_description) VALUES
(13, 14, JSON_OBJECT('text', '가까운 내과 검색')),
(13, 14, JSON_OBJECT('text', '동네 약국에서 상담')),
(13, 14, JSON_OBJECT('text', '그냥 집에서 쉰다'));

-- 에피소드 14 --> 15
INSERT INTO choice (episode_id, next_episode_id, choice_description) VALUES
(14, 15, JSON_OBJECT('text', '여권과 외국인 등록증을 챙긴다')),
(14, 15, JSON_OBJECT('text', '진료비를 챙긴다')),
(14, 15, JSON_OBJECT('text', '번역기를 준비하여 미리 증상을 한국어로 준비한다'));

-- 에피소드 15 --> 16
INSERT INTO choice (episode_id, next_episode_id, choice_description) VALUES
(15, 16, JSON_OBJECT('text', '문진표를 작성한다')),
(15, 16, JSON_OBJECT('text', '접수대에 질문한다'));

-- 시리즈 5번 '대학병원' 의 에피소드(17,18,19,20) 생성
INSERT INTO episode (episode_id, series_id, order_series, episode_title, episode_content) VALUES
(17, 5, 1, '🏥 에피소드 1. 대학병원 예약하기!', JSON_ARRAY('🤔 복잡한 대학병원 진료, 어디서부터 시작해야 할까?', '💻 온라인 예약은 빠르고 편리해.', '📞 전화로 문의하면 친절한 설명을 들을 수 있어.')),
(18, 5, 2, '✅ 에피소드 2. 예약 성공!', JSON_ARRAY( '🎉 예약이 완료됐어! 이제 무엇을 준비하면 될까?',  '📄 (여권, 건강보험증, 있다면 의뢰서) 등이 필요할 수 있어.',   '🤔 아무것도 안 챙기면 진료에 어려움이 생길 수 있으니 미리 확인해봐.')),
(19, 5, 3, '🏥 에피소드 3. 병원 방문!', JSON_ARRAY('🏃‍♂️ 드디어 병원에 도착했어! 접수부터 시작해볼까?','👩‍⚕️ 안내 데스크에 물어보면 정확히 알려줘.', '🖥️ 키오스크는 빠르게 접수할 수 있어.',  '😵 하염없이 헤매면 진료 시간을 놓칠지도 몰라!')),
(20, 5, 4, '✅ 에피소드 4. 진료 완료!', JSON_ARRAY('🧾 진료도 받고 처방전과 영수증도 챙겼어!',  '👍 대학병원은 복잡하지만 한 번 경험하면 다음에는 쉬워!',  '💡 다음 진료도 똑똑하게 예약해봐!'));

-- '대학병원' 에피소드의 선택지 생성
-- 에피소드 17 -->  18
INSERT INTO choice (episode_id, next_episode_id, choice_description) VALUES
(17, 18, JSON_OBJECT('text', '온라인 예약하기')),
(17, 18, JSON_OBJECT('text', '전화로 문의하기'));

-- 에피소드 18 --> 19
INSERT INTO choice (episode_id, next_episode_id, choice_description) VALUES
(18, 19, JSON_OBJECT('text', '준비물을 잘 챙긴다')),
(18, 19, JSON_OBJECT('text', '헷갈리면 전화로 자세히 물어본다'));

-- 에피소드 19 --> 20
INSERT INTO choice (episode_id, next_episode_id, choice_description) VALUES
(19, 20, JSON_OBJECT('text', '안내 데스크에서 물어본다')),
(19, 20, JSON_OBJECT('text', '키오스크를 사용한다')),
(19, 20, JSON_OBJECT('text', '주변인들에게 도움을 요청한다'));

-- =================================================================
-- ''
-- =================================================================

--[카테고리 및 시리즈 생성]--

-- 카테고리 3번 '교통' 생성
INSERT INTO category (category_id, category_title, category_description)
VALUES (3, '교통', '교통에 대한 시리즈를 엮음');

-- 카테고리 3 - 시리즈 6번 '버스', 시리즈  7번 '카카오 택시', 시리즈 8번 '일회용 카드'
-- 시리즈 9번 '지하철 문의', 시리즈 10번 '기차', 시리즈 11번 '기차 예약'
-- 시리즈 12번 '교통카드', 시리즈 13번 '환승지도' 생성
INSERT INTO series (series_id, category_id, title, series_description)
VALUES (2, 2, '구급차',
     JSON_ARRAY( '한국에서는 구급차 이용 자체는 무료야.','병원 소속 구급차나 의료 인력이 동승할 경우 추가 요금이 발생할 수 있어.', '구급대원에게 환자 상태를 정확히 말하기','병원까지 조용히 동행하며 협조하기')
);