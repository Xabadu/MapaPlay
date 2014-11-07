package net.medialabs.functions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Jarvis {
	
	public static int getAge(String dob) {

	    if (dob == null || dob.length() < 10) {
	        return -1;
	    }
	    //take substrings of the dob so split out year, month and day
	    int yearDOB = Integer.parseInt(dob.substring(6, 10));
	    int monthDOB = Integer.parseInt(dob.substring(0, 2));
	    int dayDOB = Integer.parseInt(dob.substring(3, 5));

	    //calculate the current year, month and day into separate variables
	    DateFormat dateFormat = new SimpleDateFormat("yyyy");
	    java.util.Date date = new java.util.Date();
	    int thisYear = Integer.parseInt(dateFormat.format(date));

	    //if the current month is less than the dob month
	    //then reduce the dob by 1 as they have not had their birthday yet this year
	    dateFormat = new SimpleDateFormat("MM");
	    date = new java.util.Date();
	    int thisMonth = Integer.parseInt(dateFormat.format(date));

	    dateFormat = new SimpleDateFormat("dd");
	    date = new java.util.Date();
	    int thisDay = Integer.parseInt(dateFormat.format(date));
	   
	    //create an age variable to hold the calculated age
	    //to start will  set the age equal to the current year minus the year of the dob
	    int age = thisYear - yearDOB;

	    if(thisMonth < monthDOB){
	    age = age-1;
	    }

	    if(thisMonth == monthDOB && thisDay < dayDOB){
	    age = age-1;
	    }

	    return age;
	}
	
}
