package com.pandocloud.freeiot.ui.login;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.pandocloud.freeiot.R;
import com.pandocloud.freeiot.api.UserApi;
import com.pandocloud.freeiot.api.WrapperBaseJsonHttpResponseHandler;
import com.pandocloud.freeiot.ui.app.AppConstants;
import com.pandocloud.freeiot.ui.app.UserState;
import com.pandocloud.freeiot.ui.base.BaseActivity;
import com.pandocloud.freeiot.ui.bean.http.BaseResponse;
import com.pandocloud.freeiot.ui.bean.http.UserRegisterResponse;
import com.pandocloud.freeiot.utils.ActivityUtils;
import com.pandocloud.freeiot.utils.AnalyticsUtils;
import com.pandocloud.freeiot.utils.CommonUtils;
import com.pandocloud.freeiot.utils.GsonUtils;
import com.pandocloud.freeiot.utils.LogUtils;
import com.umeng.analytics.MobclickAgent;

import org.apache.http.Header;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ywen on 15/5/20.
 */
public class EmailResetPwdActivity extends BaseActivity implements View.OnClickListener {

    private EditText mEmailEditView;

    private EditText mAuthCodeEditView;

    private EditText mPasswordEditView;

    private Timer mTimer;

    private int mDexTime = 60;

    private TextView mAuthCodeEidtView;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            mDexTime--;
            if (mDexTime <=0) {
                mAuthCodeEidtView.setEnabled(true);
                mAuthCodeEidtView.setText(R.string.get_authcode);
            } else {
                mAuthCodeEidtView.setText(getString(R.string.reget_authcode, mDexTime));
            }
        };
    };

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        setContentView(R.layout.activity_email_reset_pwd);

        mEmailEditView = (EditText)findViewById(R.id.email);
        mAuthCodeEditView = (EditText)findViewById(R.id.authcode);
        mPasswordEditView = (EditText)findViewById(R.id.pwd);

        mAuthCodeEidtView = (TextView) findViewById(R.id.authcode_btn);
        mAuthCodeEidtView.setOnClickListener(this);
        findViewById(R.id.reset_btn).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
    }

    public void userVertify() {
        String email = mEmailEditView.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            CommonUtils.ToastMsg(this, R.string.error_email_empty);
            return;
        }
        MobclickAgent.onEvent(EmailResetPwdActivity.this, AnalyticsUtils.AnalyticsEventKeys.EVENT_RESET_PWD_VERTIFY);

        UserApi.verification(EmailResetPwdActivity.this, AppConstants.PRODUCT_KEY, null, null, email,
            new WrapperBaseJsonHttpResponseHandler<BaseResponse>(this) {

                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String rawJsonData,
                                      BaseResponse response) {
                    if (response.isSuccess()) {
                        CommonUtils.ToastMsg(EmailResetPwdActivity.this, R.string.email_vertify_code_send);
                        mAuthCodeEidtView.setEnabled(false);
                        if (mTimer == null) {
                            mTimer = new Timer();
                        }
                        mDexTime = 60;
                        mTimer.schedule(new VertifyTimeTask(), 0, 1000);
                    } else {
                        CommonUtils.ToastMsg(EmailResetPwdActivity.this, R.string.send_authcode_failed);
                    }
                }

                @Override
                protected BaseResponse parseResponse2(String rawJsonData,
                                                      boolean isFailure) throws Throwable {
                    LogUtils.e("EmailResetPwdActivity", "rawJsonData: " + rawJsonData);
                    if (!isFailure && !TextUtils.isEmpty(rawJsonData)) {
                        return GsonUtils.getInstance().getGson().fromJson(rawJsonData, BaseResponse.class);
                    }
                    return null;
                }

                @Override
                public void onFinish() {
                    CommonUtils.dismissDialog();
                }
            });
    }


    class VertifyTimeTask extends TimerTask {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.authcode_btn:
                userVertify();
                break;
            case R.id.reset_btn:
                resetPassword();
                break;
            case R.id.back:
                ActivityUtils.animFinish(this, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(AnalyticsUtils.AnalyticsViewKeys.VIEW_EMAIL_RESET_PWD_ACTIVITY);
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(AnalyticsUtils.AnalyticsViewKeys.VIEW_EMAIL_RESET_PWD_ACTIVITY);
        MobclickAgent.onPause(this);
    }

    public void resetPassword() {
        String authCode = mAuthCodeEditView.getText().toString().trim();
        final String password = mPasswordEditView.getText().toString().trim();
        if (TextUtils.isEmpty(authCode)) {
            CommonUtils.ToastMsg(this, R.string.error_empty_authcode);
            return;
        }
        if (TextUtils.isEmpty(password)) {
            CommonUtils.ToastMsg(this, R.string.error_empty_pwd);
            return;
        }
        final String email = mEmailEditView.getText().toString().trim();

        MobclickAgent.onEvent(EmailResetPwdActivity.this, AnalyticsUtils.AnalyticsEventKeys.EVENT_RESET_PWD);

        UserApi.reset(EmailResetPwdActivity.this, AppConstants.PRODUCT_KEY, null,
            null, email, password, authCode, new WrapperBaseJsonHttpResponseHandler<UserRegisterResponse>(EmailResetPwdActivity.this) {

                @Override
                public void onStart() {
                    CommonUtils.showProgressDialog(EmailResetPwdActivity.this, null, null, new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            UserApi.cancel(EmailResetPwdActivity.this, true);
                        }
                    });
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                                      Throwable throwable, String rawJsonData,
                                      UserRegisterResponse errorResponse) {
                    super.onFailure(statusCode, headers, throwable, rawJsonData, errorResponse);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String rawJsonData,
                                      UserRegisterResponse response) {
                    if (response.isSuccess()) {
                        if (mTimer != null) {
                            mTimer.cancel();
                            mTimer = null;
                        }
                        if (response.data != null) {
                            UserState.getInstances(EmailResetPwdActivity.this).saveAccessToken(response.data.access_token);
                        }
                        CommonUtils.ToastMsg(EmailResetPwdActivity.this, R.string.reset_pwd_success);
                        ActivityUtils.animFinish(EmailResetPwdActivity.this, R.anim.slide_out_to_right, R.anim.slide_in_from_left);
                    } else {
                        super.onSuccess(statusCode, headers, rawJsonData, response);
                    }
                }

                @Override
                protected UserRegisterResponse parseResponse2(String rawJsonData,
                                                              boolean isFailure) throws Throwable {
                    LogUtils.d("UserApi#reset pwd-> parseResponse: " + rawJsonData);
                    if (!isFailure && !TextUtils.isEmpty(rawJsonData)) {
                        return GsonUtils.getInstance().getGson().fromJson(rawJsonData, UserRegisterResponse.class);
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
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }


    @Override
    protected void onDestroy() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        UserApi.cancel(EmailResetPwdActivity.this, true);
        super.onDestroy();
    }
}
