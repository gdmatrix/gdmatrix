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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.template.WebTemplate;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.Shareable;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author lopezrj-sf
 */
public abstract class ShareLinksHelper
{
  protected static final String DETAILS_SHARE_TABLE_RENDER =
    "details.shareTable.render";
  protected static final String DETAILS_SHARE_URL_PATTERN =
    "details.shareURLPattern";
  protected static final String DETAILS_SHARE_IMAGE_URL =
    "details.shareImageURL";
  protected static final String DETAILS_SHARE_URL_BROWSER_TYPE =
    "details.shareURLBrowserType";
  protected static final String DETAILS_SHARE_URL_TARGETS =
    "details.shareURLTargets";
  protected static final String DETAILS_SHARE_TEXT =
    "details.shareText";
  protected static final String DETAILS_SHARE_BY_EMAIL_ENABLED =
    "details.shareByEmail.enabled";
  protected static final String DETAILS_SHARE_BY_EMAIL_ICON =
    "details.shareByEmail.icon";

  protected abstract List<String> getShareURLList();

  public boolean isSharingEnabled()
  {
    return isRenderShareTable() && !getShareLinkList().isEmpty();
  }

  public List<Shareable.ShareLink> getShareLinkList()
  {
    List<Shareable.ShareLink> result = new ArrayList();
    List<String> shareURLList = getShareURLList();
    List<String> shareImageURLList = getShareImageURLList();
    Map<String, String> shareTargetMap = getShareTargetMap();
    String shareText = getShareText();
    if (shareURLList.size() == shareImageURLList.size())
    {
      List<String> shareURLBrowserTypeList = getShareURLBrowserTypeList();
      boolean includeAll =
      (
        shareURLBrowserTypeList.isEmpty()
        ||
        shareURLBrowserTypeList.size() != shareURLList.size()
      );
      int itemCount = shareURLList.size();
      for (int i = 0; i < itemCount; i++)
      {
        boolean include = includeAll;
        if (!includeAll)
        {
          String shareURLBrowserType = shareURLBrowserTypeList.get(i);
          include = Arrays.asList(shareURLBrowserType.split(",")).contains(
            UserSessionBean.getCurrentInstance().getBrowserType());
        }
        if (include)
        {
          String shareTarget = getShareTarget(shareTargetMap,
            shareURLList.get(i));
          String shareDescription = "Comparteix per " + shareTarget;
          Shareable.ShareLink item = new Shareable.ShareLink(shareURLList.get(i),
            shareImageURLList.get(i), shareDescription);
          result.add(item);
        }
      }
      if (isRenderShareByEmail() && getShareByEmailIcon() != null)
      {
        String shareDescription = "Comparteix per e-mail";
        Shareable.ShareLink item = new Shareable.ShareLink("EMAIL", getShareByEmailIcon(),
          shareDescription);
        result.add(item);
      }
    }
    return result;
  }

  public boolean isRenderShareByEmail()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(DETAILS_SHARE_BY_EMAIL_ENABLED);
    return (value != null ? value.equals("true") : false);
  }

  protected List<String> getShareURLList(Map<String, String> propertyMap)
  {
    List<String> result = new ArrayList<>();
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    List<String> values = menuItem.getMultiValuedProperty(
      DETAILS_SHARE_URL_PATTERN);
    for (String shareURLPattern : values)
    {
      Properties properties = new Properties();
      for (String key : propertyMap.keySet())
      {
        properties.setProperty(key, propertyMap.get(key));
      }
      String url = WebTemplate.create(shareURLPattern).merge(properties);
      result.add(url);
    }
    return result;
  }

  protected String translatePlainText(String text, String objectId,
    String language, String groupPrefix)
  {
    return translateText(text, objectId, language, groupPrefix, false);
  }

  protected String translateHtmlText(String text, String objectId,
    String language, String groupPrefix)
  {
    return translateText(text, objectId, language, groupPrefix, true);
  }

  private List<String> getShareImageURLList()
  {
    List<String> result = new ArrayList<>();
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    List<String> values = menuItem.getMultiValuedProperty(
      DETAILS_SHARE_IMAGE_URL);
    for (String shareImageURL : values)
    {
      result.add(shareImageURL);
    }
    return result;
  }

  private List<String> getShareURLBrowserTypeList()
  {
    List<String> result = new ArrayList();
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    List<String> values = menuItem.getMultiValuedProperty(
      DETAILS_SHARE_URL_BROWSER_TYPE);
    for (String shareURLBrowserType : values)
    {
      result.add(shareURLBrowserType);
    }
    return result;
  }

  private Map<String, String> getShareTargetMap()
  {
    Map<String, String> result = new HashMap();
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String targets = menuItem.getProperty(DETAILS_SHARE_URL_TARGETS);
    if (targets != null)
    {
      try
      {
        for (String target : targets.split(","))
        {
          String token = target.split(":")[0];
          String description = target.split(":")[1];
          result.put(token, description);
        }
      }
      catch (Exception ex)
      {
        //nothing here
      }
    }
    return result;
  }

  private String getShareTarget(Map<String, String> shareTargetMap,
    String shareUrl)
  {
    if (shareTargetMap != null && shareUrl != null)
    {
      for (String token : shareTargetMap.keySet())
      {
        if (shareUrl.contains(token)) return shareTargetMap.get(token);
      }
    }
    return "";
  }

  private boolean isRenderShareTable()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(DETAILS_SHARE_TABLE_RENDER);
    return (value != null ? value.equals("true") : false);
  }

  private String getShareByEmailIcon()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(DETAILS_SHARE_BY_EMAIL_ICON);
    return value;
  }

  private String getShareText()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    return menuItem.getProperty(DETAILS_SHARE_TEXT);
  }

  private String translateText(String text, String objectId, String language,
    String groupPrefix, boolean isHtml)
  {
    try
    {
      if (text != null && text.trim().length() > 0)
      {
        ApplicationBean applicationBean = ApplicationBean.getCurrentInstance();
        ApplicationBean.WebTranslator tr =
          (ApplicationBean.WebTranslator)applicationBean.getTranslator();
        StringWriter sw = new StringWriter();
        String group = groupPrefix + ":" + objectId;
        tr.translate(new StringReader(text), sw,
          (isHtml ? "text/html" : "text/plain"), language, group);
        return sw.toString();
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return "";
  }

}
