# Spring 심화 복습 & 개선과제

6/19 ~ 6/28 핵심 개선과제 내용을 복습 과제와 어느정도 병행하며 작업했습니다.

특정 컨셉 없이 단순 게시글 형태로 핵심 도메인으로 Post, User가 구성되어 있고  
Post 도메인에 추가로 Category, Tag가 구성되어 있습니다.

> ## 목차
> - [6/21 QueryDSL을 통한 검색 처리](#621-querydsl을-통한-검색-처리)
> - [6/24 목록 조회 공통 - Pagination 적용](#624-목록-조회-공통---pagination-적용)
> - [6/25 다양한 필터를 동적 쿼리로 처리](#625-다양한-필터를-동적-쿼리로-처리)
> - [비연관 관계 Join 조회](#비연관-관계-join-조회)
> - [6/26 Controller 테스트 코드](#626-controller-테스트-코드)
> - [6/27 Service 테스트 코드](#627-service-테스트-코드)
> - [6/28 Repository 테스트 코드](#628-repository-테스트-코드)
> - [서비스 + DB 통합 테스트](#서비스--db-통합-테스트)
> - [개발환경](#개발환경)
> - [의존성](#의존성)

## 주요 개선과제 파일

### [QueryDSL을 통한 쿼리 최적화 - PostRepositoryImpl.kt](src/main/kotlin/sparta/nbcamp/reviewchapter5/domain/post/repository/PostRepositoryImpl.kt)

### [Post와 관련된 테스트 코드 목록 - test/.../post](src/test/kotlin/sparta/nbcamp/reviewchapter5/domain/post)

## 6/21 QueryDSL을 통한 검색 처리

Pagination + Fetch join을 처리할 때 성능 저하를 피하기 위해 2단계로 나누어 처리합니다.

```kotlin
override fun searchByKeyword(
    searchType: PostSearchType,
    keyword: String,
    pageable: Pageable
): Page<Post> {
    val whereClause = BooleanBuilder().and(
        when (searchType) {
            PostSearchType.TITLE_CONTENT -> post.title.contains(keyword).or(post.content.contains(keyword))
            PostSearchType.TITLE -> post.title.contains(keyword)
            PostSearchType.CONTENT -> post.content.contains(keyword)
            PostSearchType.NONE -> null
        }
    )

    val (paginatedPostIds, totalCount) = basePagingIds(pageable, whereClause)

    if (paginatedPostIds.isEmpty()) {
        return PageImpl(emptyList(), pageable, 0L)
    }

    val postList = queryFactory
        .select(post)
        .from(post)
        .join(post.user, user).fetchJoin()
        .join(post.category, category).fetchJoin()
        .where(post.id.`in`(paginatedPostIds))
        .fetch()

    return PageImpl(postList, pageable, totalCount)
}
```

## 6/24 목록 조회 공통 - Pagination 적용

목록 조회에 지속적으로 쓰이는 페이징 처리를 공통으로 묶었습니다.

```kotlin
private fun basePagingIds(
    pageable: Pageable,
    whereClause: BooleanBuilder? = null
): Pair<List<Long>, Long> {
    val result = queryFactory.select(post.id)
        .from(post)
        .offset(pageable.offset)
        .limit(pageable.pageSize.toLong())
        .where(whereClause)
        .fetch()

    if (result.isEmpty()) {
        return Pair(emptyList(), 0L)
    }

    val totalCount = queryFactory.select(post.count())
        .from(post)
        .where(whereClause)
        .fetchOne()
        ?: 0L

    return Pair(result, totalCount)
}
```

## 6/25 다양한 필터를 동적 쿼리로 처리

QueryDSL의 BooleanBuilder를 활용하여 동적 쿼리를 처리합니다.

- 제목 (포함)
- 태그 (포함)
- 카테고리 (정확히 일치)
- 게시글 상태 (정확히 일치)
- N일전 게시글

```kotlin
private fun filteredBooleanBuilder(searchCondition: Map<String, String>): BooleanBuilder {
    val builder = BooleanBuilder()

    searchCondition["title"]?.let { builder.and(titleLike(it)) }
    searchCondition["category"]?.let { builder.and(categoryEq(it)) }
    searchCondition["tag"]?.let { builder.and(tagLike(it)) }
    searchCondition["status"]?.let { builder.and(statusEq(it)) }
    searchCondition["daysAgo"]?.let { builder.and(withInDays(it)) }

    return builder
}

private fun titleLike(title: String): BooleanExpression {
    return post.title.contains(title)
}

private fun categoryEq(category: String): BooleanExpression {
    return post.category.name.eq(category)
}

private fun tagLike(tag: String): BooleanExpression {
    val subQuery = JPAExpressions.select(postTag.post.id)
        .from(postTag)
        .where(postTag.tag.name.contains(tag))

    return post.id.`in`(subQuery)
}

private fun statusEq(status: String): BooleanExpression? {
    return try {
        post.status.eq(PostStatus.valueOf(status))
    } catch (e: IllegalArgumentException) {
        null
    }
}

private fun withInDays(daysAgo: String): BooleanExpression {
    val daysAgoDate = LocalDateTime.now().minusDays(daysAgo.toLong())
    val startDate = daysAgoDate.let { LocalDateTime.of(it.year, it.month, it.dayOfMonth, 0, 0, 0) }
    val midnightDate = daysAgoDate.let { LocalDateTime.of(it.year, it.month, it.dayOfMonth, 23, 59, 59) }

    return post.createdAt.between(startDate, midnightDate)
}
```

## 비연관 관계 Join 조회

Post - PostTag - Tag의 관계에서 Post와 PostTag를 조인하고 PostTag와 Tag를 조인하여 조회합니다.
PostTag가 연관관계의 주인으로 단방향으로 설정되어 있습니다.
Tuple로 조회된 결과를 Pair로 묶어 반환합니다.

```kotlin
private fun joinedPostListWithTagByIds(paginatedPostIds: List<Long>): List<Pair<Post, List<PostTag>>> {
    return queryFactory
        .select(post, postTag)
        .from(post)
        .leftJoin(post.user, user).fetchJoin()
        .leftJoin(post.category, category).fetchJoin()
        .leftJoin(postTag).on(post.id.eq(postTag.post.id))
        .leftJoin(postTag.tag, tag).fetchJoin()
        .where(post.id.`in`(paginatedPostIds))
        .fetch()
        .groupBy { it.get(post) }
        .mapValues { it.value.map { tuple -> tuple.get(postTag)!! } }
        .map { Pair(it.key!!, it.value) }
}
```

## 6/26 Controller 테스트 코드

과제의 조건인 MockMVC를 사용해서 Controller 테스트 코드를 작성했습니다.

[PostControllerTest.kt](src/test/kotlin/sparta/nbcamp/reviewchapter5/domain/post/PostControllerTest.kt)

```kotlin
describe("GET /posts/{id}는") {
    context("존재하는 ID를 요청을 보낼 때") {
        it("200 status code를 응답해야한다.") {
            // given
            val postId = 1L
            val fixtureMonkey = FixtureMonkey.builder()
                .plugin(KotlinPlugin())
                .build()

            every { postService.getPost(postId) } returns fixtureMonkey.giveMeOne(PostResponse::class.java)

            // when
            val result = mockMvc.perform(
                get("/posts/$postId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
            ).andReturn()

            // then
            result.response.status shouldBe 200

            val response = result.response.contentAsString
            if (response.isNotEmpty()) {
                val postResponse = jacksonObjectMapper().readValue(response, PostResponse::class.java)
                postResponse.id shouldBe postId
            }
        }
    }
}
```

## 6/27 Service 테스트 코드

과제의 조건인 Mockito를 사용해서 Service 테스트 코드를 작성했습니다.

[PostServiceTest.kt](src/test/kotlin/sparta/nbcamp/reviewchapter5/domain/post/PostServiceTest.kt)

```kotlin
given("어떤 사용자가 게시글 작성을 요청하고") {
    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .build()

    val sampleUser = fixtureMonkey.giveMeBuilder(User::class.java).sample()
    val createPostRequest = fixtureMonkey.giveMeOne(CreatePostRequest::class.java)
    val principal = fixtureMonkey.giveMeBuilder(UserPrincipal::class.java)
        .set("id", sampleUser.id)
        .sample()
    val samplePostId = fixtureMonkey.giveMeOne(Long::class.java)

    every { userRepository.findByIdOrNull(principal.id) } returns sampleUser

    `when`("게시글을 작성에 성공하면") {
        every { postRepository.save(any()) } answers {
            firstArg<Post>().apply {
                id = samplePostId
                createdAt = LocalDateTime.of(2021, 1, 1, 0, 0)
                updatedAt = LocalDateTime.of(2021, 1, 1, 0, 0)
            }
        }

        val result = postService.createPost(createPostRequest, principal)

        then("요청에 대한 게시글이 생성된다") {
            result shouldNotBe null
            result.id shouldBe samplePostId
            result.title shouldBe createPostRequest.title
            result.content shouldBe createPostRequest.content
        }
    }
}
```

## 6/28 Repository 테스트 코드

과제의 조건인 @DataJpaTest를 사용해서 Repository 테스트 코드를 작성했습니다.

[PostRepositoryTest.kt](src/test/kotlin/sparta/nbcamp/reviewchapter5/domain/post/PostRepositoryTest.kt)

```kotlin
@Test
fun `N일전에 정확히 해당하는 게시글이 필터링되는지 확인`() {
    // GIVEN
    val randomDaysAgo = Random.nextLong(1, 10)
    val randomDateDaysAgo = LocalDateTime.now().minusDays(randomDaysAgo)
    val searchCondition = mutableMapOf(
        "daysAgo" to randomDaysAgo.toString()
    )

    // WHEN
    val result = postRepository.filterPostList(searchCondition, Pageable.ofSize(10))

    // THEN
    result.totalElements shouldBe defaultPostList.count {
        it.createdAt.toLocalDate().year == randomDateDaysAgo.toLocalDate().year
            && it.createdAt.toLocalDate().month == randomDateDaysAgo.month
            && it.createdAt.toLocalDate().dayOfMonth == randomDateDaysAgo.dayOfMonth
    }
}
```

## 서비스 + DB 통합 테스트

Service와 Repository를 함께 테스트하는 통합 테스트 코드를 작성했습니다.

[PostServiceDBTest.kt](src/test/kotlin/sparta/nbcamp/reviewchapter5/domain/post/PostServiceDBTest.kt)

```kotlin
@Test
fun `정상적으로 getPostList()를 통한 조회가 되는지 확인`() {
    // given
    val pageable = PageRequest.of(0, 10)

    // when
    val result = postService.getPostList(pageable)

    // then
    result.size shouldBe 10
    result.isLast shouldBe true
    result.totalElements shouldBe 10
    result.content.forEachIndexed { index, postResponse ->
        postResponse.id shouldBe defaultPostList[index].id
        postResponse.title shouldBe defaultPostList[index].title
        postResponse.content shouldBe defaultPostList[index].content
        postResponse.status shouldBe defaultPostList[index].status.name
        postResponse.user.id shouldBe defaultUser.id
        postResponse.user.username shouldBe defaultUser.username
    }
}
```

## 개발환경

- `Spring Boot` 3.3.1
- `IntelliJ IDEA` 2024.1.4
- `Kotlin` 1.9.24, `JDK` 21
- `Build`: Gradle
- `DB`: H2 Database

## 의존성

- `Spring Boot`: 23.3.1
- `jjwt`: 0.12.5
- `QueryDSL`: 5.0.0
- `Kotest`: 5.8.1
- `Mockk`: 1.13.9
- `SpringMockk`: 4.0.2
- `Fixture Monkey`: 1.0.14
- `SpringDoc`: 2.5.0