package com.aicoach.ui.profile;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.aicoach.R;
import com.aicoach.databinding.ItemAchievementRealmBinding;
import com.aicoach.domain.AchievementRules;
import java.util.ArrayList;
import java.util.List;

/** Five immutable milestones; filtering does not change any XP. */
final class RealmAdapter extends RecyclerView.Adapter<RealmAdapter.Holder> {
    private final List<AchievementRules.Tier> items = new ArrayList<>();
    private long xp;
    @android.annotation.SuppressLint("NotifyDataSetChanged") // Only five static milestones; no history list.
    void submit(Long totalXp, int filter) {
        items.clear();
        xp = totalXp == null ? 0 : Math.max(0, totalXp);
        if (totalXp != null) for (AchievementRules.Tier tier : AchievementRules.REALMS) {
            boolean earned = xp >= tier.minimumXp;
            if (filter == 0 || (filter == 1) == earned) items.add(tier);
        }
        notifyDataSetChanged();
    }
    @Override public int getItemCount() { return items.size(); }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(ItemAchievementRealmBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder h, int position) {
        AchievementRules.Tier tier = items.get(position);
        boolean earned = xp >= tier.minimumXp;
        boolean current = AchievementRules.realm(xp).current == tier;
        AchievementArt.bind(h.b.imgRealm, tier);
        h.b.imgRealm.setAlpha(earned ? 1f : .35f);
        h.b.tvRealmName.setText(tier.name);
        h.b.tvRealmThreshold.setText(h.itemView.getContext().getString(R.string.achievement_threshold, AchievementArt.number(tier.minimumXp)));
        h.b.tvRealmState.setText(current ? R.string.achievement_current : earned ? R.string.achievement_unlocked : R.string.achievement_locked);
    }
    static final class Holder extends RecyclerView.ViewHolder {
        final ItemAchievementRealmBinding b;
        Holder(ItemAchievementRealmBinding b) { super(b.getRoot()); this.b = b; }
    }
}
