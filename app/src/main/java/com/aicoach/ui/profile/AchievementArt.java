package com.aicoach.ui.profile;

import android.widget.ImageView;
import com.aicoach.R;
import com.aicoach.domain.AchievementRules;
import com.bumptech.glide.Glide;
import java.text.NumberFormat;
import java.util.Locale;

final class AchievementArt {
    private AchievementArt() { }
    static String number(long value) { return NumberFormat.getIntegerInstance(new Locale("vi", "VN")).format(value); }
    static void bind(ImageView image, AchievementRules.Tier tier) {
        image.setImageTintList(null);
        Glide.with(image).load(drawable(tier)).override(432, 432).fitCenter().into(image);
    }
    @androidx.annotation.DrawableRes static int drawable(AchievementRules.Tier tier) {
        switch (tier.id) {
            case "realm_luyen_khi": return R.drawable.realm_luyen_khi;
            case "realm_truc_co": return R.drawable.realm_truc_co;
            case "realm_ket_dan": return R.drawable.realm_ket_dan;
            case "realm_nguyen_anh": return R.drawable.realm_nguyen_anh;
            case "realm_hoa_than": return R.drawable.realm_hoa_than;
            case "rank_novice": return R.drawable.rank_novice;
            case "rank_intermediate": return R.drawable.rank_intermediate;
            case "rank_advanced": return R.drawable.rank_advanced;
            case "rank_expert": return R.drawable.rank_expert;
            case "rank_master": return R.drawable.rank_master;
            case "rank_grandmaster": return R.drawable.rank_grandmaster;
            default: throw new IllegalArgumentException("Unknown achievement art: " + tier.id);
        }
    }
    static String next(android.content.Context context, AchievementRules.Progress p) {
        return p.next == null ? context.getString(R.string.achievement_max)
                : context.getString(R.string.achievement_next, number(p.remaining), p.next.name);
    }
}
