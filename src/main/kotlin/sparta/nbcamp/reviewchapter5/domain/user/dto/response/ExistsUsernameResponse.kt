package sparta.nbcamp.reviewchapter5.domain.user.dto.response

data class ExistsUsernameResponse(
    val exists: Boolean,
    val message: String
) {
    companion object {
        fun of(exists: Boolean): ExistsUsernameResponse {
            val message = if (exists) "중복된 아이디입니다." else "사용 가능한 아이디입니다."

            return ExistsUsernameResponse(exists, message)
        }
    }
}