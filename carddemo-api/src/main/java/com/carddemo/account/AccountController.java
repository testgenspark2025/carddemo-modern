package com.carddemo.account;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountViewService service;

    public AccountController(AccountViewService service) {
        this.service = service;
    }

    @GetMapping("/{acctId}")
    public AccountViewResponse get(@PathVariable long acctId) {
        if (acctId <= 0) {
            // Mirrors COACTVWC paragraph 2210-EDIT-ACCOUNT.
            throw new IllegalArgumentException(
                    "Account number must be a non zero 11 digit number");
        }
        return service.view(acctId);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(AccountNotFoundException e) {
        return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadInput(IllegalArgumentException e) {
        return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
    }
}
