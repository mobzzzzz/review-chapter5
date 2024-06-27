package sparta.nbcamp.reviewchapter5.domain.post

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import sparta.nbcamp.reviewchapter5.config.TestSecurityConfig
import sparta.nbcamp.reviewchapter5.domain.post.controller.PostController
import sparta.nbcamp.reviewchapter5.domain.post.dto.response.PostResponse
import sparta.nbcamp.reviewchapter5.domain.post.service.PostService
import sparta.nbcamp.reviewchapter5.infra.security.jwt.JwtPlugin

@WebMvcTest(PostController::class)
@AutoConfigureMockMvc
@ExtendWith(MockKExtension::class)
@ActiveProfiles("test")
@Import(PostControllerTest.TestConfig::class, TestSecurityConfig::class)
class PostControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val postService: PostService
) : DescribeSpec({
    extensions(SpringExtension)

    afterContainer {
        clearAllMocks()
    }

    describe("GET /posts/{id}는") {
        context("존재하는 ID를 요청을 보낼 때") {
            it("200 status code를 응답해야한다.") {
                // given
                val postId = 1L
                val fixtureMonkey = FixtureMonkey.builder()
                    .plugin(KotlinPlugin())
                    .build()

                every { postService.getPost(postId) } returns fixtureMonkey.giveMeOne(PostResponse::class.java)

                // when
                val result = mockMvc.perform(
                    get("/posts/$postId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                ).andReturn()

                // then
                result.response.status shouldBe 200

                val response = result.response.contentAsString
                if (response.isNotEmpty()) {
                    val postResponse = jacksonObjectMapper().readValue(response, PostResponse::class.java)
                    postResponse.id shouldBe postId
                }
            }
        }
    }

    describe("GET /posts는") {
        context("모든 게시글을 요청을 보낼 때") {
            it("200 status code를 응답해야한다.") {
                // given
                val fixtureMonkey = FixtureMonkey.builder()
                    .plugin(KotlinPlugin())
                    .build()
                val pageable = PageRequest.of(0, 10)

                every { postService.getPostList(pageable) } returns PageImpl(
                    fixtureMonkey.giveMe(
                        PostResponse::class.java,
                        10
                    ), pageable, 10
                )

                // when
                val result = mockMvc.perform(
                    get("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                ).andReturn()

                // then
                result.response.status shouldBe 200

                val response = result.response.contentAsString
                if (response.isNotEmpty()) {
                    val postResponseList = jacksonObjectMapper().readValue(response, Array<PostResponse>::class.java)
                    postResponseList.size shouldBe 10
                }
            }
        }
    }
}) {
    @MockBean
    lateinit var jwtPlugin: JwtPlugin

    @TestConfiguration
    class TestConfig {
        @Bean
        fun postService(): PostService = mockk()
    }
}