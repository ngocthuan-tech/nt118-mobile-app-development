package com.aicoach.api.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount, String> {

    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserAccount u where u.id = :id")
    Optional<UserAccount> lockForPasswordChange(@Param("id") String id);

    @Modifying(flushAutomatically = true)
    @Query("update UserAccount u set u.sessionVersion = u.sessionVersion + 1 where u.id = :id")
    int revokeSessions(@Param("id") String id);
}
