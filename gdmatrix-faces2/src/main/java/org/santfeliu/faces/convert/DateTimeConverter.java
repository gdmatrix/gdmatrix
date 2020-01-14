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
package org.santfeliu.faces.convert;

import java.io.Serializable;
import java.util.Date;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
public class DateTimeConverter implements Converter, Serializable
{
  private String userFormat = "dd/MM/yyyy HH:mm:ss";
  private String internalFormat = "yyyyMMddHHmmss";

  public DateTimeConverter()
  {
  }
  
  public DateTimeConverter(String userFormat)
  {
    this.userFormat = userFormat;
  }

  public DateTimeConverter(String internalFormat, String userFormat)
  {
    this.internalFormat = internalFormat;
    this.userFormat = userFormat;
  }

  public String getUserFormat()
  {
    return userFormat;
  }

  public void setUserFormat(String dateFormat)
  {
    this.userFormat = dateFormat;
  }

  public String getAsString(FacesContext fc, UIComponent uic, Object o)
  {
    String internalDate = (String)o;
    if (uic != null)
    {
      String iFormat = (String)uic.getAttributes().get("internalFormat");
      if (iFormat != null)
        internalFormat = iFormat;
      String uFormat = (String)uic.getAttributes().get("userFormat");
      if (uFormat != null)
        userFormat = uFormat;
    }
    if (internalDate != null)
    {
      Date date = TextUtils.parseUserDate(internalDate, internalFormat);
      return TextUtils.formatDate(date, userFormat, fc.getViewRoot().getLocale());
    }
    else
      return internalDate != null ? internalDate : null;
  }

  public Object getAsObject(FacesContext fc, UIComponent uic, String string)
  {
    if (uic != null)
    {
      String iFormat = (String)uic.getAttributes().get("internalFormat");
      if (iFormat != null)
        internalFormat = iFormat;
      String uFormat = (String)uic.getAttributes().get("userFormat");
      if (uFormat != null)
        userFormat = uFormat;
    }
    Date date = TextUtils.parseUserDate(string, userFormat);
    if (date != null)
    {
      return TextUtils.formatDate(date, internalFormat);
    }
    else
      return string;
  }


}
