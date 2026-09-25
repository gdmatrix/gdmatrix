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

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.error.PebbleException;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.santfeliu.doc.web.*;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.io.IOUtils;
import org.matrix.dic.Property;

import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
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
  @CMSProperty
  public static final String EDITOR_LANGUAGE_PROPERTY = "editor.language";
  @CMSProperty
  public static final String DISABLE_HTML_FIXER = "disableHtmlFixer";
  @CMSProperty
  public static final String PEBBLE_TEMPLATE_PROPERTY = "pebbleTemplate";
  @CMSProperty
  public static final String PEBBLE_CONTENT_KEY_PROPERTY = "pebbleContentKey";
  @CMSProperty
  public static final String PEBBLE_REFRESH_PROPERTY = "pebbleRefresh"; // seconds

  public static final String DOC_SERVLET_URL = "/documents/";
  private static final String OUTCOME = "/pages/doc/document_viewer.xhtml";

  private String headerBrowserUrl;
  private String footerBrowserUrl;

  private boolean keepLocking;
  private transient String tempUrl;
  private DocumentEditor editor;
  private boolean refreshPebble;
  
  @Inject
  PebbleBean pebbleBean;

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

  public String getEditorLanguage()
  {
    String language = getProperty(EDITOR_LANGUAGE_PROPERTY);
    if (language == null)
    {
      language = "html";
    }
    return language;
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
    String docId = getDocId();
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
      editor.storeDocument(keepLocking, isHtmlFixerDisabled());
      editor = null;
      refreshPebble = true;
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
  
  public boolean isPebbleEnabled()
  {
    return "pebble".equals(getEditorLanguage());
  }

  public String getPebbleContent()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();

    String content;
    Map<String, Object> context = createPebbleContext(userSessionBean);
    String key = getPebbleContentKey(context);    
    if (key == null) // cache is disabled
    {
      content = generatePebbleContent(context);
    }
    else // cache is enabled
    {
      try
      {
        content = refreshPebble ? null : readCachedContent(key);
        refreshPebble = false;
        if (content == null)
        {
          content = generatePebbleContent(context);
          writeCachedContent(key, content);
        }
      }
      catch (Exception ex)
      {
        content = "ERROR: " + ex;
      }
    }
    return content;
  }
  
  private File getCacheDir()
  {
    String userHome = System.getProperty("user.home");
    File dir = new File(userHome, "pebble");
    if (!dir.exists()) dir.mkdirs();
    return dir;
  }

  private String readCachedContent(String key) throws IOException
  {
    Path file = getCacheDir().toPath().resolve(key + ".html");
    System.out.println("File: " + file);

    if (!Files.exists(file)) return null;
    
    long ellapsedSeconds = (System.currentTimeMillis() - 
      Files.getLastModifiedTime(file).toMillis()) / 1000;
    
    if (ellapsedSeconds > getPebbleRefreshTime()) return null;
    
    return Files.readString(file, StandardCharsets.UTF_8);
  }

  private void writeCachedContent(String key, String content) throws IOException
  {
    Path file = getCacheDir().toPath().resolve(key + ".html");
    Path temp = Files.createTempFile(getCacheDir().toPath(), "content-", ".tmp");

    Files.writeString(temp, content, StandardCharsets.UTF_8);

    Files.move(temp, file,
      StandardCopyOption.REPLACE_EXISTING,
      StandardCopyOption.ATOMIC_MOVE);  
  }
  
  private long getPebbleRefreshTime()
  {
    String millis = getProperty(PEBBLE_REFRESH_PROPERTY);
    if (millis != null)
    {
      try
      {
        return Long.parseLong(millis) / 1000;
      }
      catch (Exception ex)
      {
      }
    }
    return 60;
  }

  private String generatePebbleContent(Map<String, Object> context)
  {
    try
    {
      String docId = getDocId();

      if (docId == null) return "ERROR: Pebble document not found.";

      Document document = getDocumentById(docId);

      String source;
      try (var is = document.getContent().getData().getInputStream())
      {
        source = IOUtils.toString(is, "UTF-8");
      }
            
      PebbleEngine engine = pebbleBean.getEngine();
      PebbleTemplate template = engine.getLiteralTemplate(source);
      
      StringWriter writer = new StringWriter();
      template.evaluate(writer, context);
      return writer.toString();
    }
    catch (PebbleException ex)
    {
      Throwable cause = ex;
      while (cause.getCause() != null)
      {
        cause = cause.getCause();
      }
      return "ERROR: line " + ex.getLineNumber() + ": " +
        ex.getPebbleMessage() + " / " + cause.toString();
    }
    catch (Exception ex)
    {
      return "ERROR: " + ex.toString();
    }    
  }
  
  private String getPebbleContentKey(Map<String, Object> context)
  {
    // <object>[:<property>]|<object>[:<property>]|...
    String pattern = getProperty(PEBBLE_CONTENT_KEY_PROPERTY);
    if (pattern == null) return null;

    StringBuilder contentKeyBuffer = new StringBuilder();

    contentKeyBuffer.append((String)context.get(DOCID_PROPERTY));
    contentKeyBuffer.append("|");
    contentKeyBuffer.append((String)context.get(PEBBLE_TEMPLATE_PROPERTY));
    contentKeyBuffer.append("|");
        
    StringBuilder buffer = new StringBuilder();
    Map<String, Object> object = null;
    for (int i = 0; i <= pattern.length(); i++)
    {
      char ch = i == pattern.length() ? '|' : pattern.charAt(i);

      if (object == null) // outside object
      {
        if (ch == ':')
        {
          String name = buffer.toString();
          buffer.setLength(0);
          Object value = context.get(name);
          if (value instanceof Map) object = (Map)value;
          else object = Collections.emptyMap();
        }
        else if (ch == '|')
        {
          String name = buffer.toString();
          buffer.setLength(0);
          if (name.length() > 0)
          {
            String value = String.valueOf(context.get(name));
            contentKeyBuffer.append(value);
            contentKeyBuffer.append("|");
          }
        }
        else
        {
          buffer.append(ch);
        }
      }
      else // inside object
      {
        if (ch == '|')
        {
          String name = buffer.toString();
          buffer.setLength(0);
          String value = String.valueOf(object.get(name));
          contentKeyBuffer.append(value);
          contentKeyBuffer.append("|");
          object = null;
        }
        else
        {
          buffer.append(ch);
        }        
      }
    }
    String key = contentKeyBuffer.toString();
    System.out.println("key-string:" + key);
    try
    {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));

      StringBuilder result = new StringBuilder();
      for (byte b : hash) 
      {
        result.append(String.format("%02x", b));
      }
      return result.toString();
    }
    catch (Exception ex)
    {
      return null;
    }
  }
  
  private Map<String, Object> createPebbleContext(UserSessionBean userSessionBean)
  {
    MenuItemCursor cursor = userSessionBean.getSelectedMenuItem();
    Map<String, Object> context = new HashMap<>();
    context.put(DOCID_PROPERTY, cursor.getProperty(DOCID_PROPERTY));
    context.put(PEBBLE_TEMPLATE_PROPERTY, cursor.getProperty(PEBBLE_TEMPLATE_PROPERTY));
    context.put("userId", userSessionBean.getUserId());
    context.put("displayName", userSessionBean.getDisplayName());
    context.put("language", userSessionBean.getLastPageLanguage());
    context.put("data", pebbleBean.getData(userSessionBean.getCredentials()));
    context.put("node", cursor.getProperties());
    context.put("params", getExternalContext().getRequestParameterMap());
    context.put("mid", cursor.getMid());
    context.put("workspaceid", userSessionBean.getMenuModel()
      .getCWorkspace().getWorkspace().getWorkspaceId());
    return context;
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
            Document document = getDocumentById(docId);
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

  private Document getDocumentById(String docId)
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

  private String getDocId()
  {
    MenuItemCursor cursor = 
      UserSessionBean.getCurrentInstance().getSelectedMenuItem();

    String docId = getDocId(cursor, true);

    if (docId == null && isPebbleEnabled())
    {
      try
      {
        String name = getProperty(PEBBLE_TEMPLATE_PROPERTY);
        if (name != null)
        {
          CachedDocumentManagerClient client = getClient();
          DocumentFilter filter = new DocumentFilter();
          Property property = new Property();
          property.setName("name");
          property.getValue().add(name);
          filter.setDocTypeId("PEBBLE");
          filter.getProperty().add(property);
          List<Document> documents = client.findDocuments(filter);
          if (!documents.isEmpty())
          {
            docId = documents.get(0).getDocId();
          }
        }
      }
      catch (Exception ex)
      {        
      }
    }
    return docId;
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
  
  private boolean isHtmlFixerDisabled()
  {
    String disableHtmlFixer = getProperty(DISABLE_HTML_FIXER);
    if (disableHtmlFixer == null)
      return false;
    return Boolean.parseBoolean(disableHtmlFixer);
  }
}
