package elegant.children.catchculture.service.review;

import elegant.children.catchculture.common.exception.CustomException;
import elegant.children.catchculture.common.exception.ErrorCode;
import elegant.children.catchculture.entity.pointhistory.PointChange;
import elegant.children.catchculture.entity.review.Review;
import elegant.children.catchculture.entity.user.User;
import elegant.children.catchculture.event.review.CreateReviewEvent;
import elegant.children.catchculture.repository.review.ReviewRepository;
import elegant.children.catchculture.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewTransactionService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void updateReviewDescription(final int reviewId, final String description) {
        reviewRepository.findById(reviewId)
                .ifPresentOrElse(review -> {
                    reviewRepository.updateReviewDescription(reviewId, description);
                }, () -> {
                    throw new CustomException(ErrorCode.INVALID_REVIEW_ID);
                });
    }

    @Transactional
    public void deleteReview(final int reviewId) {
        reviewRepository.findById(reviewId)
                .ifPresentOrElse(review -> {
                    reviewRepository.deleteReviewById(reviewId);
                }, () -> {
                    throw new CustomException(ErrorCode.INVALID_REVIEW_ID);
                });
    }

    @Transactional
    public void createReview(final Review review, final User user) {

        reviewRepository.save(review);
        final PointChange pointChange = PointChange.REVIEW;
//        user.updatePoint(pointChange.getPoint());
        userRepository.updateUserPoint(user.getId(), pointChange.getPoint());
        applicationEventPublisher.publishEvent(new CreateReviewEvent(pointChange, user));

    }
}