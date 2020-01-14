package org.santfeliu.misc.webmap.web;

import java.io.Serializable;

import java.util.HashSet;
import java.util.Set;

import javax.faces.context.FacesContext;


import org.santfeliu.faces.browser.HtmlBrowser;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.faces.menu.util.MenuUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;

@CMSManagedBean
public class WebMapBean extends WebBean implements Serializable
{
  @CMSProperty @Deprecated
  public static final String HEADER_DOCUMENT_PROPERTY = "header.document";
  @CMSProperty @Deprecated
  public static final String FOOTER_DOCUMENT_PROPERTY = "footer.document";
  @CMSProperty
  public static final String HEADER_DOCID_PROPERTY = "header.docId";
  @CMSProperty
  public static final String FOOTER_DOCID_PROPERTY = "footer.docId";
  @CMSProperty
  public static final String BASEMID_PROPERTY = "baseMid";
  
  private static final String URL_PROPERTY = "url";
  
  public static final String SERVLET_PATH = "/documents/";  
  private static final String UNIVERSAL_LANGUAGE = "%%";

  private Set expandedMenuItems;
  private final String servletURL;  
  private HtmlBrowser headerBrowser = new HtmlBrowser();
  private HtmlBrowser footerBrowser = new HtmlBrowser();

  public WebMapBean()
  {
    servletURL = getContextURL() + SERVLET_PATH;
  }
  
  public void setExpandedMenuItems(Set expandedMenuItems)
  {
    this.expandedMenuItems = expandedMenuItems;
  }

  public Set getExpandedMenuItems()
  {
    return expandedMenuItems;
  }

  public String getBaseMid()
  {
    MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();

    String baseMid =
      (String)menuModel.getSelectedMenuItem().
      getProperties().get(BASEMID_PROPERTY);
    if (baseMid == null)
    {
      baseMid = menuModel.getRootMid();
    }
    return baseMid;
  }

  public String getMenuItemURL()
  {
    MenuItemCursor cursor = getCurrentCursor();
    String action = cursor.getAction();
    if (MenuUtils.URL_ACTION.equals(action))
    {
      return (String)cursor.getDirectProperties().get(URL_PROPERTY);
    }
    else return "javascript:goMid('" + cursor.getMid() + "')";
  }

  public String getMenuItemTarget()
  {
    MenuItemCursor cursor = getCurrentCursor();
    return (String)cursor.getDirectProperties().get(TARGET_PROPERTY);
  }

  @CMSAction
  public String showWebMap()
  {
    try
    {
      if (expandedMenuItems == null)
      {
        expandedMenuItems = new HashSet();
        // expand first level
        String baseMid = getBaseMid();
        expandedMenuItems.add(baseMid);

        MenuModel menuModel =
          UserSessionBean.getCurrentInstance().getMenuModel();
        MenuItemCursor cursor = menuModel.getMenuItem(baseMid);
        cursor.moveFirstChild();
        while (!cursor.isNull())
        {
          expandedMenuItems.add(cursor.getMid());
          cursor.moveNext();
        }
      }
    }
    catch (Exception ex)
    {
      error(ex.getLocalizedMessage());
    }
    return "web_map";
  }
  
  public void setHeaderBrowser(HtmlBrowser headerBrowser)
  {
    this.headerBrowser = headerBrowser;
  }

  public HtmlBrowser getHeaderBrowser()
  {
    MenuItemCursor mic = UserSessionBean.getCurrentInstance().getMenuModel().
      getSelectedMenuItem();
    String docId =
      (String) mic.getDirectProperties().get(HEADER_DOCID_PROPERTY);
    if (docId == null)
      docId = (String) mic.getDirectProperties().get(HEADER_DOCUMENT_PROPERTY);
      
    if (docId != null)
    {
      headerBrowser.setUrl(servletURL + docId);    
      return headerBrowser;
    }
    else
      return null;
  }

  public void setFooterBrowser(HtmlBrowser footerBrowser)
  {
    this.footerBrowser = footerBrowser;
  }

  public HtmlBrowser getFooterBrowser()
  {
    MenuItemCursor mic = 
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    String docId =
      (String) mic.getDirectProperties().get(FOOTER_DOCID_PROPERTY);
    if (docId == null)
      docId = (String) mic.getDirectProperties().get(FOOTER_DOCUMENT_PROPERTY);
    
    if (docId != null)
    {
      footerBrowser.setUrl(servletURL + docId);    
      return footerBrowser;
    }
    else 
      return null;
  }  
  
  private MenuItemCursor getCurrentCursor()
  {
    MenuItemCursor cursor = 
      (MenuItemCursor)FacesContext.getCurrentInstance().
      getExternalContext().getRequestMap().get("item");
    return cursor;
  }
}
