
package com.genai.email.controller;

import com.genai.email.model.EmailResponseDTO;
import com.genai.email.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/process")
    public ResponseEntity<EmailResponseDTO> processEmail(@RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(emailService.processFile(file));
    }
}
