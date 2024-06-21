package sparta.nbcamp.reviewchapter5.domain.post.service

import org.springframework.stereotype.Service
import sparta.nbcamp.reviewchapter5.domain.post.repository.PostRepository

@Service
class PostServiceImpl(
    private val postRepository: PostRepository
) : PostService
