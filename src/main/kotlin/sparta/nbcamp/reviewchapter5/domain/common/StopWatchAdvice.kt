package sparta.nbcamp.reviewchapter5.domain.common

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class StopWatchAdvice {
    companion object {
        private val logger = LoggerFactory.getLogger("Execution Time Loger")
    }

    @Around("@annotation(sparta.nbcamp.reviewchapter5.domain.common.StopWatch)")
    fun run(joinPoint: ProceedingJoinPoint): Any? {
        val start = System.currentTimeMillis()
        val proceed = joinPoint.proceed()
        val end = System.currentTimeMillis()

        val name = joinPoint.signature.name

        logger.info("$name Execution time: ${end - start}ms")

        return proceed
    }
}