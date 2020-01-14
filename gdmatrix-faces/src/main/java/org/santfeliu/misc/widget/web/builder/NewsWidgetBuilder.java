package org.santfeliu.misc.widget.web.builder;

import org.santfeliu.misc.widget.web.WidgetDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.apache.myfaces.shared_tomahawk.taglib.UIComponentTagUtils;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.news.faces.HtmlNews;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
public class NewsWidgetBuilder extends WidgetBuilder
{
  private static final int DEFAULT_MAX_SUMMARY_CHARS = 0;  
  
  public NewsWidgetBuilder()
  {
  }

  @Override
  public UIComponent getComponent(WidgetDefinition widgetDef,
    FacesContext context)
  {
    HtmlNews component = new HtmlNews();

    component.getAttributes().put("nodeId", widgetDef.getMid());
    
    Map properties = widgetDef.getProperties();
    if (properties != null)
    {
      //Section
      String section = (String)properties.get("section");
      if (isValueReference(section))
        UIComponentTagUtils.setValueBinding(context, component, "section",
          section);
      else
      {
        List<String> sections = widgetDef.getMultivaluedProperty("section");
        component.setSection(sections);
      }

      //Translator
      setTranslationProperties(component, properties, 
        getStrictTranslationGroup(widgetDef, "new"), context);

      //rows
      String rows = (String)properties.get("rows");
      if (rows != null)
        component.setRows(Integer.valueOf(rows).intValue());

      //maxSummaryChars
      String maxSummaryChars = (String)properties.get("maxSummaryChars");
      if (maxSummaryChars != null)
        component.setMaxSummaryChars(Integer.valueOf(maxSummaryChars).intValue());
      else
        component.setMaxSummaryChars(DEFAULT_MAX_SUMMARY_CHARS);      
      
      //style
      component.setStyle((String)properties.get("newsStyle"));
      String newStyleClass = (String)properties.get("newsStyleClass");
      if (newStyleClass == null)
        newStyleClass = "news";
      component.setStyleClass(newStyleClass);

      component.setDateStyle((String)properties.get("dateStyle"));
      String dateStyleClass = (String)properties.get("dateStyleClass");
      if (dateStyleClass == null)
        dateStyleClass = "newDate";
      component.setDateStyleClass(dateStyleClass);

      component.setHeadLineStyle((String)properties.get("headLineStyle"));
      String headLineStyleClass = (String)properties.get("headLineStyleClass");
      if (headLineStyleClass == null)
        headLineStyleClass = "newHeadLine";
      component.setHeadLineStyleClass(headLineStyleClass);

      component.setImageStyle((String)properties.get("imageStyle"));
      String imageStyleClass = (String)properties.get("imageStyleClass");
      if (imageStyleClass == null)
        imageStyleClass = "newImage";
      component.setImageStyleClass(imageStyleClass);

      component.setSummaryStyle((String)properties.get("summaryStyle"));
      String summaryStyleClass = (String)properties.get("summaryStyleClass");
      if (summaryStyleClass == null)
        summaryStyleClass = "newSummary";
      component.setSummaryStyleClass(summaryStyleClass);

      component.setSectionStyle((String)properties.get("sectionStyle"));
      String sectionStyleClass = (String)properties.get("sectionStyleClass");
      if (sectionStyleClass == null)
        sectionStyleClass = "section";
      component.setSectionStyleClass(sectionStyleClass);
      
      component.setVar((String)properties.get("var"));
      String urlValue = (String)properties.get("url");
      UIComponentTagUtils.setStringProperty(context, component, "url", urlValue);

      String dateFormat = (String)properties.get("dateFormat");
      if (dateFormat == null)
        dateFormat = "dd/MM/yyyy";
      component.setDateFormat(dateFormat);

      //image dimensions
      String imageWidth = (String)properties.get("imageWidth");
      if (imageWidth != null)
        component.setImageWidth(imageWidth);
      String imageHeight = (String)properties.get("imageHeight");
      if (imageHeight != null)
        component.setImageHeight(imageHeight);
      String imageCrop = (String)properties.get("imageCrop");
      if (imageCrop != null)
        component.setImageCrop(imageCrop);

      String renderSummary = (String)properties.get("renderSummary");
      if (renderSummary != null)
        component.setRenderSummary(Boolean.valueOf(renderSummary));

      String renderDate = (String)properties.get("renderDate");
      if (renderDate != null)
        component.setRenderDate(Boolean.valueOf(renderDate));

      String urlSeparator = (String)properties.get("urlSeparator");
      if (urlSeparator == null) urlSeparator = "###";
      component.setUrlSeparator(urlSeparator);
      
      String excludeDrafts = (String)properties.get("excludeDrafts");
      List excludeDraftsList = new ArrayList();
      for (Object o : component.getSection())
      {
        if (excludeDrafts == null || excludeDrafts.equals("false"))
        {
          String mid = (String)o;
          UserSessionBean userSessionBean =
            UserSessionBean.getCurrentInstance();
          MenuItemCursor sectionNode =
            userSessionBean.getMenuModel().getMenuItem(mid);
          boolean exclude = !isEditorUser(sectionNode);
          excludeDraftsList.add(exclude);
        }
        else
        {
          excludeDraftsList.add(true);
        }
      }
      component.setExcludeDrafts(excludeDraftsList);
      
      Locale locale = context.getViewRoot().getLocale();
      ResourceBundle bundle =
        ResourceBundle.getBundle("org.santfeliu.news.web.resources.NewsBundle",
        locale);
      component.setDraftText(bundle.getString("new_search_draft"));
    }

    return component;
  }

  private boolean isEditorUser(MenuItemCursor mic)
  {
    List<String> editRoles =
      mic.getMultiValuedProperty(MenuModel.EDIT_ROLES);
    if (editRoles == null || editRoles.isEmpty()) return true;
    return UserSessionBean.getCurrentInstance().isUserInRole(editRoles);
  }

}
