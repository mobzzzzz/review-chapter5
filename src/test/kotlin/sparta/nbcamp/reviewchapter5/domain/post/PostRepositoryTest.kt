package sparta.nbcamp.reviewchapter5.domain.post

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import sparta.nbcamp.reviewchapter5.domain.post.model.Post
import sparta.nbcamp.reviewchapter5.domain.post.model.category.Category
import sparta.nbcamp.reviewchapter5.domain.post.model.tag.PostTag
import sparta.nbcamp.reviewchapter5.domain.post.model.tag.Tag
import sparta.nbcamp.reviewchapter5.domain.post.repository.PostRepository
import sparta.nbcamp.reviewchapter5.domain.post.repository.category.CategoryRepository
import sparta.nbcamp.reviewchapter5.domain.post.repository.tag.PostTagRepository
import sparta.nbcamp.reviewchapter5.domain.post.repository.tag.TagRepository
import sparta.nbcamp.reviewchapter5.domain.post.type.PostSearchType
import sparta.nbcamp.reviewchapter5.domain.user.model.User
import sparta.nbcamp.reviewchapter5.domain.user.repository.UserRepository

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class PostRepositoryTest @Autowired constructor(
    private val postRepository: PostRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
    private val postTagRepository: PostTagRepository,
    private val userRepository: UserRepository
) {
    private lateinit var defaultPostList: List<Post>

    @BeforeEach
    fun setUp() {
        val category = categoryRepository.saveAndFlush(DEFAULT_CATEGORY)
        val tag = tagRepository.saveAndFlush(DEFAULT_TAG)
        val userList = userRepository.saveAllAndFlush(DEFAULT_USER_LIST)

        defaultPostList = postRepository.saveAllAndFlush((1..10).map { index ->
            Post(
                title = "sample${index}Title",
                content = "sample${index}Content",
                user = if (index % 2 == 0) userList[0] else userList[1],
                category = category
            )
        })

        defaultPostList.forEach { post ->
            postTagRepository.saveAndFlush(PostTag(post = post, tag = tag))
        }
    }

    @Test
    fun `SearchType 이 NONE 일 경우 전체 데이터 조회되는지 확인`() {
        // WHEN
        val result1 = postRepository.searchByKeyword(PostSearchType.NONE, "", Pageable.ofSize(10))
        val result2 = postRepository.searchByKeyword(PostSearchType.NONE, "", Pageable.ofSize(6))
        val result3 = postRepository.searchByKeyword(PostSearchType.NONE, "", Pageable.ofSize(15))

        // THEN
        result1.content.size shouldBe 10
        result2.content.size shouldBe 6
        result3.content.size shouldBe 10
    }

    @Test
    fun `SearchType 이 NONE 이 아닌 경우 Keyword 에 의해 검색되는지 결과 확인`() {
        // WHEN
        val result = postRepository.searchByKeyword(PostSearchType.TITLE_CONTENT, "sample1", Pageable.ofSize(10))

        // THEN
        result.content.size shouldBe 2
    }

    @Test
    fun `Keyword 에 의해 조회된 결과가 0건일 경우 결과 확인`() {
        // WHEN
        val result = postRepository.searchByKeyword(PostSearchType.TITLE_CONTENT, "sample11", Pageable.ofSize(10))

        // THEN
        result.content.size shouldBe 0
    }

    @Test
    fun `조회된 결과가 10개, PageSize 6일 때 0Page 결과 확인`() {
        // WHEN
        val result = postRepository.searchByKeyword(PostSearchType.TITLE_CONTENT, "sample", PageRequest.of(0, 6))

        // THEN
        result.content.size shouldBe 6
        result.isLast shouldBe false
        result.totalPages shouldBe 2
        result.number shouldBe 0
        result.totalElements shouldBe 10
    }

    @Test
    fun `조회된 결과가 10개, PageSize 6일 때 1Page 결과 확인`() {
        // WHEN
        val result = postRepository.searchByKeyword(
            PostSearchType.TITLE_CONTENT,
            "sample",
            PageRequest.of(1, 6)
        )

        // THEN
        result.content.size shouldBe 4
        result.isLast shouldBe true
        result.totalPages shouldBe 2
        result.number shouldBe 1
        result.totalElements shouldBe 10
    }

    companion object {
        val DEFAULT_CATEGORY = Category(name = "Default Category")
        val DEFAULT_TAG = Tag(name = "Default Tag")
        val DEFAULT_USER_LIST = listOf(
            User(username = "User1", password = "aaaa"),
            User(username = "User2", password = "aaaa")
        )
    }
}