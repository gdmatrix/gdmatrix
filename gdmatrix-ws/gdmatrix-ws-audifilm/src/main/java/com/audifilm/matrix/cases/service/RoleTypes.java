package com.audifilm.matrix.cases.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author blanquepa
 * @author lopezrj
 */
public class RoleTypes
{
  private static HashMap<String, String[]> map = 
    new HashMap<String, String[]>();
  private static long lastModifiedMillis = 0;

  private static final String FILE_NAME = "RoleTypes.properties";

  public static List<String> getTypes(String role)
  {
    loadFile();
    String[] array = map.get(role);
    if (array != null)
      return Arrays.asList(array);
    else
      return Collections.EMPTY_LIST;
  }

  public static boolean containsType(String role, String typeId)
  {
    List<String> types = getTypes(role);
    return types.contains(typeId);
  }

  private static void loadFile()
  {
    File file = new File(MatrixConfig.getDirectory(), FILE_NAME);
    if (file.exists() && file.lastModified() != lastModifiedMillis)
    {
      map.clear();
      Properties properties = new Properties();
      try
      {
        properties.load(new FileInputStream(file));
        for (String key : properties.stringPropertyNames())
        {
          String rolesString = properties.getProperty(key);
          if (rolesString != null && !rolesString.trim().isEmpty())
          {
            String[] roleArray = rolesString.split(",");
            for (int i = 0; i < roleArray.length; i++)
            {
              roleArray[i] = roleArray[i].trim();
            }
            map.put(key, roleArray);
          }
        }
        lastModifiedMillis = file.lastModified();
      }
      catch (IOException ex)
      {
        
      }
    }
  }
  
}
