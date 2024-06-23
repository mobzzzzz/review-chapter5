package sparta.nbcamp.reviewchapter5.domain.post.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.OrderSpecifier
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import sparta.nbcamp.reviewchapter5.domain.post.model.Post
import sparta.nbcamp.reviewchapter5.domain.post.model.QPost
import sparta.nbcamp.reviewchapter5.domain.user.model.QUser
import sparta.nbcamp.reviewchapter5.infra.querydsl.QueryDslSupport

@Repository
class PostRepositoryImpl : CustomPostRepository, QueryDslSupport() {
    private val post = QPost.post
    private val user = QUser.user

    override fun findByPageableWithUser(pageable: Pageable): Page<Post> {
        val postIds = queryFactory.select(post.id)
            .from(post)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        if (postIds.isEmpty()) {
            return PageImpl(emptyList(), pageable, 0L)
        }

        val postList = queryFactory.selectFrom(post)
            .where(post.id.`in`(postIds))
            .leftJoin(post.user, user)
            .fetchJoin()
            .orderBy(*getOrderSpecifiers(pageable))
            .fetch()

        val totalCount = queryFactory.select(post.count())
            .from(post)
            .fetchOne()
            ?: 0L

        return PageImpl(postList, pageable, totalCount)
    }

    override fun searchByKeyword(searchType: String, keyword: String, pageable: Pageable): Page<Post> {
        val whereClause = when (searchType) {
            "title_content" -> post.title.contains(keyword).or(post.content.contains(keyword))
            "title" -> post.title.contains(keyword)
            "content" -> post.content.contains(keyword)
            else -> BooleanBuilder()
        }

        val postIds = queryFactory.select(post.id)
            .from(post)
            .where(whereClause)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        if (postIds.isEmpty()) {
            return PageImpl(emptyList(), pageable, 0L)
        }

        val postList = queryFactory.selectFrom(post)
            .where(post.id.`in`(postIds))
            .leftJoin(post.user, user)
            .fetchJoin()
            .orderBy(*getOrderSpecifiers(pageable))
            .fetch()

        val totalCount = queryFactory.select(post.count())
            .from(post)
            .where(whereClause)
            .fetchOne()
            ?: 0L

        return PageImpl(postList, pageable, totalCount)
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