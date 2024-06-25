package sparta.nbcamp.reviewchapter5.domain.post.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import sparta.nbcamp.reviewchapter5.domain.post.model.Post
import sparta.nbcamp.reviewchapter5.domain.post.model.PostStatus
import sparta.nbcamp.reviewchapter5.domain.post.model.QPost
import sparta.nbcamp.reviewchapter5.domain.post.type.PostSearchType
import sparta.nbcamp.reviewchapter5.domain.user.model.QUser
import sparta.nbcamp.reviewchapter5.infra.querydsl.QueryDslSupport
import java.time.LocalDateTime

@Repository
class PostRepositoryImpl : CustomPostRepository, QueryDslSupport() {
    private val post = QPost.post
    private val user = QUser.user

    override fun findByPageableWithUser(pageable: Pageable): Page<Post> {
        val (paginatedPost, totalCount) = queryFactory.basePaging(pageable, post)

        if (paginatedPost.isEmpty()) {
            return PageImpl(emptyList(), pageable, 0L)
        }

        val postList = queryFactory.selectFrom(post)
            .where(post.id.`in`(paginatedPost.map { it.id }))
            .leftJoin(post.user, user)
            .fetchJoin()
            .orderBy(*getOrderSpecifiers(pageable))
            .fetch()

        return PageImpl(postList, pageable, totalCount)
    }

    override fun searchByKeyword(searchType: PostSearchType, keyword: String, pageable: Pageable): Page<Post> {
        val whereClause = BooleanBuilder().and(
            when (searchType) {
                PostSearchType.TITLE_CONTENT -> post.title.contains(keyword).or(post.content.contains(keyword))
                PostSearchType.TITLE -> post.title.contains(keyword)
                PostSearchType.CONTENT -> post.content.contains(keyword)
                PostSearchType.NONE -> null
            }
        )

        val (paginatedPost, totalCount) = queryFactory.basePaging(pageable, post, whereClause)

        if (paginatedPost.isEmpty()) {
            return PageImpl(emptyList(), pageable, 0L)
        }

        val postList = queryFactory.selectFrom(post)
            .where(post.id.`in`(paginatedPost.map { it.id }))
            .leftJoin(post.user, user)
            .fetchJoin()
            .orderBy(*getOrderSpecifiers(pageable))
            .fetch()

        return PageImpl(postList, pageable, totalCount)
    }

    override fun filterPostList(searchCondition: MutableMap<String, String>, pageable: Pageable): Page<Post> {
        val filteredBuilder = filteredBooleanBuilder(searchCondition)

        val (paginatedPost, totalCount) = queryFactory.basePaging(pageable, post, filteredBuilder)

        if (paginatedPost.isEmpty()) {
            return PageImpl(emptyList(), pageable, 0L)
        }

        val postList = queryFactory.selectFrom(post)
            .where(post.id.`in`(paginatedPost.map { it.id }))
            .leftJoin(post.user, user)
            .fetchJoin()
            .orderBy(*getOrderSpecifiers(pageable))
            .fetch()

        return PageImpl(postList, pageable, totalCount)
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
        return post.postTags.any().tag.name.contains(tag)
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