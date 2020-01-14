package org.santfeliu.web.obj.util;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.santfeliu.dic.util.DictionaryUtils;

/**
 *
 * @author blanquepa
 */
public abstract class DynamicFormFilter extends FormFilter
{
  public void setFormProperties(List<Property> properties)
  {
    //Properties get from dynamic form
    if (properties != null)
    {
      for (Property property : properties)
      {
        DictionaryUtils.setProperty(getObjectFilter(), property.getName(), property.getValue());
      }
    }

    //Properties get from default form
    String name1 = getPropertyName1();
    String name2 = getPropertyName2();
    String value1 = getPropertyValue1();
    String value2 = getPropertyValue2();
    if (!StringUtils.isBlank(name1) && !StringUtils.isBlank(name2)
      && name1.equals(name2))
    {
      List values =
        Arrays.asList(new String[]{value1, value2});
      DictionaryUtils.setProperty(getObjectFilter(), name1, values);
    }
    else
    {
      if (!StringUtils.isBlank(name1) && !StringUtils.isBlank(value1))
        DictionaryUtils.setProperty(getObjectFilter(), name1, value1);
      if (!StringUtils.isBlank(name2) && !StringUtils.isBlank(value2))
        DictionaryUtils.setProperty(getObjectFilter(), name2, value2);
    }
  }
  
  /**
   * This method copies and transforms input properties to inner object filter
   * @param formProperties
   */  
  public abstract void setInputProperties(List<Property> formProperties);  

  protected abstract String getPropertyName1();

  protected abstract String getPropertyName2();

  protected abstract String getPropertyValue1();

  protected abstract String getPropertyValue2();
  
  protected abstract List<Property> getProperty();
}
