package com.library.service;

import com.library.dao.NotificationDAO;
import com.library.dao.impl.NotificationDAOImpl;
import com.library.model.Notification;
import com.library.model.NotificationPreference;
import com.library.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationDAO notificationDAO;

    public NotificationService() {
        this.notificationDAO = new NotificationDAOImpl();
    }

    public NotificationService(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    public List<Notification> getMemberNotifications(Long memberId, int limit) {
        return notificationDAO.findByMemberId(memberId, limit);
    }

    public int getUnreadCount(Long memberId) {
        return notificationDAO.countUnreadByMemberId(memberId);
    }

    public void markAsRead(Long notificationId, Long memberId) {
        notificationDAO.markAsRead(notificationId, memberId);
    }

    public void markAllAsRead(Long memberId) {
        notificationDAO.markAllAsRead(memberId);
    }

    public void sendNotification(Long memberId, String title, String message, NotificationType type) {
        NotificationPreference pref = notificationDAO.findPreferencesByMemberId(memberId);
        if (pref != null) {
            if (NotificationType.DUE_SOON.equals(type) && !pref.isDueDateAlert()) return;
            if (NotificationType.OVERDUE.equals(type) && !pref.isOverdueAlert()) return;
            if (NotificationType.NEW_ARRIVAL.equals(type) && !pref.isNewArrivalAlert()) return;
            if (NotificationType.RESERVATION_READY.equals(type) && !pref.isReservationAlert()) return;
        }

        notificationDAO.create(new Notification(memberId, title, message, type));
        logger.debug("Notification sent to member {}: {}", memberId, title);
    }

    public NotificationPreference getPreferences(Long memberId) {
        return notificationDAO.findPreferencesByMemberId(memberId);
    }

    public void updatePreferences(NotificationPreference pref) {
        notificationDAO.saveOrUpdatePreferences(pref);
    }
}
