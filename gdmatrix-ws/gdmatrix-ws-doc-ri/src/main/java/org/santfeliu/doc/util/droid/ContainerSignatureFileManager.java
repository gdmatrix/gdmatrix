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

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import uk.gov.nationalarchives.droid.container.httpservice.ContainerSignatureHttpService;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureFileInfo;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureServiceException;

/**
 *
 * @author blanquepa
 */
public class ContainerSignatureFileManager extends AbstractSignatureFileManager
{
  private static final String DEFAULT_SERVICE_URL = 
    "http://www.nationalarchives.gov.uk/pronom/container-signature.xml";
  private static final String DEFAULT_FILENAME_PATTERN =
    "container-signature-%s.xml";    
  
  private final String serviceURL = DEFAULT_SERVICE_URL;
  
  @Override
  public Path downloadFile(Path baseDir) throws Exception
  {
    try
    {
      ContainerSignatureHttpService containerSignatureUpdateService
        = new ContainerSignatureHttpService(serviceURL);
      SignatureFileInfo sigFileInfo = 
        containerSignatureUpdateService.importSignatureFile(baseDir);
      this.file = sigFileInfo.getFile();
    }
    catch (SignatureServiceException ex)
    {
      if (ex.getCause() instanceof FileAlreadyExistsException)
      {
        this.file = getCurrentFile(baseDir);
      }
    }
    return this.file;
  }

  @Override
  public Path getCurrentFile(Path baseDir) throws Exception
  {
    if (this.file != null)
      return this.file;
    else
      return getDefaultFile(baseDir, DEFAULT_FILENAME_PATTERN);
  }

  @Override
  public ResolverType getResolverType()
  {
    return ResolverType.CONTAINER;
  }
  
}
