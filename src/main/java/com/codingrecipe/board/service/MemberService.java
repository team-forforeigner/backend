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
     * 회원가입 로직
     * 1. 이메일로 기존 회원 조회
     * 2. 이미 인증까지 완료된 경우 -> 에러 발생
     * 3. 가입은 했지만 인증은 안 한 경우 -> 입력된 정보로 업데이트 후 인증 메일 재발송
     * 4. 아이디 중복 확인 (새로운 이메일일 경우)
     * 5. 신규 회원 생성 및 저장 후 인증 메일 발송
     */
    public void join(SignUpRequestDto dto) {
        // 이메일로 기존 가입 정보 조회
        Optional<Member> existingMemberOpt = memberRepository.findByEmail(dto.getEmail());

        if (existingMemberOpt.isPresent()) {
            Member existingMember = existingMemberOpt.get();
            // 이미 인증까지 완료된 경우, 가입 방지
            if (existingMember.isEmailVerified()) {
                throw new IllegalStateException("이미 가입이 완료된 이메일입니다.");
            } else {
                // 다른 사람이 같은 이메일로 다른 아이디를 선점하려는 경우 방지
                if (!existingMember.getUserId().equals(dto.getUserId()) && memberRepository.findByUserId(dto.getUserId()).isPresent()) {
                    throw new IllegalStateException("이미 사용 중인 아이디입니다.");
                }
                // 가입은 했으나 미인증 상태인 경우, 정보 업데이트 후 인증 메일 재발송
                updateUnverifiedMember(existingMember, dto);
                emailService.sendVerificationEmail(existingMember);
                return;
            }
        }

        // 신규 가입의 경우, 아이디 중복 확인
        if (memberRepository.findByUserId(dto.getUserId()).isPresent()) {
            throw new IllegalStateException("이미 사용 중인 아이디입니다.");
        }

        // 신규 회원 정보 생성
        Member newMember = createNewMember(dto);
        // DB에 회원 정보 저장 및 인증 메일 발송
        Member savedMember = memberRepository.save(newMember);
        emailService.sendVerificationEmail(savedMember);
    }

    // 미인증 회원 정보 업데이트
    private void updateUnverifiedMember(Member member, SignUpRequestDto dto) {
        member.setLastName(dto.getLastName());
        member.setFirstName(dto.getFirstName());
        member.setName(dto.getLastName() + " " + dto.getFirstName());
        member.setUserId(dto.getUserId());
        member.setPassword(passwordEncoder.encode(dto.getPassword()));
        member.setNationality(dto.getNationality());
    }

    // 신규 회원 엔티티 생성
    private Member createNewMember(SignUpRequestDto dto) {
        return Member.builder()
                .lastName(dto.getLastName())
                .firstName(dto.getFirstName())
                .name(dto.getLastName() + " " + dto.getFirstName())
                .userId(dto.getUserId())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .nationality(dto.getNationality())
                .role(Role.USER)
                .emailVerified(false)
                .build();
    }

    // 토큰으로 이메일 인증 처리
    public void verifyEmailByToken(String token) {
        // 토큰에서 userId 추출
        String userId = jwtUtil.getUserId(token);
        // userId로 회원 정보 조회
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 해당 회원의 이메일 인증 상태 변경
        member.setEmailVerified(true);
    }

    // 로그인 처리
    @Transactional(readOnly = true)
    public String login(String userId, String password) {
        // 아이디로 회원 정보 조회
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        // 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // 이메일 인증 완료 여부 확인
        if (!member.isEmailVerified()) {
            throw new IllegalStateException("이메일 인증이 완료되지 않았습니다.");
        }

        // 로그인 성공 및 토큰 생성
        return jwtUtil.generateToken(member.getUserId());
    }

    // 아이디 중복 여부 확인
    @Transactional(readOnly = true)
    public boolean isUserIdDuplicated(String userId) {
        return memberRepository.findByUserId(userId).isPresent();
    }

    // 이름과 이메일로 아이디 찾기
    @Transactional(readOnly = true)
    public Optional<String> findUserIdByNameAndEmail(String name, String email) {
        return memberRepository.findByNameAndEmail(name, email)
                .map(Member::getUserId);
    }

    // 비밀번호 재설정
    public void resetPassword(String userId, String name, String email) {
        // 사용자 정보 확인
        Member member = memberRepository.findByUserIdAndNameAndEmail(userId, name, email)
                .orElseThrow(() -> new IllegalArgumentException("입력하신 정보와 일치하는 사용자가 없습니다."));

        // 임시 비밀번호 생성
        String tempPassword = emailService.createCode();
        // DB에 임시 비밀번호로 업데이트
        member.setPassword(passwordEncoder.encode(tempPassword));

        // 임시 비밀번호 이메일 발송
        emailService.sendTempPasswordEmail(email, tempPassword);
    }

    // 모든 회원 목록 조회
    @Transactional(readOnly = true)
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    // ID로 단일 회원 조회
    @Transactional(readOnly = true)
    public Optional<Member> findOne(Long memberId) {
        return memberRepository.findById(memberId);
    }
}
