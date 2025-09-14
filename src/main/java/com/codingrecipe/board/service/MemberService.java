package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.domain.MemberStatus;
import com.codingrecipe.board.domain.Role;
import com.codingrecipe.board.dto.*;
import com.codingrecipe.board.event.TempPasswordEvent;
import com.codingrecipe.board.event.UserRegistrationEvent;
import com.codingrecipe.board.exception.CustomException;
import com.codingrecipe.board.exception.ErrorCode;
import com.codingrecipe.board.repository.MemberRepository;
import com.codingrecipe.board.security.JwtUtil;
import com.codingrecipe.board.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final TitleAndBadgeManager titleAndBadgeManager;
    private final Optional<S3UploaderService> s3UploaderService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${cloud.aws.s3.folder.profile}")
    private String profileFolder;

    @Value("${cloud.aws.s3.folder.profile-background}")
    private String backgroundFolder;

    /**
     * 회원가입 처리 로직
     */
    public void join(SignUpRequestDto dto) {
        // 1. 비밀번호 확인
        if (!dto.getPassword().equals(dto.getPasswordCheck())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // 2. 기존 회원 확인
        Optional<Member> existingMemberOpt = memberRepository.findByEmail(dto.getEmail());
        if (existingMemberOpt.isPresent()) {
            Member existingMember = existingMemberOpt.get();
            if (existingMember.isEmailVerified()) {
                throw new CustomException(ErrorCode.ALREADY_EXIST_EMAIL);
            } else {
                updateUnverifiedMember(existingMember, dto);
                // 이벤트 발행 (메일 발송)
                eventPublisher.publishEvent(new UserRegistrationEvent(existingMember));
                return;
            }
        }

        // 3. 신규 회원 생성
        Member newMember = createNewMember(dto);
        Member savedMember = memberRepository.save(newMember);
        // 이벤트 발행 (메일 발송)
        eventPublisher.publishEvent(new UserRegistrationEvent(savedMember));
    }

    /**
     * 미인증 사용자의 정보를 업데이트
     */
    private void updateUnverifiedMember(Member member, SignUpRequestDto dto) {
        member.setPassword(passwordEncoder.encode(dto.getPassword()));
        member.setNationality(dto.getNationality());
        member.setNickname(dto.getEmail()); // 초기 닉네임은 이메일로 설정
    }

    /**
     * 신규 회원 엔티티를 생성
     */
    private Member createNewMember(SignUpRequestDto dto) {
        return Member.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getEmail())
                .nationality(dto.getNationality())
                .role(Role.USER)
                .emailVerified(false)
                .build();
    }

    /**
     * 이메일 인증 토큰을 검증하여 회원 상태를 '인증 완료'로 변경
     */
    public void verifyEmailByToken(String token) {
        String email = jwtUtil.getEmailFromVerificationToken(token);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        member.setEmailVerified(true);
    }

    /**
     * 로그인 처리 및 JWT 토큰 발급 (정지 상태 확인 로직 추가)
     */
    @Transactional
    public String login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }

        if (!member.isEmailVerified()) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        // 정지 상태를 확인하는 가장 중요한 로직
        if (member.isSuspended()) {
            String message = "계정이 " + member.getSuspendedUntil().toString() + "까지 정지되었습니다.";
            throw new CustomException(ErrorCode.USER_SUSPENDED, message);
        }

        member.setLastLoginAt(LocalDateTime.now());
        memberRepository.save(member);

        return jwtUtil.generateToken(member);
    }

    /**
     * 임시 비밀번호를 생성하여 이메일로 발송
     */
    public void sendTempPassword(EmailRequestDto dto) {
        Member member = memberRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String tempPassword = emailService.createCode();
        member.setPassword(passwordEncoder.encode(tempPassword));
        // 이벤트 발행 (메일 발송)
        eventPublisher.publishEvent(new TempPasswordEvent(member, tempPassword));
    }

    /**
     * 현재 로그인된 사용자의 비밀번호를 변경
     */
    public void changePassword(UserPrincipal user, PasswordChangeRequest dto) {
        Member member = memberRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD, "현재 비밀번호가 일치하지 않습니다.");
        }
        if (!dto.getNewPassword().equals(dto.getNewPasswordCheck())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD, "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }
        member.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    }

    // --- 프로필 조회 로직 ---
    @Transactional(readOnly = true)
    public ProfileResponseDto getUserProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<String> availableTitles = titleAndBadgeManager.getAvailableTitles(member.getLevel());
        List<AvailableBadgeDto> availableBadges = titleAndBadgeManager.getAvailableBadges(member.getLevel());

        return new ProfileResponseDto(member, availableTitles, availableBadges);
    }

    // --- 프로필 수정 로직 ---
    public void updateProfile(Long memberId, ProfileUpdateRequestDto requestDto, MultipartFile profileImage, MultipartFile backgroundImage) throws IOException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 1. 프로필 이미지 업데이트
        if (profileImage != null && !profileImage.isEmpty()) {
            String existingProfileImageKey = member.getProfileImageUrl();
            String newProfileImageKey = s3UploaderService.map(uploader -> {
                try {
                    // 기존 이미지가 있으면 S3에서 삭제
                    if (StringUtils.hasText(existingProfileImageKey)) {
                        uploader.deleteImage(existingProfileImageKey);
                    }
                    // 새 이미지 업로드 후 파일 키 반환
                    return uploader.uploadImage(profileImage, profileFolder);
                } catch (IOException e) {
                    throw new CustomException(ErrorCode.S3_FILE_UPLOAD_FAILED);
                }
            }).orElse(null);
            member.setProfileImageUrl(newProfileImageKey);
        }

        // 2. 배경 이미지 업데이트
        if (backgroundImage != null && !backgroundImage.isEmpty()) {
            String existingBgImageKey = member.getBackgroundImageUrl();
            String newBgImageKey = s3UploaderService.map(uploader -> {
                try {
                    if (StringUtils.hasText(existingBgImageKey)) {
                        uploader.deleteImage(existingBgImageKey);
                    }
                    return uploader.uploadImage(backgroundImage, backgroundFolder);
                } catch (IOException e) {
                    throw new CustomException(ErrorCode.S3_FILE_UPLOAD_FAILED);
                }
            }).orElse(null);
            member.setBackgroundImageUrl(newBgImageKey);
        }

        // 3. 텍스트 정보 업데이트 (닉네임, 칭호, 배지)
        if (requestDto != null) {
            // 닉네임 업데이트
            if (StringUtils.hasText(requestDto.getNickname())) {
                member.setNickname(requestDto.getNickname());
            }

            // 칭호 업데이트
            if (StringUtils.hasText(requestDto.getSelectedTitle())) {
                List<String> availableTitles = titleAndBadgeManager.getAvailableTitles(member.getLevel());
                if (availableTitles.contains(requestDto.getSelectedTitle())) {
                    member.setTitle(requestDto.getSelectedTitle());
                } else {
                    throw new CustomException(ErrorCode.FORBIDDEN_ACCESS, "아직 사용할 수 없는 칭호입니다.");
                }
            }

            // 캐릭터 업데이트
            if (StringUtils.hasText(requestDto.getSelectedBadge())) {
                List<AvailableBadgeDto> availableBadges = titleAndBadgeManager.getAvailableBadges(member.getLevel());
                boolean isSelectable = availableBadges.stream()
                        .anyMatch(badge -> badge.getImageUrl().equals(requestDto.getSelectedBadge()));
                if (isSelectable) {
                    member.setBadge(requestDto.getSelectedBadge());
                } else {
                    throw new CustomException(ErrorCode.FORBIDDEN_ACCESS, "아직 사용할 수 없는 캐릭터입니다.");
                }
            }
        }
    }


    // --- 관리자용 메소드들 ---
    @Transactional(readOnly = true)
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Member> findOne(Long memberId) {
        return memberRepository.findById(memberId);
    }

    public void deleteMemberByAdmin(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        memberRepository.deleteById(memberId);
    }

    @Transactional(readOnly = true)
    public List<Member> searchMembersByEmail(String emailKeyword) {
        return memberRepository.findByEmailStartingWith(emailKeyword);
    }

    /**
     * 관리자가 회원을 기간제 정지시키거나 정지를 해제하는 메소드
     * @param memberId 정지시킬 회원의 ID
     * @param days 정지할 기간(일). 0 또는 음수 입력 시 정지 해제.
     */
    public void suspendMember(Long memberId, int days) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (days > 0) {
            LocalDateTime suspendedUntil = LocalDateTime.now().plusDays(days);
            member.setSuspendedUntil(suspendedUntil);
        } else {
            member.setSuspendedUntil(null);
        }
    }
}

