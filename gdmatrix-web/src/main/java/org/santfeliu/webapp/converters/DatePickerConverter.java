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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author blanquepa
 */
@FacesConverter(value = "datePickerConverter")
public class DatePickerConverter extends DateConverter 
{  
  public DatePickerConverter()
  {
  }
  
  @Override
  public String getAsString(FacesContext fc, UIComponent uic, 
    String internalDate)    
  {
    String userFormat = getUserFormat(uic);
    
    return getAsString(fc, internalDate, userFormat);
  }

  @Override
  public String getAsObject(FacesContext fc, UIComponent uic, String userDate)
  { 
    String userFormat = getUserFormat(uic);
    String internalFormat = getInternalFormat(uic);  
    
    return getAsObject(fc, userDate, userFormat, internalFormat);
  }  
    
  private String getInternalFormat(UIComponent uic)
  {
    String internalFormat = INTERNAL_DATE_FORMAT;
    if (uic != null)
    {  
      Boolean showTime = (Boolean)uic.getAttributes().get("showTime"); 
      Boolean timeOnly = (Boolean)uic.getAttributes().get("timeOnly");      
      if (showTime)
        internalFormat = INTERNAL_DATETIME_FORMAT;
      else if (timeOnly)
        internalFormat = INTERNAL_TIME_FORMAT;
    }      
      
    return internalFormat;
  }
  

  private String getUserFormat(UIComponent uic)
  {
    String userFormat = USER_DATE_FORMAT;
    
    if (uic != null)
    {
      String uFormat = (String)uic.getAttributes().get("pattern");
      if (uFormat != null)
        userFormat = uFormat;
      else
      {
        Boolean showTime = (Boolean)uic.getAttributes().get("showTime");
        Boolean timeOnly = (Boolean)uic.getAttributes().get("timeOnly");  
        if (showTime != null && showTime)
          userFormat = USER_DATETIME_FORMAT;
        else if (timeOnly != null && timeOnly)
          userFormat = USER_TIME_FORMAT;
      }
    }
    return userFormat;
  }  
  
  
}
