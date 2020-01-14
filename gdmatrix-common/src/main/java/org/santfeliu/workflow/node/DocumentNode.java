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
package org.santfeliu.workflow.node;

import org.santfeliu.util.Properties;
import org.santfeliu.workflow.WorkflowNode;

/**
 *
 * @author unknown
 */
public class DocumentNode extends WorkflowNode
{
  protected static final String WSDL_LOCATION = "wsdlLocation";
  protected static final String UNIVERSAL_LANGUAGE= "%%";

  public static final String UPLOAD = "upload";
  public static final String DOWNLOAD = "download";
  public static final String UPDATE_PROPERTIES = "update properties";
  public static final String COMMIT = "commit";
  public static final String ABORT = "abort";
  public static final String DELETE = "delete";
  public static final String LOCK = "lock";
  public static final String UNLOCK = "unlock";
  public static final String COMMIT_AND_LOCK = "commit & lock";

  public static final String INSTANCE_ID = "workflow.instanceId";
  public static final String DOCREFERENCE = "workflow.documentReference";

  protected String serviceURL = "http://localhost/services/doc";
  protected String operation;
  protected String filePath;
  protected String fileURL;
  protected String documentVar; // docId var
  protected String fileVar; // contenId var
  protected String reference; // documentReference (instanceId when null)
  protected Properties properties = new Properties();

  public DocumentNode()
  {
  }

  @Override
  public String getType()
  {
    return "Document";
  }

  public void setServiceURL(String serviceURL)
  {
    this.serviceURL = serviceURL;
  }

  public String getServiceURL()
  {
    return serviceURL;
  }

  public void setOperation(String operation)
  {
    this.operation = operation;
  }

  public String getOperation()
  {
    return operation;
  }

  public void setProperties(Properties properties)
  {
    this.properties = properties;
  }

  public Properties getProperties()
  {
    return properties;
  }

  public void setFilePath(String filePath)
  {
    this.filePath = filePath;
  }

  public String getFilePath()
  {
    return filePath;
  }

  public String getFileURL()
  {
    return fileURL;
  }

  public void setFileURL(String fileURL)
  {
    this.fileURL = fileURL;
  }

  public void setFileVar(String fileVar)
  {
    this.fileVar = fileVar;
  }

  public String getFileVar()
  {
    return fileVar;
  }

  public void setDocumentVar(String documentVar)
  {
    this.documentVar = documentVar;
  }

  public String getDocumentVar()
  {
    return documentVar;
  }

  public void setReference(String reference)
  {
    this.reference = reference;
  }

  public String getReference()
  {
    return reference;
  }

  @Override
  public Object clone() throws CloneNotSupportedException
  {
    DocumentNode newNode = (DocumentNode) super.clone();
    newNode.properties = (Properties) this.properties.clone();
    return newNode;
  }

  @Override
  public boolean containsText(String text)
  {
    if (super.containsText(text)) return true;
    if (documentVar != null && documentVar.equals(text)) return true;
    if (fileVar != null && fileVar.equals(text)) return true;
    return false;
  }
  
 
}
