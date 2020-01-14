package org.santfeliu.web.obj.util;

/**
 *
 * @author blanquepa
 */
public class AltNullValueColumnRenderer extends DefaultColumnRenderer
{
  private String altColumnName;

  public AltNullValueColumnRenderer(String altColumnName)
  {
    this.altColumnName = altColumnName;
  }

  @Override
  public Object getValue(String columnName, Object row)
  {
    Object value = super.getValue(columnName, row);
    if (value == null)
      value = super.getValue(altColumnName, row);

    return value;
  }
}
