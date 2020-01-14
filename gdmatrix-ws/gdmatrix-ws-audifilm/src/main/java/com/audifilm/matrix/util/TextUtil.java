/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.audifilm.matrix.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author comasfc
 */
public class TextUtil
{
  static public String toString(Object pojo)  {
    StringBuffer strBuf = new StringBuffer();

    Class cls = pojo.getClass();
    Field [] fields = cls.getDeclaredFields();
    strBuf.append(cls.getName() + "{");
    Arrays.sort(fields,  new Comparator() {

      public int compare(Object o1, Object o2)
      {
        return ((Field)o1).getName().compareTo(((Field)o2).getName());
      }

    });
    for(Field f : fields) {
      try
      {
        Method m = cls.getMethod("get" + f.getName().substring(0,1).toUpperCase() + f.getName().substring(1));
        Object result = m.invoke(pojo);
        strBuf.append(f.getName() + "=\"" + (result==null?"[null]":result.toString())+ "\"; ");
      }
      catch(Exception ex)
      {
        //Logger.getLogger(TextUtil.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    strBuf.append("}");
    return strBuf.toString();

  }

  static public String addPercent(String text)
  {
    if (text == null) return null;
    if ("".equals(text)) return "";
    return "%" + text.toUpperCase() + "%";
  }

  static public String likePattern(String pattern)
  {
    if (pattern==null || pattern.length() == 0) return null;
    StringBuffer buffer = new StringBuffer("% ");
    if (pattern.startsWith("\"") && pattern.endsWith("\""))
    {
      pattern = pattern.substring(1);
      pattern = pattern.substring(0, pattern.length() - 1);
      buffer.append(pattern);
    }
    else
    {
      char[] carray = pattern.toCharArray();
      for (int i = 0; i < carray.length; i++)
      {
        char c = Character.toLowerCase(carray[i]);
        if (c == 'a' || c == 'à' || c == 'á' || c == 'ä' ||
          c == 'e' || c == 'è' || c == 'é' ||
          c == 'i' || c == 'ì' || c == 'í' ||
          c == 'o' || c == 'ò' || c == 'ó' ||
          c == 'u' || c == 'ù' || c == 'ú' ||
          c == 'ü' || c == 'ï' || c == 'ç')
          {
            c = '_';
          }
        carray[i] = c;
      }
      buffer.append(carray);
    }
    buffer.append("%");
    return buffer.toString().toUpperCase();
  }

  static public boolean matches(String patternStr, String inputStr) {
    if (patternStr==null || patternStr.equals("")) return false;

    // Compile regular expression
    Pattern pattern = Pattern.compile(patternStr);

    // Determine if there is an exact match
    Matcher matcher = pattern.matcher(inputStr);
    return matcher.matches();

  }

  static public boolean matchesFilter(String filter, String value) {
    if (filter==null || filter.equals("")) return true;
    if (value==null) return false;

    // Compile regular expression
    String patternStr = filter.replaceAll("%", ".*");
    Pattern pattern = Pattern.compile(patternStr);

    // Determine if there is an exact match
    Matcher matcher = pattern.matcher(value);
    return matcher.matches();
  }

  static String encodeChar0(String cadena)
  {
    if (cadena==null) return null;
    if (cadena.length()==0) return "";
    if (cadena.charAt(0) == 0) return "";
    return cadena.replaceAll("\u0000", "");
  }

  static public String encodeEmpty(Object cadena)
  {
    if (cadena==null) return null;
    return encodeEmpty(cadena.toString());
  }

  static public String encodeEmpty(String cadena)
  {
    if (cadena==null || cadena.length()==0 ) return "";
    return cadena.replaceAll("\u0000", " ");
  }

  static public String toStringDateTime(Date date)
  {
    return DateFormat.getInstance().formatTimestamp(date);
  }

  static public String toStringDate(Date date)
  {
    return DateFormat.getInstance().formatDate(date);
  }

  static public String toStringTime(Date date)
  {
    return DateFormat.getInstance().formatTime(date);
  }

  static public String toStringDateTime()
  {
    return DateFormat.getInstance().now();
  }

  static public String toStringDate()
  {
    return DateFormat.getInstance().today();
  }

  static public String toStringTime()
  {
    return DateFormat.getInstance().formatTime(new Date());
  }

  static public String formatAlineatEsq(int number, int length)
  {
    return String.format("%0" + length + "d", number);
  }


}
