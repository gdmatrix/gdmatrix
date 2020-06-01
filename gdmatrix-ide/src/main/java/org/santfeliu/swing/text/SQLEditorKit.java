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
public class SQLEditorKit extends HighlightedEditorKit
{
  public SQLEditorKit()
  {
    // load default scheme
    register(SQLDocument.UNKNOW, Color.GRAY, PLAIN);
    register(SQLDocument.KEYWORD, new Color(32, 32, 192), BOLD);
    register(SQLDocument.IDENTIFIER, new Color(0, 32, 0), PLAIN);
    register(SQLDocument.NUMBER, new Color(0, 128, 0), PLAIN);
    register(SQLDocument.STRING, Color.BLUE, PLAIN);
    register(SQLDocument.OPERATOR, Color.BLACK, PLAIN);
    register(SQLDocument.COMMENT, Color.GRAY, ITALIC);
    register(SQLDocument.ALIAS, new Color(0, 128, 0), ITALIC);
    register(SQLDocument.VARIABLE, new Color(64, 64, 128), BOLD);
  }

  public Document createDefaultDocument()
  {
     SQLDocument doc = new SQLDocument();
     return doc;
  }
}