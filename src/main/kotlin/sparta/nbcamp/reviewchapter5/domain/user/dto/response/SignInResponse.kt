package sparta.nbcamp.reviewchapter5.domain.user.dto.response

import sparta.nbcamp.reviewchapter5.domain.user.model.User
import sparta.nbcamp.reviewchapter5.infra.jwt.JwtPlugin

data class SignInResponse(
    val accessToken: String,
    val refreshToken: String
) {
    companion object {
        fun from(jwtPlugin: JwtPlugin, user: User): SignInResponse {
            val accessToken = jwtPlugin.generateAccessToken(user.id.toString())
            val refreshToken = jwtPlugin.generateRefreshToken(user.id.toString())

            return SignInResponse(accessToken, refreshToken)
        }
    }
}
