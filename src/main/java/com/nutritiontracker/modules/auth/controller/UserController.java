package com.nutritiontracker.modules.auth.controller;

import com.nutritiontracker.modules.auth.dto.DeleteAccountRequest;
import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody DeleteAccountRequest request) {

        userService.deleteAccount(user.getId(), request.getPassword());
        return ResponseEntity.noContent().build();
    }
}
