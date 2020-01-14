package org.santfeliu.web.obj;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.faces.model.SelectItem;
import org.matrix.dic.Property;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.faces.FacesBean;

/**
 *
 * @author blanquepa
 */
public class PropertiesFilter extends FacesBean implements Serializable
{
  protected List<Property> inputProps;
  protected List<Property> selectProps;
  protected List<SelectItem> propDefSelectItems;
  protected String currentTypeId;

  public PropertiesFilter()
  {
    currentTypeId = null;
    addInputProperty();
    addSelectProperty();
  }

  //Public actions
  public String addInputProperty()
  {
    if (inputProps == null)
      inputProps = new ArrayList();

    Property current = (Property)getValue("#{idp}");
    if (current == null ||
       (current.getName() != null && current.getName().length() > 0))
    {
      Property dp = new Property();
      dp.setName(null);
      dp.getValue().clear();
      inputProps.add(dp);
    }

    return null;
  }

  public String addSelectProperty()
  {
    createPropDefSelectItems();
    if (selectProps == null)
      selectProps = new ArrayList();

    Property current = (Property)getValue("#{sdp}");
    if (current == null ||
       (current.getName() != null && current.getName().length() > 0))
    {
      Property dp = new Property();
      dp.setName(null);
      dp.getValue().clear();
      selectProps.add(dp);
    }

    return null;
  }

  public String removeInputProperty()
  {
    return removeDynamicProperty(inputProps, "#{idp}");
  }

  public String removeSelectProperty()
  {
    createPropDefSelectItems();
    return removeDynamicProperty(selectProps, "#{sdp}");
  }

  //Render
  public boolean isRenderPropDefSelectItems()
  {
    return (propDefSelectItems != null && propDefSelectItems.size() > 1);
  }

  public boolean isRenderInputAddButton()
  {
    Property dp = (Property)getValue("#{idp}");
    return (isLastItem(inputProps, dp) && inputProps.size() < 3);
  }

  public boolean isRenderSelectAddButton()
  {
    Property dp = (Property)getValue("#{sdp}");
    return (isLastItem(selectProps, dp) && selectProps.size() < 3);
  }

  //Accessors
  public String getCurrentTypeId()
  {
    return currentTypeId;
  }

  public void setCurrentTypeId(String currentTypeId)
  {
    this.currentTypeId = currentTypeId;
  }

  public void setSelectedTypeId(String typeId)
  {
    setCurrentTypeId(typeId);
    createPropDefSelectItems();
  }

  public List<Property> getInputProps()
  {
    return inputProps;
  }

  public void setInputProps(List<Property> inputProps)
  {
    this.inputProps = inputProps;
  }

  public List<SelectItem> getPropDefSelectItems()
  {
    return propDefSelectItems;
  }

  public void setPropDefSelectItems(List<SelectItem> propDefSelectItems)
  {
    this.propDefSelectItems = propDefSelectItems;
  }

  public List<Property> getSelectProps()
  {
    return selectProps;
  }

  public void setSelectProps(List<Property> selectProps)
  {
    this.selectProps = selectProps;
  }

  public String getSelectValue()
  {
    List<String> values = (List<String>)getValue("#{sdp.value}");
    if (values != null && values.size() > 0)
      return values.get(0);
    else
      return null;
  }

  public void setSelectValue(String value)
  {
    List<String> values = (List<String>)getValue("#{sdp.value}");
    if (values != null)
    {
      values.clear();
      values.add(value);
    }
  }

  public String getInputValue()
  {
    List<String> values = (List<String>)getValue("#{idp.value}");
    if (values != null && values.size() > 0)
      return values.get(0);
    else
      return null;
  }

  public void setInputValue(String value)
  {
    List<String> values = (List<String>)getValue("#{idp.value}");
    if (values != null)
    {
      values.clear();
      values.add(value);
    }
  }

  //Private methods
  private String removeDynamicProperty(List<Property> props,
    String currentVBExpr)
  {
    if (props != null && props.size() > 1)
    {
      Property dp = (Property)getValue(currentVBExpr);
      props.remove(dp);
    }
    return null;
  }

  public void createPropDefSelectItems()
  {
    propDefSelectItems = new ArrayList();
    propDefSelectItems.add(new SelectItem("", " "));
    String typeId = currentTypeId;
    if (typeId != null && typeId.trim().length() > 0
      && !ControllerBean.SEPARATOR_ID.equals(typeId))
    {
      Type type = TypeCache.getInstance().getType(typeId);
      if (type != null)
      {
        List<PropertyDefinition> pds = 
          type.getPropertyDefinition(false, true, false);
        for (PropertyDefinition pd : pds)
        {
          if (!pd.isHidden())
          {
            SelectItem item = new SelectItem(pd.getName(), pd.getDescription());
            propDefSelectItems.add(item);
          }
        }
      }
    }
    Collections.sort(propDefSelectItems, new Comparator<SelectItem>()
    {
      public int compare(SelectItem o1, SelectItem o2)
      {
        return o1.getLabel().compareTo(o2.getLabel());
      }
    });
  }

  private boolean isLastItem(List<Property> props, Property dp)
  {
    int lastIndex = props.lastIndexOf(dp);
    return lastIndex == props.size() - 1;
  }
}
