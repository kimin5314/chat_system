package com.example.springboot.repository;

import com.example.springboot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

//@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    User findByUsername(String username);

    List<User> findByUsernameContaining(String keyword);

    @Modifying
    @Query("UPDATE User u SET u.password = :newPassword WHERE u.username = :username")
    void changePassword(@Param("username") String username,
                        @Param("newPassword") String newPassword);

//    @Modifying
//    @Query("UPDATE User u SET u.displayName = :displayName WHERE u.username = :username")
//    void updateDisplayName(@Param("username") String username,
//                           @Param("displayName") String displayName);
}
