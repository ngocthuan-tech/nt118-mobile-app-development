package com.aicoach.ui.workout;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.aicoach.R;
import com.aicoach.common.BaseFragment;
import com.aicoach.databinding.FragmentCameraPermissionBinding;
import com.aicoach.databinding.ViewInfoRowBinding;

/**
 * Figma 18 "Xin quyền camera" + 19 (hộp thoại hệ thống).
 * Màn đầu tiên của luồng tập AI: đã có quyền thì đi thẳng sang Kiểm tra sẵn sàng.
 */
public class CameraPermissionFragment extends BaseFragment<FragmentCameraPermissionBinding> {

    private String exerciseId;

    private final ActivityResultLauncher<String> requestCamera =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    goToReadiness();
                } else {
                    showError(getString(R.string.workout_permission_denied));
                }
            });

    @Override
    protected FragmentCameraPermissionBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentCameraPermissionBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        exerciseId = CameraPermissionFragmentArgs.fromBundle(requireArguments()).getExerciseId();

        if (hasCameraPermission()) {
            goToReadiness();
            return;
        }

        bindRow(binding.rowOnDevice, R.drawable.ic_shield_24, R.color.state_success, R.color.state_success_light,
                R.string.workout_permission_on_device, R.string.workout_permission_on_device_desc);
        bindRow(binding.rowNoStorage, R.drawable.ic_lock_24, R.color.brand_primary, R.color.brand_primary_light,
                R.string.workout_permission_no_storage, R.string.workout_permission_no_storage_desc);
        bindRow(binding.rowControl, R.drawable.ic_tune_24, R.color.medal_orange, R.color.state_warning_light,
                R.string.workout_permission_control, R.string.workout_permission_control_desc);

        binding.btnAllow.setOnClickListener(v -> requestCamera.launch(Manifest.permission.CAMERA));
        binding.btnLater.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Sang Kiểm tra sẵn sàng và bỏ màn xin quyền khỏi back stack. */
    private void goToReadiness() {
        NavController nav = NavHostFragment.findNavController(this);
        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.cameraPermissionFragment, true)
                .build();
        nav.navigate(CameraPermissionFragmentDirections.actionPermissionToReadiness(exerciseId), options);
    }

    private void bindRow(ViewInfoRowBinding row, @DrawableRes int icon, @ColorRes int tint, @ColorRes int bg,
                         @StringRes int title, @StringRes int subtitle) {
        row.imgIcon.setImageResource(icon);
        row.imgIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), tint)));
        row.iconBox.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), bg)));
        row.tvTitle.setText(title);
        row.tvSubtitle.setText(subtitle);
    }
}
