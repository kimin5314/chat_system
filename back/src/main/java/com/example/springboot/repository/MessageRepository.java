package com.example.springboot.repository;

import com.example.springboot.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // Get conversation between two users
    @Query("SELECT m FROM Message m WHERE " +
           "(m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
           "(m.senderId = :userId2 AND m.receiverId = :userId1) " +
           "ORDER BY m.createdAt ASC")
    List<Message> findConversation(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);
    
    // Get all conversations for a user with last message
    @Query("SELECT m FROM Message m WHERE m.id IN (" +
           "SELECT MAX(m2.id) FROM Message m2 WHERE " +
           "m2.senderId = :userId OR m2.receiverId = :userId " +
           "GROUP BY CASE WHEN m2.senderId = :userId THEN m2.receiverId ELSE m2.senderId END" +
           ") ORDER BY m.createdAt DESC")
    List<Message> findLastMessagesForUser(@Param("userId") Integer userId);
    
    // Count unread messages for a user from a specific sender
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiverId = :receiverId AND m.senderId = :senderId AND m.isRead = false")
    Long countUnreadMessages(@Param("receiverId") Integer receiverId, @Param("senderId") Integer senderId);
    
    // Mark messages as read
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.receiverId = :receiverId AND m.senderId = :senderId AND m.isRead = false")
    void markMessagesAsRead(@Param("receiverId") Integer receiverId, @Param("senderId") Integer senderId);
    
    // Count messages by user and date range
    @Query("SELECT COUNT(m) FROM Message m WHERE " +
           "(m.senderId = :userId OR m.receiverId = :userId) AND " +
           "m.createdAt >= :startDate AND m.createdAt <= :endDate")
    Long countMessagesByUserAndDateRange(@Param("userId") Integer userId, 
                                       @Param("startDate") java.time.LocalDateTime startDate, 
                                       @Param("endDate") java.time.LocalDateTime endDate);
}
