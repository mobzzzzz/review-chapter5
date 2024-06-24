package sparta.nbcamp.reviewchapter5.domain.post.model.tag

import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import sparta.nbcamp.reviewchapter5.domain.post.model.Post
import java.io.Serializable

@Entity
@IdClass(PostTagId::class)
@Table(name = "post_tag")
class PostTag(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    val post: Post,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    val tag: Tag
)

@Embeddable
class PostTagId(
    val post: Long = 0,
    val tag: Long = 0
) : Serializable