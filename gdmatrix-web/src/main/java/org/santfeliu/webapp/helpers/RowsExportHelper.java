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
package org.santfeliu.webapp.helpers;

import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.setup.EditTab;
import org.santfeliu.webapp.setup.SearchTab;

/**
 *
 * @author lopezrj-sf
 */
public class RowsExportHelper 
{
  private static final int DEFAULT_ROW_EXPORT_LIMIT = 1000;
  
  public static int getActiveSearchTabRowExportLimit(ObjectBean objectBean)
  {
    SearchTab activeSearchTab = objectBean.getActiveSearchTab();
    if (activeSearchTab == null)
    {
      activeSearchTab = objectBean.getSearchTabs().get(0);
    }
    if (activeSearchTab.getRowExportLimit() != null)
      return activeSearchTab.getRowExportLimit();
    else
      return DEFAULT_ROW_EXPORT_LIMIT;
  }
  
  public static boolean isActiveSearchTabExportable(ObjectBean objectBean)
  {
    SearchTab activeSearchTab = objectBean.getActiveSearchTab();
    if (activeSearchTab == null)
    {
      activeSearchTab = objectBean.getSearchTabs().get(0);
    }
    if (activeSearchTab.getExportable() != null)
      return activeSearchTab.getExportable();
    else
      return true;
  }
  
  public static int getActiveEditTabRowExportLimit(ObjectBean objectBean)
  {
    EditTab activeEditTab = objectBean.getActiveEditTab();
    if (activeEditTab != null && activeEditTab.getRowExportLimit() != null)
      return activeEditTab.getRowExportLimit();
    else
      return DEFAULT_ROW_EXPORT_LIMIT;
  }
  
  public static boolean isActiveEditTabExportable(ObjectBean objectBean)
  {
    EditTab activeEditTab = objectBean.getActiveEditTab();
    if (activeEditTab != null && activeEditTab.getExportable() != null)
      return activeEditTab.getExportable();
    else
      return true;
  }
}
