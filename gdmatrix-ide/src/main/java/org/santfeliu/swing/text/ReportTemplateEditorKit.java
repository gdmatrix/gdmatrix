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
import javax.swing.text.Element;
import javax.swing.text.View;
import static org.santfeliu.swing.text.HighlightedEditorKit.BOLD;
import static org.santfeliu.swing.text.HighlightedEditorKit.ITALIC;
import static org.santfeliu.swing.text.HighlightedEditorKit.PLAIN;

/**
 *
 * @author blanquepa
 */
public class ReportTemplateEditorKit extends HighlightedEditorKit
{
  
  public ReportTemplateEditorKit()
  {
    // load default scheme
    register(ReportTemplateDocument.UNKNOW, Color.GRAY, PLAIN);

    register(ReportTemplateDocument.HTML_TAG, new Color(0, 0, 128), BOLD);
    register(ReportTemplateDocument.HTML_ATTRIBUTE, new Color(192, 0, 0), PLAIN);
    register(ReportTemplateDocument.HTML_STRING, Color.BLUE, PLAIN);
    register(ReportTemplateDocument.HTML_NUMBER, new Color(0, 128, 0), PLAIN);
    register(ReportTemplateDocument.HTML_TEXT, Color.BLACK, PLAIN);
    register(ReportTemplateDocument.HTML_SYMBOL, Color.BLACK, BOLD);
    register(ReportTemplateDocument.HTML_COMMENT, Color.GRAY, ITALIC);
    
    register(ReportTemplateDocument.JS_KEYWORD, new Color(32, 32, 192), BOLD);
    register(ReportTemplateDocument.JS_IDENTIFIER, new Color(0, 64, 0), PLAIN);
    register(ReportTemplateDocument.JS_NUMBER, new Color(0, 128, 0), PLAIN);
    register(ReportTemplateDocument.JS_STRING, Color.BLUE, PLAIN);
    register(ReportTemplateDocument.JS_OPERATOR, Color.BLACK, PLAIN);
    register(ReportTemplateDocument.JS_COMMENT, Color.GRAY, ITALIC);
    
    register(ReportTemplateDocument.SCRIPT, Color.BLUE, ITALIC);
  }
  
  @Override
  public Document createDefaultDocument()
  {
    ReportTemplateDocument doc = new ReportTemplateDocument();
    return doc;
  }
  
  @Override
  public View create(Element elem)
  {
    return new ReportTemplateView(elem, this);
  }  
  
}
