package com.assignmente.researchtool.controller;

import com.assignmente.researchtool.service.AIService;
import com.assignmente.researchtool.util.PDFUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class ResearchController {

    @Autowired
    private AIService aiService;

    @PostMapping("/extract")
    public ResponseEntity<ByteArrayResource> extract(@RequestParam("file") MultipartFile file) throws Exception {

        // 1. extract text from pdf
        String text = PDFUtil.extractText(file);

        // 2. send to AI
        String csvData = aiService.extractFinancialData(text);

        // 3. convert to downloadable file
        byte[] data = csvData.getBytes();
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=financial.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(data.length)
                .body(resource);
    }

}
