package cn.com.ths.baidulocation;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import java.util.ArrayList;
import java.util.List;

// import cn.com.ths.baidulocation.test.MainActivity;
// import cn.com.ths.baidulocation.test.R;

/**
 * 百度定位插件，默认使用gcj02定位.
 */
public class BaiduLocation extends CordovaPlugin {

	private static final String ACTION_GET_LOCATION_EVENT = "get";
	private static final String ACTION_STOP_LOCATION_EVENT = "stop";
	private static final String ACTION_CONFIG_LOCATION_EVENT = "config";

	private LocationClient mLocationClient = null;
	private BDAbstractLocationListener myListener = new MyLocationListener();
	private List<ActionModel> callbackContextList = new ArrayList<ActionModel>();
	private JSONObject locationInfo;
	private Activity activity;
    private String mCoorType ="gcj02"; // 默认返回国测局经纬度坐标系：gcj02 其他返回百度墨卡托坐标系 ：bd09 返回百度经纬度坐标系 ：bd09ll
	private int mSpan = 1000; // 默认获取定位数据间隔，单位毫秒
	private  BaiduLocation instance;
	private String action = null; // 事件action
	private JSONArray args = null; // 事件参数
	private CallbackContext callbackContext;//回调js的上下文对象
	private NotificationUtils mNotificationUtils;
	private Notification notification;
	/**
	 * 权限列表
	 */
	private String[] locPerArr = new String[] {
			Manifest.permission.READ_PHONE_STATE,

			Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.READ_EXTERNAL_STORAGE
	};

	public BaiduLocation() {
		instance = this;
	}
	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		activity = cordova.getActivity();
		initGPS();
		mLocationClient = new LocationClient(activity); // 声明LocationClient类
		mLocationClient.registerLocationListener(myListener); // 注册监听函数
		initLocation();
	}

		// private String[] locPerArr = new String[] {
		// 	Manifest.permission.READ_PHONE_STATE,
		// 	Manifest.permission.ACCESS_COARSE_LOCATION,
		// 	Manifest.permission.ACCESS_FINE_LOCATION,
		// 	Manifest.permission.READ_EXTERNAL_STORAGE,
		// 	Manifest.permission.WRITE_EXTERNAL_STORAGE,
		// 	};

     /**
	 * 检查权限并申请
	 */
	private void promtForLocation() {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
			for (int i = 0, len = locPerArr.length; i < len; i++) {
				if (!PermissionHelper.hasPermission(this, locPerArr[i])) {
					PermissionHelper.requestPermission(this, i, locPerArr[i]);
					return;
				}
			}
			exeLoc(action);
		}else{
			exeLoc(action);
		}

	}

	@Override
	public void onRequestPermissionResult(int requestCode,
			String[] permissions, int[] grantResults) throws JSONException {
		// TODO Auto-generated method stub
		for (int r : grantResults) {
			if (r == PackageManager.PERMISSION_DENIED) {
				return;
			}
		}
		promtForLocation();
	}

	/**
	 * 执行定位操作
	 * 
	 * @param action 动作类型，开启或者停止定位
	 */
	private boolean exeLoc(String action) {
		if (ACTION_GET_LOCATION_EVENT.equals(action)) { // 启动定位
			ActionModel actionModel = new ActionModel();
			actionModel.setCallbackContext(callbackContext);
			boolean isSingle = true; // 默认单次定位
			if(args.length()>0){
				try {
					isSingle = args.getBoolean(0);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			actionModel.setSingle(isSingle);
			this.callbackContextList.add(actionModel);
			cordova.getThreadPool().execute(new Runnable() {
				public void run() {
					mLocationClient.start();
				}
			});
			return true;
		} else if (ACTION_STOP_LOCATION_EVENT.equals(action)) { //终止定位
			mLocationClient.stop();
			// 清空所有定位请求回调
			callbackContextList.clear();
			return true;
		} else  if(ACTION_CONFIG_LOCATION_EVENT.equals(action)){ // 更新配置
			if(args.length()>0){
				String coorType = null;
				try {
					coorType = args.getString(0);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				mLocationClient.getLocOption().setCoorType(coorType);
			}
			if(args.length()>1){
				int span = 0;
				try {
					span = args.getInt(1);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				mLocationClient.getLocOption().setScanSpan(span);
			}
			return true;
		}
		return false;
	}



	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
//		this.callbackContextList.add(callbackContext);
		this.callbackContext = callbackContext;
		this.action = action;
		this.args = args;
		promtForLocation();
		return true;
	}

	@Override
	public void onStop() {
		Log.e("onStop","----------------onStop");
		// mLocationClient.stop();
		//开启后台定位
		if(mLocationClient.isStarted()){
			mLocationClient.enableLocInForeground(1, notification);
		}
		super.onStop();
	}


	@Override
	public void onStart() {
		Log.e("onStart","----------------onStart");
		//关闭后台定位（true：通知栏消失；false：通知栏可手动划除）
		mLocationClient.disableLocInForeground(true);
		if (mLocationClient!=null){
			mLocationClient.restart();
		}
		super.onStart();
	}

	@Override
	public void onDestroy() {
		Log.e("onDestroy","----------------onDestroy");
		// 关闭前台定位服务
		mLocationClient.disableLocInForeground(true);
		// 取消之前注册的 BDAbstractLocationListener 定位监听函数
		mLocationClient.unRegisterLocationListener(myListener);
		// 停止定位sdk
		mLocationClient.stop();

		super.onDestroy();
	}

	private void initLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
		option.setLocationPurpose(LocationClientOption.BDLocationPurpose.Sport); //运动场景定位
		option.setCoorType(mCoorType);// 可选，默认gcj02，设置返回的定位结果坐标系 bd09ll  返回国测局经纬度坐标系：gcj02 返回百度墨卡托坐标系 ：bd09 返回百度经纬度坐标系 ：bd09ll
		option.setScanSpan(mSpan);// 可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
		option.setIsNeedAddress(true);// 可选，设置是否需要地址信息，默认不需要
//		option.setOpenGps(true);// 可选，默认false,设置是否使用gps
		option.setLocationNotify(true);// 可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
		option.setIsNeedLocationDescribe(true);// 可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
		option.setIsNeedLocationPoiList(true);// 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
		option.setIgnoreKillProcess(false);// 可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
		option.SetIgnoreCacheException(false);// 可选，默认false，设置是否收集CRASH信息，默认收集
		option.setEnableSimulateGps(false);// 可选，默认false，设置是否需要过滤gps仿真结果，默认需要
		mLocationClient.setLocOption(option);

		//设置后台定位
		//android8.0及以上使用NotificationUtils
		if (Build.VERSION.SDK_INT >= 26) {
			mNotificationUtils = new NotificationUtils(cordova.getActivity());
			Notification.Builder builder2 = mNotificationUtils.getAndroidChannelNotification
					("提示", "正在运行");
			notification = builder2.build();
		} else {
			//获取一个Notification构造器
			Notification.Builder builder = new Notification.Builder(cordova.getActivity());
			Intent nfIntent = new Intent(cordova.getActivity(), cordova.getActivity().getClass());

			builder.setContentIntent(PendingIntent.
					getActivity(cordova.getActivity(), 0, nfIntent, 0)) // 设置PendingIntent
					.setContentTitle("提示") // 设置下拉列表里的标题
					//.setSmallIcon(R.drawable.ic_launcher) // 设置状态栏内的小图标
					.setContentText("正在运行") // 设置上下文内容
					.setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

			notification = builder.build(); // 获取构建好的Notification
		}
		//notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
	}

	public class MyLocationListener extends BDAbstractLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null) {
				return;
			}
			// Receive Location
			try {
				locationInfo = new JSONObject();
				locationInfo.put("time", location.getTime());
				locationInfo.put("locType", location.getLocType());
				locationInfo.put("latitude", location.getLatitude());
				locationInfo.put("longitude", location.getLongitude());
				locationInfo.put("radius", location.getRadius());
				locationInfo.put("coorType",location.getCoorType()); // 获取所用坐标系，坐标系为准(wgs84,gcj02,bd09,bd09ll)
				if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
					locationInfo.put("speed", location.getSpeed());// 单位：公里每小时
					locationInfo
							.put("satellite", location.getSatelliteNumber());
					locationInfo.put("height", location.getAltitude());// 单位：米
					locationInfo.put("direction", location.getDirection());// 单位：度
					locationInfo.put("addr", location.getAddrStr());
					locationInfo.put("province", location.getProvince());
					locationInfo.put("city", location.getCity());
					locationInfo.put("district", location.getDistrict());
					locationInfo.put("street", location.getStreet());
					locationInfo.put("describe", "gps定位成功");
				} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
					locationInfo.put("addr", location.getAddrStr());
					locationInfo.put("province", location.getProvince());
					locationInfo.put("city", location.getCity());
					locationInfo.put("district", location.getDistrict());
					locationInfo.put("street", location.getStreet());
					// 运营商信息
					locationInfo.put("operationers", location.getOperators());
					locationInfo.put("describe", "网络定位成功");
				} else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
					locationInfo.put("describe", "离线定位成功，离线定位结果也是有效的");
				} else if (location.getLocType() == BDLocation.TypeServerError) {
					locationInfo
							.put("describe",
									"服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
				} else if (location.getLocType() == BDLocation.TypeNetWorkException) {
					locationInfo.put("describe", "网络不通导致定位失败，请检查网络是否通畅");
				} else if (location.getLocType() == BDLocation.TypeCriteriaException) {
					locationInfo
							.put("describe",
									"无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
				}
				locationInfo.put("locationdescribe",
						location.getLocationDescribe());// 位置语义化信息
				// callbackContext.success(locationInfo); // 每调用一次，该方法前端只能执行一次
				PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, locationInfo);
				pluginResult.setKeepCallback(true);
				for (ActionModel actionModel: callbackContextList){
					actionModel.callbackContext.sendPluginResult(pluginResult);
					if(actionModel.isSingle){ // 如果时单次定位，移除该回调
						callbackContextList.remove(actionModel);
					}
				}
				// 发送位置数据变更
				// sendMsg(locationInfo.toString(),"onReceiveLocation");
			} catch (JSONException e) {
				e.printStackTrace();
				if(callbackContextList.size()>0){
					callbackContextList.get(callbackContextList.size()-1).callbackContext.error(e.toString());
				}
				// callbackContext.error(e.toString());
			}
		}
	}

	private void initGPS() {
		LocationManager locationManager = (LocationManager) activity
				.getSystemService(Context.LOCATION_SERVICE);
		// 判断GPS模块是否开启，如果没有则开启
		if (!locationManager
				.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)&& Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
			Toast toast = Toast.makeText(activity, "未打开位置开关，可能导致定位失败或定位不准，建议开启GPS",
					Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
	}

//	/**
//	 * 发送消息到
//	 * @param data
//	 * @param methodStr
//	 */
//	private  void sendMsg(String data,String methodStr){
//		String format = "navigator.baidulocation."+methodStr+"InAndroidCallback(%s);";
//		JSONObject jsonObject = new JSONObject();
//		try {
//			jsonObject.put("res",data);
//			final String js = String.format(format, jsonObject.toString());
//			cordova.getActivity().runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					instance.webView.loadUrl("javascript:" + js);
//				}
//			});
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//
//	}

	/**
	 * 存放 请求action的数据
	 */
	class ActionModel{
	  CallbackContext callbackContext; // 回调js 的上下文对象
	  boolean isSingle; // 是否时单次位置回调

		public ActionModel() {
		}

		public CallbackContext getCallbackContext() {
			return callbackContext;
		}

		public void setCallbackContext(CallbackContext callbackContext) {
			this.callbackContext = callbackContext;
		}

		public boolean isSingle() {
			return isSingle;
		}

		public void setSingle(boolean single) {
			isSingle = single;
		}
	}
}
