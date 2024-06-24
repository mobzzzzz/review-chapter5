package sparta.nbcamp.reviewchapter5.domain.post.repository.category

import org.springframework.data.jpa.repository.JpaRepository
import sparta.nbcamp.reviewchapter5.domain.post.model.category.Category

interface CategoryRepository : JpaRepository<Category, Long> {
    fun findByName(randomCategoryName: String): Category
}
