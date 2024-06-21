package sparta.nbcamp.reviewchapter5.domain.user.service

import sparta.nbcamp.reviewchapter5.domain.user.dto.request.SignInRequest
import sparta.nbcamp.reviewchapter5.domain.user.dto.request.SignUpRequest
import sparta.nbcamp.reviewchapter5.domain.user.dto.response.ExistsUsernameResponse
import sparta.nbcamp.reviewchapter5.domain.user.dto.response.SignInResponse
import sparta.nbcamp.reviewchapter5.domain.user.dto.response.UserResponse

interface UserService {
    /**
     * 새로운 사용자를 등록합니다.
     *
     * @param request 사용자 세부 정보를 포함하는 등록 요청
     * @return 새로 생성된 사용자의 세부 정보를 포함하는 응답
     */
    fun signUp(request: SignUpRequest): UserResponse

    /**
     * 사용자가 로그인합니다.
     *
     * @param request 사용자 세부 정보를 포함하는 로그인 요청
     * @return 로그인한 사용자의 세부 정보를 포함하는 응답
     */
    fun signIn(request: SignInRequest): SignInResponse

    /**
     * 사용자 이름이 이미 존재하는지 확인합니다.
     *
     * @param username 확인할 사용자 이름
     * @return 사용자 이름이 존재하는지 여부를 나타내는 응답
     */
    fun existsUsername(username: String): ExistsUsernameResponse
}