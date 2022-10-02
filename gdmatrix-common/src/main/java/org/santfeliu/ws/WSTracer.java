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

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import static javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 *
 * @author realor
 */
public class WSTracer implements SOAPHandler<SOAPMessageContext>
{
  private byte[] inboundMessage;
  private byte[] outboundMessage;

  public static WSTracer bind(BindingProvider port)
  {
    WSTracer tracer = new WSTracer();
    Binding binding = port.getBinding();
    List<Handler> handlerChain = binding.getHandlerChain();
    handlerChain.add(tracer);
    binding.setHandlerChain(handlerChain);

    return tracer;
  }

  public byte[] getInboundMessage()
  {
    return inboundMessage;
  }

  public void setInboundMessage(byte[] inboundMessage)
  {
    this.inboundMessage = inboundMessage;
  }

  public byte[] getOutboundMessage()
  {
    return outboundMessage;
  }

  public void setOutboundMessage(byte[] outboundMessage)
  {
    this.outboundMessage = outboundMessage;
  }

  @Override
  public boolean handleMessage(SOAPMessageContext smc)
  {
    try
    {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      SOAPMessage message = smc.getMessage();
      message.writeTo(bos);
      byte[] bytes = bos.toByteArray();

      Boolean outbound = (Boolean)smc.get(MESSAGE_OUTBOUND_PROPERTY);
      if (outbound)
      {
        outboundMessage = bytes;
      }
      else
      {
        inboundMessage = bytes;
      }
    }
    catch (Exception ex)
    {
    }
    return true;
  }

  @Override
  public boolean handleFault(SOAPMessageContext smc)
  {
    return handleMessage(smc);
  }

  @Override
  public Set<QName> getHeaders()
  {
    return null;
  }

  @Override
  public void close(MessageContext mc)
  {
  }
}
