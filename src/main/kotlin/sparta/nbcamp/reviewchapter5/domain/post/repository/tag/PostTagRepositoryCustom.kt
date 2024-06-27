package sparta.nbcamp.reviewchapter5.domain.post.repository.tag

import sparta.nbcamp.reviewchapter5.domain.post.model.tag.PostTag

interface PostTagRepositoryCustom {
    fun findTagByPostIdIn(postIds: List<Long>): List<PostTag>
}