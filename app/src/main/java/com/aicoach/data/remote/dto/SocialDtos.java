package com.aicoach.data.remote.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Request của module Cộng đồng (thử thách, lời mời, thông báo). */
public final class SocialDtos {

    private SocialDtos() {
    }

    /** POST /challenges */
    public static class CreateChallengeRequest {
        final String title;
        final String exerciseId;
        final int dailyTarget;
        final int days;
        final boolean privateGroup;

        public CreateChallengeRequest(String title, String exerciseId, int dailyTarget, int days,
                                      boolean privateGroup) {
            this.title = title;
            this.exerciseId = exerciseId;
            this.dailyTarget = dailyTarget;
            this.days = days;
            this.privateGroup = privateGroup;
        }
    }

    /** POST /challenges/{id}/invites — tối đa 20 người mỗi lần. */
    public static class InviteRequest {
        final List<String> userIds;

        public InviteRequest(Collection<String> userIds) {
            this.userIds = new ArrayList<>(userIds);
        }
    }

    /** POST /challenges/join — "AIC-7Q2K" hoặc "7q2k". */
    public static class JoinRequest {
        final String code;

        public JoinRequest(String code) {
            this.code = code;
        }
    }

    /** POST /notifications/{id}/respond */
    public static class RespondRequest {
        final boolean accept;

        public RespondRequest(boolean accept) {
            this.accept = accept;
        }
    }
}
