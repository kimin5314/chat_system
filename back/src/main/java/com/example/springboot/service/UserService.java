package com.example.springboot.service;

import com.example.springboot.dto.PasswordChangeDto;
import com.example.springboot.dto.UpdateInfoDto;
import com.example.springboot.entity.Tokens;
import com.example.springboot.entity.User;
import com.example.springboot.exception.CustomException;
import com.example.springboot.repository.TokensRepository;
import com.example.springboot.repository.UserRepository;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    @Resource
    private UserRepository userRepository;
    @Resource
    private TokensRepository tokensRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User  getUserInformation(String username) {
        User user = userRepository.findByUsername(username);
        return user;
    }

    public List<User> selectAll() {
        return userRepository.findAll();
    }

    public User login(User user) {
        String username = user.getUsername();
        String inputPassword = user.getPassword();

        User dbUser = userRepository.findByUsername(username);
        if (dbUser == null) {
            throw new CustomException("500", "账号不存在");
        }

        //  使用 passwordEncoder 比对密码
        boolean passwordMatches = passwordEncoder.matches(inputPassword, dbUser.getPassword());

        if (!passwordMatches) {
            throw new CustomException("500", "密码错误");
        }

        return dbUser;
    }

    public boolean register(User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return false; // 用户名已存在
        }

        //  对密码进行 hash 加密
        String rawPassword = user.getPassword();
        String hashedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(hashedPassword);

        // 设置默认头像和创建时间
        user.setAvatarUrl("https://avatars.githubusercontent.com/u/583231?v=4");
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
        return true;
    }



    public void insertToken(Tokens usertoken) {
        usertoken.setCreatedAt(LocalDateTime.now());
        usertoken.setExpiresAt(LocalDateTime.now().plusHours(2));
        tokensRepository.save(usertoken);
    }

    @Transactional
    public boolean changPassword(PasswordChangeDto passwordChangeDto) {
        if (userRepository.findByUsername(passwordChangeDto.getUsername())==null){
            return false;//用户不存在
        }
        User user =userRepository.findByUsername(passwordChangeDto.getUsername());
        String username = user.getUsername();
        String password =user.getPassword();
        boolean passwordMatches = passwordEncoder.matches(passwordChangeDto.getUserPassword(),password);
        if (!passwordMatches) {
            throw new CustomException("500", "密码错误");
        }
        else{
            //应该对新密码进行加密
            String hashPassword = passwordEncoder.encode(passwordChangeDto.getNewPassword());
            userRepository.changePassword(username, hashPassword);
            return true;
        }

    }

    public void updateInformation(UpdateInfoDto dto) {
        User user = userRepository.findByUsername(dto.getUsername());

        if (dto.getDisplayName() != null) {
            user.setDisplayName(dto.getDisplayName());
        }
        
        if (dto.getAvatarUrl() != null) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }
        
        userRepository.save(user);
    }
    public List<User> searchUsersByKeyword(String keyword) {
        return userRepository.findByUsernameContaining(keyword);
    }

    public User searchUserByExactUsername(String keyword) {
        return userRepository.findByUsername(keyword);
    }

    // 抛出自定义异常
    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("找不到用户，ID=" + userId));
    }

    /**
     * 更新用户的公钥
     */
    @Transactional
    public boolean updatePublicKey(Integer userId, String publicKey) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("找不到用户，ID=" + userId));
            
            user.setPublicKey(publicKey);
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取指定用户的公钥
     */
    public String getPublicKey(Integer userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            return user != null ? user.getPublicKey() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 批量获取用户的公钥
     */
    public java.util.Map<Integer, String> getPublicKeys(List<Integer> userIds) {
        java.util.Map<Integer, String> publicKeys = new java.util.HashMap<>();
        
        for (Integer userId : userIds) {
            try {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null && user.getPublicKey() != null) {
                    publicKeys.put(userId, user.getPublicKey());
                }
            } catch (Exception e) {
                // 忽略单个用户的错误，继续处理其他用户
            }
        }
        
        return publicKeys;
    }
}

