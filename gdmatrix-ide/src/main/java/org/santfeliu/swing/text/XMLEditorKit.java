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
package org.santfeliu.swing.text;

import java.awt.Color;
import javax.swing.text.Document;

/**
 *
 * @author realor
 */
public class XMLEditorKit extends HighlightedEditorKit
{
  public XMLEditorKit()
  {
    // load default scheme
    register(XMLDocument.UNKNOW, Color.GRAY, PLAIN);
    register(XMLDocument.TAG, new Color(0, 0, 128), BOLD);
    register(XMLDocument.ATTRIBUTE, new Color(192, 0, 0), PLAIN);
    register(XMLDocument.STRING, Color.BLUE, PLAIN);
    register(XMLDocument.NUMBER, new Color(0, 128, 0), PLAIN);
    register(XMLDocument.TEXT, Color.BLACK, PLAIN);
    register(XMLDocument.SYMBOL, Color.BLACK, BOLD);
    register(XMLDocument.COMMENT, Color.GRAY, ITALIC);
  }

  @Override
  public Document createDefaultDocument()
  {
     XMLDocument doc = new XMLDocument();
     return doc;
  }
}
