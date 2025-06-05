package com.example.springboot.service;

import com.example.springboot.dto.FriendRequestDto;
import com.example.springboot.entity.Contacts;
import com.example.springboot.entity.User;
import com.example.springboot.repository.ContactsRepository;
import com.example.springboot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContactService {
    @Autowired
    ContactsRepository contactsRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private UserService userService;

    public void sendFriendRequest(Integer fromUserId, Integer toUserId) {
        if (fromUserId.equals(toUserId)) {
            throw new RuntimeException("不能添加自己为好友");
        }
        System.out.println("fromUserId = " + fromUserId + ", toUserId = " + toUserId);
        Optional<Contacts> existingOpt = contactsRepository.findByUserIdAndContactId(fromUserId, toUserId);
        System.out.println("existing = " + existingOpt);

        if (existingOpt.isPresent()) {
            Contacts existing = existingOpt.get();
            String status = existing.getStatus();
            if ("accepted".equals(status)) {
                throw new RuntimeException("该用户已是你的好友");
            } else if ("pending".equals(status)) {
                throw new RuntimeException("已发送请求，等待对方处理");
            }
        }


        // 如何当前不是好友，则建立好友关系
        Contacts request = new Contacts();
        request.setUserId(fromUserId);
        request.setContactId(toUserId);
        request.setStatus("pending");
        request.setCreatedAt(LocalDateTime.now());

        contactsRepository.save(request);


    }
    @Transactional
    public List<FriendRequestDto> getPendingRequests(Integer userId) {
        List<Contacts> contacts = contactsRepository.findPendingRequestsByUserId(userId);
        return contacts.stream().map(contact -> {
            FriendRequestDto dto = new FriendRequestDto();
            dto.setId(contact.getId());
            dto.setStatus(contact.getStatus());
            dto.setUserId(contact.getUserId());
            dto.setContactId(contact.getContactId());
            dto.setCreatedAt(contact.getCreatedAt());

            // 获取发送者信息
            User sender = userService.getUserById(contact.getUserId());
            if (sender != null) {
                dto.setUsername(sender.getUsername());
                dto.setDisplayName(sender.getDisplayName());
                dto.setAvatarUrl(sender.getAvatarUrl());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    public void acceptFriendRequest(Integer fromUserId, Integer toUserId) {
        int updated = contactsRepository.acceptRequest(fromUserId, toUserId);
        if (updated == 0) {
            throw new RuntimeException("没有找到该好友请求或请求已处理");
        }

        // 双向好友关系：插入反向记录,互为对方
        Contacts reverse = new Contacts();
        reverse.setUserId(toUserId);
        reverse.setContactId(fromUserId);
        reverse.setStatus("accepted");
        reverse.setCreatedAt(LocalDateTime.now());
        contactsRepository.save(reverse);
    }

    public void rejectFriendRequest(Integer fromUserId, Integer toUserId) {
        int deleted = contactsRepository.rejectRequest(fromUserId, toUserId);
        if (deleted == 0) {
            throw new RuntimeException("没有找到该好友请求或请求已处理");
        }
    }

    /**
     * 获取用户的好友列表
     * @param userId 用户ID
     * @return 好友列表，包含好友的基本信息
     */
    @Transactional
    public List<Map<String, Object>> getFriendList(Integer userId) {
        // 获取所有已接受的好友关系
        List<Contacts> contacts = contactsRepository.findAcceptedContactsByUserId(userId);
        
        // 使用 Set 来存储已处理的好友ID，避免重复
        Set<Integer> processedFriendIds = new HashSet<>();
        
        // 转换为包含好友信息的列表
        return contacts.stream()
            .filter(contact -> {
                // 只处理未处理过的好友ID
                if (processedFriendIds.contains(contact.getContactId())) {
                    return false;
                }
                processedFriendIds.add(contact.getContactId());
                return true;
            })
            .map(contact -> {
                Map<String, Object> friendInfo = new HashMap<>();
                // 获取好友信息（contactId 是好友的ID）
                User friend = userService.getUserById(contact.getContactId());
                if (friend != null) {
                    friendInfo.put("id", friend.getId());
                    friendInfo.put("username", friend.getUsername());
                    friendInfo.put("displayName", friend.getDisplayName());
                    friendInfo.put("avatar", friend.getAvatarUrl());
                    friendInfo.put("online", false);  // 默认离线状态，后续可以通过WebSocket更新
                }
                return friendInfo;
            })
            .collect(Collectors.toList());
    }

    /**
     * 删除好友关系
     * @param userId 当前用户ID
     * @param friendId 要删除的好友ID
     */
    @Transactional
    public void deleteFriend(Integer userId, Integer friendId) {
        if (userId.equals(friendId)) {
            throw new RuntimeException("不能删除自己");
        }

        // 检查是否是好友关系
        Optional<Contacts> existing = contactsRepository.findByUserIdAndContactId(userId, friendId);
        if (existing.isEmpty() || !"accepted".equals(existing.get().getStatus())) {
            throw new RuntimeException("该用户不是你的好友");
        }

        // 删除双向好友关系
        int deleted = contactsRepository.deleteFriend(userId, friendId);
        if (deleted == 0) {
            throw new RuntimeException("删除好友失败");
        }
    }
}

