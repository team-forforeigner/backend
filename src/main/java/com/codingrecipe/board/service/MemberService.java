package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.domain.MemberStatus;
import com.codingrecipe.board.domain.Role;
import com.codingrecipe.board.dto.EmailRequestDto;
import com.codingrecipe.board.dto.PasswordChangeRequest;
import com.codingrecipe.board.dto.SignUpRequestDto;
import com.codingrecipe.board.dto.UserInfoDto;
import com.codingrecipe.board.exception.CustomException;
import com.codingrecipe.board.exception.ErrorCode;
import com.codingrecipe.board.repository.MemberRepository;
import com.codingrecipe.board.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 회원가입 처리 로직
     */
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
        String email = jwtUtil.getEmail(token);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        member.setEmailVerified(true);
    }

    /**
     * 로그인 처리 및 JWT 토큰 발급
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

        if (member.getStatus() == MemberStatus.SUSPENDED) {
            throw new CustomException(ErrorCode.USER_SUSPENDED);
        }

        member.setLastLoginAt(LocalDateTime.now());
        return jwtUtil.generateToken(member);
    }

    public void sendTempPassword(EmailRequestDto dto) {
        Member member = memberRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String tempPassword = emailService.createCode();
        member.setPassword(passwordEncoder.encode(tempPassword));
        emailService.sendTempPasswordEmail(dto.getEmail(), tempPassword);
    }

    public void changePassword(String email, PasswordChangeRequest dto) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
        if (!dto.getNewPassword().equals(dto.getNewPasswordCheck())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
        member.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    }

    @Transactional(readOnly = true)
    public UserInfoDto getMemberInfoByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return new UserInfoDto(member);
    }

    @Transactional(readOnly = true)
    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public void updateNickname(String email, String newNickname) {
        Member member = findMemberByEmail(email);
        member.setNickname(newNickname);
    }

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

    public void updateMemberStatus(Long memberId, MemberStatus status) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        member.setStatus(status);
    }
}
