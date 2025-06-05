package com.example.springboot.repository;

import com.example.springboot.entity.Tokens;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TokensRepository extends JpaRepository<Tokens, Integer> {
}


