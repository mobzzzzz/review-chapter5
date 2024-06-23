package sparta.nbcamp.reviewchapter5.domain.user.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import sparta.nbcamp.reviewchapter5.domain.user.dto.request.SignInRequest
import sparta.nbcamp.reviewchapter5.domain.user.dto.request.SignUpRequest
import sparta.nbcamp.reviewchapter5.domain.user.dto.response.ExistsUsernameResponse
import sparta.nbcamp.reviewchapter5.domain.user.dto.response.SignInResponse
import sparta.nbcamp.reviewchapter5.domain.user.dto.response.UserResponse
import sparta.nbcamp.reviewchapter5.domain.user.model.User
import sparta.nbcamp.reviewchapter5.domain.user.repository.UserRepository
import sparta.nbcamp.reviewchapter5.infra.security.jwt.JwtPlugin

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,

    private val encoder: PasswordEncoder,
    private val jwtPlugin: JwtPlugin
) : UserService {
    override fun signUp(request: SignUpRequest): UserResponse {
        return check(!userRepository.existsByUsername(request.username)) { "중복된 아이디입니다." }
            .run { userRepository.save(request.toEntity(encoder)) }
            .let { UserResponse.from(it) }
    }

    override fun signIn(request: SignInRequest): SignInResponse {
        return userRepository.findByUsername(request.username)
            .also { validateSignIn(it, request) }
            .let { SignInResponse.from(jwtPlugin, it!!) }
    }

    override fun existsUsername(username: String): ExistsUsernameResponse {
        return userRepository.existsByUsername(username)
            .let { ExistsUsernameResponse.of(it) }
    }

    private fun validateSignIn(user: User?, request: SignInRequest) {
        if (user == null || !encoder.matches(request.password, user.password)) {
            throw IllegalStateException("아이디 또는 패스워드를 확인해주세요.")
        }
    }
}