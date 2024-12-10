package com.project.gmail_search.controller;

import com.project.gmail_search.service.EmailService;
import com.google.api.services.gmail.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.security.GeneralSecurityException;
import org.json.JSONArray;
import java.util.Map;


@RestController
@RequestMapping("/gmail/")
public class EmailController{

	@Autowired
	private EmailService emailService;

	@GetMapping("search")
	public ResponseEntity<Map<String, Object>> searchEmails(@RequestParam String query) throws IOException, GeneralSecurityException {
		System.out.println(query);
		Map<String, Object> resultMap = emailService.searchEmails(query);
		return ResponseEntity.ok(resultMap);
	}

}
