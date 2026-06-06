package com.pulsedesk.pulsedesk.repository;

import com.pulsedesk.pulsedesk.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

}
