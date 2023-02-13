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
package org.santfeliu.webapp.helpers;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Locale;
import javax.faces.context.FacesContext;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.Translator;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.webapp.util.WebUtils;

/**
 *
 * @author blanquepa
 */
public abstract class TypedHelper implements Serializable
{  
  public abstract String getTypeId();
  
  public boolean isPropertyHidden(String propName)
  {
    PropertyDefinition pd = getPropertyDefinition(getTypeId(), propName);     
    return isPropertyHidden(pd);
  }
    
  public String getPropertyLabel(String propName, String altName)
  {
    String label = altName;
    PropertyDefinition pd = getPropertyDefinition(getTypeId(), propName);
    if (pd != null)
    {
      ApplicationBean applicationBean = WebUtils.getBean("applicationBean");
      Translator translator = applicationBean.getTranslator();
      StringWriter sw = new StringWriter();
      StringReader sr = new StringReader(pd.getDescription());
      String group = "typeId:" + getTypeId();
      try
      {
        Locale locale = 
          FacesContext.getCurrentInstance().getViewRoot().getLocale();
        translator.translate(sr, sw, "text/plain", locale.getLanguage(), group);
        label = sw.toString();
      }
      catch (IOException ex)
      {
      }
    }
    return label;    
  }  
    
  private boolean isPropertyHidden(PropertyDefinition pd)
  {
    if (pd == null)
      return true;
    
    String propName = pd.getName();
    
    propName = "render" + StringUtils.capitalize(propName);
    String value = WebUtils.getMenuItemProperty(propName);
    if (value != null)
      return !Boolean.parseBoolean(value);

    return pd.isHidden(); 
  }
  
  private PropertyDefinition getPropertyDefinition(String typeId,
    String propName)
  {
    Type type = TypeCache.getInstance().getType(typeId);
    if (type != null)
    {
      List<PropertyDefinition> pds = type.getPropertyDefinition();
      for (PropertyDefinition pd : pds)
      {
        if (pd.getName().equals(propName))
        {
          return pd;
        }
      }
      String superTypeId = type.getSuperTypeId();
      if (superTypeId != null)
      {
        return getPropertyDefinition(superTypeId, propName);
      }
    }
    return null;
  }


}
