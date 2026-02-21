package com.nutritiontracker.modules.notification.repository;

import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.notification.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    List<PushSubscription> findByUser(User user);

    Optional<PushSubscription> findByEndpoint(String endpoint);

    void deleteByEndpoint(String endpoint);
}
