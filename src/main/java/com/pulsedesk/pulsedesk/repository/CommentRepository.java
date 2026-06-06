package com.pulsedesk.pulsedesk.repository;

import com.pulsedesk.pulsedesk.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long>{

}