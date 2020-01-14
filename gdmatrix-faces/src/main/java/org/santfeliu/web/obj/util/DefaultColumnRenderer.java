package org.santfeliu.web.obj.util;

import java.io.Serializable;
import java.util.List;
import org.matrix.dic.Property;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.util.PojoUtils;

/**
 *
 * @author blanquepa
 */
public class DefaultColumnRenderer extends ColumnRenderer implements Serializable
{
  @Override
  public Object getValue(String columnName, Object row)
  {
    Object value = null;
    if (columnName.contains(".") || (columnName.contains("[") && columnName.contains("]")))
    {
      value = PojoUtils.getDeepStaticProperty(row, columnName);
      if (value instanceof List)
        value = ((List)value).get(0);
    }
    else
    {
      Property property = DictionaryUtils.getProperty(row, columnName);
      if (property != null)
      {
        List values = property.getValue();
        if (!values.isEmpty() && values.size() == 1)
          value = values.get(0);
        else
          value = values;
      }
    }
    return value;
  }

  @Override
  public boolean isValueEscaped()
  {
    return false;
  }
}
