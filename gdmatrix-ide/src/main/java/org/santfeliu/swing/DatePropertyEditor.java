/*
 * GDMatrix
 *
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *
 * This program is licensed and may be used, modified and redistributed under
 * the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European
 * Commission.
 *
 * Alternatively, you may redistribute and/or modify this program under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either  version 3 of the License, or (at your option)
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the licenses for the specific language governing permissions, limitations
 * and more details.
 *
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along
 * with this program; if not, you may find them at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/
 * and
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.swing;

/**
 * L2FProd.com Common Components 6.11 License.
 *
 * Copyright 2005-2006 L2FProd.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;

import java.util.Date;
import java.util.Locale;

/**
 * Date Property Editor based on <a
 * href="http://www.toedter.com/en/jcalendar/index.html">toedter
 * JCalendar </a> component. <br>
 */
/**
 *
 * @author l2fprod
 */
public class DatePropertyEditor extends AbstractPropertyEditor
{
  /**
   * Constructor for DatePropertyEditor
   */
  public DatePropertyEditor()
  {
    editor = new JDateChooser();
  }

  /**
   * Constructor for DatePropertyEditor
   *
   * @param dateFormatString string used to format the Date object,
   *          see: <b>java.text.SimpleDateFormat </b>
   *
   * @param locale Locale used to display the Date object
   */
  public DatePropertyEditor(String dateFormatString, Locale locale)
  {
    editor = new JDateChooser();
    ((JDateChooser)editor).setDateFormatString(dateFormatString);
    ((JDateChooser)editor).setLocale(locale);
  }

  /**
   * Constructor for DatePropertyEditor
   *
   * @param locale Locale used to display the Date object
   */
  public DatePropertyEditor(Locale locale)
  {
    editor = new JDateChooser();
    ((JDateChooser)editor).setLocale(locale);
  }

  /**
   * Returns the Date of the Calendar
   *
   * @return the date choosed as a <b>java.util.Date </b>b> object or
   *         null is the date is not set
   */
  public Object getValue()
  {
    return ((JDateChooser)editor).getDate();
  }

  /**
   * Sets the Date of the Calendar
   *
   * @param value the Date object
   */
  @Override
  public void setValue(Object value)
  {
    if (value != null)
    {
      ((JDateChooser)editor).setDate((Date)value);
    }
  }

  /**
   * Returns the Date formated with the locale and formatString set.
   *
   * @return the choosen Date as String
   */
  @Override
  public String getAsText()
  {
    Date date = (Date)getValue();
    java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(
      getDateFormatString());
    String s = formatter.format(date);
    return s;
  }

  /**
   * Sets the date format string. E.g "MMMMM d, yyyy" will result in
   * "July 21, 2004" if this is the selected date and locale is
   * English.
   *
   * @param dateFormatString The dateFormatString to set.
   */
  public void setDateFormatString(String dateFormatString)
  {
    ((JDateChooser)editor).setDateFormatString(dateFormatString);
  }

  /**
   * Gets the date format string.
   *
   * @return Returns the dateFormatString.
   */
  public String getDateFormatString()
  {
    return ((JDateChooser)editor).getDateFormatString();
  }

  /**
   * Sets the locale.
   *
   * @param l The new locale value
   */
  public void setLocale(Locale l)
  {
    ((JDateChooser)editor).setLocale(l);
  }

  /**
   * Returns the Locale used.
   *
   * @return the Locale object
   */
  public Locale getLocale()
  {
    return ((JDateChooser)editor).getLocale();
  }

  @Override
  public void removePropertyChangeListener(java.beans.PropertyChangeListener p1)
  {
    System.out.println(">>>" + p1);
  }
}