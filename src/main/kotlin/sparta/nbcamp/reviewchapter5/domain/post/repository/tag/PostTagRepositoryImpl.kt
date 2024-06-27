package sparta.nbcamp.reviewchapter5.domain.post.repository.tag

import org.springframework.stereotype.Repository
import sparta.nbcamp.reviewchapter5.domain.post.model.tag.PostTag
import sparta.nbcamp.reviewchapter5.domain.post.model.tag.QPostTag
import sparta.nbcamp.reviewchapter5.domain.post.model.tag.QTag
import sparta.nbcamp.reviewchapter5.infra.querydsl.QueryDslSupport

@Repository
class PostTagRepositoryImpl : PostTagRepositoryCustom, QueryDslSupport() {
    val postTag = QPostTag.postTag
    val tag = QTag.tag

    override fun findTagByPostIdIn(postIds: List<Long>): List<PostTag> {
        return queryFactory
            .selectFrom(postTag)
            .join(postTag.tag, tag).fetchJoin()
            .where(postTag.post.id.`in`(postIds))
            .fetch()
    }
}