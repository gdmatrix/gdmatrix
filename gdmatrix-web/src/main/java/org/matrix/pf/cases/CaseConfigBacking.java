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
package org.matrix.pf.cases;

import org.matrix.pf.cms.CMSConfigHelper;
import org.matrix.pf.web.WebBacking;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author blanquepa
 */
public class CaseConfigBacking extends WebBacking
{
  @CMSProperty
  public static final String LOAD_METADATA_PROPERTY = "loadMetadata";
  @CMSProperty
  public static final String RENDER_SEARCH_EXPRESSION_PROPERTY =
    "renderSearchExpression";
  
  private final CMSConfigHelper configHelper;

  public CaseConfigBacking(CMSConfigHelper configHelper)
  {
    this.configHelper = configHelper;
  }
  
  public String getLoadMetadata()
  {
    return getProperty(LOAD_METADATA_PROPERTY);
  }
  
  public void setLoadMetadata(String loadMetadata)
  {
    configHelper.setProperty(LOAD_METADATA_PROPERTY, loadMetadata);
  }
  
  public String getRenderSearchExpression()
  {
    return getProperty(RENDER_SEARCH_EXPRESSION_PROPERTY);
  }
  
  public void setRenderSearchExpression(String renderSearchExpression)
  {
    configHelper.setProperty(RENDER_SEARCH_EXPRESSION_PROPERTY, 
      renderSearchExpression);
  }  
  
}
