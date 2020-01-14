package org.santfeliu.form.type.html;

import org.santfeliu.form.Field;

/**
 *
 * @author realor
 */
public class HtmlField implements Field, Cloneable
{
  private String reference;
  private String label;
  private String type;
  private boolean readOnly;
  private int minOccurs;
  private int maxOccurs;

  public String getReference()
  {
    return reference;
  }

  public void setReference(String reference)
  {
    this.reference = reference;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public boolean isReadOnly()
  {
    return readOnly;
  }

  public void setReadOnly(boolean readOnly)
  {
    this.readOnly = readOnly;
  }

  public int getMinOccurs()
  {
    return minOccurs;
  }

  public void setMinOccurs(int minOccurs)
  {
    this.minOccurs = minOccurs;
  }

  public int getMaxOccurs()
  {
    return maxOccurs;
  }

  public void setMaxOccurs(int maxOccurs)
  {
    this.maxOccurs = maxOccurs;
  }

  public Object getNativeField()
  {
    return null;
  }

  public boolean isRequired()
  {
    return minOccurs > 0;
  }

  @Override
  public String toString()
  {
    return "HtmlField[" + reference + "] " + label;
  }

  @Override
  public HtmlField clone()
  {
    HtmlField newField = new HtmlField();
    newField.reference = reference;
    newField.label = label;    
    newField.type = type;
    newField.minOccurs = minOccurs;
    newField.maxOccurs = maxOccurs;
    newField.readOnly = readOnly;
    return newField;
  }
}
