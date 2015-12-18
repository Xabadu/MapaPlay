package net.medialabs.functions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Jarvis {

	public static int getAge(String dob) {

	    if (dob == null || dob.length() < 10) {
	        return -1;
	    }
	    int yearDOB = Integer.parseInt(dob.substring(6, 10));
	    int monthDOB = Integer.parseInt(dob.substring(0, 2));
	    int dayDOB = Integer.parseInt(dob.substring(3, 5));

	    DateFormat dateFormat = new SimpleDateFormat("yyyy");
	    java.util.Date date = new java.util.Date();
	    int thisYear = Integer.parseInt(dateFormat.format(date));

	    dateFormat = new SimpleDateFormat("MM");
	    date = new java.util.Date();
	    int thisMonth = Integer.parseInt(dateFormat.format(date));

	    dateFormat = new SimpleDateFormat("dd");
	    date = new java.util.Date();
	    int thisDay = Integer.parseInt(dateFormat.format(date));

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
