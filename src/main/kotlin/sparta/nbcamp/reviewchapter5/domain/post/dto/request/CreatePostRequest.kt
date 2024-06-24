package sparta.nbcamp.reviewchapter5.domain.post.dto.request

import sparta.nbcamp.reviewchapter5.domain.post.model.Post
import sparta.nbcamp.reviewchapter5.domain.user.model.User

data class CreatePostRequest(
    val title: String,
    val content: String
) {
    init {
        validate()
    }

    fun validate() {
        require(title.length < 500) { "Title must be shorter than 500" }
        require(content.length < 5000) { "Content must be shorter than 5000" }
    }

    fun toEntity(user: User): Post {
        return Post(
            title = title,
            content = content,
            user = user
        )
    }
}
