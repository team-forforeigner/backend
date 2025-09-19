package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.domain.Role;
import com.codingrecipe.board.dto.*;
import com.codingrecipe.board.exception.CustomException;
import com.codingrecipe.board.exception.ErrorCode;
import com.codingrecipe.board.repository.MemberRepository;
import com.codingrecipe.board.security.JwtUtil;
import com.codingrecipe.board.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
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

    public void join(SignUpRequestDto dto) {
        if (!dto.getPassword().equals(dto.getPasswordCheck())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
        Optional<Member> existingMemberOpt = memberRepository.findByEmail(dto.getEmail());
        if (existingMemberOpt.isPresent()) {
            Member existingMember = existingMemberOpt.get();
            if (existingMember.isEmailVerified()) {
                throw new CustomException(ErrorCode.ALREADY_EXIST_EMAIL);
            } else {
                updateUnverifiedMember(existingMember, dto);
                emailService.sendVerificationEmail(existingMember);
                return;
            }
        }
        Member newMember = createNewMember(dto);
        Member savedMember = memberRepository.save(newMember);
        emailService.sendVerificationEmail(savedMember);
    }

    private void updateUnverifiedMember(Member member, SignUpRequestDto dto) {
        member.setPassword(passwordEncoder.encode(dto.getPassword()));
        member.setNationality(dto.getNationality());
        member.setNickname(dto.getEmail());
    }

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

    public void verifyEmailByToken(String token) {
        String email = jwtUtil.getEmailFromVerificationToken(token);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        member.setEmailVerified(true);
    }

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

        if (member.isSuspended()) {
            String message = "계정이 " + member.getSuspendedUntil().toString() + "까지 정지되었습니다.";
            throw new CustomException(ErrorCode.USER_SUSPENDED, message);
        }

        member.setLastLoginAt(LocalDateTime.now());
        memberRepository.save(member);

        return jwtUtil.generateToken(member);
    }

    public void sendTempPassword(EmailRequestDto dto) {
        Member member = memberRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String tempPassword = emailService.createCode();
        member.setPassword(passwordEncoder.encode(tempPassword));
        emailService.sendTempPasswordEmail(dto.getEmail(), tempPassword);
    }

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

    @Transactional(readOnly = true)
    public ProfileResponseDto getUserProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        s3UploaderService.ifPresent(uploader -> {
            if (StringUtils.hasText(member.getProfileImageUrl())) {
                member.setProfileImageUrl(uploader.generatePresignedUrl(member.getProfileImageUrl()));
            }
            if (StringUtils.hasText(member.getBackgroundImageUrl())) {
                member.setBackgroundImageUrl(uploader.generatePresignedUrl(member.getBackgroundImageUrl()));
            }
        });

        List<String> availableTitles = titleAndBadgeManager.getAvailableTitles(member.getLevel());
        List<AvailableBadgeDto> availableBadges = titleAndBadgeManager.getAvailableBadges(member.getLevel());

        return new ProfileResponseDto(member, availableTitles, availableBadges);
    }

    public void updateProfile(Long memberId, ProfileUpdateRequestDto requestDto, MultipartFile profileImage, MultipartFile backgroundImage) throws IOException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (profileImage != null && !profileImage.isEmpty()) {
            String existingProfileImageKey = member.getProfileImageUrl();
            String newProfileImageKey = s3UploaderService.map(uploader -> {
                try {
                    if (StringUtils.hasText(existingProfileImageKey)) {
                        uploader.delete(uploader.extractFileKeyFromUrl(existingProfileImageKey));
                    }
                    return uploader.upload(profileImage, "profiles");
                } catch (IOException e) {
                    throw new CustomException(ErrorCode.S3_FILE_UPLOAD_FAILED);
                }
            }).orElse(null);
            member.setProfileImageUrl(newProfileImageKey);
        }
        if (backgroundImage != null && !backgroundImage.isEmpty()) {
            String existingBgImageKey = member.getBackgroundImageUrl();
            String newBgImageKey = s3UploaderService.map(uploader -> {
                try {
                    if (StringUtils.hasText(existingBgImageKey)) {
                        uploader.delete(uploader.extractFileKeyFromUrl(existingBgImageKey));
                    }
                    return uploader.upload(backgroundImage, "backgrounds");
                } catch (IOException e) {
                    throw new CustomException(ErrorCode.S3_FILE_UPLOAD_FAILED);
                }
            }).orElse(null);
            member.setBackgroundImageUrl(newBgImageKey);
        }
        if (requestDto != null) {
            if (StringUtils.hasText(requestDto.getNickname())) {
                member.setNickname(requestDto.getNickname());
            }
            if (StringUtils.hasText(requestDto.getSelectedTitle())) {
                List<String> availableTitles = titleAndBadgeManager.getAvailableTitles(member.getLevel());
                if (availableTitles.contains(requestDto.getSelectedTitle())) {
                    member.setTitle(requestDto.getSelectedTitle());
                } else {
                    throw new CustomException(ErrorCode.FORBIDDEN_ACCESS, "아직 사용할 수 없는 칭호입니다.");
                }
            }
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

    /**
     * [신규] 다른 사용자의 공개 프로필 조회 로직
     */
    @Transactional(readOnly = true)
    public PublicProfileResponseDto getPublicUserProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // S3 파일 키를 완전한 URL로 변환합니다.
        s3UploaderService.ifPresent(uploader -> {
            if (StringUtils.hasText(member.getProfileImageUrl())) {
                member.setProfileImageUrl(uploader.generatePresignedUrl(member.getProfileImageUrl()));
            }
            if (StringUtils.hasText(member.getBackgroundImageUrl())) {
                member.setBackgroundImageUrl(uploader.generatePresignedUrl(member.getBackgroundImageUrl()));
            }
        });

        return new PublicProfileResponseDto(member);
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

