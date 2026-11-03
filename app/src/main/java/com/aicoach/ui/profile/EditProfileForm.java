package com.aicoach.ui.profile;

import com.aicoach.data.model.User;
import com.aicoach.data.remote.dto.ProfileDtos;
import java.util.Objects;

/** Draft retained by the ViewModel; requests contain only changed fields. */
final class EditProfileForm {
    static final String[] GOALS = {"lose_weight", "build_muscle", "stay_fit"};
    static final String[] LEVELS = {"beginner", "intermediate", "advanced"};
    final User original;
    String name, height, weight, goal, level;
    int sessions;
    EditProfileForm(User u) {
        original = u;
        name = u.getDisplayName() == null ? "" : u.getDisplayName();
        height = u.getHeightCm() == null ? "" : String.valueOf(u.getHeightCm());
        weight = u.getWeightKg() == null ? "" : String.valueOf(u.getWeightKg());
        goal = u.getGoal(); level = u.getLevel(); sessions = u.getWeeklyGoalSessions();
    }
    static Double number(String text) {
        try { return Double.valueOf(text.trim().replace(',', '.')); }
        catch (NumberFormatException e) { return null; }
    }
    boolean validName() { return name.trim().length() >= 2 && name.trim().length() <= 50; }
    boolean validHeight() { return height.trim().isEmpty() && original.getHeightCm() == null || ProfileNumbers.validHeight(number(height)); }
    boolean validWeight() { return weight.trim().isEmpty() && original.getWeightKg() == null || ProfileNumbers.validWeight(number(weight)); }
    boolean valid() { return validName() && validHeight() && validWeight() && sessions >= 1 && sessions <= 7; }
    boolean dirty() {
        return !Objects.equals(name.trim(), original.getDisplayName()) || !Objects.equals(number(height), original.getHeightCm())
                || !Objects.equals(number(weight), original.getWeightKg()) || !Objects.equals(goal, original.getGoal())
                || !Objects.equals(level, original.getLevel()) || sessions != original.getWeeklyGoalSessions();
    }
    ProfileDtos.UpdateProfileRequest request() {
        ProfileDtos.UpdateProfileRequest r = new ProfileDtos.UpdateProfileRequest();
        if (!Objects.equals(name.trim(), original.getDisplayName())) r.displayName(name.trim());
        if (!Objects.equals(number(height), original.getHeightCm())) r.heightCm(number(height));
        if (!Objects.equals(number(weight), original.getWeightKg())) r.weightKg(number(weight));
        if (!Objects.equals(goal, original.getGoal())) r.goal(goal);
        if (!Objects.equals(level, original.getLevel())) r.level(level);
        if (sessions != original.getWeeklyGoalSessions()) r.weeklyGoalSessions(sessions);
        return r;
    }
}
