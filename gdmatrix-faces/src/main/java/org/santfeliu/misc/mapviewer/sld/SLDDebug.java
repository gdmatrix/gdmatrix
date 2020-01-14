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
