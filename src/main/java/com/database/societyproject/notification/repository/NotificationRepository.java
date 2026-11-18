package com.database.societyproject.notification.repository;

import com.database.societyproject.notification.entity.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);
    List<Notification> findBySocietyIdOrderByCreatedAtDesc(Long societyId);
}
