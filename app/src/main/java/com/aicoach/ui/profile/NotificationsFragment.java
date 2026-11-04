package com.aicoach.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentNotificationsBinding;
import com.aicoach.databinding.ItemNotificationBinding;
import com.aicoach.data.repository.SocialRepository;
import com.aicoach.ui.challenge.SocialRows;
import com.aicoach.ui.challenge.SocialViewModel;
import com.aicoach.ui.history.SegmentGroup;
import java.util.List;

public class NotificationsFragment extends BaseFragment<FragmentNotificationsBinding> {
    private SocialViewModel vm;
    private SocialRows<SocialRepository.Notice> adapter;
    @Override protected FragmentNotificationsBinding inflate(@NonNull LayoutInflater i, @Nullable ViewGroup c) {
        return FragmentNotificationsBinding.inflate(i, c, false);
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        vm = new ViewModelProvider(this).get(SocialViewModel.class);
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.btnReadAll.setOnClickListener(v -> vm.readAll());
        adapter = new SocialRows<>(R.layout.item_notification, n -> n.id, (row, n, pos) -> {
            ItemNotificationBinding b = ItemNotificationBinding.bind(row);
            b.tvSection.setVisibility(pos == 0 ? View.VISIBLE : View.GONE);
            b.tvSection.setText(R.string.social_demo_notice);
            b.tvTitle.setText(n.title);
            b.tvBody.setText(n.body);
            b.tvTime.setText(n.accepted ? R.string.social_notification_accepted
                    : n.declined ? R.string.social_notification_declined
                    : n.read ? R.string.social_notification_read : R.string.social_notification_unread);
            b.imgIcon.setImageResource(n.challenge ? R.drawable.ic_medal_24 : R.drawable.ic_alert_24);
            b.dotUnread.setVisibility(n.read ? View.GONE : View.VISIBLE);
            b.rowActions.setVisibility(n.challenge && !n.accepted && !n.declined ? View.VISIBLE : View.GONE);
            b.btnJoin.setOnClickListener(v -> { vm.resolve(n.id, true); openChallenges(); });
            b.btnDecline.setOnClickListener(v -> vm.resolve(n.id, false));
            b.content.setOnClickListener(v -> { vm.markRead(n.id); if (n.challenge) openChallenges(); });
        });
        binding.rvNotifications.setAdapter(adapter);
        SegmentGroup.bind(vm.filter(), index -> { vm.filter(index); render(); },
                binding.btnFilterAll, binding.btnFilterChallenges, binding.btnFilterSystem);
        vm.state().observe(getViewLifecycleOwner(), ignored -> render());
    }
    private void openChallenges() {
        NavHostFragment.findNavController(this).navigate(NotificationsFragmentDirections.actionNotificationsToChallenges());
    }
    private void render() {
        List<SocialRepository.Notice> list = vm.notices();
        adapter.submit(list);
        binding.emptyState.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }
    @Override public void onDestroyView() {
        binding.rvNotifications.setAdapter(null);
        super.onDestroyView();
    }
}
