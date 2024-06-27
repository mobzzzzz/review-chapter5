package sparta.nbcamp.reviewchapter5.domain.post.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.JPAExpressions
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import sparta.nbcamp.reviewchapter5.domain.post.model.Post
import sparta.nbcamp.reviewchapter5.domain.post.model.PostStatus
import sparta.nbcamp.reviewchapter5.domain.post.model.QPost
import sparta.nbcamp.reviewchapter5.domain.post.model.category.QCategory.category
import sparta.nbcamp.reviewchapter5.domain.post.model.tag.PostTag
import sparta.nbcamp.reviewchapter5.domain.post.model.tag.QPostTag
import sparta.nbcamp.reviewchapter5.domain.post.model.tag.QTag.tag
import sparta.nbcamp.reviewchapter5.domain.post.type.PostSearchType
import sparta.nbcamp.reviewchapter5.domain.user.model.QUser
import sparta.nbcamp.reviewchapter5.infra.querydsl.QueryDslSupport
import java.time.LocalDateTime

@Repository
class PostRepositoryImpl : CustomPostRepository, QueryDslSupport() {
    private val post = QPost.post
    private val user = QUser.user
    private val postTag = QPostTag.postTag

    override fun findByPageableWithUser(pageable: Pageable): Page<Pair<Post, List<PostTag>>> {
        val (paginatedPostIds, totalCount) = basePagingIds(pageable)

        if (paginatedPostIds.isEmpty()) {
            return PageImpl(emptyList(), pageable, 0L)
        }

        return PageImpl(joinedPostListWithTagByIds(paginatedPostIds), pageable, totalCount)
    }

    override fun searchByKeyword(
        searchType: PostSearchType,
        keyword: String,
        pageable: Pageable
    ): Page<Pair<Post, List<PostTag>>> {
        val whereClause = BooleanBuilder().and(
            when (searchType) {
                PostSearchType.TITLE_CONTENT -> post.title.contains(keyword).or(post.content.contains(keyword))
                PostSearchType.TITLE -> post.title.contains(keyword)
                PostSearchType.CONTENT -> post.content.contains(keyword)
                PostSearchType.NONE -> null
            }
        )

        val (paginatedPostIds, totalCount) = basePagingIds(pageable, whereClause)

        if (paginatedPostIds.isEmpty()) {
            return PageImpl(emptyList(), pageable, 0L)
        }

        return PageImpl(joinedPostListWithTagByIds(paginatedPostIds), pageable, totalCount)
    }

    override fun filterPostList(
        searchCondition: MutableMap<String, String>,
        pageable: Pageable
    ): Page<Pair<Post, List<PostTag>>> {
        val filteredBuilder = filteredBooleanBuilder(searchCondition)

        val (paginatedPostIds, totalCount) = basePagingIds(pageable, filteredBuilder)

        if (paginatedPostIds.isEmpty()) {
            return PageImpl(emptyList(), pageable, 0L)
        }

        return PageImpl(joinedPostListWithTagByIds(paginatedPostIds), pageable, totalCount)
    }

    private fun basePagingIds(
        pageable: Pageable,
        whereClause: BooleanBuilder? = null
    ): Pair<List<Long>, Long> {
        val result = queryFactory.select(post.id)
            .from(post)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .where(whereClause)
            .fetch()

        if (result.isEmpty()) {
            return Pair(emptyList(), 0L)
        }

        val totalCount = queryFactory.select(post.count())
            .from(post)
            .where(whereClause)
            .fetchOne()
            ?: 0L

        return Pair(result, totalCount)
    }

    private fun joinedPostListWithTagByIds(paginatedPostIds: List<Long>): List<Pair<Post, List<PostTag>>> {
        return queryFactory
            .select(post, postTag, tag)
            .from(post)
            .leftJoin(post.user, user).fetchJoin()
            .leftJoin(post.category, category).fetchJoin()
            .leftJoin(postTag).on(post.id.eq(postTag.post.id))
            .leftJoin(postTag.tag, tag).fetchJoin()
            .where(post.id.`in`(paginatedPostIds))
            .fetch()
            .groupBy { it.get(post) }
            .mapValues { it.value.map { tuple -> tuple.get(postTag)!! } }
            .map { Pair(it.key!!, it.value) }
    }

    private fun filteredBooleanBuilder(searchCondition: Map<String, String>): BooleanBuilder {
        val builder = BooleanBuilder()

        searchCondition["title"]?.let { builder.and(titleLike(it)) }
        searchCondition["category"]?.let { builder.and(categoryEq(it)) }
        searchCondition["tag"]?.let { builder.and(tagLike(it)) }
        searchCondition["status"]?.let { builder.and(stateEq(it)) }
        searchCondition["daysAgo"]?.let { builder.and(withInDays(it)) }

        return builder
    }

    private fun titleLike(title: String): BooleanExpression {
        return post.title.contains(title)
    }

    private fun categoryEq(category: String): BooleanExpression {
        return post.category.name.eq(category)
    }

    private fun tagLike(tag: String): BooleanExpression {
        val subQuery = JPAExpressions.select(postTag.post.id)
            .from(postTag)
            .where(postTag.tag.name.contains(tag))

        return post.id.`in`(subQuery)
    }

    private fun stateEq(stateCode: String): BooleanExpression? {
        return try {
            val status = PostStatus.valueOf(stateCode)
            post.status.eq(status)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun withInDays(daysAgo: String): BooleanExpression {
        val daysAgoDate = LocalDateTime.now().minusDays(daysAgo.toLong())
        val startDate = daysAgoDate.let { LocalDateTime.of(it.year, it.month, it.dayOfMonth, 0, 0, 0) }
        val midnightDate = daysAgoDate.let { LocalDateTime.of(it.year, it.month, it.dayOfMonth, 23, 59, 59) }

        return post.createdAt.between(startDate, midnightDate)
    }

    private fun getOrderSpecifiers(pageable: Pageable): Array<OrderSpecifier<*>> {
        val orderSpecifiers = mutableListOf<OrderSpecifier<*>>()

        pageable.sort.forEach { order ->
            val orderSpecifier = when (order.property) {
                "createdAt" -> post.createdAt
                "id" -> post.id
                "title" -> post.title
                "username" -> user.username
                else -> post.id
            }

            orderSpecifiers.add(if (order.isAscending) orderSpecifier.asc() else orderSpecifier.desc())
        }

        return orderSpecifiers.toTypedArray()
    }
}