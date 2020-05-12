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
package org.santfeliu.cases.web.detail;

import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.Case;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.form.Form;
import org.santfeliu.form.builder.TypeFormBuilder;
import org.santfeliu.web.obj.DetailBean;
import org.santfeliu.web.obj.DetailPanel;
import org.santfeliu.web.obj.util.DynamicFormsManager;

/**
 *
 * @author blanquepa
 */
public class DynamicFormDetailPanel extends DetailPanel
{
  private static final String RENDER_IF_PROPERTY_EXISTS = "renderIfPropertyExists";
  private static final String RENDERER_TYPES = "rendererTypes";

  private CaseDetailBean detailBean;
  private DynamicFormsManager dynamicFormsManager;
  private String renderIfPropertyExists;

  @Override
  public void loadData(DetailBean detailBean)
  {
    this.detailBean = (CaseDetailBean) detailBean;

    renderIfPropertyExists = getProperty(RENDER_IF_PROPERTY_EXISTS);
    
    dynamicFormsManager = new DynamicFormsManager(TypeFormBuilder.VIEW_PREFIX,
      getMultivaluedProperty(DynamicFormsManager.ALLOWED_FORM_SELECTORS));
    Case cas = this.detailBean.getCase();
    dynamicFormsManager.setCurrentTypeId(cas.getCaseTypeId());
    dynamicFormsManager.setFormDataFromProperties(
      DictionaryUtils.getProperties(cas), cas.getCaseId());
  }

  public Map getData()
  {
    return dynamicFormsManager.getData();
  }

  public void setData(Map data)
  {
    dynamicFormsManager.setData(data);
  }

  public Form getForm() 
  {
    try
    {
      return dynamicFormsManager.getForm();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return null;
    }
  }

  public boolean isTypeUndefined()
  {
    return dynamicFormsManager.isTypeUndefined();
  }

  public String getSelector()
  {
    return dynamicFormsManager.getSelector();
  }

  @Override
  public boolean isRenderContent()
  {
    return
      getSelector() != null && !isEmptyData();
  }

  @Override
  public String getType()
  {
    return "dynamicform";
  }
  
  public String getRendererTypes()
  {
    String rendererTypes = getProperty(RENDERER_TYPES);
    if (StringUtils.isBlank(rendererTypes))
      rendererTypes = "DisabledHtmlFormRenderer";
    return rendererTypes;
  }
  
  private boolean isEmptyData()
  {
    Map data = dynamicFormsManager.getData();
    return (data == null
      || data.isEmpty()
      || (renderIfPropertyExists != null &&
        (!data.containsKey(renderIfPropertyExists) || data.get(renderIfPropertyExists) == null)));
  }
}
