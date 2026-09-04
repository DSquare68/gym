package com.github.dsquare68.gym.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.dsquare68.gym.entity.TrainingRecord;

/**
 * Schemes and sessions. A Spring Data JPA repository, so saving a training saves
 * its whole graph - exercises and rounds - in one managed transaction via the
 * {@code cascade = ALL} mappings on {@link TrainingRecord}.
 *
 * <p>Nothing here touches Vaadin or {@code HubApi}: every method takes the owning
 * user id. That is the seam that keeps the external-API path honest - an Android
 * client posting a session goes through this interface, not a second write path.
 */
public interface TrainingRepository extends JpaRepository<TrainingRecord, Long> {

    /** This user's reusable schemes, newest first. */
    List<TrainingRecord> findByUserIdAndTemplateTrueOrderByIdDesc(UUID userId);

    /** This user's performed sessions, newest first. */
    List<TrainingRecord> findByUserIdAndTemplateFalseOrderByPerformedOnDescIdDesc(UUID userId);

    /** This user's performed sessions built from one scheme, newest first. */
    List<TrainingRecord> findByUserIdAndTemplateFalseAndScheme_IdOrderByPerformedOnDescIdDesc(
            UUID userId, Long schemeId);
    
}
