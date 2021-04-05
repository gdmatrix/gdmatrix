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
package org.santfeliu.workflow.store.wsfile;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import org.santfeliu.workflow.Workflow;
import org.santfeliu.workflow.WorkflowException;
import org.santfeliu.workflow.io.WorkflowReader;
import org.santfeliu.workflow.store.BaseWorkflowStore;
import org.santfeliu.workflow.store.dsdbf.DBDataStore;

/**
 *
 * @author realor
 */
public class FileWorkflowStore extends BaseWorkflowStore
{
  private static final String PKG = DBDataStore.class.getName() + ".";

  private final String WORKFLOW_EXTENSION = ".xml";
  private final String WORKFLOW_VERSION_SEPARATOR = "@";
  
  private String directory;

  public FileWorkflowStore()
  {
  }

  public void setDirectory(String directory)
  {
    if (!directory.endsWith("/")) directory += "/";  
    this.directory = directory;
  }

  public String getDirectory()
  {
    return directory;
  }

  @Override
  public void init(Properties properties) throws WorkflowException
  {
    try
    {
      setDirectory(properties.getProperty(PKG + "directory"));
    }
    catch (Exception ex)
    {
      throw WorkflowException.createException(ex);
    }
  }

  @Override
  public String getCurrentWorkflowVersion(String workflowName)
  {
    String currentVersion = "";
    File dir = new File(directory);
    File[] files = dir.listFiles();
    for (int i = 0; i < files.length; i++)
    {
      File file = files[i];
      String filename = file.getName();
      String nv[] = getWorkflowNameAndVersion(filename);
      if (nv != null)
      {
        if (nv[0].equals(workflowName) && nv[1].compareTo(currentVersion) > 0)
        {
          currentVersion = nv[1];
        }
      }
    }
    return currentVersion;
  }

  @Override
  public Collection getWorkflowNames() throws WorkflowException
  {
    HashSet set = new HashSet();
    File dir = new File(directory);
    File[] files = dir.listFiles();
    for (int i = 0; i < files.length; i++)
    {
      File file = files[i];
      String filename = file.getName();
      String nv[] = getWorkflowNameAndVersion(filename);
      if (nv != null)
      {
        set.add(nv[0]);
      }
    }
    return set;
  }
  
  /* if workflowVersion == null returns the last version */
  @Override
  protected Workflow loadWorkflow(
    String workflowName, String workflowVersion) throws WorkflowException
  {
    try
    {
      if (workflowVersion == null)
        workflowVersion = getCurrentWorkflowVersion(workflowName);

      File file = new File(directory + workflowName + 
        WORKFLOW_VERSION_SEPARATOR + workflowVersion + WORKFLOW_EXTENSION);
      if (!file.exists())
      {
        file = new File(directory + workflowName + WORKFLOW_EXTENSION);
        if (!file.exists())
          throw new WorkflowException("Invalid workflow name or version");
      }
      WorkflowReader reader = new WorkflowReader();
      Workflow workflow = reader.read(new FileInputStream(file));
      workflow.setVersion(workflowVersion);
      return workflow;
    }
    catch (Exception ex)
    {
      throw WorkflowException.createException(ex);
    }
  }

  private String[] getWorkflowNameAndVersion(String filename)
  {
    String result[] = new String[2];
    int index = filename.lastIndexOf(WORKFLOW_EXTENSION);
    if (index == -1) return null;
    
    String nameVersion = filename.substring(0, index);
    index = nameVersion.indexOf(WORKFLOW_VERSION_SEPARATOR);
    if (index == -1)
    {
      result[0] = nameVersion;
      result[1] = "1";
    }
    else
    {
      result[0] = nameVersion.substring(0, index);
      result[1] = nameVersion.substring(index + 1);
    }
    return result;
  }

  public static void main(String[] args)
  {
    try
    {
      FileWorkflowStore store = new FileWorkflowStore();
      store.setDirectory("c:/workflow");
      System.out.println(store.getWorkflowNames());
      System.out.println(store.getCurrentWorkflowVersion("endevina4"));
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
