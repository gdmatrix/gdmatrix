package org.santfeliu.kernel.web;

import java.util.List;

import javax.faces.model.SelectItem;

import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.PersonAddress;
import org.matrix.kernel.PersonAddressFilter;
import org.matrix.kernel.PersonAddressView;

import org.santfeliu.web.obj.PageBean;


public class AddressPersonsBean extends PageBean
{
  private String personId;
  private List<PersonAddressView> rows;

  public AddressPersonsBean()
  {
    load();
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public String getPersonId()
  {
    return personId;
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
    return "address_persons";
  }

  public String store()
  {
    return show();
  }

  public String showPerson()
  {
    return getControllerBean().showObject("Person",
      (String)getValue("#{row.person.personId}"));
  }

  public String searchPerson()
  {
    return getControllerBean().searchObject("Person",
      "#{addressPersonsBean.personId}");
  }

  public String removePerson()
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
      error(ex);
    }
    return null;
  }

  public String addPerson()
  {
    try
    {
      String addressId = getObjectId();
      KernelManagerPort port = KernelConfigBean.getPort();
      PersonAddress personAddress = new PersonAddress();
      personAddress.setPersonId(personId);
      personAddress.setAddressId(addressId);
      port.storePersonAddress(personAddress);
      this.personId = null;
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public List<SelectItem> getPersonSelectItems()
  {
    PersonBean personBean = (PersonBean)getBean("personBean");
    return personBean.getSelectItems(personId);
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
        filter.setAddressId(getObjectId());
        rows = KernelConfigBean.getPort().findPersonAddressViews(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
}
