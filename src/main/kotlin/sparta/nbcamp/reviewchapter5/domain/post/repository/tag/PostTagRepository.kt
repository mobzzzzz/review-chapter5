package sparta.nbcamp.reviewchapter5.domain.post.repository.tag

import org.springframework.data.jpa.repository.JpaRepository
import sparta.nbcamp.reviewchapter5.domain.post.model.tag.PostTag
import sparta.nbcamp.reviewchapter5.domain.post.model.tag.PostTagId

interface PostTagRepository : JpaRepository<PostTag, PostTagId>, PostTagRepositoryCustom
