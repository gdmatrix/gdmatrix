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
package org.santfeliu.doc.util.authcopy;

import java.util.Iterator;
import java.util.Map;
import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import org.matrix.dic.Property;
import org.matrix.dic.Type;
import org.matrix.signature.SignatureManagerPort;
import org.matrix.signature.SignatureManagerService;
import org.matrix.signature.SignedDocument;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.signature.PropertyListConverter;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author blanquepa
 */
public class SignatureManagerSource implements Source
{
  private Document document;
  
  private static final String TITLE = "title";
  private static final String DOCTYPEID = "docTypeId";
  
  @Override
  public Document getDocument(String sigId) throws Exception
  {
    SignatureManagerPort port = getSignatureManagerPort();

    if (document == null)
    {
      SignedDocument doc = port.getDocument(sigId);
      document = toDocument(doc);
      document.setCsv(sigId);
      document.extractSignatures();      
    }

    return document;    
  }
  
  private SignatureManagerPort getSignatureManagerPort()
    throws Exception
  {
    String userId =
      MatrixConfig.getProperty("adminCredentials.userId");
    String password =
      MatrixConfig.getProperty("adminCredentials.password");

    WSEndpoint endpoint =
      WSDirectory.getInstance().getEndpoint(SignatureManagerService.class);
    return endpoint.getPort(SignatureManagerPort.class, userId, password);
  }  

  private Document toDocument(SignedDocument doc)
  {
    Map documentProperties = PropertyListConverter.toMap(doc.getProperties());
    Document result = new Document();
    
    //Title
    result.setTitle((String) documentProperties.get(TITLE));
    
    //Type
    Type type = TypeCache.getInstance().getType((String) documentProperties.get(DOCTYPEID));
    result.setDocType(type.getDescription());

    //Data
    ByteArrayDataSource ds = new ByteArrayDataSource(doc.getData(), "text/xml");
    result.setData(new DataHandler(ds));
    
    Iterator it = doc.getProperties().getProperty().iterator();
    while (it.hasNext())
    {
      org.matrix.signature.Property sProp = 
        (org.matrix.signature.Property) it.next();
      Property prop = new Property();
      prop.setName(sProp.getName());
      prop.getValue().add(sProp.getValue());
      result.getProperties().add(prop);
    }
    return result;    
  }
}
