package com.pandocloud.freeiot.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.pandocloud.android.api.interfaces.RequestListener;
import com.pandocloud.android.api.interfaces.SimpleRequestListener;
import com.pandocloud.freeiot.R;
import com.pandocloud.freeiot.api.UserApi;
import com.pandocloud.freeiot.api.WrapperBaseJsonHttpResponseHandler;
import com.pandocloud.freeiot.ui.MainActivity;
import com.pandocloud.freeiot.ui.app.AppConstants;
import com.pandocloud.freeiot.ui.app.UserState;
import com.pandocloud.freeiot.ui.base.BaseActivity;
import com.pandocloud.freeiot.ui.bean.http.UserLoginResponse;
import com.pandocloud.freeiot.ui.bean.http.UserRegisterResponse;
import com.pandocloud.freeiot.ui.helper.ProductInfoHelper;
import com.pandocloud.freeiot.utils.ActivityUtils;
import com.pandocloud.freeiot.utils.AnalyticsUtils;
import com.pandocloud.freeiot.utils.CommonUtils;
import com.pandocloud.freeiot.utils.GsonUtils;
import com.pandocloud.freeiot.utils.LogUtils;
import com.umeng.analytics.MobclickAgent;

import org.apache.http.Header;

public class EmailRegisterActivity extends BaseActivity implements View.OnClickListener {

    private EditText mEmailEditView;
    private EditText mPwdEditView;
    private EditText mPwdAgainEditView;

    private ProductInfoHelper mProductInfoHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_email_register);

        initView();
    }

    public void initView() {
        mEmailEditView = (EditText) findViewById(R.id.email);
        mPwdEditView = (EditText) findViewById(R.id.pwd);
        mPwdAgainEditView = (EditText) findViewById(R.id.pwd_again);
        findViewById(R.id.register_btn).setOnClickListener(this);

        mProductInfoHelper = new ProductInfoHelper(productRequestListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_btn:
                register();
                break;
        }
    }

    public void register() {
        final String email = mEmailEditView.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            CommonUtils.ToastMsg(this, R.string.error_email_empty);
            return;
        }
        final String pwd = mPwdEditView.getText().toString().trim();
        if (TextUtils.isEmpty(pwd)) {
            CommonUtils.ToastMsg(this, R.string.error_empty_pwd);
            return;
        }
        String pwdAgain = mPwdAgainEditView.getText().toString().trim();
        if (TextUtils.isEmpty(pwdAgain)) {
            CommonUtils.ToastMsg(this, R.string.again_pwd_empty);
            return;
        }
        if (!pwd.equals(pwdAgain)) {
            CommonUtils.ToastMsg(this, R.string.pwd_not_equal);
            return;
        }
        UserApi.mailRegister(this, AppConstants.PRODUCT_KEY, null, email, pwd,
            new WrapperBaseJsonHttpResponseHandler<UserRegisterResponse>(EmailRegisterActivity.this) {

                @Override
                public void onStart() {
                    super.onStart();
                    CommonUtils.showProgressDialog(EmailRegisterActivity.this, null, null);
                }

                @Override
                protected UserRegisterResponse parseResponse2(String rawJsonData, boolean isFailure) throws Throwable {
                    if (isFailure || TextUtils.isEmpty(rawJsonData)) {
                        return null;
                    }
                    return GsonUtils.getInstance().getGson().fromJson(rawJsonData, UserRegisterResponse.class);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String rawJsonData, UserRegisterResponse response) {
                    if (response.isSuccess()) {
                        if (response.data != null) {
                            UserState.getInstances(EmailRegisterActivity.this).saveAccessToken(response.data.access_token);
                        }
                        login(email, pwd);
                    } else {
                        CommonUtils.dismissDialog();
                        super.onSuccess(statusCode, headers, rawJsonData, response);
                    }
                }
            });
    }

    public void login(String email, String pwd) {
        UserApi.login(EmailRegisterActivity.this, AppConstants.PRODUCT_KEY,
        null,
        null,
        email,
        pwd,
        new WrapperBaseJsonHttpResponseHandler<UserLoginResponse>(this) {

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  Throwable throwable, String rawJsonData, UserLoginResponse errorResponse) {
                CommonUtils.ToastMsg(EmailRegisterActivity.this, R.string.register_success);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonData,
                                  UserLoginResponse response) {
                if (response.isSuccess()) {
                    if (response.data != null) {
                        UserState.getInstances(EmailRegisterActivity.this).saveAccessToken(response.data.access_token);
                    }
                    mProductInfoHelper.getProductInfo(EmailRegisterActivity.this);
                } else {
                    CommonUtils.ToastMsg(EmailRegisterActivity.this, R.string.register_success);
                    onBackPressed();
                }
            }

            @Override
            protected UserLoginResponse parseResponse2(String rawJsonData,
                                                       boolean isFailure) throws Throwable {
                LogUtils.e("RegisterActivity", "rawJsonData: " + rawJsonData);
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


    RequestListener productRequestListener = new SimpleRequestListener() {

        public void onSuccess() {
            CommonUtils.ToastMsg(EmailRegisterActivity.this, R.string.register_success);
            setResult(RESULT_OK);
            ActivityUtils.start(EmailRegisterActivity.this, MainActivity.class, R.anim.slide_in_from_right, R.anim.slide_out_to_left);
            finish();
        }

        public void onFail(Exception e) {
            CommonUtils.ToastMsg(EmailRegisterActivity.this, R.string.register_success);
            onBackPressed();
        }

        public void onFinish() {
            CommonUtils.dismissDialog();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(AnalyticsUtils.AnalyticsViewKeys.VIEW_EMAIL_REGISTER_ACTIVITY);
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(AnalyticsUtils.AnalyticsViewKeys.VIEW_EMAIL_REGISTER_ACTIVITY);
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UserApi.cancel(EmailRegisterActivity.this, true);
    }
}
