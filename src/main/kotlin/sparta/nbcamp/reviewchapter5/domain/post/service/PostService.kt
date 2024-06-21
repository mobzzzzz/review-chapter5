package sparta.nbcamp.reviewchapter5.domain.post.service

import org.springframework.stereotype.Service
import sparta.nbcamp.reviewchapter5.domain.post.repository.PostRepository

@Service
class PostService(
    private val postRepository: PostRepository
)
