package com.example.ql_congviec;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;

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
@Config(sdk = 34, manifest = Config.NONE)
public class RegisterActivityTest {

    private Register activity;
    private DBHelperDatabase mockDbh;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.buildActivity(Register.class)
                .create().start().resume().get();

        // Mock DB và inject vào activity
        mockDbh = Mockito.mock(DBHelperDatabase.class);
        setPrivateField(activity, "dbh", mockDbh);

        ShadowToast.reset();
    }

    // TC-1: fullName rỗng
    @Test
    public void TC1_emptyFullName_showToast_noDb() throws Exception {
        callHandleRegister(activity, "", "a@gmail.com", "user", "123", "123");
        assertEquals("Vui lòng điền đầy đủ thông tin.", ShadowToast.getTextOfLatestToast());
        verifyNoInteractions(mockDbh);
    }
    // TC-2: email rỗng
    @Test
    public void TC2_emptyEmail_showToast_noDb() throws Exception {
        callHandleRegister(activity, "A", "", "user", "123", "123");
        assertEquals("Vui lòng điền đầy đủ thông tin.", ShadowToast.getTextOfLatestToast());
        verifyNoInteractions(mockDbh);
    }
    // TC-3: username rỗng
    @Test
    public void TC3_emptyUsername_showToast_noDb() throws Exception {
        callHandleRegister(activity, "A", "a@gmail.com", "", "123", "123");
        assertEquals("Vui lòng điền đầy đủ thông tin.", ShadowToast.getTextOfLatestToast());
        verifyNoInteractions(mockDbh);
    }
    // TC-4: password rỗng
    @Test
    public void TC4_emptyPassword_showToast_noDb() throws Exception {
        callHandleRegister(activity, "A", "a@gmail.com", "user", "", "123");
        assertEquals("Vui lòng điền đầy đủ thông tin.", ShadowToast.getTextOfLatestToast());
        verifyNoInteractions(mockDbh);
    }

    // TC-5: confirm password rỗng
    @Test
    public void TC5_emptyConfirmPassword_showToast_noDb() throws Exception {
        callHandleRegister(activity, "A", "a@gmail.com", "user", "123", "");
        assertEquals("Vui lòng điền đầy đủ thông tin.", ShadowToast.getTextOfLatestToast());
        verifyNoInteractions(mockDbh);
    }
    // TC-6: password != confirm
    @Test
    public void TC6_passwordMismatch_showToast_noDb() throws Exception {
        callHandleRegister(activity, "A", "a@gmail.com", "user", "123", "456");
        assertEquals("Password và Confirm Password không khớp.", ShadowToast.getTextOfLatestToast());
        verifyNoInteractions(mockDbh);
        assertNull(shadowOf(activity).getNextStartedActivity());
        assertFalse(activity.isFinishing());
    }
    // TC-7: username hoặc email đã tồn tại
    @Test
    public void TC7_duplicateUserOrEmail_showToast_noNavigate() throws Exception {
        when(mockDbh.insertUserRaw("user", "123", "A", "a@gmail.com")).thenReturn(false);
        callHandleRegister(activity, "A", "a@gmail.com", "user", "123", "123");
        assertEquals("Đăng ký thất bại: username hoặc email đã tồn tại.", ShadowToast.getTextOfLatestToast());
        assertNull(shadowOf(activity).getNextStartedActivity());
        assertFalse(activity.isFinishing());
        verify(mockDbh).insertUserRaw("user", "123", "A", "a@gmail.com");
    }

    // TC-8: đăng ký thành công
    @Test
    public void TC8_success_showToast_navigateLogin() throws Exception {
        when(mockDbh.insertUserRaw("user_new", "123", "A", "a@gmail.com")).thenReturn(true);
        callHandleRegister(activity, "A", "a@gmail.com", "user_new", "123", "123");
        assertEquals("Đăng ký thành công!", ShadowToast.getTextOfLatestToast());
        Intent next = shadowOf(activity).getNextStartedActivity();
        assertNotNull(next);
        assertEquals(Login.class.getName(), next.getComponent().getClassName());
        assertTrue(activity.isFinishing());
        verify(mockDbh).insertUserRaw("user_new", "123", "A", "a@gmail.com");
    }

    /* ---------------- Helpers ---------------- */
    private static void callHandleRegister(Object target,
                                           String full, String mail, String user, String pass, String conf)
            throws Exception {
        Method m = target.getClass().getDeclaredMethod(
                "handleRegister", String.class, String.class, String.class, String.class, String.class);
        m.setAccessible(true);
        m.invoke(target, full, mail, user, pass, conf);
    }

    private static void setPrivateField(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }
}
