package org.santfeliu.web.obj.util;

import java.io.Serializable;

/**
 *
 * @author blanquepa
 */
public class EmailColumnRenderer extends ColumnRenderer implements Serializable
{
  @Override
  public Object getValue(String columnName, Object row)
  {
    DefaultColumnRenderer defaultColumnRenderer = new DefaultColumnRenderer();
    Object value = defaultColumnRenderer.getValue(columnName, row);
    if (value != null && String.valueOf(value).contains("@"))
      value = "<a href='mailto:" + value + "'>" + value + "</a>";
    return value;
  }

  @Override
  public boolean isValueEscaped()
  {
    return false;
  }

}
