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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormDescriptor;
import org.santfeliu.form.type.html.HtmlForm;

/**
 *
 * @author realor
 */
public class URLFormBuilder extends FileFormBuilder
{
  public static final String PREFIX = "url";

  public List<FormDescriptor> findForms(String selector)
  {
    URL url = getURL(selector);
    if (url != null)
    {
      String filename = url.getFile();
      String title = getTitle(filename);
      FormDescriptor descriptor = new FormDescriptor();
      descriptor.setTitle(title);
      descriptor.setSelector(selector);
      return Collections.singletonList(descriptor);
    }
    return Collections.EMPTY_LIST;
  }

  public Form getForm(String selector)
  {
    Form form = null;
    URL url = getURL(selector);
    if (url != null)
    {
      Class formClass = getFormClass(url.getFile());
      if (formClass == null)
      {
        try
        {
          URLConnection conn = url.openConnection();
          String contentType = conn.getContentType();
          if (contentType.indexOf("html") != -1)
          {
            formClass = HtmlForm.class;
          }
        }
        catch (Exception ex)
        {
        }
      }
      if (formClass != null)
      {
        try
        {
          form = (Form)formClass.newInstance();
          form.read(url.openStream());
          setup(form);
        }
        catch (Exception ex)
        {
          throw new RuntimeException(ex);
        }
      }
    }
    return form;
  }

  protected URL getURL(String selector)
  {
    URL url = null;
    if (selector.startsWith(PREFIX + ":"))
    {
      String urlString = selector.substring(PREFIX.length() + 1);
      try
      {
        url = new URL(urlString);
      }
      catch (MalformedURLException ex)
      {
      }
    }
    return url;
  }
}
