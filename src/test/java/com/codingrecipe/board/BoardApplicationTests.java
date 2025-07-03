package com.codingrecipe.board;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource; // [추가] import 구문

// [추가] 이 테스트를 실행할 때만 사용할 임시 프로퍼티(설정값)를 지정합니다.
// 이렇게 하면 BoardService에 있는 @Value("${file-upload-path}")가 값을 찾지 못하는 오류가 해결됩니다.
@TestPropertySource(properties = {"file-upload-path=C:/test_upload/"})
@SpringBootTest
class BoardApplicationTests {

	@Test
	void contextLoads() {
	}

}