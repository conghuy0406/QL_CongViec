package com.example.ql_congviec;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase; // Import SQLiteDatabase
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ql_congviec.Database.DBHelperDatabase;
import com.example.ql_congviec.model.Category;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    /* ────────── UI ────────── */
    private GridLayout gridLayout;
    private Button     btnAdd, btnCalendar, btnLogout;

    /* ────────── DB ────────── */
    private DBHelperDatabase dbh;

    /* ────────── Life-cycle ────────── */
    @Override protected void onCreate(Bundle savedInstanceState) {          // ①
        super.onCreate(savedInstanceState);
        dbh = new DBHelperDatabase(this);                                   // ②
        dbh.getWritableDatabase();

        setContentView(R.layout.activity_main);

        // Áp padding tránh system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),
                (v, insets) -> {
                    Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
                    return insets;
                });

        mapViews();                                                         // ③
        setClickListeners();                                                // ④
        loadCategoriesIntoGrid();                                           // ⑤
    }

    @Override protected void onResume() {                                   // ⑥
        super.onResume();
        gridLayout.removeAllViews();
        loadCategoriesIntoGrid();
    }

    /* ══════════════════════════════ PRIVATE METHODS ══════════════════════════════ */

    private void mapViews() {
        gridLayout   = findViewById(R.id.gridLayout);
        btnAdd       = findViewById(R.id.btn_add);
        btnCalendar  = findViewById(R.id.btn_calender);
        btnLogout    = findViewById(R.id.btn_logout);
    }

    private void setClickListeners() {
        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, addCategory.class)));

        btnCalendar.setOnClickListener(v ->
                startActivity(new Intent(this, TaskCalendarActivity.class)));

        btnLogout.setOnClickListener(v -> performLogout());
    }

    /* ────────── Load & render grid ────────── */
    private void loadCategoriesIntoGrid() {
        List<Category> categories = dbh.getAllCategories();
        int userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getInt("user_id", -1);
        if (userId == -1) return; // Chưa login

        for (Category cat : categories) {
            View card = createCardForCategory(cat, userId);
            gridLayout.addView(card);
        }
    }

    private View createCardForCategory(Category cat, int userId) {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.task_item, gridLayout, false);

        ImageView icon     = view.findViewById(R.id.task_icon);
        TextView  name     = view.findViewById(R.id.task_name);
        TextView  count    = view.findViewById(R.id.task_count);
        ImageView btnEdit  = view.findViewById(R.id.btn_edit);
        ImageView btnDel   = view.findViewById(R.id.btn_delete);

        icon.setImageResource(cat.getIconResId());
        name.setText(cat.getName());

        int taskCount = dbh.countTasksByCategory(cat.getId(), userId);
        count.setText(taskCount + (taskCount == 1 ? " Task" : " Tasks"));

        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width       = 0;
        lp.height      = GridLayout.LayoutParams.WRAP_CONTENT;
        lp.columnSpec  = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(16, 16, 16, 16);
        view.setLayoutParams(lp);

        btnEdit.setOnClickListener(v -> startEditCategory(cat));
        btnDel .setOnClickListener(v -> showDeleteDialog(cat)); // Sẽ gọi showDeleteDialog -> deleteCategoryAndRefreshUI
        view.setOnClickListener (v -> openTaskDetail(cat));

        return view;
    }

    /* ────────── Navigation helpers ────────── */
    private void startEditCategory(Category cat) {
        Intent i = new Intent(this, addCategory.class);
        i.putExtra("isEdit", true);
        i.putExtra("CATEGORY_ID",   cat.getId());
        i.putExtra("CATEGORY_NAME", cat.getName());
        i.putExtra("CATEGORY_ICON", cat.getIconResId());
        startActivity(i);
    }

    private void openTaskDetail(Category cat) {
        Intent i = new Intent(this, activity_task_detail.class);
        i.putExtra("CATEGORY_ID",   cat.getId());
        i.putExtra("CATEGORY_NAME", cat.getName());
        i.putExtra("CATEGORY_ICON", cat.getIconResId());
        startActivity(i);
    }

    /* ────────── Logout ────────── */
    private void performLogout() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent i = new Intent(this, Login.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    /* ────────── Delete category flow (NEW: Combined logic) ────────── */
    private void showDeleteDialog(Category cat) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        View dlg = getLayoutInflater().inflate(R.layout.dialog_custom_alert, null);
        b.setView(dlg);

        ((TextView) dlg.findViewById(R.id.tv_dialog_title)).setText("Xóa Category");
        ((TextView) dlg.findViewById(R.id.tv_dialog_message))
                .setText("Bạn có chắc muốn xóa \"" + cat.getName() + "\" không?");

        AlertDialog dialog = b.create();

        dlg.findViewById(R.id.btn_dialog_positive).setOnClickListener(v -> {
            // Gọi hàm gộp logic xóa và cập nhật UI
            deleteCategoryAndRefreshUI(cat);
            dialog.dismiss();
        });
        dlg.findViewById(R.id.btn_dialog_negative).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Gộp logic xóa category khỏi DB và cập nhật UI.
     * Hàm này thay thế cả dbh.deleteCategoryById và handleDeleteCategory.
     * @param cat Category cần xóa.
     */
    private void deleteCategoryAndRefreshUI(Category cat) {
        SQLiteDatabase db = dbh.getWritableDatabase();
        // Kiểm tra xem category có tồn tại không
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM Category WHERE category_id = ?",
                new String[]{String.valueOf(cat.getId())}
        );
        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        if (exists) {
            // Xóa nếu tồn tại
            String sql = "DELETE FROM Category WHERE category_id = ?";
            db.execSQL(sql, new Object[]{cat.getId()});
            Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show();
            gridLayout.removeAllViews();
            loadCategoriesIntoGrid();
        } else {
            // Không tồn tại => xóa thất bại
            Toast.makeText(this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }
}