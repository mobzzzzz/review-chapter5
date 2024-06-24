package sparta.nbcamp.reviewchapter5.domain.post.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import sparta.nbcamp.reviewchapter5.domain.post.model.Post

interface CustomPostRepository {
    fun findByPageableWithUser(pageable: Pageable): Page<Post>
    fun searchByKeyword(searchType: String, keyword: String, pageable: Pageable): Page<Post>
    fun filterPostList(searchCondition: MutableMap<String, String>, pageable: Pageable): Page<Post>
}