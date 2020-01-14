package org.santfeliu.security.web;

import org.matrix.security.SecurityManagerPort;
import org.matrix.security.User;

import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.browser.HtmlBrowser;
import org.santfeliu.faces.menu.model.MenuItemCursor;

import org.santfeliu.web.UserSessionBean;

public class RegisterBean extends FacesBean
{
  private String username;
  private String displayName;
  private String password1;
  private String password2;
  private boolean showMessages;
  private HtmlBrowser browser;
  private HtmlBrowser browserOk;
  

  public RegisterBean()
  {
   
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  public String getUsername()
  {
    return username;
  }

  public void setDisplayName(String displayName)
  {
    this.displayName = displayName;
  }

  public String getDisplayName()
  {
    return displayName;
  }

  public void setPassword1(String password1)
  {
    this.password1 = password1;
  }

  public String getPassword1()
  {
    return password1;
  }

  public void setPassword2(String password2)
  {
    this.password2 = password2;
  }

  public String getPassword2()
  {
    return password2;
  }

  public void setShowMessages(boolean showMessages)
  {
    this.showMessages = showMessages;
  }

  public boolean isShowMessages()
  {
    return showMessages;
  }

  public void setBrowser(HtmlBrowser browser)
  {
    this.browser = browser;
    if (browser.getUrl() == null)
      browser.setUrl(getBrowserUrl("registerDoccod"));    
  }

  public HtmlBrowser getBrowser()
  {
    return browser;
  }

  public void setBrowserOk(HtmlBrowser browserOk)
  {
    this.browserOk = browserOk;
    if (browserOk.getUrl() == null)
      browserOk.setUrl(getBrowserUrl("registerDoccodOk"));
  }

  public HtmlBrowser getBrowserOk()
  {
    return browserOk;
  }
  
  // action methods  
  public String register()
  {
    try
    {
      showMessages = true;
      if (password1.equals(password2))
      {
        SecurityManagerPort port = SecurityConfigBean.getPort(true);
        try
        {
          port.loadUser(username);
          error("USER_ALREADY_EXISTS");
        }
        catch (Exception ex)
        {
          // user not found
          User user = new User();
          user.setUserId(username);
          user.setPassword(password1);
          user.setDisplayName(displayName);
          port.storeUser(user);
          return "ok";
        }
      }
      else
      {
        error("PASSWORD_MISMATCH");
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  //private Methods
  private String getBrowserUrl(String MIproperty)
  { 
    MenuItemCursor cursor = 
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    String docId = (String)cursor.getProperties().get(MIproperty);
    if (docId == null) return null;

    return getContextURL() + "/documents/" + docId;
  }
}
