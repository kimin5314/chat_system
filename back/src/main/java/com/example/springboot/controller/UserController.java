package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.config.FileProperties;
import com.example.springboot.dto.PasswordChangeDto;
import com.example.springboot.dto.UpdateInfoDto;
import com.example.springboot.entity.Tokens;
import com.example.springboot.entity.User;
import com.example.springboot.service.UserService;
import com.example.springboot.utils.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;
    
    @Autowired
    private FileProperties fileProperties;

    /**
     * 查询所有的User表里面的数据
     *
     * @return
     */
    @GetMapping("/selectAll")
    public Result selectAll() {
        List<User> list = userService.selectAll();
        return Result.success(list);
    }

    /**
     * 用户登录
     * @param user username password
     * @return token
     */
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        User loginUser = userService.login(user);
        String token = JwtUtil.createToken(loginUser.getUsername(), loginUser.getId());

        //构建token实体
        Tokens usertoken = new Tokens();
        usertoken.setUserId(loginUser.getId());
        usertoken.setToken(token);

        userService.insertToken(usertoken);

        // 返回 token
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);

        return Result.success(data);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        try {
            Boolean success = userService.register(user);
            if (success) {
                return Result.success();
            } else {
                return Result.error("400", "用户名已存在");
            }
        } catch (Exception e) {
            return Result.error("500", "注册失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码
     * @param tokenHeader
     * @param passwordChangeDto
     * @return
     */
    @PostMapping("/password")
    public Result changePassword(@RequestHeader("Authorization") String tokenHeader,
                                 @RequestBody PasswordChangeDto passwordChangeDto){
        String token = tokenHeader.replace("Bearer ","");
        String username = JwtUtil.getUsernameFromToken(token);
        passwordChangeDto.setUsername(username);
        boolean passwordFlag = userService.changPassword(passwordChangeDto);
        if(passwordFlag){
            return Result.success();
        } else {
            return Result.error();
        }
    }

    /**
     * 上传用户个人信息
     * @param tokenHeader
     * @return
     */
    @PostMapping("/profile")
    public Result userInformation(@RequestHeader("Authorization") String tokenHeader){
        String token = tokenHeader.replace("Bearer ","");
        String username = JwtUtil.getUsernameFromToken(token);
        User user = userService.getUserInformation(username);
        return Result.success(user);
    }

    /**
     * 修改用户信息
     */
    @PostMapping("/update")
    public Result updateInformation(@RequestHeader("Authorization") String tokenHeader,
                                   @RequestBody UpdateInfoDto updateInfoDto){
       String token = tokenHeader.replace("Bearer ","");
       String username = JwtUtil.getUsernameFromToken(token);
       updateInfoDto.setUsername(username);
       userService.updateInformation(updateInfoDto);
       return Result.success();
   }

    /**
     * 上传用户头像
     */
    @PostMapping("/upload-avatar")
    public Result uploadAvatar(@RequestHeader("Authorization") String tokenHeader,
                              @RequestParam("file") MultipartFile file) {
        try {
            String token = tokenHeader.replace("Bearer ", "");
            String username = JwtUtil.getUsernameFromToken(token);
            
            if (file.isEmpty()) {
                return Result.error("400", "请选择要上传的文件");
            }
            
            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("400", "只支持图片文件格式");
            }
            
            // 限制文件大小 (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return Result.error("400", "文件大小不能超过5MB");
            }
            
            // 创建上传目录
            String avatarDir = fileProperties.getAvatarDir();
            Path uploadPath = Paths.get(avatarDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = username + "_" + System.currentTimeMillis() + extension;
            
            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            // 构建直接访问URL (相对于Web根目录的路径)
            String avatarUrl = "/uploads/avatar/" + filename;
            
            // 更新用户头像URL
            UpdateInfoDto updateInfoDto = new UpdateInfoDto();
            updateInfoDto.setUsername(username);
            updateInfoDto.setAvatarUrl(avatarUrl);
            userService.updateInformation(updateInfoDto);
            
            Map<String, String> data = new HashMap<>();
            data.put("avatarUrl", avatarUrl);
            
            return Result.success(data);
        } catch (IOException e) {
            return Result.error("500", "文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.error("500", "头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 实现搜索框查找,实现精确查找加模糊查找，优先精确查找
     */
    @PostMapping("/search")
    public Result searchUser(@RequestHeader("Authorization") String tokenHeader,
                             @RequestBody Map<String, String> payload){
        String keyword = payload.get("keyword");
        List<User> result = new ArrayList<>();

        // 先查精确匹配
        User exactUser = userService.searchUserByExactUsername(keyword);
        if (exactUser != null) {
            result.add(exactUser);
            return Result.success(exactUser);
        }

        // 如果没有精确匹配，再做模糊查询
        List<User> searchUser = userService.searchUsersByKeyword(keyword);
        return Result.success(searchUser);
    }

    /**
     * 根据用户ID获取用户信息
     */
    @GetMapping("/info/{userId}")
    public Result getUserById(@PathVariable Integer userId) {
        User user = userService.getUserById(userId);
        if (user != null) {
            // 出于安全考虑，不返回密码
            user.setPassword(null);
            return Result.success(user);
        }
        return Result.error("404", "用户不存在");
    }

    /**
     * 更新用户的公钥（用于端到端加密）
     */
    @PostMapping("/public-key")
    public Result updatePublicKey(@RequestHeader("Authorization") String tokenHeader,
                                  @RequestBody Map<String, String> request) {
        try {
            String token = tokenHeader.replace("Bearer ", "");
            Integer userId = JwtUtil.getUserIdFromToken(token);
            String publicKey = request.get("publicKey");
            
            if (publicKey == null || publicKey.trim().isEmpty()) {
                return Result.error("400", "公钥不能为空");
            }
            
            boolean success = userService.updatePublicKey(userId, publicKey);
            if (success) {
                return Result.success("公钥更新成功");
            } else {
                return Result.error("500", "公钥更新失败");
            }
        } catch (Exception e) {
            return Result.error("500", "更新公钥失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定用户的公钥（用于端到端加密）
     */
    @GetMapping("/public-key/{userId}")
    public Result getPublicKey(@RequestHeader("Authorization") String tokenHeader,
                               @PathVariable Integer userId) {
        try {
            String token = tokenHeader.replace("Bearer ", "");
            // 验证token有效性
            JwtUtil.getUserIdFromToken(token);
            
            String publicKey = userService.getPublicKey(userId);
            if (publicKey != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("userId", userId);
                data.put("publicKey", publicKey);
                return Result.success(data);
            } else {
                return Result.error("404", "用户公钥不存在");
            }
        } catch (Exception e) {
            return Result.error("500", "获取公钥失败: " + e.getMessage());
        }
    }

    /**
     * 批量获取好友的公钥
     */
    @PostMapping("/public-keys")
    public Result getPublicKeys(@RequestHeader("Authorization") String tokenHeader,
                                @RequestBody Map<String, List<Integer>> request) {
        try {
            String token = tokenHeader.replace("Bearer ", "");
            // 验证token有效性
            JwtUtil.getUserIdFromToken(token);
            
            List<Integer> userIds = request.get("userIds");
            if (userIds == null || userIds.isEmpty()) {
                return Result.error("400", "用户ID列表不能为空");
            }
            
            Map<Integer, String> publicKeys = userService.getPublicKeys(userIds);
            return Result.success(publicKeys);
        } catch (Exception e) {
            return Result.error("500", "获取公钥失败: " + e.getMessage());
        }
    }
}


