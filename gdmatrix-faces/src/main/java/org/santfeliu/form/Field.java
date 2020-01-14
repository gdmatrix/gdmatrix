package org.santfeliu.form;

/**
 *
 * @author realor
 */
public interface Field
{
  static final String NUMBER = "NUMBER";
  static final String TEXT = "TEXT";
  static final String BOOLEAN = "BOOLEAN";
  static final String DATE = "DATE";
  static final String TIME = "TIME";
  static final String DATETIME = "DATETIME";
  
  String getReference();
  String getLabel();
  String getType();
  boolean isReadOnly();
  int getMinOccurs();
  int getMaxOccurs();
  Object getNativeField();  
}
