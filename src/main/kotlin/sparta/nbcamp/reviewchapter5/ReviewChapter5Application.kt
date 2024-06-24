package sparta.nbcamp.reviewchapter5

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy

@EnableAspectJAutoProxy
@SpringBootApplication
class ReviewChapter5Application

fun main(args: Array<String>) {
    runApplication<ReviewChapter5Application>(*args)
}
