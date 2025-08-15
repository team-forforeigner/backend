package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.Member;
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

    /**
     * 미인증 사용자의 정보를 업데이트
     */
    private void updateUnverifiedMember(Member member, SignUpRequestDto dto) {
        member.setPassword(passwordEncoder.encode(dto.getPassword()));
        member.setNationality(dto.getNationality());
        member.setNickname(dto.getEmail());
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
        String email = jwtUtil.getEmail(token);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        member.setEmailVerified(true);
    }

    /**
     * 로그인 처리 및 JWT 토큰 발급
     */
    @Transactional(readOnly = true)
    public String login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }

        if (!member.isEmailVerified()) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
        return jwtUtil.generateToken(member.getEmail());
    }

    /**
     * 임시 비밀번호를 생성하여 이메일로 발송
     */
    public void sendTempPassword(EmailRequestDto dto) {
        Member member = memberRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String tempPassword = emailService.createCode();
        member.setPassword(passwordEncoder.encode(tempPassword));
        emailService.sendTempPasswordEmail(dto.getEmail(), tempPassword);
    }

    /**
     * 현재 로그인된 사용자의 비밀번호를 변경
     */
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

    /**
     * 이메일로 사용자 정보를 조회하여 DTO로 반환
     */
    @Transactional(readOnly = true)
    public UserInfoDto getMemberInfoByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return new UserInfoDto(member);
    }

    /**
     * 이메일로 사용자 엔티티를 조회 (내부 로직용)
     */
    @Transactional(readOnly = true)
    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 사용자의 닉네임을 변경
     */
    public void updateNickname(String email, String newNickname) {
        Member member = findMemberByEmail(email);
        member.setNickname(newNickname);
    }

    /**
     * 모든 회원 목록을 조회 (관리자용)
     */
    @Transactional(readOnly = true)
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    /**
     * ID로 특정 회원 정보를 조회 (관리자용)
     */
    @Transactional(readOnly = true)
    public Optional<Member> findOne(Long memberId) {
        return memberRepository.findById(memberId);
    }
}