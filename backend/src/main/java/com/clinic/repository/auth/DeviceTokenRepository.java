package com.clinic.repository.auth;

import com.clinic.entity.auth.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Integer> {
    Optional<DeviceToken> findByToken(String token);
    List<DeviceToken> findByAccount_AccountId(Integer accountId);
}
