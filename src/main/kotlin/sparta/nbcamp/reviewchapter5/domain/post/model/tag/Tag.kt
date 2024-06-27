package sparta.nbcamp.reviewchapter5.domain.post.model.tag

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "tag")
class Tag(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true)
    val name: String,

    @OneToMany(mappedBy = "tag", cascade = [CascadeType.ALL], orphanRemoval = true)
    var postTags: MutableSet<PostTag> = mutableSetOf()
)