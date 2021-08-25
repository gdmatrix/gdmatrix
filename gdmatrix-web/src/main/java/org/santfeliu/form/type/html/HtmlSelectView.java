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
package org.santfeliu.form.type.html;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.form.View;
import org.santfeliu.util.Table;
import org.santfeliu.util.data.DataProviderFactory;
import org.santfeliu.util.data.DataProvider;

/**
 *
 * @author realor
 */
public class HtmlSelectView extends HtmlView
{
  private DataProvider dataProvider;

  public void setDataref(String reference)
  {
    properties.put("dataref", reference);
    dataProvider = null;
  }

  public String getDataref()
  {
    return properties.get("dataref");
  }

  public String getSql()
  {
    return properties.get("sql");
  }

  public void setSql(String sql)
  {
    properties.put("sql", sql);
    dataProvider = null;
  }

  public String getConnection()
  {
    return properties.get("connection");
  }

  public void setConnection(String connection)
  {
    properties.put("connection", connection);
    dataProvider = null;
  }

  public String getUsername()
  {
    return properties.get("username");
  }

  public void setUsername(String username)
  {
    properties.put("username", username);
    dataProvider = null;
  }

  public String getPassword()
  {
    return properties.get("password");
  }

  public void setPassword(String password)
  {
    properties.put("password", password);
    dataProvider = null;
  }

  @Override
  public void setProperty(String name, String value)
  {
    super.setProperty(name, value);
    if (name.equals("sql")) dataProvider = null;
  }

  public void evaluate(HtmlForm form, Map context)
  {
    try
    {
      DataProvider provider = getDataProvider();
      if (provider != null)
      {
        Table result = provider.getData(context);
        for (int i = 0; i < result.getRowCount(); i++)
        {
          HtmlView option = new HtmlView();
          option.tag = "option";
          option.setViewType(View.ITEM);
          option.setProperty("value",
            String.valueOf(result.getElementAt(i, 0)));
          if (result.getRow(i).size() >= 3 && 
            result.getElementAt(i, 2) instanceof String)
          {
            String title = (String)result.getElementAt(i, 2);
            if (!StringUtils.isBlank(title))
            {
              option.setProperty("title", title);
            }
          }
          HtmlView optionValue = new HtmlView();
          optionValue.tag = "#text";
          optionValue.setViewType(View.TEXT);
          optionValue.setProperty("text",
            String.valueOf(result.getElementAt(i, 1)));
          option.getChildren().add(optionValue);
          getChildren().add(option);
        }
      }
    }
    catch (Exception ex)
    {
      // ignore
    }
  }

  public List<String> getParameters(HtmlForm form)
  {
    try
    {
      DataProvider provider = getDataProvider();
      if (provider != null) return provider.getParameters();
    }
    catch (Exception ex)
    {
      // ignore
    }
    return Collections.EMPTY_LIST;
  }

  private DataProvider getDataProvider() throws Exception
  {
    if (dataProvider == null)
    {
      String dataref = null;

      String sql = getSql();
      if (sql != null) // old way
      {
        String connection = getConnection();
        if (connection == null) connection = "";
        String username = getUsername();
        if (username == null) username = "";
        String password = getPassword();
        if (password == null) password = "";
        sql = sql.replaceAll("\\\\n", "\n");
        dataref = "sql:" + connection + ":" + username + ":" + password +
          ":" + sql;
      }
      else // new way
      {
        dataref = getDataref();
      }
      // instance dataProvider
      if (!StringUtils.isBlank(dataref))
      {
        DataProviderFactory factory = DataProviderFactory.getInstance();
        dataProvider = factory.createProvider(dataref);
      }
    }
    return dataProvider;
  }
}
