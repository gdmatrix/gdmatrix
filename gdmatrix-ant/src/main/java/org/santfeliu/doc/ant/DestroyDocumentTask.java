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
package org.santfeliu.doc.ant;

import java.util.List;
import org.apache.tools.ant.BuildException;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentConstants;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.doc.DocumentManagerService;
import org.matrix.doc.State;
import org.matrix.util.WSEndpoint;
import org.santfeliu.ant.ws.WSTask;

/**
 *
 * @author blanquepa
 */
public class DestroyDocumentTask extends WSTask
{
  public static final String ALL = "all";
  public static final String PURGE = "purge";

  private String docIdVar;
  private String mode;
  private boolean logical = false;

  public String getDocIdVar()
  {
    return docIdVar;
  }

  public void setDocIdVar(String docIdVar)
  {
    this.docIdVar = docIdVar;
  }

  public boolean isLogical()
  {
    return logical;
  }

  public void setLogical(boolean logical)
  {
    this.logical = logical;
  }

  public String getMode()
  {
    return mode;
  }

  public void setMode(String mode)
  {
    this.mode = mode;
  }

  @Override
  public void execute()
  {
    WSEndpoint endpoint = getEndpoint(DocumentManagerService.class);
    DocumentManagerPort port =
      endpoint.getPort(DocumentManagerPort.class, getUsername(), getPassword());

    if (docIdVar == null)
      throw new BuildException("docIdVar not found");
    String docId = (String)getVariable(docIdVar);

    if (logical)
    {
      Document doc =
        port.loadDocument(docId, DocumentConstants.LAST_VERSION, ContentInfo.ID);
      int lastVersion = doc.getVersion();

      int version = DocumentConstants.FIND_ALL_VERSIONS;
      DocumentFilter filter = new DocumentFilter();
      filter.getDocId().add(docId);
      filter.setVersion(version);
      List<Document> documents = port.findDocuments(filter);
      if (documents != null)
      {
        for (Document document : documents)
        {
          if (PURGE.equalsIgnoreCase(mode) && document.getVersion() != lastVersion ||
              !PURGE.equalsIgnoreCase(mode))
          {
            document.setState(State.DELETED);
            document.setIncremental(true);
            port.storeDocument(document);
          }
        }
      }
    }
    else
    {
      int version = DocumentConstants.DELETE_ALL_VERSIONS;

      if (PURGE.equalsIgnoreCase(mode))
        version = DocumentConstants.DELETE_OLD_VERSIONS;

      port.removeDocument(docId, version);
    }
  }
}
