package com.example.springboot.repository;

import com.example.springboot.entity.Contacts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContactsRepository extends JpaRepository<Contacts, Integer> {

    // 根据用户 ID 和联系人 ID 查找
    Optional<Contacts> findByUserIdAndContactId(Integer userId, Integer contactId);

    // 查找请求发给某用户的 pending/accepted 状态
    List<Contacts> findByContactIdAndStatus(Integer contactId, String status);

    // 某用户发出的请求状态
    List<Contacts> findByUserIdAndStatus(Integer userId, String status);

    @Modifying
    @Transactional
    @Query("UPDATE Contacts c SET c.status = 'accepted' WHERE c.userId = :fromUserId AND c.contactId = :toUserId AND c.status = 'pending'")
    int acceptRequest(@Param("fromUserId") Integer fromUserId, @Param("toUserId") Integer toUserId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Contacts c WHERE c.userId = :fromUserId AND c.contactId = :toUserId AND c.status = 'pending'")
    int rejectRequest(@Param("fromUserId") Integer fromUserId, @Param("toUserId") Integer toUserId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Contacts c WHERE (c.userId = :userId AND c.contactId = :friendId AND c.status = 'accepted') OR (c.userId = :friendId AND c.contactId = :userId AND c.status = 'accepted')")
    int deleteFriend(@Param("userId") Integer userId, @Param("friendId") Integer friendId);



    // 用于检查是否存在这两个用户之间的记录（双向任一方向）
    @Query("SELECT c FROM Contacts c WHERE (c.userId = :userId AND c.contactId = :contactId) OR (c.userId = :contactId AND c.contactId = :userId)")
    Contacts findByUsers(@Param("userId") Integer userId, @Param("contactId") Integer contactId);

    // 找出某用户收到的好友请求（状态为 pending）
    @Query("SELECT c FROM Contacts c WHERE c.contactId = :userId AND c.status = 'pending'")
    List<Contacts> findPendingRequestsByUserId(@Param("userId") Integer userId);

    // 找出某用户已通过的好友（即 userId 为当前用户，状态为 accepted）
    @Query("SELECT c FROM Contacts c WHERE c.userId = :userId AND c.status = 'accepted'")
    List<Contacts> findAcceptedContactsByUserId(@Param("userId") Integer userId);
}
