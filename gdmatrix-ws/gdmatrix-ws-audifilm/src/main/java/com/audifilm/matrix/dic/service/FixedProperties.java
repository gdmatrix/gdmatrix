package com.audifilm.matrix.dic.service;

import com.audifilm.matrix.util.DateFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.matrix.dic.PropertyDefinition;
import org.matrix.dic.PropertyType;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author comasfc
 */
public class FixedProperties
{
  Properties _fileProperties;
  String filePropertiesPath;
  long lastModifiedTimestamp;

  public FixedProperties(String filePropertiesPath)
  {
    this.filePropertiesPath = filePropertiesPath;
    this.lastModifiedTimestamp = 0;
  }

  public void refresh()
  {
    try
    {
      if (_fileProperties==null) _fileProperties = new Properties();

      synchronized(this)
      {
        if (filePropertiesPath==null)
        {
          filePropertiesPath = MatrixConfig.getPathProperty(
            getClass().getName() + ".propertyDefinitionsFile");
        }

        File file = new File(filePropertiesPath);
        if (file.exists() && file.lastModified()>lastModifiedTimestamp)
        {
          _fileProperties.clear();
          InputStream ins = null;
          try {
            ins = new FileInputStream(file);
            _fileProperties.load(ins);
            
          } catch(IOException ex) {
            ex.printStackTrace();
          } finally {
            if (ins!=null) ins.close();
          }
          lastModifiedTimestamp = file.lastModified();
        }
      }
    }
    catch (Exception ex)
    {
      Logger.getLogger(DictionaryManager.class.getName()).log(Level.SEVERE, null, ex);
    }
  }


  public boolean isModified(long timestampsince)
  {
    long modified = getLastModifiedTimestamp();
    return (timestampsince<=modified);
  }
  
  public boolean isModified(String dateTime1, String dateTime2)
  {
    DateFormat dateFormat = DateFormat.getInstance();
    long time1 = 0;
    try
    {
      time1 = dateFormat.parse(dateTime1).getTime();
    }
    catch (ParseException ex)
    {
      Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
    }
    long time2 = System.currentTimeMillis();
    try
    {
      time2 = dateFormat.parse(dateTime1).getTime();
    }
    catch (ParseException ex)
    {
      Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
    }
    long modified = getLastModifiedTimestamp();
    return (time1<=modified && modified<=time2);
  }

  public Properties getFileProperties()
  {
    refresh();
    return _fileProperties;
  }

  public long getLastModifiedTimestamp()
  {
    refresh();
    return lastModifiedTimestamp;
  }


  public List<PropertyDefinition> loadPropertyDefinitions(String typeId)
  {
    Properties fileProperties = getFileProperties();
    List<PropertyDefinition> propertyList = new ArrayList<PropertyDefinition>();

    
    String [] typeProperties = fileProperties.getProperty(typeId + ".properties", "").split(",");
    for(int i=0; i<typeProperties.length; i++)
    {
      String name = typeProperties[i].trim();
      if  (name.equals("")) continue;

      String proppath = typeId + ".property." + name + ".";
      PropertyDefinition propDef = new PropertyDefinition();
      propDef.setName(name);
      propDef.setDescription(fileProperties.getProperty(proppath + "description", ""));

      try {
        propDef.setType(PropertyType.valueOf(fileProperties.getProperty(proppath + "type", "TEXT")));
      } catch(Exception ex) {
        ex.printStackTrace();
        propDef.setType(PropertyType.TEXT);
      }

      String [] values = fileProperties.getProperty(proppath + "value", "").split(";");
      for (int v=0; v<values.length; v++)
      {
        if (values[v].equals("") && i==values.length-1) continue;
        propDef.getValue().add(values[v]);
      }

      //Integer
      String value = fileProperties.getProperty(proppath + "size", "0");
      try {
        propDef.setSize(Integer.parseInt(value));
      } catch(Exception ex) {
        propDef.setSize(0);
      }

      value = fileProperties.getProperty(proppath + "minOccurs", "0");
      try {
        propDef.setMinOccurs(Integer.parseInt(value));
      } catch(Exception ex) {
        propDef.setMinOccurs(0);
      }

      value = fileProperties.getProperty(proppath + "maxOccurs", "1");
      try {
        propDef.setMaxOccurs(Integer.parseInt(value));
      } catch(Exception ex) {
        propDef.setMaxOccurs(1);
      }

      //Boolean
      value = fileProperties.getProperty(proppath + "hidden","true");
      propDef.setHidden(value==null || !(value.equals("false") || value.equals("N") || value.equals("0")));

      value = fileProperties.getProperty(proppath + "readOnly","true");
      propDef.setReadOnly(value==null || !(value.equals("false") || value.equals("N") || value.equals("0")));

      propertyList.add(propDef);
    }
    return propertyList;

  }

}
