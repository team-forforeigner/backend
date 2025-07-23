package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.domain.Role;
import com.codingrecipe.board.dto.SignUpRequestDto;
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
     * 회원가입 로직 (수정됨)
     * 1. 비밀번호 일치 확인
     * 2. 이메일로 기존 회원 조회
     * 3. 이미 인증까지 완료된 경우 -> 에러 발생
     * 4. 가입은 했지만 인증은 안 한 경우 -> 입력된 정보로 업데이트 후 인증 메일 재발송
     * 5. 신규 회원 생성 및 저장 후 인증 메일 발송
     */
    public void join(SignUpRequestDto dto) {
        // 1. 비밀번호, 비밀번호 확인 일치 여부 확인
        if (!dto.getPassword().equals(dto.getPasswordCheck())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 2. 이메일로 기존 가입 정보 조회
        Optional<Member> existingMemberOpt = memberRepository.findByEmail(dto.getEmail());

        if (existingMemberOpt.isPresent()) {
            Member existingMember = existingMemberOpt.get();
            // 3. 이미 인증까지 완료된 경우, 가입 방지
            if (existingMember.isEmailVerified()) {
                throw new IllegalStateException("이미 가입이 완료된 이메일입니다.");
            } else {
                // 4. 가입은 했으나 미인증 상태인 경우, 정보 업데이트 후 인증 메일 재발송
                updateUnverifiedMember(existingMember, dto);
                emailService.sendVerificationEmail(existingMember); // Member 객체에 email 정보가 있어야 함
                return;
            }
        }

        // 5. 신규 회원 정보 생성 및 저장
        Member newMember = createNewMember(dto);
        Member savedMember = memberRepository.save(newMember);
        emailService.sendVerificationEmail(savedMember);
    }

    // 미인증 회원 정보 업데이트 (수정됨)
    private void updateUnverifiedMember(Member member, SignUpRequestDto dto) {
        member.setPassword(passwordEncoder.encode(dto.getPassword()));
        member.setNationality(dto.getNationality());
    }

    // 신규 회원 엔티티 생성 (수정됨)
    private Member createNewMember(SignUpRequestDto dto) {
        return Member.builder()
                .email(dto.getEmail()) // email을 ID로 사용
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getEmail()) // 초기 닉네임을 이메일로 설정
                .nationality(dto.getNationality())
                .role(Role.USER)
                .emailVerified(false)
                .build();
    }

    // 토큰으로 이메일 인증 처리 (수정됨)
    public void verifyEmailByToken(String token) {
        // 토큰에서 email 추출 (JwtUtil 변경 필요)
        String email = jwtUtil.getEmail(token);
        // email로 회원 정보 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 해당 회원의 이메일 인증 상태 변경
        member.setEmailVerified(true);
    }

    // 로그인 처리 (수정됨)
    @Transactional(readOnly = true)
    public String login(String email, String password) {
        // 이메일로 회원 정보 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 이메일 인증 완료 여부 확인
        if (!member.isEmailVerified()) {
            // 인증 메일 재발송 등의 로직을 추가할 수 있습니다.
            throw new IllegalStateException("이메일 인증이 완료되지 않았습니다.");
        }

        // 로그인 성공 및 토큰 생성 (페이로드에 email 사용, JwtUtil 변경 필요)
        return jwtUtil.generateToken(member.getEmail());
    }

    // 아이디 찾기 기능 삭제
    // public Optional<String> findUserIdByNameAndEmail(String name, String email) { ... }

    // 아이디 중복 확인 기능 삭제
    // public boolean isUserIdDuplicated(String userId) { ... }

    // 비밀번호 재설정 (임시 비밀번호 발급) (수정됨)
    public void sendTempPassword(String email) {
        // 사용자 정보 확인
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("입력하신 이메일로 가입된 사용자가 없습니다."));

        // 임시 비밀번호 생성
        String tempPassword = emailService.createCode();
        // DB에 임시 비밀번호로 업데이트
        member.setPassword(passwordEncoder.encode(tempPassword));

        // 임시 비밀번호 이메일 발송
        emailService.sendTempPasswordEmail(email, tempPassword);
    }

    // (기존 코드 유지)
    @Transactional(readOnly = true)
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Member> findOne(Long memberId) {
        return memberRepository.findById(memberId);
    }
}