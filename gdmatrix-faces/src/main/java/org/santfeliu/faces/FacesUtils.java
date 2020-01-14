package org.santfeliu.faces;

import java.lang.reflect.Method;

import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.faces.FactoryFinder;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.matrix.util.MessageBuilder;
import org.santfeliu.util.TextUtils;

public class FacesUtils
{
  public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";

  public static FacesContext createFacesContext(ServletContext servletContext,
    HttpServletRequest request, HttpServletResponse response)
    throws Exception
  {
    FacesContextFactory facesContextFactory = (FacesContextFactory)
      FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
    LifecycleFactory lifecycleFactory =
      (LifecycleFactory)FactoryFinder.getFactory(
        FactoryFinder.LIFECYCLE_FACTORY);
    String lifecycleId =
      servletContext.getInitParameter("javax.faces.LIFECYCLE_ID");
    if (lifecycleId == null)
    {
      lifecycleId = LifecycleFactory.DEFAULT_LIFECYCLE;
    }
    Lifecycle lifecycle = lifecycleFactory.getLifecycle(lifecycleId);

    FacesContext context = facesContextFactory.getFacesContext(
      servletContext, request, response, lifecycle);
    return context;
  }

  public static FacesMessage createFacesMessage(String summary, String detail,
    Locale locale, Object params[], FacesMessage.Severity severity)
  {
    if (params != null)
    {
      MessageFormat mf = new MessageFormat(summary, locale);
      summary = mf.format(params, new StringBuffer(), null).toString();
      if (detail != null)
      {
         mf.applyPattern(detail);
         detail = mf.format(params, new StringBuffer(), null).toString();
      }
    }
    return new FacesMessage(severity, summary, detail);
  }
  
  public static FacesMessage getFacesMessage(Exception ex)
  {
    String messageId = ex.getMessage();
    if (messageId == null) messageId = ex.toString();
    return getFacesMessage(messageId, null, FacesMessage.SEVERITY_ERROR);
  }

  public static FacesMessage getFacesMessage(String messageId,
    Object params[], FacesMessage.Severity severity)
  {
    FacesMessage facesMessage;
    FacesContext context = FacesContext.getCurrentInstance();
    Locale locale = context.getViewRoot().getLocale();
    try
    {
      // look for Web Service message
      String summary = MessageBuilder.getMessage(messageId, locale);
      facesMessage = new FacesMessage(severity, summary, null);
    }
    catch (MissingResourceException mex)
    {
      // look for JavaServer Faces message
      String bundleName = context.getApplication().getMessageBundle();
      ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
      String summary = null;
      String detail = null;
      try
      {
        summary = bundle.getString(messageId);
        detail = bundle.getString(messageId + "_detail");
      }
      catch (MissingResourceException ex)
      {
      }

      if (summary == null && severity != FacesMessage.SEVERITY_INFO)
      {
        detail = null;
        try
        {
          if (severity != FacesMessage.SEVERITY_WARN)
          {
            summary = bundle.getString(UNKNOWN_ERROR);
            params = new Object[]{messageId};
          }
        }
        catch (MissingResourceException ex)
        {
          summary = messageId;
          params = null;
        }
      }
      if (summary == null)
      {
        summary = messageId;
      }
      facesMessage =
        createFacesMessage(summary, detail, locale, params, severity);
    }
    return facesMessage;
  }

  public static void addMessage(String summary, String detail, Object[] params, 
    FacesMessage.Severity severity)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Locale locale = context.getViewRoot().getLocale();
    
    FacesMessage message = 
      FacesUtils.createFacesMessage(summary, detail, locale, params, severity);
    context.addMessage(null, message);    
  }
  
  public static void addMessage(String messageId,
    Object params[], FacesMessage.Severity severity)
  {
    FacesMessage message = getFacesMessage(messageId, params, severity);
    FacesContext context = FacesContext.getCurrentInstance();
    context.addMessage(null, message);
  }

  public static void addMessage(UIComponent component, String messageId,
    Object params[], FacesMessage.Severity severity)
  {
    FacesMessage message = getFacesMessage(messageId, params, severity);
    FacesContext context = FacesContext.getCurrentInstance();
    context.addMessage(component.getClientId(context), message);
  }

  public static void addMessage(Exception ex)
  {
    FacesMessage message = getFacesMessage(ex);
    FacesContext context = FacesContext.getCurrentInstance();
    context.addMessage(null, message);
  }

  public static void addMessage(UIComponent component, Exception ex)
  {
    FacesMessage message = getFacesMessage(ex);
    FacesContext context = FacesContext.getCurrentInstance();
    context.addMessage(component.getClientId(context), message);
  }

  public static List<SelectItem> getListSelectItems(Collection list,
    String valueProperty, String labelProperty, boolean nullable)
  {
    List<SelectItem> selectItems = new ArrayList<SelectItem>();
    try
    {
      if (nullable)
      {
        SelectItem selectItem = new SelectItem("", " ");
        selectItems.add(selectItem);
      }
      for (Object item : list)
      {
        SelectItem selectItem = new SelectItem();

        Method vm = item.getClass().getMethod(
          "get" + valueProperty.substring(0, 1).toUpperCase() +
          valueProperty.substring(1), new Class[]{});
        Object value = vm.invoke(item, new Object[]{});
        if (value == null) value = "";
        selectItem.setValue(value);

        Method lm = item.getClass().getMethod(
          "get" + labelProperty.substring(0, 1).toUpperCase() +
          labelProperty.substring(1), new Class[]{});
        Object label = lm.invoke(item, new Object[]{});
        if (label == null) label = value;
        selectItem.setLabel(label.toString());

        selectItems.add(selectItem);
      }
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
    return selectItems;
  }

  public static SelectItem[] getEnumSelectItems(Class clazz,
    ResourceBundle bundle)
  {
    Class<Enum> enumClass = clazz;
    Enum[] constants = enumClass.getEnumConstants();
    SelectItem[] selectItems = new SelectItem[constants.length];
    int i = 0;
    for (Enum constant : constants)
    {
      String label = constant.toString();
      if (bundle != null)
      {
        label = bundle.getString(clazz.getName() + "." + label);
      }
      selectItems[i++] = new SelectItem(constant, label);
    }
    return selectItems;
  }

  public static Locale getViewLocale()
  {
    UIViewRoot view = FacesContext.getCurrentInstance().getViewRoot();
    return view.getLocale();
  }

  public static String getViewLanguage()
  {
    UIViewRoot view = FacesContext.getCurrentInstance().getViewRoot();
    return view.getLocale().getLanguage();
  }

  public static String getParentFormId(UIComponent component,
    FacesContext context)
  {
    UIComponent parent = component.getParent();
    while (parent != null)
    {
      if (parent instanceof UIForm)
      {
        return ((UIForm)parent).getClientId(context);
      }
      parent = parent.getParent();
    }
    return null;
  }

  public static List<SelectItem> sortSelectItems(List<SelectItem> selectItems)
  {
    Collections.sort(selectItems, new Comparator<SelectItem>()
    {
      public int compare(SelectItem o1, SelectItem o2)
      {
        String o1Label = TextUtils.unAccent(o1.getLabel());
        String o2Label = TextUtils.unAccent(o2.getLabel());
        return o1Label.compareTo(o2Label);
      }
    });
    return selectItems;
  }

}
