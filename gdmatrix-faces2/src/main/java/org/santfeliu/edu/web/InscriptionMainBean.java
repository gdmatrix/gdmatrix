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
package org.santfeliu.edu.web;

import java.util.HashMap;
import java.util.List;

import java.util.Map;

import javax.faces.model.SelectItem;

import org.matrix.edu.Inscription;

import org.matrix.edu.Property;

import org.santfeliu.kernel.web.PersonBean;
import org.santfeliu.web.obj.PageBean;

/**
 *
 * @author unknown
 */
public class InscriptionMainBean extends PageBean
{
  private static final String FORM_PROPERTY = "inscription_form_url";
  private Inscription inscription;
  private Map properties = new HashMap();
  
  public InscriptionMainBean()
  {
    load();
  }

  public String show()
  {
    return "inscription_main";
  }

  public void setInscription(Inscription inscription)
  {
    this.inscription = inscription;
  }

  public Inscription getInscription()
  {
    return inscription;
  }
  
  public List<SelectItem> getPersonSelectItems()
  {
    PersonBean personBean = (PersonBean)getBean("personBean");
    return personBean.getSelectItems(inscription.getPersonId());
  }
  
  public String searchPerson()
  {
    return getControllerBean().searchObject("Person",
      "#{inscriptionMainBean.inscription.personId}");
  }
  
  public String showPerson()
  {    
    return getControllerBean().showObject("Person",
      inscription.getPersonId());
  }
  
  public List<SelectItem> getCourseSelectItems()
  {
    CourseBean courseBean = (CourseBean)getBean("courseBean");
    return courseBean.getSelectItems(inscription.getCourseId());
  }
  
  public String searchCourse()
  {
    return getControllerBean().searchObject("Course",
      "#{inscriptionMainBean.inscription.courseId}");
  }
  
  public String showCourse()
  {    
    return getControllerBean().showObject("Course",
      inscription.getCourseId());
  }  
  
  public String store()
  {
    try
    {
      for (Object e : properties.entrySet())
      {
        Map.Entry entry = (Map.Entry)e;
        Property property = new Property();
        property.setName((String)entry.getKey());
        Object value = entry.getValue();
        if (value != null)
        {
          property.setValue(String.valueOf(value));
        }
        inscription.getProperties().add(property);
      }
      this.inscription = 
        EducationConfigBean.getPort().storeInscription(inscription);
      setObjectId(inscription.getInscriptionId());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return show();
  }
  
  public String getFormUrl()
  {
    return getProperty(FORM_PROPERTY);
  }

  public Map getProperties()
  {
    return properties;
  }
  
  public void setProperties(Map properties)
  {
    this.properties = properties;
  }
  
  private void load()
  {
    if (isNew())
    {
      this.inscription = new Inscription();
    }
    else
    {
      try
      {
        this.inscription =
          EducationConfigBean.getPort().loadInscription(getObjectId());
      }
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        error(ex);
        this.inscription = new Inscription();
      }
      for (Property property : inscription.getProperties())
      {
        properties.put(property.getName(), property.getValue());
      }
      inscription.getProperties().clear();
    }
  }
}
