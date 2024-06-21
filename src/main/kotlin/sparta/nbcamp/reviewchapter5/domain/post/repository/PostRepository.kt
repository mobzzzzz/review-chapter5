package sparta.nbcamp.reviewchapter5.domain.post.repository

import org.springframework.data.jpa.repository.JpaRepository
import sparta.nbcamp.reviewchapter5.domain.post.model.Post

interface PostRepository : JpaRepository<Post, Long>, CustomPostRepository