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

import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import org.matrix.cases.Case;
import org.matrix.cases.CaseConstants;
import org.matrix.dic.Property;
import org.matrix.pf.script.ScriptFormHelper;
import org.matrix.pf.web.PageBacking;
import org.matrix.pf.web.helper.TabHelper;
import org.matrix.pf.web.helper.TypedHelper;
import org.matrix.pf.web.helper.TypedTabPage;
import org.matrix.web.WebUtils;
import org.santfeliu.cases.web.CaseConfigBean;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.matrix.pf.script.ScriptFormPage;
import org.matrix.pf.web.MainPage;

/**
 *
 * @author blanquepa
 */
@Named("caseMainBacking")
public class CaseMainBacking extends PageBacking
  implements TypedTabPage, ScriptFormPage, MainPage
{
  public static final String SHOW_AUDIT_PROPERTIES = "_showAuditProperties";
  public static final String OUTCOME = "pf_case_main";  
  
  private Case cas;
  private TypedHelper typedHelper;
  private TabHelper tabHelper;
  private ScriptFormHelper scriptFormHelper;
  
  private CaseBacking caseBacking;

  public CaseMainBacking()
  {
    //Let to super class constructor.  
  }
  
  @PostConstruct
  public void init()
  {
    caseBacking = WebUtils.getBacking("caseBacking");     
    typedHelper = new TypedHelper(this);  
    tabHelper = new TabHelper(this);
    scriptFormHelper = new ScriptFormHelper(this);
  }

  @Override
  public CaseBacking getObjectBacking()
  {
    return caseBacking;
  }

  @Override
  public String getRootTypeId()
  {
    return caseBacking.getRootTypeId();
  }

  @Override
  public String getTypeId()
  {
    if (cas != null) 
      return cas.getCaseTypeId();
    else
      return getMenuItemTypeId();
  }

  @Override
  public TypedHelper getTypedHelper()
  {
    return typedHelper;
  } 

  @Override
  public TabHelper getTabHelper()
  {
    return tabHelper;
  }

  @Override
  public ScriptFormHelper getScriptFormHelper()
  {
    return scriptFormHelper;
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
  public String getPageObjectId()
  {
    return caseBacking.getObjectId();
  }
  
  public Date getStartDateTime()
  {
    if (cas != null && cas.getStartDate() != null)
      return getDate(cas.getStartDate(), cas.getStartTime());
    else
      return null;
  }
  
  public Date getEndDateTime()
  {
    if (cas != null && cas.getEndDate() != null)
      return getDate(cas.getEndDate(), cas.getEndTime());
    else
      return null;
  }  
    
  public void setStartDateTime(Date date)
  {
    if (date != null && cas != null)
    {
      cas.setStartDate(TextUtils.formatDate(date, "yyyyMMdd"));
      cas.setStartTime(TextUtils.formatDate(date, "HHmmss"));
    }
  }
  
  public void setEndDateTime(Date date)
  {
    if (date != null && cas != null)
    {
      cas.setEndDate(TextUtils.formatDate(date, "yyyyMMdd"));
      cas.setEndTime(TextUtils.formatDate(date, "HHmmss"));
    }
  }  
  
  @Override
  public String show(String pageId)
  {
    caseBacking.setObjectId(pageId);
    return show();
  }
  
  @Override
  public String show()
  {
    populate(); 
    return OUTCOME;
  }
  
  @Override
  public void reset()
  {
    create();
  }
  
  @Override
  public void create()
  {
    cas = new Case();   
    cas.setCaseTypeId(getMenuItemTypeId());
  }
  
  @Override
  public void load()
  {
    String caseId = getPageObjectId();
    if (caseId != null)
    {
      try
      {
        cas = CaseConfigBean.getPort().loadCase(caseId);
        scriptFormHelper.reload();
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
  }  
  
  @Override
  public String store()
  {
    try
    {
      //TODO: Incremental store (in helper?)
      scriptFormHelper.mergeProperties();
      CaseConfigBean.getPort().storeCase(cas);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;    
  }
  
  @Override
  public List<Property> getProperties()
  {
    return cas.getProperty();
  }
    
  @Override
  public String cancel()
  {
    populate();
    return null;
  }
  
  //TODO: AuditHelper?
  public boolean isShowAuditProperties()
  {
    try
    {
      if (UserSessionBean.getCurrentInstance().isUserInRole(
        CaseConstants.CASE_ADMIN_ROLE))
        return true;
      
      if (cas == null)
        return false;

      org.santfeliu.dic.Type type =
        TypeCache.getInstance().getType(cas.getCaseTypeId());
      if (type != null)
      {
        String showAuditProperty = getProperty(SHOW_AUDIT_PROPERTIES);
        return showAuditProperty == null || 
          !"false".equalsIgnoreCase(showAuditProperty);
      }
      else 
        return true;
    }
    catch (Exception ex)
    {
      return true;
    }
  }  
      
  private Date getDate(String date, String time)
  {
    String dateTime = 
      TextUtils.concatDateAndTime(date, time);
    return TextUtils.parseInternalDate(dateTime);    
  }  
}
