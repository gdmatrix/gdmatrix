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
          optionValue.setViewType(View.LABEL);
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
