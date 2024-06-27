package sparta.nbcamp.reviewchapter5.domain.post.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import sparta.nbcamp.reviewchapter5.domain.post.model.Post
import sparta.nbcamp.reviewchapter5.domain.post.model.tag.PostTag
import sparta.nbcamp.reviewchapter5.domain.post.type.PostSearchType

interface CustomPostRepository {
    fun findByPageableWithUser(pageable: Pageable): Page<Pair<Post, List<PostTag>>>
    fun searchByKeyword(
        searchType: PostSearchType,
        keyword: String,
        pageable: Pageable
    ): Page<Pair<Post, List<PostTag>>>

    fun filterPostList(searchCondition: MutableMap<String, String>, pageable: Pageable): Page<Pair<Post, List<PostTag>>>
}