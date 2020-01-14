package org.santfeliu.security.web;

import org.matrix.security.SecurityManagerPort;

import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.browser.HtmlBrowser;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.web.UserSessionBean;

public class PasswordBean extends FacesBean
{
  private String oldPassword;
  private String newPassword1;
  private String newPassword2;
  private String passwordMessage;
  private boolean showMessages;
  private HtmlBrowser browser;
  private HtmlBrowser browserOk;
  
  public PasswordBean()
  {
  }

  public void setOldPassword(String oldPassword)
  {
    this.oldPassword = oldPassword;
  }

  public String getOldPassword()
  {
    return oldPassword;
  }

  public void setNewPassword1(String newPassword1)
  {
    this.newPassword1 = newPassword1;
  }

  public String getNewPassword1()
  {
    return newPassword1;
  }

  public void setNewPassword2(String newPassword2)
  {
    this.newPassword2 = newPassword2;
  }

  public String getNewPassword2()
  {
    return newPassword2;
  }

  public void setPasswordMessage(String passwordMessage)
  {
    this.passwordMessage = passwordMessage;
  }

  public String getPasswordMessage()
  {
    return passwordMessage;
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
      browser.setUrl(getBrowserUrl("changePassDoccod"));
  }

  public HtmlBrowser getBrowser()
  {
    return browser;
  }

  public void setBrowserOk(HtmlBrowser browserOk)
  {
    this.browserOk = browserOk;
    if (browserOk.getUrl() == null)
      browserOk.setUrl(getBrowserUrl("changePassDoccodOk"));
  }

  public HtmlBrowser getBrowserOk()
  {
    return browserOk;
  }
  // action methods
  public String changePassword()
  {
    try
    {
      showMessages = true;
      if (newPassword1.equals(newPassword2))
      {
        UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
        String userId = userSessionBean.getUserId();
        SecurityManagerPort port = SecurityConfigBean.getPort();
        port.changePassword(userId, oldPassword, newPassword1);
        return "ok";
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
