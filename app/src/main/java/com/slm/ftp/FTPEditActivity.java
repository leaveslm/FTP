package com.slm.ftp;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.slm.method.Constant;
import com.slm.method.FTPInfo;

public class FTPEditActivity extends AppCompatActivity {

    private EditText ftpNameEditText, hostNameEditText, portEditText, userNameEditText, passwordEditText;
    private Button saveButton;

    private FTPInfo ftpInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp_edit);

        // 返回按钮
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        ftpNameEditText = (EditText) findViewById(R.id.ftpNameEditText);
        hostNameEditText = (EditText) findViewById(R.id.hostNameEditText);
        portEditText = (EditText) findViewById(R.id.portEditText);
        userNameEditText = (EditText) findViewById(R.id.userNameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        saveButton = (Button) findViewById(R.id.saveButton);

        // 判断是否为更新请求
        if (getIntent().getIntExtra("requestCode", 1) == Constant.UPDATE_REQUEST_CODE) {
            ftpInfo = (FTPInfo) getIntent().getSerializableExtra("ftpInfo");
            ftpNameEditText.setText(ftpInfo.getFtpName());
            hostNameEditText.setText(ftpInfo.getHostName());
            portEditText.setText(String.valueOf(ftpInfo.getPort()));
            userNameEditText.setText(ftpInfo.getUserName());
            passwordEditText.setText(ftpInfo.getPassword());
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ftpConfig();
                Intent intent = new Intent();
                intent.putExtra("ftpInfo", ftpInfo);

                // 判断是否为更新请求
                if (getIntent().getIntExtra("requestCode", 1) == Constant.UPDATE_REQUEST_CODE) {
                    setResult(Constant.UPDATE_REQUEST_CODE, intent);
                } else {
                    setResult(Constant.ADD_REQUEST_CODE, intent);
                }
                FTPEditActivity.this.finish();
            }
        });

    }

    public void ftpConfig() {
        if (ftpInfo == null) {
            ftpInfo = new FTPInfo();
        }
        ftpInfo.setFtpName(ftpNameEditText.getText().toString());
        ftpInfo.setHostName(hostNameEditText.getText().toString());
        if (portEditText.getText().toString().equals("")) {
            portEditText.setText("21"); // 默认 21 端口
        }
        ftpInfo.setPort(Integer.valueOf(portEditText.getText().toString()));
        if (userNameEditText.getText().toString().equals("")) {
            userNameEditText.setText("anonymous"); // 默认匿名登陆
        }
        ftpInfo.setUserName(userNameEditText.getText().toString());
        ftpInfo.setPassword(passwordEditText.getText().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        menu.add(0, 1, 0, "");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                setResult(Constant.FTP_CANCEL);
                finish();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
