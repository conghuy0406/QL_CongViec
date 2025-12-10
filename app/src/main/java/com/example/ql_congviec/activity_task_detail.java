package com.example.ql_congviec;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context; // Import Context
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ql_congviec.Database.DBHelperDatabase;
import com.example.ql_congviec.model.UserTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Activity hiển thị, thêm, sửa, xoá Task – toàn bộ CRUD dùng SQL thuần. */
public class activity_task_detail extends AppCompatActivity {

    /* ────────── UI ────────── */
    private EditText    edtSearch;
    private ImageView   btnBack;
    private LinearLayout btnAdd;
    private TextView    txtNumTask;
    private ProgressBar progressBar;
    private LinearLayout activeTaskContainer, doneTaskContainer;

    /* ────────── DATA ────────── */
    private int categoryId;
    private DBHelperDatabase dbh; // Biến thành viên để sử dụng lại
    private int currentUserId; // Để lưu trữ User ID hiện tại

    /* ═════════════════════════════▬▬▬▬▬▬▬▬▬▬▬══ */
    /* LIFE-CYCLE              */
    /* ═══════════════════════════════════════════ */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_task_detail);
        applyEdgeToEdge();

        mapViews();
        receiveIntent();
        dbh = new DBHelperDatabase(this); // Khởi tạo dbh TẠI ĐÂY
        currentUserId = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("user_id", -1); // Lấy User ID

        loadTasks(""); // Tải task ban đầu
        setListeners();
        checkNotificationPermission();
    }

    /* ────────── Edge-to-Edge padding ────────── */
    private void applyEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });
    }

    /* ────────── View binding ────────── */
    private void mapViews() {
        edtSearch           = findViewById(R.id.edt_search_task);
        btnAdd              = findViewById(R.id.btn_add_task);
        btnBack             = findViewById(R.id.btnback);
        activeTaskContainer = findViewById(R.id.active_task_container);
        doneTaskContainer   = findViewById(R.id.completed_task_container);
        progressBar         = findViewById(R.id.progress_bar);
        txtNumTask          = findViewById(R.id.txt_num_task);
    }

    /* ────────── Nhận dữ liệu Intent ────────── */
    private void receiveIntent() {
        Intent i  = getIntent();
        categoryId = i.getIntExtra("CATEGORY_ID", -1);
        String name = i.getStringExtra("CATEGORY_NAME");
        int iconRes = i.getIntExtra("CATEGORY_ICON", R.drawable.ic_persona);

        ((TextView) findViewById(R.id.txt_category_name)).setText(name);
        ((ImageView) findViewById(R.id.img_category_icon)).setImageResource(iconRes);
    }

    /* ────────── Listeners ────────── */
    private void setListeners() {
        // Gọi dialog UI để người dùng nhập/chọn dữ liệu
        btnAdd.setOnClickListener(v -> showTaskDialogCommon(null));

        btnBack.setOnClickListener(v -> {
            Intent r = new Intent().putExtra("UPDATED_TASK_COUNT", getActiveTaskCount());
            setResult(RESULT_OK, r);
            finish();
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable e) { loadTasks(e.toString().trim()); }
        });
    }

    /* ════════════════════ LOAD & HIỂN THỊ TASK (VẪN GIỮ NGUYÊN) ════════════════════ */
    private void loadTasks(String query) {
        activeTaskContainer.removeAllViews();
        doneTaskContainer.removeAllViews();

        // Sử dụng currentUserId đã được lấy từ onCreate
        for (UserTask t : (query.isEmpty()
                ? dbh.getTasksByCategory(categoryId, currentUserId)
                : dbh.getTasksByTitleAndCategory(query, categoryId, currentUserId))) {
            addTaskView(t);
        }
        updateTaskCount();
    }

    private void addTaskView(UserTask t) {
        View v  = LayoutInflater.from(this).inflate(R.layout.item_task_full, null);
        CheckBox cb  = v.findViewById(R.id.checkbox);
        TextView tvT = v.findViewById(R.id.task_title);
        TextView tvD = v.findViewById(R.id.task_description);
        TextView tvI = v.findViewById(R.id.task_info);

        tvT.setText(t.getTitle());
        tvD.setText(t.getDescription()==null||t.getDescription().isEmpty()
                ? "No description" : t.getDescription());
        tvI.setText(String.format(Locale.getDefault(),
                "Due: %s • Priority: %s • Created: %s",
                t.getDueDate()==null ? "N/A" : t.getDueDate(),
                t.getPriority(), t.getCreatedAt()));

        cb.setChecked("DONE".equalsIgnoreCase(t.getStatus()));
        LinearLayout container = cb.isChecked()? doneTaskContainer : activeTaskContainer;
        container.addView(v);

        // Đặt Listener cho checkbox và item view
        setTaskListeners(cb, v, t.getTaskId());
    }

    private void setTaskListeners(CheckBox cb, View item, int taskId) {
        // Xử lý khi trạng thái checkbox thay đổi (Toggle Status)
        cb.setOnCheckedChangeListener((b, checked) -> {
            toggleTaskStatus(taskId, checked);
            loadTasks(edtSearch.getText().toString().trim());
        });

        // Xử lý khi nhấn giữ vào item (Show Options: Edit/Delete)
        View.OnLongClickListener longClk = vv -> {
            new AlertDialog.Builder(this)
                    .setTitle("Choose action")
                    .setItems(new String[]{"Edit", "Delete"}, (d, which) -> {
                        if (which == 0) { // Edit
                            UserTask t = dbh.getTaskById(taskId);
                            if (t != null) {
                                showTaskDialogCommon(t); // Vẫn gọi dialog để người dùng tương tác
                            } else {
                                Toast.makeText(this, "Task not found!", Toast.LENGTH_SHORT).show();
                            }
                        } else { // Delete
                            // Gọi hàm logic xóa task trực tiếp, sau đó cập nhật UI
                            if (deleteTask(taskId)) { // Gọi hàm xóa đã tinh gọn
                                Toast.makeText(this, "Task deleted!", Toast.LENGTH_SHORT).show();
                                loadTasks(edtSearch.getText().toString().trim());
                            } else {
                                Toast.makeText(this, "Failed to delete task.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).show();
            return true;
        };
        cb.setOnLongClickListener(longClk);
        item.setOnLongClickListener(longClk);
    }

    /* ═════════════════════ HÀM HIỂN THỊ DIALOG UI (VẪN CẦN CHO TƯƠNG TÁC NGƯỜI DÙNG) ═════════════════════ */

    /**
     * Phương thức chung để hiển thị dialog thêm/sửa task.
     * Sau khi người dùng xác nhận, nó sẽ gọi các hàm logic CRUD tương ứng.
     * @param task Đối tượng UserTask để sửa (null nếu là thêm mới).
     */
    private void showTaskDialogCommon(UserTask task) {
        View dv = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        EditText edtT = dv.findViewById(R.id.edit_task_title);
        EditText edtD = dv.findViewById(R.id.edit_task_description);
        EditText edtDue = dv.findViewById(R.id.edit_task_due_date);
        Spinner spP = dv.findViewById(R.id.spinner_priority);
        Spinner spS = dv.findViewById(R.id.spinner_status);

        edtDue.setFocusable(false);
        edtDue.setOnClickListener(v -> showDateTimePicker(edtDue));

        String[] ps = {"LOW","MEDIUM","HIGH"};
        String[] sts= {"TODO","IN_PROGRESS","DONE"};
        spP.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ps));
        spS.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sts));

        boolean isEdit = task != null;
        if (isEdit){
            edtT.setText(task.getTitle());
            edtD.setText(task.getDescription());
            edtDue.setText(task.getDueDate());
            spP.setSelection(java.util.Arrays.asList(ps).indexOf(task.getPriority()));
            spS.setSelection(java.util.Arrays.asList(sts).indexOf(task.getStatus()));
        }

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Edit Task" : "New Task")
                .setView(dv)
                .setPositiveButton(isEdit ? "Save" : "Add", (dg, w)->{
                    // Lấy dữ liệu từ UI
                    String tTitle = edtT.getText().toString().trim();
                    if (tTitle.isEmpty()){
                        Toast.makeText(this,"Title cannot be empty",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String tDesc = edtD.getText().toString().trim();
                    String tDue  = edtDue.getText().toString().trim();
                    String pri   = spP.getSelectedItem().toString();
                    String sta   = spS.getSelectedItem().toString();

                    // Gọi các hàm logic CRUD đã tinh gọn
                    boolean opSuccess;
                    if (isEdit){
                        opSuccess = updateTask(task.getTaskId(), tTitle, tDesc, tDue, pri, sta, this); // Truyền Context
                    } else {
                        opSuccess = addTask(tTitle, tDesc, tDue, pri, sta, categoryId, currentUserId, this); // Truyền Context
                    }

                    if (opSuccess) {
                        Toast.makeText(this, isEdit ? "Task updated!" : "Task added!", Toast.LENGTH_SHORT).show();
                        loadTasks(edtSearch.getText().toString().trim()); // Cập nhật UI sau thao tác
                    } else {
                        Toast.makeText(this, isEdit ? "Failed to update task." : "Failed to add task.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    /* ══════════════════════════════════════════════════════════ */
    /* ────────────────── CÁC HÀM CHÍNH CHO UNIT TEST (LOGIC THUẦN) ───────────────── */
    /* ══════════════════════════════════════════════════════════ */
    public boolean addTask(String title, String desc, String due, String pri, String sta,
                           int categoryId, int userId, Context context) {
        if (title == null || title.isEmpty()) {
            return false; // Title không được rỗng
        }
        SQLiteDatabase db = dbh.getWritableDatabase(); // Mở kết nối DB
        Cursor cursor = null;
        long newTaskId = -1;
        // Thực hiện INSERT
        db.execSQL(
                "INSERT INTO UserTask (title,description,due_date,priority,status,category_id,user_id)" +
                        " VALUES (?,?,?,?,?,?,?)",
                new Object[]{ title, desc,
                        due.isEmpty() ? null : due, // Xử lý ngày hết hạn rỗng
                        pri, sta, categoryId, userId }
        );
        // Lấy ID của hàng vừa được chèn
        cursor = db.rawQuery("SELECT last_insert_rowid()", null);
        if (cursor.moveToFirst()) {
            newTaskId = cursor.getLong(0);
        }
        cursor.close();
        db.close();
        // Xử lý kết quả và thiết lập nhắc nhở
        if (newTaskId != -1) {
            setTaskReminderLogic(context, title, due, (int) newTaskId);
            return true;
        }
        return false;
    }

    /**
     * HÀM 2: CẬP NHẬT TASK (Logic thuần túy, không try-catch).
     */
    public boolean updateTask(int taskId, String title, String desc, String due, String pri, String sta, Context context) {

        if (title == null || title.isEmpty() || taskId == -1) {
            return false;
        }
        SQLiteDatabase db = dbh.getWritableDatabase();
        Cursor cursor = null;
        int rowsAffected = 0;
        cursor = db.rawQuery("SELECT COUNT(*) FROM UserTask WHERE task_id = ?", new String[]{String.valueOf(taskId)});
        if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
            cursor.close();
            db.close();
            return false;
        }
        cursor.close();
        rowsAffected = db.update("UserTask",
                DBHelperDatabase.getContentValues(title, desc, due, pri, sta, -1, -1),
                "task_id=?", new String[]{String.valueOf(taskId)});
        db.close();
        if (rowsAffected > 0) {
            setTaskReminderLogic(context, title, due, taskId);
            return true;
        }
        return false;
    }

    /**
     * HÀM 3: XÓA TASK (Logic thuần túy, không try-catch).
     */
    public boolean deleteTask(int taskId) {
        if (taskId == -1) {
            return false; // ID không hợp lệ
        }
        SQLiteDatabase db = dbh.getWritableDatabase();
        Cursor cursor = null;
        int rowsAffected = 0;
        cursor = db.rawQuery("SELECT COUNT(*) FROM UserTask WHERE task_id = ?", new String[]{String.valueOf(taskId)});

        if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
            cursor.close();
            db.close();
            return false;
        }
        cursor.close();
        rowsAffected = db.delete("UserTask", "task_id=?", new String[]{String.valueOf(taskId)});
        db.close();
        if (rowsAffected > 0) {
            cancelTaskReminderLogic(this, taskId);
            return true;
        }
        return false;
    }


    /* ════════════════════════ REMINDER LOGIC (Tách khỏi UI) ════════════════════════ */

    /**
     * Logic để thiết lập báo thức. Được tách ra để dễ kiểm thử.
     * @param context Context (cần cho getSystemService và Intent).
     * @param title Tiêu đề task.
     * @param due Ngày hết hạn.
     * @param id ID task.
     */
    @SuppressLint("ScheduleExactAlarm")
    public void setTaskReminderLogic(Context context, String title, String due, int id){
        if (due == null || due.isEmpty()) {
            cancelTaskReminderLogic(context, id);
            return;
        }
        try{
            Date dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(due);
            if (dt == null || !dt.after(new Date())) {
                cancelTaskReminderLogic(context, id);
                return;
            }

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            // Quyền SCHEDULE_EXACT_ALARM: Không kiểm tra ở đây để unit test dễ hơn.
            // Việc kiểm tra và yêu cầu quyền sẽ thuộc về lớp Activity.

            Intent i = new Intent(context, TaskReminderReceiver.class)
                    .putExtra("TASK_TITLE", title)
                    .putExtra("TASK_ID", id);
            PendingIntent pi = PendingIntent.getBroadcast(context, id, i,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            am.setExact(AlarmManager.RTC_WAKEUP, dt.getTime(), pi);
        }catch(ParseException e){
            // Trong unit test, bạn có thể kiểm tra xem exception có bị ném ra không.
            // Trong production, Toast sẽ xử lý điều này.
            cancelTaskReminderLogic(context, id);
        }
    }

    /**
     * Logic để hủy báo thức. Được tách ra để dễ kiểm thử.
     * @param context Context.
     * @param id ID task.
     */
    public void cancelTaskReminderLogic(Context context, int id) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, TaskReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, id, i, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pi != null) {
            am.cancel(pi);
            pi.cancel();
        }
    }


    /* ════════════════════════ INTERNAL SQL HELPER (VẪN CÓ THỂ GIỮ PRIVATE) ════════════════════════ */
    // Các hàm này vẫn là private vì chúng chỉ là lớp giao tiếp trực tiếp với SQLite,
    // và các hàm `addTask`, `updateTask`, `deleteTask` đã là lớp trừu tượng tốt rồi.

    /** SQL INSERT Task */
    private long insertTaskIntoDatabase(String title, String desc, String due, String pri, String sta){
        // Lưu ý: dbh đã được khởi tạo trong onCreate của Activity.
        // Để unit test độc lập hơn, bạn có thể truyền dbh vào các hàm public CRUD.
        SQLiteDatabase db = dbh.getWritableDatabase(); // Sử dụng dbh của Activity
        long id = -1;
        try {
            db.execSQL(
                    "INSERT INTO UserTask (title,description,due_date,priority,status,category_id,user_id)" +
                            " VALUES (?,?,?,?,?,?,?)",
                    new Object[]{ title, desc,
                            due.isEmpty() ? null : due,
                            pri, sta, categoryId,
                            currentUserId } // Sử dụng currentUserId
            );
            Cursor c = db.rawQuery("SELECT last_insert_rowid()", null);
            if (c.moveToFirst()) {
                id = c.getLong(0);
            }
            c.close();
        } finally {
            db.close();
        }
        return id;
    }

    /** SQL UPDATE Task */
    private void updateTaskInDatabase(int id, String title, String desc, String due, String pri, String sta){
        SQLiteDatabase db = dbh.getWritableDatabase();
        try {
            db.execSQL(
                    "UPDATE UserTask SET title=?,description=?,due_date=?,priority=?,status=? WHERE task_id=?",
                    new Object[]{ title, desc, due.isEmpty() ? null : due, pri, sta, id });
        } finally {
            db.close();
        }
    }

    /** SQL DELETE Task */
    private void deleteTaskFromDatabase(int id){
        SQLiteDatabase db = dbh.getWritableDatabase();
        try {
            db.execSQL("DELETE FROM UserTask WHERE task_id=?", new Object[]{ id });
            // LƯU Ý: Không gọi cancelTaskReminder ở đây nữa, mà gọi ở hàm `deleteTask` bên ngoài.
        } finally {
            db.close();
        }
    }

    /** SQL UPDATE Task Status (Toggle) */
    private void toggleTaskStatus(int id, boolean done){
        SQLiteDatabase db = dbh.getWritableDatabase();
        try {
            db.execSQL("UPDATE UserTask SET status=? WHERE task_id=?",
                    new Object[]{ done ? "DONE" : "TODO", id });
        } finally {
            db.close();
        }
    }

    /* ════════════════════════ Picker & Helper (Không thay đổi) ════════════════════════ */
    private void showDateTimePicker(EditText target){
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this,(d,y,m,day)->{
            cal.set(y,m,day);
            new TimePickerDialog(this,(t,h,min)->{
                cal.set(Calendar.HOUR_OF_DAY,h);
                cal.set(Calendar.MINUTE,min);
                cal.set(Calendar.SECOND,0);
                target.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault())
                        .format(cal.getTime()));
            },cal.get(Calendar.HOUR_OF_DAY),cal.get(Calendar.MINUTE),true).show();
        },cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateTaskCount(){
        int act = activeTaskContainer.getChildCount();
        int done= doneTaskContainer.getChildCount();
        int tot = act+done;
        txtNumTask.setText(act+" Tasks");
        progressBar.setProgress(tot==0?0:(int)(done*100f/tot));
    }
    private int getActiveTaskCount(){ return activeTaskContainer.getChildCount(); }

    @Override public void onBackPressed(){
        Intent r=new Intent().putExtra("UPDATED_TASK_COUNT",getActiveTaskCount());
        setResult(RESULT_OK,r); super.onBackPressed();
    }

    /* ════════════════════════ Permission (Không thay đổi) ════════════════════════ */
    private void checkNotificationPermission(){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this,android.Manifest.permission.POST_NOTIFICATIONS)
                        !=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},101);
        }
    }
    @Override public void onRequestPermissionsResult(int req,String[] p,int[] g){
        if (req==101){
            Toast.makeText(this,
                    (g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)
                            ? "Đã được cấp quyền thông báo."
                            : "Không thể gửi thông báo nếu không có quyền.",
                    Toast.LENGTH_LONG).show();
        }
        super.onRequestPermissionsResult(req,p,g);
    }
}