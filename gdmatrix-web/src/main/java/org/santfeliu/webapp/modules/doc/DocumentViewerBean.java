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
package org.santfeliu.webapp.modules.doc;

import org.santfeliu.doc.web.*;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.RelatedDocument;
import org.matrix.doc.RelationType;

import org.santfeliu.doc.client.CachedDocumentManagerClient;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author blanquepa
 */
@CMSManagedBean
@Named
@RequestScoped
public class DocumentViewerBean extends WebBean implements Serializable
{
  @CMSProperty
  public static final String IFRAME_PROPERTY = "iframe";
  @CMSProperty
  public static final String IFRAME_WIDTH_PROPERTY = "iframe.width";
  @CMSProperty
  public static final String IFRAME_HEIGHT_PROPERTY = "iframe.height";
  @CMSProperty
  public static final String DOCID_PROPERTY = "docId";
  @CMSProperty
  public static final String URL_PROPERTY = "url";
  @CMSProperty
  public static final String HEADER_DOCID_PROPERTY = "header.docId";
  @CMSProperty
  public static final String PRINT_ENABLED_PROPERTY = "printEnabled";
  @CMSProperty
  public static final String FOOTER_DOCID_PROPERTY = "footer.docId";
  public static final String DOC_SERVLET_URL = "/documents/";  
  
  private static final String OUTCOME = "/pages/doc/document_viewer.xhtml";

  private String headerBrowserUrl;
  private String footerBrowserUrl;
  
  private boolean keepLocking;
  private transient String tempUrl;
  private DocumentEditor editor;

  public DocumentEditor getEditor()
  {
    return editor;
  }

  public void setEditor(DocumentEditor editor)
  {
    this.editor = editor;
  }

  public String getUrl()
  {
    tempUrl = getDocumentUrl();
    return tempUrl;
  }

  public boolean isRenderEditButton()
  {
    return (isEditorUser() && !isEditing());
  }
  
  public boolean isEditorUser()
  {
    MenuItemCursor mic = 
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    List<String> editRoles;
    try
    {
      editRoles = mic.getEditRoles();
    }
    catch (Exception e)
    {
      return false;
    }
    
    if (editRoles == null || editRoles.isEmpty()) return true;
    return UserSessionBean.getCurrentInstance().isUserInRole(editRoles);
  }

  public boolean isIframe()
  {
    String value = (String)UserSessionBean.getCurrentInstance().getMenuModel().
      getSelectedMenuItem().getProperties().get(IFRAME_PROPERTY);
    return "true".equals(value);
  }

  public String getIframeWidth()
  {
    String value = (String)UserSessionBean.getCurrentInstance().getMenuModel().
      getSelectedMenuItem().getProperties().get(IFRAME_WIDTH_PROPERTY);
    return (value == null) ? "100%" : value;
  }

  public String getIframeHeight()
  {
    String value = (String)UserSessionBean.getCurrentInstance().getMenuModel().
      getSelectedMenuItem().getProperties().get(IFRAME_HEIGHT_PROPERTY);
    return (value == null) ? "400px" : value;
  }

  public boolean isEditing()
  {
    return (editor != null && editor.isLockUser());
  }

  public boolean isPrintEnabled()
  {
    String printEnabled = getProperty(PRINT_ENABLED_PROPERTY);
    return printEnabled == null || "true".equals(printEnabled);
  }

  public Document getDocument()
  {
    return editor.getDocument();
  }
  
  public String getLockUserId()
  {
    return (editor != null ? editor.getDocument().getLockUserId() : null);
  }

  public String getHeaderBrowserUrl()
  {
    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    String docId =
      (String) mic.getDirectProperties().get(HEADER_DOCID_PROPERTY);

    if (docId != null)
    {
      headerBrowserUrl = getContextPath() + DOC_SERVLET_URL + docId;
      return headerBrowserUrl;
    }
    else
    {
      return null;
    }
  }

  public void setHeaderBrowserUrl(String headerBrowserUrl)
  {
    this.headerBrowserUrl = headerBrowserUrl;
  }

  public String getFooterBrowserUrl()
  {
    MenuItemCursor mic =
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    String docId =
      (String) mic.getDirectProperties().get(FOOTER_DOCID_PROPERTY);

    if (docId != null)
    {
      footerBrowserUrl = getContextPath() + DOC_SERVLET_URL + docId;
      return footerBrowserUrl;
    }
    else
    {
      return null;
    }
  }

  public void setFooterBrowserUrl(String footerBrowserUrl)
  {
    this.footerBrowserUrl = footerBrowserUrl;
  }
  
  public String getContent()
  {
    return OUTCOME;
  }  

  // action methods
  @CMSAction
  public String show()
  {
    try
    {
      editor = null;
      String template = UserSessionBean.getCurrentInstance().getTemplate();
      return "/templates/" + template + "/template.xhtml";      
    }
    catch (Exception ex)
    {
      error(ex);
      return null;
    }
  }

  public void editDocument()
  {
    keepLocking = false;
    MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();
    MenuItemCursor mic = menuModel.getSelectedMenuItem();        
    String docId = getDocId(mic);
    try
    {
      editor = new DocumentEditor(docId);
      editor.editDocument(true);
    }
    catch (DocumentEditor.DocumentLockedByUser dlex)
    {
      error(dlex.getMessage(), new Object[]{dlex.getUserId()});
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void saveDocument()
  {
    try
    {
      editor.storeDocument(keepLocking);
      editor = null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void closeDocument()
  {
    if (editor != null)
    {
      try
      {
        if (!keepLocking)
          editor.unlockDocument();
      }
      catch (Exception e)
      {
        error(e);
      }
      finally
      {
        editor = null;
      }
    }
  }
  
  private String getDocumentUrl()
  {
    MenuModel menuModel = UserSessionBean.getCurrentInstance().getMenuModel();
    MenuItemCursor cursor = menuModel.getSelectedMenuItem();
    Map directProperties = cursor.getDirectProperties();

    String url = (String)directProperties.get(URL_PROPERTY);
    if (url == null)
    {
      String docId = getDocId(cursor);
      if (docId != null)
      {
        if (UserSessionBean.getCurrentInstance().isAnonymousUser())
        {
          url = getDocumentServletURL() + docId;
        }
        else // connect by userId/password
        {
          try
          {
            String language = FacesUtils.getViewLanguage();
            Document document = getDocumentFromWS(docId);
            if (!language.equals(document.getLanguage()))
              document = getTranslation(document, language);
            String uuid = document.getContent().getContentId();
            url = getDocumentServletURL() + uuid;
          }
          catch (Exception ex)
          {
            error(ex);
            url = null;
          }
        }
      }
    }
    return url;
  }
  
  private String getDocumentServletURL()
  {
    return getContextURL() + DOC_SERVLET_URL;
  }
  
  private Document getDocumentFromWS(String docId)
    throws Exception
  {
    return getClient().loadDocument(docId, 0, ContentInfo.ALL);
  }
    
  public void setKeepLocking(boolean keepLocking)
  {
    this.keepLocking = keepLocking;
  }

  public boolean isKeepLocking()
  {
    return keepLocking;
  }

  public String getTranslationGroup()
  {
    MenuItemCursor cursor = 
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    String docId = getDocId(cursor);
    return ("doc:" + docId);
  }

  private String getDocId(MenuItemCursor mic)
  {    
    return getDocId(mic, true);
  }
  
  private String getDocId(MenuItemCursor mic, boolean directProperty)
  {    
    String docId = 
      mic.getBrowserSensitiveProperty(DOCID_PROPERTY, !directProperty);

    return docId;
  }
  
  private Document getTranslation(Document document, String language)
    throws Exception
  {
    List<RelatedDocument> relDocs = document.getRelatedDocument();
    for (RelatedDocument relDoc : relDocs)
    {
      RelationType relType = relDoc.getRelationType();
      String relName = relDoc.getName();        
      if (RelationType.TRANSLATION.equals(relType) && language.equals(relName))
      {
        return getClient().loadDocument(relDoc.getDocId(), 0, ContentInfo.ALL);
      }
    }
    
    return document;
  }

  private CachedDocumentManagerClient getClient()
    throws Exception
  {
    CachedDocumentManagerClient client = new CachedDocumentManagerClient(
      UserSessionBean.getCurrentInstance().getUsername(),
      UserSessionBean.getCurrentInstance().getPassword());

    return client;
  }
}
