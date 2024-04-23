/*
 * GDMatrix
 *  
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *  
 * This program is licensed and may be used, modified and redistributed under 
 * the terms of the European Public License (EUPL), either version 1.1 or (at 
 * your option) any later version as soon as they are approved by the European 
 * Commission.
 *  
 * Alternatively, you may redistribute and/or modify this program under the 
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either  version 3 of the License, or (at your option) 
 * any later version. 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *    
 * See the licenses for the specific language governing permissions, limitations 
 * and more details.
 *    
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along 
 * with this program; if not, you may find them at: 
 *    
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/ 
 * and 
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.util;

import java.text.BreakIterator;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author unknown
 */
public class TextUtils
{
  private static final Pattern REMOVE_TAGS = Pattern.compile("<.+?>");
  
  public static String leftPadding(String text, String padding)
  {
    if (text == null) return null;
    if (text.length() == 0) return null;
    String tp = padding + text;
    int index = tp.length() - padding.length();
    if (index >= 0) tp = tp.substring(index);
    return tp;
  }

  public static String lowerLeftPadding(String text, String padding)
  {
    String s = leftPadding(text, padding);
    return s == null ? null : s.toLowerCase();
  }

  public static String upperLeftPadding(String text, String padding)
  {
    String s = leftPadding(text, padding);
    return s == null ? null : s.toUpperCase();
  }

  public static String replaceTabAndCRWithBlank(String text)
  {
    StringBuilder buffer = new StringBuilder();
    boolean blank = false;
    for (int i = 0; i < text.length(); i++)
    {
      char ch = text.charAt(i);
      if (ch == '\n' || ch == '\r' || ch == '\t' || ch == ' ')
      {
        if (!blank) buffer.append(' ');
        blank = true;
      }
      else
      {
        buffer.append(ch);
        blank = false;
      }
    }
    return buffer.toString();
  }

  public static String replaceSpecialChars(String text)
  {
    text = text.replace('‘','\'');
    text = text.replace('’','\'');
    text = text.replace('“','"');
    text = text.replace('”','"');
    text = text.replace('•', '·');
    text = text.replace('€', 'e');
    text = text.replace('´', '\'');
    text = text.replace('`', '\'');
    return text;
  }

  public static String collectionToString(Collection values)
  {
    return collectionToString(values, ",");
  }

  public static String collectionToString(Collection values, String separator)
  {
    String resultString = null;
    if (values != null && values.size() > 0)
    {
      StringBuilder sb = new StringBuilder();
      for (Object value : values)
      {
        if (value != null)
          sb.append(value.toString());
        sb.append(separator);
      }
      sb.deleteCharAt(sb.lastIndexOf(separator));
      resultString = sb.toString();
    }
    return resultString;
  }

  public static List<String> stringToList(String values, String separator)
  {
    List<String> result = null;
    if (values != null && values.length() > 0)
    {
      result = new ArrayList();
      String[] splitValues = values.split(separator);
      result.addAll(Arrays.asList(splitValues));
    }
    return result;
  }

  public static String listToJSArray(List<String> list)
  {
    if (list == null) return null;

    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < list.size(); i++)
    {
      sb.append("\"").append(list.get(i)).append("\"");
      if (i + 1 < list.size()) sb.append(",");
    }
    sb.append("]");
    return sb.toString();
  }

  public static Date parseInternalDate(String dateString)
  {
    if (dateString == null) return null;
    String pattern = (dateString.trim().length() == 8) ?
        "yyyyMMdd" : "yyyyMMddHHmmss";
    try
    {
      SimpleDateFormat df = new SimpleDateFormat(pattern);
      return df.parse(dateString);
    }
    catch (ParseException ex)
    {
      return null;
    }
  }

  public static Date parseUserDate(String dateString, String pattern)
  {
    return parseUserDate(dateString, pattern, true);
  }

  public static Date parseUserDate(String dateString, String pattern,
    boolean lenient)
  {
    if (dateString == null) return null;
    try
    {
      SimpleDateFormat df = new SimpleDateFormat(pattern);
      df.setLenient(lenient);
      return df.parse(dateString);
    }
    catch (ParseException ex)
    {
      return null;
    }
  }

  public static Date parseUnknownDate(String dateString)
  {
    SimpleDateFormat testFormat;

    try
    {
      testFormat =
        new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
      return testFormat.parse(dateString);
    }
    catch (Exception ex) { }

    try
    {
      testFormat =
        new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
      return testFormat.parse(dateString);
    }
    catch (Exception ex) { }

    try
    {
      String auxDateString = dateString.substring(0, 23) + "GMT" +
        dateString.substring(23, dateString.length());
      testFormat =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz", Locale.ENGLISH);
      return testFormat.parse(auxDateString);
    }
    catch (Exception ex) { }

    try
    {
      testFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy",
        Locale.ENGLISH);
      return testFormat.parse(dateString);
    }
    catch (Exception ex) { }

    try
    {
      testFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
        Locale.ENGLISH);
      testFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      return testFormat.parse(dateString);
    }
    catch (Exception ex) { }

    try
    {
      testFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ",
        Locale.ENGLISH);
      testFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      return testFormat.parse(dateString);
    }
    catch (Exception ex) { }

    try
    {
      testFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
      return testFormat.parse(dateString);
    }
    catch (Exception ex) { }

    try
    {
      testFormat =
        new SimpleDateFormat("EEE, d MMM yyyy HH:mm Z", Locale.ENGLISH);
      return testFormat.parse(dateString);
    }
    catch (Exception ex) { }

    return null;
  }

  public static String parseUserTime(String userTime)
  {
    if (StringUtils.isBlank(userTime)) return null;
    try
    {
      StringBuilder buffer = new StringBuilder();
      String blocks[] = userTime.split(":");
      String shour = blocks[0];
      int hour = Integer.parseInt(shour);
      if (hour < 0 || hour > 23) return null;

      if (hour < 10) buffer.append("0");
      buffer.append(String.valueOf(hour));
      if (blocks.length > 1)
      {
        String smin = blocks[1];
        int min = Integer.parseInt(smin);
        if (min < 0 || min > 59) return null;
        
        if (min < 10) buffer.append("0");
        buffer.append(String.valueOf(min));
        
        if (blocks.length > 2)
        {
          String ssec = blocks[2];
          int sec = Integer.parseInt(ssec);
          if (sec < 0 || sec > 59) return null;

          if (sec < 10) buffer.append("0");
          buffer.append(String.valueOf(sec));
        }
        else buffer.append("00");
      }
      else buffer.append("0000");
      return buffer.toString();
    }
    catch (Exception ex)
    {
    }
    return null;
  }

  public static String formatInternalDate(String date, String pattern)
  {
    Date parsed = parseInternalDate(date);
    if (parsed == null) return null;
    return formatDate(parsed, pattern);
  }
  
  public static String formatInternalTime(String internalTime)
  {
    if (internalTime == null) return null;

    StringBuilder buffer = null;
    if (internalTime.length() >= 2)
    {
      buffer = new StringBuilder();
      buffer.append(internalTime.substring(0, 2));
      if (internalTime.length() >= 4)
      {
        buffer.append(":");
        buffer.append(internalTime.substring(2, 4));
        if (internalTime.length() >= 6)
        {
          String value = internalTime.substring(4, 6);
          if (!"00".equals(value))
          {
            buffer.append(":");
            buffer.append(value);
          }
        }
      }
      else buffer.append(":00");
    }
    return buffer == null ? null : buffer.toString();
  }

  public static String formatDate(Date date, String pattern)
  {
    return formatDate(date, pattern, Locale.getDefault());
  }

  public static String formatDate(Date date, String pattern, Locale locale)
  {
    if (date == null) return null;
    SimpleDateFormat df;
    try
    {
      df = new SimpleDateFormat(pattern, locale);
    }
    catch (IllegalArgumentException ex)
    {
      if (pattern.contains("LLLL"))
        pattern = pattern.replaceAll("LLLL", "MMMM");
      else if (pattern.contains("LLL"))
        pattern = pattern.replaceAll("LLL", "MMM");
      df = new SimpleDateFormat(pattern, locale);
    }
    return df.format(date);
  }
  
  public static String getStandaloneMonthName(Calendar c, int style, Locale locale)
  {
    String month;
    int STANDALONE_MASK = 0x8000;
    try
    {
      month = c.getDisplayName(Calendar.MONTH, (style | STANDALONE_MASK), locale);
    }
    catch (IllegalArgumentException ex)
    {
      month = c.getDisplayName(Calendar.MONTH, style, locale);
    }
    return month;
  }
  
  public static String getStandaloneMonthPattern(String pattern)
  {
    String result = pattern;
    try
    {
      SimpleDateFormat df = new SimpleDateFormat(pattern);
    }
    catch (IllegalArgumentException ex)
    {
      if (pattern.contains("LLLL"))
        result = pattern.replaceAll("LLLL", "MMMM");
      else if (pattern.contains("LLL"))
        result = pattern.replaceAll("LLL", "MMM");
    }    
    return result;
  }

  public static boolean isValidInternalDate(String dateString)
  {
    if (dateString == null) return true;
    if (dateString.length() != 8) return false;
    try
    {
      SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
      df.parse(dateString);
      return true;
    }
    catch (ParseException ex)
    {
      return false;
    }
  }

  public static boolean isValidInternalDateTime(String dateTimeString)
  {
    if (dateTimeString == null) return true;
    if (dateTimeString.length() != 14) return false;
    try
    {
      SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
      df.parse(dateTimeString);
      return true;
    }
    catch (ParseException ex)
    {
      return false;
    }
  }

  public static String formatDateAsISO8601String(Date date)
  {
    if (date == null) return null;
    String result = formatDate(date, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    //convert YYYYMMDDTHH:mm:ss+HH00 into YYYYMMDDTHH:mm:ss+HH:00
    //- note the added colon for the Timezone
    result = result.substring(0, result.length()-2)
      + ":" + result.substring(result.length()-2);
    return result;
  }
  
  public static String concatDateAndTime(String date, String time)
  {
    String dateTime = null;
    
    if (date != null && time != null)
      dateTime = date + time;
    else if (date != null && time == null)
      dateTime = date + "000000";

    return dateTime;
  }  

  private static final char[] kDigits = { '0', '1', '2', '3', '4', '5', '6',
    '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

  public static char[] bytesToHex(byte[] raw)
  {
    int length = raw.length;
    char[] hex = new char[length * 2];
    for (int i = 0; i < length; i++) {
      int value = (raw[i] + 256) % 256;
      int highIndex = value >> 4;
      int lowIndex = value & 0x0f;
      hex[i * 2 + 0] = kDigits[highIndex];
      hex[i * 2 + 1] = kDigits[lowIndex];
    }
    return hex;
  }

  public static String unAccent(String text)
  {
    if (text == null)
      return null;

    return Normalizer.normalize(text, Normalizer.Form.NFD)
      .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
  }

  public static String wordWrap(String text, int numberOfChars,
    String toBeContinuedString)
  {
    try
    {
      if (toBeContinuedString == null)
        toBeContinuedString = "...";
      BreakIterator bi = BreakIterator.getWordInstance();
      bi.setText(text);
      int firstBefore = bi.preceding(numberOfChars);
      return text.substring(0, firstBefore) + toBeContinuedString;
    }
    catch (Exception ex)
    {
      return text;
    }
  }

  public static Collection<String> splitWords(String text)
  {
    return splitWords(text, null);
  }

  public static Collection<String> splitWords(String text, Collection<String> set)
  {
    if (set == null)
    {
      set = new ArrayList<String>();
    }
    String valueArray[] = text.split("[;,\n\r\t]");
    for (String value : valueArray)
    {
      value = value.trim();
      if (value.length() > 0)
      {
        set.add(value);
      }
    }
    return set;
  }

  public static String joinWords(Collection<String> words)
  {
    return joinWords(words, ",");
  }
  
  public static String joinWords(Collection<String> words, String separator)
  {
    StringBuilder buffer = new StringBuilder();
    for (String word : words)
    {
      if (buffer.length() > 0) buffer.append(separator);
      buffer.append(word);
    }
    return buffer.toString();
  }

  public static String normalizeName(String name)
  {
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < name.length(); i++)
    {
      char ch = name.charAt(i);
      if (ch < 128 && (Character.isLetter(ch)
        || Character.isDigit(ch) || ch == '_'))
      {
        buffer.append(ch);
      }
      else if (ch == ' ')
      {
        buffer.append('_');
      }
    }
    return buffer.toString();
  }
  
  public static String removeTags(String text) 
  {
    if (text == null || text.length() == 0) 
    {
      return text;
    }
    Matcher m = REMOVE_TAGS.matcher(text);
    return m.replaceAll("");
  }

  public static String getTextWithLinks(String text)
  {
    text = removeTags(text);
    StringBuilder buffer = new StringBuilder();
    int index = text.indexOf("http");
    while (index >= 0)
    {
      buffer.append(text.substring(0, index));
      text = text.substring(index);
      StringBuilder linkBuffer = new StringBuilder();
      int pos = 0;
      while (pos < text.length())
      {
        char ch = text.charAt(pos);
        if (ch != ' ' && ch != '\n' && ch != '\t')
        {
          linkBuffer.append(ch);
        }
        else break;
        pos++;
      }
      String link = linkBuffer.toString();
      
      if (link.length() > 10 && 
        (link.startsWith("http://") || link.startsWith("https://")))
      {
        buffer.append("<a href=\"");
        buffer.append(link);
        buffer.append("\" target=\"_blank\">");
        buffer.append(link);
        buffer.append("</a>");
      }
      else
      {
        buffer.append(link);
      }
      text = text.substring(link.length());
      index = text.indexOf("http");
    }
    buffer.append(text);
    return buffer.toString().replaceAll("\\\n", "<br>");
  }
  
  /**
   * Replaces the use of reserved words in EL expressions like new, class 
   * and case replaced by its corresponding method name. 
   * xBean.case.caseId --> xBean.getCase().caseId
   * xBean.class.name --> xBean.getClass().name
   * xBean.new.newId --> xBean.getNew().newId
   * xBean.new --> xBean.isNew()
   * @param expression
   * @return 
   */
  public static String replaceReservedWords(String expression)
  {
    if (expression != null && expression.startsWith("#{") && 
      (expression.contains("new") || expression.contains("class") || 
        expression.contains("case")))
    {
      Pattern p = Pattern.compile("\\.(new|case|class)([\\s\\}\\.])");
      Matcher m = p.matcher(expression);
      StringBuffer sb = new StringBuffer();
      while (m.find())
      {
        if (m.group(1).equals("new") && !m.group(2).equals("."))
          m.appendReplacement(
            sb, ".is" + StringUtils.capitalize(m.group(1)) + "()" + m.group(2));
        else
          m.appendReplacement(
            sb, ".get" + StringUtils.capitalize(m.group(1)) + "()" + m.group(2)); 
      }
      m.appendTail(sb);
    
      return sb.toString();
    }
    else
      return expression;
  }
  
  public static String normalize(String text)
  {
    if (text == null) return null;
    
    char[] cArray = text.toCharArray();
    for (int i = 0; i < cArray.length; i++)
    {
      char c = cArray[i];
      if ((c == 'à') || (c == 'ä') || (c == 'á')) cArray[i] = 'a';
      else if ((c == 'è') || (c == 'ë') || (c == 'é')) cArray[i] = 'e';
      else if ((c == 'ì') || (c == 'ï') || (c == 'í')) cArray[i] = 'i'; 
      else if ((c == 'ò') || (c == 'ö') || (c == 'ó')) cArray[i] = 'o'; 
      else if ((c == 'ù') || (c == 'ü') || (c == 'ú')) cArray[i] = 'u';     
      else if ((c == 'À') || (c == 'Ä') || (c == 'Á')) cArray[i] = 'A'; 
      else if ((c == 'È') || (c == 'Ë') || (c == 'É')) cArray[i] = 'E';
      else if ((c == 'Ì') || (c == 'Ï') || (c == 'Í')) cArray[i] = 'I'; 
      else if ((c == 'Ò') || (c == 'Ö') || (c == 'Ó')) cArray[i] = 'O'; 
      else if ((c == 'Ù') || (c == 'Ü') || (c == 'Ú')) cArray[i] = 'U';     
    }
    return new String(cArray);
  }  
  
  public static void main(String[] args)
  {
//    String original = "“z” ’z’ l•l";
//    System.out.println(original);
//    System.out.println(TextUtils.replaceSpecialChars(original));
//    System.out.println(TextUtils.parseInternalDate("20090101150505"));
//    System.out.println(wordWrap("El dia que te encontré en la calle", 15, "..."));
    String s = TextUtils.parseUserTime("16:59:00");
    System.out.println(s);
    System.out.println(TextUtils.formatInternalTime(s));
    System.out.println(TextUtils.getTextWithLinks("adasda http: dfsdfsdf http://wwwaasd.com 898 https://www.google.com"));
  }
}
