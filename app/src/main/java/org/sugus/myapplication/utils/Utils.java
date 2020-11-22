package org.sugus.myapplication.utils;

import android.content.Context;
import android.content.res.AssetManager;
import org.sugus.myapplication.R;
import org.sugus.myapplication.entity.City;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.List;

public class Utils {
    public static int getWeatherResource(String weather){
        switch (weather){
            case "晴":
                return R.drawable.qing;
            case "阴":
                return R.drawable.yin;
            case "多云":
                return R.drawable.duoyun;
            case "大雨":
                return R.drawable.dayu;
            case "雾":
                return R.drawable.wu;
            case "小雨":
                return R.drawable.xiaoyu;
            case "阵雨":
                return R.drawable.zhenyu;
            case "雷阵雨":
                return R.drawable.leizhenyu;
            case "暴雨":
                return R.drawable.baoyu;
            case "大暴雨":
                return R.drawable.dabaoyu;
            case "特大暴雨":
                return R.drawable.tedabaoyu;
            case "雨夹雪":
                return R.drawable.yujiaxue;
            case "中雨":
                return R.drawable.zhongyu;
            case "小雪":
                return R.drawable.xiaoxue;
            case "中雪":
                return R.drawable.zhongxue;
            case "大雪":
                return R.drawable.daxue;
            case "阵雪":
                return R.drawable.zhenxue;
            case "霾":
                return R.drawable.mai;
            case "沙尘暴":
                return R.drawable.shachenbao;
            default:
                return 0;
        }
    }

    public static List<City> getXMLInfo(Context context){
        AssetManager assetManager = context.getAssets();
        try {
            InputStream is = assetManager.open("province_list.xml");
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XmlParser parser = new XmlParser();
            sp.parse(is, parser);
            is.close();
            return parser.getProvinceList();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
