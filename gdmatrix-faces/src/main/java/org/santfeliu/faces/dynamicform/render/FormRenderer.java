package org.santfeliu.faces.dynamicform.render;

import javax.faces.context.FacesContext;
import javax.faces.render.Renderer;
import org.santfeliu.form.Form;

/**
 *
 * @author realor
 */
public abstract class FormRenderer extends Renderer
{
  public int getSuitability(Form form, FacesContext facesContext)
  {
    return 1;
  }

  @Override
  public boolean getRendersChildren()
  {
    return true;
  }
}
