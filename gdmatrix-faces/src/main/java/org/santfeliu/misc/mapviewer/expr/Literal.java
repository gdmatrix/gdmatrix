package org.santfeliu.misc.mapviewer.expr;

/**
 *
 * @author realor
 */
public class Literal extends Expression
{
  public static final int NULL = 0;
  public static final int STRING = 1;
  public static final int NUMBER = 2;
  public static final int BOOLEAN = 3;
  public static final int DATE = 4;
  
  public static final String TRUE = "TRUE";
  public static final String FALSE = "FALSE";
  
  private String value;
  private int type;

  public Literal()
  {    
  }
  
  public Literal(int type, String value)
  {
    this.type = type;
    this.value = value;
  }
  
  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public int getType()
  {
    return type;
  }

  public void setType(int type)
  {
    this.type = type;
  }
  
  public void detectType()
  {
    // TODO: detect date literals
    if (value.equals(TRUE) || value.equals(FALSE)) type = BOOLEAN;
    else
    {
      try
      {
        Double.parseDouble(value);
        type = NUMBER;
      }
      catch (NumberFormatException ex)
      {
        type = STRING;
      }
    }
  }
}
