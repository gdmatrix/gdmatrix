package com.audifilm.matrix.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

/**
 *
 * @author comasfc
 */
public class NumberFormat {


 static public DecimalFormat getDecimalFormatSDE(int length, int decimals)
  {
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    dfs.setDecimalSeparator(',');

    StringBuilder pattern = new StringBuilder();
    for(int i=0; i<length; i++)
    {
      pattern.append("#");
    }
    if (decimals>0) {
      pattern.append(".");
      for(int i=0; i<decimals; i++) {
        pattern.append("#");
      }
    }

    DecimalFormat format = new DecimalFormat(pattern.toString(), dfs);
    format.setDecimalSeparatorAlwaysShown(false);

    return new DecimalFormat(pattern.toString(), dfs);
  }

  static public String formatPropertyValue(int length, int decimals, String value) {
    DecimalFormat format = getDecimalFormatSDE(length , decimals);
    if (decimals>0) {
      try
      {
        Number n = format.parse(value);
        return Double.toString(n.doubleValue());
      }
      catch (ParseException ex)
      {
          System.err.println("Error formating property value:" + value);
          ex.printStackTrace();
      }
    } else {
      try
      {
        Number n = format.parse(value);
        return Integer.toString(n.intValue());
      }
      catch (ParseException ex)
      {
          System.err.println("Error formating property value:" + value);
          ex.printStackTrace();
      }
    }
    return value;
  }



  static public String parsePropertyValue(int length, int decimals, String value) {
      DecimalFormat format = getDecimalFormatSDE(length , decimals);
      if (decimals>0) {
        try
        {
          double d = Double.parseDouble(value);
          return format.format(d);
        }
        catch (NumberFormatException ex)
        {
          System.err.println("Error parsing property value:" + value);
          ex.printStackTrace();
        }
      } else {
        try
        {
          int i = Integer.parseInt(value);
          return format.format(i);
        }
        catch (NumberFormatException ex)
        {
          System.err.println("Error parsing property value:" + value);
          ex.printStackTrace();
        }
      }

    return value;
  }


}
