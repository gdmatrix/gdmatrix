package org.santfeliu.kernel.web;

import java.util.List;

import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;

import org.matrix.kernel.Address;
import org.matrix.kernel.KernelConstants;


import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.TypifiedPageBean;


public class AddressMainBean extends TypifiedPageBean
{
  private Address address;
  private transient List<SelectItem> streetSelectItems;

  public AddressMainBean()
  {
    super(DictionaryConstants.ADDRESS_TYPE, KernelConstants.KERNEL_ADMIN_ROLE);
    load();
  }

  public void setAddress(Address address)
  {
    this.address = address;
  }

  public Address getAddress()
  {
    if (address == null) address = new Address();
    return address;
  }

  public String show()
  {
    return "address_main";
  }

  public String store()
  {
    try
    {
      if (ControllerBean.NEW_OBJECT_ID.equals(address.getStreetId()))
      {
        address.setStreetId(null);
      }
      address = KernelConfigBean.getPort().storeAddress(address);
      setObjectId(address.getAddressId());
    }
    catch (Exception ex)
    {
      error(ex);
    }    
    return show();
  }

  public boolean isModified()
  {
    return true;
  }

  public String searchStreet()
  {
    return getControllerBean().searchObject("Street",
      "#{addressMainBean.address.streetId}");
  }

  public String showStreet()
  {
    return getControllerBean().showObject("Street", address.getStreetId());
  }

  
  public List<SelectItem> getStreetSelectItems()
  {
    if (streetSelectItems == null)
    {
      StreetBean streetBean = (StreetBean)getBean("streetBean");
      return streetBean.getSelectItems(address.getStreetId());
    }
    return streetSelectItems;
  }

  public String showType()
  {
    return getControllerBean().showObject("Type",
      getAddress().getAddressTypeId());
  }

  public boolean isRenderShowTypeButton()
  {
    return getAddress().getAddressTypeId() != null &&
      getAddress().getAddressTypeId().trim().length() > 0;
  }
  
  private void load()
  {
    if (isNew())
    {
      initAddress();
    }
    else
    {
      try
      {
        this.address = KernelConfigBean.getPort().loadAddress(getObjectId());
      } // object was concurrently removed?
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        error(ex); 
        initAddress();
      }
    }
  }
  
  private void initAddress()
  {
    this.address = new Address();
  }
}
