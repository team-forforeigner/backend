
-- =================================================================
-- '날씨 - 벚꽃'
-- =================================================================

--[카테고리 및 시리즈 생성]--

-- 카테고리 생성
INSERT IGNORE INTO category (category_id, category_title, category_description)
VALUES (1, '봄', '봄 -  축제 또는 황사에 대한 시리즈를 엮음');

-- 시리즈 생성
INSERT IGNORE INTO series (series_id, category_id, title, series_description)
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
INSERT IGNORE INTO episode (episode_id, series_id, order_series, episode_title, episode_content)
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
INSERT IGNORE INTO episode (episode_id, series_id, order_series, episode_title, episode_content)
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
INSERT IGNORE INTO episode (episode_id, series_id, order_series, episode_title, episode_content)
VALUES (3, 1, 3, '🎉 에피소드 3. 드디어 축제 도착!',
    JSON_ARRAY(
        '👨‍👩‍👧‍👦 사람들이 진짜 많아! 다들 들떠 있어.',
        '🎺 진해에선 군악대 퍼레이드랑 멋진 야경이 있어.',
        '🚚 여의도에선 푸드트럭 음식이랑 거리 공연이 인기야.',
        '😌 꽃밭 근처에 앉아서 여유롭게 구경하는 것도 좋아.'
    )
);

-- 에피소드 4 생성
INSERT IGNORE INTO episode (episode_id, series_id, order_series, episode_title, episode_content)
VALUES (4, 1, 4, '🌸 에피소드 4. 벚꽃의 기억',
    JSON_ARRAY(
        '✨ 아름다운 벚꽃도 구경하고,',
        '👏 축제에 대해 이제 마스터한 너 정말! 수고했어.'
    )
);

-- -- [선택지 생성 및 에피소드 연결] -- --

-- 에피소드 1의 선택지 (-> 에피소드 2)
INSERT IGNORE INTO choice (episode_id, next_episode_id, choice_description) VALUES
(1, 2, JSON_OBJECT('text', '여의도 벚꽃축제')),
(1, 2, JSON_OBJECT('text', '진해 군항제'));

-- 에피소드 2의 선택지 (-> 에피소드 3)
INSERT IGNORE INTO choice (episode_id, next_episode_id, choice_description) VALUES
(2, 3, JSON_OBJECT('text', '필요한 준비물을 체크하기')),
(2, 3, JSON_OBJECT('text', '귀찮으니까 난 가서 다 살래!'));

-- 8. 에피소드 3의 선택지 (-> 에피소드 4로 연결)
INSERT IGNORE INTO choice (episode_id, next_episode_id, choice_description) VALUES
(3, 4, JSON_OBJECT('text', '진해: 군악대 퍼레이드, 야간 조명')),
(3, 4, JSON_OBJECT('text', '여의도: 푸드트럭, 거리 공연')),
(3, 4, JSON_OBJECT('text', '앉아서 꽃 구경하기'));

-- =================================================================
-- '병원'
-- =================================================================

--[카테고리 및 시리즈 생성]--

-- 카테고리 2번 '응급상황' , 카테고리 3번 '병원' 생성
INSERT IGNORE INTO category (category_id, category_title, category_description) VALUES
(2, '응급상황', '응급상황 - 시리즈를 엮음'),
(3, '병원', '병원 - 동네/대학병원에 대한 시리즈를 엮음');

-- 카테고리 2 - 시리즈 2번 '구급차' 생성
INSERT IGNORE INTO series (series_id, category_id, title, series_description)
VALUES (2, 2, '🚑 구급차 호출',
     JSON_ARRAY( '한국에서는 구급차 이용 자체는 무료야.','병원 소속 구급차나 의료 인력이 동승할 경우 추가 요금이 발생할 수 있어.', '구급대원에게 환자 상태를 정확히 말하기','병원까지 조용히 동행하며 협조하기')
);

-- 카테고리 2 - 시리즈 3번 '📞 119 신고하기' 생성
INSERT IGNORE INTO series (series_id, category_id, title, series_description)
VALUES (3, 2, '📞 119 신고하기',
     JSON_ARRAY('신속한 신고가 생명을 구할 수 있어요.', '정확한 위치와 상황을 설명해야 해요.', '침착하게 응급처치를 시도해보세요.')
);

-- 시리즈 2번 '🚑 구급차 호출'의 에피소드(5, 6, 7, 8) 생성
INSERT IGNORE INTO episode (episode_id, series_id, order_series, episode_title, episode_content) VALUES
(5, 2, 1, '🚑 에피소드 1. 구급차 호출 완료!', JSON_ARRAY('💨 119에 신고한 뒤, 구급차가 출동 중이야.','👋 도로에 나가서 손을 흔들면 구급차가 빨리 찾을 수 있어.','🚫 전화만 반복하면 혼선을 줄 수 있어.')),
(6, 2, 2, '🗣️ 에피소드 2. 환자 정보 전달!', JSON_ARRAY('🧑‍⚕️ 구급대원이 도착했어. 정확한 정보 전달이 필요해.', '📝 환자의 나이와 증상 그리고 상황을 설명하면 응급 처치에 큰 도움이 돼.',  '🤔 이름만 말하면 부족해.','🚫 아무 말도 하지 않으면 대응이 어려워.')),
(7, 2, 3, '🚑 에피소드 3. 병원 이동 중!', JSON_ARRAY('🏃‍♂️ 구급차 안, 상황이 긴박하지만 차분하게 행동해야 해.','🤫 의료진 요청에 따르며 조용히 동행하는 게 좋아.','❓ 질문이 많으면 처치를 방해할 수 있어.')),
(8, 2, 4, '🏥 에피소드 4. 병원 도착!', JSON_ARRAY('👍 구급차가 무사히 병원에 도착했어.','👏 신속하고 침착한 대응, 정말 잘했어!'));

-- '🚑 구급차 호출' 에피소드의 선택지 생성
-- 에피소드 5의 선택지 (-> 에피소드 6)
INSERT IGNORE INTO choice (episode_id, next_episode_id, choice_description) VALUES
(5, 6, JSON_OBJECT('text', '도로에 나가서 손을 흔든다')),
(5, 6, JSON_OBJECT('text', '전화를 반복하지 않는다'));

-- 에피소드 6의 선택지 (-> 에피소드 7)
INSERT IGNORE INTO choice (episode_id, next_episode_id, choice_description) VALUES
(6, 7, JSON_OBJECT('text', '환자의 나이와 증상을 설명한다')),
(6, 7, JSON_OBJECT('text', '최대한 환자의 정보를 말한다'));

-- 에피소드 7의 선택지 (-> 에피소드 8)
INSERT IGNORE INTO choice (episode_id, next_episode_id, choice_description) VALUES
(7, 8, JSON_OBJECT('text', '조용히 동행하며 의료진 요청에 따른다')),
(7, 8, JSON_OBJECT('text', '정말 필요한 질문만 한다'));

-- 시리즈 3번 '📞 119 신고하기' 의 에피소드(9,10,11,12) 생성
 INSERT IGNORE INTO episode (episode_id, series_id, order_series, episode_title, episode_content) VALUES
(9, 3, 1, '🚨 에피소드 1. 긴급 상황 발생!!', JSON_ARRAY('😱 친구가 갑자기 쓰러졌어! 당황하지 말고 침착하게 대처해야 해.','🚑 119에 즉시 신고하면 가장 빠르게 도움을 받을 수 있어.', '⏳ 인터넷 검색은 시간이 오래 걸릴 수 있어.', '🚫 그냥 기다리는 건 매우 위험해!')),
(10, 3, 2, '📞 에피소드 2. 신고 내용 전달!', JSON_ARRAY('☎️ 119에 연결되었어. 상황을 정확히 설명해야 해.', '📍 위치와 증상은 가장 중요한 정보야. (위치를 모른다면 모른다고 해! 위치 추적이 가능해)', '🗣️ 증상을 말하지 않으면 응급치료가 지연될 수 있어.', '💡 구급대원이 전화로 대처법을 말해주면서 출동할거야!')),
(11, 3, 3, '🩹 에피소드 3. 응급처치!', JSON_ARRAY('🤔 구급차가 도착하기 전까지 무엇을 해야 할까?','❤️ 기본적인 응급처치는 생명을 구할 수 있어',  '😨 아무것도 하지 않으면 상태가 악화될 수 있어.', '💬 구급대원에게 응급처치를 물어보자!')),
(12, 3, 4, '✅ 에피소드 4. 구조 완료!', JSON_ARRAY( '🙌 구급차가 무사히 도착했어! 빠른 판단이 큰 도움이 되었어.','👍 긴급 상황에서도 침착하게 잘 대처했네!'));

-- '📞 119 신고하기' 에피소드의 선택지 생성
-- 에피소드 9 -->  10
INSERT IGNORE INTO choice (episode_id, next_episode_id, choice_description) VALUES
(9, 10, JSON_OBJECT('text', '119에 바로 신고한다')),
(9, 10, JSON_OBJECT('text', '주변 사람들에게 119 신고를 부탁한다'));

-- 에피소드 10 --> 11
INSERT IGNORE INTO choice (episode_id, next_episode_id, choice_description) VALUES
(10, 11, JSON_OBJECT('text', '위치와 증상을 설명한다')),
(10, 11, JSON_OBJECT('text', '상황을 자세히 설명한다')),
(10, 11, JSON_OBJECT('text', '침착하게 구급대원의 지시를 따른다'));

-- 에피소드 11 --> 12
INSERT IGNORE INTO choice (episode_id, next_episode_id, choice_description) VALUES
(11, 12, JSON_OBJECT('text', '기본 응급처치 시도한다')),
(11, 12, JSON_OBJECT('text', '구급대원에게 응급처치를 물어본다'));

-- 카테고리 3 - 시리즈 4번 '🏥 지역 병원' 생성
INSERT IGNORE INTO series (series_id, category_id, title, series_description)
VALUES (4, 3, '🏥 지역 병원',
     JSON_ARRAY('가까운 병원은 대기 시간이 짧고 접근이 쉬워요.', '건강보험증이나 신분증은 꼭 지참하세요.', '진료 후에는 약국에서 약을 받을 수 있어요.')
);

-- 카테고리 3 - 시리즈 5번 '🏫 대학 병원' 생성
INSERT IGNORE INTO series (series_id, category_id, title, series_description)
VALUES (5, 3, '🏫 대학 병원',
     JSON_ARRAY('대학병원은 전문 진료과가 세분화되어 있어요.', '사전 예약이 필수인 경우가 많아요.', '접수, 대기, 진료, 수납 등 절차를 잘 확인하세요.')
);

-- 시리즈 4번 '🏥 지역 병원' 의 에피소드(13, 14, 15, 16) 생성
INSERT IGNORE INTO episode (episode_id, series_id, order_series, episode_title, episode_content) VALUES
(13, 4, 1, '🤒 에피소드 1. 감기에 걸렸어!', JSON_ARRAY('🤧 콧물과 기침이 계속돼...',  '🏥 가까운 내과를 찾아가면 빠르고 편리한 진료를 받을 수 있어.', '💊 약국에서도 간단한 상담과 약 구입이 가능해.',  '🛌 집에서 푹 쉬는게 좋아.')),
(14, 4, 2, '🏥 에피소드 2. 아플 때는 병원에 가야해! 병원 방문을 예행 연습해보자!', JSON_ARRAY('📄 진료비와 필요한 서류를 준비해야 해.', '💳 여권 또는 외국인 등록증을 지참해야 해.',  '🤔 아무것도 안 챙기면 진료가 어려울 수도 있어.')),
(15, 4, 3, '👩‍⚕️ 에피소드 3. 접수와 진료!', JSON_ARRAY('🏥 병원에 도착했어!', '📝 문진표를 먼저 작성하면 진료가 더 정확해져.',   '💬 접수대에 궁금한 걸 물어보면 도움을 받을 수 있어.')),
(16, 4, 4, '✅ 에피소드 4. 진료 완료!', JSON_ARRAY('💊 진료도 끝나고 약 처방도 받았어!', '😌 이제 안심이 되네. 오늘 너무 수고했어.','❤️ 건강은 지킬수록 소중해!'));

-- '🏥 지역 병원' 에피소드의 선택지 생성
-- 에피소드 13 -->  14
INSERT IGNORE INTO choice (episode_id, next_episode_id, choice_description) VALUES
(13, 14, JSON_OBJECT('text', '가까운 내과 검색')),
(13, 14, JSON_OBJECT('text', '동네 약국에서 상담')),
(13, 14, JSON_OBJECT('text', '그냥 집에서 쉰다'));

-- 에피소드 14 --> 15
INSERT IGNORE INTO choice (episode_id, next_episode_id, choice_description) VALUES
(14, 15, JSON_OBJECT('text', '여권과 외국인 등록증을 챙긴다')),
(14, 15, JSON_OBJECT('text', '진료비를 챙긴다')),
(14, 15, JSON_OBJECT('text', '번역기를 준비하여 미리 증상을 한국어로 준비한다'));

-- 에피소드 15 --> 16
INSERT IGNORE INTO choice (episode_id, next_episode_id, choice_description) VALUES
(15, 16, JSON_OBJECT('text', '문진표를 작성한다')),
(15, 16, JSON_OBJECT('text', '접수대에 질문한다'));

-- 시리즈 5번 '🏫 대학 병원' 의 에피소드(17,18,19,20) 생성
INSERT IGNORE INTO episode (episode_id, series_id, order_series, episode_title, episode_content) VALUES
(17, 5, 1, '🏥 에피소드 1. 대학병원 예약하기!', JSON_ARRAY('🤔 복잡한 대학병원 진료, 어디서부터 시작해야 할까?', '💻 온라인 예약은 빠르고 편리해.', '📞 전화로 문의하면 친절한 설명을 들을 수 있어.')),
(18, 5, 2, '✅ 에피소드 2. 예약 성공!', JSON_ARRAY( '🎉 예약이 완료됐어! 이제 무엇을 준비하면 될까?',  '📄 (여권, 건강보험증, 있다면 의뢰서) 등이 필요할 수 있어.',   '🤔 아무것도 안 챙기면 진료에 어려움이 생길 수 있으니 미리 확인해봐.')),
(19, 5, 3, '🏥 에피소드 3. 병원 방문!', JSON_ARRAY('🏃‍♂️ 드디어 병원에 도착했어! 접수부터 시작해볼까?','👩‍⚕️ 안내 데스크에 물어보면 정확히 알려줘.', '🖥️ 키오스크는 빠르게 접수할 수 있어.',  '😵 하염없이 헤매면 진료 시간을 놓칠지도 몰라!')),
(20, 5, 4, '✅ 에피소드 4. 진료 완료!', JSON_ARRAY('🧾 진료도 받고 처방전과 영수증도 챙겼어!',  '👍 대학병원은 복잡하지만 한 번 경험하면 다음에는 쉬워!',  '💡 다음 진료도 똑똑하게 예약해봐!'));

-- '🏫 대학 병원' 에피소드의 선택지 생성
-- 에피소드 17 -->  18
INSERT IGNORE INTO choice (episode_id, next_episode_id, choice_description) VALUES
(17, 18, JSON_OBJECT('text', '온라인 예약하기')),
(17, 18, JSON_OBJECT('text', '전화로 문의하기'));

-- 에피소드 18 --> 19
INSERT IGNORE INTO choice (episode_id, next_episode_id, choice_description) VALUES
(18, 19, JSON_OBJECT('text', '준비물을 잘 챙긴다')),
(18, 19, JSON_OBJECT('text', '헷갈리면 전화로 자세히 물어본다'));

-- 에피소드 19 --> 20
INSERT IGNORE INTO choice (episode_id, next_episode_id, choice_description) VALUES
(19, 20, JSON_OBJECT('text', '안내 데스크에서 물어본다')),
(19, 20, JSON_OBJECT('text', '키오스크를 사용한다')),
(19, 20, JSON_OBJECT('text', '주변인들에게 도움을 요청한다'));

-- =================================================================
-- 교통
-- =================================================================

--[카테고리 및 시리즈 생성]--

-- 카테고리 4번 '한국의 대중교통' , 카테고리 5번 '지하철' , 카테고리 6번 '택시', 카테고리 7번 '기차'
INSERT IGNORE INTO category (category_id, category_title, category_description) VALUES
(4, '한국의 대중교통', '교통카드 구매 등을 엮음'),
(5, '지하철 이용 꿀팁', '지하철 관련 시리즈를 엮음'),
(6, '택시', '택시 관련 시리즈를 엮음'),
(7, '기차', '기차 관련 시리즈를 엮음');

-- 카테고리 4 - 시리즈 6번 'TransportCard', 시리즈  7번 'TransportMap',
-- 카테고리 5 - 시리즈 8번 'OneTimeCard - 일회용 카드 구매', 시리즈 9번 'SubwayComplaint - 민원 제기'
-- 카테고리 6 - 시리즈 10번 'Kakao - 택시'
-- 카테고리 7 - 시리즈 11번 '예매하기', 시리즈 12번 '이용 꿀팁' 생성
INSERT IGNORE INTO series (series_id, category_id, title, series_description) VALUES
(6, 4, '💳 교통카드', JSON_ARRAY('교통카드는 지하철 역 자동판매기, 편의점(GS25, CU, 7-Eleven 등)에서 구입할 수 있어.',
'충전은 지하철 역, 편의점, 교통카드 앱(T-money Pay 등)에서도 가능해.', '한 장의 교통카드로 지하철, 버스, 공항버스, 일부 택시, 코레일까지 이용할 수 있어.',
'분실 대비로 모바일 교통카드를 사용하는 것도 좋아.')),
(7, 4, '🗺️ 지도 앱', JSON_ARRAY('카카오맵, 네이버지도는 실시간 교통, 도착 시간, 혼잡도 정보를 제공해.',
'목적지를 입력하면 환승, 예상 소요 시간, 도보 거리, 최적 경로까지 자동 계산돼.', '길을 잃었을 땐 현재 위치 공유로 친구에게 위치를 보내거나 목적지를 재설정하면 돼.',
'한국에서는 건물명 검색이 더 정확할 때가 많아. “OO마트”처럼 입력해봐.')),
(8, 5, '💳 1회용 카드 구매', JSON_ARRAY('1회용 교통카드는 지하철역 내 무인 발매기에서 쉽게 구매할 수 있어.', '출발역과 도착역을 선택하고 요금을 지불하면 발급돼.',
'구매 시에는 운임 요금 + 보증금 500원이 함께 결제돼.', ' 보증금은 도착역 반환기기에서 돌려받을 수 있어.' , '한 번 사용하면 재사용이 불가능하고, 분실 시 환불도 안 돼.',
'사용 후 꼭 반환기를 이용해 보증금을 돌려받자.', '이 카드는 지하철 전용이며 버스에서는 사용할 수 없어.' )),
(9, 5, '📮 민원 제기', JSON_ARRAY('서울교통공사 앱 또는 대표 전화(1577-1234)</strong>로 신고 가능.', '앱 신고 시 사진·위치·시간을 첨부하면 처리 속도가 빨라짐.',
'혼자 어려우면 주변 승객들과 상황 공유 후 협조 요청.', '신고 후 앱에서 결과 확인 가능, 보통 24시간 내 피드백 제공.')),
(10, 6, '🚕 카카오 택시', JSON_ARRAY('출발지와 목적지를 입력하면 택시 호출 가능, 현재 위치 자동 인식.', '일반·블랙·대형 밴 등 차량 선택 가능, 도착까지 거리·시간 실시간 확인.', '앱에 카드/간편결제 등록 시 자동 결제.',
'공항·혼잡 지역은 배차 지연 가능, 카카오T 블루로 빠른 배차 가능.')),
(11, 4, '🎫 예매하기', JSON_ARRAY('코레일톡 앱 또는 무인 발권기 활용', '예매는 출발 1개월 전부터 가능', '성수기에는 미리 예약 필수!')),
(12, 4, '🚄 이용 꿀팁', JSON_ARRAY('짐은 선반에! 복도에 놓지 않기', '조용한 칸에서는 통화 자제', '음식은 깨끗하게 정리'));

-- 시리즈 6번 '💳 교통카드' 의 에피소드(21,22,23) 생성
INSERT IGNORE INTO episode (episode_id, series_id, order_series, episode_title, episode_content) VALUES
(21, 6, 1, '🎟️ 에피소드 1. 교통카드를 처음 사는 날', JSON_ARRAY('📍 교통카드는 버스, 지하철 모두 이용할 수 있어!', '🚇 역에서 바로 구매 가능하고',  '🏪 편의점에서도 쉽게 구할 수 있어', '📦 온라인 신청은 시간이 조금 걸리지만 편리해')),
-- 지하철
(22, 6, 2, '💰 에피소드 2. 충전이 필요할 때!', JSON_ARRAY('💡 교통카드는 미리 충전해야 해!',  '🔌 지하철 역 자동 충전기: 가장 빠르고 정확해', '🏪 편의점 충전: CU나 GS25 등 대부분 가능해', '📱 앱 충전: 모바일로 쉽게 해결!')),
(23, 6, 3, '에피소드 3. 교통카드 에피소드 완료', JSON_ARRAY('교통카드를 잘 사용했어요. 당신의 선택은 어땠을까요?')),
--편의점
(24, 6, 2, '💰 에피소드 2. 충전이 필요할 때!', JSON_ARRAY('💡 교통카드는 미리 충전해야 해!',  '🔌 지하철 역 자동 충전기: 가장 빠르고 정확해', '🏪 편의점 충전: CU나 GS25 등 대부분 가능해', '📱 앱 충전: 모바일로 쉽게 해결!')),
--온라인
(25, 6, 2, '💰 에피소드 2. 충전이 필요할 때!', JSON_ARRAY('💡 교통카드는 미리 충전해야 해!',  '🔌 지하철 역 자동 충전기: 가장 빠르고 정확해', '🏪 편의점 충전: CU나 GS25 등 대부분 가능해', '📱 앱 충전: 모바일로 쉽게 해결!'));

-- 교통카드 에피소드 선택지 추가
INSERT IGNORE INTO choice (episode_id, next_episode_id, choice_description) VALUES
(21, 22, JSON_OBJECT('text', '지하철 역에서 구매하기')), (21, 24, JSON_OBJECT('text', '편의점에서 구매하기')), (21,25,JSON_OBJECT('text','온라인으로 신청하기')),
(22, 23, JSON_OBJECT('text', '지하철 역 자동 충전기')), (22, 23, JSON_OBJECT('text', '편의점에서 충전')), (22,23,JSON_OBJECT('text', '앱으로 충전')),
(24, 23, JSON_OBJECT('text', '지하철 역 자동 충전기')), (24, 23, JSON_OBJECT('text', '편의점에서 충전')), (24,23,JSON_OBJECT('text', '앱으로 충전')),
(25, 23, JSON_OBJECT('text', '지하철 역 자동 충전기')), (25, 23, JSON_OBJECT('text', '편의점에서 충전')), (25,23,JSON_OBJECT('text', '앱으로 충전'));

-- 시리즈 7번 '🗺️ 지도 앱' 의 에피소드 (26, 27, 28, 29) 생성
INSERT IGNORE INTO episode (episode_id, series_id, order_series, episode_title, episode_content) VALUES
(26, 7, 1, '🚉 에피소드 1. 목적지까지 어떻게 가지?', JSON_ARRAY('오늘은 낯선 장소로 가야 해! 길을 잘 몰라서 걱정돼. 😥', '📲 지도 앱을 쓸까? 아니면 지하철 노선도로 충분할까?', '🤔 아니면 그냥 감에 맡기고 가볼까...?', '가장 좋은 길 찾기 방법을 골라보자!')),
-- 1: 네이버 지도
(27, 7, 2, '📍 에피소드 2. 목적지 검색!', JSON_ARRAY('📲 네이버 지도 앱을 켜고 목적지를 입력했어!', '🚉 지하철, 버스 환승 정보는 물론이고', '⏱️ 실시간 도착 시간까지 확인 가능해서 완전 편리해.', '⭐ 낯선 곳에서도 자신감이 생겼어!')),
(28, 7, 3, '🚏 에피소드 3. 도착지까지 이동!', JSON_ARRAY('🗺️ 앱 안내를 따라 환승도 잘하고,', '🚶 도보 거리까지 표시돼서 헤맬 틈이 없었어.', '🎧 음성 안내 기능까지 켜니까 두 손이 자유로워서 더 좋았어!')),
-- 2: 지하철 노선도
(29, 7, 2, '📍 에피소드 2. 노선도 분석 중...', JSON_ARRAY('🗺️ 지하철 노선도를 보며 경로를 분석 중이야.', '❌ 하지만 실시간 도착 정보가 없어서 예상이 어렵고', '🔁 환승역 계산도 헷갈려서 살짝 불안해...')),
(30, 7, 3, '🚏 에피소드 3. 길을 잃었어요!', JSON_ARRAY('❗ 예상과 달리 목적지 역을 지나쳐 버렸어!', '📉 환승 타이밍도 놓쳤고, 다시 돌아가려니 시간이 꽤 걸려.', '😓 계획대로 되지 않아서 아쉽다...')),
-- 3: 운에 맡기기
(31, 7, 2, '🌀 에피소드 2. 목적지와 멀어졌어!', JSON_ARRAY('😵 어딘지도 모르겠고, 표지판도 안 보여.', '', '📱 게다가 휴대폰 배터리도 얼마 안 남았어.', '🚶 계속 걷긴 했지만 방향이 맞는지 모르겠어...')),
(32, 7, 3, '🗺️ 에피소드 3. 다시 시도!', JSON_ARRAY('⏰ 겨우 목적지 근처에 도착했지만', '😮 일정은 이미 늦어버렸고, 정신적으로도 지쳤어.', '📓 다음부턴 똑똑하게 준비하고 싶어!')),
--4. 엔딩
(33, 7, 4, '에피소드 4. END', JSON_ARRAY('엔딩'));
--  선택지 생성
INSERT IGNORE INTO choice (episode_id, next_episode_id, choice_description) VALUES
(26, 27, JSON_OBJECT('text', '네이버 지도 앱을 사용하기')), (26, 29, JSON_OBJECT('text', '지하철 노선도만 보기')), (26, 31, JSON_OBJECT('text','길을 헤매다 운에 맡기기')),
(27, 28, JSON_OBJECT('text', '목적지 입력하기')), (27, 28, JSON_OBJECT('text', '실시간 도착 정보 확인하기')),
(29, 30, JSON_OBJECT('text', '종이 지도로 경로를 그리기')),  (29, 30, JSON_OBJECT('text', '환승역만 집중해서 외우기')),
(31, 32, JSON_OBJECT('text', '사람들에게 물어보기')), (31, 32, JSON_OBJECT('text', '택시를 부르기')),
(28, 33, JSON_OBJECT('text', '음성 안내를 키기')), (28, 33, JSON_OBJECT('text', '도보 길찾기를 사용하기')),
(30, 33, JSON_OBJECT('text', '역무원에게 물어보기')), (30, 33, JSON_OBJECT('text', '지도 앱을 다운받기')),
(32, 33, JSON_OBJECT('text', '다음부터는 앱을 쓰자 다짐하기')), (32, 33, JSON_OBJECT('text', '경로를 다시 메모해두기'));

-- 시리즈 8번 '💳 1회용 카드 구매' 의 에피소드 (34, 35, 36, 37) 생성
INSERT IGNORE INTO episode (episode_id, series_id, order_series, episode_title, episode_content) VALUES
(34, 8, 1, '🚇 에피소드 1. 지하철을 처음 탔어요!', JSON_ARRAY('1회용 교통카드를 구매하려면 어떻게 해야 할까?', '💳 무인 발매기 이용 가능','🧍 역무실에서 도움 받기', '🚫 그냥 개찰구 들어가면...? 위험!')),
-- 1: 무인 발매기
(35, 8, 2, '📍 에피소드 2. 보증금 반환은 어떻게?', JSON_ARRAY('💳 카드를 찍었더니 보증금 500원이 포함되어 있었어!', '🔁 도착역에서 반환기 사용 가능','😮 그냥 집에 가면 보증금 못 받아')),
-- 2: 역사 문의
(36, 8, 2, '📍 에피소드 2. 무사히 교통카드 구매 완료! 사용 후 보증금 반환은 어떻게?', JSON_ARRAY('👮‍♂️ 역무원이 1회용 교통카드 구매 방법을 친절히 설명해줬어!',  '🔁 도착역에서 보증금 반환기 이용 가능해', '🤷‍♂️ 그냥 귀찮아하면 돈 손해')),
-- 엔딩
(37, 8, 3, '🚏 에피소드 3. END', JSON_ARRAY('엔딩'));
-- 선택지 생성
INSERT IGNORE INTO choice (episode_id, next_episode_id, choice_description) VALUES
(34, 35, JSON_OBJECT('text', '무인 발매기에서 카드 구매하기')), (34, 36, JSON_OBJECT('text', '역무실에 문의하기')),
(35, 37, JSON_OBJECT('text', '도착역에서 반환기 이용하기')), (35, 37, JSON_OBJECT('text', '역무원에게 도움 요청하기')),
(36, 37, JSON_OBJECT('text', '도착역에서 반환기 이용하기')), (36, 37, JSON_OBJECT('text', '역무원에게 도움 요청하기'));

-- 시리즈 9번 '📮 민원 제기' 의 에피소드 (36, 37, 38, 39 -> 38, 39, 40, 41,, 42) 생성
INSERT IGNORE INTO episode (episode_id, series_id, order_series, episode_title, episode_content) VALUES
(38, 9, 1, '🚇 에피소드 1. 지하철에서 이상한 사람을 봤어!', JSON_ARRAY('지하철 안에서 누군가 불쾌한 행동을 하고 있어. 😟', '큰 소리는 아니지만 주변이 불편해하는 게 느껴져.',
'이럴 땐 어떻게 해야 할까?', '📲 서울교통공사 앱 신고',  '👥 주변 사람과 함께 행동', '😶 무시하면 변화 없을 수 있음...')),
-- 1: 지하철 민원 앱에 신고하기
(39, 9, 2, '📞 에피소드 2. 민원 접수 후', JSON_ARRAY('신고가 정상적으로 접수되었어. ✅', '📲 앱으로 접수하면 문자 알림이나 푸시 메시지로 결과를 알려줘.',  '📞 전화를 한 번 더 하면 빠른 조치가 이루어질 수도 있어',
'🕰️ 그냥 기다리기만 하면 느릴 수 있으니, 상황을 지켜보며 재신고도 고려하자.')),
-- 2: 주변 사람에게 말하기
(40, 9, 2, '👥 에피소드 2. 주변과의 소통', JSON_ARRAY('용기 내서 옆 사람에게 상황을 알렸어.', '🙆‍♀️ 다른 사람들도 같은 생각이었고, 함께 신고하기로 했어.',  '💬 누군가 나서주면 따라오는 경우가 많지.',
'📞 그래도 공식 채널 신고는 꼭 필요해!')),
-- 3: 그냥 모른 척하기
(41, 9, 2, '😶 에피소드 2. 아무 대응도 하지 않으면?', JSON_ARRAY('모른 척했더니 아무 일도 바뀌지 않았어.',  '😥 그 사람의 행동은 계속되고 누군가는 불편해하고 있어.',
'💡 지금이라도 신고할까?')),
-- 엔딩
(42, 9, 3, '🚏 에피소드 3. END', JSON_ARRAY('엔딩'));

-- 선택지 생성
INSERT IGNORE INTO choice (episode_id, next_episode_id, choice_description) VALUES
(38, 39, JSON_OBJECT('text', '지하철 민원 앱에 신고하기')), (38, 40, JSON_OBJECT('text', '주변 사람에게 말하기')), (38, 41, JSON_OBJECT('text', '그냥 모른 척하기')),
(39, 42, JSON_OBJECT('text', '관리자 대응을 기다리기')), (39, 42, JSON_OBJECT('text', '다시 한 번 전화로 신고하기')),
(40, 42, JSON_OBJECT('text', '함께 신고하기')), (40, 42, JSON_OBJECT('text', '함께 상황을 해결해보기')),
(41, 42, JSON_OBJECT('text', '지금이라도 신고하기')), (41, 42, JSON_OBJECT('text', '주변인에게 도움 요청하기'));

-- 시리즈 10번 '🚕 카카오 택시' 에피소드 생성 (41, 42, 43, 44 -> 43, 44, 45, 46, 47, 48)
INSERT IGNORE INTO episode (episode_id, series_id, order_series, episode_title, episode_content) VALUES
(43, 10, 1, '🚖 에피소드 1. 목적지 설정', JSON_ARRAY('처음으로 카카오T 앱을 사용해서 택시를 불러보려고 해.', '📍 먼저 도착할 장소를 입력하고,',  '🚗 그 다음 어떤 행동을 할까?',
'차량 종류를 골라서 호출을 진행할 수도 있고,', '주소가 정확한지 다시 확인하고 수정할 수도 있어.')),
-- 1: '차량 유형 선택하기'
(44, 10, 2, '🚕 에피소드 2. 택시 호출 성공!', JSON_ARRAY('🚗 일반 택시를 선택하고 호출을 눌렀어.', '앱 화면에서 기사님 정보와 도착 예정 시간이 표시돼.',
'🚘 차량이 이동 중이고, 실시간으로 지도에서 위치를 확인할 수 있어.', '📞 필요하면 채팅이나 전화로 기사님과 연락도 가능해.')),
(45, 10, 3, '🛬 에피소드 3. 목적지 도착!', JSON_ARRAY('택시를 타고 무사히 목적지에 도착했어.', '💳 결제는 자동으로 완료되었고, 영수증도 앱에 저장돼.',
'👍 기사님 서비스는 어땠어?', '리뷰를 남길 수도 있어!')),
-- 2: '목적지를 다시 입력하기'
(46, 10, 2, '📍 에피소드 2. 주소 재확인',JSON_ARRAY('입력했던 목적지 주소가 잘못된 것 같아서 다시 입력했어.',  '정확한 건물명이나 도로명 주소를 입력하면 더 빠르게 배차돼.',
'이제 다음 단계로 넘어가보자!')),
(47, 10, 3, '🛬 에피소드 3. 목적지 도착!',JSON_ARRAY('택시를 타고 무사히 목적지에 도착했어.', '💳 결제는 자동으로 완료되었고, 영수증도 앱에 저장돼.',
'👍 기사님 서비스는 어땠어?', '리뷰를 남길 수도 있어!')),
-- 3: END
(48, 10, 4, '🚏 에피소드 4. END', JSON_ARRAY('엔딩'));

-- 선택지 생성
INSERT IGNORE INTO choice (episode_id, next_episode_id, choice_description) VALUES
(43, 44, JSON_OBJECT('text', '차량 유형 선택하기')), (43, 46, JSON_OBJECT('text', '목적지를 다시 입력하기')),
(44, 45, JSON_OBJECT('text', '차량 도착까지 기다리기')), (44, 45, JSON_OBJECT('text', '기사에게 메시지를 보내기')),
(46, 47, JSON_OBJECT('text', '차량 유형 선택하기')), (46, 47, JSON_OBJECT('text', '배차를 기다리기')),
(45, 48, JSON_OBJECT('text', '리뷰를 남기기')), (47, 48, JSON_OBJECT('text', '리뷰를 남기기')),
(45, 48, JSON_OBJECT('text', '리뷰 없이 종료하기')), (47, 48, JSON_OBJECT('text', '리뷰 없이 종료하기'));

-- 시리즈 11번 '🎫 예매하기' 에피소드 생성 (45, 46, 47, 48 ... 52) TODO: 번호 수정해야 하는데 시간이 너무 늦어서 나중에... ㅠㅠ
INSERT IGNORE INTO episode (episode_id, series_id, order_series, episode_title, episode_content) VALUES
(49, 11, 1, '🚄 에피소드 1. 기차표를 예매하자!', JSON_ARRAY('여행을 떠나기 위해 기차표를 예매하려고 해!','📱 코레일톡 앱: 스마트폰으로 간편하게 예매', '🏢 역 창구: 직접 가서 직원에게 예매',
 '🖥️ 무인 발권기: 기계로 스스로 예매 가능', '어떤 방법으로 예매할까?')),
 -- 1: 코레일톡 앱 사용하기
(50, 11, 2, '📱 에피소드 2. 앱으로 예매 중!', JSON_ARRAY('📲 앱에서 원하는 기차 시간과 좌석을 고를 수 있어.',' 📅 출발일 선택하고, 🚉 노선과 시간 체크도 잊지 마', '✅ 어떤 걸 먼저 할까?')),
(51, 11, 3, '✅ 에피소드 3. 예매 완료!', JSON_ARRAY('📧 모바일 티켓이 도착했어!',  '🚉 기차 시간에 맞춰 역으로 가자.',  '기차 타기 전에 뭐 할까?')),
 -- 2: 역 창구 방문하기
(52, 11, 2, '👨‍💼 에피소드 2. 창구에서 예매 중', JSON_ARRAY('👋 역무원에게 행선지를 말했어.', '📅 출발 날짜, 시간도 잘 설명했지!', '✅ 다음엔 어떻게 할까?')),
(53, 11, 3, '✅ 에피소드 3. 표 수령 완료!', JSON_ARRAY('🎫 종이 티켓을 받았어! 이제 플랫폼으로 향하면 돼.', '기차 타기 전에 뭐 할까?')),
 -- 3: 무인 발권기 이용하기
(54, 11, 2, '🤖 에피소드 2. 기계 앞에서 고르기', JSON_ARRAY('🧾 화면을 보며 날짜와 시간, 좌석을 선택 중이야.',
'처음 써보는 기계라 조금 낯설지만 괜찮아!', '✅ 무엇부터 해볼까?')),
(55, 11, 3, '✅ 에피소드 3. 티켓 출력 완료!', JSON_ARRAY('🖨️ 티켓이 인쇄됐어! 미션 성공!',   '기차 출발까지 시간 여유가 있어. 무얼 할까?')),
 --END
(56, 11, 4, '🚏 에피소드 4. END', JSON_ARRAY('엔딩'));
--
---- 선택지 생성
INSERT IGNORE INTO choice (episode_id, next_episode_id, choice_description) VALUES
(49, 50, JSON_OBJECT('text', '코레일톡 앱 사용하기')), (49, 52, JSON_OBJECT('text', '역 창구 방문하기')),
(49, 54, JSON_OBJECT('text', '무인 발권기 이용하기')), (50, 51, JSON_OBJECT('text', '시간표 먼저 확인')),
(50, 51, JSON_OBJECT('text', '좌석 먼저 고르기')), (52, 53, JSON_OBJECT('text', '시간표 확인하기')),
(52, 53, JSON_OBJECT('text', '좌석 위치 묻기')), (54, 55, JSON_OBJECT('text', '노선 먼저 확인하기')),
(54, 55, JSON_OBJECT('text', '좌석부터 고르기')), (51, 56, JSON_OBJECT('text', '플랫폼 미리 찾기')),
(51, 56, JSON_OBJECT('text', '간식 사기')), (51, 56, JSON_OBJECT('text', '화장실 가기')),
(53, 56, JSON_OBJECT('text', '간식 사기')),  (53, 56, JSON_OBJECT('text', '화장실 가기')),
(53, 56, JSON_OBJECT('text', '기차 사진 찍기')), (55, 56, JSON_OBJECT('text', '간식 사기')),
(55, 56, JSON_OBJECT('text', '자리에서 대기')), (55, 56, JSON_OBJECT('text', '기차 사진 찍기'));
--
---- 시리즈 12번 '🚄 이용 꿀팁' 에피소드 생성 (53 ... 60) (=> 57,58,59,60,61...)
INSERT IGNORE INTO episode (episode_id, series_id, order_series, episode_title, episode_content) VALUES
(57, 12, 1, '🚄 에피소드 1. 기차 이용 꿀팁 시리즈!', JSON_ARRAY('기차를 더 편리하게 타는 꿀팁 알려줄게!', '🛋️ 기차 안에서 할 일',  '🏁 내릴 때 주의할 점',  '어떤 주제부터 볼까?')),
-- 1: 기차 타기 전 준비하기
(58, 12, 2, '🎒 에피소드 2. 출발 전 준비물 체크', JSON_ARRAY('🚉 기차 타기 전에 놓치기 쉬운 준비물 체크!',  '🎫 티켓과 신분증은 물론, 핸드폰 충전 상태 확인했어?',
'🔋 보조배터리 없으면 장거리 여행 중 곤란해질 수 있어.', '🍞 간식은 기차 안에서 식사 대용으로도 좋아.',  '✅ 자, 가장 먼저 챙길 건 뭐야?')),
(59, 12, 3, '🏃 에피소드 3. 기차역에 도착!', JSON_ARRAY('⏰ 출발 20분 전 도착! 여유롭지만 할 게 많아.',  '🚻 화장실은 지금 가는 게 좋아, 기차 안은 복잡할 수 있거든.',
'🧭 플랫폼 위치도 미리 확인하면 당황하지 않아.', '🛒 편의점에서 물이나 민트도 유용할 수 있어!')),
-- 2: 기차 안에서 할 일하기
(60, 12, 2, '🎧 에피소드 2. 자리에 앉았어!', JSON_ARRAY('🚅 자리에 앉고 나면 한숨 돌릴 시간.', '🎒 짐은 선반에 잘 올렸어?',  '🎶 이어폰은 꼭 착용해서 다른 승객에게 방해되지 않게!','🌄 창밖 풍경도 놓치지 말고.')),
(61, 12, 3, '📵 에피소드 3. 조용한 기차, 조용한 배려', JSON_ARRAY('🤫 기차는 대부분 조용한 분위기야. 통화는 꼭 짧게, 조용히!', '🍱 간식 포장도 깨끗이 정리하고',
 '👩‍👩‍👧‍👦 다른 승객이 탑승할 수 있도록 짐은 통로에 두지 않기!')),
-- 3: 기차 내릴 때 주의하기
(62, 12, 2, '📍 에피소드 2. 도착 10분 전 알림!', JSON_ARRAY('📣 곧 목적지 도착! 지금이 짐 정리의 타이밍이야.', '🧳 선반에 올려둔 짐은 미리 꺼내서 내리기 편하게 해두고,',
'🧼 좌석 주변 정돈은 매너의 기본!')),
(63, 12, 3, '🛤️ 에피소드 3. 하차 후 마무리!', JSON_ARRAY('🚉 도착! 수고했어.',  '📸 찍은 사진들을 정리하면서 여행을 되새겨봐.',
'📝 코레일톡에 이용 후기 남기면 다른 사람에게도 도움 될 수 있어.',  '🚶‍♀️ 역 주변에 숨은 명소는 없을까?')),
-- 4: END
(64, 11, 4, '🚏 에피소드 4. END', '엔딩');

---- 선택지 생성
INSERT IGNORE INTO choice (episode_id, next_episode_id, choice_description) VALUES
(57, 58, JSON_OBJECT('text', '기차 타기 전 준비하기')), (57, 60, JSON_OBJECT('text', '기차 안에서 할 일하기')), (57, 62, JSON_OBJECT('text', '기차 내릴 때 주의하기')),
(58, 59, JSON_OBJECT('text', '티켓부터 확인하기')), (58, 59, JSON_OBJECT('text', '간식 챙기기')),
(59, 64, JSON_OBJECT('text', '플랫폼 위치 확인하기')), (59, 64, JSON_OBJECT('text', '화장실 들르기')), (59, 64, JSON_OBJECT('text','편의점 구경하기')),
(60, 61, JSON_OBJECT('text', '음악 듣기')), (60, 61, JSON_OBJECT('text', '간식 먹기')), (60, 61, JSON_OBJECT('text', '창 밖 풍경 감상하기')),
(61, 64, JSON_OBJECT('text', '조용히 하기')), (61, 64, JSON_OBJECT('text', '자리 깨끗이 하기')), (61, 64, JSON_OBJECT('text', '다른 승객 배려하기')),
(62, 63, JSON_OBJECT('text', '짐 정리하기')), (62, 63, JSON_OBJECT('text', '좌석 주변 정돈하기')),
(63, 64, JSON_OBJECT('text', '기차 후기 작성하기')), (63, 64, JSON_OBJECT('text', '사진 정리하기')), (63, 64, JSON_OBJECT('text', '역 주변 탐방하기'));

-- -- [스크립트 완료] -- --
SELECT '서바이벌 데이터가 성공적으로 저장되었습니다.' AS message;