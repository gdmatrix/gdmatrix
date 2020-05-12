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
package org.santfeliu.cases.web;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.InterventionFilter;
import org.matrix.dic.Property;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.util.FilterUtils;
import org.santfeliu.web.obj.util.DynamicFormFilter;
import org.santfeliu.web.obj.util.FormFilter;

/**
 *
 * @author blanquepa
 */
public class InterventionFormFilter extends DynamicFormFilter
{
  private String intIdInput;
  private String commentsInput;
  private String propertyNameInput1 = null;
  private String propertyValueInput1 = null;
  private String propertyNameInput2 = null;
  private String propertyValueInput2 = null;  
  
  private InterventionFilter interventionFilter;
  
  public InterventionFormFilter()
  {
    interventionFilter = new InterventionFilter();
  }  

  public String getIntId()
  {
    return intIdInput;
  }

  public void setIntId(String intIdInput)
  {
    this.intIdInput = intIdInput;
  }

  public String getComments()
  {
    return commentsInput;
  }

  public void setComments(String commentsInput)
  {
    this.commentsInput = commentsInput;
  }

  public InterventionFilter getInterventionFilter()
  {
    return interventionFilter;
  }

  public void setInterventionFilter(InterventionFilter interventionFilter)
  {
    this.interventionFilter = interventionFilter;
  }

  @Override
  protected Object getObjectFilter()
  {
    return interventionFilter;
  }

  @Override
  public void setFirstResult(int value)
  {
    this.interventionFilter.setFirstResult(value);
  }

  @Override
  public void setMaxResults(int value)
  {
    this.interventionFilter.setMaxResults(value);
  }

  protected void copy(FormFilter src, FormFilter dst)
  {
    InterventionFormFilter dff = new InterventionFormFilter();
    if (src instanceof InterventionFormFilter && dst instanceof InterventionFormFilter)
    {
      InterventionFormFilter sff = (InterventionFormFilter)src;
      dff.setInterventionFilter(sff.getInterventionFilter());
      dff.setIntId(sff.getIntId());
      dff.setComments(sff.getComments());
      dff.setPropertyName1(sff.getPropertyName1());
      dff.setPropertyName2(sff.getPropertyName2());
      dff.setPropertyValue1(sff.getPropertyValue1());
      dff.setPropertyValue2(sff.getPropertyValue2());
      dst = dff;
    }
  }

  @Override
  protected void clearAll()
  {
    this.intIdInput = null;
    this.commentsInput = null;
    this.interventionFilter = new InterventionFilter();
    propertyNameInput1 = null;
    propertyValueInput1 = null;
    propertyNameInput2 = null;
    propertyValueInput2 = null;
  }

  @Override
  public boolean isEmpty()
  {
    return (StringUtils.isBlank(getIntId()) 
      && StringUtils.isBlank(getComments())
      && StringUtils.isBlank(getPropertyName1())
      && StringUtils.isBlank(getPropertyName2())
      && StringUtils.isBlank(getInterventionFilter().getFromDate())
      && StringUtils.isBlank(getInterventionFilter().getToDate())
      && StringUtils.isBlank(getInterventionFilter().getIntTypeId())      
      && StringUtils.isBlank(getInterventionFilter().getPersonId()) 
      && StringUtils.isBlank(getInterventionFilter().getCaseId())                  
      );
  }

  void clearLists()
  {
    interventionFilter.getProperty().clear();
    interventionFilter.getIntId().clear();
  }
  
  public void setInputProperties(List<Property> formProperties)
  {
    //intId
    if (!StringUtils.isBlank(intIdInput))
      interventionFilter.getIntId().add(intIdInput.trim());

    //description with wildcards
    if (!StringUtils.isBlank(commentsInput))
      interventionFilter.setComments(FilterUtils.addWildcards(commentsInput));
    else if (DictionaryUtils.getPropertyByName(formProperties, "comments") != null)
      interventionFilter.setComments(FilterUtils.addWildcards(interventionFilter.getComments()));
    else
      interventionFilter.setComments(null);
  }  

  public String getPropertyName1()
  {
    return propertyNameInput1;
  }

  public void setPropertyName1(String propertyNameInput1)
  {
    this.propertyNameInput1 = propertyNameInput1;
  }

  public String getPropertyName2()
  {
    return propertyNameInput2;
  }

  public void setPropertyName2(String propertyNameInput2)
  {
    this.propertyNameInput2 = propertyNameInput2;
  }

  public String getPropertyValue1()
  {
    return propertyValueInput1;
  }

  public void setPropertyValue1(String propertyValueInput1)
  {
    this.propertyValueInput1 = propertyValueInput1;
  }

  public String getPropertyValue2()
  {
    return propertyValueInput2;
  }

  public void setPropertyValue2(String propertyValueInput2)
  {
    this.propertyValueInput2 = propertyValueInput2;
  }
  
  public List<Property> getProperty()
  {
    if (interventionFilter == null)
      interventionFilter = new InterventionFilter();
    return interventionFilter.getProperty();
  }  
  
  public void setIntTypeId(String intTypeId)
  {
    this.interventionFilter.setIntTypeId(intTypeId);
  }
  
  public void setPersonId(String personId)
  {
    this.interventionFilter.setPersonId(personId);
  }
  
  public String getPersonId()
  {
    return this.interventionFilter.getPersonId();
  }
  
  public void setCaseId(String caseId)
  {
    this.interventionFilter.setCaseId(caseId);
  }
  
  public String getCaseId()
  {
    return this.interventionFilter.getCaseId();
  }

}
