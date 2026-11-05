package com.jayakumar.notification.repository;

import com.jayakumar.notification.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    Optional<Notification> findByEventId(String eventId);

    List<Notification> findAllByOrderIdOrderByCreatedAtDesc(Long orderId);
}
