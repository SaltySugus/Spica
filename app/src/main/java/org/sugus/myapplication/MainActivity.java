package org.sugus.myapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.weather.*;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import org.sugus.myapplication.adapter.WeatherAdapter;
import org.sugus.myapplication.utils.ToastUtil;
import org.sugus.myapplication.utils.Utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements WeatherSearch.OnWeatherSearchListener, AMapLocationListener {

    private WeatherSearchQuery mquery;
    private WeatherSearch mweathersearch;
    private LocalWeatherLive weatherlive;
    private LocalWeatherForecast weatherforecast;
    private List<LocalDayWeatherForecast> forecastlist = null;
    private String cityname = "北京市";//天气搜索的城市，可以写名称或adcode；
    private BottomSheetBehavior bottomSheetBehavior;
    LocalDayWeatherForecast todayWeather;
    private BottomSheetBehavior mBehavior;
    RelativeLayout mCLContentBottomSheet;

    //定位服务类。此类提供单次定位、持续定位、地理围栏、最后位置相关功能
    private AMapLocationClient locationClient;
//    private OnLocationChangedListener listener;
    //定位参数设置
    private AMapLocationClientOption clientOption;

    private TextView cityTemp;
    private TextView provideTime;
    private ImageView weatherImage;
    private TextView weatherText;
    private TextView dayWp;
    private TextView nightWp;
    private TextView dayDir;
    private TextView nightDir;
    private TextView dayWeather;
    private TextView nightWeather;
    private TextView dayTemp;


    public String getCityname() {
        return cityname;
    }

    public void setCityname(String cityname) {
        this.cityname = cityname;
    }

    //是否需要检测后台定位权限，设置为true时，如果用户没有给予后台定位权限会弹窗提示
    private boolean needCheckBackLocation = false;
    //如果设置了target > 28，需要增加这个权限，否则不会弹出"始终允许"这个选择框
    private static String BACK_LOCATION_PERMISSION = "android.permission.ACCESS_BACKGROUND_LOCATION";

    protected String[] needPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            BACK_LOCATION_PERMISSION
    };

    private static final int PERMISSON_REQUESTCODE = 0;

    /**
     * 判断是否需要检测，防止不停的弹框
     */
    private boolean isNeedCheck = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(Build.VERSION.SDK_INT > 28
                && getApplicationContext().getApplicationInfo().targetSdkVersion > 28) {
            needPermissions = new String[] {
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,
                    BACK_LOCATION_PERMISSION
            };
            needCheckBackLocation = true;
        }
        initLocation();
        startLocation();
        initView();
        dataReceiver();
        initBottomSheet();

        this.setTitle("正在定位");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.onClick();
            }
        });

//        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
//        initNavBar();

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    public void dataReceiver(){
        Intent intent = getIntent();
        String cityname = intent.getStringExtra("city_name");
        if(cityname != null && !"".equals(cityname)) {
            setCityname(cityname);
        }
    }

    public void onClick(){
        Intent intent = new Intent(this, CityMenuActivity.class);
        startActivity(intent);
//        this.finish();
    }

    public void initRecycle(List<LocalDayWeatherForecast> list){
        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        LinearLayoutManager layout = new LinearLayoutManager(this);
        layout.setOrientation(LinearLayoutManager.HORIZONTAL);//设置为横向排列
        recyclerView.setLayoutManager(layout);

        WeatherAdapter adapter = new WeatherAdapter(list);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String cityname = intent.getStringExtra("city_name");
        if(cityname != null && !"".equals(cityname)) {
            this.cityname = cityname;
        }
        this.setTitle(this.cityname);
        searchforcastsweather();
        searchliveweather();
    }

    private void initBottomSheet() {
        mCLContentBottomSheet = findViewById(R.id.ll_content_bottom_sheet);
        mBehavior = BottomSheetBehavior.from(mCLContentBottomSheet);
    }

    public void initView(){
        provideTime = findViewById(R.id.provideTime);
        cityTemp = findViewById(R.id.cityTemp);
        weatherImage = findViewById(R.id.weather);
        dayTemp = findViewById(R.id.temp);
        dayWp = findViewById(R.id.day_wp);
        dayWeather = findViewById(R.id.day_weather);
        dayDir = findViewById(R.id.day_dir);
        nightDir = findViewById(R.id.night_dir);
        nightWeather = findViewById(R.id.night_weather);
        nightWp = findViewById(R.id.night_wp);
        weatherText = findViewById(R.id.weather_text);
    }

    /**
     * 预报天气查询
     */
    private void searchforcastsweather() {
        mquery = new WeatherSearchQuery(cityname, WeatherSearchQuery.WEATHER_TYPE_FORECAST);//检索参数为城市和天气类型，实时天气为1、天气预报为2
        mweathersearch = new WeatherSearch(this);
        mweathersearch.setOnWeatherSearchListener(this);
        mweathersearch.setQuery(mquery);
        mweathersearch.searchWeatherAsyn(); //异步搜索
    }

    /**
     * 实时天气查询
     */
    private void searchliveweather() {
        mquery = new WeatherSearchQuery(cityname, WeatherSearchQuery.WEATHER_TYPE_LIVE);//检索参数为城市和天气类型，实时天气为1、天气预报为2
        mweathersearch = new WeatherSearch(this);
        mweathersearch.setOnWeatherSearchListener(this);
        mweathersearch.setQuery(mquery);
        mweathersearch.searchWeatherAsyn(); //异步搜索
    }

    /**
     * 实时天气查询回调
     */
    @Override
    public void onWeatherLiveSearched(LocalWeatherLiveResult weatherLiveResult, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (weatherLiveResult != null && weatherLiveResult.getLiveResult() != null) {
                weatherlive = weatherLiveResult.getLiveResult();
                cityTemp.setText(weatherlive.getTemperature() + "°");
                provideTime.setText(weatherlive.getReportTime() + "发布");
                weatherText.setText(weatherlive.getWeather());
                weatherImage.setImageResource(Utils.getWeatherResource(weatherlive.getWeather()));
            } else {
                ToastUtil.show(MainActivity.this, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(MainActivity.this, rCode);
        }
    }

    /**
     * 天气预报查询结果回调
     */
    @Override
    public void onWeatherForecastSearched(
            LocalWeatherForecastResult weatherForecastResult, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            //调用后获取到的是当日及未来三天预报
            if (weatherForecastResult != null && weatherForecastResult.getForecastResult() != null
                    && weatherForecastResult.getForecastResult().getWeatherForecast() != null
                    && weatherForecastResult.getForecastResult().getWeatherForecast().size() > 0) {
                weatherforecast = weatherForecastResult.getForecastResult();
                forecastlist = weatherforecast.getWeatherForecast();
                //获取当日天气
                todayWeather = forecastlist.remove(0);
                fillForecast();
                fillToday();

            } else {
                ToastUtil.show(MainActivity.this, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(MainActivity.this, rCode);
        }
    }

    private void fillToday() {
        dayWp.setText(todayWeather.getDayWindPower());
        dayDir.setText(todayWeather.getDayWindDirection());
        dayWeather.setText(todayWeather.getDayWeather());
        dayTemp.setText(todayWeather.getDayTemp() + "°/" +todayWeather.getNightTemp() + "°");

        nightWp.setText(todayWeather.getNightWindPower());
        nightWeather.setText(todayWeather.getNightWeather());
        nightDir.setText(todayWeather.getNightWindDirection());
    }

    private void fillForecast() {
        for (LocalDayWeatherForecast localForecast : forecastlist) {
            switch (Integer.parseInt(localForecast.getWeek())) {
                case 1:
                    localForecast.setWeek("周一");
                    break;
                case 2:
                    localForecast.setWeek("周二");
                    break;
                case 3:
                    localForecast.setWeek("周三");
                    break;
                case 4:
                    localForecast.setWeek("周四");
                    break;
                case 5:
                    localForecast.setWeek("周五");
                    break;
                case 6:
                    localForecast.setWeek("周六");
                    break;
                case 7:
                    localForecast.setWeek("周日");
                    break;
                default:
                    break;
            }
        }
        initRecycle(forecastlist);

    }

    @Override
    protected void onResume() {
        try{
            super.onResume();
            if (Build.VERSION.SDK_INT >= 23) {
                if (isNeedCheck) {
                    checkPermissions(needPermissions);
                }
            }
        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    /**
     * @param
     * @since 2.5.0
     */
    @TargetApi(23)
    private void checkPermissions(String... permissions) {
        try{
            if (Build.VERSION.SDK_INT >= 23 && getApplicationInfo().targetSdkVersion >= 23) {
                List<String> needRequestPermissonList = findDeniedPermissions(permissions);
                if (null != needRequestPermissonList
                        && needRequestPermissonList.size() > 0) {
                    try {
                        String[] array = needRequestPermissonList.toArray(new String[needRequestPermissonList.size()]);
                        Method method = getClass().getMethod("requestPermissions", new Class[]{String[].class, int.class});
                        method.invoke(this, array, 0);
                    } catch (Throwable e) {

                    }
                }
            }

        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     */
    @TargetApi(23)
    private List<String> findDeniedPermissions(String[] permissions) {
        try{
            List<String> needRequestPermissonList = new ArrayList<String>();
            if (Build.VERSION.SDK_INT >= 23 && getApplicationInfo().targetSdkVersion >= 23) {
                for (String perm : permissions) {
                    if (checkMySelfPermission(perm) != PackageManager.PERMISSION_GRANTED
                            || shouldShowMyRequestPermissionRationale(perm)) {
                        if(!needCheckBackLocation
                                && BACK_LOCATION_PERMISSION.equals(perm)) {
                            continue;
                        }
                        needRequestPermissonList.add(perm);
                    }
                }
            }
            return needRequestPermissonList;
        }catch(Throwable e){
            e.printStackTrace();
        }
        return null;
    }

    private int checkMySelfPermission(String perm) {
        try {
            Method method = getClass().getMethod("checkSelfPermission", new Class[]{String.class});
            Integer permissionInt = (Integer) method.invoke(this, perm);
            return permissionInt;
        } catch (Throwable e) {
        }
        return -1;
    }

    private boolean shouldShowMyRequestPermissionRationale(String perm) {
        try {
            Method method = getClass().getMethod("shouldShowRequestPermissionRationale", new Class[]{String.class});
            Boolean permissionInt = (Boolean) method.invoke(this, perm);
            return permissionInt;
        } catch (Throwable e) {
        }
        return false;
    }

    /**
     * 检测是否说有的权限都已经授权
     *
     * @param grantResults
     * @return
     * @since 2.5.0
     */
    private boolean verifyPermissions(int[] grantResults) {
        try{
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }catch(Throwable e){
            e.printStackTrace();
        }
        return true;
    }

    @TargetApi(23)
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] paramArrayOfInt) {
        try{
            if (Build.VERSION.SDK_INT >= 23) {
                if (requestCode == PERMISSON_REQUESTCODE) {
                    if (!verifyPermissions(paramArrayOfInt)) {
                        showMissingPermissionDialog();
                        isNeedCheck = false;
                    }
                }
            }
        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    /**
     * 显示提示信息
     *
     * @since 2.5.0
     */
    private void showMissingPermissionDialog() {
        try{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示");
            builder.setMessage("当前应用缺少必要权限。\n\n请点击\"设置\"-\"权限\"-打开所需权限");

            // 拒绝, 退出应用
            builder.setNegativeButton("取消",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try{
                                finish();
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    });

            builder.setPositiveButton("设置",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                startAppSettings();
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    });

            builder.setCancelable(false);

            builder.show();
        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    /**
     * 启动应用的设置
     *
     * @since 2.5.0
     */
    private void startAppSettings() {
        try{
            Intent intent = new Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if(aMapLocation != null){
            if(aMapLocation.getErrorCode() == 0){
                setCityname(aMapLocation.getCity());
                stopLocation();
                this.setTitle(getCityname());
                searchforcastsweather();
                searchliveweather();
            }else{
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError","location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }

    }

    //初始化定位信息
    public void initLocation(){
        //初始化定位
        locationClient = new AMapLocationClient(this);
        //设置定位回调监听
        locationClient.setLocationListener(this);
        //初始化AMapLocationClientOption对象
        clientOption = new AMapLocationClientOption();

        //设置定位模式
        clientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);

        //仅获取一次定位
        clientOption.setOnceLocation(true);
        //获取最近3s内精度最高的一次定位
        clientOption.setOnceLocationLatest(true);

        if(null != locationClient){
            locationClient.setLocationOption(clientOption);
            locationClient.stopLocation();
            locationClient.startLocation();
        }
    }

    public void startLocation(){
        if(locationClient != null){
            locationClient.startLocation();
        }
    }

    public void stopLocation(){
        if(locationClient != null){
            locationClient.stopLocation();
        }
    }
}