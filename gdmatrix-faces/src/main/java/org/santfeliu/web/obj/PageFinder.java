package org.santfeliu.web.obj;

import java.lang.reflect.Field;
import java.util.List;

public class PageFinder
{
  public static int findFirstRowIndex(List objectList, int rowsPerPage,
    String fieldName, Object fieldValue)
  {
    boolean found = false;
    int rowIndex = 0;
    for (int i = 0; i < objectList.size() && !found; i++)
    {
      Object object = objectList.get(i);
      if (isObjectMatch(object, fieldName, fieldValue))
      {
        found = true;
        rowIndex = i;
      }
    }
    if (found)
    {
      return rowsPerPage * (rowIndex / rowsPerPage);
    }
    else return 0;
  }

  private static boolean isObjectMatch(Object object, String fieldName,
    Object fieldValue)
  {
    try
    {
      Class<?> c = object.getClass();
      Field f = c.getDeclaredField(fieldName);
      f.setAccessible(true);      
      return fieldValue.equals(f.get(object));
    }
    catch (NoSuchFieldException ex)
    {
      return false;
    }
    catch (IllegalAccessException ex)
    {
      return false;
    }
  }

}
