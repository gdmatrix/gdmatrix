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

import java.io.StringReader;
import java.io.StringWriter;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.DetailPanel;

/**
 * Implemented by panels that shows results on a dataTable.
 * 
 * @author blanquepa
 */
public abstract class TabulatedDetailPanel extends DetailPanel
{
  public static final String TABLE_SUMMARY_PROPERTY = "tableSummary";
  
  private String tableSummary;

  public String getTableSummary()
  {
    if (this.tableSummary != null)
      return tableSummary;
    else
    {
      String tableSummary = getProperty(TABLE_SUMMARY_PROPERTY);
      Translator translator = UserSessionBean.getCurrentInstance().getTranslator();
      String translationGroup = UserSessionBean.getCurrentInstance().getTranslationGroup();
      if (translator != null)
      {
        String userLanguage = FacesUtils.getViewLanguage();
        StringWriter sw = new StringWriter();
        try
        {
          translator.translate(new StringReader(tableSummary), sw, "text/plain",
            userLanguage, translationGroup);
          this.tableSummary = sw.toString();
        }
        catch (Exception ex)
        {
          //Summary not translated
          this.tableSummary = tableSummary;
        }
      }    
    }
    return this.tableSummary;
  }
 
}
