package sparta.nbcamp.reviewchapter5.infra.querydsl

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext

abstract class QueryDslSupport {

    @PersistenceContext
    lateinit var entityManager: EntityManager

    val queryFactory: JPAQueryFactory by lazy {
        JPAQueryFactory(entityManager)
    }
}