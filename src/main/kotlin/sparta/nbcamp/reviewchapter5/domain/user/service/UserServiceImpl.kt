package sparta.nbcamp.reviewchapter5.domain.user.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import sparta.nbcamp.reviewchapter5.domain.user.dto.request.SignUpRequest
import sparta.nbcamp.reviewchapter5.domain.user.dto.response.ExistsUsernameResponse
import sparta.nbcamp.reviewchapter5.domain.user.dto.response.UserResponse
import sparta.nbcamp.reviewchapter5.domain.user.repository.UserRepository

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,

    private val encoder: PasswordEncoder
) : UserService {
    override fun signup(request: SignUpRequest): UserResponse {
        return check(!userRepository.existsByUsername(request.username)) { "중복된 아이디입니다." }
            .run { userRepository.save(request.toEntity(encoder)) }
            .let { UserResponse.from(it) }
    }

    override fun existsUsername(username: String): ExistsUsernameResponse {
        return userRepository.existsByUsername(username)
            .let { ExistsUsernameResponse.of(it) }
    }
}