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
package org.santfeliu.webapp.modules.onlyoffice;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import static java.util.Map.entry;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.matrix.dic.Property;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentManagerPort;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.util.MatrixConfig;
import static org.santfeliu.webapp.modules.doc.DocModuleBean.getPort;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.util.jwt.JWTUtils;

/**
 *
 * @author granadogj
 */
@Named
@RequestScoped
public class OnlyOfficeBean extends WebBean
{

  private static final Logger logger = Logger.getLogger(OnlyOfficeBean.class.getName());

  private Map config;
  private String jwtSecret;

  @PostConstruct
  public void init()
  {
    this.jwtSecret = MatrixConfig.getProperty("org.santfeliu.onlyOffice.JwtSecret");
  }

  public void show()
  {
    // Block the execution if the inegration is disabled
    if (!getOnlyOfficeEnabled())
    {
      logger.log(Level.WARNING, "OnlyOffice integration is disabled");
      error("DOCUMENT_ONLYOFFICE_DISABLED");
      return;
    }

    try
    {
      String docId = FacesContext.getCurrentInstance()
        .getExternalContext().getRequestParameterMap().get("docId");

      String accessMode = FacesContext.getCurrentInstance()
        .getExternalContext().getRequestParameterMap().get("accessMode");

      String userId = UserSessionBean.getCurrentInstance().getUserId();
      String username = UserSessionBean.getCurrentInstance().getUsername();

      if (docId == null)
      {
        config = new HashMap<>();
        logger.log(Level.WARNING, "DocId is null, cannot load the document");
        error("DOCUMENT_NO_DOCID");
        return;
      }

      DocumentManagerPort port = getPort(false);
      Document document = port.loadDocument(docId, 0, ContentInfo.METADATA);
      MimeTypeMap mime = new MimeTypeMap();

      String fileType = mime.getExtension(document.getContent().getContentType());

      //Check if the type of document is supported
      if (isSupportedType(fileType) == null)
      {
        return;
      }

      // Only check the user permissions if the user enters in the edit mode
      if ("edit".equals(accessMode))
      {
        // Tries to save, check read
        try
        {
          //If it works. Edit permisions are granted
          port.storeDocument(document);
          accessMode = "edit";
        }
        catch (Exception ex)
        {
          // If an error occurs while trying to save, we open it in "read only"
          accessMode = "view";
          logger.log(Level.INFO, "The user {0} has not the required permissions to modify the document, opening in 'view' mode", userId);
        }
      }
      else
      {
        accessMode = "view";
      }

      //If you have edit or view permissions you are allowed to continue
      String key = document.getContent().getContentId();

      //Check for the oldKey, if not exists it is added
      if (!existsOldKey(document) && "edit".equals(accessMode))
      {
        System.out.println("[INFO] Adding the oldKey to the properties");
        addOldKeyProperty(document, key);
        port.storeDocument(document); // Save the key immediately
      }

      //If the oldKey exists, the document is being eddited, we use the same key to add the users to the current editing session
      if (existsOldKey(document))
      {
        String existingKey = document.getProperty().stream()
          .filter(p -> "oldKey".equals(p.getName()))
          .findFirst()
          .map(p -> p.getValue().get(0)) //stored key
          .orElse(key); // fallback
        key = existingKey;
      }

      //Deny if the users tries to open more than one window of the same document
      if (isUserInDocument(userId, key))
      {
        error("DOCUMENT_SAME_DOCUMENT");
        return;
      }

      // -- Config data --
      String title = document.getTitle();
      String documentType = EXTENSION_TO_DOCUMENT_TYPE.get(fileType.toLowerCase());
      String lang = !"%%".equals(document.getLanguage()) ? document.getLanguage() : "es";
      String docUrl = String.format("https://%s/documents/%s/%s.%s", getHost(), key, title, fileType);
      String callbackUrl = String.format("https://%s/onlyoffice?fileKey=%s", getHost(), docId);

      config = new HashMap();

      // -- config.document --
      Map<String, Object> docu = new HashMap<>();
      docu.put("fileType", fileType);
      docu.put("title", title);
      docu.put("key", key); //Document identifier (contentId), changes when modified document is stored
      docu.put("url", docUrl);

      // --config.referenceData --
      Map<String, Object> referenceData = new HashMap<>();
      referenceData.put("fileKey", docId);  //Unique document identifier 
      docu.put("referenceData", referenceData);

      config.put("document", docu);

      // ---- config.documentType ----
      config.put("documentType", documentType);

      // ---- config.editorConfig ----
      Map<String, Object> editorConfig = new HashMap<>();
      editorConfig.put("mode", accessMode);
      editorConfig.put("lang", lang);
      editorConfig.put("callbackUrl", callbackUrl);

      Map<String, Object> user = new HashMap<>();
      user.put("id", userId);
      user.put("name", username);
      editorConfig.put("user", user);

      // --- config.customization --
      Map<String, Object> customization = new HashMap<>();
      customization.put("forcesave", true);
      customization.put("saveAs", true);
      editorConfig.put("customization", customization);

      config.put("editorConfig", editorConfig);

      //JWT Token generation
      JWTUtils jwt = new JWTUtils(jwtSecret);
      String token = jwt.generateToken(config); //Expiration time default = 8h

      //Add token into the config.json
      config.put("token", token);

    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public String getContent()
  {
    return "/pages/onlyoffice/editor.xhtml";
  }

  public String getConfig()
  {
    String configString = new Gson().toJson(config);
    return configString;
  }

  private static final Map<String, String> EXTENSION_TO_DOCUMENT_TYPE = Map.ofEntries(
    // Word - Text Document
    entry("doc", "word"), entry("docx", "word"), entry("rtf", "word"), entry("txt", "word"),
    entry("odt", "word"), entry("dotx", "word"), entry("xml", "word"),
    // Cell - Spreadsheet
    entry("xls", "cell"), entry("xlsx", "cell"), entry("ods", "cell"), entry("csv", "cell"),
    // Slide - Presentation
    entry("ppt", "slide"), entry("pptx", "slide"), entry("odp", "slide"),
    // PDF
    entry("pdf", "pdf"), entry("xps", "pdf"),
    // Diagram
    entry("vsdx", "diagram"), entry("vssx", "diagram")
  );

  /**
   * Return the document type like OnlyOffice needs
   *
   * @param extension Document extension
   * @return Document type, required by OnlyOffice
   */
  private String isSupportedType(String extension)
  {
    
    if (extension == null || extension.isEmpty())
    {
      error("DOCUMENT_NULL_EXTENSION");
      return null;
    }

    String docType = EXTENSION_TO_DOCUMENT_TYPE.get(extension.toLowerCase());

    if (docType == null)
    {
      error("DOCUMENT_INVALID_EXTENSION");
      return null;
    }

    return docType;
  }

  /**
   * Add oldKey to the properties list
   *
   * @param document Document to modify
   * @param oldKey ContentId to use as key of the document editing session
   */
  private static void addOldKeyProperty(Document document, String oldKey)
  {
    // Create the new property
    Property oldKeyProp = new Property();
    oldKeyProp.setName("oldKey");
    oldKeyProp.getValue().add(oldKey);

    // Add the property to the document metadata
    document.getProperty().add(oldKeyProp);
  }

  /**
   * Return the oldKey if exists in the document properties
   *
   * @param document Document to verify
   * @return The oldKey if exists, false if not.
   */
  private static boolean existsOldKey(Document document)
  {
    return document.getProperty().stream().anyMatch(p -> "oldKey".equals(p.getName()));
  }

  /**
   * Check if the user is in the document editing session
   *
   * @param userId
   * @param key The document key (contentId)
   * @return true if the user is in the editing session, false if not
   * @throws Exception
   */
  private boolean isUserInDocument(String userId, String key) throws Exception
  {
    String commandServiceUrl = getOnlyOfficeHost() +"/command";
    Gson gson = new Gson();
    Map<String, Object> jsonMap = new HashMap<>();
    jsonMap.put("c", "info");
    jsonMap.put("key", key);
    jsonMap.put("userId", userId);

    //JWT Token generation
    JWTUtils jwt = new JWTUtils(jwtSecret);
    String token = jwt.generateToken(jsonMap);
    jsonMap.put("token", token);

    HttpClient client = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(5)) //5s timeout
      .build();

    HttpRequest request = HttpRequest.newBuilder()
      .uri(new URI(commandServiceUrl)) //URI of command service
      .header("Content-Type", "application/json;charset=UTF-8")
      .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(jsonMap)))
      .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) //If the statusCode is different from OK (200), we send a RuntimeException
    {
      throw new RuntimeException("Error in HTTP call: " + response.statusCode());
    }

    if (response.body() == null || response.body().isEmpty())
    {
      throw new RuntimeException("The server response is empty");
    }

    JsonObject responseJson = com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();

    if (responseJson.has("users"))
    {
      var usersArray = responseJson.getAsJsonArray("users");
      for (int i = 0; i < usersArray.size(); i++)
      {
        if (usersArray.get(i).getAsString().equals(userId))
        {
          return true;
        }
      }
    }
    return false;
  }

  private Boolean getOnlyOfficeEnabled()
  {
    return Boolean.valueOf(MatrixConfig.getProperty("org.santfeliu.onlyOffice.enabled"));
  }

  private String getHost()
  {
    return java.lang.System.getProperty("host");
  }

  public String getOnlyOfficeHost()
  {
    return MatrixConfig.getProperty("org.santfeliu.onlyOffice.url");
  }

  public boolean isOfficeButtonVisible(String contentType)
  {
    if (contentType == null || contentType.isEmpty())
    {
      return false;
    }

    MimeTypeMap mime = new MimeTypeMap();
    String ext = mime.getExtension(contentType);

    if (ext == null || ext.isEmpty())
    {
      return false;
    }

    boolean supported = EXTENSION_TO_DOCUMENT_TYPE.containsKey(ext.toLowerCase());
    return supported && getOnlyOfficeEnabled();
  }
}
