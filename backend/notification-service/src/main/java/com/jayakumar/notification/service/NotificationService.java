package com.jayakumar.notification.service;

import com.jayakumar.notification.dto.CreateNotificationRequest;
import com.jayakumar.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(CreateNotificationRequest request);

    NotificationResponse getNotificationById(String notificationId);

    List<NotificationResponse> getNotificationsByOrderId(Long orderId);
}
