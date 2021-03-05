package com.sail.sailright2new;

import android.location.Location;
import android.os.Environment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


// Method from https://medium.com/@ssaurel/parsing-xml-data-in-android-apps-71ef607fbb16

public class Marks {

    ArrayList<Mark> marks = new ArrayList<>();
    ArrayList listNames = new ArrayList();

    // Parse the marks.gpx file to an ArrayList of name and
  public void parseXML() throws IOException {
        String appDirectory = "SailRight";

        File dir = new File(Environment.getExternalStorageDirectory(), appDirectory);
        File yourFile = new File(dir, "marks.gpx");
//      InputStream yourFile = getAssets().open("marks.xml");


        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;

        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(yourFile);
            doc.getDocumentElement().normalize();

//            Log.e("Root_element" , doc.getDocumentElement().getNodeName());
            NodeList nodeList = doc.getElementsByTagName("mark");

            marks = new ArrayList<Mark>();
            listNames = new ArrayList<>();
            String markLat, markLon;

            for (int i = 0; i < nodeList.getLength(); i++) {

                Element element = (Element) nodeList.item(i);

                // Create an ArrayList of mark names and coords from the parsed marks.gpx file
                Mark model = new Mark();
                model.setmarkName(element.getAttribute("name"));
                markLat = "-" + String.valueOf(Double.parseDouble(element.getAttribute("lat_deg")) +
                        Double.parseDouble(element.getAttribute("lat_min"))/60 );
                markLon = String.valueOf(Double.parseDouble(element.getAttribute("lon_deg")) +
                        Double.parseDouble(element.getAttribute("lon_min"))/60) ;

                model.setMarkLat(markLat);
                model.setMarkLon(markLon);
                marks.add(model);
                listNames.add(element.getAttribute("name"));
            }

        } catch (SAXException | ParserConfigurationException | IOException e1) {
            e1.printStackTrace();
        }
    }

    // Find the coordinates of the next mark
    public Location getNextMark(String nextMark) {

        Location nextMarkLoc = new Location("");
        String tryMark;

        for (int i = 0; i < marks.size(); i++) {
            tryMark = marks.get(i).getmarkName();

            if (tryMark.equals(nextMark)) {
                nextMarkLoc.setLatitude(Double.parseDouble(marks.get(i).getMarkLat()));
                nextMarkLoc.setLongitude(Double.parseDouble(marks.get(i).getMarkLon()));
                nextMarkLoc.setTime(Calendar.getInstance().getTimeInMillis());
            break;
            }
//                Log.e("***** try this mark", marks.get(i).getmarkName());
        }
//        Log.e("*** nextMark =", nextMark);
//        Log.e("****nextMarkLoc =", nextMark + " " + String.valueOf(nextMarkLoc));

        return nextMarkLoc;
    }
}

