package sparta.nbcamp.reviewchapter5.domain.post.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import sparta.nbcamp.reviewchapter5.domain.post.dto.request.CreatePostRequest
import sparta.nbcamp.reviewchapter5.domain.post.dto.response.PostResponse
import sparta.nbcamp.reviewchapter5.domain.post.type.PostSearchType
import sparta.nbcamp.reviewchapter5.infra.security.UserPrincipal

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
    fun searchPostList(searchType: PostSearchType, keyword: String, pageable: Pageable): Page<PostResponse>

    /**
     * 게시글 필터링
     *
     * @param searchCondition 검색 조건
     * @param pageable 페이징 정보
     * @return 필터링된 게시글 목록
     */
    fun filterPostList(searchCondition: MutableMap<String, String>, pageable: Pageable): Page<PostResponse>

    /**
     * 게시글 작성
     *
     * @param request 게시글 작성 요청
     * @param principal 사용자 정보
     * @return 작성된 게시글 정보
     */
    fun createPost(request: CreatePostRequest, principal: UserPrincipal): PostResponse
}