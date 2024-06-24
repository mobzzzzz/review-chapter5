package sparta.nbcamp.reviewchapter5.domain.post.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import sparta.nbcamp.reviewchapter5.domain.common.StopWatch
import sparta.nbcamp.reviewchapter5.domain.post.dto.request.CreatePostRequest
import sparta.nbcamp.reviewchapter5.domain.post.dto.response.PostResponse
import sparta.nbcamp.reviewchapter5.domain.post.repository.PostRepository
import sparta.nbcamp.reviewchapter5.domain.user.repository.UserRepository
import sparta.nbcamp.reviewchapter5.exception.ModelNotFoundException
import sparta.nbcamp.reviewchapter5.infra.security.UserPrincipal

@Service
class PostServiceImpl(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : PostService {

    @StopWatch
    override fun getPostList(pageable: Pageable): Page<PostResponse> {
        return postRepository.findByPageableWithUser(pageable).map { PostResponse.from(it) }
    }

    @StopWatch
    override fun getPost(postId: Long): PostResponse {
        return postRepository.findByIdOrNull(postId)
            ?.let { PostResponse.from(it) }
            ?: throw ModelNotFoundException("Post", postId)
    }

    @StopWatch
    override fun searchPostList(searchType: String, keyword: String, pageable: Pageable): Page<PostResponse> {
        return postRepository.searchByKeyword(searchType, keyword, pageable).map { PostResponse.from(it) }
    }

    override fun createPost(request: CreatePostRequest, principal: UserPrincipal): PostResponse {
        return userRepository.findByIdOrNull(principal.id)
            ?.let { request.toEntity(it) }
            ?.also { postRepository.save(it) }
            ?.let { PostResponse.from(it) }
            ?: throw ModelNotFoundException("User", principal.id)
    }
}
