package com.nutritiontracker.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteAccountRequest {
    @NotBlank(message = "Confirmation is required to delete the account")
    private String password;
}
