// 관리자 기능(신고, 배너, 퀴즈, 회원) 관련 API 컨트롤러
package com.codingrecipe.board.controller;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.dto.BannerDTO;
import com.codingrecipe.board.dto.QuizCreateRequest;
import com.codingrecipe.board.dto.QuizDetailResponse;
import com.codingrecipe.board.dto.QuizSimpleResponse;
import com.codingrecipe.board.domain.ReportEntity;
import com.codingrecipe.board.repository.ReportRepository;
import com.codingrecipe.board.service.BannerService;
import com.codingrecipe.board.service.MemberService;
import com.codingrecipe.board.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin") // 이 컨트롤러의 모든 API는 /api/admin 경로를 가짐
public class AdminController {

    // 의존성 주입
    private final ReportRepository reportRepository;
    private final BannerService bannerService;
    private final QuizService quizService;
    private final MemberService memberService;

    // --- 신고 관리 ---
    @GetMapping("/reports")
    public List<ReportEntity> getAllReports() {
        // 모든 신고 내역을 최신순으로 조회
        return reportRepository.findAll(Sort.by(Sort.Direction.DESC, "reportedAt"));
    }

    // --- 배너 관리 ---
    @GetMapping("/banners")
    public ResponseEntity<List<BannerDTO>> getAllBanners() {
        // 모든 배너 목록 조회
        List<BannerDTO> banners = bannerService.findAllBanners();
        return ResponseEntity.ok(banners);
    }

    @PostMapping("/banners")
    public ResponseEntity<String> createBanner(@RequestPart("bannerDTO") BannerDTO bannerDTO,
                                               @RequestPart("imageFile") MultipartFile imageFile) {
        // 신규 배너 생성 (이미지 파일 포함)
        try {
            bannerService.createBanner(bannerDTO, imageFile);
            return ResponseEntity.status(HttpStatus.CREATED).body("배너가 추가되었습니다");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("오류 발생: " + e.getMessage());
        }
    }

    @PutMapping("/banners/{id}")
    public ResponseEntity<String> updateBanner(@PathVariable Long id,
                                               @RequestPart("bannerDTO") BannerDTO bannerDTO,
                                               @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        // 기존 배너 정보 수정 (이미지 파일은 선택적으로 업데이트)
        try {
            bannerService.updateBanner(id, bannerDTO, imageFile);
            return ResponseEntity.ok("배너가 수정되었습니다");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("오류 발생: " + e.getMessage());
        }
    }

    @DeleteMapping("/banners/{id}")
    public ResponseEntity<String> deleteBanner(@PathVariable Long id) {
        // ID로 특정 배너 삭제
        try {
            bannerService.deleteBanner(id);
            return ResponseEntity.ok("배너가 삭제되었습니다");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("오류 발생: " + e.getMessage());
        }
    }

    // --- 퀴즈 관리 ---
    @PostMapping("/quizzes")
    public ResponseEntity<QuizDetailResponse> createQuiz(@RequestBody QuizCreateRequest request) {
        // 신규 퀴즈 생성
        QuizDetailResponse createdQuiz = quizService.createQuiz(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuiz);
    }

    @GetMapping("/quizzes")
    public ResponseEntity<List<QuizSimpleResponse>> getAllQuizzes() {
        // 모든 퀴즈 목록을 간략한 정보로 조회
        List<QuizSimpleResponse> quizzes = quizService.findAllQuizzes();
        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/quizzes/{quizId}")
    public ResponseEntity<QuizDetailResponse> getQuizById(@PathVariable Long quizId) {
        // ID로 특정 퀴즈의 상세 정보 조회
        QuizDetailResponse quiz = quizService.findQuizDetailById(quizId);
        return ResponseEntity.ok(quiz);
    }

    @PutMapping("/quizzes/{quizId}")
    public ResponseEntity<QuizDetailResponse> updateQuiz(@PathVariable Long quizId, @RequestBody QuizCreateRequest request) {
        // ID로 특정 퀴즈 정보 수정
        QuizDetailResponse updatedQuiz = quizService.updateQuiz(quizId, request);
        return ResponseEntity.ok(updatedQuiz);
    }

    @DeleteMapping("/quizzes/{quizId}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long quizId) {
        // ID로 특정 퀴즈 삭제
        quizService.deleteQuiz(quizId);
        return ResponseEntity.noContent().build();
    }

    // --- 사용자 관리 ---
    @GetMapping("/members")
    public ResponseEntity<List<Member>> getAllMembers() {
        // 모든 회원 목록 조회
        List<Member> members = memberService.findMembers();
        return ResponseEntity.ok(members);
    }

    @GetMapping("/members/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        // ID로 특정 회원 정보 조회
        Optional<Member> member = memberService.findOne(id);
        return member.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
