package com.example.ql_congviec;

import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.GridLayout;

import androidx.test.core.app.ApplicationProvider;

import com.example.ql_congviec.Database.DBHelperDatabase;
import com.example.ql_congviec.model.Category;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, manifest = Config.NONE)
public class DeleteCategoryTest {

    private MainActivity activity;
    private DBHelperDatabase mockDbh;
    private SQLiteDatabase mockDb;
    private Cursor mockCursor;
    private GridLayout gridLayout;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.buildActivity(MainActivity.class)
                .create().start().resume().get();

        mockDbh = mock(DBHelperDatabase.class);
        mockDb = mock(SQLiteDatabase.class);
        mockCursor = mock(Cursor.class);

        // gridLayout giả có 1 view sẵn
        gridLayout = new GridLayout(ApplicationProvider.getApplicationContext());
        gridLayout.addView(new GridLayout(ApplicationProvider.getApplicationContext()));

        setPrivateField(activity, "dbh", mockDbh);
        setPrivateField(activity, "gridLayout", gridLayout);

        ShadowToast.reset();
    }

    /** TC-1: moveToFirst = false => Xóa thất bại, không execSQL, không refresh UI */
    @Test
    public void TC1_categoryNotFound_moveToFirstFalse() throws Exception {
        Category cat = mock(Category.class);
        when(cat.getId()).thenReturn(123);
        when(mockDbh.getWritableDatabase()).thenReturn(mockDb);
        when(mockDb.rawQuery(anyString(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(false);
        callDelete(activity, cat);
        assertEquals("Xóa thất bại", ShadowToast.getTextOfLatestToast());
        verify(mockDb, never()).execSQL(anyString(), any());
        assertEquals(1, gridLayout.getChildCount());
        verify(mockCursor).close();
        verify(mockDb).close();
    }
    @Test
    public void TC2_categoryCountZero_deleteFail() throws Exception {
        Category cat = mock(Category.class);
        when(cat.getId()).thenReturn(123);
        when(mockDbh.getWritableDatabase()).thenReturn(mockDb);
        when(mockDb.rawQuery(anyString(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getInt(0)).thenReturn(0);
        callDelete(activity, cat);
        assertEquals("Xóa thất bại", ShadowToast.getTextOfLatestToast());
        verify(mockDb, never()).execSQL(anyString(), any());
        assertEquals(1, gridLayout.getChildCount());
        verify(mockCursor).close();
        verify(mockDb).close();
    }

    @Test
    public void TC3_categoryExists_deleteSuccess() throws Exception {
        Category cat = mock(Category.class);
        when(cat.getId()).thenReturn(123);
        when(mockDbh.getWritableDatabase()).thenReturn(mockDb);
        when(mockDb.rawQuery(anyString(), any())).thenReturn(mockCursor);
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockCursor.getInt(0)).thenReturn(1);
        when(mockDbh.getAllCategories()).thenReturn(Collections.emptyList());
        callDelete(activity, cat);
        assertEquals("Đã xóa", ShadowToast.getTextOfLatestToast());
        verify(mockDb).execSQL(eq("DELETE FROM Category WHERE category_id = ?"), aryEq(new Object[]{123}));
        assertEquals(0, gridLayout.getChildCount());
        verify(mockDbh).getAllCategories();
        verify(mockCursor).close();
        verify(mockDb, atLeastOnce()).close();
    }
    /* ==== Helpers ==== */
    private static void callDelete(Object target, Object cat) throws Exception {
        Method m = target.getClass().getDeclaredMethod("deleteCategoryAndRefreshUI",
                com.example.ql_congviec.model.Category.class);
        m.setAccessible(true);
        m.invoke(target, cat);
    }

    private static void setPrivateField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
