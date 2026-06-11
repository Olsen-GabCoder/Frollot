package com.frollot.service

import com.frollot.dto.CreateCommentRequest
import com.frollot.dto.CreatePostRequest
import com.frollot.model.*
import com.frollot.repository.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.*

@ExtendWith(MockKExtension::class)
class SocialServiceTest {

    // Repositories critiques - mocks stricts
    @MockK
    private lateinit var postRepository: PostRepository

    @MockK
    private lateinit var postLikeRepository: PostLikeRepository

    @MockK
    private lateinit var commentRepository: CommentRepository

    @MockK
    private lateinit var userRepository: UserRepository

    // Repositories secondaires - mocks relaxés
    @MockK(relaxed = true)
    private lateinit var postFavoriteRepository: PostFavoriteRepository

    @MockK(relaxed = true)
    private lateinit var postArchiveRepository: PostArchiveRepository

    @MockK(relaxed = true)
    private lateinit var postTagRepository: PostTagRepository

    @MockK(relaxed = true)
    private lateinit var salonRepository: SalonRepository

    @MockK(relaxed = true)
    private lateinit var postServiceRepository: PostServiceRepository

    @MockK(relaxed = true)
    private lateinit var salonServiceRepository: SalonServiceRepository

    @MockK(relaxed = true)
    private lateinit var hairHashtagRepository: HairHashtagRepository

    @MockK(relaxed = true)
    private lateinit var postHashtagRepository: PostHashtagRepository

    @MockK(relaxed = true)
    private lateinit var postMediaRepository: PostMediaRepository

    @MockK(relaxed = true)
    private lateinit var followRepository: FollowRepository

    @MockK(relaxed = true)
    private lateinit var postShareRepository: PostShareRepository

    @MockK(relaxed = true)
    private lateinit var postReactionRepository: PostReactionRepository

    @MockK(relaxed = true)
    private lateinit var portfolioRepository: PortfolioRepository

    @MockK(relaxed = true)
    private lateinit var portfolioPostRepository: PortfolioPostRepository

    @MockK(relaxed = true)
    private lateinit var salonHighlightedPostRepository: SalonHighlightedPostRepository

    @MockK(relaxed = true)
    private lateinit var salonStaffRepository: SalonStaffRepository

    @MockK(relaxed = true)
    private lateinit var badgeRepository: BadgeRepository

    @MockK(relaxed = true)
    private lateinit var userBadgeRepository: UserBadgeRepository

    @MockK(relaxed = true)
    private lateinit var collectionRepository: CollectionRepository

    @MockK(relaxed = true)
    private lateinit var collectionPostRepository: CollectionPostRepository

    private lateinit var socialService: SocialService

    private lateinit var author: User
    private lateinit var post: Post

    @BeforeEach
    fun setUp() {
        socialService = SocialService(
            postRepository,
            postLikeRepository,
            postFavoriteRepository,
            postArchiveRepository,
            commentRepository,
            userRepository,
            postTagRepository,
            salonRepository,
            postServiceRepository,
            salonServiceRepository,
            hairHashtagRepository,
            postHashtagRepository,
            postMediaRepository,
            followRepository,
            postShareRepository,
            postReactionRepository,
            portfolioRepository,
            portfolioPostRepository,
            salonHighlightedPostRepository,
            salonStaffRepository,
            badgeRepository,
            userBadgeRepository,
            collectionRepository,
            collectionPostRepository
        )

        author = User(
            id = "user-001",
            email = "author@test.com",
            passwordHash = "hash",
            userType = UserType.client,
            firstName = "John",
            lastName = "Doe",
            isActive = true
        )

        post = Post(
            id = "post-001",
            author = author,
            content = "Test post content",
            likesCount = 0
        )
    }

    @Test
    fun `createPost devrait créer un post valide`() {
        // Given
        val request = CreatePostRequest(
            authorId = "user-001",
            content = "New post content",
            imageUrl = null
        )

        every { userRepository.findById("user-001") } returns Optional.of(author)
        every { postRepository.save(any()) } returnsArgument 0
        every { postLikeRepository.existsByPostIdAndUserId(any(), any()) } returns false
        every { commentRepository.countByPostId(any()) } returns 0L

        // When
        val result = socialService.createPost(request)

        // Then
        assert(result.id != null)
        assert(result.content == "New post content")
        verify { postRepository.save(any()) }
    }

    @Test
    fun `toggleLike devrait ajouter un like si absent`() {
        // Given
        val liker = User(
            id = "user-002",
            email = "liker@test.com",
            passwordHash = "hash",
            userType = UserType.client
        )
        
        every { postRepository.findById("post-001") } returns Optional.of(post)
        every { userRepository.existsById("user-002") } returns true
        every { userRepository.findById("user-002") } returns Optional.of(liker)
        every { postLikeRepository.findByPostIdAndUserId("post-001", "user-002") } returns null
        every { postLikeRepository.save(any()) } returnsArgument 0
        every { postLikeRepository.countByPostId("post-001") } returns 1L
        every { commentRepository.countByPostId("post-001") } returns 0L
        every { postRepository.save(any()) } returnsArgument 0
        every { postLikeRepository.existsByPostIdAndUserId("post-001", "user-002") } returns true

        // When
        val result = socialService.toggleLike("post-001", "user-002")

        // Then
        assert(result.isLikedByCurrentUser == true)
        verify { postLikeRepository.save(any()) }
        verify { postRepository.save(any()) }
    }

    @Test
    fun `toggleLike devrait retirer un like si présent`() {
        // Given
        val liker = User(
            id = "user-002",
            email = "liker@test.com",
            passwordHash = "hash",
            userType = UserType.client
        )
        
        val like = PostLike(
            id = "like-001",
            post = post,
            user = liker
        )

        every { postRepository.findById("post-001") } returns Optional.of(post)
        every { userRepository.existsById("user-002") } returns true
        every { postLikeRepository.findByPostIdAndUserId("post-001", "user-002") } returns like
        every { postLikeRepository.delete(any()) } just Runs
        every { postLikeRepository.countByPostId("post-001") } returns 0L
        every { commentRepository.countByPostId("post-001") } returns 0L
        every { postRepository.save(any()) } returnsArgument 0
        every { postLikeRepository.existsByPostIdAndUserId("post-001", "user-002") } returns false

        // When
        val result = socialService.toggleLike("post-001", "user-002")

        // Then
        assert(result.isLikedByCurrentUser == false)
        verify(exactly = 1) { postLikeRepository.delete(like) }
        verify { postRepository.save(any()) }
    }

    @Test
    fun `createComment devrait créer un commentaire valide`() {
        // Given
        val request = CreateCommentRequest(
            postId = "post-001",
            authorId = "user-001",
            content = "Test comment"
        )

        every { postRepository.findById("post-001") } returns Optional.of(post)
        every { userRepository.findById("user-001") } returns Optional.of(author)
        every { commentRepository.save(any()) } returnsArgument 0

        // When
        val result = socialService.createComment(request)

        // Then
        assert(result.id != null)
        assert(result.content == "Test comment")
        verify { commentRepository.save(any()) }
    }

    @Test
    fun `deletePost devrait supprimer un post par son propriétaire`() {
        // Given
        every { postRepository.findById("post-001") } returns Optional.of(post)
        every { postLikeRepository.deleteByPostId("post-001") } just Runs
        every { commentRepository.deleteByPostId("post-001") } just Runs
        every { postRepository.delete(any()) } just Runs

        // When
        socialService.deletePost("post-001", "user-001")

        // Then
        verify { postRepository.delete(post) }
        verify { postLikeRepository.deleteByPostId("post-001") }
        verify { commentRepository.deleteByPostId("post-001") }
    }

    @Test
    fun `deletePost devrait rejeter la suppression par un non-propriétaire`() {
        // Given
        every { postRepository.findById("post-001") } returns Optional.of(post)

        // When / Then
        assertThrows<SocialService.UnauthorizedAccessException> {
            socialService.deletePost("post-001", "other-user-001")
        }
    }

    @Test
    fun `getFeed devrait retourner les posts avec pagination`() {
        // Given
        val pageable = PageRequest.of(0, 20)
        val postsPage = PageImpl(listOf(post), pageable, 1)

        // Mocks pour getFeed sans currentUserId (cas plus simple)
        // Note: La méthode utilise Pageable.unpaged() en interne, donc on mocke avec any()
        every { postRepository.findAllOrderByCreatedAtDesc(any()) } returns postsPage
        every { postLikeRepository.existsByPostIdAndUserId(any(), any()) } returns false
        every { commentRepository.countByPostId(any()) } returns 0L

        // When - appeler sans currentUserId pour simplifier le test
        val result = socialService.getFeed(pageable, null)

        // Then
        assert(result.content.size == 1)
        assert(result.content[0].id == "post-001")
    }
}
