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
package cat.aoc.iarxiu.mets;

import javax.xml.namespace.QName;
import cat.aoc.iarxiu.util.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author blanquepa
 */
public abstract class XmlMetadata extends Metadata
{
  protected QName namespace;

  protected Element getRootElement(Element element) throws Exception
  {
    Node node = DOMUtils.getFirstChild(element);
    String tagName = node.getLocalName();
    if (MetsConstants.XMLDATA_TAG.equals(tagName))
    {
      node = DOMUtils.getFirstChild((Element)node);
      if (node instanceof Element)
      {
        element = (Element)node;
      }
    }
    else
      throw new Exception("'" + MetsConstants.XMLDATA_TAG + "' tag expected");
    return element;
  }

  protected QName getNamespace()
  {
    return namespace;
  }

  protected void setNamespace(QName namespace)
  {
    this.namespace = namespace;
  }
}
