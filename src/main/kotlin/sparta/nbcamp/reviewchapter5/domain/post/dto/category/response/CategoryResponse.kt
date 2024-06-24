package sparta.nbcamp.reviewchapter5.domain.post.dto.category.response

import sparta.nbcamp.reviewchapter5.domain.post.model.category.Category

data class CategoryResponse(
    val id: Long,
    val name: String
) {
    companion object {
        fun from(category: Category) = CategoryResponse(
            id = category.id!!,
            name = category.name
        )
    }
}
