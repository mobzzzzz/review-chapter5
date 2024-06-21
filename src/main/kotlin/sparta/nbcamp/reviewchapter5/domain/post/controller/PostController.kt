package sparta.nbcamp.reviewchapter5.domain.post.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import sparta.nbcamp.reviewchapter5.domain.post.service.PostService

@RestController
@RequestMapping("/posts")
class PostController(
    private val postService: PostService
)