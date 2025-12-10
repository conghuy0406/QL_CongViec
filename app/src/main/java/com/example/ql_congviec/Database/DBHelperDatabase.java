package com.example.ql_congviec.Database;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.ql_congviec.model.Category;
import com.example.ql_congviec.model.UserTask;

import java.util.ArrayList;
import java.util.List;

public class DBHelperDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "QL_CongViec.db";
    private static final int DATABASE_VERSION = 1;
    public DBHelperDatabase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    //
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng User
        String createUserTable = "CREATE TABLE User (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password TEXT NOT NULL, " +
                "full_name TEXT NOT NULL, " +
                "email TEXT NOT NULL UNIQUE, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";
        db.execSQL(createUserTable);

        // Tạo bảng Category
        String createCategoryTable = "CREATE TABLE Category (" +
                "category_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "icon INTEGER, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";
        db.execSQL(createCategoryTable);

        // Tạo bảng UserTask
        String createUserTaskTable = "CREATE TABLE UserTask (" +
                "task_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "due_date DATETIME, " +
                "status TEXT DEFAULT 'TODO', " +
                "priority TEXT DEFAULT 'MEDIUM', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "category_id INTEGER, " +
                "user_id INTEGER, " +
                "FOREIGN KEY (category_id) REFERENCES Category(category_id) ON DELETE SET NULL, " +
                "FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE" +
                ");";
        db.execSQL(createUserTaskTable);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS User ");
        db.execSQL("DROP TABLE IF EXISTS Category ");
        db.execSQL("DROP TABLE IF EXISTS UserTask ");
        //Tiến hành tạo bảng mới
        onCreate(db);
    }
    //Select
    public SQLiteDatabase kietNoiDBRead(){
        SQLiteDatabase db = getReadableDatabase();
        return db;
    }
    //insert, update, delete
    public SQLiteDatabase kietNoiDBWrite(){
        SQLiteDatabase db = getWritableDatabase();
        return db;
    }
    // Trong DBHelperDatabase.java

    public List<UserTask> getTasksByCategory(int categoryId, int userId) {
        List<UserTask> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT task_id, title, description, due_date, status, priority, created_at, user_id FROM UserTask " +
                        "WHERE category_id = ? AND user_id = ?",
                new String[]{ String.valueOf(categoryId), String.valueOf(userId) }
        );

        if (cursor.moveToFirst()) {
            do {
                int taskId = cursor.getInt(0);
                String title = cursor.getString(1);
                String description = cursor.getString(2);
                String dueDate = cursor.getString(3);
                String status = cursor.getString(4);
                String priority = cursor.getString(5);
                String createdAt = cursor.getString(6);
                int userIdFromDB = cursor.getInt(7);

                UserTask task = new UserTask(taskId, title, description, dueDate, status, priority, createdAt, categoryId, userIdFromDB);
                taskList.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return taskList;
    }

    public boolean updateCategory(int categoryId, String newName, int newIconResId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            String sql = "UPDATE Category SET name = ?, icon = ? WHERE category_id = ?";
            db.execSQL(sql, new Object[]{newName, newIconResId, categoryId});
            db.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            db.close();
            return false;
        }
    }


    public boolean insertUserRaw(String username, String password, String fullName, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            String sql = "INSERT INTO User (username, password, full_name, email) " +
                    "VALUES (?, ?, ?, ?)";
            db.execSQL(sql, new Object[]{username, password, fullName, email});
            return true;
        } catch (android.database.SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close(); // Đảm bảo luôn đóng DB
        }
    }



    public boolean checkUserRaw(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;

        try {
            String sql = "SELECT user_id FROM User WHERE username = ? AND password = ?";
            cursor = db.rawQuery(sql, new String[]{username, password});
            exists = cursor.moveToFirst();
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return exists;
    }

    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int userId = -1;

        try {
            String sql = "SELECT user_id FROM User WHERE username = ?";
            cursor = db.rawQuery(sql, new String[]{username});
            if (cursor.moveToFirst()) {
                userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
            }
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return userId;
    }



    public List<UserTask> getTasksByDate(String dueDateOnly, int userId) {
        List<UserTask> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT u.task_id, u.title, u.description, u.due_date, u.status, u.priority, u.created_at, " +
                "u.category_id, u.user_id, c.name AS category_name " +
                "FROM UserTask u LEFT JOIN Category c ON u.category_id = c.category_id " +
                "WHERE strftime('%Y-%m-%d', u.due_date) = ? AND u.user_id = ?";

        Cursor cursor = db.rawQuery(sql, new String[]{dueDateOnly, String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                int taskId = cursor.getInt(0);
                String title = cursor.getString(1);
                String description = cursor.getString(2);
                String dueDate = cursor.getString(3);
                String status = cursor.getString(4);
                String priority = cursor.getString(5);
                String createdAt = cursor.getString(6);
                int categoryId = cursor.getInt(7);
                int userIdFromDB = cursor.getInt(8);
                String categoryName = cursor.getString(9);

                UserTask task = new UserTask(taskId, title, description, dueDate, status, priority, createdAt, categoryId, userIdFromDB);
                task.setCategoryName(categoryName);
                taskList.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return taskList;
    }



    @SuppressLint("Range")
    public UserTask getTaskById(int taskId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM UserTask WHERE task_id = ?", new String[]{String.valueOf(taskId)});

        UserTask task = null;
        if (cursor.moveToFirst()) {
            task = new UserTask(
                    cursor.getInt(cursor.getColumnIndex("task_id")),
                    cursor.getString(cursor.getColumnIndex("title")),
                    cursor.getString(cursor.getColumnIndex("description")),
                    cursor.getString(cursor.getColumnIndex("due_date")),
                    cursor.getString(cursor.getColumnIndex("status")),
                    cursor.getString(cursor.getColumnIndex("priority")),
                    cursor.getString(cursor.getColumnIndex("created_at")),
                    cursor.getInt(cursor.getColumnIndex("category_id")),
                    cursor.getInt(cursor.getColumnIndex("user_id"))
            );
        }

        cursor.close();
        db.close();
        return task;
    }
    // Lấy danh sách tất cả Category
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase db = this.kietNoiDBRead();
        Cursor cursor = db.rawQuery("SELECT * FROM Category", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("category_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                int icon = cursor.getInt(cursor.getColumnIndexOrThrow("icon"));
                categories.add(new Category(id, name, desc, icon));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return categories;
    }

    public List<UserTask> getTasksByTitleAndCategory(String title, int categoryId, int userId) {
        List<UserTask> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM UserTask WHERE title LIKE ? AND category_id = ? AND user_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{"%" + title + "%", String.valueOf(categoryId), String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                UserTask task = new UserTask(
                        cursor.getInt(cursor.getColumnIndexOrThrow("task_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getString(cursor.getColumnIndexOrThrow("due_date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status")),
                        cursor.getString(cursor.getColumnIndexOrThrow("priority")),
                        cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("category_id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("user_id"))
                );
                tasks.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return tasks;
    }
    // Đếm số task theo category và user
    public int countTasksByCategory(int categoryId, int userId) {
        int count = 0;
        SQLiteDatabase db = this.kietNoiDBRead();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM UserTask WHERE category_id = ? AND user_id = ?",
                new String[]{ String.valueOf(categoryId), String.valueOf(userId) }
        );

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return count;
    }
    // Xoá category theo ID
    public boolean deleteCategoryById(int categoryId) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            String sql = "DELETE FROM Category WHERE category_id = ?";
            db.execSQL(sql, new Object[]{categoryId});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (db != null) db.close();
        }
    }

    public long insertSimpleTask(String title, int categoryId, int userId) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            String sql = "INSERT INTO UserTask (title, status, priority, category_id, user_id) " +
                    "VALUES (?, 'TODO', 'MEDIUM', ?, ?)";
            db.execSQL(sql, new Object[]{title, categoryId, userId});
            return 1; // giả định thành công, vì execSQL không trả về rowId
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            if (db != null) db.close();
        }
    }

    public long insertFullUserTask(String title, String description, String dueDate, String priority, String status, int categoryId, int userId) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            String sql = "INSERT INTO UserTask (title, description, due_date, priority, status, category_id, user_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            db.execSQL(sql, new Object[]{title, description, dueDate, priority, status, categoryId, userId});
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            if (db != null) db.close();
        }
    }

    public void updateTaskStatus(int taskId, String newStatus) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            String sql = "UPDATE UserTask SET status = ? WHERE task_id = ?";
            db.execSQL(sql, new Object[]{newStatus, taskId});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) db.close();
        }
    }

    public void updateTaskTitle(int taskId, String newTitle) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            String sql = "UPDATE UserTask SET title = ? WHERE task_id = ?";
            db.execSQL(sql, new Object[]{newTitle, taskId});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) db.close();
        }
    }

    public void updateFullTask(int taskId, String title, String description, String dueDate, String priority, String status) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            String sql = "UPDATE UserTask SET title = ?, description = ?, due_date = ?, priority = ?, status = ? WHERE task_id = ?";
            db.execSQL(sql, new Object[]{title, description, dueDate, priority, status, taskId});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) db.close();
        }
    }

    public void deleteTaskById(int taskId) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            String sql = "DELETE FROM UserTask WHERE task_id = ?";
            db.execSQL(sql, new Object[]{taskId});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) db.close();
        }
    }
    public static ContentValues getContentValues(String title, String description, String dueDate,
                                                 String priority, String status, int categoryId, int userId) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("description", description);
        values.put("due_date", dueDate.isEmpty() ? null : dueDate);
        values.put("priority", priority);
        values.put("status", status);
        if (categoryId != -1) { // Chỉ thêm nếu có giá trị hợp lệ
            values.put("category_id", categoryId);
        }
        if (userId != -1) { // Chỉ thêm nếu có giá trị hợp lệ
            values.put("user_id", userId);
        }
        return values;
    }






}
