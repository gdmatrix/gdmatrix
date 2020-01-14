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
package org.santfeliu.sign.service;

import java.util.List;
import javax.activation.DataHandler;
import javax.jws.WebService;
import org.matrix.doc.ContentInfo;
import org.matrix.sign.SignatureInfo;
import org.matrix.sign.SignatureManagerMetaData;
import org.matrix.sign.SignatureManagerPort;
import org.matrix.sign.SignedDocument;
import org.matrix.sign.SignedDocumentFilter;
import org.matrix.sign.TransformOption;

/**
 *
 * @author realor
 */
@WebService(endpointInterface = "org.matrix.sign.SignatureManagerPort")
public class SignatureManager implements SignatureManagerPort
{
  public SignedDocument loadSignedDocument(String sigDocId,
    ContentInfo contentInfo, SignatureInfo signatureInfo, Boolean validate)
  {
    return null;
  }

  public SignedDocument storeSignedDocument(SignedDocument signedDocument)
  {
    return null;
  }

  public boolean removeSignedDocument(String sigDocId)
  {
    return false;
  }

  public List<SignedDocument> findSignedDocuments(SignedDocumentFilter filter)
  {
    return null;
  }

  public DataHandler transformSignedDocument(String sigDocId,
    String toSigDocFormatId, List<TransformOption> option)
  {
    return null;
  }

  public SignatureManagerMetaData getManagerMetaData()
  {
    return null;
  }
}
