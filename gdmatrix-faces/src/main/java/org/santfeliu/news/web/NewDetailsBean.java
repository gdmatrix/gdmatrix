package org.santfeliu.news.web;

import org.santfeliu.web.ShareableWebBean;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.context.ExternalContext;
import javax.servlet.http.HttpServletRequest;
import org.matrix.news.New;
import org.matrix.news.NewDocument;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.doc.web.DocumentBean;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.news.client.NewsManagerClient;
import org.santfeliu.util.HTMLCharTranslator;
import org.santfeliu.util.HTMLNormalizer;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.web.HttpUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.ControllerBean;

public class NewDetailsBean extends ShareableWebBean implements Serializable
{
  private static final String DETAILS_IMAGE_RENDER =
    "details.image.render";
  private static final String DETAILS_IMAGE_HEIGHT =
    "details.image.height";
  private static final String DETAILS_IMAGE_WIDTH =
    "details.image.width";
  private static final String DETAILS_PRINT_ENABLED = 
    "details.printEnabled";

  private static final String DOC_SERVLET_PATH = "/documents/";
  private static final String ICON_PATH = "/common/doc/images/extensions/";

  private New newObject;
  private int readingCount;
  private List<NewDocument> documentList = new ArrayList<NewDocument>();
  private String imageDocId;
  private boolean readingCountRender = true;
  
  public NewDetailsBean()
  {
  }

  public void setNewObject(New newObject)
  {
    this.newObject = newObject;
  }

  public New getNewObject()
  {
    return newObject;
  }
  
  public void setReadingCount(int readingCount)
  {
    this.readingCount = readingCount;
  }

  public int getReadingCount()
  {
    return readingCount;
  }

  public void setDocumentList(List<NewDocument> documentList)
  {
    this.documentList = documentList;
  }

  public List<NewDocument> getDocumentList()
  {
    return documentList;
  }

  public void setImageDocId(String imageDocId)
  {
    this.imageDocId = imageDocId;
  }

  public String getImageDocId()
  {
    return imageDocId;
  }

  public boolean getDocumentListRender()
  {
    if (documentList == null) return false;
    else return (documentList.size() > 0);
  }

  public void setReadingCountRender(boolean readingCountRender)
  {
    this.readingCountRender = readingCountRender;
  }

  public boolean isReadingCountRender()
  {
    return readingCountRender;
  }

  public String getHumanStartDay() throws Exception
  {
    try
    {
      SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
      SimpleDateFormat dayHumanFormat = new SimpleDateFormat("dd/MM/yyyy");
      
      String sysStartDay = newObject.getStartDate();
      if ((sysStartDay == null) || (sysStartDay.length() == 0)) 
        return "";
      else
        return dayHumanFormat.format(dayFormat.parse(sysStartDay));      
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String getDocumentUrl()
  { 
    String url = "";
    NewDocument doc = (NewDocument)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    if (doc != null)  
    {
      String title = doc.getTitle();
      String mimeType = doc.getMimeType();
      String extension = MimeTypeMap.getMimeTypeMap().getExtension(mimeType);
      String filename = DocumentUtils.getFilename(title) + "." + extension;      
      url = getContextPath() + DOC_SERVLET_PATH + doc.getDocumentId() + "/" +
        filename;
    }
    return url;
  }  

  public boolean isRenderImage()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String value = menuItem.getProperty(DETAILS_IMAGE_RENDER);
    return (!"false".equalsIgnoreCase(value)) && getImageURL() != null;
  }

  public String getImageHeight()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    return menuItem.getProperty(DETAILS_IMAGE_HEIGHT);
  }

  public String getImageWidth()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    return menuItem.getProperty(DETAILS_IMAGE_WIDTH);
  }

  public String getImageURL()
  {
    String value = getImageDocId();
    return (value == null ? null : getContextPath() + DOC_SERVLET_PATH + value);
  }

  public String getMimeTypePath() 
  {
    NewDocument doc = (NewDocument)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    return DocumentBean.getContentTypeIcon(getContextPath() + ICON_PATH, doc.getMimeType());
  }
  
  public String getDocumentAriaLabel()
  {
    NewDocument doc = (NewDocument)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String openNewWindowLabel = 
      MatrixConfig.getProperty("org.santfeliu.web.OpenNewWindowLabel");
    return "(*" + doc.getTitle() + "*)" + " (" + openNewWindowLabel + ")";
  }

  public String getShownText()
  {
    String newText = newObject.getText();
    if ((newText != null) && (newText.length() > 0))
    {
      newText = HTMLCharTranslator.toHTMLText(newText, 
        NewsConfigBean.HTML_IGNORED_CHARS);                      
      return newText;
    }
    else
    {
      String newSummary = newObject.getSummary();
      if ((newSummary != null) && (newSummary.length() > 0))
      {
        newSummary = HTMLCharTranslator.toHTMLText(newSummary, 
          NewsConfigBean.HTML_IGNORED_CHARS); 
        return newSummary;
      }      
    }
    return "";
  }

  public String getTranslationGroup()
  {
    return "new:" + newObject.getNewId();
  }

  public void prepareView(NewsManagerClient client) throws Exception
  {
    prepareView(client, null);
  }
  
  public void prepareView(NewsManagerClient client, List<NewDocument> newDocumentList) throws Exception
  {
    String newId = newObject.getNewId();
    if (newDocumentList == null)
    {
      newDocumentList = client.findNewDocumentsFromCache(newId, null);
    }    
    for (NewDocument nd : newDocumentList)
    {
      if (nd.getNewDocTypeId().endsWith(NewsConfigBean.EXTENDED_INFO_TYPE))
      {
        documentList.add(nd);
      }
      else if (nd.getNewDocTypeId().endsWith(NewsConfigBean.DETAILS_IMAGE_TYPE))
      {
        imageDocId = nd.getDocumentId();
      }
      else if (nd.getNewDocTypeId().endsWith(NewsConfigBean.CAROUSEL_AND_DETAILS_IMAGE_TYPE))
      {
        if (imageDocId == null) imageDocId = nd.getDocumentId();
      }
      else if (nd.getNewDocTypeId().endsWith(NewsConfigBean.LIST_AND_DETAILS_IMAGE_TYPE))
      {
        if (imageDocId == null) imageDocId = nd.getDocumentId();
      }
    }
  }
  
  public boolean isEditLinkRender() throws Exception
  {
    MenuItemCursor mic = UserSessionBean.getCurrentInstance().getMenuModel().
      getSelectedMenuItem();  
    String propertyValue = 
      mic.getProperty(NewSearchBySectionBean.EDIT_NODE_PROPERTY);
    if (propertyValue != null)
    {
      try
      {
        UserSessionBean.getCurrentInstance().getMenuModel().
          getMenuItemByMid(propertyValue);
      }
      catch (Exception ex)
      {
        return false;
      }
      return isEditorUser(mic);
    }
    return false;
  }
  
  public String editNew()
  {
    return ControllerBean.getCurrentInstance().showObject("New", 
      newObject.getNewId());      
  }  
  
  public boolean isPrintEnabled()
  {
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String printEnabled = menuItem.getProperty(DETAILS_PRINT_ENABLED);
    return "true".equals(printEnabled);
  }
  
  @Override
  protected List<String> getShareURLList()
  {
    List<String> result = new ArrayList<String>();
    if (newObject != null)
    {    
      MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
        getMenuModel().getSelectedMenuItem();    
      Map<String, String> propertyMap = new HashMap();
      propertyMap.put("xmid", menuItem.getMid());
      propertyMap.put("newid", newObject.getNewId());
      String language =
        getFacesContext().getViewRoot().getLocale().getLanguage();
      propertyMap.put("language", language);
      String headline = translatePlainText(newObject.getHeadline(),
        newObject.getNewId(), language, "new");
      propertyMap.put("info", headline);
      String summary = HTMLNormalizer.cleanHTML(
        translateHtmlText(newObject.getSummary(), 
          newObject.getNewId(), language, "new"), true
      );
      propertyMap.put("summary", summary);  
      propertyMap.put("idparam", "newid");
      propertyMap.put("idvalue", newObject.getNewId());      
      result = getShareURLList(propertyMap);
    }
    return result;
  }  
  
  @Override
  protected String getEmailDefaultSubject()
  {    
    return newObject.getHeadline();
  }

  @Override
  protected String getEmailDefaultBody()
  {
    return getNewURL();    
  }  
  
  private String getNewURL()
  {
    ExternalContext extContext = getFacesContext().getExternalContext();
    HttpServletRequest request = (HttpServletRequest)extContext.getRequest();
    String serverName = HttpUtils.getServerName(request);    
    String serverURL = "http://" + serverName;    
    MenuItemCursor menuItem = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();
    String language = getFacesContext().getViewRoot().getLocale().getLanguage();
    return serverURL + "/go.faces?xmid=" + menuItem.getMid() +
      "&newid=" + newObject.getNewId() + "&language=" + language;
  }
  
  private boolean isEditorUser(MenuItemCursor mic)
  {
    List<String> editRoles =
      mic.getMultiValuedProperty(MenuModel.EDIT_ROLES);
    if (editRoles == null || editRoles.isEmpty()) return true;
    return UserSessionBean.getCurrentInstance().isUserInRole(editRoles);
  }

}
