package com.aicoach.ui.profile;

import android.widget.TextView;
import androidx.navigation.fragment.NavHostFragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.aicoach.MainActivity;
import com.aicoach.R;
import com.aicoach.data.local.SessionEndReason;
import com.aicoach.data.local.SessionEvents;
import com.aicoach.data.local.TokenManager;
import com.aicoach.ui.auth.SessionEndedDialog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SessionEndedFlowTest {
    private TokenManager tokens;
    private String previous;
    @Before public void before(){
        tokens=TokenManager.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext());
        previous=tokens.getToken();tokens.clear();main(SessionEvents::consume);
    }
    @After public void after(){tokens.clear();if(previous!=null)tokens.saveToken(previous);main(SessionEvents::consume);}
    private static void main(Runnable r){InstrumentationRegistry.getInstrumentation().runOnMainSync(r);}
    private static void idle(){InstrumentationRegistry.getInstrumentation().waitForIdleSync();}
    private static NavHostFragment host(MainActivity a){return (NavHostFragment)a.getSupportFragmentManager().findFragmentById(R.id.navHostFragment);}
    private void queue(SessionEndReason reason){tokens.saveToken("old");assertTrue(tokens.expireIfMatches("old",reason));}
    @Test public void anotherDeviceNoticeSurvivesRotationAndBackCannotRevealProfile() throws Exception {
        queue(SessionEndReason.PASSWORD_CHANGED);
        try(ActivityScenario<MainActivity> s=ActivityScenario.launch(MainActivity.class)) {
            idle(); assertDialog(s,R.string.session_password_changed);
            capture("session-other-device"); s.recreate();idle();assertDialog(s,R.string.session_password_changed);
            s.onActivity(a->{
                assertEquals(R.id.loginFragment,host(a).getNavController().getCurrentDestination().getId());
                assertNull(host(a).getNavController().getPreviousBackStackEntry());
                ((SessionEndedDialog)a.getSupportFragmentManager().findFragmentByTag(SessionEndedDialog.TAG)).dismiss();
            });idle();
            s.onActivity(a->assertNull(a.getSupportFragmentManager().findFragmentByTag(SessionEndedDialog.TAG)));
        }
    }
    @Test public void successNoticeExplainsAllOldSessionsAndRequiresNewPassword() {
        queue(SessionEndReason.PASSWORD_UPDATED_HERE);
        try(ActivityScenario<MainActivity> s=ActivityScenario.launch(MainActivity.class)) {
            idle();assertDialog(s,R.string.session_password_updated_here);assertFalse(tokens.isLoggedIn());
        }
    }
    @Test public void duplicateSignalsShowOnlyOneDialog() {
        queue(SessionEndReason.EXPIRED);
        try(ActivityScenario<MainActivity> s=ActivityScenario.launch(MainActivity.class)) {
            idle(); main(SessionEvents::notifyExpired);idle();main(SessionEvents::notifyExpired);idle();
            assertDialog(s,R.string.session_expired);
            s.onActivity(a->assertEquals(1,a.getSupportFragmentManager().getFragments().stream()
                    .filter(f->f instanceof SessionEndedDialog).count()));
        }
    }
    private void assertDialog(ActivityScenario<MainActivity> s,int message){
        s.onActivity(a->{
            SessionEndedDialog dialog=(SessionEndedDialog)a.getSupportFragmentManager().findFragmentByTag(SessionEndedDialog.TAG);
            assertNotNull(dialog);assertTrue(dialog.requireDialog().isShowing());
            TextView text=dialog.requireDialog().findViewById(android.R.id.message);
            assertEquals(a.getString(message),text.getText().toString());
        });
    }
    private static void capture(String name) throws Exception {
        if(!"true".equals(InstrumentationRegistry.getArguments().getString("captureScreenshots")))return;
        Thread.sleep(500);idle();
        android.graphics.Bitmap bitmap=InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        java.io.File dir=InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir("session-tests");
        try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(dir,name+".png"))){bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);}
        finally{bitmap.recycle();}
    }
}
