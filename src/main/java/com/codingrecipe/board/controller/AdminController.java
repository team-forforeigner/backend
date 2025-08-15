package com.codingrecipe.board.controller;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.dto.BannerDTO;
import com.codingrecipe.board.dto.MemberAdminResponseDto;
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
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final ReportRepository reportRepository;
    private final BannerService bannerService;
    private final QuizService quizService;
    private final MemberService memberService;

    // --- 신고 관리 ---
    @GetMapping("/reports")
    public List<ReportEntity> getAllReports() {
        return reportRepository.findAll(Sort.by(Sort.Direction.DESC, "reportedAt"));
    }

    // --- 배너 관리 ---
    @GetMapping("/banners")
    public ResponseEntity<List<BannerDTO>> getAllBanners() {
        List<BannerDTO> banners = bannerService.findAllBanners();
        return ResponseEntity.ok(banners);
    }

    @PostMapping("/banners")
    public ResponseEntity<String> createBanner(@RequestPart("bannerDTO") BannerDTO bannerDTO,
                                               @RequestPart("imageFile") MultipartFile imageFile) {
        bannerService.createBanner(bannerDTO, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body("배너가 추가되었습니다");
    }

    @PutMapping("/banners/{id}")
    public ResponseEntity<String> updateBanner(@PathVariable Long id,
                                               @RequestPart("bannerDTO") BannerDTO bannerDTO,
                                               @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        bannerService.updateBanner(id, bannerDTO, imageFile);
        return ResponseEntity.ok("배너가 수정되었습니다");
    }

    @DeleteMapping("/banners/{id}")
    public ResponseEntity<String> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.ok("배너가 삭제되었습니다");
    }

    // --- 퀴즈 관리 ---
    @PostMapping("/quizzes")
    public ResponseEntity<QuizDetailResponse> createQuiz(@RequestBody QuizCreateRequest request) {
        QuizDetailResponse createdQuiz = quizService.createQuiz(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuiz);
    }

    @GetMapping("/quizzes")
    public ResponseEntity<List<QuizSimpleResponse>> getAllQuizzes() {
        List<QuizSimpleResponse> quizzes = quizService.findAllQuizzes();
        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/quizzes/{quizId}")
    public ResponseEntity<QuizDetailResponse> getQuizById(@PathVariable Long quizId) {
        QuizDetailResponse quiz = quizService.findQuizDetailById(quizId);
        return ResponseEntity.ok(quiz);
    }

    @PutMapping("/quizzes/{quizId}")
    public ResponseEntity<QuizDetailResponse> updateQuiz(@PathVariable Long quizId, @RequestBody QuizCreateRequest request) {
        QuizDetailResponse updatedQuiz = quizService.updateQuiz(quizId, request);
        return ResponseEntity.ok(updatedQuiz);
    }

    @DeleteMapping("/quizzes/{quizId}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long quizId) {
        quizService.deleteQuiz(quizId);
        return ResponseEntity.noContent().build();
    }

    // --- 사용자 관리 ---
    @GetMapping("/members")
    public ResponseEntity<List<MemberAdminResponseDto>> getAllMembers() {
        List<Member> members = memberService.findMembers();
        List<MemberAdminResponseDto> response = members.stream()
                .map(MemberAdminResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/members/{id}")
    public ResponseEntity<MemberAdminResponseDto> getMemberById(@PathVariable Long id) {
        Optional<Member> memberOpt = memberService.findOne(id);
        return memberOpt.map(member -> ResponseEntity.ok(new MemberAdminResponseDto(member)))
                .orElse(ResponseEntity.notFound().build());
    }
}