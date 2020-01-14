
package org.santfeliu.misc.mapviewer.pdfgen;

/**
 *
 * @author realor
 */
public class NodeIdParser
{
  /* id format in components: *<componentName>_<argument> */
  public static final String COMPONENT_TAG = "comp_"; // deprecated
  private String componentName;
  private String argument;

  public void parse(String id)
  {
    componentName = null;
    argument = null;
    if (id.startsWith(COMPONENT_TAG))
    {
      id = id.substring(COMPONENT_TAG.length());
    }
    int index = id.indexOf("_");
    if (index != -1)
    {
      componentName = id.substring(0, index);
      argument = id.substring(index + 1);
    }
    else
    {
      componentName = id;
    }
  }

  public String getComponentName()
  {
    return componentName;
  }

  public String getArgument()
  {
    return argument;
  }
}
