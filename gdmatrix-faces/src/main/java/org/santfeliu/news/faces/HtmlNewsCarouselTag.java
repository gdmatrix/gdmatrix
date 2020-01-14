package org.santfeliu.news.faces;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;

/**
 *
 * @author lopezrj
 */
public class HtmlNewsCarouselTag extends UIComponentTag
{
  private String section;
  private String rows;
  private String transitionTime;
  private String translator;
  private String translationGroup;
  private String style;
  private String styleClass;
  private String var;
  private String moreInfoLabel;
  private String moreNewsLabel;
  private String moreInfoURL;
  private String moreNewsURL;
  private String excludeDrafts;

  public String getRows()
  {
    return rows;
  }

  public void setRows(String rows)
  {
    this.rows = rows;
  }

  public String getTransitionTime()
  {
    return transitionTime;
  }

  public void setTransitionTime(String transitionTime)
  {
    this.transitionTime = transitionTime;
  }

  public String getSection()
  {
    return section;
  }

  public void setSection(String section)
  {
    this.section = section;
  }

  public String getTranslationGroup()
  {
    return translationGroup;
  }

  public void setTranslationGroup(String translationGroup)
  {
    this.translationGroup = translationGroup;
  }

  public String getTranslator()
  {
    return translator;
  }

  public void setTranslator(String translator)
  {
    this.translator = translator;
  }

  public String getStyle()
  {
    return style;
  }

  public void setStyle(String style)
  {
    this.style = style;
  }

  public String getStyleClass()
  {
    return styleClass;
  }

  public void setStyleClass(String styleClass)
  {
    this.styleClass = styleClass;
  }

  public String getVar()
  {
    return var;
  }

  public void setVar(String var)
  {
    this.var = var;
  }

  public String getMoreInfoLabel()
  {
    return moreInfoLabel;
  }

  public void setMoreInfoLabel(String moreInfoLabel)
  {
    this.moreInfoLabel = moreInfoLabel;
  }

  public String getMoreNewsLabel()
  {
    return moreNewsLabel;
  }

  public void setMoreNewsLabel(String moreNewsLabel)
  {
    this.moreNewsLabel = moreNewsLabel;
  }

  public String getMoreInfoURL()
  {
    return moreInfoURL;
  }

  public void setMoreInfoURL(String moreInfoURL)
  {
    this.moreInfoURL = moreInfoURL;
  }

  public String getMoreNewsURL()
  {
    return moreNewsURL;
  }

  public void setMoreNewsURL(String moreNewsURL)
  {
    this.moreNewsURL = moreNewsURL;
  }

  public String getExcludeDrafts()
  {
    return excludeDrafts;
  }

  public void setExcludeDrafts(String excludeDrafts)
  {
    this.excludeDrafts = excludeDrafts;
  }

  @Override
  protected void setProperties(UIComponent component)
  {
    try
    {
      FacesContext context = FacesContext.getCurrentInstance();
      super.setProperties(component);
      UIComponentTagUtils.setIntegerProperty(
        context, component, "rows", rows);
      UIComponentTagUtils.setIntegerProperty(
        context, component, "transitionTime", transitionTime);
      if (section != null)
      {
        if (isValueReference(section))
        {
          ValueBinding vb = context.getApplication().createValueBinding(section);
          component.setValueBinding("section", vb);
        }
        else
        {
          UIComponentTagUtils.setStringProperty(
            context, component, "section", section);
        }
      }
      if (translator != null)
      {
        if (isValueReference(translator))
        {
          ValueBinding vb = context.getApplication().createValueBinding(translator);
          component.setValueBinding("translator", vb);
        }
      }
      UIComponentTagUtils.setStringProperty(
        context, component, "translationGroup", translationGroup);
      UIComponentTagUtils.setStringProperty(
        context, component, "style", style);
      UIComponentTagUtils.setStringProperty(
        context, component, "styleClass", styleClass);
      UIComponentTagUtils.setStringProperty(
        context, component, "var", var);
      UIComponentTagUtils.setStringProperty(
        context, component, "moreInfoLabel", moreInfoLabel);
      UIComponentTagUtils.setStringProperty(
        context, component, "moreNewsLabel", moreNewsLabel);
      UIComponentTagUtils.setStringProperty(
        context, component, "moreInfoURL", moreInfoURL);
      UIComponentTagUtils.setStringProperty(
        context, component, "moreNewsURL", moreNewsURL);
      UIComponentTagUtils.setBooleanProperty(
        context, component, "excludeDrafts", excludeDrafts);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  @Override
  public void release()
  {
    super.release();
    rows = null;
    transitionTime = null;
    section = null;
    translator = null;
    translationGroup = null;
    moreInfoLabel = null;
    moreNewsLabel = null;
    moreInfoURL = null;
    moreNewsURL = null;
  }

  @Override
  public String getComponentType()
  {
    return "NewsCarouselWidget";
  }

  @Override
  public String getRendererType()
  {
    return null;
  }
}
