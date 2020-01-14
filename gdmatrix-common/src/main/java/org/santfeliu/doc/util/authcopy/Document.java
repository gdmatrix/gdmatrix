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

import java.util.ArrayList;
import java.util.List;
import javax.activation.DataHandler;
import org.matrix.dic.Property;
import org.santfeliu.dic.util.DictionaryUtils;

/**
 *
 * @author blanquepa
 */
public class Document
{
  private String csv;
  private String title;
  private String docType;
  private String contentType;
  private DataHandler data;
  private List<String> signatures;
  private List<Property> properties = new ArrayList();
  
  public String getTitle()
  {
    return title;
  }

  void setTitle(String title)
  {
    this.title = title;
  }

  public String getDocType()
  {
    return docType;
  }

  void setDocType(String docType)
  {
    this.docType = docType;
  }

  public String getCsv()
  {
    return csv;
  }

  void setCsv(String csv)
  {
    this.csv = csv;
  }

  public String getContentType()
  {
    return contentType;
  }

  void setContentType(String contentType)
  {
    this.contentType = contentType;
  }
  
  public DataHandler getData()
  {
    return data;
  }

  void setData(DataHandler data)
  {
    this.data = data;
  }

  public List<String> getSignatures()
  {
    return signatures;
  }
  
  void addSignature(String signature)
  {
    if (this.signatures == null)
      this.signatures = new ArrayList();
    
    signatures.add(signature);
  }
  
  public boolean isSigned()
  {
    return (signatures != null && !signatures.isEmpty());
  }

  public List<Property> getProperties()
  {
    return properties;
  }

  void setProperties(List<Property> properties)
  {
    this.properties = properties;
  }
  
  boolean isXmlSignedDocument()
  {
    return "text/xml".equals(contentType)
      && DictionaryUtils.getPropertyValue(properties, "SignedDocumentClass") != null;
  }
  
  boolean isPdf()
  {
    return "application/pdf".equals(contentType);
  }
  
}
