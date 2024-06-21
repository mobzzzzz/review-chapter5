package sparta.nbcamp.reviewchapter5.exception

data class ModelNotFoundException(
    val modelName: String,
    val id: Long
) : RuntimeException(
    "Model $modelName with given id $id not found"
)