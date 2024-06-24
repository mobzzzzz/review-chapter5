package sparta.nbcamp.reviewchapter5.domain.common.configuration

import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import sparta.nbcamp.reviewchapter5.domain.post.model.category.Category
import sparta.nbcamp.reviewchapter5.domain.post.repository.category.CategoryRepository

@Configuration
class CategoryInitializer(
    val categoryRepository: CategoryRepository
) {
    @Bean
    @Order(2)
    fun defaultCategoryList() = ApplicationRunner {
        val categories = listOf(
            "Free",
            "Question",
            "Share",
            "Notice",
        )

        categoryRepository.saveAll(categories.map { Category(name = it) })
    }
}