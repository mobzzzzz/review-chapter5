package sparta.nbcamp.reviewchapter5.exception

data class AccessDeniedException(
    private val text: String
) : RuntimeException(
    "Access Denied: $text"
)