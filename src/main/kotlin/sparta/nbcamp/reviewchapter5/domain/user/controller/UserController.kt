package sparta.nbcamp.reviewchapter5.domain.user.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import sparta.nbcamp.reviewchapter5.domain.user.service.UserService

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
)