package com.servicedesk.channel.repository;

import com.servicedesk.channel.entity.WhatsAppContact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WhatsAppContactRepository extends JpaRepository<WhatsAppContact, String> {

    Optional<WhatsAppContact> findByChannelIdAndWaId(String channelId, String waId);

    Optional<WhatsAppContact> findByChannelIdAndPhoneNumber(String channelId, String phoneNumber);

    List<WhatsAppContact> findByChannelId(String channelId);

    Page<WhatsAppContact> findByChannelIdOrderByLastMessageAtDesc(String channelId, Pageable pageable);

    Optional<WhatsAppContact> findByCustomerId(String customerId);

    List<WhatsAppContact> findByTicketId(String ticketId);

    @Query("SELECT c FROM WhatsAppContact c WHERE c.channelId = :channelId " +
            "AND c.isBlocked = false ORDER BY c.lastMessageAt DESC")
    Page<WhatsAppContact> findActiveContacts(
            @Param("channelId") String channelId,
            Pageable pageable);

    @Query("SELECT COUNT(c) FROM WhatsAppContact c WHERE c.channelId = :channelId")
    long countByChannelId(@Param("channelId") String channelId);
}
