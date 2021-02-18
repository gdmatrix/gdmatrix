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
import java.util.ArrayList;
import java.util.List;
import uk.gov.nationalarchives.droid.container.AbstractContainerIdentifier;
import uk.gov.nationalarchives.droid.container.ContainerFileIdentificationRequestFactory;
import uk.gov.nationalarchives.droid.container.ContainerSignatureFileReader;
import uk.gov.nationalarchives.droid.container.ole2.Ole2Identifier;
import uk.gov.nationalarchives.droid.container.zip.ZipIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.DroidCore;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResultCollection;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ArchiveFormatResolverImpl;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifierFactory;
import uk.gov.nationalarchives.droid.core.interfaces.archive.ContainerIdentifierFactoryImpl;

/**
 *
 * @author blanquepa
 */
public class ContainerSignaturesResolver implements Resolver
{
  private final ContainerIdentifierFactory containerIdentifierFactory;
  private ArchiveFormatResolverImpl containerFormatResolver;
  private final List<AbstractContainerIdentifier> containerIdentifiers; 
  private final DroidCore droidCore;
  
  public ContainerSignaturesResolver(DroidCore droidCore)
  {
    this.droidCore = droidCore;
    
    this.containerIdentifierFactory = new ContainerIdentifierFactoryImpl(); 
    this.containerIdentifiers = new ArrayList<AbstractContainerIdentifier>();
    ZipIdentifier zipIdentifier = new ZipIdentifier();
    zipIdentifier.setContainerType("ZIP");
    zipIdentifier.setContainerIdentifierFactory(containerIdentifierFactory);
    containerIdentifierFactory.addContainerIdentifier("ZIP", zipIdentifier);
    containerIdentifiers.add(zipIdentifier);

    Ole2Identifier ole2Identifier = new Ole2Identifier();
    ole2Identifier.setContainerType("OLE2");
    ole2Identifier.setContainerIdentifierFactory(containerIdentifierFactory);
    containerIdentifierFactory.addContainerIdentifier("OLE2", ole2Identifier);
    containerIdentifiers.add(ole2Identifier);

  }

  @Override
  public void readSignatureFile(File signatureFile) throws Exception
  { 
    containerFormatResolver
      = new ArchiveFormatResolverImpl();
    ContainerFileIdentificationRequestFactory cfirFactory
      = new ContainerFileIdentificationRequestFactory();
    for (AbstractContainerIdentifier item : containerIdentifiers)
    {
      item.setContainerFormatResolver(containerFormatResolver);
      item.setDroidCore(droidCore);
      ContainerSignatureFileReader signatureReader
        = new ContainerSignatureFileReader(signatureFile.getAbsolutePath());
      item.setSignatureReader(signatureReader);
      item.init();
      item.getIdentifierEngine().setRequestFactory(cfirFactory);
    }
  }

  public IdentificationResult identifyPuid(IdentificationRequest request, 
    IdentificationResult idenResult) throws Exception
  {
    if (idenResult != null)
    {  
      String containerContentType =
        containerFormatResolver.forPuid(idenResult.getPuid());

      if (containerContentType != null)
      {
        AbstractContainerIdentifier containerIdentifier =
          (AbstractContainerIdentifier) containerIdentifierFactory
            .getIdentifier(containerContentType);

        IdentificationResultCollection results =
          containerIdentifier.submit(request);
        
        if (!results.getResults().isEmpty())
        {
          droidCore.removeLowerPriorityHits(results);
          idenResult = results.getResults().get(0);
        }
      }
    }
    return idenResult;
  }

  @Override
  public IdentificationResult identify(String filename) throws Exception
  {
    //Do nothing. Only resolves PUIDs after BinarySignaturesResolver 
    //identification.
    return null;
  }

  @Override
  public ResolverType getType()
  {
    return ResolverType.CONTAINER;
  }
  
}
