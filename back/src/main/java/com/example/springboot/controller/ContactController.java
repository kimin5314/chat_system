package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.FriendRequestDto;
import com.example.springboot.entity.User;
import com.example.springboot.service.ContactService;
import com.example.springboot.service.UserService;
import com.example.springboot.utils.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contacts")
public class ContactController {

    @Resource
    private ContactService contactService;
    @Resource
    private UserService userService;

    /**
     * 发送好友请求
     * @param tokenHeader
     * @param request
     * @return
     */
    @PostMapping("/request")
    public Result sendFriendRequest(@RequestHeader("Authorization") String tokenHeader,
                                    @RequestBody Map<String, Object> request) {
        Integer toUserId = (Integer) request.get("userId");
        String token = tokenHeader.replace("Bearer ","");
        String fromUsername = JwtUtil.getUsernameFromToken(token);
        User fromUser = userService.getUserInformation(fromUsername);
        Integer fromUserId = fromUser.getId();
        try {
            contactService.sendFriendRequest(fromUserId, toUserId);
            return Result.success();
        } catch (RuntimeException e) {
            // 根据不同的错误消息返回不同的提示
            String errorMessage = e.getMessage();
            if ("不能添加自己为好友".equals(errorMessage)) {
                return Result.error("不能添加自己为好友");
            } else if ("该用户已是你的好友".equals(errorMessage)) {
                return Result.error("该用户已是你的好友");
            } else if ("已发送请求，等待对方处理".equals(errorMessage)) {
                return Result.error("已发送请求，等待对方处理");
            } else {
                return Result.error("发送好友请求失败");
            }
        }
    }

    /**
     * 查询好友请求
     */

    @GetMapping("/requests")
    public Result getPendingRequests(@RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "");
        String username = JwtUtil.getUsernameFromToken(token);
        User user = userService.getUserInformation(username);
        System.out.println(user.getId());
        List<FriendRequestDto> requests = contactService.getPendingRequests(user.getId());
        return Result.success(requests);
    }

    /**
     * 同意好友请求
     */
    @PostMapping("/requests/accept")
    public Result acceptRequest(@RequestHeader("Authorization") String tokenHeader, @RequestBody Map<String, Object> body) {
        String token = tokenHeader.replace("Bearer ", "");
        String username = JwtUtil.getUsernameFromToken(token);
        User user = userService.getUserInformation(username);
        Integer fromUserId = (Integer) body.get("fromUserId");
        contactService.acceptFriendRequest(fromUserId, user.getId());
        return Result.success();
    }

    /**拒绝好友请求
     *
     */
    @PostMapping("/requests/reject")
    public Result rejectRequest(@RequestHeader("Authorization") String tokenHeader,
                                @RequestBody Map<String, Object> body) {
        String token = tokenHeader.replace("Bearer ", "");
        String username = JwtUtil.getUsernameFromToken(token);
        User user = userService.getUserInformation(username);
        Integer fromUserId = (Integer) body.get("fromUserId");

        try {
            contactService.rejectFriendRequest(fromUserId, user.getId());
            return Result.success("好友请求已拒绝");
        } catch (RuntimeException e) {
            return Result.error();
        }
    }

    /**
     * 获取好友列表
     */
    @GetMapping("/list")
    public Result getFriendList(@RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "");
        String username = JwtUtil.getUsernameFromToken(token);
        User user = userService.getUserInformation(username);
        List<Map<String, Object>> friends = contactService.getFriendList(user.getId());
        return Result.success(friends);
    }

    /**
     * 删除好友
     */
    @DeleteMapping("/{friendId}")
    public Result deleteFriend(@RequestHeader("Authorization") String tokenHeader,
                             @PathVariable Integer friendId) {
        String token = tokenHeader.replace("Bearer ", "");
        String username = JwtUtil.getUsernameFromToken(token);
        User user = userService.getUserInformation(username);

        try {
            contactService.deleteFriend(user.getId(), friendId);
            return Result.success("删除好友成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

}
