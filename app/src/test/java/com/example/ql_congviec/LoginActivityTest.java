package com.example.ql_congviec;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.CheckBox;

import androidx.test.core.app.ApplicationProvider;

import com.example.ql_congviec.Database.DBHelperDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, manifest = Config.NONE)   // ép SDK 34 cho Robolectric
public class LoginActivityTest {

    private Login activity;
    private DBHelperDatabase mockDbh;

    private String PREFS_NAME, KEY_USERNAME, KEY_PASSWORD, KEY_REMEMBER, KEY_USER_ID;

    @Before
    public void setUp() throws Exception {
        // Xóa prefs TRƯỚC khi tạo Activity để tránh auto-login
        Context appCtx = ApplicationProvider.getApplicationContext();
        appCtx.getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit().clear().apply();

        activity = Robolectric.buildActivity(Login.class).create().start().resume().get();

        // Lấy hằng số private từ Login
        PREFS_NAME   = (String) getStaticField(Login.class, "PREFS_NAME");
        KEY_USERNAME = (String) getStaticField(Login.class, "KEY_USERNAME");
        KEY_PASSWORD = (String) getStaticField(Login.class, "KEY_PASSWORD");
        KEY_REMEMBER = (String) getStaticField(Login.class, "KEY_REMEMBER");
        KEY_USER_ID  = (String) getStaticField(Login.class, "KEY_USER_ID");

        // Mock DB & inject
        mockDbh = Mockito.mock(DBHelperDatabase.class);
        setPrivateField(activity, "dbh", mockDbh);

        // Fake checkbox Remember Me
        CheckBox cb = new CheckBox(activity);
        cb.setChecked(true);
        setPrivateField(activity, "cbRememberMe", cb);

        ShadowToast.reset();
    }
    @Test
    public void TC1_emptyUsername_showToast() throws Exception {
        callHandleLogin(activity, "", "abc");
        assertEquals("Vui lòng nhập username và password.", ShadowToast.getTextOfLatestToast());
        verifyNoInteractions(mockDbh);
    }

    @Test
    public void TC2_emptyPassword_showToast() throws Exception {
        callHandleLogin(activity, "user01", "");
        assertEquals("Vui lòng nhập username và password.", ShadowToast.getTextOfLatestToast());
        verifyNoMoreInteractions(mockDbh);
    }

    @Test
    public void TC3_wrongPassword_showToast() throws Exception {
        when(mockDbh.checkUserRaw("user01", "SaiPass")).thenReturn(false);
        callHandleLogin(activity, "user01", "SaiPass");
        assertEquals("Sai username hoặc password.", ShadowToast.getTextOfLatestToast());
        verify(mockDbh).checkUserRaw("user01", "SaiPass");
        verify(mockDbh, never()).getUserId(anyString());
        assertNull(shadowOf(activity).getNextStartedActivity());
    }

    @Test
    public void TC4_correctLogin_withRemember_navigateHome() throws Exception {
        when(mockDbh.checkUserRaw("user01", "123")).thenReturn(true);
        when(mockDbh.getUserId("user01")).thenReturn(1);
        ((CheckBox) getPrivateField(activity, "cbRememberMe")).setChecked(true);

        callHandleLogin(activity, "user01", "123");

        assertEquals("Đăng nhập thành công!", ShadowToast.getTextOfLatestToast());
        Intent next = shadowOf(activity).getNextStartedActivity();
        assertNotNull(next);
        assertEquals(MainActivity.class.getName(), next.getComponent().getClassName());
        assertTrue(activity.isFinishing());

        SharedPreferences sp = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        assertEquals("user01", sp.getString(KEY_USERNAME, null));
        assertEquals("123", sp.getString(KEY_PASSWORD, null));
        assertTrue(sp.getBoolean(KEY_REMEMBER, false));
        assertEquals(1, sp.getInt(KEY_USER_ID, -1));

        verify(mockDbh).checkUserRaw("user01", "123");
        verify(mockDbh).getUserId("user01");
    }


    // ---------- Helpers (reflection) ----------
    private static void callHandleLogin(Object target, String user, String pass) throws Exception {
        Method m = target.getClass().getDeclaredMethod("handleLogin", String.class, String.class);
        m.setAccessible(true);
        m.invoke(target, user, pass);
    }
    private static void setPrivateField(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }
    private static Object getPrivateField(Object target, String field) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        return f.get(target);
    }
    private static Object getStaticField(Class<?> cls, String field) throws Exception {
        Field f = cls.getDeclaredField(field);
        f.setAccessible(true);
        return f.get(null);
    }
}
