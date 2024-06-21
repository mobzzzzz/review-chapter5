package sparta.nbcamp.reviewchapter5.domain.user.service

import org.springframework.stereotype.Service
import sparta.nbcamp.reviewchapter5.domain.user.repository.UserRepository

@Service
class UserService(
    private val userRepository: UserRepository
)