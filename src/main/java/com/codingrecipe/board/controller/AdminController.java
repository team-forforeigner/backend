package com.codingrecipe.board.controller;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.domain.ReportEntity;
import com.codingrecipe.board.dto.*;
import com.codingrecipe.board.exception.ErrorCode;
import com.codingrecipe.board.repository.ReportRepository;
import com.codingrecipe.board.service.*;
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
    private final BoardService boardService;
    private final CommentService commentService;

    // --- 신고 관리 ---
    @GetMapping("/reports")
    public ResponseEntity<ApiResponseDto<List<ReportEntity>>> getAllReports() {
        List<ReportEntity> reports = reportRepository.findAll(Sort.by(Sort.Direction.DESC, "reportedAt"));
        return ResponseEntity.ok(ApiResponseDto.success(reports));
    }

    // --- 배너 관리 ---
    @GetMapping("/banners")
    public ResponseEntity<ApiResponseDto<List<BannerDTO>>> getAllBanners() {
        List<BannerDTO> banners = bannerService.findAllBanners();
        return ResponseEntity.ok(ApiResponseDto.success(banners));
    }

    @PostMapping("/banners")
    public ResponseEntity<ApiResponseDto<Void>> createBanner(@RequestPart("bannerDTO") BannerDTO bannerDTO,
                                                             @RequestPart("imageFile") MultipartFile imageFile) {
        bannerService.createBanner(bannerDTO, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success("배너가 추가되었습니다."));
    }

    @PutMapping("/banners/{id}")
    public ResponseEntity<ApiResponseDto<Void>> updateBanner(@PathVariable Long id,
                                                             @RequestPart("bannerDTO") BannerDTO bannerDTO,
                                                             @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        bannerService.updateBanner(id, bannerDTO, imageFile);
        return ResponseEntity.ok(ApiResponseDto.success("배너가 수정되었습니다."));
    }

    @DeleteMapping("/banners/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.ok(ApiResponseDto.success("배너가 삭제되었습니다."));
    }

    // --- 퀴즈 관리 ---
    @PostMapping("/quizzes")
    public ResponseEntity<ApiResponseDto<QuizDetailResponse>> createQuiz(@RequestBody QuizCreateRequest request) {
        QuizDetailResponse createdQuiz = quizService.createQuiz(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(createdQuiz));
    }

    @GetMapping("/quizzes")
    public ResponseEntity<ApiResponseDto<List<QuizSimpleResponse>>> getAllQuizzes() {
        List<QuizSimpleResponse> quizzes = quizService.findAllQuizzes();
        return ResponseEntity.ok(ApiResponseDto.success(quizzes));
    }

    @GetMapping("/quizzes/{quizId}")
    public ResponseEntity<ApiResponseDto<QuizDetailResponse>> getQuizById(@PathVariable Long quizId) {
        QuizDetailResponse quiz = quizService.findQuizDetailById(quizId);
        return ResponseEntity.ok(ApiResponseDto.success(quiz));
    }

    @PutMapping("/quizzes/{quizId}")
    public ResponseEntity<ApiResponseDto<QuizDetailResponse>> updateQuiz(@PathVariable Long quizId, @RequestBody QuizCreateRequest request) {
        QuizDetailResponse updatedQuiz = quizService.updateQuiz(quizId, request);
        return ResponseEntity.ok(ApiResponseDto.success(updatedQuiz));
    }

    @DeleteMapping("/quizzes/{quizId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteQuiz(@PathVariable Long quizId) {
        quizService.deleteQuiz(quizId);
        return ResponseEntity.ok(ApiResponseDto.success("퀴즈가 삭제되었습니다."));
    }

    // --- 사용자 관리 ---
    @GetMapping("/members")
    public ResponseEntity<ApiResponseDto<List<MemberAdminResponseDto>>> getAllMembers() {
        List<MemberAdminResponseDto> response = memberService.findMembers().stream()
                .map(MemberAdminResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    @GetMapping("/members/search")
    public ResponseEntity<ApiResponseDto<List<MemberAdminResponseDto>>> searchMembersByEmail(@RequestParam String email) {
        List<MemberAdminResponseDto> response = memberService.searchMembersByEmail(email).stream()
                .map(MemberAdminResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    @GetMapping("/members/{id}")
    public ResponseEntity<ApiResponseDto<MemberAdminResponseDto>> getMemberById(@PathVariable Long id) {
        Optional<Member> memberOpt = memberService.findOne(id);
        return memberOpt.map(member -> ResponseEntity.ok(ApiResponseDto.success(new MemberAdminResponseDto(member))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDto.error(ErrorCode.USER_NOT_FOUND)));
    }

    @PatchMapping("/members/{id}/status")
    public ResponseEntity<ApiResponseDto<Void>> updateMemberStatus(@PathVariable Long id, @RequestBody MemberStatusUpdateRequestDto dto) {
        memberService.updateMemberStatus(id, dto.getStatus());
        return ResponseEntity.ok(ApiResponseDto.success("회원 상태가 성공적으로 변경되었습니다."));
    }

    @DeleteMapping("/members/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteMember(@PathVariable Long id) {
        memberService.deleteMemberByAdmin(id);
        return ResponseEntity.ok(ApiResponseDto.success("해당 회원을 삭제했습니다."));
    }

    // --- 게시글/댓글 관리 ---
    @DeleteMapping("/boards/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteBoard(@PathVariable Long id) {
        boardService.deleteBoardByAdmin(id);
        return ResponseEntity.ok(ApiResponseDto.success("게시글을 삭제했습니다."));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteComment(@PathVariable Long id) {
        commentService.deleteCommentByAdmin(id);
        return ResponseEntity.ok(ApiResponseDto.success("댓글을 삭제했습니다."));
    }
}