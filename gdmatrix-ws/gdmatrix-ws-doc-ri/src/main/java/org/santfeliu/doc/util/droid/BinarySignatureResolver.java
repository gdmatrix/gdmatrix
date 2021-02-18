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
package org.santfeliu.doc.util.droid;

import java.io.File;
import java.io.IOException;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.RequestIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;

/**
 *
 * @author blanquepa
 */
public class BinarySignatureResolver implements Resolver
{
  private final BinarySignatureIdentifier droidCore;
  
  private final ContainerSignaturesResolver containerResolver;
  
  public BinarySignatureResolver()
  {
    this.droidCore = new BinarySignatureIdentifier();
    this.containerResolver = new ContainerSignaturesResolver(droidCore);
  }

  public BinarySignatureIdentifier getDroidCore()
  {
    return droidCore;
  }

  public ContainerSignaturesResolver getContainerResolver()
  {
    return containerResolver;
  }

  @Override
  public void readSignatureFile(File signatureFile) throws Exception
  {
    readSignatureFile(signatureFile.getAbsolutePath());
  }

  public void readSignatureFile(String signatureFile) throws Exception
  {
    droidCore.setSignatureFile(signatureFile);
    droidCore.init();
  }  

  @Override
  public IdentificationResult identify(String filename) throws Exception
  {
    IdentificationRequest idenRequest = setUpIdentificationRequest(filename);
    IdentificationResult idenResult = matchBinarySignatures(idenRequest);   
    return containerResolver.identifyPuid(idenRequest, idenResult);
  }
  
  private IdentificationResult matchBinarySignatures(
    IdentificationRequest request) throws IOException
  {
    IdentificationResultCollection results = 
      droidCore.matchBinarySignatures(request);
    
    if (results.getResults().isEmpty())
      results = droidCore.matchExtensions(request, true);
    
    droidCore.removeLowerPriorityHits(results);
    if (!results.getResults().isEmpty())
      return results.getResults().get(0);
    else
      return null;     
  }

  private FileSystemIdentificationRequest 
    setUpIdentificationRequest(String filename) throws IOException
  {
    File file = new File(filename);
    RequestMetaData metaData
      = new RequestMetaData(file.length(), file.lastModified(), filename);
    RequestIdentifier requestIdentifier = new RequestIdentifier(file.toURI());
    FileSystemIdentificationRequest request
      = new FileSystemIdentificationRequest(metaData, requestIdentifier);

    request.open(file.toPath());

    return request;
  }  

  @Override
  public ResolverType getType()
  {
    return ResolverType.BINARY;
  }
  
}
