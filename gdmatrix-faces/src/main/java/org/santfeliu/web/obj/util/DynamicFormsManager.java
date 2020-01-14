package org.santfeliu.web.obj.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.matrix.dic.Property;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.dic.util.PropertyConverter;
import org.santfeliu.form.Form;
import org.santfeliu.form.FormDescriptor;
import org.santfeliu.form.FormFactory;
import org.santfeliu.form.builder.TypeFormBuilder;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;

/**
 *
 * @author blanquepa
 */
public class DynamicFormsManager extends WebBean implements Serializable
{
  public static final String ALLOWED_FORM_SELECTORS = "formSelectors";
  
  private static final String OBJECT_ID_PROPERTY = "_objectId";
  
  protected String currentTypeId;
  protected Map data = new HashMap();
  protected String selector;

  private String typeFormBuilderPrefix;
  private List<String> allowedFormSelectors;

  public DynamicFormsManager(String typeFormBuilderPrefix)
  {
    this.typeFormBuilderPrefix = typeFormBuilderPrefix;
  }

  public DynamicFormsManager(String typeFormBuilderPrefix, List<String> allowedFormSelectors)
  {
    this.typeFormBuilderPrefix = typeFormBuilderPrefix;
    this.allowedFormSelectors = allowedFormSelectors;
  }

  public String getCurrentTypeId()
  {
    return currentTypeId;
  }

  public void setCurrentTypeId(String currentTypeId)
  {
    this.currentTypeId = currentTypeId;
    findForm();
  }

  public void refreshForms()
  {
    findForm();
  }

  public Map getData()
  {
    return data;
  }

  public void setData(Map data)
  {
    this.data = data;
  }

  public String getSelector()
  {
    return selector;
  }

  public void setSelector(String selector)
  {
    this.selector = selector;
  }

  public List<String> getAllowedFormSelectors()
  {
    if (this.allowedFormSelectors != null)
      return allowedFormSelectors;
    else
      return getSelectedMenuItem()
        .getMultiValuedProperty(DynamicFormsManager.ALLOWED_FORM_SELECTORS);
  }

  public Form getForm() throws Exception
  {
    if (selector != null)
    {
      FormFactory formFactory = FormFactory.getInstance();
      // update form only in render phase
      boolean updated = getFacesContext().getRenderResponse();
      return formFactory.getForm(selector, getData(), updated);
    }
    return null;
  }

  public List<Property> transformFormDataToProperties()
  {
    if (data != null)
    {
      Type type = getSelectedType();
      if (type != null)
      {
        PropertyConverter converter = new PropertyConverter(type);
        List<Property> propertyList = converter.toPropertyList(data);
        Property auxObjectIdProperty =
          DictionaryUtils.getPropertyByName(propertyList, OBJECT_ID_PROPERTY);
        if (auxObjectIdProperty != null)
        {
          propertyList.remove(auxObjectIdProperty);
        }
        return propertyList;
      }
    }
    return null;
  }

  public void setFormDataFromProperties(List<Property> properties,
    String objectId)
  {
    if (properties != null)
    {
      Type type = getSelectedType();
      if (type != null)
      {
        PropertyConverter converter = new PropertyConverter(type);
        data = converter.toPropertyMap(properties);
      }
      else data = new HashMap();
    }
    if (objectId != null)
      data.put(OBJECT_ID_PROPERTY, objectId);
  }

  public boolean isTypeUndefined()
  {
    return currentTypeId == null || currentTypeId.length() == 0;
  }

  public org.santfeliu.dic.Type getSelectedType()
  {
    TypeCache typeCache = TypeCache.getInstance();
    if (!isTypeUndefined())
      return typeCache.getType(currentTypeId);
    else return null;
  }

  private void findForm()
  {
    if (!isTypeUndefined())
    {
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      String selectorBase =
        typeFormBuilderPrefix + ":" + currentTypeId +
        TypeFormBuilder.USERID + userSessionBean.getUserId() +
        TypeFormBuilder.PASSWORD + userSessionBean.getPassword();
      FormFactory formFactory = FormFactory.getInstance();
      List<FormDescriptor> descriptors = formFactory.findForms(selectorBase);
      if (descriptors != null && !descriptors.isEmpty())
      {
        String newSelector = null;
        if (descriptors.size() > 0)
        {
          for (FormDescriptor descriptor : descriptors)
          {
            if (matches(getAllowedFormSelectors(), descriptor.getSelector()) &&
              newSelector == null)
              newSelector = descriptor.getSelector();
          }
        }

        if (newSelector == null || !newSelector.equals(selector))
        {
          this.selector = newSelector;
          data.clear();
          updateForm(selector); //TODO: resituar
        }
      }
      else
      {
        data.clear();
        this.selector = null;
      }
    }
    else
    {
      data.clear();
      this.selector = null;
    }
  }

  private void updateForm(String selector)
  {
    if (selector != null)
    {
      FormFactory factory = FormFactory.getInstance();
      factory.clearForm(selector);
    }
  }

  private boolean matches(List<String> formSelectors, String selector)
  {
    if (selector != null && formSelectors != null && !formSelectors.isEmpty())
    {
      for (String formSelector : formSelectors)
      {
        if (selector.matches(formSelector))
          return true;
      }
    }
    return false;
  }
}
