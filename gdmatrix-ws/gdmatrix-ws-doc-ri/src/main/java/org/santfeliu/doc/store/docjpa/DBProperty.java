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
package org.santfeliu.doc.store.docjpa;


/**
 *
 * @author unknown
 */
public class DBProperty
{
  public static final String UNIVERSAL_LANGUAGE = "%%";
  
  private String docId;
  private int version;
  private String name;
  private int index;  
  private String value;
  
  private DBDocument document;

  
  public DBProperty()
  {
  }
  
  public DBProperty(String documentId, int version, String name, int index,
    String language, String value)
  {
    this.docId = documentId;
    this.version = version;
    this.index = index;
    int idx = name.indexOf(";");
    if (idx > 0)
    {
      String[] splitName = name.split(";");
      name = splitName[0];
      language = splitName[1];
    }
    this.name = name;
    this.value = value;
  }

  public void setDocId(String documentId)
  {
    this.docId = documentId;
  }

  public String getDocId()
  {
    return docId;
  }

  public void setVersion(int version)
  {
    this.version = version;
  }

  public int getVersion()
  {
    return version;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }

  public void setIndex(int index)
  {
    this.index = index;
  }

  public int getIndex()
  {
    return index;
  }

  public String toString()
  {
    String result = 
      "Property: " + docId + ";" + version + ";" + name + ";" + index + "-->" + value;
      
    return result;
  }

  public void setDocument(DBDocument document)
  {
    this.document = document;
  }

  public DBDocument getDocument()
  {
    return document;
  }
}
