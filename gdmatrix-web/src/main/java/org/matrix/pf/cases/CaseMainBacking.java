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

import javax.annotation.PostConstruct;
import javax.inject.Named;
import org.matrix.cases.Case;
import org.matrix.cases.CaseConstants;
import org.matrix.pf.web.PageBacking;
import org.matrix.pf.web.helper.TypedHelper;
import org.santfeliu.cases.web.CaseConfigBean;
import org.matrix.pf.web.helper.TypedPage;
import org.matrix.web.WebUtils;

/**
 *
 * @author blanquepa
 */
@Named("caseMainBacking")
public class CaseMainBacking extends PageBacking
  implements TypedPage
{
  private Case cas;
  private TypedHelper typedHelper;
  
  public CaseMainBacking()
  {
  }
  
  @PostConstruct
  public void init()
  {
    objectBacking = WebUtils.getInstance(CaseBacking.class);    
    typedHelper = new TypedHelper(this);
    load();
  }

  @Override
  public String getRootTypeId()
  {
    return objectBacking.getRootTypeId();
  }

  @Override
  public String getTypeId()
  {
    return getObjectTypeId();
  }

  @Override
  public String getAdminRole()
  {
    return CaseConstants.CASE_ADMIN_ROLE;
  }

  @Override
  public TypedHelper getTypedHelper()
  {
    return typedHelper;
  }

  public Case getCase()
  {
    return cas;
  }

  public void setCase(Case cas)
  {
    this.cas = cas;
  }

  @Override
  public String getPageId()
  {
    return objectBacking.getObjectId();
  }
  
  @Override
  public String show(String pageId)
  {
    objectBacking.setObjectId(pageId);
    return show();
  }
  
  @Override
  public String show()
  {
    load(); 
    return "pf_case_main";
  }
 
  public String store()
  {
    try
    {
      CaseConfigBean.getPort().storeCase(cas);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;    
  }
  
  public String remove()
  {
    error("Not implemented yet");
    return null;
  }
  
  public String cancel()
  {
    load();
    return null;
  }
    
  private void load()
  {
    String caseId = getPageId();
    if (caseId != null)
    {
      try
      {
        cas = CaseConfigBean.getPort().loadCase(caseId);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
  }
}
