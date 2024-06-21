package sparta.nbcamp.reviewchapter5.domain.user.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import sparta.nbcamp.reviewchapter5.domain.user.dto.request.SignUpRequest
import sparta.nbcamp.reviewchapter5.domain.user.dto.response.ExistsUsernameResponse
import sparta.nbcamp.reviewchapter5.domain.user.dto.response.UserResponse
import sparta.nbcamp.reviewchapter5.domain.user.service.UserService

@RestController
@RequestMapping
class UserController(
    private val userService: UserService
) {
    @PostMapping("/signup")
    fun signup(@RequestBody request: SignUpRequest): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.signup(request))
    }

    @GetMapping("/users/exists-username")
    fun existsUsername(@RequestParam username: String): ResponseEntity<ExistsUsernameResponse> {
        return ResponseEntity.ok(userService.existsUsername(username))
    }
}