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
package org.matrix.pf.script;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import org.matrix.pf.cms.CMSContent;
import org.matrix.pf.web.ObjectBacking;
import org.matrix.pf.web.PageBacking;

/**
 * @author blanquepa
 */
@CMSContent(typeId = "Script")
@Named("scriptBacking")
public class ScriptBacking extends PageBacking 
  implements ScriptPage
{
  private static final String OUTCOME = "pf_script"; 
  
  protected String pageName;
  protected String label;  
  protected String beanName; //Name of the scriptBean 
  
  protected ScriptHelper scriptHelper;

  public ScriptBacking()
  {
  }
  
  @PostConstruct
  public void init()
  { 
    scriptHelper = new ScriptHelper(this);
  }
  
  @Override
  public ObjectBacking getObjectBacking()
  {
    return null;
  }

  @Override
  public String getScriptPageName()
  {
    return pageName;
  }

  @Override
  public String getScriptBackingName()
  {
    return beanName;
  }

  public String getXhtmlFormUrl()
  {
    return scriptHelper.getXhtmlFormUrl(pageName);
  }    
  
  public String show(String pageName, String beanName)
  {
    try
    {
      this.pageName = pageName;
      this.label = getSelectedMenuItem().getLabel();
      this.beanName = beanName != null ? beanName : null;    
      scriptHelper.show();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return OUTCOME;
  } 

  @Override
  public String show(String page)
  {
    return show(page, null);
  }
  
  @Override
  public String show()
  {
    return show(null);
  } 
  
  @Override
  public String getPageObjectId()
  {
    String objectId = null;
    try 
    {
      objectId = (String) scriptHelper.call("getObjectId");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return objectId;
  } 

  public ScriptHelper getScriptHelper()
  {
    return scriptHelper;
  }
     
}
