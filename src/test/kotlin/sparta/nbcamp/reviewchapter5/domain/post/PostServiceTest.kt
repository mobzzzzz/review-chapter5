package sparta.nbcamp.reviewchapter5.domain.post

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import sparta.nbcamp.reviewchapter5.domain.post.dto.request.CreatePostRequest
import sparta.nbcamp.reviewchapter5.domain.post.model.Post
import sparta.nbcamp.reviewchapter5.domain.post.repository.PostRepository
import sparta.nbcamp.reviewchapter5.domain.post.repository.tag.PostTagRepository
import sparta.nbcamp.reviewchapter5.domain.post.service.PostService
import sparta.nbcamp.reviewchapter5.domain.post.service.PostServiceImpl
import sparta.nbcamp.reviewchapter5.domain.user.model.User
import sparta.nbcamp.reviewchapter5.domain.user.repository.UserRepository
import sparta.nbcamp.reviewchapter5.infra.security.UserPrincipal
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
@Import(PostControllerTest.TestConfig::class)
@ActiveProfiles("test")
class PostServiceTest(
    @MockkBean private val postRepository: PostRepository,
    @MockkBean private val postTagRepository: PostTagRepository,
    @MockkBean private val userRepository: UserRepository
) : BehaviorSpec({
    extension(SpringExtension)

    afterContainer {
        clearAllMocks()
    }

    val postService = PostServiceImpl(postRepository, postTagRepository, userRepository)

    given("어떤 사용자가 게시글 작성을 요청하고") {
        val fixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()

        val sampleUser = fixtureMonkey.giveMeBuilder(User::class.java)
            .set("id", 1L)
            .sample()
        val createPostRequest = fixtureMonkey.giveMeOne(CreatePostRequest::class.java)
        val principal = fixtureMonkey.giveMeBuilder(UserPrincipal::class.java)
            .set("id", sampleUser.id)
            .sample()
        val samplePostId = fixtureMonkey.giveMeOne(Long::class.java)

        every { userRepository.findByIdOrNull(principal.id) } returns sampleUser

        `when`("게시글을 작성에 성공하면") {
            every { postRepository.save(any()) } answers {
                firstArg<Post>().apply {
                    id = samplePostId
                    createdAt = LocalDateTime.of(2021, 1, 1, 0, 0)
                    updatedAt = LocalDateTime.of(2021, 1, 1, 0, 0)
                }
            }

            val result = postService.createPost(createPostRequest, principal)

            then("요청에 대한 게시글이 생성된다") {
                result shouldNotBe null
                result.id shouldBe samplePostId
                result.title shouldBe createPostRequest.title
                result.content shouldBe createPostRequest.content
            }
        }
    }
}) {
    @TestConfiguration
    class TestConfig {
        @Bean
        fun postService(): PostService = mockk()
    }
}