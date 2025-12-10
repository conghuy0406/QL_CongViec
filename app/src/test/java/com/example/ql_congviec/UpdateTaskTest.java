package com.example.ql_congviec;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;

import com.example.ql_congviec.Database.DBHelperDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.lang.reflect.Field;
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, manifest = Config.NONE)
public class UpdateTaskTest {

    private activity_task_detail spyAct;
    private DBHelperDatabase mockDbh;
    private SQLiteDatabase mockDb;
    private Cursor mockCursor;

    @Before
    public void setUp() {
        spyAct = spy(Robolectric.buildActivity(activity_task_detail.class)
                .create().start().resume().get());

        mockDbh = mock(DBHelperDatabase.class);
        mockDb = mock(SQLiteDatabase.class);
        mockCursor = mock(Cursor.class);

        // Gán DBHelper mock vào activity
        setPrivateField(spyAct, "dbh", mockDbh);

        // Chặn gọi reminder thật
        doNothing().when(spyAct).setTaskReminderLogic(any(), anyString(), anyString(), anyInt());
    }

    @After
    public void tearDown() {
        spyAct = null;
    }

    /* ==== TC-1 ==== */
    @Test
    public void TC1_nullTitle_returnsFalse_noDbAccess() {
        boolean result = spyAct.updateTask(10, null, "desc", "2025-08-15", "MEDIUM", "TODO", spyAct);
        assertFalse(result);
        verify(mockDbh, never()).getWritableDatabase();
        verify(mockDbh, never()).getReadableDatabase();
        verify(spyAct, never()).setTaskReminderLogic(any(), any(), any(), anyInt());
    }
    /* ==== TC-2 ==== */
    @Test
    public void TC2_emptyTitle_returnsFalse_noDbAccess() {
        boolean result = spyAct.updateTask(10, "", "desc", "2025-08-15", "MEDIUM", "TODO", spyAct);
        assertFalse(result);
        verify(mockDbh, never()).getWritableDatabase();
        verify(mockDbh, never()).getReadableDatabase();
        verify(spyAct, never()).setTaskReminderLogic(any(), any(), any(), anyInt());
    }
    /* ==== TC-3 ==== */
    @Test
    public void TC3_negativeId_returnsFalse_noDbAccess() {
        boolean result = spyAct.updateTask(-1, "Task A", "desc", "2025-08-15", "MEDIUM", "TODO", spyAct);
        assertFalse(result);
        verify(mockDbh, never()).getWritableDatabase();
        verify(mockDbh, never()).getReadableDatabase();
        verify(spyAct, never()).setTaskReminderLogic(any(), any(), any(), anyInt());
    }

    /* ==== TC-4 ==== */
    @Test
    public void TC4_notExistButUpdate_returnsTrue_callsReminder() {
        when(mockDbh.getWritableDatabase()).thenReturn(mockDb);
        when(mockDb.rawQuery(anyString(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(false);
        when(mockDb.update(anyString(), any(), any(), any())).thenReturn(1);

        boolean result = spyAct.updateTask(10, "Task A", "desc", "2025-08-15", "HIGH", "TODO", spyAct);

        assertTrue(result);
        verify(mockDb).update(anyString(), any(), any(), any());
        verify(spyAct).setTaskReminderLogic(any(), eq("Task A"), eq("2025-08-15"), eq(10));
        verify(mockCursor).close();
        verify(mockDb).close();
    }
    /* ==== TC-5 ==== */
    @Test
    public void TC5_existCountZero_returnsFalse_noUpdate_noReminder() {
        when(mockDbh.getWritableDatabase()).thenReturn(mockDb);
        when(mockDb.rawQuery(anyString(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getInt(0)).thenReturn(0);
        boolean result = spyAct.updateTask(10, "Task A", "desc", "2025-08-15", "LOW", "TODO", spyAct);
        assertFalse(result);
        verify(mockDb, never()).update(anyString(), any(), any(), any());
        verify(spyAct, never()).setTaskReminderLogic(any(), any(), any(), anyInt());
        verify(mockCursor).close();
        verify(mockDb).close();
    }
    /* ==== TC-6 ==== */
    @Test
    public void TC6_existCountOne_updateFail_returnsFalse_noReminder() {
        when(mockDbh.getWritableDatabase()).thenReturn(mockDb);
        when(mockDb.rawQuery(anyString(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getInt(0)).thenReturn(1); // phải >0 mới vào update
        when(mockDb.update(anyString(), any(), any(), any())).thenReturn(0); // update fail
        boolean result = spyAct.updateTask(10, "Task A", "desc", "2025-08-15", "LOW", "TODO", spyAct);
        assertFalse(result);
        verify(mockDb).update(anyString(), any(), any(), any());
        verify(spyAct, never()).setTaskReminderLogic(any(), any(), any(), anyInt());
        verify(mockCursor).close();
        verify(mockDb).close();
    }
    /* ==== TC-7 ==== */
    @Test
    public void TC7_existCountOne_updateOk_returnsTrue_callsReminder() {
        when(mockDbh.getWritableDatabase()).thenReturn(mockDb);
        when(mockDb.rawQuery(anyString(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getInt(0)).thenReturn(1);
        when(mockDb.update(anyString(), any(), any(), any())).thenReturn(1);
        boolean result = spyAct.updateTask(10, "Task A", "desc", "2025-08-15", "LOW", "TODO", spyAct);
        assertTrue(result);
        verify(mockDb).update(anyString(), any(), any(), any());
        verify(spyAct).setTaskReminderLogic(any(), eq("Task A"), eq("2025-08-15"), eq(10));
        verify(mockCursor).close();
        verify(mockDb).close();
    }

    /* ==== Helper ==== */
    private static void setPrivateField(Object target, String name, Object val) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, val);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
