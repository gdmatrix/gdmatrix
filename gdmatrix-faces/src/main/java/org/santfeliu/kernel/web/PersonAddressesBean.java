package org.santfeliu.kernel.web;

import java.util.List;

import javax.faces.model.SelectItem;

import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.PersonAddress;
import org.matrix.kernel.PersonAddressFilter;
import org.matrix.kernel.PersonAddressView;

import org.santfeliu.web.obj.PageBean;


public class PersonAddressesBean extends PageBean
{
  private String addressId;
  private List<PersonAddressView> rows;

  public PersonAddressesBean()
  {
    load();
  }

  public void setAddressId(String addressId)
  {
    this.addressId = addressId;
  }

  public String getAddressId()
  {
    return addressId;
  }

  public void setRows(List<PersonAddressView> rows)
  {
    this.rows = rows;
  }

  public List<PersonAddressView> getRows()
  {
    return rows;
  }

  public boolean isModified()
  {
    return false;
  }
  
  public String show()
  {
    return "person_addresses";
  } 
  
  public String store()
  {
    return show();
  }

  public String showAddress()
  {
    return getControllerBean().showObject("Address",
      (String)getValue("#{row.address.addressId}"));
  }

  public String searchAddress()
  {
    return getControllerBean().searchObject("Address",
      "#{personAddressesBean.addressId}");
  }

  public String removeAddress()
  {
    try
    {
      PersonAddressView row = (PersonAddressView)getRequestMap().get("row");
      KernelManagerPort port = KernelConfigBean.getPort();
      port.removePersonAddress(row.getPersonAddressId());
      load();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      error(ex);
    }
    return null;
  }

  public String addAddress()
  {
    try
    {
      String personId = getObjectId();
      KernelManagerPort port = KernelConfigBean.getPort();
      PersonAddress personAddress = new PersonAddress();
      personAddress.setPersonId(personId);
      personAddress.setAddressId(addressId);
      port.storePersonAddress(personAddress);
      this.addressId = null;
      load();      
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      error(ex);
    }
    return null;
  }

  public List<SelectItem> getAddressSelectItems()
  {
    AddressBean addressBean = (AddressBean)getBean("addressBean");
    return addressBean.getSelectItems(addressId);
  }

  public int getRowCount()
  {
    return (getRows() == null ? 0 : getRows().size());
  }

  private void load()
  {
    try
    {
      if (!isNew())
      {
        PersonAddressFilter filter = new PersonAddressFilter();
        filter.setPersonId(getObjectId());
        rows = KernelConfigBean.getPort().findPersonAddressViews(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
}
