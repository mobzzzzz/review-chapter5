package sparta.nbcamp.reviewchapter5.domain.post.dto.category.response

import sparta.nbcamp.reviewchapter5.domain.post.model.category.Category

data class CategoryResponse(
    val id: Long,
    val name: String
) {
    companion object {
        fun from(category: Category?): CategoryResponse? {
            return category?.let {
                CategoryResponse(
                    id = it.id!!,
                    name = it.name
                )
            }
        }
    }
}
