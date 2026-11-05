package com.aicoach.ui.challenge;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.aicoach.common.Resource;
import com.aicoach.data.repository.SocialRepository;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Keeps form/filter state across rotation; the midterm data itself is intentionally in memory. */
public class SocialViewModel extends ViewModel {
    private final SocialRepository repo = SocialRepository.get();
    private final SavedStateHandle saved;
    public final Set<String> selected = new HashSet<>();
    public SocialViewModel(SavedStateHandle saved) {
        this.saved = saved;
        ArrayList<String> ids = saved.get("selected");
        if (ids != null) selected.addAll(ids);
    }
    public LiveData<Resource<SocialRepository.Snapshot>> state() { return repo.changes(); }
    public int filter() { return value("filter", 0); }
    public void filter(int value) { saved.set("filter", value); }
    public int target() { return value("target", 50); }
    public void target(int value) { saved.set("target", Math.max(10, Math.min(1000, value))); }
    public int days() { return value("days", 7); }
    public void days(int value) { saved.set("days", value); }
    public String exercise() { return value("exercise", "squat"); }
    public void exercise(String value) { saved.set("exercise", value); }
    private <T> T value(String key, T fallback) {
        T value = saved.get(key); return value == null ? fallback : value;
    }
    public String create(String name, boolean privateGroup) {
        return repo.create(name, exercise(), target(), days(), privateGroup);
    }
    public List<SocialRepository.ChallengeItem> challenges() { return repo.challenges(); }
    public SocialRepository.ChallengeItem find(String id) { return repo.find(id); }
    public List<SocialRepository.Friend> leaderboard() { return repo.leaderboard(filter() == 1); }
    public List<SocialRepository.Friend> friends(String query) {
        List<SocialRepository.Friend> result = new ArrayList<>();
        String normalized = normalize(query);
        for (SocialRepository.Friend friend : repo.friends()) {
            if (normalize(friend.name).contains(normalized)) result.add(friend);
        }
        return result;
    }
    private static String normalize(String text) {
        return Normalizer.normalize(text.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace('đ', 'd');
    }
    public void select(String id, boolean on) {
        if (on) selected.add(id); else selected.remove(id);
        saved.set("selected", new ArrayList<>(selected));
    }
    public void invite(String id) {
        repo.invite(id, selected); selected.clear(); saved.set("selected", new ArrayList<String>());
    }
    public List<SocialRepository.Notice> notices() {
        List<SocialRepository.Notice> result = new ArrayList<>();
        for (SocialRepository.Notice n : repo.notices()) {
            if (filter() == 0 || (filter() == 1) == n.challenge) result.add(n);
        }
        return result;
    }
    public void readAll() { repo.readAll(); }
    public void markRead(String id) { repo.markRead(id); }
    public void resolve(String id, boolean accept) { repo.resolve(id, accept); }
}
