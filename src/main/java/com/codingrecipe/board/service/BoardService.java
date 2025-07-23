package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.dto.BoardDTO;
import com.codingrecipe.board.entity.BoardEntity;
import com.codingrecipe.board.entity.BoardFileEntity;
import com.codingrecipe.board.entity.BoardLikeEntity;
import com.codingrecipe.board.entity.CategoryEntity;
import com.codingrecipe.board.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final BoardFileRepository boardFileRepository;
    private final CategoryRepository categoryRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final S3UploaderService s3UploaderService;

    @Autowired
    public BoardService(BoardRepository boardRepository, MemberRepository memberRepository, BoardFileRepository boardFileRepository, CategoryRepository categoryRepository, BoardLikeRepository boardLikeRepository, @Autowired(required = false) S3UploaderService s3UploaderService) {
        this.boardRepository = boardRepository;
        this.memberRepository = memberRepository;
        this.boardFileRepository = boardFileRepository;
        this.categoryRepository = categoryRepository;
        this.boardLikeRepository = boardLikeRepository;
        this.s3UploaderService = s3UploaderService;
    }

    @Transactional
    public Long save(BoardDTO boardDTO, String email) throws IOException { // 변경: userId -> email
        // 변경: findByUserId -> findByEmail
        Member writer = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

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

            if (s3UploaderService != null) {
                String originalFilename = boardFile.getOriginalFilename();
                String storedFileName = s3UploaderService.upload(boardFile, "images");
                BoardFileEntity boardFileEntity = BoardFileEntity.toBoardFileEntity(savedEntity, originalFilename, storedFileName);
                boardFileRepository.save(boardFileEntity);
            } else {
                System.out.println("S3 Uploader가 없어 파일 저장을 건너뜁니다. (local profile)");
            }

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
    public Page<BoardDTO> pagingByWriter(String email, Pageable pageable) { // 변경: userId -> email
        // 참고: BoardRepository에 findByWriter_Email 메소드가 필요합니다.
        Page<BoardEntity> boardEntities = boardRepository.findByWriter_Email(email, pageable);
        return boardEntities.map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<BoardDTO> findTop3ByLikes() {
        List<BoardEntity> boardEntityList = boardRepository.findTop3ByOrderByBoardLikesDesc();
        return boardEntityList.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BoardDTO findById(Long id) {
        BoardEntity boardEntity = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));
        BoardDTO boardDTO = convertToDto(boardEntity);
        if (s3UploaderService != null && boardEntity.getFileAttached() == 1 && !boardEntity.getBoardFileEntityList().isEmpty()) {
            String storedFileName = boardEntity.getBoardFileEntityList().get(0).getStoredFileName();
            String fileUrl = s3UploaderService.generatePresignedUrl(storedFileName);
            boardDTO.setFileUrl(fileUrl);
        }
        return boardDTO;
    }

    public void update(Long id, BoardDTO boardDTO, String email) { // 변경: userId -> email
        BoardEntity boardEntity = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));

        // 변경: getUserId() -> getEmail()
        if (boardEntity.getWriter() == null || !boardEntity.getWriter().getEmail().equals(email)) {
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

    public void delete(Long boardId, String email) { // 변경: userId -> email
        BoardEntity boardEntity = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 변경: getUserId() -> getEmail()
        if (boardEntity.getWriter() == null || !boardEntity.getWriter().getEmail().equals(email)) {
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

    public boolean toggleLike(Long boardId, String email) { // 변경: userId -> email
        // 변경: findByUserId -> findByEmail
        Member member = memberRepository.findByEmail(email)
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
    public List<BoardDTO> getMyLikedPosts(String email) { // 변경: userId -> email
        // 변경: findByUserId -> findByEmail
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        List<BoardLikeEntity> likes = boardLikeRepository.findAllByMember(member);
        return likes.stream()
                .map(like -> convertToDto(like.getBoard()))
                .collect(Collectors.toList());
    }

    private BoardDTO convertToDto(BoardEntity board) {
        return new BoardDTO(
                board.getId(),
                // 변경: getName() -> getNickname()
                board.getWriter() != null ? board.getWriter().getNickname() : "탈퇴한 사용자",
                board.getBoardTitle(),
                board.getBoardHits(),
                board.getBoardLikes(),
                board.getCreatedTime(),
                board.getCategory() != null ? board.getCategory().getName() : "미지정"
        );
    }
}