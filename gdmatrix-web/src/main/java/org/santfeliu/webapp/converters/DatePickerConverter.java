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
package org.santfeliu.webapp.converters;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
@FacesConverter(value = "datePickerConverter")
public class DatePickerConverter implements Converter<String>, Serializable
{
  private static final String INTERNAL_DATETIME_FORMAT = "yyyyMMddHHmmss";
  private static final String INTERNAL_DATE_FORMAT = "yyyyMMdd";
  private static final String INTERNAL_TIME_FORMAT = "HHmmss";
  
  private static final String USER_DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
  private static final String USER_DATE_FORMAT = "dd/MM/yyyy";
  private static final String USER_TIME_FORMAT = "HH:mm:ss";
  
  private String internalFormat = INTERNAL_DATE_FORMAT;    
  private String userFormat = USER_DATE_FORMAT;


  public DatePickerConverter()
  {
  }
  
  public DatePickerConverter(String userFormat, String internalFormat)
  {
    this.userFormat = userFormat;
    this.internalFormat = internalFormat;
  }
  
  @Override
  public String getAsString(FacesContext fc, UIComponent uic, 
    String internalDate)
  {
    if (uic != null)
    {
      String uFormat = (String)uic.getAttributes().get("pattern");
      if (uFormat != null)
        userFormat = uFormat;
      else
      {
        Boolean showTime = (Boolean)uic.getAttributes().get("showTime");
        Boolean timeOnly = (Boolean)uic.getAttributes().get("timeOnly");  
        if (showTime)
          userFormat = USER_DATETIME_FORMAT;
        else if (timeOnly)
          userFormat = USER_TIME_FORMAT;
      }
    }
    if (internalDate != null)
    {
      Date date = TextUtils.parseInternalDate(internalDate);
      Locale locale = fc.getViewRoot().getLocale();
      return TextUtils.formatDate(date, userFormat, locale);
    }
    else
      return internalDate != null ? internalDate : null;
  }

  @Override
  public String getAsObject(FacesContext fc, UIComponent uic, String userDate)
  {
    if (uic != null)
    {
      String uFormat = (String)uic.getAttributes().get("pattern");        
      if (uFormat != null)
        userFormat = uFormat;

      Boolean showTime = (Boolean)uic.getAttributes().get("showTime"); 
      Boolean timeOnly = (Boolean)uic.getAttributes().get("timeOnly");      
      if (showTime)
        internalFormat = INTERNAL_DATETIME_FORMAT;
      else if (timeOnly)
        internalFormat = INTERNAL_TIME_FORMAT;
    }
    Date date = TextUtils.parseUserDate(userDate, userFormat);
    if (date != null)
    {
      Locale locale = fc.getViewRoot().getLocale();
      return TextUtils.formatDate(date, internalFormat, locale);
    }
    else
      return userDate;
  }
}
