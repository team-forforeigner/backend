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

    public void addScrap(String email, Long boardId) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (scrapRepository.findByMemberAndBoard(member, board).isPresent()) {
            throw new IllegalStateException("이미 스크랩한 게시글입니다.");
        }

        ScrapEntity scrap = new ScrapEntity();
        scrap.setMember(member);
        scrap.setBoard(board);
        scrapRepository.save(scrap);
    }

    public void removeScrap(String email, Long boardId) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        ScrapEntity scrap = scrapRepository.findByMemberAndBoard(member, board)
                .orElseThrow(() -> new IllegalArgumentException("스크랩 정보를 찾을 수 없습니다."));

        scrapRepository.delete(scrap);
    }

    @Transactional(readOnly = true)
    public List<BoardDTO> getMyScraps(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        List<ScrapEntity> scraps = scrapRepository.findAllByMember(member);

        return scraps.stream()
                .map(scrap -> BoardDTO.toBoardDTO(scrap.getBoard()))
                .collect(Collectors.toList());
    }
}