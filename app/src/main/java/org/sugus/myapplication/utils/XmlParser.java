package org.sugus.myapplication.utils;

import org.sugus.myapplication.entity.City;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

//解析文件中的城市名称
public class XmlParser extends DefaultHandler {

    private List<City> cities;
    private String tagName;

    public List<City> getProvinceList() {
        return cities;
    }

    @Override
    public void startDocument() throws SAXException {
        //super.startDocument();
        cities = new ArrayList<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //super.startElement(uri, localName, qName, attributes);
        this.tagName = localName;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        //super.characters(ch, start, length);
        String data = new String(ch, start, length);
        if(tagName != null){
            if(tagName.equals("item")){
                City city = new City(data);
                cities.add(city);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        //super.endElement(uri, localName, qName);
        this.tagName = null;
    }
}
