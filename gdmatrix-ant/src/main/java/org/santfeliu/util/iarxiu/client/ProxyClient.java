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
package org.santfeliu.util.iarxiu.client;

import org.springframework.oxm.xmlbeans.XmlBeansMarshaller;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import com.hp.iarxiu.core.schemas._2_0.ingest.IngestRequestDocument;
import com.hp.iarxiu.core.schemas._2_0.ingest.IngestResponseDocument;


/**
 *
 * @author unknown
 */
public class ProxyClient extends WebServiceGatewaySupport
{
	public ProxyClient()
  {
		XmlBeansMarshaller xmlBeansMarshaller = new XmlBeansMarshaller();
		setMarshaller(xmlBeansMarshaller);
		setUnmarshaller(xmlBeansMarshaller);
	}

	public IngestResponseDocument send(IngestRequestDocument request)
  {
		return (IngestResponseDocument)
      getWebServiceTemplate().marshalSendAndReceive(request);
	}
	
	public Object send(Object request)
  {
		return getWebServiceTemplate().marshalSendAndReceive(request);
	}

  public String getTrustStore()
  {
    return System.getProperty("javax.net.ssl.trustStore");
  }

  public void setTrustStore(String trustStore)
  {
		System.setProperty("javax.net.ssl.trustStore", trustStore);
  }

  public String getTrustStorePassword()
  {
		return System.getProperty("javax.net.ssl.trustStorePassword");
  }

  public void setTrustStorePassword(String trustStorePassword)
  {
		System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
  }

  public String getTrustStoreType()
  {
    return System.getProperty("javax.net.ssl.trustStoreType");
  }

  public void setTrustStoreType(String trustStoreType)
  {
		System.setProperty("javax.net.ssl.trustStoreType", trustStoreType);
  }
}
