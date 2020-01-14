package org.santfeliu.kernel.web;

import java.text.SimpleDateFormat;

import java.util.List;

import java.util.ResourceBundle;

import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.matrix.kernel.KernelConstants;

import org.matrix.kernel.KernelList;
import org.matrix.kernel.Person;
import org.matrix.kernel.Sex;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;

import org.santfeliu.faces.FacesUtils;
import org.santfeliu.web.obj.ControllerBean;
import org.santfeliu.web.obj.TypifiedPageBean;


public class PersonMainBean extends TypifiedPageBean
{
  private Person person;
  private transient List<SelectItem> personParticleSelectItems;
  private transient List<SelectItem> countrySelectItems;
  private transient List<SelectItem> citySelectItems;

  public PersonMainBean()
  {
    super(DictionaryConstants.PERSON_TYPE, KernelConstants.KERNEL_ADMIN_ROLE);
    load();
  }

  public void setPerson(Person person)
  {
    this.person = person;
  }

  public Person getPerson()
  {
    return person;
  }

  public int getAge()
  {
    int age = 0;
    String birthDate = person.getBirthDate();
    if (birthDate != null)
    {
      SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
      String todayDate = df.format(new java.util.Date());
      int birthYear = Integer.parseInt(birthDate.substring(0, 4));
      int currentYear = Integer.parseInt(todayDate.substring(0, 4));
      age = currentYear - birthYear;
      if (todayDate.substring(4).compareTo(birthDate.substring(4)) < 0)
      {
        age--;
      }
    }
    return age < 0 ? 0 : age;
  }

  public String show()
  {
    try
    {
      postShow();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    finally
    {
      return "person_main";
    }
  }
  
  @Override
  public void preShow()
  {
    try
    {     
      postShow();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public String store()
  {
    try
    {
      if (ControllerBean.NEW_OBJECT_ID.equals(person.getBirthCityId()))
      {
        person.setBirthCityId(null);
      }
      if (ControllerBean.NEW_OBJECT_ID.equals(person.getNationalityId()))
      {
        person.setNationalityId(null);
      }
      person = KernelConfigBean.getPort().storePerson(person);
      setObjectId(person.getPersonId());
    }
    catch (Exception ex)
    {
      error(ex);
    }    
    return show();
  }

  public String searchBirthCity()
  {
    return getControllerBean().searchObject("City",
      "#{personMainBean.person.birthCityId}");
  }

  public String showBirthCity()
  {
    if (person.getBirthCityId() == null) return null;
    return getControllerBean().showObject("City", person.getBirthCityId());
  }

  public String searchNationality()
  {
    return getControllerBean().searchObject("Country",
      "#{personMainBean.person.nationalityId}");
  }

  public String showNationality()
  {
    if (person.getNationalityId() == null) return null;
    return getControllerBean().showObject("Country",
      person.getNationalityId());
  }

  public boolean isModified()
  {
    return true;
  }
  
//  public SelectItem[] getPersonTypeSelectItems()
//  {
//    ResourceBundle bundle = ResourceBundle.getBundle(
//      "org.santfeliu.kernel.web.resources.KernelBundle", getLocale());
//    return FacesUtils.getEnumSelectItems(PersonType.class, bundle);
//  }

  public SelectItem[] getSexSelectItems()
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
     "org.santfeliu.kernel.web.resources.KernelBundle", getLocale());    
    return FacesUtils.getEnumSelectItems(Sex.class, bundle);
  }

  public List<SelectItem> getPersonParticleSelectItems()
  {
    if (personParticleSelectItems == null)
    {
      try
      {
        personParticleSelectItems = FacesUtils.getListSelectItems(
          KernelConfigBean.getPort().listKernelListItems(
          KernelList.PERSON_PARTICLE),
          "itemId", "label", true);
      }
      catch (Exception ex)
      {
        error(ex);
      }
    }
    return personParticleSelectItems;
  }

  public List<SelectItem> getNationalitySelectItems()
  {
    if (countrySelectItems == null)
    {
      CountryBean countryBean = (CountryBean)getBean("countryBean");
      countrySelectItems = countryBean.getSelectItems(person.getNationalityId());
    }
    return countrySelectItems;
  }

  public List<SelectItem> getCitySelectItems()
  {
    if (citySelectItems == null)
    {
      CityBean cityBean = (CityBean)getBean("cityBean");
      citySelectItems = cityBean.getSelectItems(person.getBirthCityId());
    }

    return citySelectItems;
  }

  public boolean isJuristicPerson()
  {
    if (person == null) return true;
    else 
    {
      String personTypeId = person.getPersonTypeId();
      if (personTypeId != null && personTypeId.trim().length() > 0)
      {
        Type type = TypeCache.getInstance().getType(personTypeId);
        if (type == null)
          return false;
      
        PropertyDefinition pd = type.getPropertyDefinition("juristic");
        return (pd != null && "true".equals(pd.getValue().get(0)));
      }
      else
        return false;
    }
  }

  public String showType()
  {
    return getControllerBean().showObject("Type",
      getPerson().getPersonTypeId());
  }

  public boolean isRenderShowTypeButton()
  {
    return getPerson().getPersonTypeId() != null &&
      getPerson().getPersonTypeId().trim().length() > 0;
  }  

  private void load()
  {
    if (isNew())
    {
      initPerson();
    }
    else
    {
      try
      {
        this.person = KernelConfigBean.getPort().loadPerson(getObjectId());
      }
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        error(ex);
        initPerson();
      }
    }
  }

  private void initPerson()
  {
    this.person = new Person();
    person.setSex(Sex.NONE);
  }
}
