package sparta.nbcamp.reviewchapter5.domain.user.dto.request

import org.springframework.security.crypto.password.PasswordEncoder
import sparta.nbcamp.reviewchapter5.domain.user.model.User

data class SignUpRequest(
    val username: String,
    val password: String,
    val confirmPassword: String
) {
    fun validate() {
        require(username.length > 3) { "Username must be longer than 3" }
        require(username.matches(Regex("^[a-zA-Z0-9]*$"))) { "Username only alphabet and number allowed" }
        require(password.length > 4) { "Password must be longer than 4" }
        require(!password.contains(username)) { "Password must not contain username" }
        require(password == confirmPassword) { "Password must be equal to confirmPassword" }
    }

    fun toEntity(encoder: PasswordEncoder): User {
        return User(
            username = username,
            password = encoder.encode(password)
        )
    }
}