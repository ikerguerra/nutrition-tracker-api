package com.nutritiontracker.modules.notification.service;

import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.notification.dto.PushSubscriptionRequest;
import com.nutritiontracker.modules.notification.entity.PushSubscription;
import com.nutritiontracker.modules.notification.repository.PushSubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Security;
import java.util.Optional;

@Service
@Slf4j
public class NotificationService {

    private final PushSubscriptionRepository repository;
    private final PushService pushService;

    public NotificationService(
            PushSubscriptionRepository repository,
            @Value("${app.vapid.public-key}") String publicKey,
            @Value("${app.vapid.private-key}") String privateKey,
            @Value("${app.vapid.subject}") String subject) {

        this.repository = repository;

        // Add BouncyCastle as an algorithm provider
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        this.pushService = new PushService();
        try {
            this.pushService.setPublicKey(publicKey);
            this.pushService.setPrivateKey(privateKey);
            this.pushService.setSubject(subject);
        } catch (Exception e) {
            log.error("Error configuring VAPID keys for Web Push", e);
        }
    }

    @Transactional
    public void subscribe(User user, PushSubscriptionRequest request) {
        Optional<PushSubscription> existing = repository.findByEndpoint(request.getEndpoint());
        if (existing.isPresent()) {
            PushSubscription sub = existing.get();
            if (!sub.getUser().getId().equals(user.getId())) {
                sub.setUser(user);
                repository.save(sub);
            }
            return;
        }

        PushSubscription subscription = PushSubscription.builder()
                .user(user)
                .endpoint(request.getEndpoint())
                .p256dh(request.getP256dh())
                .auth(request.getAuth())
                .build();

        repository.save(subscription);
        log.info("New push subscription saved for user {}", user.getId());
    }

    @Transactional
    public void unsubscribe(String endpoint) {
        repository.deleteByEndpoint(endpoint);
        log.info("Push subscription removed for endpoint {}", endpoint);
    }

    public void sendNotification(PushSubscription sub, String payload) {
        try {
            Subscription.Keys keys = new Subscription.Keys(sub.getP256dh(), sub.getAuth());
            Subscription subscription = new Subscription(sub.getEndpoint(), keys);

            Notification notification = new Notification(subscription, payload);
            pushService.send(notification);
            log.debug("Push notification sent to user {}", sub.getUser().getId());
        } catch (Exception e) {
            log.error("Failed to send push notification to user {}", sub.getUser().getId(), e);
            // If the endpoint is no longer valid (e.g. 410 Gone), we should remove it
            if (e.getMessage() != null && e.getMessage().contains("410")) {
                repository.delete(sub);
            }
        }
    }
}
