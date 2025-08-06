// 게시글 스크랩 추가, 삭제, 조회 등 비즈니스 로직을 처리하는 서비스
package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.dto.BoardDTO;
import com.codingrecipe.board.domain.BoardEntity;
import com.codingrecipe.board.domain.ScrapEntity;
import com.codingrecipe.board.repository.BoardRepository;
import com.codingrecipe.board.repository.MemberRepository;
import com.codingrecipe.board.repository.ScrapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;

    /**
     * 특정 게시글을 스크랩 목록에 추가
     */
    public void addScrap(String email, Long boardId) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        // 이미 스크랩한 게시글인지 확인
        if (scrapRepository.findByMemberAndBoard(member, board).isPresent()) {
            throw new IllegalStateException("이미 스크랩한 게시글입니다");
        }

        // 새로운 스크랩 엔티티 생성 및 저장
        ScrapEntity scrap = new ScrapEntity();
        scrap.setMember(member);
        scrap.setBoard(board);
        scrapRepository.save(scrap);
    }

    /**
     * 특정 게시글을 스크랩 목록에서 삭제
     */
    public void removeScrap(String email, Long boardId) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));
        // 삭제할 스크랩 정보 조회
        ScrapEntity scrap = scrapRepository.findByMemberAndBoard(member, board)
                .orElseThrow(() -> new IllegalArgumentException("스크랩 정보를 찾을 수 없습니다"));

        scrapRepository.delete(scrap);
    }

    /**
     * 현재 로그인된 사용자가 스크랩한 모든 게시글 목록을 조회
     */
    @Transactional(readOnly = true)
    public List<BoardDTO> getMyScraps(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        List<ScrapEntity> scraps = scrapRepository.findAllByMember(member);

        // 스크랩 목록을 게시글 DTO 목록으로 변환하여 반환
        return scraps.stream()
                .map(scrap -> BoardDTO.toBoardDTO(scrap.getBoard()))
                .collect(Collectors.toList());
    }
}
