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
package org.santfeliu.webapp.modules.assistant.langchain4j;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.activation.DataHandler;
import org.apache.commons.io.IOUtils;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.security.AccessControl;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.util.MemoryDataSource;
import org.santfeliu.webapp.modules.doc.DocModuleBean;
import static org.matrix.dic.DictionaryConstants.READ_ACTION;
import static org.matrix.dic.DictionaryConstants.WRITE_ACTION;
import static org.matrix.doc.DocumentConstants.UNIVERSAL_LANGUAGE;

/**
 *
 * @author realor
 */
public class AssistantStore
{
  public static final String ASSISTANT_TYPEID = "Assistant";

  private DocumentManagerPort documentManagerPort;

  public static AssistantStore getInstance(Credentials credentials)
  {
    AssistantStore assistantStore = new AssistantStore();
    assistantStore.documentManagerPort = DocModuleBean.getPort(
      credentials.getUserId(), credentials.getPassword());
    return assistantStore;
  }

  public List<AssistantSummary> getAssistants()
  {
    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId(ASSISTANT_TYPEID);
    filter.getOutputProperty().add("assistantId");
    filter.getOutputProperty().add("name");
    filter.getOutputProperty().add("description");

    List<AssistantSummary> assistants = new ArrayList<>();
    List<Document> documents = getPort().findDocuments(filter);
    for (Document document : documents)
    {
      String assistantId =
        DictionaryUtils.getPropertyValue(document.getProperty(), "assistantId");
      String name =
        DictionaryUtils.getPropertyValue(document.getProperty(), "name");
      String description =
        DictionaryUtils.getPropertyValue(document.getProperty(), "description");

      assistants.add(new AssistantSummary(assistantId, name, description));
    }
    return assistants;
  }

  public Assistant loadAssistant(String assistantId) throws Exception
  {
    Document document = findDocumentByAssistantId(assistantId);
    if (document == null) throw new Exception("Assistant not found: " + assistantId);

    String contentId = document.getContent().getContentId();
    Content content = getPort().loadContent(contentId);
    InputStream is = content.getData().getInputStream();
    String json = IOUtils.toString(is, "UTF-8");
    Gson gson = new Gson();
    Assistant assistant = gson.fromJson(json, Assistant.class);
    assistant.setDocId(document.getDocId());
    assistant.setCreationUserId(document.getCaptureUserId());
    assistant.setChangeUserId(document.getChangeUserId());
    List<AccessControl> acl = document.getAccessControl();
    for (AccessControl ac : acl)
    {
      if (READ_ACTION.equals(ac.getAction()))
      {
        assistant.setReadRoleId(ac.getRoleId());
      }
      if (WRITE_ACTION.equals(ac.getAction()))
      {
        assistant.setWriteRoleId(ac.getRoleId());
      }
    }
    return assistant;
  }

  public void saveAssistant(Assistant assistant) throws Exception
  {
    Gson gson  = new GsonBuilder().disableHtmlEscaping().create();
    String json = gson.toJson(assistant);

    Document document = new Document();
    document.setDocId(assistant.getDocId());
    document.setDocTypeId(ASSISTANT_TYPEID);

    document.setTitle(assistant.getName());

    if (assistant.getDocId() == null)
    {
      Property property = new Property();
      property.setName("assistantId");
      property.getValue().add(assistant.getAssistantId());
      document.getProperty().add(property);
    }

    DictionaryUtils.setProperty(document, "name", assistant.getName());
    DictionaryUtils.setProperty(document, "description", assistant.getDescription());

    document.setIncremental(true);
    MemoryDataSource ds =
      new MemoryDataSource(json.getBytes("UTF-8"), "assistant", "application/json");
    DataHandler dh = new DataHandler(ds);
    Content content = new Content();
    content.setData(dh);
    content.setContentType("application/json");
    content.setLanguage(UNIVERSAL_LANGUAGE);
    document.setContent(content);

    String readRoleId = assistant.getReadRoleId();
    if (readRoleId != null)
    {
      AccessControl read = new AccessControl();
      read.setAction(READ_ACTION);
      read.setRoleId(readRoleId);
      document.getAccessControl().add(read);
    }
    String writeRoleId = assistant.getWriteRoleId();
    if (writeRoleId != null)
    {
      AccessControl write = new AccessControl();
      write.setAction(WRITE_ACTION);
      write.setRoleId(writeRoleId);
      document.getAccessControl().add(write);
    }
    document = getPort().storeDocument(document);

    assistant.setDocId(document.getDocId());
  }

  public void deleteAssistant(String assistantId)
  {
    Document document = findDocumentByAssistantId(assistantId);
    if (document != null)
    {
      getPort().removeDocument(document.getDocId(), 0);
    }
  }

  public class AssistantSummary implements Serializable
  {
    String assistantId;
    String name;
    String description;

    public AssistantSummary(String assistantId, String name, String description)
    {
      this.assistantId = assistantId;
      this.name = name;
      this.description = description;
    }

    public String getAssistantId()
    {
      return assistantId;
    }

    public String getName()
    {
      return name;
    }

    public String getDescription()
    {
      return description;
    }
  }

  public DocumentManagerPort getPort()
  {
    if (documentManagerPort == null)
    {
      documentManagerPort = DocModuleBean.getPort("anonymous", null);
    }
    return documentManagerPort;
  }

  private Document findDocumentByAssistantId(String assistantId)
  {
    DocumentFilter filter = new DocumentFilter();
    filter.setDocTypeId(ASSISTANT_TYPEID);
    Property property = new Property();
    property.setName("assistantId");
    property.getValue().add(assistantId);
    filter.getProperty().add(property);

    DocumentManagerPort port = getPort();
    List<Document> documents = port.findDocuments(filter);
    if (documents.isEmpty()) return null;

    return documents.get(0);
  }
}
