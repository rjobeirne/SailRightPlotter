package com.sail.sailrightplotter;

import android.os.Environment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Courses1 {

    ArrayList<Course> courses = new ArrayList<>();


    // Parse the courses.gpx file to an ArrayList of name and route
  public void parseXML() throws IOException {
        String appDirectory = "SailRight";

        File dir = new File(Environment.getExternalStorageDirectory(), appDirectory);
        File courseFile = new File(dir, "courses1.gpx");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;

        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(courseFile);
            doc.getDocumentElement().normalize();

//            Log.e("Root_element courses" , doc.getDocumentElement().getNodeName());
            NodeList nodeList = doc.getElementsByTagName("course");

            courses = new ArrayList<Course>();

            for (int i = 0; i < nodeList.getLength(); i++) {

                Element element = (Element) nodeList.item(i);

                // Create an ArrayList of courses names and routes from the parsed marks.gpx file
                Course model = new Course();
                model.setCourseName(element.getAttribute("name"));
                model.setCourseRoute(element.getAttribute("route"));
                model.setCourseRounding(element.getAttribute("rounding"));
                model.setCourseDist(element.getAttribute("dist"));
                courses.add(model);
            }

        } catch (SAXException | ParserConfigurationException | IOException e1) {
            e1.printStackTrace();
        }
    }

    // Find the route of the selected course
    public ArrayList getCourse(String selectedCourse) {
        ArrayList course = new ArrayList();

        String tryCourse;
        String courseString;

        for (int j = 0; j < courses.size(); j++) {
            tryCourse = courses.get(j).getCourseName();

            if (tryCourse.equals(selectedCourse)) {
                courseString = courses.get(j).getCourseRoute();

                // Convert string to ArrayList
                course = new ArrayList<String>(Arrays.asList(courseString.split(",")));
            }
        }
        return course;
    }

    // Find the roundings for the selected course
    public ArrayList getRounding(String selectedCourse) {
        ArrayList round = new ArrayList();

        String tryRound;
        String roundingString;

        for (int j = 0; j < courses.size(); j++) {
            tryRound = courses.get(j).getCourseName();

            if (tryRound.equals(selectedCourse)) {
                roundingString = courses.get(j).getCourseRounding();

                // Convert string to ArrayList
                round = new ArrayList<String>(Arrays.asList(roundingString.split(",")));
            }
        }
        return round;
    }

    // Find the distance for the selected course
    public String getCourseDist(String selectedCourse) {

        String courseDist = "-";
        String tryDist;

        for (int j = 0; j < courses.size(); j++) {
            tryDist = courses.get(j).getCourseName();

            if (tryDist.equals(selectedCourse)) {
                courseDist = courses.get(j).getCourseDist();
            }
        }
        return courseDist;
    }
}

