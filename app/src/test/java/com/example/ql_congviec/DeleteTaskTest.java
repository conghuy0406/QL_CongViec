package com.example.ql_congviec;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
public class DeleteTaskTest {

    private activity_task_detail act, spyAct;
    private DBHelperDatabase mockDbh;
    private SQLiteDatabase mockDb;
    private Cursor mockCursor;

    @Before
    public void setUp() throws Exception {
        act = Robolectric.buildActivity(activity_task_detail.class).create().start().resume().get();
        spyAct = spy(act);

        mockDbh = mock(DBHelperDatabase.class);
        mockDb = mock(SQLiteDatabase.class);
        mockCursor = mock(Cursor.class);

        when(mockDbh.getWritableDatabase()).thenReturn(mockDb);
        when(mockDbh.getReadableDatabase()).thenReturn(mockDb);
        setPrivateField(spyAct, "dbh", mockDbh);

        doNothing().when(spyAct).cancelTaskReminderLogic(any(), anyInt());
    }

    @After
    public void tearDown() {
        mockDb = null;
        mockCursor = null;
    }

    @Test // TC-1: taskId = -1 → false, không mở DB
    public void tc1_invalidId_returnsFalse_noDbOpen() {
        boolean ok = spyAct.deleteTask(-1);
        assertFalse(ok);
        verify(mockDbh, never()).getReadableDatabase();
        verify(spyAct, never()).cancelTaskReminderLogic(any(), anyInt());
    }
    @Test // TC-2: moveToFirst=false, delete=1 → true, có cancel
    public void tc2_moveToFirstFalse_delete1_returnsTrue_withCancel() {
        when(mockDb.rawQuery(anyString(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(false);
        when(mockDb.delete(anyString(), anyString(), any())).thenReturn(1);
        boolean ok = spyAct.deleteTask(10);
        assertTrue(ok);
        verify(mockDb).delete(anyString(), anyString(), any());
        verify(spyAct).cancelTaskReminderLogic(eq(spyAct), eq(10));
        verify(mockCursor).close();
        verify(mockDb).close();
    }
    @Test // TC-3: moveToFirst=false, delete=0 → false, không cancel
    public void tc3_moveToFirstFalse_delete0_returnsFalse_noCancel() {
        when(mockDb.rawQuery(anyString(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(false);
        when(mockDb.delete(anyString(), anyString(), any())).thenReturn(0);
        boolean ok = spyAct.deleteTask(10);
        assertFalse(ok);
        verify(mockDb).delete(anyString(), anyString(), any());
        verify(spyAct, never()).cancelTaskReminderLogic(any(), anyInt());
        verify(mockCursor).close();
        verify(mockDb).close();
    }

    @Test // TC-4: moveToFirst=true, getInt=0 → false, không delete
    public void tc4_moveToFirstTrue_count0_returnsFalse_noDelete_noCancel() {
        when(mockDb.rawQuery(anyString(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getInt(0)).thenReturn(0);
        boolean ok = spyAct.deleteTask(10);
        assertFalse(ok);
        verify(mockDb, never()).delete(anyString(), anyString(), any());
        verify(spyAct, never()).cancelTaskReminderLogic(any(), anyInt());
        verify(mockCursor).close();
        verify(mockDb).close();
    }
    @Test // TC-5: moveToFirst=true, count=1, delete=1 → true, có cancel
    public void tc5_moveToFirstTrue_count1_delete1_returnsTrue_withCancel() {
        when(mockDb.rawQuery(anyString(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getInt(0)).thenReturn(1);
        when(mockDb.delete(anyString(), anyString(), any())).thenReturn(1);
        boolean ok = spyAct.deleteTask(10);
        assertTrue(ok);
        verify(mockDb).delete(anyString(), anyString(), any());
        verify(spyAct).cancelTaskReminderLogic(eq(spyAct), eq(10));
        verify(mockCursor).close();
        verify(mockDb).close();
    }

    @Test // TC-6: moveToFirst=true, count=1, delete=0 → false, không cancel
    public void tc6_moveToFirstTrue_count1_delete0_returnsFalse_noCancel() {
        when(mockDb.rawQuery(anyString(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getInt(0)).thenReturn(1);
        when(mockDb.delete(anyString(), anyString(), any())).thenReturn(0);

        boolean ok = spyAct.deleteTask(10);

        assertFalse(ok);
        verify(mockDb).delete(anyString(), anyString(), any());
        verify(spyAct, never()).cancelTaskReminderLogic(any(), anyInt());
        verify(mockCursor).close();
        verify(mockDb).close();
    }

    /* ---------- Helpers ---------- */
    private static void setPrivateField(Object target, String name, Object val) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, val);
    }
}
