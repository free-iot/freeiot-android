package com.pandocloud.freeiot.ui;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;

import com.pandocloud.freeiot.R;
import com.pandocloud.freeiot.api.UserApi;
import com.pandocloud.freeiot.api.WrapperBaseJsonHttpResponseHandler;
import com.pandocloud.freeiot.ui.app.AppConstants;
import com.pandocloud.freeiot.ui.app.UserState;
import com.pandocloud.freeiot.ui.bean.http.BaseResponse;
import com.pandocloud.freeiot.ui.device.config.GateWayConfigActivity;
import com.pandocloud.freeiot.ui.home.MyDevicesListFragment;
import com.pandocloud.freeiot.ui.login.LoginActivity;
import com.pandocloud.freeiot.utils.ActivityUtils;
import com.pandocloud.freeiot.utils.AnalyticsUtils;
import com.pandocloud.freeiot.utils.CommonUtils;
import com.pandocloud.freeiot.utils.GsonUtils;
import com.pandocloud.freeiot.utils.LogUtils;
import com.umeng.analytics.MobclickAgent;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.main_red_color));

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        if (position == 0) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, new MyDevicesListFragment())
                    .commit();
        } else if (position == 1) {
            logout();
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.my_devices);
                break;
            case 2:
                mTitle = getString(R.string.logout);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.main_red_color));
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            ActivityUtils.start(MainActivity.this, GateWayConfigActivity.class, R.anim.slide_in_from_right, R.anim.slide_out_to_left);

            MobclickAgent.onEvent(MainActivity.this, AnalyticsUtils.AnalyticsEventKeys.EVENT_ADD_DEVICE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void logout() {
        UserApi.logout(this, UserState.getInstances(this).getAccessToken(""),
        new WrapperBaseJsonHttpResponseHandler<BaseResponse>(MainActivity.this) {

            @Override
            protected BaseResponse parseResponse2(String rawJsonData,
                                                  boolean isFailure) throws Throwable {
                LogUtils.d("logout rawJsonData: " + rawJsonData);
                if (!isFailure && !TextUtils.isEmpty(rawJsonData)) {
                    return GsonUtils.getInstance().getGson().fromJson(rawJsonData, BaseResponse.class);
                }
                return null;
            }
        });

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                UserState.getInstances(MainActivity.this).logout(MainActivity.this);
                ActivityUtils.start(MainActivity.this, LoginActivity.class, R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                finish();
            }
        }, 1000);
    }

}
