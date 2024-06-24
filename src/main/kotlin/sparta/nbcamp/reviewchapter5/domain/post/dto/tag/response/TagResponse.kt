package sparta.nbcamp.reviewchapter5.domain.post.dto.tag.response

import sparta.nbcamp.reviewchapter5.domain.post.model.tag.Tag

data class TagResponse(
    val id: Long,
    val name: String
) {
    companion object {
        fun from(tag: Tag) = TagResponse(
            id = tag.id!!,
            name = tag.name
        )
    }
}