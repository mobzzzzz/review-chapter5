package sparta.nbcamp.reviewchapter5.domain.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import sparta.nbcamp.reviewchapter5.domain.user.model.User

interface UserRepository : JpaRepository<User, Long>, CustomUserRepository