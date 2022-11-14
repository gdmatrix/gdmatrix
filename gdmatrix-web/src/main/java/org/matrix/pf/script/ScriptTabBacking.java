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
import org.matrix.pf.web.helper.TabHelper;
import org.matrix.pf.web.helper.TabPage;

/**
 *
 * @author blanquepa
 */
public class ScriptTabBacking extends ScriptBacking
  implements TabPage
{
  private TabHelper tabHelper;  

  @PostConstruct
  @Override
  public void init()
  { 
    super.init();
    tabHelper = new TabHelper(this);
  }  
  
  @Override
  public void populate()
  {
    load();
  }

  @Override
  public void create()
  {
    try 
    {
      scriptHelper.call("create");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  @Override
  public void load()
  {
    try
    {
      scriptHelper.show();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  @Override
  public String store()
  {
    String outcome = null;
    try
    {
      outcome = (String) scriptHelper.call("store");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @Override
  public void reset()
  {
    try
    {
      scriptHelper.call("reset");
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }  
  
  @Override
  public String cancel()
  {
    String outcome = null;
    try
    {
      outcome = (String) scriptHelper.call("cancel");
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return outcome;
  }  
  
  @Override
  public TabHelper getTabHelper()
  {
    return tabHelper;
  }  
}
