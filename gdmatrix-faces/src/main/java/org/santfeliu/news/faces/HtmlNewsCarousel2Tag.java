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
public class HtmlNewsCarousel2Tag extends UIComponentTag
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
  private String moreNewsAriaLabel;
  private String prevBlockLabel;
  private String prevBlockIconURL;
  private String showBlockLabel;
  private String nextBlockLabel; 
  private String nextBlockIconURL;
  private String moreInfoURL;
  private String moreNewsURL;
  private String excludeDrafts;
  private String imageWidth;
  private String imageHeight;
  private String imageCrop;  
  private String maxSummaryChars;
  private String renderDate;  
  private String newsPerBlock;

  public String getRows()
  {
    return rows;
  }

  public void setRows(String rows)
  {
    this.rows = rows;
  }

  public String getNewsPerBlock()
  {
    return newsPerBlock;
  }

  public void setNewsPerBlock(String newsPerBlock)
  {
    this.newsPerBlock = newsPerBlock;
  }

  public String getTransitionTime()
  {
    return transitionTime;
  }

  public void setTransitionTime(String transitionTime)
  {
    this.transitionTime = transitionTime;
  }

  public String getMaxSummaryChars()
  {
    return maxSummaryChars;
  }

  public void setMaxSummaryChars(String maxSummaryChars)
  {
    this.maxSummaryChars = maxSummaryChars;
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

  public String getMoreNewsAriaLabel()
  {
    return moreNewsAriaLabel;
  }

  public void setMoreNewsAriaLabel(String moreNewsAriaLabel)
  {
    this.moreNewsAriaLabel = moreNewsAriaLabel;
  }

  public String getPrevBlockLabel()
  {
    return prevBlockLabel;
  }

  public void setPrevBlockLabel(String prevBlockLabel)
  {
    this.prevBlockLabel = prevBlockLabel;
  }

  public String getShowBlockLabel()
  {
    return showBlockLabel;
  }

  public void setShowBlockLabel(String showBlockLabel)
  {
    this.showBlockLabel = showBlockLabel;
  }

  public String getNextBlockLabel()
  {
    return nextBlockLabel;
  }

  public void setNextBlockLabel(String nextBlockLabel)
  {
    this.nextBlockLabel = nextBlockLabel;
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
  
  public String getImageCrop()
  {
    return imageCrop;
  }

  public void setImageCrop(String imageCrop)
  {
    this.imageCrop = imageCrop;
  }

  public String getImageHeight()
  {
    return imageHeight;
  }

  public void setImageHeight(String imageHeight)
  {
    this.imageHeight = imageHeight;
  }

  public String getImageWidth()
  {
    return imageWidth;
  }

  public void setImageWidth(String imageWidth)
  {
    this.imageWidth = imageWidth;
  }  
  
  public String getRenderDate()
  {
    return renderDate;
  }

  public void setRenderDate(String renderDate)
  {
    this.renderDate = renderDate;
  }  

  public String getPrevBlockIconURL()
  {
    return prevBlockIconURL;
  }

  public void setPrevBlockIconURL(String prevBlockIconURL)
  {
    this.prevBlockIconURL = prevBlockIconURL;
  }

  public String getNextBlockIconURL()
  {
    return nextBlockIconURL;
  }

  public void setNextBlockIconURL(String nextBlockIconURL)
  {
    this.nextBlockIconURL = nextBlockIconURL;
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
      UIComponentTagUtils.setIntegerProperty(
        context, component, "maxSummaryChars", maxSummaryChars);
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
        context, component, "moreNewsAriaLabel", moreNewsAriaLabel);
      UIComponentTagUtils.setStringProperty(
        context, component, "prevBlockLabel", prevBlockLabel);
      UIComponentTagUtils.setStringProperty(
        context, component, "prevBlockIconURL", prevBlockIconURL);
      UIComponentTagUtils.setStringProperty(
        context, component, "showBlockLabel", showBlockLabel);
      UIComponentTagUtils.setStringProperty(
        context, component, "nextBlockLabel", nextBlockLabel);
      UIComponentTagUtils.setStringProperty(
        context, component, "nextBlockIconURL", nextBlockIconURL);
      UIComponentTagUtils.setStringProperty(
        context, component, "moreInfoURL", moreInfoURL);
      UIComponentTagUtils.setStringProperty(
        context, component, "moreNewsURL", moreNewsURL);
      UIComponentTagUtils.setBooleanProperty(
        context, component, "excludeDrafts", excludeDrafts);
      UIComponentTagUtils.setStringProperty(
        context, component, "imageWidth", imageWidth);
      UIComponentTagUtils.setStringProperty(
        context, component, "imageHeight", imageHeight);
      UIComponentTagUtils.setStringProperty(
        context, component, "imageCrop", imageCrop);  
      UIComponentTagUtils.setBooleanProperty(
        context, component, "renderDate", renderDate); 
      UIComponentTagUtils.setIntegerProperty(
        context, component, "newsPerBlock", newsPerBlock);      
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
    maxSummaryChars = null;
    section = null;
    translator = null;
    translationGroup = null;
    moreInfoLabel = null;
    moreNewsLabel = null;
    moreNewsAriaLabel = null;
    prevBlockLabel = null;
    prevBlockIconURL = null;
    showBlockLabel = null;
    nextBlockLabel = null;
    nextBlockIconURL = null;
    moreInfoURL = null;
    moreNewsURL = null;
    imageWidth = null;
    imageHeight = null;
    imageCrop = null;
    renderDate = null;
    newsPerBlock = null;
  }

  @Override
  public String getComponentType()
  {
    return "NewsCarousel2Widget";
  }

  @Override
  public String getRendererType()
  {
    return null;
  }
}
