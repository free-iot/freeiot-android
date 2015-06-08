package com.pandocloud.freeiot.ui.device;

import java.util.List;

import org.apache.http.Header;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pandocloud.android.api.AbsOpenApi;
import com.pandocloud.freeiot.R;
import com.pandocloud.freeiot.api.DevicesApi;
import com.pandocloud.freeiot.api.WrapperBaseJsonHttpResponseHandler;
import com.pandocloud.freeiot.ui.app.UserState;
import com.pandocloud.freeiot.ui.base.BaseActivity;
import com.pandocloud.freeiot.ui.base.EasyBaseAdapter;
import com.pandocloud.freeiot.ui.bean.DevicePermission;
import com.pandocloud.freeiot.ui.bean.http.BaseResponse;
import com.pandocloud.freeiot.ui.bean.http.DevicesPermissionResponse;
import com.pandocloud.freeiot.utils.ActivityUtils;
import com.pandocloud.freeiot.utils.CommonUtils;
import com.pandocloud.freeiot.utils.DialogFactory;
import com.pandocloud.freeiot.utils.GsonUtils;
import com.pandocloud.freeiot.utils.UIUtils;


/**
 * 设备所有授权信息列表
 * @author ywen
 *
 */
public class DevicePermissionsListActivity extends BaseActivity implements OnClickListener, OnRefreshListener {

	public static final int DEVICE_AUTH_REQUEST_CODE = 12;
	
	private static final int MENU_MODIFY_PERMISSSION = 1;
	
	private static final int MENU_DELETE_PERMISSION = 2;
	
	private ListView mListView;
	
	private SwipeRefreshLayout swipeLayout;
	
	private String identifier;
	
	private DevicePermissionAdapter adapter;
	
	private int listPosition;
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_deivces_permission_list);
		
		identifier = getIntent().getStringExtra("identifier");
		
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
		swipeLayout.setColorScheme(R.color.main_red_color);
		swipeLayout.setOnRefreshListener(this);
		swipeLayout.setProgressViewOffset(true, 0, UIUtils.dip2px(this, 48));
        mListView = (ListView) findViewById(R.id.listview);
        
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.btn_add).setOnClickListener(this);
        loadDeviceAuthList(true);
	}

	
	@Override
	public void onRefresh() {
		loadDeviceAuthList(true);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			onBackPressed();
			break;
		case R.id.btn_add:
			Bundle bundle = new Bundle();
			bundle.putString("identifier", identifier);
			ActivityUtils.start(DevicePermissionsListActivity.this, DeviceAuthActivity.class, bundle, DEVICE_AUTH_REQUEST_CODE, R.anim.slide_in_from_right, R.anim.slide_out_to_left);
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == DEVICE_AUTH_REQUEST_CODE) {
				loadDeviceAuthList(false);
			}
		}
	}
	
	public void loadDeviceAuthList(final boolean showLoading) {
		DevicesApi.devicePermissions(this,
        UserState.getInstances(this).getAccessToken(""),
        identifier,
        new WrapperBaseJsonHttpResponseHandler<DevicesPermissionResponse>(this) {

            @Override
            public void onStart() {
                if (showLoading) {
                    swipeLayout.setRefreshing(true);
                }
            }

            @Override
            protected DevicesPermissionResponse parseResponse2(
                    String rawJsonData, boolean isFailure)throws Throwable {
                if (isFailure || TextUtils.isEmpty(rawJsonData)) {
                    return null;
                }
                return GsonUtils.getInstance().getGson().fromJson(rawJsonData, DevicesPermissionResponse.class);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  String rawJsonData,
                                  DevicesPermissionResponse response) {
                if (response.isSuccess()) {
                    if (adapter == null) {
                        adapter = new DevicePermissionAdapter(DevicePermissionsListActivity.this, response.data);
                        mListView.setAdapter(adapter);
                    } else {
                        adapter.updateDataSet(response.data);
                    }
                } else {
                    super.onSuccess(statusCode, headers, rawJsonData, response);
                }
            }

            @Override
            public void onFinish() {
                swipeLayout.setRefreshing(false);
            }
        });
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final DevicePermission dp = (DevicePermission) adapter.getItem(listPosition);
		switch (item.getItemId()) {
		case MENU_MODIFY_PERMISSSION:
			Bundle bundle = new Bundle();
			bundle.putString("identifier", identifier);
			bundle.putInt("permissionId", dp.permission_id);
			bundle.putInt("privilege", dp.privilege);
			ActivityUtils.start(DevicePermissionsListActivity.this, DevicePermissionModifyActivity.class, bundle,DEVICE_AUTH_REQUEST_CODE,  R.anim.slide_in_from_right, R.anim.slide_out_to_left);
			break;
		case MENU_DELETE_PERMISSION:
			DialogFactory.createCommonDialog(DevicePermissionsListActivity.this, getString(R.string.warn),
                    getString(R.string.auth_delete_tip),
                    getString(android.R.string.ok), getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            delete(listPosition, dp.permission_id);
                        }
                    }, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
			break;
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}
	
	public void delete(final int position, int permissionId) {
		DevicesApi.devicePermissionDelete(this,
        UserState.getInstances(this).getAccessToken(""),
        identifier,
        permissionId,
        new WrapperBaseJsonHttpResponseHandler<BaseResponse>(this) {

            @Override
            public void onStart() {
                CommonUtils.showingProgressDialog();
            }

            @Override
            protected BaseResponse parseResponse2(String rawJsonData,
                    boolean isFailure) throws Throwable {
                if (isFailure || TextUtils.isEmpty(rawJsonData)) {
                    return null;
                }
                return GsonUtils.getInstance().getGson().fromJson(rawJsonData, BaseResponse.class);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                    String rawJsonData, BaseResponse response) {
                if (response.isSuccess()) {
                    adapter.removeAtPosition(position);
                } else {
                    super.onSuccess(statusCode, headers, rawJsonData, response);
                }
            }

            @Override
            public void onFinish() {
                CommonUtils.dismissDialog();
            }
        });
	}
	
	
	class DevicePermissionAdapter extends EasyBaseAdapter<DevicePermission> {

		public DevicePermissionAdapter(Context context, List<DevicePermission> dataset) {
			super(context, dataset);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.adapter_devices_permission_item, parent, false);
			}
			final View itemView = convertView;
			DevicePermission dp = (DevicePermission) getItem(position);
			TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
			TextView tvPermission = (TextView) convertView.findViewById(R.id.tv_permission);
			if (!TextUtils.isEmpty(dp.name)) {
				tvName.setText(dp.name);
			} else if (!TextUtils.isEmpty(dp.phone)) {
				tvName.setText(dp.phone);
			}
			
			tvPermission.setText(dp.privilege == DeviceAuthActivity.PERMISSION_READ_NOTIFICATION ?
					R.string.tip_auth_read_only : R.string.tip_auth_read_write);
			final ImageView ivSetting = (ImageView) convertView.findViewById(R.id.setting);
			convertView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				
				@Override
				public void onCreateContextMenu(ContextMenu menu, View v,
						ContextMenuInfo menuInfo) {
					menu.add(0, MENU_MODIFY_PERMISSSION, 0, getString(R.string.device_auth_modify));
					menu.add(0, MENU_DELETE_PERMISSION, 0, getString(R.string.device_auth_delete));
				}
			});
			ivSetting.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					listPosition = position;
					itemView.showContextMenu();
				}
			});
			return convertView;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		AbsOpenApi.cancel(this, true);
	}
}
