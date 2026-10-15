package com.aicoach;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.aicoach.databinding.ActivityMainBinding;

/**
 * Activity duy nhất của app. Mọi màn hình là Fragment, điều hướng bằng nav_root.xml.
 * Không ai tạo thêm Activity khi chưa báo Trưởng nhóm.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Màn chờ chuẩn Android 12+ (S01). Phải gọi trước super.onCreate.
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applySystemBarInsets();
        setupNavigation();
    }

    private void setupNavigation() {
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navHostFragment);
        if (navHost == null) return;
        NavController navController = navHost.getNavController();

        NavigationUI.setupWithNavController(binding.bottomNav, navController);

        // Ẩn thanh tab ở luồng đăng nhập và khi đang tập với camera
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean hideBottomNav = isInsideGraph(destination, R.id.nav_auth)
                    || isInsideGraph(destination, R.id.nav_workout);
            binding.bottomNav.setVisibility(hideBottomNav ? View.GONE : View.VISIBLE);
        });

        // TODO (Thành viên A): khi đã có google-services.json, nếu chưa đăng nhập thì
        //  navController.navigate(R.id.action_global_to_auth);
    }

    private static boolean isInsideGraph(@NonNull NavDestination destination, int graphId) {
        NavGraph parent = destination.getParent();
        while (parent != null) {
            if (parent.getId() == graphId) return true;
            parent = parent.getParent();
        }
        return false;
    }

    /** Chừa chỗ cho thanh trạng thái và thanh điều hướng hệ thống (Android 15 bắt buộc edge-to-edge). */
    private void applySystemBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
