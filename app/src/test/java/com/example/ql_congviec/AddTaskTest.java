package com.example.ql_congviec;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ql_congviec.Database.DBHelperDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, manifest = Config.NONE)
public class AddTaskTest {

    private activity_task_detail screen; // Activity thật
    private DBHelperDatabase mockDbh;
    private SQLiteDatabase mockDb;
    private Cursor mockCursor;

    @Before
    public void setUp() throws Exception {
        screen = spy(Robolectric.buildActivity(activity_task_detail.class)
                .create().start().resume().get());

        mockDbh = mock(DBHelperDatabase.class);
        mockDb = mock(SQLiteDatabase.class);
        mockCursor = mock(Cursor.class);

        // Mặc định giả lập DB mở được, trả về cursor mock
        when(mockDbh.getWritableDatabase()).thenReturn(mockDb);
        when(mockDb.rawQuery(anyString(), any())).thenReturn(mockCursor);

        // Stub reminder logic để không thực thi thật
        doNothing().when(screen)
                .setTaskReminderLogic(any(Context.class), anyString(), anyString(), anyInt());

        injectDbHelper(screen, mockDbh);
    }

    // TC-1: title=null
    @Test
    public void addTask_TC1_titleNull_returnsFalse_noDbOpen() throws Exception {
        boolean result = (boolean) callAddTask(screen, null, "desc", "2026-01-01 08:00:00",
                "LOW", "TODO", 1, 10, screen);
        assertFalse(result);
        verify(mockDbh, never()).getWritableDatabase();
    }
    // TC-2: title=""
    @Test
    public void addTask_TC2_titleEmpty_returnsFalse_noDbOpen() throws Exception {
        boolean result = (boolean) callAddTask(screen, "", "desc", "2026-01-01 08:00:00",
                "LOW", "TODO", 1, 10, screen);
        assertFalse(result);
        verify(mockDbh, never()).getWritableDatabase();
    }
    // TC-3: moveToFirst() = false
    @Test
    public void addTask_TC3_moveToFirstFalse_returnsFalse_closeResources() throws Exception {
        when(mockCursor.moveToFirst()).thenReturn(false);
        boolean result = (boolean) callAddTask(screen, "Task A", "desc", "2026-01-01 08:00:00",
                "LOW", "TODO", 1, 10, screen);
        assertFalse(result);
        verify(screen, never()).setTaskReminderLogic(any(), anyString(), anyString(), anyInt());
        verify(mockCursor).close();
        verify(mockDb).close();
    }

    // TC-4: getLong(0) = 15
    @Test
    public void addTask_TC4_getLongPositive_returnsTrue_callsReminder() throws Exception {
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getLong(0)).thenReturn(15L);
        String title = "Task A";
        String due = "2026-01-01 08:00:00";
        boolean result = (boolean) callAddTask(screen, title, "desc", due,
                "HIGH", "TODO", 1, 10, screen);
        assertTrue(result);
        verify(screen).setTaskReminderLogic(any(Context.class), eq(title), eq(due), eq(15));
        verify(mockCursor).close();
        verify(mockDb).close();
    }
    // TC-5: getLong(0) = -1
    @Test
    public void addTask_TC5_getLongMinusOne_returnsFalse_closeResources() throws Exception {
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getLong(0)).thenReturn(-1L);
        boolean result = (boolean) callAddTask(screen, "Task A", "desc", "2026-01-01 08:00:00",
                "LOW", "TODO", 1, 10, screen);
        assertFalse(result);
        verify(mockCursor).close();
        verify(mockDb).close();
    }

    /* ===== Helpers ===== */
    private static void injectDbHelper(Object target, DBHelperDatabase helper) throws Exception {
        Field f = target.getClass().getDeclaredField("dbh");
        f.setAccessible(true);
        f.set(target, helper);
    }

    private static Object callAddTask(Object target,
                                      String title,
                                      String desc,
                                      String dueDate,
                                      String priority,
                                      String status,
                                      int categoryId,
                                      int userId,
                                      Context ctx) throws Exception {
        Method m = target.getClass().getDeclaredMethod("addTask",
                String.class, String.class, String.class,
                String.class, String.class,
                int.class, int.class, Context.class);
        m.setAccessible(true);
        return m.invoke(target, title, desc, dueDate, priority, status, categoryId, userId, ctx);
    }
}
