package com.pulsedesk.pulsedesk.service;

import com.pulsedesk.pulsedesk.model.Comment;
import com.pulsedesk.pulsedesk.model.Ticket;
import com.pulsedesk.pulsedesk.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketService ticketService;

    @Value("${huggingface.api.key}")
    private String apiKey;

    public CommentService(CommentRepository commentRepository, TicketService ticketService) {
        this.commentRepository = commentRepository;
        this.ticketService = ticketService;
    }

    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    public Comment submitComment(String text) throws Exception {
        Comment comment = new Comment();
        comment.setText(text);
        comment.setCreatedAt(LocalDateTime.now());
        //Saving the comment, in case the AI analysis call fails
        Comment savedComment = commentRepository.save(comment);
        //AI analysis is wrapped so a failed API call doesn't prevent the comment from saving
        try {
            analyzeAndCreateTicket(savedComment);
        } catch (Exception e) {
            System.out.println("AI analysis failed: " + e.getMessage());
        }
        return savedComment;
    }

    private void analyzeAndCreateTicket(Comment comment) throws Exception {
        String prompt = "You are a support triage assistant for a platform called PulseDesk. "
                + "Analyze the following user comment and decide if it should become a support ticket. "
                + "A comment should become a ticket if it describes a problem, issue, bug, billing concern, or feature request. "
                + "If it should become a ticket respond with ONLY a JSON object in this exact format: "
                + "{\"isTicket\": true, \"title\": \"short title\", \"category\": \"bug or feature or billing or account or other\", "
                + "\"priority\": \"low or medium or high\", \"summary\": \"brief summary\"} "
                + "If it should NOT become a ticket respond with ONLY: {\"isTicket\": false} "
                + "Comment: " + comment.getText();

        String requestBody = "{\"model\": \"meta-llama/Llama-3.1-8B-Instruct:nscale\", "
                + "\"messages\": [{\"role\": \"user\", \"content\": \"" + prompt.replace("\"", "\\\"") + "\"}]}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://router.huggingface.co/v1/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();

        //Logging the AI response for clearer inner workings and debugging purposes, SystemOutPrint instead of SLF4J logger for simplicity
        // (it's kept here on purpose)
        System.out.println("AI Response: " + responseBody);

        int contentStart = responseBody.indexOf("\"content\":\"");
        if (contentStart == -1) return;

        contentStart += "\"content\":\"".length();
        int contentEnd = responseBody.indexOf("\",\"role\"", contentStart);
        if (contentEnd == -1) return;

        String rawContent = responseBody.substring(contentStart, contentEnd);
        //Hugging Face returns escaped quates, so we unescape them
        String json = rawContent.replace("\\\"", "\"").replace("\\n", "").trim();

        int jsonStart = json.indexOf("{");
        int jsonEnd = json.lastIndexOf("}");
        if (jsonStart == -1 || jsonEnd == -1) return;

        json = json.substring(jsonStart, jsonEnd + 1);

        //checking both formats in case of AI adding a space after colons in JSON format
        if (json.contains("\"isTicket\": true") || json.contains("\"isTicket\":true")) {
            Ticket ticket = new Ticket();
            ticket.setCommentId(comment.getId());
            ticket.setTitle(extractField(json, "title"));
            ticket.setCategory(extractField(json, "category"));
            ticket.setPriority(extractField(json, "priority"));
            ticket.setSummary(extractField(json, "summary"));
            ticketService.save(ticket);
        }
    }

    private String extractField(String json, String field) {
        String search = "\"" + field + "\": \"";
        int start = json.indexOf(search);
        if (start == -1) {
            search = "\"" + field + "\":\"";
            start = json.indexOf(search);
        }
        if (start == -1) return "unknown";
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return "unknown";
        return json.substring(start, end);
    }
}


