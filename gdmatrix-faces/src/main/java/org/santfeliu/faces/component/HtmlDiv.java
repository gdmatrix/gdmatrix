package org.santfeliu.faces.component;

import java.io.IOException;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import org.apache.myfaces.custom.div.Div;

/**
 *
 * @author blanquepa
 */
public class HtmlDiv extends Div
{
  private Boolean _ariaHidden;
  private String _role;  
  
  public static final String COMPONENT_TYPE = "Div";  

  public void setAriaHidden(Boolean ariaHidden)
  {
    this._ariaHidden = ariaHidden;
  }

  public Boolean getAriaHidden()
  {
    if (_ariaHidden != null) return _ariaHidden;
    ValueBinding vb = getValueBinding("ariaHidden");
    return (vb != null ? (Boolean)vb.getValue(getFacesContext()) : 
      Boolean.FALSE);
  }

  public String getRole()
  {
    if (_role != null)
      return _role;
    ValueBinding vb = getValueBinding("role");
    return vb != null? (String) vb.getValue(getFacesContext()): null;
  }

  public void setRole(String role)
  {
    this._role = role;
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (context == null)
      throw new NullPointerException();
   
    if (!isRendered()) 
      return;
    
    super.encodeBegin(context);

    ResponseWriter writer = context.getResponseWriter();    

    if (getAriaHidden())
    {
      writer.writeAttribute("aria-hidden", "true", null);
    }
    String role = getRole();
    if (role != null)
    {
      writer.writeAttribute("role", role, null);
    }    
  } 
 
  @Override
  public void encodeEnd(FacesContext context) throws IOException
  {
    super.encodeEnd(context);
  }
  
  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[3];
    values[0] = super.saveState(context);
    values[1] = _ariaHidden;   
    values[2] = _role;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[]) state;
    super.restoreState(context, values[0]);
    _ariaHidden = (Boolean)values[1];
    _role = (String)values[2];
  }   

}
