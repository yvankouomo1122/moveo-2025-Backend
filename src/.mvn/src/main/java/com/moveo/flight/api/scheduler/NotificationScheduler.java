package com.moveo.flight.api.scheduler;

import com.moveo.flight.api.model.Notification;
import com.moveo.flight.api.model.NotificationStatus;
import com.moveo.flight.api.model.NotificationType;
import com.moveo.flight.api.repository.NotificationRepository;
import com.moveo.flight.api.service.EmailService;
import com.moveo.flight.api.service.SmsService;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
public class NotificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService SmsService;

    public NotificationScheduler(NotificationRepository notificationRepository, EmailService emailService, SmsService SmsService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
        this.SmsService = SmsService;
    }

    /**
     * Runs every 1 minute to send pending EMAIL notifications
     */
    @Scheduled(fixedRate = 60000) // every 60 seconds
    public void sendPendingNotifications() {
        List<Notification> pendingNotifications =
                notificationRepository.findByStatusWithUser(NotificationStatus.PENDING);

        for (Notification notif : pendingNotifications) {
            try {
                if (notif.getType() == NotificationType.EMAIL) {
                        emailService.sendEmail(
                            notif.getUser().getEmail(),
                            "Moveo Flight Notification",
                            notif.getMessage()
                    );
                }
                else{
                    SmsService.sendSms(
                                notif.getUser().getPhoneNumber(),
                                notif.getMessage()
                    );
                }
                notif.setStatus(NotificationStatus.SENT);
                notificationRepository.save(notif); // Save the notification as SENT after sending the email
            } catch (MessagingException e) {
                logger.error("Failed to send email to {}: {}", notif.getUser().getEmail(), e.getMessage());
                notif.setStatus(NotificationStatus.FAILED);
                notificationRepository.save(notif);
            }
        }
    }
}
