package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.*;
import com.codingrecipe.board.dto.BoardDTO;
import com.codingrecipe.board.dto.LikeResponseDTO;
import com.codingrecipe.board.exception.CustomException;
import com.codingrecipe.board.exception.ErrorCode;
import com.codingrecipe.board.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final BoardFileRepository boardFileRepository;
    private final CategoryRepository categoryRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final CommentRepository commentRepository;
    private final ScrapRepository scrapRepository;
    private final Optional<S3UploaderService> s3UploaderService;

    // S3 버킷 내 게시글 이미지가 저장될 폴더 경로
    @Value("${cloud.aws.s3.folder.post-image}")
    private String postImageFolder;

    @Transactional
    public Long save(BoardDTO boardDTO, String email) throws IOException {
        Member writer = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        CategoryEntity category = categoryRepository.findById(boardDTO.getCategoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        if ("공지사항".equals(category.getName())) {
            if (writer.getRole() != Role.ADMIN) {
                throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
            }
        }

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
            // --- [수정] S3 업로드 로직을 비동기로 호출 ---
            BoardEntity savedEntity = boardRepository.save(boardEntity);
            uploadToS3AndSaveFile(boardFile, savedEntity);
            return savedEntity.getId();
        }
    }

    @Async("threadPoolTaskExecutor")
    @Transactional
    public CompletableFuture<Void> uploadToS3AndSaveFile(MultipartFile boardFile, BoardEntity savedEntity) {
        s3UploaderService.ifPresent(uploader -> {
            try {
                log.info("S3 비동기 업로드를 시작합니다. 스레드: {}", Thread.currentThread().getName());
                String originalFilename = boardFile.getOriginalFilename();
                String storedFileName = uploader.uploadImage(boardFile, postImageFolder);

                // 비동기 작업 내에서 DB에 접근하려면 다시 엔티티를 조회해야 할 수 있습니다.
                BoardEntity boardToUpdate = boardRepository.findById(savedEntity.getId())
                        .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

                BoardFileEntity boardFileEntity = BoardFileEntity.toBoardFileEntity(boardToUpdate, originalFilename, storedFileName);
                boardFileRepository.save(boardFileEntity);
                log.info("S3 비동기 업로드 및 파일 정보 저장이 완료되었습니다.");
            } catch (IOException e) {
                log.error("S3 비동기 업로드 중 오류 발생", e);
                throw new CustomException(ErrorCode.S3_FILE_UPLOAD_FAILED);
            }
        });
        return CompletableFuture.completedFuture(null);
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
    public Page<BoardDTO> pagingByWriter(String email, Pageable pageable) {
        Page<BoardEntity> boardEntities = boardRepository.findByWriter_Email(email, pageable);
        return boardEntities.map(this::convertToDto);
    }
    @Transactional(readOnly = true)
    public List<BoardDTO> findTop3ByLikes() {
        List<BoardEntity> boardEntityList = boardRepository.findTop3ByOrderByBoardLikesDesc();
        return boardEntityList.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    @Transactional
    public BoardDTO findById(Long id) {
        boardRepository.updateHits(id);
        BoardEntity boardEntity = boardRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
        BoardDTO boardDTO = BoardDTO.toBoardDTO(boardEntity);

        s3UploaderService.ifPresent(uploader -> {
            if (boardEntity.getFileAttached() == 1 && !boardEntity.getBoardFileEntityList().isEmpty()) {
                String storedFileName = boardEntity.getBoardFileEntityList().get(0).getStoredFileName();
                String fileUrl = storedFileName;
                boardDTO.setFileUrl(fileUrl);
            }
        });
        return boardDTO;
    }
    public void update(Long id, BoardDTO boardDTO, String email) {
        BoardEntity boardEntity = boardRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        if (boardEntity.getWriter() == null || !boardEntity.getWriter().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        if (boardDTO.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(boardDTO.getCategoryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
            boardEntity.setCategory(category);
        }

        boardEntity.setBoardTitle(boardDTO.getBoardTitle());
        boardEntity.setBoardContents(boardDTO.getBoardContents());
    }
    public void delete(Long boardId, String email) {
        BoardEntity boardEntity = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        if (boardEntity.getWriter() == null || !boardEntity.getWriter().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        deleteS3FileIfAttached(boardEntity);
        // 일반 사용자는 연관 데이터(좋아요, 스크랩 등)를 직접 삭제하지 않으므로
        // 현재 BoardEntity의 @OneToMany에 cascade.REMOVE가 있으므로 그냥 삭제해도 동작함
        boardRepository.delete(boardEntity);
    }
    @Transactional(readOnly = true)
    public Page<BoardDTO> searchPosts(String keyword, Pageable pageable) {
        Page<BoardEntity> boardEntities = boardRepository.findByBoardTitleContainingOrBoardContentsContaining(keyword, keyword, pageable);
        return boardEntities.map(this::convertToDto);
    }
    public LikeResponseDTO toggleLike(Long boardId, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        Optional<BoardLikeEntity> like = boardLikeRepository.findByMemberAndBoard(member, board);

        if (like.isPresent()) {
            boardLikeRepository.delete(like.get());
            if (board.getBoardLikes() > 0) {
                board.setBoardLikes(board.getBoardLikes() - 1);
            }
            return new LikeResponseDTO(false, board.getBoardLikes());
        } else {
            BoardLikeEntity newLike = new BoardLikeEntity();
            newLike.setMember(member);
            newLike.setBoard(board);
            boardLikeRepository.save(newLike);
            board.setBoardLikes(board.getBoardLikes() + 1);
            return new LikeResponseDTO(true, board.getBoardLikes());
        }
    }
    @Transactional(readOnly = true)
    public List<BoardDTO> getMyLikedPosts(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        List<BoardLikeEntity> likes = boardLikeRepository.findAllByMember(member);
        return likes.stream()
                .map(like -> convertToDto(like.getBoard()))
                .collect(Collectors.toList());
    }
    private BoardDTO convertToDto(BoardEntity board) {
        return BoardDTO.toBoardDTO(board);
    }

    public void deleteBoardByAdmin(Long boardId) {
        BoardEntity boardEntity = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        // 1. S3 파일 삭제
        deleteS3FileIfAttached(boardEntity);

        // 2. 이 게시글을 참조하는 모든 자식 데이터들을 명시적으로 삭제
        commentRepository.deleteAll(boardEntity.getCommentEntityList());
        boardLikeRepository.deleteAll(boardEntity.getBoardLikeEntityList()); // 좋아요 삭제
        scrapRepository.deleteAll(boardEntity.getScrapEntityList());         // 스크랩 삭제

        // 3. 모든 자식 데이터가 정리된 후, 게시글 최종 삭제
        boardRepository.delete(boardEntity);
    }

    private void deleteS3FileIfAttached(BoardEntity boardEntity) {
        if (boardEntity.getFileAttached() == 1 && boardEntity.getBoardFileEntityList() != null && !boardEntity.getBoardFileEntityList().isEmpty()) {
            s3UploaderService.ifPresent(uploader -> {
                String storedFileName = boardEntity.getBoardFileEntityList().get(0).getStoredFileName();
                uploader.deleteImage(storedFileName);
            });
        }
    }
}

