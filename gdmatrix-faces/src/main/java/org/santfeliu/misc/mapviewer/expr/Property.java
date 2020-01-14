package org.santfeliu.misc.mapviewer.expr;

/**
 *
 * @author realor
 */
public class Property extends Expression
{
  private String name;

  public Property()
  {    
  }
  
  public Property(String name)
  {
    this.name = name;
  }
  
  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }
}
