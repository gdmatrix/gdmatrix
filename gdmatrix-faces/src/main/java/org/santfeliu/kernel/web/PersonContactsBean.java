package org.santfeliu.kernel.web;

import java.util.List;

import org.matrix.dic.DictionaryConstants;

import org.matrix.kernel.Contact;
import org.matrix.kernel.ContactFilter;
import org.matrix.kernel.ContactView;
import org.matrix.kernel.KernelConstants;

import org.santfeliu.web.obj.TypifiedPageBean;


public class PersonContactsBean extends TypifiedPageBean
{
  private ContactView editingRow;
  private List<ContactView> rows;
//  private transient List<SelectItem> contactTypeSelectItems;

  public PersonContactsBean()
  {
    super(DictionaryConstants.CONTACT_TYPE, KernelConstants.KERNEL_ADMIN_ROLE);
    load();
  }

  public void setRows(List<ContactView> rows)
  {
    this.rows = rows;
  }

  public List<ContactView> getRows()
  {
    return rows;
  }

//  public List<SelectItem> getContactTypeSelectItems()
//  {
//    if (contactTypeSelectItems == null)
//    {
//      try
//      {
//        contactTypeSelectItems = FacesUtils.getListSelectItems(
//          KernelConfigBean.getPort().listKernelListItems(
//          KernelList.CONTACT_TYPE),
//          "itemId", "label", false);
//      }
//      catch (Exception ex)
//      {
//        error(ex);
//      }
//    }
//    return contactTypeSelectItems;
//  }

  public boolean isModified()
  {
    return true;
  }

  public String show()
  {
    return "person_contacts";
  }

  public String store()
  {
    if (editingRow != null)
    {
      storeContact();
    }
    else
    {
      load();
    }
    return show();
  }
  
  public ContactView getEditingRow()
  {
    return editingRow;
  }
  
  public String addContact()
  {
    ContactView contactView = new ContactView();
    rows.add(contactView); // empty row
    editingRow = contactView;
    return null;
  }
  
  public String editContact()
  {
    editingRow = (ContactView)getExternalContext().
      getRequestMap().get("row");

    return null;
  }

  public String removeContact()
  {
    try
    {
      ContactView row = (ContactView)getExternalContext().
        getRequestMap().get("row");
      KernelConfigBean.getPort().removeContact(row.getContactId());
      load();
      return null;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String storeContact()
  {
    try
    {
      Contact contact = new Contact();
      contact.setContactId(editingRow.getContactId());
      contact.setPersonId(getObjectId());
      contact.setContactTypeId(editingRow.getContactTypeId());
      contact.setValue(editingRow.getValue());
      contact.setComments(editingRow.getComments());
      KernelConfigBean.getPort().storeContact(contact);
      editingRow = null;
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String cancelContact()
  {
    editingRow = null;
    load();
    return null;
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
        ContactFilter filter = new ContactFilter();
        filter.setPersonId(getObjectId());

        rows = KernelConfigBean.getPort().findContactViews(filter);
        //ContactView contactView = new ContactView();
        //rows.add(contactView); // empty row
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }  
}
