package org.santfeliu.web;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.MailSender;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.script.WebScriptableBase;
import org.santfeliu.util.template.Template;
import org.santfeliu.util.template.WebTemplate;

/**
 *
 * @author lopezrj
 */
public abstract class ShareableWebBean extends WebBean implements Shareable
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
  
  private String emailName;
  private String emailFrom;
  private String emailTo;
  private String emailSubject;
  private String emailText;  

  protected abstract List<String> getShareURLList();  
  protected abstract String getEmailDefaultSubject();
  protected abstract String getEmailDefaultBody();  
  
  public String getEmailName()
  {
    return emailName;
  }

  public void setEmailName(String emailName)
  {
    this.emailName = emailName;
  }

  public String getEmailFrom()
  {
    return emailFrom;
  }

  public void setEmailFrom(String emailFrom)
  {
    this.emailFrom = emailFrom;
  }

  public String getEmailTo()
  {
    return emailTo;
  }

  public void setEmailTo(String emailTo)
  {
    this.emailTo = emailTo;
  }

  public String getEmailSubject()
  {
    return emailSubject;
  }

  public void setEmailSubject(String emailSubject)
  {
    this.emailSubject = emailSubject;
  }

  public String getEmailText()
  {
    return emailText;
  }

  public void setEmailText(String emailText)
  {
    this.emailText = emailText;
  }  
  
  public String sendEmail() throws Exception
  {
    try
    {
      String host = MatrixConfig.getProperty("mail.smtp.host");
      MailSender.sendMail(host, 
        (getEmailName() == null ? getEmailFrom() : getEmailName() + " <" + getEmailFrom() + ">"), 
        getEmailTo(), getEmailSubject(), getEmailText(), false);
      resetEmailFields();
      info("EMAIL_SENT_SUCCESSFULLY");
    }
    catch (Exception ex)
    {
      error("ERROR_SENDING_EMAIL");
      ex.printStackTrace();
    }    
    return null;
  }
  
  public String getEmailSharingScripts()
  {
    StringBuilder sb = new StringBuilder();    
    sb.append("<script type=\"text/javascript\">").append("\n");
    sb.append("function sendEmail() ").append("\n");
    sb.append("{").append("\n");
    sb.append("  var isValid = ").append("\n");
    sb.append("  !(").append("\n");
    sb.append("    document.getElementById('sendEmailFrom').value.trim() == '' || ").append("\n");
    sb.append("    document.getElementById('sendEmailTo').value.trim() == '' || ").append("\n");
    sb.append("    document.getElementById('sendEmailText').value.trim() == ''").append("\n");
    sb.append("  );").append("\n");
    sb.append("  if (isValid) ").append("\n");
    sb.append("  {").append("\n");
    sb.append("    document.getElementById('sendEmailError').style.visibility = 'hidden';").append("\n");
    sb.append("    document.getElementById('sendEmailButton').click();").append("\n");
    sb.append("  }").append("\n");
    sb.append("  else").append("\n");
    sb.append("  {").append("\n");
    sb.append("    document.getElementById('sendEmailError').style.visibility = 'visible';").append("\n");
    sb.append("  }").append("\n");
    sb.append("} ").append("\n\n");    
    sb.append("function switchSendEmailDiv(event)").append("\n");
    sb.append("{").append("\n");
    sb.append("  var mainEvent = event ? event : window.event;").append("\n");
    sb.append("  var formElem = document.getElementById('sendEmailDiv');").append("\n");    
    sb.append("  var display = formElem.style.display;").append("\n");
    sb.append("  if (display === 'block')").append("\n"); //hide
    sb.append("  {").append("\n");
    sb.append("    document.getElementById('sendEmailError').style.visibility = 'hidden';").append("\n");
    sb.append("    formElem.style.display = 'none';").append("\n");
    sb.append("  }").append("\n");
    sb.append("  else").append("\n"); //show
    sb.append("  {").append("\n");
    sb.append("    document.getElementById('mainform').appendChild(formElem);").append("\n");    
    sb.append("    formElem.style.display = 'block';").append("\n");
    sb.append("    formElem.style.position = 'absolute';").append("\n");
    sb.append("    document.getElementById('sendEmailSubject').value = ").append("\n");
    sb.append("      '").append(getEmailDefaultSubject().replace("'", "\\'")).append("';").append("\n");
    sb.append("    document.getElementById('sendEmailText').value = \"").append(getEmailDefaultBody()).append("\";").append("\n");
    sb.append("    var bodyRect = document.body.getBoundingClientRect();").append("\n");
    sb.append("    var elemRect = mainEvent.target.getBoundingClientRect();").append("\n");
    sb.append("    formElem.style.left = (elemRect.left - bodyRect.left - 10 - formElem.offsetWidth) + 'px';").append("\n");
    sb.append("    formElem.style.top = (elemRect.top - bodyRect.top - 10 - formElem.offsetHeight) + 'px';").append("\n");
    sb.append("  }").append("\n");
    sb.append("}").append("\n");     
    sb.append("</script>").append("\n");
    return sb.toString();
  }
  
  @Override
  public boolean isSharingEnabled()
  {
    return isRenderShareTable() && !getShareLinkList().isEmpty();
  }
  
  @Override
  public List<ShareLink> getShareLinkList()
  {
    List<ShareLink> result = new ArrayList();
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
          String shareDescription = 
            (shareText != null ? (shareText + " ") : "") + shareTarget;
          ShareLink item = new ShareLink(shareURLList.get(i), 
            shareImageURLList.get(i), shareDescription);
          result.add(item);
        }
      }
      if (isRenderShareByEmail() && getShareByEmailIcon() != null)
      {
        String shareDescription = 
          (shareText != null ? (shareText + " ") : "") + "E-mail";
        ShareLink item = new ShareLink("EMAIL", getShareByEmailIcon(), 
          shareDescription);
        result.add(item);
      }
    }
    return result;
  }

  protected List<String> getShareURLList(Map<String, String> propertyMap)
  {
    List<String> result = new ArrayList<String>();
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
  
  protected void resetEmailFields()
  {
    emailName = null;
    emailFrom = null;
    emailTo = null;
    emailSubject = null;
    emailText = null;
  }
  
  private List<String> getShareImageURLList()
  {
    List<String> result = new ArrayList<String>();
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

  private boolean isRenderShareByEmail()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(DETAILS_SHARE_BY_EMAIL_ENABLED);
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
        ApplicationBean applicationBean =
          (ApplicationBean)getServletContext().getAttribute("applicationBean");
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
