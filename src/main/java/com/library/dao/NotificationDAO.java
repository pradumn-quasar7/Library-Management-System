package com.library.dao;

import com.library.model.Notification;
import com.library.model.NotificationPreference;

import java.sql.Connection;
import java.util.List;

public interface NotificationDAO {
    Notification findById(Long id);
    List<Notification> findByMemberId(Long memberId, int limit);
    int countUnreadByMemberId(Long memberId);
    Long create(Connection conn, Notification notification);
    Long create(Notification notification);
    boolean markAsRead(Long id, Long memberId);
    boolean markAllAsRead(Long memberId);
    NotificationPreference findPreferencesByMemberId(Long memberId);
    boolean saveOrUpdatePreferences(NotificationPreference pref);
}
