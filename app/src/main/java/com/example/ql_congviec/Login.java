package com.example.ql_congviec;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class Login extends AppCompatActivity {

    /* ────────── UI ────────── */
    private EditText  etUsername, etPassword;
    private Button    btnLogin;
    private TextView  tvGoRegister;
    private ImageView ivTogglePassword;
    private CheckBox  cbRememberMe;

    /* ────────── DB ────────── */
    private DBHelperDatabase dbh;

    /* ────────── State ────────── */
    private boolean isPasswordVisible = false;

    /* ────────── Pref keys ────────── */
    private static final String PREFS_NAME   = "user_prefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember";
    private static final String KEY_USER_ID  = "user_id";

    /* ────────── Life-cycle ────────── */
    @Override protected void onCreate(Bundle savedInstanceState) {          // ①
        super.onCreate(savedInstanceState);

        dbh = new DBHelperDatabase(this);                                   // ②
        if (autoLoginIfRemembered()) return;                                // ③

        setContentView(R.layout.activity_login);                            // ④
        mapViews();                                                         // ⑤
        loadSavedCredentials();                                             // ⑥
        setClickListeners();                                                // ⑦
    }

    /* ══════════════════════════════ PRIVATE METHODS ══════════════════════════════ */

    /** Thử tự đăng nhập nếu đã chọn "Remember me" trước đó. */
    private boolean autoLoginIfRemembered() {                               // A1
        SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (!pref.getBoolean(KEY_REMEMBER, false)) return false;

        String u = pref.getString(KEY_USERNAME, "");
        String p = pref.getString(KEY_PASSWORD, "");
        if (!dbh.checkUserRaw(u, p)) return false;

        int userId = dbh.getUserId(u);
        pref.edit().putInt(KEY_USER_ID, userId).apply();
        startActivity(new Intent(this, MainActivity.class));
        finish();
        return true;
    }

    private void mapViews() {
        etUsername      = findViewById(R.id.et_login_username);
        etPassword      = findViewById(R.id.et_login_password);
        btnLogin        = findViewById(R.id.btn_login);
        tvGoRegister    = findViewById(R.id.tv_go_register);
        ivTogglePassword= findViewById(R.id.iv_toggle_password);
        cbRememberMe    = findViewById(R.id.cb_remember_me);
    }

    private void setClickListeners() {
        ivTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        btnLogin.setOnClickListener(v -> {
            String u = etUsername.getText().toString().trim();
            String p = etPassword.getText().toString();
            handleLogin(u, p);                                              // B1
        });

        tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, Register.class)));
    }

    /* ────────── Login flow ────────── */
    private void handleLogin(String user, String pass) {
        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập username và password.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!dbh.checkUserRaw(user, pass)) {
            Toast.makeText(this, "Sai username hoặc password.", Toast.LENGTH_SHORT).show();
            return;
        }
        int id = dbh.getUserId(user);
        saveCredentials(user, pass, cbRememberMe.isChecked());
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putInt(KEY_USER_ID, id).apply();
        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /* ────────── Helpers ────────── */
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        int type = isPasswordVisible
                ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
        etPassword.setInputType(type);
        ivTogglePassword.setImageResource(
                isPasswordVisible ? android.R.drawable.ic_menu_gallery
                        : android.R.drawable.ic_menu_view);
        etPassword.setSelection(etPassword.length());
    }

    private void loadSavedCredentials() {
        SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (!pref.getBoolean(KEY_REMEMBER, false)) return;

        etUsername.setText(pref.getString(KEY_USERNAME, ""));
        etPassword.setText(pref.getString(KEY_PASSWORD, ""));
        cbRememberMe.setChecked(true);
    }

    private void saveCredentials(String user, String pass, boolean remember) {
        SharedPreferences.Editor ed = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        if (remember) {
            ed.putString(KEY_USERNAME, user)
                    .putString(KEY_PASSWORD, pass)
                    .putBoolean(KEY_REMEMBER, true);
        } else {
            ed.remove(KEY_USERNAME).remove(KEY_PASSWORD).putBoolean(KEY_REMEMBER, false);
        }
        ed.apply();
    }
}
