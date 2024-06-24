package sparta.nbcamp.reviewchapter5.domain.common.configuration

import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import sparta.nbcamp.reviewchapter5.domain.post.model.Post
import sparta.nbcamp.reviewchapter5.domain.post.model.tag.PostTag
import sparta.nbcamp.reviewchapter5.domain.post.repository.PostRepository
import sparta.nbcamp.reviewchapter5.domain.post.repository.category.CategoryRepository
import sparta.nbcamp.reviewchapter5.domain.post.repository.tag.PostTagRepository
import sparta.nbcamp.reviewchapter5.domain.post.repository.tag.TagRepository
import sparta.nbcamp.reviewchapter5.domain.user.model.User
import sparta.nbcamp.reviewchapter5.domain.user.repository.UserRepository

@Configuration
class PostInitializer(
    val postRepository: PostRepository,
    val userRepository: UserRepository,
    val categoryRepository: CategoryRepository,
    val tagRepository: TagRepository,
    val postTagRepository: PostTagRepository
) {
    @Bean
    @Order(3)
    fun defaultPostList() = ApplicationRunner {
        val tagList = tagRepository.findAll()
        val testUser = userRepository.save(
            User(
                username = "testuser",
                password = "password123",
            )
        )

        val posts = (1..10).map {
            val randomCategory = categoryRepository.findAll().shuffled().first()

            Post(
                title = "Post $it",
                content = "Content $it",
                user = testUser,
                category = randomCategory,
            )
        }

        postRepository.saveAll(posts)

        val postTagList = posts.flatMap { post ->
            val randomTag = tagList.shuffled().take(2)

            randomTag.map {
                PostTag(
                    post = post,
                    tag = it
                )
            }
        }

        postTagRepository.saveAll(postTagList)
    }
}