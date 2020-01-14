package org.santfeliu.web.obj.util;

/**
 *
 * @author blanquepa
 */
public abstract class ColumnRenderer
{
//  protected String valuePrefix;
//  protected String valueSuffix;
//
//  public String getValuePrefix()
//  {
//    return valuePrefix;
//  }
//
//  public void setValuePrefix(String valuePrefix)
//  {
//    this.valuePrefix = valuePrefix;
//  }
//
//  public String getValueSuffix()
//  {
//    return valueSuffix;
//  }
//
//  public void setValueSuffix(String valueSuffix)
//  {
//    this.valueSuffix = valueSuffix;
//  }

//  public Object getFormattedValue(String columnName, Object row)
//  {
//    String prefix = getValuePrefix() != null ? getValuePrefix() : "";
//    String suffix = getValueSuffix() != null ? getValueSuffix() : "";
//    return prefix + getValue(columnName, row) + suffix;
//  }

  public abstract Object getValue(String columnName, Object row);

  public abstract boolean isValueEscaped();

}
