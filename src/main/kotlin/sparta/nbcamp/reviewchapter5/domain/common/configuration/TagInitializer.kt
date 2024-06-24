package sparta.nbcamp.reviewchapter5.domain.common.configuration

import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import sparta.nbcamp.reviewchapter5.domain.post.model.tag.Tag
import sparta.nbcamp.reviewchapter5.domain.post.repository.tag.TagRepository

@Configuration
class TagInitializer(
    private val tagRepository: TagRepository
) {
    @Bean
    @Order(1)
    fun defaultTagList() = ApplicationRunner {
        val tagList = listOf(
            "Kotlin",
            "Spring",
            "JPA",
            "TIL",
        )

        tagRepository.saveAll(tagList.map { Tag(name = it) })
    }
}