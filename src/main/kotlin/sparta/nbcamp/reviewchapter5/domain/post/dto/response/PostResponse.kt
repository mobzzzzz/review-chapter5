package sparta.nbcamp.reviewchapter5.domain.post.dto.response

import sparta.nbcamp.reviewchapter5.domain.post.dto.category.response.CategoryResponse
import sparta.nbcamp.reviewchapter5.domain.post.dto.tag.response.TagResponse
import sparta.nbcamp.reviewchapter5.domain.post.model.Post
import sparta.nbcamp.reviewchapter5.domain.user.dto.response.UserResponse
import java.time.LocalDateTime

data class PostResponse(
    val id: Long,
    val title: String,
    val content: String,
    val status: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val user: UserResponse,
    val category: CategoryResponse? = null,
    val tagList: List<TagResponse> = emptyList(),
) {
    companion object {
        fun from(post: Post) = PostResponse(
            id = post.id!!,
            title = post.title,
            content = post.content,
            status = post.status.name,
            createdAt = post.createdAt,
            updatedAt = post.updatedAt,
            user = UserResponse.from(post.user),
            category = post.category.let { CategoryResponse.from(it) },
            tagList = post.tagList.map { TagResponse.from(it) }
        )
    }
}
