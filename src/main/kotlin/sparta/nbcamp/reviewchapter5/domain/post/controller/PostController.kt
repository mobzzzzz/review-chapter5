package sparta.nbcamp.reviewchapter5.domain.post.controller

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import sparta.nbcamp.reviewchapter5.domain.post.dto.request.CreatePostRequest
import sparta.nbcamp.reviewchapter5.domain.post.dto.response.PostResponse
import sparta.nbcamp.reviewchapter5.domain.post.service.PostService
import sparta.nbcamp.reviewchapter5.infra.security.UserPrincipal

@RestController
@RequestMapping("/posts")
class PostController(
    private val postService: PostService
) {
    @GetMapping
    fun getPostList(
        @PageableDefault(
            page = 1,
            size = 20,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable
    ): ResponseEntity<Page<PostResponse>> {
        return ResponseEntity.ok(postService.getPostList(pageable))
    }

    @GetMapping("/{postId}")
    fun getPost(@PathVariable postId: Long): ResponseEntity<PostResponse> {
        return ResponseEntity.ok(postService.getPost(postId))
    }

    @GetMapping("/search")
    fun searchPost(
        @RequestParam searchType: String,
        @RequestParam keyword: String,
        @PageableDefault(
            page = 1,
            size = 20,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable
    ): ResponseEntity<Page<PostResponse>> {
        return when (searchType) {
            "title_content", "title", "content" -> ResponseEntity.ok(
                postService.searchPostList(
                    searchType,
                    keyword,
                    pageable
                )
            )

            else -> ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/filter")
    fun filterPosts(
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) tag: String?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) daysAgo: Long?,
        @PageableDefault(
            page = 1,
            size = 20,
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        ) pageable: Pageable
    ): ResponseEntity<Page<PostResponse>> {
        val searchCondition = mutableMapOf<String, String>()
        title?.let { searchCondition["title"] = it }
        tag?.let { searchCondition["tag"] = it }
        category?.let { searchCondition["category"] = it }
        status?.let { searchCondition["status"] = it }
        daysAgo?.let { searchCondition["daysAgo"] = it.toString() }

        return ResponseEntity.ok(
            postService.filterPosts(
                searchCondition = searchCondition,
                pageable = pageable
            )
        )
    }

    @PostMapping
    fun createPost(
        @RequestBody request: CreatePostRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<PostResponse> {
        return ResponseEntity.ok(postService.createPost(request, principal))
    }
}