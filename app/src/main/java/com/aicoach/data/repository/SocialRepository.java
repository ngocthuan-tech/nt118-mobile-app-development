package com.aicoach.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.aicoach.common.Resource;
import com.aicoach.data.mock.MockSocialData;
import com.aicoach.data.model.Challenge;
import java.time.LocalDate;
import java.util.*;

/** Midterm data is process-local. No remote invitations, membership, or XP are changed. */
public final class SocialRepository extends BaseRepository {
    public static final class ChallengeItem extends Challenge {
        public final String id, name, exercise;
        public final int target, days;
        public final boolean privateGroup;
        public final Set<String> invited;

        public ChallengeItem(String id, String name, String exercise, int target, int days,
                boolean privateGroup, Set<String> invited) {
            super(id, name, "Bạn (mẫu)", exercise, target, LocalDate.now().toString(),
                    LocalDate.now().plusDays(days - 1).toString(), Collections.emptyList());
            this.id = id; this.name = name; this.exercise = exercise;
            this.target = target; this.days = days; this.privateGroup = privateGroup;
            this.invited = Collections.unmodifiableSet(new HashSet<>(invited));
        }
    }

    public static String invitationCode(ChallengeItem challenge) {
        return challenge.id.replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
    }

    public static final class Friend {
        public final String id, name;
        public final int points;
        public Friend(String id, String name, int points) {
            this.id = id; this.name = name; this.points = points;
        }
    }

    public static final class Notice {
        public final String id, title, body;
        public final boolean challenge, read, accepted, declined;
        public Notice(String id, String title, String body, boolean challenge,
                boolean read, boolean accepted, boolean declined) {
            this.id = id; this.title = title; this.body = body; this.challenge = challenge;
            this.read = read; this.accepted = accepted; this.declined = declined;
        }
        Notice update(boolean read, boolean accepted, boolean declined) {
            return new Notice(id, title, body, challenge, read, accepted, declined);
        }
    }

    public static final class Snapshot {
        public final List<ChallengeItem> challenges;
        public final List<Notice> notices;
        Snapshot(List<ChallengeItem> challenges, List<Notice> notices) {
            this.challenges = Collections.unmodifiableList(new ArrayList<>(challenges));
            this.notices = Collections.unmodifiableList(new ArrayList<>(notices));
        }
    }

    private static final SocialRepository INSTANCE = new SocialRepository();
    public static SocialRepository get() { return INSTANCE; }
    private final List<ChallengeItem> challenges = new ArrayList<>(MockSocialData.challenges());
    private final List<Friend> friends = MockSocialData.friends();
    private final List<Notice> notices = new ArrayList<>(MockSocialData.notices());
    private final MutableLiveData<Resource<Snapshot>> state;

    private SocialRepository() { state = new MutableLiveData<>(mock(snapshot()).getValue()); }
    private Snapshot snapshot() { return new Snapshot(challenges, notices); }
    public LiveData<Resource<Snapshot>> changes() { return state; }
    private void changed() { state.setValue(Resource.success(snapshot())); }
    public List<ChallengeItem> challenges() { return new ArrayList<>(challenges); }
    public ChallengeItem find(String id) {
        for (ChallengeItem c : challenges) if (c.id.equals(id)) return c;
        return null;
    }
    public List<Friend> friends() { return new ArrayList<>(friends); }
    public List<Friend> leaderboard(boolean national) {
        List<Friend> result = new ArrayList<>(friends);
        if (national) result.addAll(MockSocialData.nationalLeaders());
        result.sort((a, b) -> Integer.compare(b.points, a.points));
        return result;
    }
    public List<Notice> notices() { return new ArrayList<>(notices); }

    public String create(String name, String exercise, int target, int days, boolean privateGroup) {
        if (name == null || name.trim().length() < 3 || name.trim().length() > 50
                || target < 10 || target > 1000 || !Arrays.asList(3, 7, 14, 30).contains(days)
                || !Arrays.asList("squat", "pushup", "lunge", "jumping_jack").contains(exercise)) {
            throw new IllegalArgumentException("Invalid challenge");
        }
        String id = UUID.randomUUID().toString();
        challenges.add(0, new ChallengeItem(id, name.trim(), exercise, target, days,
                privateGroup, Collections.emptySet()));
        changed();
        return id;
    }

    public void invite(String id, Set<String> ids) {
        ChallengeItem current = find(id);
        if (current == null) throw new IllegalArgumentException("Unknown challenge");
        Set<String> invited = new HashSet<>(current.invited);
        for (Friend friend : friends) if (ids.contains(friend.id)) invited.add(friend.id);
        challenges.set(challenges.indexOf(current), new ChallengeItem(current.id, current.name,
                current.exercise, current.target, current.days, current.privateGroup, invited));
        changed();
    }

    /** Clear process-local user-entered demo data on explicit logout. */
    public void resetDemoData() {
        challenges.clear(); challenges.addAll(MockSocialData.challenges());
        notices.clear(); notices.addAll(MockSocialData.notices());
        changed();
    }

    public void readAll() {
        for (int i = 0; i < notices.size(); i++) {
            Notice n = notices.get(i);
            notices.set(i, n.update(true, n.accepted, n.declined));
        }
        changed();
    }

    /** Opening an invitation marks it read, without accepting or declining it. */
    public void markRead(String id) { updateNotice(id, null); }
    public void resolve(String id, boolean accept) { updateNotice(id, accept); }
    private void updateNotice(String id, Boolean accept) {
        for (int i = 0; i < notices.size(); i++) {
            Notice n = notices.get(i);
            if (!n.id.equals(id)) continue;
            boolean canResolve = n.challenge && !n.accepted && !n.declined && accept != null;
            notices.set(i, n.update(true, canResolve ? accept : n.accepted,
                    canResolve ? !accept : n.declined));
            changed();
            return;
        }
    }
}
