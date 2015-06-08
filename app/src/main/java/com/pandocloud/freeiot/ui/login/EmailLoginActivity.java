package com.pandocloud.freeiot.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.pandocloud.android.api.AbsOpenApi;
import com.pandocloud.freeiot.R;
import com.pandocloud.freeiot.api.UserApi;
import com.pandocloud.freeiot.api.WrapperBaseJsonHttpResponseHandler;
import com.pandocloud.freeiot.ui.MainActivity;
import com.pandocloud.freeiot.ui.app.AppConstants;
import com.pandocloud.freeiot.ui.app.UserState;
import com.pandocloud.freeiot.ui.base.BaseActivity;
import com.pandocloud.freeiot.ui.bean.http.UserLoginResponse;
import com.pandocloud.freeiot.utils.ActivityUtils;
import com.pandocloud.freeiot.utils.AnalyticsUtils;
import com.pandocloud.freeiot.utils.CommonUtils;
import com.pandocloud.freeiot.utils.GsonUtils;
import com.pandocloud.freeiot.utils.LogUtils;
import com.umeng.analytics.MobclickAgent;

import org.apache.http.Header;

/**
 * Created by ywen on 15/5/20.
 */
public class EmailLoginActivity extends BaseActivity implements View.OnClickListener {

    private EditText mEmailEditView;

    private EditText mPasswordEditView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_email_login);

        mEmailEditView = (EditText) findViewById(R.id.mobile);
        mPasswordEditView = (EditText)findViewById(R.id.pwd);
        findViewById(R.id.login_btn).setOnClickListener(this);
        findViewById(R.id.register_btn).setOnClickListener(this);
        findViewById(R.id.reset_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                login();
                break;
            case R.id.register_btn:
                ActivityUtils.start(EmailLoginActivity.this, EmailRegisterActivity.class, LoginActivity.REIGSTER_REQUEST_CODE, R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            case R.id.reset_btn:
                ActivityUtils.start(EmailLoginActivity.this, EmailResetPwdActivity.class, R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            default:
                break;
        }
    }

    public void login() {
        String email = mEmailEditView.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            CommonUtils.ToastMsg(this, R.string.error_email_empty);
            return;
        }
        String pwd = mPasswordEditView.getText().toString().trim();
        if (TextUtils.isEmpty(pwd)) {
            CommonUtils.ToastMsg(this, R.string.error_login_pwd_empty);
            return;
        }

        MobclickAgent.onEvent(this, AnalyticsUtils.AnalyticsEventKeys.EVENT_LOGIN);

        UserApi.login(EmailLoginActivity.this, AppConstants.PRODUCT_KEY, null,
            null,
            email,
            pwd,
            new WrapperBaseJsonHttpResponseHandler<UserLoginResponse>(EmailLoginActivity.this) {

                @Override
                public void onStart() {
                    CommonUtils.showProgressDialog(EmailLoginActivity.this, null, null);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String rawJsonData,
                                      UserLoginResponse response) {
                    if (response.isSuccess()) {
                        if (response.data != null) {
                            UserState.getInstances(EmailLoginActivity.this).saveAccessToken(response.data.access_token);
                        }
                        CommonUtils.ToastMsg(EmailLoginActivity.this, R.string.login_success);
                        ActivityUtils.start(EmailLoginActivity.this, MainActivity.class, R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                        finish();
                    } else {
                        CommonUtils.dismissDialog();
                        super.onSuccess(statusCode, headers, rawJsonData, response);
                    }
                }

                @Override
                protected UserLoginResponse parseResponse2(String rawJsonData, boolean isFailure) throws Throwable {
                    LogUtils.d("UserApi#login-> parseResponse: " + rawJsonData);
                    if (!isFailure && !TextUtils.isEmpty(rawJsonData)) {
                        return GsonUtils.getInstance().getGson().fromJson(rawJsonData, UserLoginResponse.class);
                    }
                    return null;
                }

                @Override
                public void onFinish() {
                    CommonUtils.dismissDialog();
                }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(AnalyticsUtils.AnalyticsViewKeys.VIEW_EMAIL_LOGIN_ACTIVITY);
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(AnalyticsUtils.AnalyticsViewKeys.VIEW_EMAIL_LOGIN_ACTIVITY);
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AbsOpenApi.cancel(this, true);
    }
}
