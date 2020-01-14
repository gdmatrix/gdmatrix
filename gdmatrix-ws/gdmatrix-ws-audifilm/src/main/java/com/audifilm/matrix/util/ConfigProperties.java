package com.audifilm.matrix.util;

import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author comasfc
 */
public class ConfigProperties {

  static public String getProperty(String propertyId) {
     String value = null;
     try {
       value = MatrixConfig.getProperty(propertyId);
     } catch(Exception ex) {

     }
     if (value==null) {
       value = System.getProperty(propertyId);
     }
     return value;
  }

  static public String getProperty(String propertyId, String defaultValue) {
     String value = null;
     try {
       value = MatrixConfig.getProperty(propertyId);
     } catch(Exception ex) {
       
     }
     if (value==null) {
       value = System.getProperty(propertyId, defaultValue);
     }
     return (value==null)?defaultValue:value;
  }



}
