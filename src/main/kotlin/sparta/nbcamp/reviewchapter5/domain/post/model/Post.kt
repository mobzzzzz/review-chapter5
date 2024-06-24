package sparta.nbcamp.reviewchapter5.domain.post.model

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import sparta.nbcamp.reviewchapter5.domain.common.BaseTimeEntity
import sparta.nbcamp.reviewchapter5.domain.post.model.category.Category
import sparta.nbcamp.reviewchapter5.domain.post.model.tag.PostTag
import sparta.nbcamp.reviewchapter5.domain.post.model.tag.Tag
import sparta.nbcamp.reviewchapter5.domain.user.model.User

@Entity
@Table(name = "post")
class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val title: String,

    val content: String,

    @ManyToOne
    @JoinColumn(name = "user_id")
    var user: User,

    @Enumerated(EnumType.STRING)
    var status: PostStatus = PostStatus.RECOMMEND,

    @ManyToOne
    @JoinColumn(name = "category_id")
    var category: Category,

    @OneToMany(mappedBy = "post")
    var postTags: Set<PostTag> = hashSetOf()
) : BaseTimeEntity() {
    val tagList: List<Tag>
        get() = postTags.map { it.tag }
}