package com.moveo.flight.api.repository;

import com.moveo.flight.api.model.Notification;
import com.moveo.flight.api.model.User;
import com.moveo.flight.api.model.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for Notification entity
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Get all notifications for a specific user */
    List<Notification> findByUser(User user);

    /** Get all notifications by status (e.g. PENDING to send later) */
    List<Notification> findByStatus(NotificationStatus status);

    /* Fetch pending notifications with user to avoid LazyInitialization */
    @Query("SELECT n FROM Notification n JOIN FETCH n.user WHERE n.status = :status")
    List<Notification> findByStatusWithUser(@Param("status") NotificationStatus status);

}

