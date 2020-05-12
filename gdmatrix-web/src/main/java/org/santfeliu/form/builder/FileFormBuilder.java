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
package org.santfeliu.form.builder;

import org.santfeliu.form.type.html.HtmlForm;

/**
 *
 * @author realor
 */
public abstract class FileFormBuilder extends AbstractFormBuilder
{
  public static final Object[][] formTypes = new Object[][]
  {
    {"htm", HtmlForm.class},
    {"html", HtmlForm.class},
    {"form", HtmlForm.class}
  };

  protected Class getFormClass(String filename)
  {
    Class formClass = null;
    int i = 0;
    while (i < formTypes.length && formClass == null)
    {
      if (filename.endsWith("." + formTypes[i][0]))
      {
        formClass = (Class)formTypes[i][1];
      }
      i++;
    }
    return formClass;
  }

  protected String getTitle(String filename)
  {
    int index = filename.lastIndexOf("/");
    if (index != -1)
    {
      filename = filename.substring(index + 1);
    }

    String title = filename;
    int index2 = title.lastIndexOf(".");
    if (index2 != -1)
    {
      title = title.substring(0, index2);
    }
    title = title.substring(0, 1).toUpperCase() + title.substring(1);
    return title;
  }
}
