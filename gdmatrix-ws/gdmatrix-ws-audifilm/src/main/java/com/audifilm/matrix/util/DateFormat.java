package com.audifilm.matrix.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DateFormat: classe "Thread Safe" per formatar dates en format "yyyyMMdd"
 * *
 * @author comasfc
 */
public class DateFormat {

  static public final String DateFormatPattern = "yyyyMMdd";
  static public final String TimeFormatPattern = "HHmmss";
  static public final String TimeStampFormatPattern = "yyyyMMddHHmmss";
  static public final String SDEVaribleDateFormatPattern = "dd/MM/yyyy";
  static public final String SDEVaribleTimeFormatPattern = "HH:mm:ss";

  private java.text.DateFormat dateFormat;
  private java.text.DateFormat timeFormat;
  private java.text.DateFormat timestampFormat;
  private java.text.DateFormat sdeVariableDateFormat;

  private final Object lock = new Object();

  static private DateFormat instance = new DateFormat();

  static public DateFormat getInstance() {
    return instance;
  }

  private java.text.DateFormat getDateFormat() {
    if (dateFormat==null)
      dateFormat = new SimpleDateFormat(DateFormatPattern);

    return dateFormat;
  }
  private java.text.DateFormat getTimeFormat() {
    if (timeFormat==null)
      timeFormat = new SimpleDateFormat(TimeFormatPattern);
    return timeFormat;
  }
  private java.text.DateFormat getTimestampFormat() {
    if (timestampFormat==null)
      timestampFormat = new SimpleDateFormat(TimeStampFormatPattern);
    return timestampFormat;
  }
  private java.text.DateFormat getSdeVariableDateFormat() {
    if (sdeVariableDateFormat==null)
      sdeVariableDateFormat = new SimpleDateFormat(SDEVaribleDateFormatPattern);
    return sdeVariableDateFormat;
  }

  public String today() {
    synchronized(lock) {
      return getDateFormat().format(new Date());
    }
  }
  
  public String now() {
    synchronized(lock) {
      return getTimestampFormat().format(new Date());
    }
  }

  public Date parse(String source) throws ParseException {
    if (source==null) return null;
    String str=source.trim();
    if (str.length() == DateFormatPattern.length()) {
      synchronized(lock) {
        return getDateFormat().parse(str);
      }
    }
    if (str.length() == TimeFormatPattern.length()) {
      synchronized(lock) {
        return getTimeFormat().parse(str);
      }
    }
    if (str.length() == TimeStampFormatPattern.length()) {
      synchronized(lock) {
        return getTimestampFormat().parse(str);
      }
    }
    synchronized(lock) {
      return getSdeVariableDateFormat().parse(str);
    }
  }

  public String formatDate(Date date) {
    synchronized(lock) {
      return getDateFormat().format(date);
    }
  }

  public String formatTime(Date date) {
    synchronized(lock) {
      return getTimeFormat().format(date);
    }
  }

  public String formatTimestamp(Date date) {
    synchronized(lock) {
      return getTimestampFormat().format(date);
    }
  }

  public String formatVariableToPropertyValue(String value)
  {
    try {
      synchronized(lock) {
        return getDateFormat().format( getSdeVariableDateFormat().parse(value) );
      }
    } catch(Exception ex) {
      System.err.println("Error formating property value:" + value);
      ex.printStackTrace();
    }
    return value;
  }

  public String parsePropertyToVariableValue(String value)
  {
    try {
      synchronized(lock) {
        return getSdeVariableDateFormat().format(getDateFormat().parse(value));
      }
    } catch(Exception ex) {
      System.err.println("Error parsing property value:" + value);
      ex.printStackTrace();
    }
    return value;
  }


 static public void main(String args[]) throws ParseException {

    long secondsInAYear = 3600 * 24 * 365 * 1000;
    long time = Math.round((System.currentTimeMillis() + Math.random() * secondsInAYear)/1000) * 1000;
    Date date = new Date( time );

    String str = DateFormat.getInstance().formatTimestamp(date);
    Date strDate = DateFormat.getInstance().parse(str);

    if (strDate.getTime() == date.getTime()) {
      System.out.println(" ok " + date + " " + str + " " + strDate + " " + time + " " + date.getTime() + " " + strDate.getTime());
    } else {
      System.err.println(" KO " + date + " " + str + " " + strDate + " " + time + " " + date.getTime() + " " + strDate.getTime());
    }

  }
}
