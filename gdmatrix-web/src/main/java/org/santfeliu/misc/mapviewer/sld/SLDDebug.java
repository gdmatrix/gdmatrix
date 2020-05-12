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
package org.santfeliu.misc.mapviewer.sld;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 *
 * @author real
 */
public class SLDDebug
{
  public void printSLD(SLDRoot root, PrintStream out)
  {
    out.println("SLD " + root.getAttributes());
    List<SLDNamedLayer> namedLayers = root.getNamedLayers();
    for (SLDNamedLayer namedLayer : namedLayers)
    {
      out.println("  NamedLayer " + namedLayer.getLayerName());
      for (SLDUserStyle userStyle : namedLayer.getUserStyles())
      {
        out.println("    UserStyle " + userStyle.getStyleName());
        for (SLDRule rule : userStyle.getRules())
        {
          out.println("      Rule " + rule.getTitle());
          for (SLDSymbolizer symbolizer : rule.getSymbolizers())
          {
            out.println("        " + symbolizer.getClass().getSimpleName());
          }
        }
      }
    }
  }  
  
  public void printNode(SLDNode node, PrintStream out) throws IOException
  {
    printNode(node, out, 0);
  }
  
  private void printNode(SLDNode node, PrintStream out, int indent) 
    throws IOException
  {
    for (int i = 0; i < indent; i++) out.print(" ");
    if (node.getName() == null) out.println("Text: " + node.getTextValue());
    else
    {
      if (node.getPrefix() != null) out.print(node.getPrefix() + ":");
      out.print(node.getName() + " " + node.getAttributes() +
        " (" + node.getClass().getSimpleName() + ") ");
      if (node.getTextValue() != null)
        out.print("\"" + node.getTextValue() + "\"");
      out.println();
      for (int c = 0; c < node.getChildCount(); c++)
      {
        SLDNode child = node.getChild(c);
        printNode(child, out, indent + 2);
      }
    }
  }
}
