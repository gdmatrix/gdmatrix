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
package org.santfeliu.signature.xmldsig;

import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.resolver.ResourceResolverException;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import static org.santfeliu.signature.xmldsig.HTTPResolver.log;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 *
 * @author realor
 */
public class DetachedResolver extends ResourceResolverSpi
{
  @Override
  public XMLSignatureInput engineResolve(Attr uri, String baseURI) 
    throws ResourceResolverException
  {
    Element signatureElem = 
      (Element)((Element)uri.getOwnerElement().getParentNode()).getParentNode();
    return new XMLSignatureInput(signatureElem.getLastChild());
  }

  @Override
  public boolean engineCanResolve(Attr uri, String baseURI)
  {
    if (uri == null)
    {
      log.debug("quick fail, uri == null");
      return false;
    }
    String uriNodeValue = uri.getNodeValue();
    return "DetachedObjectReference-1".equals(uriNodeValue);      
  }
}
