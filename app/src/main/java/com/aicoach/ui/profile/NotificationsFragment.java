package com.aicoach.ui.profile;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.common.Resource;
import com.aicoach.data.model.Social;
import com.aicoach.databinding.FragmentNotificationsBinding;
import com.aicoach.databinding.ItemNotificationBinding;
import com.aicoach.ui.challenge.SocialFormat;
import com.aicoach.ui.challenge.SocialRows;
import com.aicoach.ui.challenge.SocialViewModel;
import com.aicoach.ui.history.SegmentGroup;
import com.google.android.material.snackbar.Snackbar;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 22 Thông báo (mở từ chuông ở Trang chủ): lời mời thử thách có nút Tham gia / Từ chối, thông báo hệ thống;
 * chia nhóm theo ngày, chưa đọc có nền nổi bật. Dữ liệu từ GET /notifications.
 */
public class NotificationsFragment extends BaseFragment<FragmentNotificationsBinding> {
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private SocialViewModel vm;
    private SocialRows<Row> adapter;

    /** Một dòng đã tính sẵn tiêu đề nhóm ngày, để adapter không phải nhìn sang dòng bên cạnh. */
    static final class Row {
        final Social.Notice notice;
        @Nullable final String section;

        Row(Social.Notice notice, @Nullable String section) {
            this.notice = notice;
            this.section = section;
        }
    }

    @Override protected FragmentNotificationsBinding inflate(@NonNull LayoutInflater i, @Nullable ViewGroup c) {
        return FragmentNotificationsBinding.inflate(i, c, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        vm = new ViewModelProvider(this).get(SocialViewModel.class);
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.btnReadAll.setOnClickListener(v -> vm.readAll());
        adapter = new SocialRows<>(R.layout.item_notification, r -> String.valueOf(r.notice.getId()),
                (row, r, pos) -> bind(row, r));
        binding.rvNotifications.setAdapter(adapter);
        SegmentGroup.bind(vm.filter(), index -> {
            vm.filter(index);
            vm.loadNotices();
        }, binding.btnFilterAll, binding.btnFilterChallenges, binding.btnFilterSystem);
        vm.noticesState().observe(getViewLifecycleOwner(), r -> {
            if (r == null) return;
            if (r.status == Resource.Status.ERROR) showError(r.message);
            render(r.status == Resource.Status.LOADING);
        });
        vm.respondState().observe(getViewLifecycleOwner(), this::onResponded);
        vm.loadNotices();
    }

    private void bind(View view, Row row) {
        Context context = view.getContext();
        Social.Notice n = row.notice;
        ItemNotificationBinding b = ItemNotificationBinding.bind(view);
        boolean hasSection = row.section != null && !row.section.isEmpty();
        b.tvSection.setVisibility(hasSection ? View.VISIBLE : View.GONE);
        b.tvSection.setText(row.section);
        b.tvTitle.setText(n.getTitle());
        b.tvBody.setText(n.getBody());
        CharSequence when = relativeTime(n.getCreatedAt());
        String status = inviteStatus(context, n);
        b.tvTime.setText(status == null ? when : context.getString(R.string.social_notification_time, status, when));
        b.imgIcon.setImageResource(n.isChallenge() ? R.drawable.ic_medal_24 : R.drawable.ic_alert_24);
        b.dotUnread.setVisibility(n.isRead() ? View.GONE : View.VISIBLE);
        b.content.setBackgroundResource(n.isRead() ? 0 : R.drawable.bg_social_unread);
        b.rowActions.setVisibility(n.isPendingInvite() ? View.VISIBLE : View.GONE);
        boolean idle = !vm.responding();
        b.btnJoin.setEnabled(idle);
        b.btnDecline.setEnabled(idle);
        b.btnJoin.setOnClickListener(v -> respond(n, true));
        b.btnDecline.setOnClickListener(v -> respond(n, false));
        b.content.setOnClickListener(v -> {
            if (!n.isRead()) vm.markRead(n.getId());
            // Lời mời đang chờ phải bấm Tham gia / Từ chối; thông báo thử thách khác mở tab Thử thách.
            if (n.isChallenge() && !n.isPendingInvite()) openChallenges();
        });
    }

    @Nullable
    private static String inviteStatus(Context context, Social.Notice n) {
        if (!n.isInvite() || n.getInviteStatus() == null) return null;
        switch (n.getInviteStatus()) {
            case Social.Notice.ACCEPTED:
                return context.getString(R.string.social_notification_accepted);
            case Social.Notice.DECLINED:
                return context.getString(R.string.social_notification_declined);
            default:
                return context.getString(R.string.social_notification_pending);
        }
    }

    private static CharSequence relativeTime(@Nullable String iso) {
        long ms = SocialFormat.epochMillis(iso);
        if (ms < 0) return "";
        return DateUtils.getRelativeTimeSpanString(ms, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
    }

    private void respond(Social.Notice n, boolean accept) {
        vm.respond(n.getId(), accept);
        adapter.notifyItemRangeChanged(0, adapter.getItemCount());
    }

    private void onResponded(@Nullable Resource<Social.Notice> r) {
        if (r == null || r.status == Resource.Status.LOADING) return;
        vm.consumeRespond();
        if (r.status == Resource.Status.SUCCESS && r.data != null) {
            if (Social.Notice.ACCEPTED.equals(r.data.getInviteStatus())) {
                openChallenges();
                return;
            }
            Snackbar.make(binding.getRoot(), R.string.social_declined_done, Snackbar.LENGTH_SHORT).show();
        } else {
            showError(r.message);
        }
        if (adapter != null) adapter.notifyItemRangeChanged(0, adapter.getItemCount());
    }

    private void openChallenges() {
        NavHostFragment.findNavController(this).navigate(NotificationsFragmentDirections.actionNotificationsToChallenges());
    }

    private void render(boolean loading) {
        if (binding == null) return;
        List<Social.Notice> list = vm.noticeList();
        adapter.submit(rows(list));
        binding.emptyState.setVisibility(list.isEmpty() && !loading ? View.VISIBLE : View.GONE);
        binding.btnReadAll.setEnabled(vm.unreadCount() > 0);
    }

    private List<Row> rows(List<Social.Notice> list) {
        LocalDate today = LocalDate.now(SocialFormat.ZONE);
        List<Row> out = new ArrayList<>();
        LocalDate previous = null;
        for (int i = 0; i < list.size(); i++) {
            Social.Notice n = list.get(i);
            LocalDate day = SocialFormat.dateOf(n.getCreatedAt());
            String section = null;
            if (i == 0 || !Objects.equals(day, previous)) section = dayLabel(day, today);
            previous = day;
            out.add(new Row(n, section));
        }
        return out;
    }

    @Nullable
    private String dayLabel(@Nullable LocalDate day, LocalDate today) {
        if (day == null) return null;
        if (day.equals(today)) return getString(R.string.social_today);
        if (day.equals(today.minusDays(1))) return getString(R.string.social_yesterday);
        return day.format(DAY);
    }

    @Override public void onDestroyView() {
        binding.rvNotifications.setAdapter(null);
        adapter = null;
        super.onDestroyView();
    }
}
