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
package org.santfeliu.doc.store;

import java.util.List;

import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.dic.Property;
import org.matrix.doc.RelatedDocument;

/**
 *
 * @author blanquepa
 */
public interface DocumentStoreConnection
{
  public Document storeDocument(Document document)
    throws Exception;
    
  public void storeRelatedDocuments(String docId, int version, 
    List<RelatedDocument> relDocs)
    throws Exception;

  public void storeProperties(String docId, int version, 
    List<Property> properties)
    throws Exception;

  public void storeAccessControlList(Document document)
    throws Exception;

  public Document loadDocument(String docId, int version)
    throws Exception;

  public boolean removeDocument(String docId, int version, boolean persistent) throws Exception;

  public void removeProperties(String docId, int version) throws Exception;

  public List<Document> findDocuments(DocumentFilter filter, 
    List<String> userRoles, boolean isAdminUser)
    throws Exception;
    
  public int countDocuments(DocumentFilter filter, List<String> userRoles,
    boolean isAdminUser)
    throws Exception;

  public boolean isContentInUse(String contentId)
    throws Exception;
 
  public void commit()
    throws Exception;
    
  public void rollback()
    throws Exception;
    
  public void close()
    throws Exception;
}
