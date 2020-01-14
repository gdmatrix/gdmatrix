package org.santfeliu.faces.component;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.HtmlRenderUtils;
import org.santfeliu.faces.Translator;

/**
 *
 * @author blanquepa
 */
public class HtmlOutputLink 
  extends javax.faces.component.html.HtmlOutputLink
{
  private Translator _translator;
  private String _translationGroup; 
  private String _ariaLabel;
  private Boolean _ariaHidden;
  private String _role;  
  
  public void setTranslator(Translator translator)
  {
    this._translator = translator;
  }

  public Translator getTranslator()
  {
    if (_translator != null)
      return _translator;
    ValueBinding vb = getValueBinding("translator");
    return vb != null? (Translator) vb.getValue(getFacesContext()): null;
  }

  public void setTranslationGroup(String translationGroup)
  {
    this._translationGroup = translationGroup;
  }

  public String getTranslationGroup()
  {
    if (_translationGroup != null)
      return _translationGroup;
    ValueBinding vb = getValueBinding("translationGroup");
    return vb != null? (String) vb.getValue(getFacesContext()): null;
  }

  public void setAriaLabel(String _ariaLabel)
  {
    this._ariaLabel = _ariaLabel;
  }
  
  public String getAriaLabel()
  {
    if (_ariaLabel != null)
      return _ariaLabel;
    ValueBinding vb = getValueBinding("ariaLabel");
    return vb != null? (String) vb.getValue(getFacesContext()): null;
  }
  
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
  
  public void encodeBegin(FacesContext context) throws IOException
  {
    if (context == null)
      throw new NullPointerException();
   
    if (!isRendered()) 
      return;

    ResponseWriter writer = context.getResponseWriter();
    
    HtmlRenderUtils.renderOverlay(writer);

    writer.startElement("a", this);

    Object value = getValue();
    writer.writeAttribute("href", (value != null ? value : ""), null);

    if (getId() != null && !getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))    
    {
      writer.writeAttribute("id", getId(), null);
    }
    if (getStyle() != null)
    {
      writer.writeAttribute("style", getStyle(), null);
    }
    if (getStyleClass() != null)
    {
      writer.writeAttribute("class", getStyleClass(), null);
    }
    String ariaLabel = getAriaLabel();
    if (ariaLabel != null)
    {
      writer.writeAttribute("aria-label", translate(ariaLabel), null);
    }    
    if (getAriaHidden())
    {
      writer.writeAttribute("aria-hidden", "true", null);
    }
    String role = getRole();
    if (role != null)
    {
      writer.writeAttribute("role", role, null);
    }    
    if (getAccesskey() != null)
    {
      writer.writeAttribute("accesskey", getAccesskey(), null);
    }
    if (getTabindex()!= null)
    {
      writer.writeAttribute("tabindex", getTabindex(), null);
    }    
    if (getTarget() != null)
    {
      writer.writeAttribute("target", getTarget(), null);
    }
    if (getOnclick() != null)
    {
      writer.writeAttribute("onclick", getOnclick(), null);
    }
    if (getOnfocus() != null)
    {
      writer.writeAttribute("onfocus", getOnclick(), null);
    } 
    if (getTitle() != null)
    {
      writer.writeAttribute("title", translate(getTitle()), null);
    }   
   
  } 
 
  @Override
  public void encodeEnd(FacesContext context) throws IOException
  {
    if (!isRendered()) return;    
    ResponseWriter writer = context.getResponseWriter();
    writer.endElement("a");
  }  

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[5];
    values[0] = super.saveState(context);
    values[1] = _translationGroup;
    values[2] = _ariaLabel; 
    values[3] = _ariaHidden;   
    values[4] = _role;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[]) state;
    super.restoreState(context, values[0]);
    _translationGroup = (String)values[1];
    _ariaLabel = (String)values[2];
    _ariaHidden = (Boolean)values[3];
    _role = (String)values[4];
  } 
  
  private String translate(String text) throws IOException
  {
    Translator translator = getTranslator();
    if (translator != null)
    {
      String userLanguage = FacesUtils.getViewLanguage();
      StringWriter sw = new StringWriter();
      translator.translate(new StringReader(text), sw, "text/plain",
        userLanguage, getTranslationGroup());
      text = sw.toString();
    }
    return text;
  }  

}
