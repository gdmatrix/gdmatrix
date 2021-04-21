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
package org.santfeliu.news.web;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.faces.model.SelectItem;
import org.apache.myfaces.custom.tabbedpane.HtmlPanelTabbedPane;
import org.matrix.cms.CMSConstants;
import org.matrix.dic.DictionaryConstants;
import org.matrix.news.New;
import org.matrix.news.NewStoreOptions;
import org.matrix.news.Source;
import org.santfeliu.doc.util.HtmlFixer;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.TypifiedPageBean;

/**
 *
 * @author unknown
 */
public class NewMainBean extends TypifiedPageBean
{  
  private New newObject;
  private String startDateTime;
  private String endDateTime;
  private transient HtmlPanelTabbedPane tabbedPane = new HtmlPanelTabbedPane();
  private List<SelectItem> sourceItems;
  private boolean cleanSummary;
  private boolean cleanText;

  private boolean cleanHtmlInputText;
  private boolean editingSummary;
  private String htmlInputText;
  private boolean customUrlTargetBlank;

  public NewMainBean()
  {
    super(DictionaryConstants.NEW_TYPE, CMSConstants.MENU_ADMIN_ROLE);
    load();
  }

  public String getEndDateTime()
  {
    return endDateTime;
  }

  public void setEndDateTime(String endDateTime)
  {
    this.endDateTime = endDateTime;
  }

  public String getStartDateTime()
  {
    return startDateTime;
  }

  public void setStartDateTime(String startDateTime)
  {
    this.startDateTime = startDateTime;
  }

  public void setNewObject(New newObject)
  {
    this.newObject = newObject;
  }

  public New getNewObject()
  {
    return newObject;
  }

  public void setTabbedPane(HtmlPanelTabbedPane tabbedPane)
  {
    this.tabbedPane = tabbedPane;
  }

  public HtmlPanelTabbedPane getTabbedPane()
  {
    return tabbedPane;
  }

  public void setSourceItems(List<SelectItem> sourceItems)
  {
    this.sourceItems = sourceItems;
  }

  public List<SelectItem> getSourceItems()
  {
    return sourceItems;
  }

  public boolean isCleanHtmlInputText()
  {
    return cleanHtmlInputText;
  }

  public void setCleanHtmlInputText(boolean cleanHtmlInputText)
  {
    if (isEditingSummary())
    {
      cleanSummary = cleanHtmlInputText;
    }
    else
    {
      cleanText = cleanHtmlInputText;
    }
    this.cleanHtmlInputText = cleanHtmlInputText;
  }

  public String getHtmlInputText()
  {
    return htmlInputText;
  }

  public void setHtmlInputText(String htmlInputText)
  {
    if (isEditingSummary())
    {
      newObject.setSummary(htmlInputText);
    }
    else
    {
      newObject.setText(htmlInputText);
    }
    this.htmlInputText = htmlInputText;
  }

  public boolean isEditingSummary()
  {
    return editingSummary;
  }

  public void setEditingSummary(boolean editingSummary)
  {
    this.editingSummary = editingSummary;
  }

  public boolean isCustomUrlTargetBlank() 
  {
    return customUrlTargetBlank;
  }

  public void setCustomUrlTargetBlank(boolean customUrlTargetBlank) 
  {
    this.customUrlTargetBlank = customUrlTargetBlank;
  }

  public String show()
  {
    try
    {
      if (isEditingSummary())
      {
        setHtmlInputText(newObject.getSummary());
        setCleanHtmlInputText(cleanSummary);
      }
      else
      {
        setHtmlInputText(newObject.getText());
        setCleanHtmlInputText(cleanText);
      }
      if ((newObject != null) && (newObject.getNewId() != null))
      {
        int totalReadCount = NewsConfigBean.getPort().
          getTotalNewCounterFromCache(newObject.getNewId());
        newObject.setTotalReadingCount(totalReadCount);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "new_main";
  }

  public String switchToSummary()
  {
    editingSummary = true;
    return show();
  }

  public String switchToText()
  {
    editingSummary = false;
    return show();
  }

  public String load()
  {
    try
    {
      editingSummary = true;
      cleanSummary = true;
      cleanText = true;
      List<Source> sources = NewsConfigBean.getPort().findSourcesFromCache();
      sourceItems = FacesUtils.getListSelectItems(sources, "id", "name", true);
      if (isNew())
      {
        initNew();
      }
      else
      {
        try
        {
          newObject = NewsConfigBean.getPort().loadNewFromCache(getObjectId());
          setStartDateTime(newObject.getStartDate() + newObject.getStartTime());
          setEndDateTime(newObject.getEndDate() + newObject.getEndTime());
          setCustomUrlTargetBlank("_blank".equals(
            newObject.getCustomUrlTarget()));
        }
        catch (Exception ex)
        {
          getObjectBean().clearObject();
          error(ex);
          initNew();
        }
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return show();
  }
  
  public String store()
  {
    try
    {
      SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
      Date now = new Date();
      if ((startDateTime == null) || (startDateTime.trim().length() == 0))
      {
        newObject.setStartDate(dayFormat.format(now));
        newObject.setStartTime("000000");
        setStartDateTime(newObject.getStartDate() + newObject.getStartTime());
      }
      else
      {        
        newObject.setStartDate(startDateTime.substring(0, 8));
        newObject.setStartTime(startDateTime.substring(8, 14));
      }
      if ((endDateTime == null) || (endDateTime.trim().length() == 0))
      {
        GregorianCalendar cal = new GregorianCalendar();
        cal.add(Calendar.DAY_OF_MONTH, 30);
        Date laterDate = cal.getTime();
        newObject.setEndDate(dayFormat.format(laterDate));
        newObject.setEndTime("000000");
        setEndDateTime(newObject.getEndDate() + newObject.getEndTime());
      }
      else
      {
        newObject.setEndDate(endDateTime.substring(0, 8));
        newObject.setEndTime(endDateTime.substring(8, 14));
      }
      newObject.setUserId(UserSessionBean.getCurrentInstance().getUsername());
      newObject.setCustomUrl(newObject.getCustomUrl().trim());
      newObject.setCustomUrlTarget(isCustomUrlTargetBlank() ? "_blank" : null);
      
      NewStoreOptions newStoreOptions = new NewStoreOptions();
      newStoreOptions.setCleanSummary(cleanSummary);
      newStoreOptions.setCleanText(cleanText);
      
      //Correcció dels atributs aria-label als enllaços
      String scriptName = MatrixConfig.getProperty("htmlFixer.script");
      String userId = MatrixConfig.getProperty("adminCredentials.userId");
      String password = MatrixConfig.getProperty("adminCredentials.password");
      HtmlFixer htmlFixer = new HtmlFixer(scriptName, userId, password);
      newObject.setSummary(htmlFixer.fixCode(newObject.getSummary()));
      newObject.setText(htmlFixer.fixCode(newObject.getText()));

      newObject = NewsConfigBean.getPort().storeNew(newObject, newStoreOptions);
      setObjectId(newObject.getNewId());
    }
    catch (Exception ex)
    { 
      error(ex);
    }
    return show();
  }

  public String getEditSummaryButtonTitle()
  {
    Locale locale = getFacesContext().getViewRoot().getLocale();
    ResourceBundle bundle =
      ResourceBundle.getBundle("org.santfeliu.news.web.resources.NewsBundle",
      locale);
    if (editingSummary)
    {
      return bundle.getString("new_main_editingSummary");
    }
    else
    {
      return bundle.getString("new_main_summary");
    }
  }

  public String getEditTextButtonTitle()
  {
    Locale locale = getFacesContext().getViewRoot().getLocale();
    ResourceBundle bundle =
      ResourceBundle.getBundle("org.santfeliu.news.web.resources.NewsBundle",
      locale);
    if (editingSummary)
    {
      return bundle.getString("new_main_text");
    }
    else
    {
      return bundle.getString("new_main_editingText");
    }
  }

  public String getCleanCheckBoxTitle()
  {
    Locale locale = getFacesContext().getViewRoot().getLocale();
    ResourceBundle bundle =
      ResourceBundle.getBundle("org.santfeliu.news.web.resources.NewsBundle",
      locale);
    if (editingSummary)
    {
      return bundle.getString("new_main_cleanSummary");
    }
    else
    {
      return bundle.getString("new_main_cleanText");
    }
  }

  private void initNew()
  {
    newObject = new New();
    newObject.setDraft(true);
  }
  
}
