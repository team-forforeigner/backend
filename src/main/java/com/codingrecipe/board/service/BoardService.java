package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.dto.BoardDTO;
import com.codingrecipe.board.entity.BoardEntity;
import com.codingrecipe.board.entity.BoardFileEntity;
import com.codingrecipe.board.entity.BoardLikeEntity;
import com.codingrecipe.board.entity.CategoryEntity;
import com.codingrecipe.board.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final BoardFileRepository boardFileRepository;
    private final CategoryRepository categoryRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final S3UploaderService s3UploaderService; // S3 서비스 활성화

    // 로컬 파일 저장 경로
    // @Value("${file-upload-path}")
    // private String uploadPath;

    @Transactional
    public Long save(BoardDTO boardDTO, String userId) throws IOException {
        Member writer = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        CategoryEntity category = categoryRepository.findById(boardDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + boardDTO.getCategoryId()));

        BoardEntity boardEntity = new BoardEntity();
        boardEntity.setWriter(writer);
        boardEntity.setBoardTitle(boardDTO.getBoardTitle());
        boardEntity.setBoardContents(boardDTO.getBoardContents());
        boardEntity.setBoardHits(0);
        boardEntity.setBoardLikes(0);
        boardEntity.setCategory(category);

        MultipartFile boardFile = boardDTO.getBoardFile();
        if (boardFile == null || boardFile.isEmpty()) {
            boardEntity.setFileAttached(0);
            return boardRepository.save(boardEntity).getId();
        } else {
            boardEntity.setFileAttached(1);
            BoardEntity savedEntity = boardRepository.save(boardEntity);

            String originalFilename = boardFile.getOriginalFilename();
            String storedFileName;

            /*
            // 기존 로컬 테스트용 파일 저장 로직
            storedFileName = System.currentTimeMillis() + "_" + originalFilename;
            String savePath = uploadPath + storedFileName;
            boardFile.transferTo(new File(savePath));
            */

            // AWS S3 파일 업로드 로직 활성화
            storedFileName = s3UploaderService.upload(boardFile, "images"); // "images"는 S3 버킷 안의 폴더명

            BoardFileEntity boardFileEntity = BoardFileEntity.toBoardFileEntity(savedEntity, originalFilename, storedFileName);
            boardFileRepository.save(boardFileEntity);

            return savedEntity.getId();
        }
    }

    @Transactional(readOnly = true)
    public Page<BoardDTO> paging(Pageable pageable) {
        Page<BoardEntity> boardEntities = boardRepository.findAll(pageable);
        return boardEntities.map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<BoardDTO> pagingByCategory(Long categoryId, Pageable pageable) {
        Page<BoardEntity> boardEntities = boardRepository.findByCategoryId(categoryId, pageable);
        return boardEntities.map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<BoardDTO> pagingByWriter(String userId, Pageable pageable) {
        Page<BoardEntity> boardEntities = boardRepository.findByWriter_UserId(userId, pageable);
        return boardEntities.map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<BoardDTO> findTop3ByLikes() {
        List<BoardEntity> boardEntityList = boardRepository.findTop3ByOrderByBoardLikesDesc();
        return boardEntityList.stream().map(BoardDTO::toBoardDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BoardDTO findById(Long id) {
        BoardEntity boardEntity = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));

        // Entity -> DTO 기본 변환
        BoardDTO boardDTO = BoardDTO.toBoardDTO(boardEntity);

        // 파일이 첨부된 경우, S3 Pre-signed URL을 생성하여 DTO에 설정
        if (boardEntity.getFileAttached() == 1 && !boardEntity.getBoardFileEntityList().isEmpty()) {
            // storedFileName은 S3에 저장된 파일의 키(key) 입니다.
            String storedFileName = boardEntity.getBoardFileEntityList().get(0).getStoredFileName();
            String fileUrl = s3UploaderService.generatePresignedUrl(storedFileName);
            boardDTO.setFileUrl(fileUrl);
        }

        return boardDTO;
    }

    public void update(Long id, BoardDTO boardDTO, String userId) {
        BoardEntity boardEntity = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));

        if (boardEntity.getWriter() == null || !boardEntity.getWriter().getUserId().equals(userId)) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }

        if (boardDTO.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(boardDTO.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + boardDTO.getCategoryId()));
            boardEntity.setCategory(category);
        }

        boardEntity.setBoardTitle(boardDTO.getBoardTitle());
        boardEntity.setBoardContents(boardDTO.getBoardContents());
    }

    public void delete(Long boardId, String userId) {
        BoardEntity boardEntity = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (boardEntity.getWriter() == null || !boardEntity.getWriter().getUserId().equals(userId)) {
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }
        boardRepository.delete(boardEntity);
    }

    public void updateHits(Long id) {
        boardRepository.updateHits(id);
    }

    @Transactional(readOnly = true)
    public int getLikes(Long id) {
        return boardRepository.findById(id)
                .map(BoardEntity::getBoardLikes)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));
    }

    @Transactional(readOnly = true)
    public Page<BoardDTO> searchPosts(String keyword, Pageable pageable) {
        Page<BoardEntity> boardEntities = boardRepository.findByBoardTitleContainingOrBoardContentsContaining(keyword, keyword, pageable);
        return boardEntities.map(this::convertToDto);
    }

    public boolean toggleLike(Long boardId, String userId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Optional<BoardLikeEntity> like = boardLikeRepository.findByMemberAndBoard(member, board);

        if (like.isPresent()) {
            boardLikeRepository.delete(like.get());
            board.setBoardLikes(board.getBoardLikes() - 1);
            return false;
        } else {
            BoardLikeEntity newLike = new BoardLikeEntity();
            newLike.setMember(member);
            newLike.setBoard(board);
            boardLikeRepository.save(newLike);
            board.setBoardLikes(board.getBoardLikes() + 1);
            return true;
        }
    }

    @Transactional(readOnly = true)
    public List<BoardDTO> getMyLikedPosts(String userId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<BoardLikeEntity> likes = boardLikeRepository.findAllByMember(member);

        return likes.stream()
                .map(like -> BoardDTO.toBoardDTO(like.getBoard()))
                .collect(Collectors.toList());
    }

    // DTO 변환을 담당하는 별도 메소드 (코드 중복 제거)
    private BoardDTO convertToDto(BoardEntity board) {
        return new BoardDTO(
                board.getId(),
                board.getWriter() != null ? board.getWriter().getName() : "탈퇴한 사용자",
                board.getBoardTitle(),
                board.getBoardHits(),
                board.getBoardLikes(),
                board.getCreatedTime(),
                board.getCategory() != null ? board.getCategory().getName() : "미지정"
        );
    }
}