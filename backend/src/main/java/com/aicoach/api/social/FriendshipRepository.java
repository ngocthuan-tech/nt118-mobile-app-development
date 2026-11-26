package com.aicoach.api.social;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, String> {

    List<Friendship> findByUserId(String userId);

    boolean existsByUserIdAndFriendId(String userId, String friendId);
}
