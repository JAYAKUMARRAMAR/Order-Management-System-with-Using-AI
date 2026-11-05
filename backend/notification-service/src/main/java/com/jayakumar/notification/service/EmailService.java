package com.jayakumar.notification.service;

import com.jayakumar.notification.entity.Notification;

public interface EmailService {

    void sendOrderNotification(Notification notification);
}
