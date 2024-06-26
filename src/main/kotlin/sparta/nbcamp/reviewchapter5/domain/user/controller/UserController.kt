package sparta.nbcamp.reviewchapter5.domain.user.controller

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import sparta.nbcamp.reviewchapter5.domain.user.dto.request.SignInRequest
import sparta.nbcamp.reviewchapter5.domain.user.dto.request.SignUpRequest
import sparta.nbcamp.reviewchapter5.domain.user.dto.response.ExistsUsernameResponse
import sparta.nbcamp.reviewchapter5.domain.user.dto.response.SignInResponse
import sparta.nbcamp.reviewchapter5.domain.user.dto.response.UserResponse
import sparta.nbcamp.reviewchapter5.domain.user.service.UserService

@RestController
@RequestMapping
class UserController(
    private val userService: UserService
) {
    @PostMapping("/sign-up")
    fun signup(@RequestBody request: SignUpRequest): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.signUp(request))
    }

    @PostMapping("/sign-in")
    fun signin(@RequestBody request: SignInRequest, response: HttpServletResponse): ResponseEntity<SignInResponse> {
        val signInResponse = userService.signIn(request)

        val cookie = ResponseCookie.from("refreshToken", signInResponse.refreshToken)
            .path("/")
            .httpOnly(true)
            .maxAge(7 * 24 * 60 * 60)
            .sameSite("None")
            .build()

        response.addHeader("Set-Cookie", cookie.toString())

        return ResponseEntity.ok(signInResponse.copy(refreshToken = "http-only"))
    }

    @GetMapping("/users/exists-username")
    fun existsUsername(@RequestParam username: String): ResponseEntity<ExistsUsernameResponse> {
        return ResponseEntity.ok(userService.existsUsername(username))
    }
}