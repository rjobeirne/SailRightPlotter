# SailRight
An android app to navigate around the Royal Melbourne Yacht Squadron pursuit courses. The app includes the courses and mark locations as specified in the Sailing Instructions V6.0. Some mark locations have been updated from a club survey conducted May 2020.

## Operation
The app uses the devices GPS system to determine the speed, heading and location of the boat. It only operates in a dashboard fashion providing only numerical data and not any graphical or map information. It operates in one of two modes:
### Start Mode
A countdown clock is provided for timing the start. The time to reach the start line is calculated from the boat's speed, location and where the boat will cross the line based on current heading. A warning tone (airhorn) is given at every minute of the countdown. A shotgun sound is heard when the clock reaches zero and the screen changes to Race Mode and the first mark of the course is set as the destination. If the device calculates the boat will cross the line more than a preset time after the alloted start time (default 15 seconds) a disappointment tone (wah wah) will sound. A button is provide to manually return the app to Race Mode if needed.
### Race Mode
When first started the app will open into Race Mode. Here the course can be selected by scrolling through all the courses. The course can then be reviewed by scrolling through all the marks for that course. For each course the first destination is 'Start' which acts as a button to switch the device into Start Mode. When the device is returned to Race Mode from Start Mode it will display the name of the next mark, the bearing and distance to the mark, the boat's heading and speed and the time to reach the mark. It also calculates the variance of the boat's heading to the mark bearing to assist in finding lay lines. As the boat approaches within a preset distance to the mark (default 50m) a warning sound (klaxon) is given and the next mark is progressed to the next in the course.

In all course the last mark is 'Finish'. The actual position of the finish is where the boat's current heading will intersect with the finish line. Distance and time are determined from this point. When the boat has crossed the line a celebatory tone is given (woop woop).
## Other Info
### Courses and Marks
On installation of the app a directory called SailRight is created in the internal shared storage area of the device (e.g. /storage/sdcard/SailRight) and the courses and marks files copied into this directory. The app then uses these files for the data. These files can be edited withe any text editor to reflect any changes in the courses or mark locations. They will not be overwritten, so to revert to the original files delete the files or the directory and restart the app.
### Smoothing
The app calls for updated GPS data as frequently as possible which is about 1 per second. The GPS data from mobile devices can be somewhat erratic. To counter this the actual speeds and boat headings are calculated by averaging the last preset number of readings (default 4). Increasing this number will provide less scatter but slower response. These smoothed figures are used in other calculations.
### Settings
Access to the settings page is only available when the course is showing 'RMYS'. To enter the settings page a long press of the spanner and screwdriver icon is requred to prevent inadvertent pressing. Settings allow changes to speed and heading smoothing factors, proximity alert distance, auto advance to next mark and default start time for the start countdown clock. Individual notification tone can be stopped.
### Screen Orientation
The app is only intented to operate in landscape orientation and it is locked that way.
### Navigation Bars
To maximise screen space the navigation bars will appear only temporarily. In order to acces them it is necessary to either swipe left from the right or swipe down from the top , and they will be visible for about 2 seconds. To exit the app, swipe left from the right and tap the back symbol **twice**.
