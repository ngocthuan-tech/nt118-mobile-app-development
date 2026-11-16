package com.aicoach.domain;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Display thresholds v1. XP is always supplied by the server, never granted locally. */
public final class AchievementRules {
    public static final class Tier {
        public final String id, name;
        public final long minimumXp;
        private Tier(String id, String name, long minimumXp) {
            this.id = id; this.name = name; this.minimumXp = minimumXp;
        }
    }
    public static final List<Tier> REALMS = Collections.unmodifiableList(Arrays.asList(
            new Tier("realm_luyen_khi", "Luyện Khí", 0),
            new Tier("realm_truc_co", "Trúc Cơ", 1000),
            new Tier("realm_ket_dan", "Kết Đan", 5000),
            new Tier("realm_nguyen_anh", "Nguyên Anh", 15000),
            new Tier("realm_hoa_than", "Hóa Thần", 40000)));
    public static final List<Tier> RANKS = Collections.unmodifiableList(Arrays.asList(
            new Tier("rank_novice", "Novice", 0),
            new Tier("rank_intermediate", "Intermediate", 200),
            new Tier("rank_advanced", "Advanced", 750),
            new Tier("rank_expert", "Expert", 2000),
            new Tier("rank_master", "Master", 5000),
            new Tier("rank_grandmaster", "Grandmaster", 12000)));

    public static final class Progress {
        public final Tier current, next;
        public final long xp, remaining;
        public final int percent;
        private Progress(List<Tier> tiers, long rawXp) {
            xp = Math.max(0, rawXp);
            int index = 0;
            while (index + 1 < tiers.size() && xp >= tiers.get(index + 1).minimumXp) index++;
            current = tiers.get(index);
            next = index + 1 < tiers.size() ? tiers.get(index + 1) : null;
            remaining = next == null ? 0 : next.minimumXp - xp;
            percent = next == null ? 100 : (int) (100 * (xp - current.minimumXp)
                    / (next.minimumXp - current.minimumXp));
        }
    }
    public static Progress realm(long totalXp) { return new Progress(REALMS, totalXp); }
    public static Progress rank(long exerciseXp) { return new Progress(RANKS, exerciseXp); }
    private AchievementRules() { }
}
