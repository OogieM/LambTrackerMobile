package com.weyr_associates.lambtracker;


/**
 * Write a description of class Utilities here.
 * 
 * @author ww 
 * @version 2013-02-15
 */
public class Utilities 
{
    public static int    JGREG        = 15 + 31*(10+12*1582);   // 15 October 1582
    public static double SECS_PER_DAY = 86400.0;
    public static double HALFSECOND   = 0.5;

    /**
     * Constructor for objects of class Utilities
     */
    public Utilities()
    {

    }

   public static double toJulian( int[] ymd )
    {
    int year       = ymd[0]; // yyyy
    int month      = ymd[1]; // jan=1, feb=2,...
    int day        = ymd[2]; // 1 - 31
    int julianYear = year;
    
    if( year < 0 )  // 10 BCE => -10
        julianYear++;
    
    int julianMonth = month;
    
    if( month > 2 )
        julianMonth++;
    
    else
        {
        julianYear--;
        julianMonth += 13;
        }

    double julian = (java.lang.Math.floor(365.25 * julianYear)
                    + java.lang.Math.floor(30.6001*julianMonth) + day + 1720995.0);
    
    // Gregorian Calendar adopted Oct. 15, 1582 (2299161)
    if( day + 31 * (month + 12 * year) >= JGREG )
        {
        // change over to Gregorian calendar
        int ja  = (int)(0.01 * julianYear);
        julian += 2 - ja + (0.25 * ja);
        }
    
    return java.lang.Math.floor(julian) - 0.5;    // start of civil day 0h UTC
    }

public double toJulianWithTime( int[] ymdhms )
   {
   double jd  = toJulian( ymdhms );
   int    hh  = ymdhms[3],
          mm  = ymdhms[4],
          ss  = ymdhms[5];
   return jd + (hh * 3600 + mm * 60 + ss) / SECS_PER_DAY;
   }


 /**
* Converts a Julian day to a calendar date
* ref :
* Numerical Recipes in C, 2nd ed., Cambridge University Press 1992
*/
public int[] fromJulian( double injulian )
    {
    int    jalpha, ja, jb, jc, jd, je, year,month, day;
    double julian = injulian + (HALFSECOND / 86400.0);
    ja = (int) (julian + 0.5);
    
    if( ja >= JGREG )
        {    
        jalpha = (int) (((ja - 1867216) - 0.25) / 36524.25);
        ja = ja + 1 + jalpha - jalpha / 4;
        }

    jb    = ja + 1524;
    jc    = (int) (6680.0 + ((jb - 2439870) - 122.1) / 365.25);
    jd    = 365 * jc + jc / 4;
    je    = (int) ((jb - jd) / 30.6001);
    day   = jb - jd - (int) (30.6001 * je);
    month = je - 1;
    
    if( month > 12 )
        month = month - 12;
    
    year = jc - 4715;
    
    if( month > 2 )
        year--;
    
    if( year <= 0 )
        year--;

    return new int[] {year, month, day};
    }

public int[] fromJulianWithTime( double julian )
    {
    double jd = julian + (HALFSECOND / 86400.0);
    jd = Math.floor( julian + 0.5 ) - 0.5;
    double time = (julian + (HALFSECOND / 86400.0)) - jd;
    int[]  date = fromJulian( jd );
    int    secs = (int) Math.floor( SECS_PER_DAY * time );
    int    hrs  = secs / 3600;
    secs -= hrs * 3600;
    int mins = secs / 60;
    secs -= mins * 60;
    return new int[] { date[0], date[1], date[2], hrs, mins, secs };
    }


}
