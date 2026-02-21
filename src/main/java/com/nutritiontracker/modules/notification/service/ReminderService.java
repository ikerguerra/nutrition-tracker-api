package com.nutritiontracker.modules.notification.service;

import com.nutritiontracker.modules.dailylog.repository.DailyLogRepository;
import com.nutritiontracker.modules.notification.entity.PushSubscription;
import com.nutritiontracker.modules.notification.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {

    private final PushSubscriptionRepository subscriptionRepository;
    private final DailyLogRepository dailyLogRepository;
    private final NotificationService notificationService;

    // Run every day at 20:00 (8:00 PM) server time
    @Scheduled(cron = "0 0 20 * * *")
    public void sendDailyReminders() {
        log.info("Starting daily push notification reminders job");
        LocalDate today = LocalDate.now();
        List<PushSubscription> allSubscriptions = subscriptionRepository.findAll();

        int sentCount = 0;
        for (PushSubscription sub : allSubscriptions) {
            Long userId = sub.getUser().getId();

            // Check if user has a daily log for today
            boolean hasLogToday = dailyLogRepository.existsByUserIdAndDate(userId, today);

            if (!hasLogToday) {
                // Send reminder!
                String payload = "{\"title\": \"No olvides tus comidas\", \"body\": \"Aún no has registrado ninguna comida hoy. ¡Mantén tu racha!\"}";
                notificationService.sendNotification(sub, payload);
                sentCount++;
            }
        }

        log.info("Finished sending {} push notification reminders", sentCount);
    }
}
