package sparta.nbcamp.reviewchapter5.domain.post.dto.response

import sparta.nbcamp.reviewchapter5.domain.post.model.Post
import sparta.nbcamp.reviewchapter5.domain.user.dto.response.UserResponse

data class PostResponse(
    val id: Long,
    val title: String,
    val content: String,
    val user: UserResponse
) {
    companion object {
        fun from(post: Post) = PostResponse(
            id = post.id!!,
            title = post.title,
            content = post.content,
            user = UserResponse.from(post.user)
        )
    }
}
