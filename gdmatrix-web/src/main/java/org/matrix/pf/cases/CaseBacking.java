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

import javax.inject.Named;
import org.matrix.cases.Case;
import org.matrix.cases.CaseConstants;
import org.matrix.pf.cms.CMSContent;
import org.matrix.pf.web.ObjectBacking;
import org.matrix.pf.web.SearchBacking;
import org.matrix.web.WebUtils;
import org.santfeliu.cases.web.CaseConfigBean;

/**
 *
 * @author blanquepa
 */
@CMSContent(typeId = "Case")
@Named("caseBacking")
public class CaseBacking extends ObjectBacking
{
  public CaseBacking()
  {
    super();
  }
  
  @Override
  public void loadTabs()
  {
    super.loadTabs();
  }
  
  @Override
  public SearchBacking getSearchBacking()
  {
    return WebUtils.getInstance(CaseSearchBacking.class);
  }

  @Override
  public String getObjectId(Object obj)
  {
    Case cas = (Case)obj;
    return cas.getCaseId();
  }
  
  @Override
  public String getDescription()
  {
    CaseMainBacking mainBacking = WebUtils.getInstance(CaseMainBacking.class);
    if (mainBacking != null)
      return getDescription(mainBacking.getCase());
    else
      return super.getDescription();
  }  
  
  @Override
  public String getDescription(String objectId)
  {
    String description = "";
    objectId = super.getDescription(objectId);
    try
    {
      Case cas = CaseConfigBean.getPort().loadCase(objectId);
      if (cas != null)
        description = getDescription(cas);
    }
    catch (Exception ex)
    {
      error(ex.getMessage());
    }
    return description;
  }
  
  @Override
  public String getDescription(Object obj)
  {
    Case cas = (Case)obj;
    if (cas == null) return "";
    return cas.getTitle();
  }   

  @Override
  public String show()
  {
    return super.show(); 
  }

  @Override
  public boolean remove(String objectId)
  {
    try
    {
      return CaseConfigBean.getPort().removeCase(objectId);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return false;
  }

  @Override
  public String getAdminRole()
  {
    return CaseConstants.CASE_ADMIN_ROLE;
  }
  
  @Override
  public boolean isEditable()
  {
    //TODO:
    return true;
  }  
       
}
