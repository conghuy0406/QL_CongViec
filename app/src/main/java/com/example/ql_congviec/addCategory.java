package com.example.ql_congviec;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ql_congviec.Database.DBHelperDatabase;

public class addCategory extends AppCompatActivity {

    /* ────────── UI ────────── */
    private EditText edtName;
    private Spinner  spinnerIcon;
    private Button   btnSave;

    /* ────────── State ────────── */
    private boolean isEditMode = false;
    private int     categoryId = -1;
    private int     selectedIconIndex = 0;

    /* ────────── Data ────────── */
    private final int[] iconResIds = {
            R.drawable.ic_persona,
            R.drawable.ic_task,
            R.drawable.ic_car,
            R.drawable.ic_shopping
    };

    DBHelperDatabase dbHelper;

    /* ────────── Life-cycle ────────── */
    @SuppressLint("MissingInflatedId")
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        dbHelper     = new DBHelperDatabase(this);
        edtName      = findViewById(R.id.edt_task_name);
        spinnerIcon  = findViewById(R.id.spinner_icon);
        btnSave      = findViewById(R.id.btn_add_taskk);

        detectEditMode();
        setupSpinner();

        btnSave.setOnClickListener(v -> onSaveClicked());
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
    }

    /* ══════════════════════════════ PRIVATE METHODS ══════════════════════════════ */

    /** Kiểm tra Intent xem đang Sửa hay Thêm. */
    private void detectEditMode() {
        if (getIntent().hasExtra("CATEGORY_ID")) {
            isEditMode = true;
            categoryId = getIntent().getIntExtra("CATEGORY_ID", -1);
            String oldName = getIntent().getStringExtra("CATEGORY_NAME");
            int    oldIcon = getIntent().getIntExtra("CATEGORY_ICON", R.drawable.ic_persona);

            edtName.setText(oldName);
            for (int i = 0; i < iconResIds.length; i++) {
                if (iconResIds[i] == oldIcon) {
                    selectedIconIndex = i;
                    break;
                }
            }
            btnSave.setText("Cập nhật");
        }
    }

    /** Khởi tạo spinner icon. */
    private void setupSpinner() {
        IconSpinnerAdapter adapter = new IconSpinnerAdapter(this, iconResIds);
        spinnerIcon.setAdapter(adapter);
        spinnerIcon.setSelection(selectedIconIndex);

        spinnerIcon.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                selectedIconIndex = pos;
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedIconIndex = 0;
            }
        });
    }

    /** Xử lý nhấn nút Lưu. */
    private void onSaveClicked() {
        String name = edtName.getText().toString().trim();

        // ⚠️ BẮT BUỘC KIỂM TRA TÊN TẠI ĐÂY ⚠️
        if (name.isEmpty()) {
            Toast.makeText(this, "Tên category không được để trống.", Toast.LENGTH_SHORT).show();
            return; // Dừng ngay nếu tên trống
        }

        int icon = iconResIds[selectedIconIndex];
        boolean ok;

        if (isEditMode) {
            ok = updateCategory(categoryId, name, icon);
        } else {
            ok = insertCategory(name, icon);
        }

        Toast.makeText(this, ok ? "Thao tác thành công" : "Thao tác thất bại", Toast.LENGTH_SHORT).show();
        if (ok) finish();
    }

    /* ══════════════════════════════ LOGIC CƠ SỞ DỮ LIỆU ĐƯỢC TÁCH RIÊNG ══════════════════════════════ */

    /**
     * Thêm mới một Category vào cơ sở dữ liệu.
     * @param name Tên category.
     * @param iconResId Resource ID của icon.
     * @return true nếu thêm thành công, false nếu thất bại.
     */
    public boolean insertCategory(@NonNull String name, int iconResId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(
                "INSERT INTO Category (name, description, icon) VALUES (?, ?, ?)",
                new Object[]{ name, "", iconResId }
        );
        Cursor cursor = db.rawQuery("SELECT last_insert_rowid()", null);
        long newId = -1;
        if (cursor.moveToFirst()) {
            newId = cursor.getLong(0);
        }
        cursor.close();
        db.close();

        if (newId != -1 && newId != 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Cập nhật một Category hiện có trong cơ sở dữ liệu.
     * @param categoryId ID của category cần cập nhật.
     * @param name Tên category mới.
     * @param iconResId Resource ID của icon mới.
     * @return true nếu cập nhật thành công, false nếu thất bại.
     */
    public boolean updateCategory(int categoryId, @NonNull String name, int iconResId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // 1. Kiểm tra ID không hợp lệ
        if (categoryId == -1) {
            db.close();
            return false;
        }
        // 2. Kiểm tra sự tồn tại của category
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM Category WHERE category_id = ?", new String[]{String.valueOf(categoryId)});
        if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
            // Category không tồn tại
            cursor.close();
            db.close();
            return false;
        }
        cursor.close();
        db.execSQL(
                "UPDATE Category SET name = ?, icon = ? WHERE category_id = ?",
                new Object[]{ name, iconResId, categoryId }
        );
        db.close();
        return true;
    }
}