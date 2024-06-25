package sparta.nbcamp.reviewchapter5.infra.querydsl

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.dsl.EntityPathBase
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.domain.Pageable

abstract class QueryDslSupport {

    @PersistenceContext
    lateinit var entityManager: EntityManager

    val queryFactory: JPAQueryFactory by lazy {
        JPAQueryFactory(entityManager)
    }

    fun <T> JPAQueryFactory.basePaging(
        pageable: Pageable,
        entityPathBase: EntityPathBase<T>,
        whereClause: BooleanBuilder? = null
    ): Pair<List<T>, Long> {
        val result = this.select(entityPathBase)
            .from(entityPathBase)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .where(whereClause)
            .fetch()

        if (result.isEmpty()) {
            return Pair(emptyList(), 0L)
        }

        val totalCount = this.select(entityPathBase.count())
            .from(entityPathBase)
            .where(whereClause)
            .fetchOne()
            ?: 0L

        return Pair(result, totalCount)
    }
}