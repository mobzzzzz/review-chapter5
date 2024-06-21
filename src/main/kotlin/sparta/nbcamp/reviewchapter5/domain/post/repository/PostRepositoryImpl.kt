package sparta.nbcamp.reviewchapter5.domain.post.repository

import org.springframework.stereotype.Repository
import sparta.nbcamp.reviewchapter5.infra.querydsl.QueryDslSupport

@Repository
class PostRepositoryImpl : CustomPostRepository, QueryDslSupport()