package com.aicoach.api.auth.reset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {

    /** Mã mới nhất của người dùng — chỉ mã này còn dùng được, mã cũ hơn coi như huỷ. */
    Optional<PasswordResetCode> findFirstByUserIdOrderByCreatedAtDesc(String userId);

    @Modifying
    @Query("delete from PasswordResetCode c where c.userId = :userId")
    void deleteAllForUser(@Param("userId") String userId);
}
