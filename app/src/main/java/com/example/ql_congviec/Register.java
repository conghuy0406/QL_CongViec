package com.example.ql_congviec;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ql_congviec.Database.DBHelperDatabase;

public class Register extends AppCompatActivity {

    /* ────────── UI ────────── */
    private EditText  etFullName, etEmail, etUsername, etPassword, etConfirm;
    private Button    btnRegister;
    private TextView  tvGoLogin;
    private ImageView ivTogglePass, ivToggleConfirm;

    /* ────────── DB ────────── */
    private DBHelperDatabase dbh;

    /* ────────── State ────────── */
    private boolean passVisible = false;
    private boolean confirmVisible = false;

    /* ────────── Life-cycle ────────── */
    @Override protected void onCreate(Bundle savedInstanceState) {            // ①
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mapViews();                                                           // ②
        dbh = new DBHelperDatabase(this);                                     // ③
        setClickListeners();                                                  // ④
    }

    /* ══════════════════════════════ PRIVATE METHODS ══════════════════════════════ */

    private void mapViews() {
        etFullName      = findViewById(R.id.et_register_fullname);
        etEmail         = findViewById(R.id.et_register_email);
        etUsername      = findViewById(R.id.et_register_username);
        etPassword      = findViewById(R.id.et_register_password);
        etConfirm       = findViewById(R.id.et_register_confirm_password);
        btnRegister     = findViewById(R.id.btn_register);
        tvGoLogin       = findViewById(R.id.tv_go_login);
        ivTogglePass    = findViewById(R.id.iv_toggle_password);
        ivToggleConfirm = findViewById(R.id.iv_toggle_confirm_password);
    }

    private void setClickListeners() {
        ivTogglePass   .setOnClickListener(v -> toggleVisibility(etPassword, ivTogglePass, true));
        ivToggleConfirm.setOnClickListener(v -> toggleVisibility(etConfirm , ivToggleConfirm, false));

        btnRegister.setOnClickListener(v -> {
            String full = etFullName.getText().toString().trim();
            String mail = etEmail   .getText().toString().trim();
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString();
            String conf = etConfirm .getText().toString();
            handleRegister(full, mail, user, pass, conf);                     // ⑤
        });

        tvGoLogin.setOnClickListener(v ->
                startActivity(new Intent(this, Login.class)));
    }

    private void toggleVisibility(EditText field, ImageView icon, boolean isPass) {
        boolean curr = isPass ? passVisible : confirmVisible;
        curr = !curr;
        int type = curr ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
        field.setInputType(type);
        icon.setImageResource(curr ? android.R.drawable.ic_menu_gallery
                : android.R.drawable.ic_menu_view);
        field.setSelection(field.length());
        if (isPass) passVisible = curr; else confirmVisible = curr;
    }

    /* ────────── Register flow ────────── */
    private void handleRegister(String full, String mail, String user, String pass, String conf) {

        if (full.isEmpty() || mail.isEmpty() || user.isEmpty() || pass.isEmpty() || conf.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
        } else {
            if (!pass.equals(conf)) {
                Toast.makeText(this, "Password và Confirm Password không khớp.", Toast.LENGTH_SHORT).show();
            } else {
                boolean ok = dbh.insertUserRaw(user, pass, full, mail);
                if (ok) {
                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, Login.class));
                    finish();
                } else {
                    Toast.makeText(this, "Đăng ký thất bại: username hoặc email đã tồn tại.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}
