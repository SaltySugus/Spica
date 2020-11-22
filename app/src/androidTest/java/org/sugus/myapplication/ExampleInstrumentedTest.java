package org.sugus.myapplication;

import android.content.Context;
import android.content.res.AssetManager;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sugus.myapplication.entity.City;
import org.sugus.myapplication.utils.XmlParser;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("org.sugus.myapplication", appContext.getPackageName());
    }

    @Test
    public void parseXml() {
        //System.out.println("lol");
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        AssetManager assetManager = appContext.getAssets();
        try {
            InputStream is = assetManager.open("province_list.xml");
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XmlParser parser = new XmlParser();
            sp.parse(is, parser);
            is.close();

            List<City> provinceList = parser.getProvinceList();
            for(City province : provinceList){
                System.out.println(province);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}