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
package org.santfeliu.web.obj;

import java.lang.reflect.Constructor;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.faces.beansaver.Savable;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;

/**
 *
 * @author blanquepa
 */
public abstract class DetailPanel extends WebBean implements Savable
{
  public static final String HEADER_DOCUMENT_PROPERTY = "header.url";
  public static final String FOOTER_DOCUMENT_PROPERTY = "footer.url";
  public static final String PANEL_STYLECLASS_PROPERTY = "panelStyleClass";
  public static final String PANEL_ARIAHIDDEN_PROPERTY = "panelAriaHidden";
  public static final String TITLE_PROPERTY = "title";
  public static final String HEADING_LEVEL_PROPERTY = "headingLevel";

  private String mid;

  private String headerBrowserUrl;
  private String footerBrowserUrl;
  private int first = 0;

  public DetailPanel()
  {
  }

  public static DetailPanel getInstance(String panelClassName, String mid)
  {
    try
    {
      Class panelClass = Class.forName(panelClassName);
      Constructor c = panelClass.getConstructor();
      DetailPanel detailPanel = (DetailPanel) c.newInstance();
      detailPanel.setMid(mid);
      return detailPanel;
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  public int getFirst()
  {
    return first;
  }

  public void setFirst(int first)
  {
    this.first = first;
  }

  public String getMid()
  {
    return mid;
  }

  public void setMid(String mid)
  {
    this.mid = mid;
  }

  public String getTitle()
  {
    String title = getProperty(TITLE_PROPERTY);
    if (StringUtils.isBlank(title))
      title = getLabel();
    return title;
  }

  public String getLabel()
  {
    String label = getProperty(WebBean.LABEL_PROPERTY);
    if (StringUtils.isBlank(label))
      label = getType();
    return label;
  }

  @Override
  public String getProperty(String propertyName)
  {
    return UserSessionBean.getCurrentInstance().getMenuModel().getMenuItem(mid).
      getProperty(propertyName);
  }

  public String getProperty(String propertyName, String defaultValue)
  {
    String value = getProperty(propertyName);
    return value != null ? value : defaultValue;
  }

  public List<String> getMultivaluedProperty(String propertyName)
  {
    return UserSessionBean.getCurrentInstance().getMenuModel().getMenuItem(mid).
      getDirectMultiValuedProperty(propertyName);
  }

  public ControllerBean getControllerBean()
  {
    return ControllerBean.getCurrentInstance();
  }

  public void load(DetailBean detailBean)
  {
    loadData(detailBean);
  }

  public String getHeaderBrowserUrl()
  {
    headerBrowserUrl = getProperty(HEADER_DOCUMENT_PROPERTY);
    return headerBrowserUrl;
  }

  public void setHeaderBrowserUrl(String headerBrowserUrl)
  {
    this.headerBrowserUrl = headerBrowserUrl;
  }

  public String getFooterBrowserUrl()
  {
    footerBrowserUrl = getProperty(FOOTER_DOCUMENT_PROPERTY);
    return footerBrowserUrl;
  }

  public void setFooterBrowserUrl(String footerBrowserUrl)
  {
    this.footerBrowserUrl = footerBrowserUrl;
  }

  public String getStyleClass()
  {
    String styleClass = getProperty(PANEL_STYLECLASS_PROPERTY);
    return styleClass != null ? styleClass : "casePanel";
  }
  
  public Boolean getAriaHidden()
  {
    String ariaHidden = getProperty(PANEL_ARIAHIDDEN_PROPERTY, "false");
    return Boolean.valueOf(ariaHidden);
  }
  
  public Integer getHeadingLevel()
  {
    String hlevel = getProperty(HEADING_LEVEL_PROPERTY);
    if (hlevel != null)
    {
      Integer level = Integer.valueOf(hlevel);
      if (level >= 1 && level <= 6)
        return level;
      else
        return null;
    }
    else
      return null;
  }

  public abstract void loadData(DetailBean detailBean);

  public abstract boolean isRenderContent();

  public abstract String getType();
}
