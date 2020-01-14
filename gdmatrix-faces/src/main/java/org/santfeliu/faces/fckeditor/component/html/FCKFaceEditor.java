package org.santfeliu.faces.fckeditor.component.html;

import java.io.IOException;

import java.util.Map;


import javax.faces.component.html.HtmlInputTextarea;
import javax.faces.context.FacesContext;

import javax.faces.el.ValueBinding;
import javax.faces.render.RenderKit;
import javax.faces.render.Renderer;


import org.santfeliu.faces.fckeditor.taglib.html.FCKFaceEditorTag;
import org.santfeliu.web.UserPreferences;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author srecinto
 *
 */
public class FCKFaceEditor extends HtmlInputTextarea
{
  public static final String COMPONENT_FAMILY = "org.fckfaces.FCKFacesFamily";
  private static final String DEFAULT_RENDERER_TYPE = "org.ckfaces.EditorRenderer";
  private String toolbarSet;
  private String height;
  private String width;
  private Map configProperties;

  public String getComponentType()
  {
    return FCKFaceEditorTag.COMPONENT_TYPE;
  }

  public String getRendererType()
  {
    return DEFAULT_RENDERER_TYPE;
  }
  
  public void encodeBegin(FacesContext context)
    throws IOException
  {
    super.encodeBegin(context);
  }

  /**
    * Moved to encode end so that the inline java script will run after the textArea was rendered before this script is run
    * @param context
    * @throws IOException
    */
  public void encodeEnd(FacesContext context)
    throws IOException
  {
    String editorRenderer = DEFAULT_RENDERER_TYPE;
    
    try
    {
      UserPreferences userPreferences = UserSessionBean.getCurrentInstance().getUserPreferences();      
      editorRenderer = userPreferences.getPreference("ckEditor");
    }
    catch (Exception ex)
    {
    }
    
    if (editorRenderer == null)
      editorRenderer = DEFAULT_RENDERER_TYPE;

    RenderKit renderKit = context.getRenderKit();
    Renderer renderer = renderKit.getRenderer(getFamily(), editorRenderer);
    if (renderer != null) 
      renderer.encodeEnd(context, this);
    else
      getRenderer(context).encodeEnd(context, this);
  }

  /**
	 *
	 * @return
	 */
  public String getFamily()
  {
    return COMPONENT_FAMILY;
  }

  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[6];
    values[0] = super.saveState(context);
    values[1] = toolbarSet;
    values[2] = width;
    values[3] = height;
    values[4] = configProperties;

    return values;
  }

  public void restoreState(FacesContext context, Object state)
  {
    Object values[] = (Object[]) state;
    super.restoreState(context, values[0]);
    this.toolbarSet = (String) values[1];
    this.width = (String)values[2];
    this.height = (String)values[3];
    this.configProperties = (Map)values[4];
  }

  public String getToolbarSet()
  {
    return toolbarSet;
  }

  public void setToolbarSet(String toolbarSet)
  {
    this.toolbarSet = toolbarSet;
  }

  public String getHeight()
  {
    return height;
  }

  public void setHeight(String height)
  {
    System.out.println("compsetheight=" + height);
    this.height = height;
  }

  public String getWidth()
  {
    return width;
  }

  public void setWidth(String width)
  {
    this.width = width;
  }

  public void setConfigProperties(Map configProperties)
  {
    this.configProperties = configProperties;
  }

  public Map getConfigProperties()
  {
    if (configProperties != null) return configProperties;
    ValueBinding vb = getValueBinding("configProperties");
    return vb != null ? (Map)vb.getValue(getFacesContext()) : null;
  }
}
