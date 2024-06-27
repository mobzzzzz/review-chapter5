package sparta.nbcamp.reviewchapter5.domain.post

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import sparta.nbcamp.reviewchapter5.domain.post.dto.request.CreatePostRequest
import sparta.nbcamp.reviewchapter5.domain.post.model.Post
import sparta.nbcamp.reviewchapter5.domain.post.model.PostStatus
import sparta.nbcamp.reviewchapter5.domain.post.model.category.Category
import sparta.nbcamp.reviewchapter5.domain.post.model.tag.PostTag
import sparta.nbcamp.reviewchapter5.domain.post.model.tag.Tag
import sparta.nbcamp.reviewchapter5.domain.post.repository.PostRepository
import sparta.nbcamp.reviewchapter5.domain.post.repository.category.CategoryRepository
import sparta.nbcamp.reviewchapter5.domain.post.repository.tag.PostTagRepository
import sparta.nbcamp.reviewchapter5.domain.post.repository.tag.TagRepository
import sparta.nbcamp.reviewchapter5.domain.post.service.PostServiceImpl
import sparta.nbcamp.reviewchapter5.domain.user.model.User
import sparta.nbcamp.reviewchapter5.domain.user.repository.UserRepository
import sparta.nbcamp.reviewchapter5.infra.security.UserPrincipal

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class PostServiceDBTest @Autowired constructor(
    private val postRepository: PostRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
    private val postTagRepository: PostTagRepository,
    private val userRepository: UserRepository
) {

    private lateinit var defaultPostList: List<Post>
    private lateinit var defaultUser: User
    private val postService = PostServiceImpl(postRepository, userRepository)

    @BeforeEach
    fun setUp() {
        val fixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()

        val category = categoryRepository.saveAndFlush(fixtureMonkey.giveMeOne(Category::class.java))
        val tag = tagRepository.saveAndFlush(fixtureMonkey.giveMeOne(Tag::class.java))
        defaultUser = userRepository.saveAndFlush(fixtureMonkey.giveMeOne(User::class.java))

        defaultPostList = postRepository.saveAllAndFlush(
            fixtureMonkey.giveMeBuilder(Post::class.java)
                .set("category", category)
                .set("user", defaultUser)
                .sampleList(10)
        )

        postTagRepository.saveAllAndFlush(defaultPostList.map {
            PostTag(post = it, tag = tag)
        })
    }

    // Postservice의 getPostList 메서드를 테스트하는 코드입니다.
    @Test
    fun `정상적으로 getPostList()를 통한 조회가 되는지 확인`() {
        // given
        val pageable = PageRequest.of(0, 10)

        // when
        val result = postService.getPostList(pageable)

        // then
        result.size shouldBe 10
        result.isLast shouldBe true
        result.totalElements shouldBe 10
        result.content.forEachIndexed { index, postResponse ->
            postResponse.id shouldBe defaultPostList[index].id
            postResponse.title shouldBe defaultPostList[index].title
            postResponse.content shouldBe defaultPostList[index].content
            postResponse.status shouldBe defaultPostList[index].status.name
            postResponse.user.id shouldBe defaultUser.id
            postResponse.user.username shouldBe defaultUser.username
        }
    }

    @Test
    fun `정상적으로 createPost()를 통한 생성이 되는지 확인`() {
        // given
        val request = CreatePostRequest(
            title = "testTitle",
            content = "testContent"
        )

        val testUser = userRepository.findByIdOrNull(defaultUser.id!!)

        val principal = UserPrincipal(testUser?.id!!)

        // when
        val result = postService.createPost(request, principal)

        // then
        result.title shouldBe "testTitle"
        result.content shouldBe "testContent"
        result.status shouldBe PostStatus.NORMAL.name
        result.user.id shouldBe testUser.id
        result.user.username shouldBe testUser.username

        val everyPost = postRepository.findAll()
        everyPost.size shouldBe 11
        everyPost.filter { it.title == "testTitle" }.let {
            it.size shouldBe 1
            it.first().title shouldBe "testTitle"
            it.first().content shouldBe "testContent"
            it.first().user.username shouldBe testUser.username
        }
    }
}