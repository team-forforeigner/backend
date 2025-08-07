// 회원 가입, 로그인, 정보 수정 등 회원 관련 비즈니스 로직을 처리하는 서비스
package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.domain.Role;
import com.codingrecipe.board.dto.EmailRequestDto;
import com.codingrecipe.board.dto.PasswordChangeRequest;
import com.codingrecipe.board.dto.SignUpRequestDto;
import com.codingrecipe.board.dto.UserInfoDto;
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
        // 비밀번호와 비밀번호 확인이 일치하는지 검사
        if (!dto.getPassword().equals(dto.getPasswordCheck())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }
        Optional<Member> existingMemberOpt = memberRepository.findByEmail(dto.getEmail());
        if (existingMemberOpt.isPresent()) {
            Member existingMember = existingMemberOpt.get();
            // 이미 이메일 인증까지 완료된 경우
            if (existingMember.isEmailVerified()) {
                throw new IllegalStateException("이미 가입이 완료된 이메일입니다");
            } else {
                // 이메일 인증을 완료하지 않은 사용자가 다시 가입을 시도하는 경우
                updateUnverifiedMember(existingMember, dto);
                emailService.sendVerificationEmail(existingMember); // 인증 메일 재전송
                return;
            }
        }
        // 신규 회원인 경우
        Member newMember = createNewMember(dto);
        Member savedMember = memberRepository.save(newMember);
        emailService.sendVerificationEmail(savedMember); // 인증 메일 발송
    }

    /**
     * 미인증 사용자의 정보를 업데이트
     */
    private void updateUnverifiedMember(Member member, SignUpRequestDto dto) {
        member.setPassword(passwordEncoder.encode(dto.getPassword()));
        member.setNationality(dto.getNationality());
        member.setNickname(dto.getEmail()); // 초기 닉네임을 이메일로 설정
    }

    /**
     * 신규 회원 엔티티를 생성
     */
    private Member createNewMember(SignUpRequestDto dto) {
        return Member.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getEmail()) // 초기 닉네임을 이메일로 설정
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
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        member.setEmailVerified(true);
    }

    /**
     * 로그인 처리 및 JWT 토큰 발급
     */
    @Transactional(readOnly = true)
    public String login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다"));
        // 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다");
        }
        // 이메일 인증 완료 여부 확인
        if (!member.isEmailVerified()) {
            throw new IllegalStateException("이메일 인증이 완료되지 않았습니다");
        }
        return jwtUtil.generateToken(member.getEmail());
    }

    /**
     * 임시 비밀번호를 생성하여 이메일로 발송
     */
    public void sendTempPassword(EmailRequestDto dto) {
        Member member = memberRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("입력하신 이메일로 가입된 사용자가 없습니다"));
        String tempPassword = emailService.createCode();
        member.setPassword(passwordEncoder.encode(tempPassword)); // 임시 비밀번호로 변경
        emailService.sendTempPasswordEmail(dto.getEmail(), tempPassword);
    }

    /**
     * 현재 로그인된 사용자의 비밀번호를 변경
     */
    public void changePassword(String email, PasswordChangeRequest dto) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 현재 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(dto.getCurrentPassword(), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다");
        }
        // 새 비밀번호와 확인용 비밀번호 일치 여부 확인
        if (!dto.getNewPassword().equals(dto.getNewPasswordCheck())) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다");
        }
        member.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    }

    /**
     * 이메일로 사용자 정보를 조회하여 DTO로 반환
     */
    @Transactional(readOnly = true)
    public UserInfoDto getMemberInfoByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));
        return new UserInfoDto(member);
    }

    /**
     * 이메일로 사용자 엔티티를 조회 (내부 로직용)
     */
    @Transactional(readOnly = true)
    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
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
