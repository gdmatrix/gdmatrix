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
package org.santfeliu.ws;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author realor
 */
public class WSExceptionFactory
{
  public static WebServiceException create(Exception ex, String ... details)
  {
    if (ex instanceof WebServiceException) return (WebServiceException)ex;

    String messageId = ex.getMessage(); // recognized error
    if (messageId == null) messageId = "UNKNOWN_ERROR";

    return create(messageId, details);
  }

  public static WebServiceException create(String messageId, String ... details)
  {
    try
    {
      SOAPFactory soapFactory = SOAPFactory.newInstance();
      SOAPFault fault = soapFactory.createFault(messageId,
        new QName("http://www.w3.org/2003/05/soap-envelope", "Receiver"));
      Detail d = fault.addDetail();
      for (String line : details)
      {
        d.addDetailEntry(new QName("line")).addTextNode(line);
      }
      return new SOAPFaultException(fault);
    }
    catch (Exception ex2)
    {
      return new WebServiceException(messageId);
    }
  }

  public static List<String> getDetails(Exception ex)
  {
    ArrayList<String> details = new ArrayList();
    if (ex instanceof SOAPFaultException)
    {
      Detail detail = ((SOAPFaultException)ex).getFault().getDetail();
      if (detail != null)
      {
        Iterator<DetailEntry> iter = detail.getDetailEntries();
        while (iter.hasNext())
        {
          NodeList childNodes = iter.next().getChildNodes();
          for (int i = 0; i < childNodes.getLength(); i++)
          {
            Node node = childNodes.item(i);
            if (node.getNodeName().equals("message"))
            {
              details.add(node.getTextContent());
              break;
            }
          }
        }
      }
    }
    return details;
  }
}


