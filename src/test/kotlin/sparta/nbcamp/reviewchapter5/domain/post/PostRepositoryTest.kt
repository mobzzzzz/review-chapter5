package sparta.nbcamp.reviewchapter5.domain.post

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import net.jqwik.api.Arbitraries
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.random.Random

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
    private lateinit var defaultPostTagList: List<PostTag>
    private lateinit var defaultCategory: Category
    private lateinit var defaultTag: Tag

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @BeforeEach
    fun setUp() {
        val fixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()

        defaultCategory = categoryRepository.saveAndFlush(
            fixtureMonkey.giveMeBuilder(Category::class.java)
                .set("name", Arbitraries.strings().withCharRange('a', 'z').ofMinLength(5).ofMaxLength(10))
                .sample()
        )
        defaultTag = tagRepository.saveAndFlush(
            fixtureMonkey.giveMeBuilder(Tag::class.java)
                .set("name", Arbitraries.strings().withCharRange('a', 'z').ofMinLength(5).ofMaxLength(10))
                .sample()
        )
        val user = userRepository.saveAndFlush(fixtureMonkey.giveMeOne(User::class.java))

        defaultPostList = postRepository.saveAllAndFlush(
            fixtureMonkey.giveMeBuilder(Post::class.java)
                .set("category", defaultCategory)
                .set("user", user)
                .set("title", Arbitraries.strings().withCharRange('a', 'z').ofMinLength(10).ofMaxLength(20))
                .set("content", Arbitraries.strings().withCharRange('a', 'z').ofMinLength(20).ofMaxLength(30))
                .sampleList(10)
        )

        val now = LocalDateTime.now()
        defaultPostList.forEach {

            val randomDate = LocalDateTime.of(
                LocalDate.of(now.year, now.month, now.dayOfMonth).minusDays(Random.nextLong(1, 10)),
                LocalTime.of(Random.nextInt(0, 24), Random.nextInt(0, 60), Random.nextInt(0, 60))
            )

            entityManager.createNativeQuery("UPDATE post SET created_at = :randomDate, updated_at = :randomDate WHERE id = :id")
                .setParameter("randomDate", randomDate)
                .setParameter("id", it.id)
                .executeUpdate()

            entityManager.refresh(it)
        }

        defaultPostTagList = postTagRepository.saveAllAndFlush(defaultPostList.map {
            PostTag(post = it, tag = defaultTag)
        })
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
        val result = postRepository.searchByKeyword(PostSearchType.TITLE_CONTENT, "a", Pageable.ofSize(10))

        // THEN
        result.content.size shouldBe defaultPostList.count { it.title.contains("a") || it.content.contains("a") }
    }

    @Test
    fun `Keyword 에 의해 조회된 결과가 0건일 경우 결과 확인`() {
        // WHEN
        val result = postRepository.searchByKeyword(PostSearchType.TITLE_CONTENT, "1null2", Pageable.ofSize(10))

        // THEN
        result.content.size shouldBe 0
    }

    @Test
    fun `조회된 결과가 10개, PageSize 6일 때 0Page 결과 확인`() {
        // WHEN
        val result = postRepository.searchByKeyword(PostSearchType.TITLE_CONTENT, "", PageRequest.of(0, 6))

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
            "",
            PageRequest.of(1, 6)
        )

        // THEN
        result.content.size shouldBe 4
        result.isLast shouldBe true
        result.totalPages shouldBe 2
        result.number shouldBe 1
        result.totalElements shouldBe 10
    }

    @Test
    fun `태그가 포함된 게시글이 필터링되는지 확인`() {
        // GIVEN
        val tagKeyword = defaultTag.name.substring(0, 2)
        val searchCondition = mutableMapOf(
            "tag" to tagKeyword
        )

        // WHEN
        val result = postRepository.filterPostList(searchCondition, Pageable.ofSize(10))

        // THEN
        result.totalElements shouldBe defaultPostTagList
            .groupBy { it.post.id }
            .count { (_, postTagList) -> postTagList.any { it.tag.name.contains(tagKeyword) } }
    }

    @Test
    fun `카테고리가 정확히 해당하는 게시글이 필터링되는지 확인`() {
        // GIVEN
        val searchCondition = mutableMapOf(
            "category" to defaultCategory.name
        )

        // WHEN
        val result = postRepository.filterPostList(searchCondition, Pageable.ofSize(10))

        // THEN
        result.totalElements shouldBe defaultPostList.count { it.category?.name == searchCondition["category"] }
    }

    @Test
    fun `N일전에 정확히 해당하는 게시글이 필터링되는지 확인`() {
        // GIVEN
        val randomDaysAgo = Random.nextLong(1, 10)
        val randomDateDaysAgo = LocalDateTime.now().minusDays(randomDaysAgo)
        val searchCondition = mutableMapOf(
            "daysAgo" to randomDaysAgo.toString()
        )

        // WHEN
        val result = postRepository.filterPostList(searchCondition, Pageable.ofSize(10))

        // THEN
        result.totalElements shouldBe defaultPostList.count {
            it.createdAt.toLocalDate().year == randomDateDaysAgo.toLocalDate().year
                && it.createdAt.toLocalDate().month == randomDateDaysAgo.month
                && it.createdAt.toLocalDate().dayOfMonth == randomDateDaysAgo.dayOfMonth
        }
    }
}