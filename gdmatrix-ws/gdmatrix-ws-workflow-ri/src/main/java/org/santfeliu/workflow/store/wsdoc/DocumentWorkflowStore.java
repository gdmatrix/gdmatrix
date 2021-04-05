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
package org.santfeliu.workflow.store.wsdoc;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.dic.Property;
import org.matrix.translation.TranslationConstants;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.workflow.Workflow;
import org.santfeliu.workflow.WorkflowException;
import org.santfeliu.workflow.io.WorkflowReader;
import org.santfeliu.workflow.store.BaseWorkflowStore;
import org.santfeliu.workflow.util.WorkflowFixer;
import org.santfeliu.workflow.util.WorkflowFixer.Issue;

/**
 *
 * @author realor
 */
public class DocumentWorkflowStore extends BaseWorkflowStore
{
  public static final String PROPERTY_NAME = "workflow.xml";

  private String userId;
  private String password;

  public DocumentWorkflowStore()
  {
  }

  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  public String getUserId()
  {
    return userId;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public String getPassword()
  {
    return password;
  }

  @Override
  public void init(Properties properties) throws WorkflowException
  {
    try
    {
      setUserId(properties.getProperty("adminCredentials.userId"));
      setPassword(properties.getProperty("adminCredentials.password"));
    }
    catch (Exception ex)
    {
      throw WorkflowException.createException(ex);
    }
  }

  @Override
  public String getCurrentWorkflowVersion(String workflowName)
    throws WorkflowException
  {
    try
    {
      DocumentManagerClient client = getDocumentManagerClient();
      String docTypeId = "WORKFLOW";
      DocumentFilter filter = new DocumentFilter();
      filter.setDocTypeId(docTypeId);
      Property property = new Property();
      property.setName(PROPERTY_NAME);
      property.getValue().add(workflowName);
      filter.getProperty().add(property);
      filter.setVersion(0);
      List<Document> documents = client.findDocuments(filter);
      if (documents.isEmpty())
      {
        throw new WorkflowException("Invalid workflow name");
      }
      else
      {
        return String.valueOf(documents.get(0).getVersion());
      }
    }
    catch (Exception ex)
    {
      throw WorkflowException.createException(ex);
    }
  }

  @Override
  public Collection getWorkflowNames() throws WorkflowException
  {
    return null;
  }

  /* if workflowVersion == null returns the last version */

  @Override
  protected Workflow loadWorkflow(String workflowName, String workflowVersion)
    throws WorkflowException
  {
    Workflow workflow = null;
    try
    {
      System.out.println("WFN:" + workflowName + " VER:" +
                         workflowVersion);

      DocumentManagerClient client = getDocumentManagerClient();
      Document document = null;
      if (workflowVersion == null)
      {
        document = client.loadDocumentByName("WORKFLOW", PROPERTY_NAME,
          workflowName, TranslationConstants.UNIVERSAL_LANGUAGE, 0);
      }
      else
      {
        document = client.loadDocumentByName("WORKFLOW", PROPERTY_NAME,
          workflowName, TranslationConstants.UNIVERSAL_LANGUAGE,
          Integer.parseInt(workflowVersion));
      }
      if (document == null)
        throw new WorkflowException("Invalid workflow name or version");

      workflowVersion = String.valueOf(document.getVersion());
      String contentId = document.getContent().getContentId();

      System.out.println("Workflow contentId: " + contentId);

      DataHandler dh = document.getContent().getData();
      DataSource dataSource = dh.getDataSource();
      InputStream stream = dataSource.getInputStream();
      try
      {
        WorkflowReader reader =
          new WorkflowReader("org.santfeliu.workflow.processor");
        workflow = reader.read(stream);
        workflow.setVersion(workflowVersion);
      }
      finally
      {
        stream.close();
      }
      // auto fix workflow
      String fixEnabled =
        MatrixConfig.getProperty("org.santfeliu.workflow.fixEnabled");
      if ("true".equals(fixEnabled))
      {
        String fixRulesURL =
          MatrixConfig.getProperty("org.santfeliu.workflow.fixRulesURL");
        if (fixRulesURL != null)
        {
          fixWorkflow(workflow, fixRulesURL);
        }
      }
      return workflow;
    }
    catch (WorkflowException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new WorkflowException(ex);
    }
  }

  protected DocumentManagerClient getDocumentManagerClient()
    throws Exception
  {
    return new DocumentManagerClient(userId, password);
  }

  private void fixWorkflow(Workflow workflow, String fixRulesURL)
  {
    try
    {
      URL url = new URL(fixRulesURL);
      WorkflowFixer fixer = WorkflowFixer.getInstance(url);
      List<Issue> issues = fixer.check(workflow.getNodes());
      for (Issue issue : issues)
      {
        issue.fix();
      }
    }
    catch (Exception ex)
    {
    }
  }
}
