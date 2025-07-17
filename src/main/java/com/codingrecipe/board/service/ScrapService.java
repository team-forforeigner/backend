package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.dto.BoardDTO;
import com.codingrecipe.board.entity.BoardEntity;
import com.codingrecipe.board.entity.ScrapEntity;
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

    public void addScrap(String userId, Long boardId) {
        // 1. 사용자 정보 조회
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        // 2. 게시글 정보 조회
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 3. 이미 스크랩했는지 확인 (중복 방지)
        if (scrapRepository.findByMemberAndBoard(member, board).isPresent()) {
            throw new IllegalStateException("이미 스크랩한 게시글입니다.");
        }

        // 4. 스크랩 정보 저장
        ScrapEntity scrap = new ScrapEntity();
        scrap.setMember(member);
        scrap.setBoard(board);
        scrapRepository.save(scrap);
    }

    public void removeScrap(String userId, Long boardId) {
        // 1. 사용자 정보 조회
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        // 2. 게시글 정보 조회
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        // 3. 스크랩 정보 조회
        ScrapEntity scrap = scrapRepository.findByMemberAndBoard(member, board)
                .orElseThrow(() -> new IllegalArgumentException("스크랩 정보를 찾을 수 없습니다."));

        // 4. 스크랩 정보 삭제
        scrapRepository.delete(scrap);
    }

    @Transactional(readOnly = true)
    public List<BoardDTO> getMyScraps(String userId) {
        // 1. 사용자 정보 조회
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        // 2. 해당 사용자의 모든 스크랩 정보 조회
        List<ScrapEntity> scraps = scrapRepository.findAllByMember(member);

        // 3. 스크랩 정보에서 게시글 정보만 추출하여 DTO로 변환
        return scraps.stream()
                .map(scrap -> BoardDTO.toBoardDTO(scrap.getBoard()))
                .collect(Collectors.toList());
    }
}