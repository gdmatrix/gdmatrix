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
package org.santfeliu.workflow.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import org.matrix.dic.DictionaryConstants;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.dic.Property;
import org.matrix.doc.State;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.Properties;
import org.santfeliu.util.template.Template;
import org.santfeliu.workflow.WorkflowActor;
import org.santfeliu.workflow.WorkflowInstance;

/**
 *
 * @author realor
 */
public class DocumentNode extends org.santfeliu.workflow.node.DocumentNode
  implements NodeProcessor
{
  @Override
  public String process(WorkflowInstance instance, WorkflowActor actor)
    throws Exception
  {
    if (UPLOAD.equals(operation))
    {
      uploadFile(instance);
    }
    else if (DOWNLOAD.equals(operation))
    {
      downloadFile(instance);
    }
    else
    {
      applyOperationToInstanceFiles(instance);
    }
    return CONTINUE_OUTCOME;
  }

  private void uploadFile(WorkflowInstance instance) throws Exception
  {
    DataHandler dataHandler = null;
    File tmpFile = null;

    if (filePath != null && filePath.length() > 0) // upload a file
    {
      String path = Template.create(filePath).merge(instance);
      File file = new File(path);
      if (!file.exists()) throw new FileNotFoundException(path);
      dataHandler = new DataHandler(new FileDataSource(file));
    }
    else if (fileURL != null && fileURL.length() > 0) // upload a url content
    {
      URL url = new URL(Template.create(fileURL).merge(instance));
      tmpFile = IOUtils.writeToFile(url.openStream());
      dataHandler = new DataHandler(new FileDataSource(tmpFile));
    }

    if (dataHandler != null)
    {
      DocumentManagerClient client = getDocumentManagerClient(instance);
      HashMap data = new HashMap();

      Properties finalProps = new Properties();
      Template.merge(properties, finalProps, instance);
      data.putAll(finalProps); // add other properties
      data.put(INSTANCE_ID, instance.getInstanceId());

      Document document = new Document();
      Content content = new Content();
      content.setData(dataHandler);
      document.setContent(content);
      // set default properties
      document.setTitle("Document from instance #" + instance.getInstanceId());
      document.setDocTypeId(DictionaryConstants.DOCUMENT_TYPE);
      document.setState(State.DRAFT);
      document.setLanguage(UNIVERSAL_LANGUAGE);
      // set user properties
      document.setIncremental(false);
      for (Object key : data.keySet())
      {
        Property property = new Property();
        property.setName((String)key);
        property.getValue().add((String)data.get(key));
        document.getProperty().add(property);
      }
      // store document
      document = client.storeDocument(document);
      String docId = document.getDocId();
      String contentId = document.getContent().getContentId();

      if (fileVar != null && fileVar.length() > 0)
      {
        instance.put(fileVar, contentId);
      }
      if (documentVar != null && documentVar.length() > 0)
      {
        instance.put(documentVar, docId);
      }
    }
    if (tmpFile != null) tmpFile.delete();
  }

  private void downloadFile(WorkflowInstance instance)
    throws Exception
  {
    if (filePath != null && filePath.length() > 0)
    {
      String path = Template.create(filePath).merge(instance);
      if (fileVar != null && fileVar.length() > 0)
      {
        String uuid = (String) instance.get(fileVar);
        if (uuid != null)
        {
          DocumentManagerClient client = getDocumentManagerClient(instance);
          Content content = client.loadContent(uuid);
          DataHandler dh = content.getData();

          FileOutputStream fos = new FileOutputStream(path);
          try
          {
            dh.writeTo(fos);
          }
          finally
          {
            fos.close();
          }
        }
      }
      else if (documentVar != null && documentVar.length() > 0)
      {
        String docid = (String)instance.get(documentVar);
        if (docid != null)
        {
          DocumentManagerClient client = getDocumentManagerClient(instance);
          Document document = client.loadDocument(docid, 0, ContentInfo.ALL);
          DataHandler dh = document.getContent().getData();

          FileOutputStream fos = new FileOutputStream(path);
          try
          {
            dh.writeTo(fos);
          }
          finally
          {
            fos.close();
          }
        }
      }
    }
  }

  private void applyOperationToInstanceFiles(WorkflowInstance instance)
    throws Exception
  {
    DocumentManagerClient client = getDocumentManagerClient(instance);
    String instanceId = instance.getInstanceId();
    DocumentFilter filter = new DocumentFilter();
    filter.setVersion(0);
    if (documentVar == null || documentVar.trim().length() == 0)
    {
      Property property = new Property();
      if (reference == null || reference.trim().length() == 0)
      {
        property.setName(INSTANCE_ID);
        property.getValue().add(instanceId);
      }
      else
      {
        property.setName(DOCREFERENCE);
        property.getValue().add(instanceId + ":" + reference);
      }
      filter.getProperty().add(property);
    }
    else
    {
      Object o = instance.get(documentVar);
      if (o == null) throw new Exception("documentVar is null");

      String docid = String.valueOf(o);
      filter.getDocId().add(docid);
    }
    List<Document> documents = client.findDocuments(filter);

    List<Property> propertyList = null;

    for (Document document : documents)
    {
      String docid = document.getDocId();

      if (LOCK.equals(operation))
      {
        client.lockDocument(docid, 0);
      }
      else if (UNLOCK.equals(operation))
      {
        client.unlockDocument(docid, 0);
      }
      else if (UPDATE_PROPERTIES.equals(operation))
      {
        if (propertyList == null)
        {
          propertyList = new ArrayList<>();
          Properties finalProps = new Properties();
          Template.merge(properties, finalProps, instance);
          for (Object key : finalProps.keySet())
          {
            Property property = new Property();
            property.setName((String)key);
            property.getValue().add((String)finalProps.get(key));
            propertyList.add(property);
          }
          Property property = new Property();
          property.setName(INSTANCE_ID);
          property.getValue().add(instance.getInstanceId());
          propertyList.add(property);
        }
        document.setVersion(0);
        document.setIncremental(true);
        document.getProperty().clear();
        document.getProperty().addAll(propertyList);
        client.storeDocument(document);
      }
      else if (COMMIT.equals(operation))
      {
        if (State.DRAFT.equals(document.getState()))
        {
          document.setState(State.COMPLETE);
          document.setVersion(0);
          document.setIncremental(true);
          client.storeDocument(document);
        }
      }
      else if (ABORT.equals(operation))
      {
        if (State.DRAFT.equals(document.getState()))
        {
          client.removeDocument(docid, 0);
        }
      }
      else if (DELETE.equals(operation))
      {
        client.removeDocument(docid, 0);
      }
      else if (COMMIT_AND_LOCK.equals(operation))
      {
        document.setState(State.COMPLETE);
        document.setVersion(0);
        document.setIncremental(true);
        //document.setDocTypeId("DNTest");
        client.storeDocument(document);
        client.lockDocument(docid, 0);
      }
    }
  }

  private DocumentManagerClient getDocumentManagerClient(
    WorkflowInstance instance) throws Exception
  {
    String wsdlLocation = null;

    String userId =
      MatrixConfig.getProperty("adminCredentials.userId");
    String password =
      MatrixConfig.getProperty("adminCredentials.password");

    if (serviceURL != null && serviceURL.trim().length() > 0)
    {
      wsdlLocation = Template.create(serviceURL).merge(instance) + "?wsdl";
      return new DocumentManagerClient(wsdlLocation, userId, password);
    }
    else // default
      return new DocumentManagerClient(userId, password);
  }
}
