package sparta.nbcamp.reviewchapter5.domain.user.repository

import org.springframework.stereotype.Repository
import sparta.nbcamp.reviewchapter5.domain.user.model.QUser
import sparta.nbcamp.reviewchapter5.infra.querydsl.QueryDslSupport

@Repository
class UserRepositoryImpl : CustomUserRepository, QueryDslSupport() {

    private val user = QUser.user
}