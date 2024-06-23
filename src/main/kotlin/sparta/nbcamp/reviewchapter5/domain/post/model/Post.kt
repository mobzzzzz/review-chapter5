package sparta.nbcamp.reviewchapter5.domain.post.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import sparta.nbcamp.reviewchapter5.domain.common.BaseTimeEntity
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
) : BaseTimeEntity()