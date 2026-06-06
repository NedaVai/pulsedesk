package com.pulsedesk.pulsedesk.controller;

import com.pulsedesk.pulsedesk.model.Comment;
import com.pulsedesk.pulsedesk.service.CommentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<Comment> getAllComments() {
        return commentService.getAllComments();
    }

    @PostMapping
    public Comment submitComment(@RequestBody CommentRequest request) throws Exception {
        return commentService.submitComment(request.getText());
    }

    public static class CommentRequest {
        private String text;
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}
