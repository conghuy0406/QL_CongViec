package com.example.ql_congviec;

import static org.junit.Assert.*;
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
import java.lang.reflect.Method;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, manifest = Config.NONE)
public class updateCategory {

    private Object screen;                 // addCategory / AddCategory activity thật
    private DBHelperDatabase mockDb;
    private String dbPath;                 // đường dẫn DB file tạm

    @Before
    public void setUp() throws Exception {
        // Tìm đúng Activity (tùy bạn đặt addCategory hay AddCategory)
        Class<?> cls;
        try { cls = Class.forName("com.example.ql_congviec.addCategory"); }
        catch (ClassNotFoundException e) { cls = Class.forName("com.example.ql_congviec.AddCategory"); }
        screen = Robolectric.buildActivity((Class) cls).create().start().resume().get();

        // 1) Tạo DB file + schema giống DB thật
        File dbFile = ApplicationProvider.getApplicationContext().getDatabasePath("unit_update_category.db");
        if (!dbFile.getParentFile().exists()) dbFile.getParentFile().mkdirs();
        dbPath = dbFile.getPath();

        SQLiteDatabase init = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
        createCategorySchema(init);
        init.close();

        // 2) Mock DBHelper → luôn mở kết nối mới vào CÙNG file
        mockDb = mock(DBHelperDatabase.class);
        when(mockDb.getWritableDatabase()).thenAnswer(i ->
                SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE));
        when(mockDb.getReadableDatabase()).thenAnswer(i ->
                SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE));

        injectDbHelper(screen, mockDb);
    }

    @After
    public void tearDown() {
        try { new File(dbPath).delete(); } catch (Exception ignored) {}
    }

    @Test
    public void tc1_invalidId_returnsFalse_noQueryOrUpdate() throws Exception {
        boolean ok = (boolean) callUpdate(screen, -1, "Work", 101);
        assertFalse(ok);
        verify(mockDb, atLeastOnce()).getWritableDatabase();
        verifyNoMoreInteractions(mockDb); // không query/UPDATE
    }
    @Test
    public void tc2_moveToFirstFalse_performsUpdate() throws Exception {
        SQLiteDatabase mockSql = mock(SQLiteDatabase.class);
        Cursor mockCursor = mock(Cursor.class);
        when(mockDb.getWritableDatabase()).thenReturn(mockSql);
        when(mockSql.rawQuery(anyString(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(false);

        boolean ok = (boolean) callUpdate(screen, 10, "Work", 101);

        assertTrue(ok);
        verify(mockSql).execSQL(startsWith("UPDATE Category"), any());
        verify(mockCursor).close();
        verify(mockSql).close();
    }

    @Test
    public void tc3_moveToFirstTrue_countZero_returnsFalse() throws Exception {
        SQLiteDatabase mockSql = mock(SQLiteDatabase.class);
        Cursor mockCursor = mock(Cursor.class);
        when(mockDb.getWritableDatabase()).thenReturn(mockSql);
        when(mockSql.rawQuery(anyString(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getInt(0)).thenReturn(0);
        boolean ok = (boolean) callUpdate(screen, 10, "Work", 101);
        assertFalse(ok);
        verify(mockCursor).close();
        verify(mockSql).close();
        verify(mockSql, never()).execSQL(startsWith("UPDATE Category"), any());
    }
    @Test
    public void tc4_moveToFirstTrue_countOne_performsUpdate() throws Exception {
        SQLiteDatabase mockSql = mock(SQLiteDatabase.class);
        Cursor mockCursor = mock(Cursor.class);
        when(mockDb.getWritableDatabase()).thenReturn(mockSql);
        when(mockSql.rawQuery(anyString(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getInt(0)).thenReturn(1);
        boolean ok = (boolean) callUpdate(screen, 10, "Work", 101);
        assertTrue(ok);
        verify(mockSql).execSQL(eq("UPDATE Category SET name = ?, icon = ? WHERE category_id = ?"), any());
        verify(mockCursor).close();
        verify(mockSql).close();
    }


    /* ========= Helpers ========= */

    // Schema khớp DBHelperDatabase.onCreate() (KHÔNG có UNIQUE(name))
    private static void createCategorySchema(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=OFF");
        db.execSQL("CREATE TABLE IF NOT EXISTS Category (" +
                "category_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "description TEXT," +
                "icon INTEGER," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
    }

    private static void injectDbHelper(Object target, DBHelperDatabase helper) throws Exception {
        try {
            Field f = target.getClass().getDeclaredField("dbHelper");
            f.setAccessible(true); f.set(target, helper);
        } catch (NoSuchFieldException e) {
            Field f = target.getClass().getDeclaredField("dbh");
            f.setAccessible(true); f.set(target, helper);
        }
    }

    private static Object callUpdate(Object target, int id, String name, int icon) throws Exception {
        Method m = target.getClass().getDeclaredMethod("updateCategory", int.class, String.class, int.class);
        m.setAccessible(true);
        return m.invoke(target, id, name, icon);
    }
}
