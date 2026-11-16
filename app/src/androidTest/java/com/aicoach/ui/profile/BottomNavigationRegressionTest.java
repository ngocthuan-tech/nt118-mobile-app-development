package com.aicoach.ui.profile;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.navigation.fragment.NavHostFragment;
import com.aicoach.MainActivity;
import com.aicoach.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class BottomNavigationRegressionTest {
    @Test public void homeAndLibraryResolveFromSiblingGraphs() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                NavHostFragment host = (NavHostFragment) activity.getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
                BottomNavigationView tabs = activity.findViewById(R.id.bottomNav);
                for (int source : new int[]{R.id.nav_challenge, R.id.nav_social}) {
                    host.getNavController().navigate(source);
                    tabs.setSelectedItemId(R.id.homeFragment);
                    assertEquals(R.id.homeFragment, host.getNavController().getCurrentDestination().getId());
                    host.getNavController().navigate(source);
                    tabs.setSelectedItemId(R.id.exerciseLibraryFragment);
                    assertEquals(R.id.exerciseLibraryFragment, host.getNavController().getCurrentDestination().getId());
                }
            });
        }
    }
}
