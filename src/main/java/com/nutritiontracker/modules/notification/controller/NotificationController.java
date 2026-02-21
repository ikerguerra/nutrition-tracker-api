package com.nutritiontracker.modules.notification.controller;

import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.notification.dto.PushSubscriptionRequest;
import com.nutritiontracker.modules.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PushSubscriptionRequest request) {
        notificationService.subscribe(user, request);
        return ResponseEntity.ok().build();
    }

}
