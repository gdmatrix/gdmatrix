package org.santfeliu.kernel.web;

import java.util.List;

import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;
import org.matrix.kernel.KernelConstants;

import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.PersonRepresentant;
import org.matrix.kernel.PersonRepresentantFilter;
import org.matrix.kernel.PersonRepresentantView;

import org.santfeliu.web.obj.TypifiedPageBean;


public class PersonRepresentantsBean extends TypifiedPageBean
{
  private PersonRepresentant editingRepresentant;
  private List<PersonRepresentantView> rows;
//  private transient List<SelectItem> representationTypeSelectItems;

  public PersonRepresentantsBean()
  {
    super(DictionaryConstants.PERSON_REPRESENTANT_TYPE,
      KernelConstants.KERNEL_ADMIN_ROLE);
    load();
  }

  public void setEditingRepresentant(PersonRepresentant editingRepresentant)
  {
    this.editingRepresentant = editingRepresentant;
  }

  public PersonRepresentant getEditingRepresentant()
  {
    return editingRepresentant;
  }

  public void setRows(List<PersonRepresentantView> rows)
  {
    this.rows = rows;
  }

  public List<PersonRepresentantView> getRows()
  {
    return rows;
  }

  public boolean isModified()
  {
    return editingRepresentant != null;
  }
  
  public String show()
  {
    return "person_representants";
  }

  public String store()
  {
    if (editingRepresentant != null)
    {
      storeRepresentant();
    }
    else
    {
      load();
    }
    return show();
  }

  public String showRepresentant()
  {
    return getControllerBean().showObject("Person",
      (String)getValue("#{row.representant.personId}"));
  }

  public String removeRepresentant()
  {
    try
    {
      PersonRepresentantView row = 
        (PersonRepresentantView)getRequestMap().get("row");
      KernelManagerPort port = KernelConfigBean.getPort();
      port.removePersonRepresentant(row.getPersonRepresentantId());
      load();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      error(ex);
    }
    return null;
  }

  public String editRepresentant()
  {
    try
    {
      PersonRepresentantView row = 
        (PersonRepresentantView)getRequestMap().get("row");

      KernelManagerPort port = KernelConfigBean.getPort();
      editingRepresentant = 
        port.loadPersonRepresentant(row.getPersonRepresentantId());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      error(ex);
    }
    return null;
  }
  
  public String addRepresentant()
  {
    editingRepresentant = new PersonRepresentant();
    editingRepresentant.setPersonId(getObjectId());
    return null;
  }

  public String searchRepresentant()
  {
    return getControllerBean().searchObject("Person",
      "#{personRepresentantsBean.editingRepresentant.representantId}");
  }

  public String storeRepresentant()
  {
    try
    {
      KernelConfigBean.getPort().storePersonRepresentant(editingRepresentant);
      editingRepresentant = null;
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String cancelRepresentant()
  {
    editingRepresentant = null;
    load();
    return null;
  }

  public List<SelectItem> getRepresentantSelectItems()
  {
    PersonBean personBean = (PersonBean)getBean("personBean");
    return personBean.getSelectItems(editingRepresentant.getRepresentantId());
  }
  
//  public List<SelectItem> getRepresentationTypeSelectItems()
//  {
//    if (representationTypeSelectItems == null)
//    {
//      try
//      {
//        representationTypeSelectItems = FacesUtils.getListSelectItems(
//          KernelConfigBean.getPort().listKernelListItems(
//          KernelList.REPRESENTATION_TYPE),
//          "itemId", "label", true);
//      }
//      catch (Exception ex)
//      {
//        error(ex);
//      }
//    }
//    return representationTypeSelectItems;
//  }

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
        PersonRepresentantFilter filter = new PersonRepresentantFilter();
        filter.setPersonId(getObjectId());
        rows = KernelConfigBean.getPort().findPersonRepresentantViews(filter);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
}
