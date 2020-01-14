package org.santfeliu.form;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author realor
 */
public interface View
{
  // view types
  static final String UNKNOWN = "UNKNOWN";
  static final String GROUP = "GROUP";
  static final String TABLE = "TABLE";
  static final String STYLE = "STYLE";
  static final String LABEL = "LABEL";
  static final String TEXTFIELD = "TEXTFIELD";
  static final String PASSWORDFIELD = "PASSWORDFIELD";
  static final String RADIO = "RADIO";
  static final String CHECKBOX = "CHECKBOX";
  static final String LIST = "LIST";
  static final String ITEM = "ITEM";
  static final String SELECT = "SELECT";
  static final String SLIDER = "SLIDER";
  static final String CALENDAR = "CALENDAR";
  static final String BUTTON = "BUTTON";

  String getId();
  String getReference();
  String getViewType();
  View getParent();
  List<View> getChildren();
  Object getProperty(String name);
  Collection<String> getPropertyNames();
  public String getNativeViewType();
}
