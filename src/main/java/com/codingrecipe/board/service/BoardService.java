package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.*;
import com.codingrecipe.board.dto.BoardDTO;
import com.codingrecipe.board.dto.LikeResponseDTO;
import com.codingrecipe.board.exception.CustomException;
import com.codingrecipe.board.exception.ErrorCode;
import com.codingrecipe.board.repository.*;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class BoardService {

    // 의존성 주입
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final BoardFileRepository boardFileRepository;
    private final CategoryRepository categoryRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final Optional<S3UploaderService> s3UploaderService;

    /**
     * 새로운 게시글을 저장 (파일 첨부 처리 포함)
     */
    @Transactional
    public Long save(BoardDTO boardDTO, String email) throws IOException {
        // 작성자 정보 조회, 없으면 USER_NOT_FOUND 예외 발생
        Member writer = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 카테고리 정보 조회, 없으면 CATEGORY_NOT_FOUND 예외 발생
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
        // 첨부 파일이 없는 경우
        if (boardFile == null || boardFile.isEmpty()) {
            boardEntity.setFileAttached(0);
            return boardRepository.save(boardEntity).getId();
        } else { // 첨부 파일이 있는 경우
            boardEntity.setFileAttached(1);
            // 게시글 정보를 먼저 저장하여 ID를 확보
            BoardEntity savedEntity = boardRepository.save(boardEntity);

            // S3 서비스가 활성화된 경우에만 파일 업로드 수행 (local 프로필 대응)
            s3UploaderService.ifPresent(uploader -> {
                try {
                    String originalFilename = boardFile.getOriginalFilename();
                    String storedFileName = uploader.uploadImage(boardFile, "community");
                    // 파일 정보 엔티티 생성 및 저장
                    BoardFileEntity boardFileEntity = BoardFileEntity.toBoardFileEntity(savedEntity, originalFilename, storedFileName);
                    boardFileRepository.save(boardFileEntity);
                } catch (IOException e) {
                    // S3 업로드 실패 시 S3_FILE_UPLOAD_FAILED 예외 발생
                    throw new CustomException(ErrorCode.S3_FILE_UPLOAD_FAILED);
                }
            });

            return savedEntity.getId();
        }
    }

    /**
     * 전체 게시글 목록을 페이징하여 조회
     */
    @Transactional(readOnly = true)
    public Page<BoardDTO> paging(Pageable pageable) {
        Page<BoardEntity> boardEntities = boardRepository.findAll(pageable);
        return boardEntities.map(this::convertToDto);
    }

    /**
     * 특정 카테고리의 게시글 목록을 페이징하여 조회
     */
    @Transactional(readOnly = true)
    public Page<BoardDTO> pagingByCategory(Long categoryId, Pageable pageable) {
        Page<BoardEntity> boardEntities = boardRepository.findByCategoryId(categoryId, pageable);
        return boardEntities.map(this::convertToDto);
    }

    /**
     * 특정 사용자가 작성한 게시글 목록을 페이징하여 조회
     */
    @Transactional(readOnly = true)
    public Page<BoardDTO> pagingByWriter(String email, Pageable pageable) {
        Page<BoardEntity> boardEntities = boardRepository.findByWriter_Email(email, pageable);
        return boardEntities.map(this::convertToDto);
    }

    /**
     * 좋아요 수가 가장 많은 상위 3개 게시글 조회
     */
    @Transactional(readOnly = true)
    public List<BoardDTO> findTop3ByLikes() {
        List<BoardEntity> boardEntityList = boardRepository.findTop3ByOrderByBoardLikesDesc();
        return boardEntityList.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * 특정 게시글의 상세 정보를 조회 (조회수 증가 처리 포함)
     */
    @Transactional
    public BoardDTO findById(Long id) {
        boardRepository.updateHits(id); // 조회수 1 증가
        BoardEntity boardEntity = boardRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
        BoardDTO boardDTO = BoardDTO.toBoardDTO(boardEntity);

        // S3 서비스가 활성화되어 있고 파일이 첨부된 경우, 미리 서명된 URL 생성
        s3UploaderService.ifPresent(uploader -> {
            if (boardEntity.getFileAttached() == 1 && !boardEntity.getBoardFileEntityList().isEmpty()) {
                String storedFileName = boardEntity.getBoardFileEntityList().get(0).getStoredFileName();
                boardDTO.setFileUrl(storedFileName);
            }
        });
        return boardDTO;
    }

    /**
     * 특정 게시글의 정보를 수정 (작성자만 가능)
     */
    public void update(Long id, BoardDTO boardDTO, String email) {
        BoardEntity boardEntity = boardRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        // 수정 권한 확인
        if (boardEntity.getWriter() == null || !boardEntity.getWriter().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        // 카테고리 변경이 있는 경우
        if (boardDTO.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(boardDTO.getCategoryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
            boardEntity.setCategory(category);
        }

        // 제목 및 내용 업데이트
        boardEntity.setBoardTitle(boardDTO.getBoardTitle());
        boardEntity.setBoardContents(boardDTO.getBoardContents());
    }

    /**
     * 특정 게시글을 삭제 (작성자만 가능, S3 파일 포함)
     */
    public void delete(Long boardId, String email) {
        BoardEntity boardEntity = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        // 삭제 권한 확인
        if (boardEntity.getWriter() == null || !boardEntity.getWriter().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        // S3 서비스가 활성화된 경우에만 S3 파일 삭제 로직 수행
        s3UploaderService.ifPresent(uploader -> {
            if (boardEntity.getFileAttached() == 1 && boardEntity.getBoardFileEntityList() != null && !boardEntity.getBoardFileEntityList().isEmpty()) {
                String storedFileName = boardEntity.getBoardFileEntityList().get(0).getStoredFileName();
                uploader.deleteImage(storedFileName);
            }
        });

        boardRepository.delete(boardEntity);
    }

    /**
     * 키워드로 게시글을 검색한 결과를 페이징하여 조회
     */
    @Transactional(readOnly = true)
    public Page<BoardDTO> searchPosts(String keyword, Pageable pageable) {
        Page<BoardEntity> boardEntities = boardRepository.findByBoardTitleContainingOrBoardContentsContaining(keyword, keyword, pageable);
        return boardEntities.map(this::convertToDto);
    }

    /**
     * 게시글 좋아요를 추가하거나 취소 (토글 방식)
     */
    public LikeResponseDTO toggleLike(Long boardId, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        Optional<BoardLikeEntity> like = boardLikeRepository.findByMemberAndBoard(member, board);

        if (like.isPresent()) { // 이미 좋아요를 누른 경우 -> 좋아요 취소
            boardLikeRepository.delete(like.get());
            if (board.getBoardLikes() > 0) {
                board.setBoardLikes(board.getBoardLikes() - 1);
            }
            return new LikeResponseDTO(false, board.getBoardLikes());
        } else { // 좋아요를 누르지 않은 경우 -> 좋아요 추가
            BoardLikeEntity newLike = new BoardLikeEntity();
            newLike.setMember(member);
            newLike.setBoard(board);
            boardLikeRepository.save(newLike);
            board.setBoardLikes(board.getBoardLikes() + 1);
            return new LikeResponseDTO(true, board.getBoardLikes());
        }
    }

    /**
     * 특정 사용자가 좋아요를 누른 모든 게시글 목록을 조회
     */
    @Transactional(readOnly = true)
    public List<BoardDTO> getMyLikedPosts(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        List<BoardLikeEntity> likes = boardLikeRepository.findAllByMember(member);
        return likes.stream()
                .map(like -> convertToDto(like.getBoard()))
                .collect(Collectors.toList());
    }

    /**
     * BoardEntity를 목록 조회용 BoardDTO로 변환하는 내부 메서드
     */
    private BoardDTO convertToDto(BoardEntity board) {
        return BoardDTO.toBoardDTO(board);
    }
    public void deleteBoardByAdmin(Long boardId) {
        BoardEntity boardEntity = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        s3UploaderService.ifPresent(uploader -> {
            if (boardEntity.getFileAttached() == 1 && boardEntity.getBoardFileEntityList() != null && !boardEntity.getBoardFileEntityList().isEmpty()) {
                String storedFileName = boardEntity.getBoardFileEntityList().get(0).getStoredFileName();
                uploader.deleteImage(storedFileName);
            }
        });

        boardRepository.delete(boardEntity);
    }
}