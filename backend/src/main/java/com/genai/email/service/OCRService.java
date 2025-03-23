
package com.genai.email.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OCRService {

    public boolean isImageOrScanned(MultipartFile file) {
        String name = file.getOriginalFilename();
        return name != null && (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"));
    }

    public String extractFromImage(MultipartFile file) {
        return "[Mock OCR Extracted Text]";
    }
}
