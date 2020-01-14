package org.santfeliu.kernel.web;

import java.util.List;
import javax.faces.model.SelectItem;

import org.matrix.kernel.AddressFilter;
import org.matrix.kernel.AddressView;
import org.matrix.kernel.PersonAddressFilter;
import org.matrix.kernel.PersonAddressView;
import org.santfeliu.web.obj.ControllerBean;

import org.santfeliu.web.obj.ObjectBean;

public class AddressBean extends ObjectBean
{
  public AddressBean()
  {
  }

  public String getObjectTypeId()
  {
    return "Address";
  }
  
  @Override
  public String getDescription()
  {
    // TODO: get provisional data
    if (isNew()) return "";
    return getDescription(getObjectId());
  }
  
  @Override
  public String getDescription(String objectId)
  {
    StringBuilder buffer = new StringBuilder();
    try
    {
      AddressFilter filter = new AddressFilter();
      filter.getAddressIdList().add(objectId);
      List<AddressView> addressViews = 
        KernelConfigBean.getPort().findAddressViews(filter);
      if (addressViews.size() > 0)
      {
        AddressView addressView = addressViews.get(0);
        buffer.append(addressView.getDescription()).append(" ");
        buffer.append(addressView.getCity()).append(" (");
        buffer.append(addressView.getCountry()).append(")");
        buffer.append(" (");
        buffer.append(objectId);
        buffer.append(")");
        return buffer.toString();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return objectId;
  }

  public List<SelectItem> getSelectItems(String addressId, String personId)
  {
    // returns address items + addresses from person identified by personId
    List<SelectItem> items = super.getSelectItems(addressId);
    if (personId != null && !personId.equals(ControllerBean.NEW_OBJECT_ID))
    {
      PersonAddressFilter filter = new PersonAddressFilter();
      filter.setPersonId(personId);
      List<PersonAddressView> personAddressViews =
        KernelConfigBean.getPort().findPersonAddressViews(filter);
      for (PersonAddressView personAddressView : personAddressViews)
      {
        AddressView addressView = personAddressView.getAddress();
        SelectItem selectItem = new SelectItem();
        selectItem.setValue(addressView.getAddressId());
        selectItem.setLabel(addressView.getDescription());
        selectItem.setDescription(addressView.getDescription());
        items.add(1, selectItem); // position 0 is blank
      }
    }
    return items;
  }

  @Override
  public String remove()
  {
    try
    {
      if (!isNew())
      {
        KernelConfigBean.getPort().removeAddress(getObjectId());
        removed();
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return getControllerBean().show();
  }
}
