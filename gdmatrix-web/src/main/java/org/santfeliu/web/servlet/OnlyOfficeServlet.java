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
package org.santfeliu.web.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.santfeliu.doc.client.CachedDocumentManagerClient;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.jwt.JWTUtils;

/**
 *
 * @author granadogj
 */
public class OnlyOfficeServlet extends HttpServlet
{
  private static final Logger logger = Logger.getLogger(OnlyOfficeServlet.class.getName()); 
  
  @Override
  public void init() throws ServletException
  {
    super.init();
  }
  
  /**
   * Recieves the the modified document from the OnlyOffice server and save it
   *
   * @param request 
   * @param response
   * @throws ServletException 
   * @throws IOException 
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    // Shows error if the integration is disabled
    if (!isOnlyOfficeEnabled()) {
      response.setContentType("application/json;charset=UTF-8");
      response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      response.getWriter().write("{\"error\":1,\"message\":\"OnlyOffice integration is disabled\"}");
      logger.log(Level.WARNING, "OnlyOffice integration is disabled");
      return;
    }
        
    response.setContentType("application/json;charset=UTF-8");
    try
    {
      //Take the fileKey (docId) parameter from the url
      String fileKey = request.getParameter("fileKey");
      
      String requestBody = readRequestBody(request);
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      JsonObject callbackData = gson.fromJson(requestBody, JsonObject.class);
     
      //Extract the JWT token returned by the OnlyOffice server
      JWTUtils jwtUtil = new JWTUtils(MatrixConfig.getProperty("org.santfeliu.onlyOffice.JwtSecret"));
      String token = extractToken(request, callbackData);
      
      if (token == null)
      {
        logger.log(Level.WARNING, "The token was not found in the request");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("{\"error\": 1, \"message\": \"No JWT token\"}");
        return;
      } 
      else
      {
        // Check if the JWT token is valid
        if (!jwtUtil.isTokenValid(token))
        {
          logger.log(Level.WARNING, "Invalid JWT Token");
          response.setStatus(HttpServletResponse.SC_FORBIDDEN);
          response.getWriter().write("{\"error\": 1, \"message\": \"Invalid JWT token\"}");
          return;
        }
      }

      //Extract the necessary data
      int status = callbackData.has("status") ? callbackData.get("status").getAsInt() : -1;
      String key = callbackData.has("key") ? callbackData.get("key").getAsString() : "UNKNOWN";
      String url = callbackData.has("url") ? callbackData.get("url").getAsString() : "N/A";
      String userId = getUserId(callbackData);
     
      processState(status, key, fileKey, url, callbackData, userId);

      //Correct response for the OnlyOffice server == 0
      response.getWriter().write("{\"error\": 0}");

    } 
    catch (Exception ex)
    {
      logger.log(Level.WARNING, "ERROR processing the callback: {0}", ex.getMessage());
      response.getWriter().write("{\"error\": 1, \"message\": \"Error processing the callback\"}");
    }
  }

  /**
   * Extracts the token from the callback payload.
   *
   * @param request The incoming HTTP request.
   * @param callbackData The JsonObject containig the callback data.
   * @return A String representing the token, or null if not found.
   */
  private String extractToken(HttpServletRequest request, JsonObject callbackData)
  {
    //Look for the token in the JSON payload.
    if (callbackData.has("token"))
    {
      String token = callbackData.get("token").getAsString();
      return token;
    }
    
    return null;
  }

  /**
   * Processes the document state based on the provided status code and session
   * data.
   *
   * @param status The status code representing the document state.
   * @param key The contentId key from the document, used to identify the
   * editing session.
   * @param docId The unique document identifier.
   * @param url The URL to download the document.
   * @param data The JsonObject containing the data.
   * @param userId The userId from the user triggering this event.
   */
  private void processState(int status, String key, String docId, String url, JsonObject callbackData, String userId)
  {
    // See hoy many users are in the edit session
    JsonArray usersArray = callbackData.has("users") ? callbackData.get("users").getAsJsonArray() : new JsonArray();
    JsonArray actionsArray = callbackData.has("actions") ? callbackData.get("actions").getAsJsonArray() : new JsonArray();
    int remainingUsers = usersArray.size();
    
    logger.log(Level.INFO, "Document: {0}", docId);

    if (!usersArray.isEmpty())
    {
      System.out.println(remainingUsers + " users in the session: " + usersArray);
    }

    // Actions from each users
    if (!actionsArray.isEmpty())
    {
      System.out.println("Actions performed: " + actionsArray);
      for (JsonElement actionElement : actionsArray)
      {
        JsonObject action = actionElement.getAsJsonObject();
        int type = action.has("type") ? action.get("type").getAsInt() : -1;

        //Informs of the user actions
        switch (type)
        {
          case 0:
            logger.log(Level.INFO, "The user {0} disconnects from the document.", userId);
            break;
          case 1:
            logger.log(Level.INFO, "The user {0} connects to the document.", userId);
            break;
          case 2:
            logger.log(Level.INFO, "The user {0} clicks the forcesave button.");
            break;
          default:
            logger.log(Level.INFO, "The user {0} performs an unknown action", userId);
            break;
        }
      }
    }

    // Document status
    switch (status)
    {
      case 1:
        logger.log(Level.INFO, "[INFO] - STATUS 1: Document is being edited...");
        break;

      case 2: //AutoSave
        logger.log(Level.INFO, "[INFO] - STATUS 2: Document is ready for saving");
        try
        {
          byte[] documentBytes = downloadDocument(url);
          updateDocument(userId, docId, documentBytes, true);
          if (remainingUsers == 0)
          {
            logger.log(Level.INFO, "Last user exited from the document {0}", docId);
          }

        } 
        catch (IOException e)
        {
          logger.log(Level.WARNING, "[ERROR] downloading/saving {0}", e.getMessage());
        }
        break;

      case 3:
        logger.log(Level.INFO, "[ERROR] - STATUS 3: Document saving error has occurred");
        break;

      case 4:
        logger.log(Level.INFO, "[INFO] - STATUS 4: Document closed without changes");
        if (remainingUsers == 0 && !userId.equals("N/A"))
        {
          logger.log(Level.INFO, "Last user exited from the document {0}", docId);
          updateDocument(userId, docId, null, true); //Only removing the key
        }
        break;

      case 6: //ForceSave
        logger.log(Level.INFO, "[INFO] - STATUS 6: Document is being edited, but the current document state is saved (Forcesave)");
        try
        {
          byte[] documentBytes = downloadDocument(url);
          updateDocument(userId, docId, documentBytes, false);
          logger.log(Level.INFO, "FORCESAVE completed");
          
        } 
        catch (IOException ex)
        {
          logger.log(Level.WARNING, "[ERROR] Forcesave error: {0}", ex.getMessage());
        }
        break;

      case 7:
        logger.log(Level.WARNING, "[ERROR] - STATUS 7: Error in the forcesave process.");
        break;

      default:
        logger.log(Level.INFO, "Known Status: {0}", status);
    }
  }

  /**
   * Read and process the request body.
   *
   * @param request HttpServletRequest from the OnlyOffice server
   * @return Processed data in String value
   * @throws IOException
   */
  private String readRequestBody(HttpServletRequest request) throws IOException
  {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader reader = request.getReader())
    {
      String line;
      while ((line = reader.readLine()) != null)
      {
        sb.append(line);
      }
    }
    return sb.toString();
  }

  /**
   * Download the binary data of the modified document from OnlyOffice server
   *
   * @param url The download url returned by the OnlyOffice server
   * @return ByteArray with the document data.
   * @throws IOException
   */
  private byte[] downloadDocument(String url) throws IOException
  {
    System.out.println("DOWNLOAD URL: " + url);
    try
    {
      return IOUtils.toByteArray(new URI(url));

    } 
    catch (URISyntaxException ex)
    {
      logger.log(Level.WARNING, "[ERROR] Error downloading document: {0}", ex.getMessage());
      return null;
    }
  }

  /**
   * Extracts the userId from the callback data.
   *
   * @param callbackData The JsonObject containing the callback information
   * @return A String representing the userId, or "N/A" if not found.
   */
  private String getUserId(JsonObject callbackData)
  {
    JsonArray actions = callbackData.has("actions") ? callbackData.getAsJsonArray("actions") : new JsonArray();
    JsonObject firstAction = (actions.size() > 0) ? actions.get(0).getAsJsonObject() : new JsonObject();

    return firstAction.has("userid") ? firstAction.get("userid").getAsString() : "N/A";
  }

  /**
   * Saves the document and updates its metadata This is the only method that
   * interacts directly with the CachedDocumentManagerClient
   *
   * @param userId The uninque identifier of the user triggering this event.
   * @param docId The unique and permanent identifier of the document.
   * @param docuContent The bytes of the new document content, or null if the
   * content should remain unchanged.
   * @param removeKey 'true' if the oldKey should be removed.
   */
  private void updateDocument(String userId, String docId, byte[] docuContent, boolean removeKey)
  {
    try
    {
      String password = MatrixConfig.getProperty("org.santfeliu.security.service.SecurityManager.masterPassword");

      CachedDocumentManagerClient client = new CachedDocumentManagerClient(userId, password);

      Document document = client.getPort().loadDocument(docId, 0, ContentInfo.METADATA);

      if (document == null)
      {
        logger.log(Level.WARNING, "Failed to load docId {0}", docId);
        return;
      }

      // Update the document content if provided.
      if (docuContent != null && docuContent.length > 0)
      {
        System.out.println("[INFO] Updating document content...");
        String mimeType = document.getContent().getContentType();

        ByteArrayDataSource bs = new ByteArrayDataSource(docuContent, mimeType);
        DataHandler dh = new DataHandler(bs);
        Content content = new Content();
        content.setData(dh);
        content.setContentType(dh.getContentType()); // Use the DataHandler's content type.

        String contentType = dh.getContentType();
        if ("application/octet-stream".equals(contentType))
        {
          contentType = null;
        }
        content.setContentType(contentType);

        document.setContent(content);
      } else
      {
        logger.log(Level.INFO, "[INFO] Content will not be updated, only the metadata (oldKey) will be modified");
      }

      // Remove the oldKey property if the last user leaves the document.
      if (removeKey)
      {
        System.out.println("[INFO] Removing oldKey before exiting...");
        document.getProperty().removeIf(p -> "oldKey".equals(p.getName()));
      }

      // Store the document with the changes (content and/or metadata).
      client.getPort().storeDocument(document);
      logger.log(Level.INFO, "Document updated successfully");
      
    } 
    catch (Exception ex)
    {
      logger.log(Level.WARNING, "[ERROR] Error in updateDocument: {0}", ex.getMessage());
    }
  }
  
  private Boolean isOnlyOfficeEnabled(){
    return Boolean.valueOf(MatrixConfig.getProperty("org.santfeliu.onlyOffice.enabled"));
  }
}
