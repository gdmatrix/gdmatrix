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

import org.santfeliu.util.iarxiu.converter.Converter;
import java.util.ArrayList;
import java.util.List;
import org.santfeliu.util.iarxiu.mets.Div;
import org.santfeliu.util.iarxiu.mets.Metadata;
import org.santfeliu.util.iarxiu.mets.MetsConstants;

/**
 *
 * @author blanquepa
 */
public class PITCase extends PITObject
{
  PITCase(PIT pit)
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

  void setCaseId(String caseId)
  {
    div.setLabel(caseId);
  }

  public String getCaseId()
  {
    return div.getLabel();
  }

  public PITDocument newDocument()
  {
    int count = getDocuments().size();
    PITDocument doc = new PITDocument(pit);
    doc.setDocId(getCaseId() + "_DOC_" + count);
    div.getDivs().add(doc.getDiv());

    return doc;
  }

  public List<PITDocument> getDocuments()
  {
    List<PITDocument> result = new ArrayList<PITDocument>();
    for (Div d : this.div.getDivs())
    {
      PITDocument document = new PITDocument(pit);
      document.copyFrom(d);
      result.add(document);
    }
    return result;
  }

//  public Metadata newDmdMetadata() throws Exception
//  {
//    return newDmdMetadata(MetsConstants.OTHER_MDTYPE_VALUE, PIT.CASE_URN);
//  }
//
//  public Metadata newDmdMetadata(Object src) throws Exception
//  {
//    return newDmdMetadata(MetsConstants.OTHER_MDTYPE_VALUE, PIT.CASE_URN, src);
//  }

  @Override
  public Metadata newDmdMetadata(String type, String urn) throws Exception
  {
    return super.newDmdMetadata(type, urn);
  }

  public Metadata newDmdMetadata(String type, String urn, Object source, Converter converter)
    throws Exception
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

  public Metadata newAmdMetadata() throws Exception
  {
    return newAmdMetadata(MetsConstants.OTHER_MDTYPE_VALUE, PIT.CASE_URN);
  }

  public Metadata newAmdMetadata(Object source) throws Exception
  {
    return newAmdMetadata(MetsConstants.OTHER_MDTYPE_VALUE, PIT.CASE_URN, source);
  }

  @Override
  public Metadata newAmdMetadata(String type, String urn) throws Exception
  {
    return super.newAmdMetadata(type, urn);
  }

  public Metadata newAmdMetadata(String type, String urn, Object source)
    throws Exception
  {
    Metadata metadata = newAmdMetadata(type, urn);
    Converter converter = pit.getConverter(urn);
    converter.convert(source, metadata);
    return metadata;
  }

  @Override
  public List<Metadata> getAmdMetadatas()
  {
    return super.getAmdMetadatas();
  }
}
