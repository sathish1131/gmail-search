package com.project.gmail_search.controller;

import com.project.gmail_search.service.EmailService;
import com.google.api.services.gmail.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/gmail/")
public class EmailController{

	@Autowired
	private EmailService emailService;

	@GetMapping("search")
	public List<Message> searchEmails(@RequestParam String alias) throws IOException, GeneralSecurityException {
		return emailService.searchEmails(alias);
	}

}
