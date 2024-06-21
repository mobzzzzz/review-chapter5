package sparta.nbcamp.reviewchapter5.domain.user.dto.response

import sparta.nbcamp.reviewchapter5.domain.user.model.User

data class UserResponse(
    val id: Long,
    val username: String
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id!!,
                username = user.username
            )
        }
    }
}
