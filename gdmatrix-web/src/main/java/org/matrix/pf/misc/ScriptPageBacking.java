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
package org.matrix.pf.misc;

import javax.inject.Named;
import org.matrix.pf.web.helper.TabPage;

/**
 *
 * @author blanquepa
 */
@Named("scriptPageBacking")
public class ScriptPageBacking extends ScriptBacking implements TabPage
{
  private static final String OUTCOME = "pf_script_page";  

  private PageType pageType = PageType.OBJECT;

  private enum PageType
  {
    OBJECT,
    OBJECT_SEARCH
  }
  
  //TODO do it better
  public String getObjectCommonPage()
  {
    if (objectBacking != null)
    {
      String pageTypeId = objectBacking.getPageTypeId();
      if (pageTypeId != null)
      {
        switch (pageTypeId)
        {
          case "Person":
            return "kernel/person_common";
          case "Case":
            return "cases/case_common";
          default:
            return "obj/object_common";
        }
      }
    }
    return "obj/object_common";
  }
  
  public String getObjectPageType()
  {    
    return pageType.toString().toLowerCase();
  }
  
  @Override
  public String show(String page, String backing)
  {        
    super.show(page, backing);
    populate();    
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
  public void create()
  {
    call("create"); 
  }

  @Override
  public void load()
  {
    call("populate");
  }

  @Override
  public String store()
  {
    return (String) call("store");
  }

  @Override
  public void reset()
  {
    call("reset");
  }  

  @Override
  public String getPageObjectId()
  {
    return null; //TODO
  }

}
