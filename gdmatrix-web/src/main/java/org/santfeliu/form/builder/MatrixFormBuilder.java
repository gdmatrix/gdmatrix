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
package org.santfeliu.form.builder;

import java.io.InputStream;
import org.matrix.doc.Content;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author realor
 */
public abstract class MatrixFormBuilder extends AbstractFormBuilder
{
  public static final String FORM_TYPEID = "FORM";

  protected DocumentManagerClient getDocumentManagerClient() throws Exception
  {
    return getDocumentManagerClient(null, null);
  }

  protected DocumentManagerClient getDocumentManagerClient(
    String userId, String password) throws Exception
  {
    if (userId == null)
    {
      userId = MatrixConfig.getProperty("adminCredentials.userId");
      password = MatrixConfig.getProperty("adminCredentials.password");
    }
    return new DocumentManagerClient(userId, password);
  }

  protected InputStream getDocumentStream(String contentId)
    throws Exception
  {
    DocumentManagerClient client = getDocumentManagerClient();
    Content content = client.loadContent(contentId);
    return content.getData().getInputStream();
  }
}
