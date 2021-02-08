package com.sail.sailright2;

import android.os.Environment;
import android.util.Log;

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

public class Courses {

    ArrayList<Course> courses = new ArrayList<>();


    // Parse the courses.gpx file to an ArrayList of name and route
  public void parseXML() throws IOException {
        String appDirectory = "SailRight";

        File dir = new File(Environment.getExternalStorageDirectory(), appDirectory);
        File courseFile = new File(dir, "courses.gpx");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;

        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(courseFile);
            doc.getDocumentElement().normalize();

            Log.e("Root_element courses" , doc.getDocumentElement().getNodeName());
            NodeList nodeList = doc.getElementsByTagName("course");

            courses = new ArrayList<Course>();

            for (int i = 0; i < nodeList.getLength(); i++) {

                Element element = (Element) nodeList.item(i);

                // Create an ArrayList of courses names and routes from the parsed marks.gpx file
                Course model = new Course();
                model.setCourseName(element.getAttribute("name"));
                model.setCourseRoute(element.getAttribute("route"));
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
                course =  (ArrayList) new ArrayList<String>(Arrays.asList(courseString.split(",")));
            }
        }
        return course;
    }
}

