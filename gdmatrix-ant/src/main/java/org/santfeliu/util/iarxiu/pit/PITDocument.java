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
package org.santfeliu.util.iarxiu.pit;

import org.santfeliu.util.iarxiu.converter.DocumentConverter;
import org.santfeliu.util.iarxiu.converter.Converter;
import java.util.ArrayList;
import java.util.List;
import org.santfeliu.util.iarxiu.mets.Div;
import org.santfeliu.util.iarxiu.mets.Metadata;

/**
 *
 * @author blanquepa
 */
public class PITDocument extends PITObject
{
  PITDocument(PIT pit)
  {
    this.div = new Div();
    this.pit = pit;
  }

  void copyFrom(Div div)
  {
    this.div.setLabel(div.getLabel());
    this.div.setDmdMetadatas(div.getDmdMetadatas());
    this.div.setAmdMetadatas(div.getAmdMetadatas());
    this.div.setDivs(div.getDivs());
  }

  void setDocId(String docId)
  {
    div.setLabel(docId);
  }

  public String getDocId()
  {
    return div.getLabel();
  }

//  public Metadata newDmdMetadata() throws Exception
//  {
//    String urn = pit.getType().equals(PIT.CASE_TEMPLATE_URN) ?
//      PIT.CASE_DOCUMENT_URN : PIT.DOCUMENT_URN;
//    return this.newDmdMetadata(MetsConstants.OTHER_MDTYPE_VALUE, urn);
//  }
//
//  public Metadata newDmdMetadata(Object src) throws Exception
//  {
//    String urn = pit.getType().equals(PIT.CASE_TEMPLATE_URN) ?
//      PIT.CASE_DOCUMENT_URN : PIT.DOCUMENT_URN;
//    return newDmdMetadata(MetsConstants.OTHER_MDTYPE_VALUE, urn, src);
//  }
//
//  @Override
//  public Metadata newDmdMetadata(String type, String urn) throws Exception
//  {
//    return super.newDmdMetadata(type, urn);
//  }
//
//  public Metadata newDmdMetadata(String type, String urn, Object source)
//    throws Exception
//  {
//    Metadata metadata = newDmdMetadata(type, urn);
//    Converter converter = pit.getConverter(urn);
//    converter.convert(source, metadata);
//    return metadata;
//  }

  @Override
  public Metadata newDmdMetadata(String type, String urn)
    throws Exception
  {
    Metadata metadata = super.newDmdMetadata(type, urn);
    return metadata;
  }  
  
  public Metadata newDmdMetadata(String type, String urn, Object source, 
    Converter converter) throws Exception
  {
    Metadata metadata = newDmdMetadata(type, urn);
    converter.convert(source, metadata);
    return metadata;
  }  
  
  @Override
  public List<Metadata> getDmdMetadatas()
  {
    return super.getDmdMetadatas();
  }

//  public Metadata newAmdMetadata() throws Exception
//  {
//    return newAmdMetadata(MetsConstants.OTHER_MDTYPE_VALUE, PIT.DOCUMENT_URN);
//  }
//
//  @Override
//  public Metadata newAmdMetadata(String type, String urn) throws Exception
//  {
//    return super.newAmdMetadata(type, urn);
//  }
//
//  public Metadata newAmdMetadata(Object source) throws Exception
//  {
//    return newAmdMetadata(MetsConstants.OTHER_MDTYPE_VALUE, PIT.DOCUMENT_URN, source);
//  }

  @Override
  public Metadata newAmdMetadata(String type, String urn)
    throws Exception
  {
    Metadata metadata = super.newAmdMetadata(type, urn);
    return metadata;
  }
  
  public Metadata newAmdMetadata(String type, String urn, Object source, 
    Converter converter) throws Exception
  {
    Metadata metadata = newAmdMetadata(type, urn);
    converter.convert(source, metadata);
    return metadata;
  }

  @Override
  public List<Metadata> getAmdMetadatas()
  {
    return super.getAmdMetadatas();
  }

  public PITContent newContent()
  {
    int count = getContents().size();
    PITContent cnt = new PITContent(pit);
    String id = getDocId() + "_CNT_" + count;
    cnt.setId(id);
    cnt.setName(id); //Initialize with id value
    div.getDivs().add(cnt.getDiv());

    return cnt;
  }

  public PITContent newContent(Object source) throws Exception
  {
    PITContent pitContent = newContent();

    String urn = pit.getType().equals(PIT.CASE_TEMPLATE_URN) ?
      PIT.CASE_DOCUMENT_URN : PIT.DOCUMENT_URN;
    DocumentConverter converter = (DocumentConverter)pit.getConverter(urn);
    converter.convert(source, pitContent);
    
    return pitContent;
  }

  public PITContent newContent(Object source, DocumentConverter converter)
    throws Exception
  {
    PITContent pitContent = newContent();
    converter.convert(source, pitContent);
    
    return pitContent;
  }
  
  public List<PITContent> getContents()
  {
    List<PITContent> result = new ArrayList<PITContent>();
    for (Div d : div.getDivs())
    {
      PITContent content = new PITContent(pit);
      content.copyFrom(d);
      result.add(content);
    }

    return result;
  }
}
