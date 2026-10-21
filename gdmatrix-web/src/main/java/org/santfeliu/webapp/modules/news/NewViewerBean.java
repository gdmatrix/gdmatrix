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
package org.santfeliu.webapp.modules.news;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.inject.Named;
import org.matrix.news.New;
import org.matrix.news.NewDocument;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.news.client.NewsManagerClient;
import org.santfeliu.util.HTMLCharTranslator;
import org.santfeliu.util.HTMLNormalizer;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.ApplicationBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.webapp.helpers.ShareLinksHelper;

/**
 *
 * @author realor
 */
@CMSManagedBean
@Named
@RequestScoped
public class NewViewerBean extends WebBean implements Serializable
{
  public static final String NEWID_PARAMETER = "newid";
  public static final String SECTIONID_PARAMETER = "sectionid";

  private static final String OUTCOME = "/pages/news/new_viewer.xhtml";
  private static final String DOC_SERVLET_PATH = "/documents/";

  @CMSProperty
  private static final String EDIT_NODE_PROPERTY = "newSearchMid";

  private New newObject;
  private String imageContentId;
  private int readingCount;
  private List<NewDocument> documentList = new ArrayList<>();
  private ShareLinksHelper shareLinksHelper;
  
  @PostConstruct
  public void init()
  {
    shareLinksHelper = new ShareLinksHelper()
    {
      @Override
      protected List<String> getShareURLList()
      {
        List<String> result = new ArrayList<>();
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
    };
  }

  public New getNewObject()
  {
    return newObject;
  }

  public void setNewObject(New newObject)
  {
    this.newObject = newObject;
  }

  public String getImageContentId()
  {
    return imageContentId;
  }

  public void setImageContentId(String imageContentId)
  {
    this.imageContentId = imageContentId;
  }

  public int getReadingCount()
  {
    return readingCount;
    //TODO Show newSection readings in certain scenarios
  }

  public void setReadingCount(int readingCount)
  {
    this.readingCount = readingCount;
  }

  public boolean isReadingCountRender()
  {
    return true; //TODO Use custom node property
  }

  public List<NewDocument> getDocumentList()
  {
    return documentList;
  }

  public void setDocumentList(List<NewDocument> documentList)
  {
    this.documentList = documentList;
  }

  public ShareLinksHelper getShareLinksHelper()
  {
    return shareLinksHelper;
  }

  public void setShareLinksHelper(ShareLinksHelper shareLinksHelper)
  {
    this.shareLinksHelper = shareLinksHelper;
  }

  public String getContent()
  {
    return OUTCOME;
  }

  public String getStartDate() throws Exception
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

  // action methods
  @CMSAction
  public String show()
  {
    try
    {
      newObject = null;
      imageContentId = null;
      documentList.clear();
      readingCount = 0;
      ExternalContext externalContext = getExternalContext();
      Map<String, String> parameterMap = externalContext.getRequestParameterMap();
      String newId = parameterMap.get(NEWID_PARAMETER);
      if (newId != null)
      {
        String sectionId = parameterMap.get(SECTIONID_PARAMETER); 
        loadNew(sectionId, newId, null, true, true);
      }
      else
      {
        throw new Exception(translate("$$newsBundle.new_inaccessible_error"));
      }
    }
    catch (Exception ex)
    {
      newObject = null;
      error(ex);
    }
    String template = UserSessionBean.getCurrentInstance().getTemplate();
    return "/templates/" + template + "/template.xhtml";
  }

  protected void loadNew(String sectionId, String newId,
    List<NewDocument> newDocumentList, boolean checkVisibility,
    boolean incrementCounter) throws Exception
  {
    NewsManagerClient client = NewsModuleBean.getClient();

    //Load new object
    newObject = client.loadNewFromCache(newId);

    // Check Visibility)
    if (checkVisibility)
    {
      boolean[] check = NewsModuleBean.checkNewSections(client, newId);
      boolean userCanRead = check[0];
      boolean userCanEdit = check[1];
      if (!userCanRead)
        throw new Exception(translate("$$newsBundle.new_inaccessible_error"));
      if (newObject.isDraft() && !userCanEdit)
        throw new Exception(translate("$$newsBundle.new_inaccessible_error"));
    }

    //Increment reading counter
    if (incrementCounter)
    {
      this.readingCount = client.incrementNewCounter(newId, sectionId);
      //TODO Increment and show newSection readings in certain scenarios
    }
    else
    {
      this.readingCount = newObject.getTotalReadingCount();
    }

    //Load document list
    loadDocumentList(client, newDocumentList);
  }
  
  private void loadDocumentList(NewsManagerClient client,
    List<NewDocument> newDocumentList) throws Exception
  {
    String newId = newObject.getNewId();
    if (newDocumentList == null)
    {
      newDocumentList = client.findNewDocumentsFromCache(newId, null);
    }
    for (NewDocument newDocument : newDocumentList)
    {
      if (newDocument.getNewDocTypeId().endsWith(
        NewsModuleBean.EXTENDED_INFO_TYPE))
      {
        documentList.add(newDocument);
      }
      else if (newDocument.getNewDocTypeId()
        .endsWith(NewsModuleBean.DETAILS_IMAGE_TYPE))
      {
        this.imageContentId = newDocument.getContentId();
      }
      else if (newDocument.getNewDocTypeId()
        .endsWith(NewsModuleBean.CAROUSEL_AND_DETAILS_IMAGE_TYPE))
      {
        if (this.imageContentId == null)
          this.imageContentId = newDocument.getContentId();
      }
      else if (newDocument.getNewDocTypeId()
        .endsWith(NewsModuleBean.LIST_AND_DETAILS_IMAGE_TYPE))
      {
        if (this.imageContentId == null)
          this.imageContentId = newDocument.getContentId();
      }
    }
  }

  public String getShownText()
  {
    String newText = newObject.getText();
    if ((newText != null) && (newText.length() > 0))
    {
      newText = HTMLCharTranslator.toHTMLText(newText,
        NewsModuleBean.HTML_IGNORED_CHARS);
      return newText;
    }
    else
    {
      String newSummary = newObject.getSummary();
      if ((newSummary != null) && (newSummary.length() > 0))
      {
        newSummary = HTMLCharTranslator.toHTMLText(newSummary,
          NewsModuleBean.HTML_IGNORED_CHARS);
        return newSummary;
      }
    }
    return "";
  }

  public boolean getDocumentListRender()
  {
    if (documentList == null)
      return false;
    else
      return (!documentList.isEmpty());
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

  public String getDocumentAriaLabel()
  {
    NewDocument doc = (NewDocument)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String openNewWindowLabel =
      MatrixConfig.getProperty("org.santfeliu.web.OpenNewWindowLabel");
    String trOpenNewWindowLabel =
      ApplicationBean.getCurrentInstance().translate(openNewWindowLabel);
    return doc.getTitle() + " (" + trOpenNewWindowLabel + ")";
  }

  public boolean isEditLinkRender() throws Exception
  {
    MenuItemCursor mic = UserSessionBean.getCurrentInstance().getMenuModel().
      getSelectedMenuItem();
    String propertyValue = mic.getProperty(EDIT_NODE_PROPERTY);
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

  private boolean isEditorUser(MenuItemCursor mic)
  {
    List<String> editRoles =
      mic.getMultiValuedProperty(MenuModel.EDIT_ROLES);
    if (editRoles == null || editRoles.isEmpty()) return true;
    return UserSessionBean.getCurrentInstance().isUserInRole(editRoles);
  }

  private String translate(String text)
  {
    return ApplicationBean.getCurrentInstance().translate(text);
  }

}
