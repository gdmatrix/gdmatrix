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
package org.santfeliu.signature.store;

import java.util.HashMap;

import java.util.Properties;

import org.santfeliu.signature.SignedDocument;
import org.santfeliu.signature.SignedDocumentStore;


/**
 *
 * @author unknown
 */
public class MemoryStore implements SignedDocumentStore
{
  static HashMap documents = new HashMap();

  public MemoryStore()
  {
  }

  public void init(Properties properties) throws Exception
  {
  }

  public SignedDocument loadSignedDocument(String sigId)
    throws Exception
  {
    if (!documents.containsKey(sigId))
      throw new Exception("signature:SIGNED_DOCUMENT_NOT_FOUND");

    return (SignedDocument)documents.get(sigId);
  }
  
  public String createSignedDocument(SignedDocument document)
    throws Exception
  {
    String sigId = createSigId();
    documents.put(sigId, document);
    return sigId;
  }

  public void updateSignedDocument(String sigId, SignedDocument document)
    throws Exception
  {
    if (!documents.containsKey(sigId))
      throw new Exception("signature:SIGNED_DOCUMENT_NOT_FOUND");
    
    documents.put(sigId, document);
  }

  public void deleteSignedDocument(String sigId)
    throws Exception
  {
    documents.remove(sigId);
  }
  
  public void closeSignedDocument(String sigId)
  {
  }
  
  private String createSigId()
  {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < 3; i++)
    {
      int number = (int)(Math.random() * Integer.MAX_VALUE);
      buffer.append(Integer.toHexString(number).toUpperCase());
    }
    return buffer.toString();
  }
}
