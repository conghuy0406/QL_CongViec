package com.example.ql_congviec;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, manifest = Config.NONE)
public class InsertCategoryTest {

    private Object screen;
    private DBHelperDatabase mockDbh;
    private SQLiteDatabase mockDb;
    private Cursor mockCursor;

    @Before
    public void setUp() throws Exception {
        // Tìm class AddCategory hoặc addCategory
        Class<?> cls;
        try {
            cls = Class.forName("com.example.ql_congviec.AddCategory");
        } catch (ClassNotFoundException e) {
            cls = Class.forName("com.example.ql_congviec.addCategory");
        }
        screen = Robolectric.buildActivity((Class) cls).create().start().resume().get();

        // Giả lập (mock) các đối tượng cần thiết: DBHelper, Database và Cursor
        mockDbh = mock(DBHelperDatabase.class);
        mockDb = mock(SQLiteDatabase.class);
        mockCursor = mock(Cursor.class);

        // Stubbing: Thiết lập hành vi cho các đối tượng mock
        when(mockDbh.getWritableDatabase()).thenReturn(mockDb);
        when(mockDb.rawQuery(anyString(), any())).thenReturn(mockCursor);
        when(mockDb.rawQuery(anyString(), any(String[].class))).thenReturn(mockCursor);

        // Inject DBHelper mock vào activity instance
        injectDbHelper(screen, mockDbh);
    }

    // Phương thức tearDown() không cần thiết vì không có file DB thật để dọn dẹp

    // TC-1: name="Work", iconResId=101; moveToFirst()=false -> hàm trả về false; đã close() cursor & db
    @Test
    public void insert_TC1_moveToFirstFalse_returnsFalse() throws Exception {
        // Stub: Mock cursor sẽ trả về false khi moveToFirst() được gọi
        when(mockCursor.moveToFirst()).thenReturn(false);
        // Gọi phương thức cần kiểm thử
        boolean ok = (boolean) callInsert(screen, "Work", 101);
        assertFalse(ok);
        // Xác minh rằng cursor và db đã được đóng
        verify(mockCursor).close();
        verify(mockDb).close();
    }
    // TC-2: name="Work", iconResId=101; moveToFirst()=true, getLong(0)=0 -> hàm trả về false
    @Test
    public void insert_TC2_getLongZero_returnsFalse() throws Exception {
        // Stub: Mock cursor sẽ trả về true cho moveToFirst() và 0 cho getLong(0)
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getLong(0)).thenReturn(0L);
        // Gọi phương thức cần kiểm thử
        boolean ok = (boolean) callInsert(screen, "Work", 101);
        assertFalse(ok);
        // Xác minh rằng cursor và db đã được đóng
        verify(mockCursor).close();
        verify(mockDb).close();
    }
    // TC-3: name="Work", iconResId=101; moveToFirst()=true, getLong(0)=15 -> hàm trả về true
    @Test
    public void insert_TC3_getLongPositive_returnsTrue() throws Exception {
        // Stub: Mock cursor sẽ trả về true cho moveToFirst() và 15 cho getLong(0)
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getLong(0)).thenReturn(15L);

        // Gọi phương thức cần kiểm thử
        boolean ok = (boolean) callInsert(screen, "Work", 101);
        assertTrue(ok);

        // Xác minh rằng cursor và db đã được đóng
        verify(mockCursor).close();
        verify(mockDb).close();
    }
    // TC-4: name="Work", iconResId=101; moveToFirst()=true, getLong(0)=-1 -> hàm trả về false
    @Test
    public void insert_TC4_getLongMinusOne_returnsFalse() throws Exception {
        // Stub: Mock cursor sẽ trả về true cho moveToFirst() và -1 cho getLong(0)
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getLong(0)).thenReturn(-1L);
        // Gọi phương thức cần kiểm thử
        boolean ok = (boolean) callInsert(screen, "Work", 101);
        assertFalse(ok);
        // Xác minh rằng cursor và db đã được đóng
        verify(mockCursor).close();
        verify(mockDb).close();
    }

    /* ========= Helpers ========= */

    /**
     * Chèn một DBHelperDatabase mock vào một trường riêng tư của đối tượng mục tiêu.
     * @param target Đối tượng cần chèn trường vào.
     * @param helper Instance DBHelperDatabase đã mock.
     */
    private static void injectDbHelper(Object target, DBHelperDatabase helper) throws Exception {
        Field f;
        try {
            f = target.getClass().getDeclaredField("dbHelper");
            f.setAccessible(true);
            f.set(target, helper);
        } catch (NoSuchFieldException e) {
            f = target.getClass().getDeclaredField("dbh");
            f.setAccessible(true);
            f.set(target, helper);
        }
    }

    /**
     * Gọi một phương thức riêng tư tên là "insertCategory" trên đối tượng mục tiêu bằng reflection.
     * @param target Đối tượng để gọi phương thức.
     * @param name Tên danh mục.
     * @param icon Icon ID.
     * @return Kết quả của lời gọi phương thức.
     */
    private static Object callInsert(Object target, String name, int icon) throws Exception {
        Method m = target.getClass().getDeclaredMethod("insertCategory", String.class, int.class);
        m.setAccessible(true);
        return m.invoke(target, name, icon);
    }
}
