package sparta.nbcamp.reviewchapter5.domain.post.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import sparta.nbcamp.reviewchapter5.domain.post.dto.response.PostResponse

interface PostService {
    /**
     * 게시글 목록 조회
     *
     * @param pageable 페이징 정보
     * @return 게시글 목록
     */
    fun getPostList(pageable: Pageable): Page<PostResponse>

    /**
     * 게시글 상세 조회
     *
     * @param postId 게시글 ID
     * @return 게시글 상세 정보
     */
    fun getPost(postId: Long): PostResponse

    /**
     * 게시글 검색
     *
     * @param searchType 검색 타입
     * @param keyword 검색어
     * @param pageable 페이징 정보
     * @return 검색된 게시글 목록
     */
    fun searchPostList(searchType: String, keyword: String, pageable: Pageable): Page<PostResponse>
}