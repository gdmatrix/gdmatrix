/*
 * GDMatrix
 *  
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *  
 * This program is licensed and may be used, modified and redistributed under 
 * the terms of the European Public License (EUPL), either version 1.1 or (at 
 * your option) any later version as soon as they are approved by the European 
 * Commission.
 *  
 * Alternatively, you may redistribute and/or modify this program under the 
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either  version 3 of the License, or (at your option) 
 * any later version. 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *    
 * See the licenses for the specific language governing permissions, limitations 
 * and more details.
 *    
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along 
 * with this program; if not, you may find them at: 
 *    
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/ 
 * and 
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.workflow.web;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.matrix.kernel.City;
import org.matrix.kernel.Country;
import org.matrix.kernel.CountryFilter;
import org.matrix.kernel.KernelManagerPort;
import org.matrix.kernel.KernelManagerService;
import org.matrix.kernel.PersonFilter;
import org.matrix.kernel.Person;
import org.matrix.kernel.Sex;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.util.Properties;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.workflow.form.Form;
import org.santfeliu.workflow.form.PersonForm;

/**
 *
 * @author realor
 * @author blanquepa
 */
public class PersonFormBean extends FormBean
{
  public static final String INVALID_NIF = "INVALID_NIF";
  public static final String INVALID_NIE = "INVALID_NIE";
  public static final String INVALID_PASSPORT = "INVALID_PASSPORT";

  public static final String EXTERNAL_SCOPE = "external";
  public static final String INTERNAL_SCOPE = "internal";
  public static final int DEFAULT_MAX_RESULTS = 10;

  public static final String NIF_NIE_IDENTITY = "nif";
  public static final String PASSPORT_IDENTITY = "passport";
  public static final String NO_IDENTITY = "none";

  private String message;
  private String prefix;
  private String personType = "F"; // F: phisical, J: juristic
  private String scope = INTERNAL_SCOPE;
  private boolean identityRequired = true;
  private boolean personRequired = true;
  private boolean creationAllowed = false;
  private String creationPersonTypeId;
  private int maxResults = DEFAULT_MAX_RESULTS;

  private String personId;
  private String name;
  private String surname1;
  private String surname2;
  private Sex sex;
  private String NIF;
  private String passport;
  private String nationalityId; // countryId
  private String CIF;
  private String phone;
  private String email;
  private String birthDate;
  private String identityType = NIF_NIE_IDENTITY;
  private Number selectedRowIndex;

  private List<Option> rows;
  private Option selected;
  private String personIdInfo;
  private List<Country> countries;

  private boolean rowParity;

  public enum OptionType
  {
    NORMAL,
    NEW,
    NONE
  }

  @Override
  public String show(Form form)
  {
    Properties parameters = form.getParameters();
    Object value;
    value = parameters.get("prefix");
    if (value != null) prefix = String.valueOf(value);
    value = parameters.get("message");
    if (value != null) message = String.valueOf(value);
    value = parameters.get("personType");
    if (value != null) personType = String.valueOf(value);
    value = parameters.get("scope");
    if (value != null) scope = String.valueOf(value);
    value = parameters.get("identityRequired");
    if (value != null) identityRequired = Boolean.parseBoolean((String)value);
    value = parameters.get("personRequired");
    if (value != null) personRequired = Boolean.parseBoolean((String)value);
    value = parameters.get("creationAllowed");
    if (value != null) creationAllowed = Boolean.parseBoolean((String)value);
    value = parameters.get("creationPersonTypeId");
    if (value != null) creationPersonTypeId = String.valueOf(value);
    value = parameters.get("maxResults");
    if (value != null) maxResults = Integer.valueOf((String)value);

    loadPreviousVariables();
    loadUserSessionBeanValues();

    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    instanceBean.setForwardEnabled(false);

    return "person_form";
  }

  public String searchPerson()
  {
    rows = new ArrayList<Option>();

    if (EXTERNAL_SCOPE.equals(scope))
    {
      externalSearchPersons();
      selected = null;

      if (getOptions().isEmpty())
        return null;
      else
      {
        Option row = rows.get(0);
        if (row.identified)
        {
          selected = row;
          InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
          instanceBean.setForwardEnabled(true);
          return instanceBean.forward();
        }
      }
    }
    else
    {
      internalSearchPersons();
    }

    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    instanceBean.setForwardEnabled(!getOptions().isEmpty());

    return null;
  }

  @Override
  public Map submit()
  {
    HashMap variables = new HashMap();
    String varPrefix = "";

    if (prefix != null)
      varPrefix = this.prefix + "_";

    if (selected != null || selectedRowIndex != null)
    {
      //Get option selected by user
      if (selected == null)
        selected = getOptions().get(selectedRowIndex.intValue());

      if (selected != null
        && selected.getOptionType() == OptionType.NORMAL)
      {
        Person person = selected.getPerson();
        personId = person.getPersonId();
        name = person.getName();
        surname1 = (!StringUtils.isBlank(person.getFirstParticle()) ?
          person.getFirstParticle() + " " : "") + person.getFirstSurname();
        surname2 = (!StringUtils.isBlank(person.getSecondParticle()) ?
          person.getSecondParticle() + " " : "") + person.getSecondSurname();
        sex = person.getSex();
        NIF = person.getNif();
        passport = person.getPassport();
        nationalityId = person.getNationalityId();
      }
      else if (selected != null
        && selected.getOptionType() == OptionType.NEW
        && creationPersonTypeId != null)
      {
        Person person = new Person();
        person.setPersonTypeId(creationPersonTypeId);
        person.setName(name);
        person.setFirstSurname(surname1);
        person.setSecondSurname(surname2);
        person.setSex(sex);
        person.setNif(NIF);
        person.setPassport(passport);
        person.setNationalityId(nationalityId);
        person = getKernelManagerPort().storePerson(person);
        personId = person.getPersonId();
      }
    }

    variables.put(varPrefix + PersonForm.PERSON_ID, personId);
    variables.put(varPrefix + PersonForm.NAME, name);
    variables.put(varPrefix + PersonForm.SURNAME1, surname1);
    variables.put(varPrefix + PersonForm.SURNAME2, surname2);
    String sex = (Sex.MALE == this.sex ? "M" : (Sex.FEMALE == this.sex ? "F" : ""));
    variables.put(varPrefix + PersonForm.SEX, sex);
    variables.put(varPrefix + PersonForm.NIF, NIF);
    variables.put(varPrefix + PersonForm.PASSPORT, passport);
    variables.put(varPrefix + PersonForm.NATIONALITY_ID, nationalityId);
    variables.put(varPrefix + PersonForm.CIF, CIF);
    variables.put(varPrefix + PersonForm.PHONE, phone);
    variables.put(varPrefix + PersonForm.EMAIL, email);
    return variables;
  }

  public String showPerson()
  {
    Option row = (Option)getValue("#{row}");
    if (row != null)
    {
      if (row.showInfo)
        row.showInfo = false;
      else
      {
        personIdInfo = row.getPerson().getPersonId();
        Person person = getKernelManagerPort().loadPerson(personIdInfo);
        row.person = person;
        row.showInfo = true;
      }
    }
    return null;
  }

  public String getPersonNationality()
  {
    if (countries != null && !countries.isEmpty())
    {
      Option row = (Option)getValue("#{row}");
      if (row != null)
      {
        for (Country country : countries)
        {
          if (country.getCountryId().equals(row.getPerson().getNationalityId()))
            return country.getName();
        }
        return row.getPerson().getNationalityId();
      }
    }
    return "";

  }

  public String getPersonBirthCity()
  {
    Option row = (Option)getValue("#{row}");
    if (row != null)
    {
      String cityId = row.getPerson().getBirthCityId();
      if (cityId != null)
      {
        City city = getKernelManagerPort().loadCity(cityId);
        if (city != null)
          return city.getName();
        else
          return cityId;
      }
    }

    return "";
  }

  public List<Option> getOptions()
  {
    List result = new ArrayList();
    if (rows != null)
    {
      for (Option row : rows)
      {
        if (EXTERNAL_SCOPE.equals(scope))
        {
          if (row.isIdentified()
            ||(row.getScore() >= 3 && row.getPercent() >= 85)
            )
          {
            result.add(row);
          }
        }
        else
          result.add(row);
      }
    }

    if (!isPersonIdRequired() && rows != null)
    {
      Option row = new Option("Continuar sense seleccionar cap persona.", OptionType.NONE);
      result.add(row);
    }

    if (isCreationAllowed() && rows != null 
      && (isIdentityRequired() && !(StringUtils.isBlank(NIF) && StringUtils.isBlank(passport))
          || !isIdentityRequired()) && creationPersonTypeId != null)
    {
      Option row = new Option("Crear la persona agafant les dades del formulari. Comproveu que les dades entrades son les de la persona a crear.", OptionType.NEW);
      result.add(row);
    }

    return result;
  }

  public List<Option> getRows()
  {
    return rows;
  }

  public void setRows(List<Option> rows)
  {
    this.rows = rows;
  }

  public String getMessage()
  {
    return message;
  }

  public void setMessage(String message)
  {
    this.message = message;
  }

  public String getPrefix()
  {
    return prefix;
  }

  public void setPrefix(String prefix)
  {
    this.prefix = prefix;
  }

  public String getPersonType()
  {
    return personType;
  }

  public void setPersonType(String personType)
  {
    this.personType = personType;
  }

  public String getScope()
  {
    return scope;
  }

  public void setScope(String scope)
  {
    this.scope = scope;
  }

  public String getPersonId()
  {
    return personId;
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getSurname1()
  {
    return surname1;
  }

  public void setSurname1(String surname1)
  {
    this.surname1 = surname1;
  }

  public String getSurname2()
  {
    return surname2;
  }

  public void setSurname2(String surname2)
  {
    this.surname2 = surname2;
  }

  public Sex getSex()
  {
    return sex;
  }

  public void setSex(Sex sex)
  {
    this.sex = sex;
  }

  public String getNIF()
  {
    return NIF;
  }

  public void setNIF(String nif)
  {
    this.NIF = nif;
  }

  public String getPassport()
  {
    return passport;
  }

  public void setPassport(String passport)
  {
    this.passport = passport;
  }

  public String getNationalityId()
  {
    return nationalityId;
  }

  public void setNationalityId(String nationalityId)
  {
    this.nationalityId = nationalityId;
  }

  public String getCIF()
  {
    return CIF;
  }

  public void setCIF(String cif)
  {
    this.CIF = cif;
  }

  public String getPhone()
  {
    return phone;
  }

  public void setPhone(String phone)
  {
    this.phone = phone;
  }

  public String getEmail()
  {
    return email;
  }

  public void setEmail(String email)
  {
    this.email = email;
  }

  public String getBirthDate()
  {
    return birthDate;
  }

  public void setBirthDate(String birthDate)
  {
    this.birthDate = birthDate;
  }

  public String getIdentityType()
  {
    return identityType;
  }

  public void setIdentityType(String identityType)
  {
    this.identityType = identityType;
  }

  public boolean isIdentityRequired()
  {
    return identityRequired;
  }

  public void setIdentityRequired(boolean identityRequired)
  {
    this.identityRequired = identityRequired;
  }

  public boolean isCreationAllowed()
  {
    return creationAllowed;
  }

  public void setCreationAllowed(boolean creationAllowed)
  {
    this.creationAllowed = creationAllowed;
  }

  public boolean isPersonIdRequired()
  {
    return personRequired;
  }

  public void setPersonIdRequired(boolean personIdRequired)
  {
    this.personRequired = personIdRequired;
  }

  public List<SelectItem> getPersonSelectItems()
  {
    List<SelectItem> personSelectItems = new ArrayList<SelectItem>();
    if (rows != null)
    {
      for (Option row : rows)
      {
        SelectItem item = new SelectItem();
        item.setValue(row.person.getPersonId());
        item.setLabel(row.toString());
        personSelectItems.add(item);
      }
    }
    return personSelectItems;
  }

  public Number getSelectedRowIndex()
  {
    return selectedRowIndex;
  }

  public void setSelectedRowIndex(Number selectedRowIndex)
  {
    this.selectedRowIndex = selectedRowIndex;
  }

  public String getSelectItemClass()
  {
    rowParity = !rowParity;
    return rowParity ? "row1" : "row2";
  }

  public List<SelectItem> getCountrySelectItems()
  {
    ArrayList selectItems = new ArrayList<SelectItem>();
    try
    {
      KernelManagerPort port = getKernelManagerPort();
      CountryFilter filter = new CountryFilter();
      countries = port.findCountries(filter);
      for (Country country : countries)
      {
        SelectItem selectItem = new SelectItem();
        selectItem.setLabel(country.getName());
        selectItem.setValue(country.getCountryId());
        selectItems.add(selectItem);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return selectItems;
  }

  public SelectItem[] getSexSelectItems()
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
     "org.santfeliu.kernel.web.resources.KernelBundle", getLocale());
    return FacesUtils.getEnumSelectItems(Sex.class, bundle);
  }

  private void externalSearchPersons()
  {
    KernelManagerPort port = getKernelManagerPort();
    List<Person> persons = new ArrayList();
    PersonFilter filter = new PersonFilter();
    if (!StringUtils.isBlank(NIF) || !StringUtils.isBlank(passport)
      || !StringUtils.isBlank(CIF))
    {
      if (!StringUtils.isBlank(NIF))
        filter.setNif(NIF);
      else if (!StringUtils.isBlank(passport))
        filter.setPassport(passport);
      else if (!StringUtils.isBlank(CIF))
        filter.setNif(CIF);
      filter.setFirstResult(0);
      filter.setMaxResults(10);
      persons = port.findPersons(filter);
    }

    if (persons.size() >= 1)
      copyToRows(persons);
    else
    {
      filter = new PersonFilter();
      filter.setFirstSurname(surname1);
      filter.setSecondSurname(surname2);
      persons = port.findPersons(filter);
      copyToRows(persons);
    }
  }

  private void internalSearchPersons()
  {
    KernelManagerPort port = getKernelManagerPort();
    List<Person> persons = new ArrayList();
    PersonFilter filter = new PersonFilter();
    if (!StringUtils.isBlank(NIF))
      filter.setNif(NIF);
    else if (!StringUtils.isBlank(passport))
      filter.setPassport(passport);
    else if (!StringUtils.isBlank(CIF))
      filter.setNif(CIF);
    filter.setFirstSurname(surname1);
    filter.setSecondSurname(surname2);
    if (StringUtils.isBlank(surname1) && StringUtils.isBlank(NIF)
      && StringUtils.isBlank(passport))
      filter.setName(name);
    persons = port.findPersons(filter);

    copyToRows(persons);
  }

  private void copyToRows(List<Person> persons)
  {
    for (Person person : persons)
    {
      Option row = new Option(person);
      row.score = 0;
      row.counter = 0;

      if (!StringUtils.isBlank(NIF) && NIF.trim().equalsIgnoreCase(person.getNif()) ||
        !StringUtils.isBlank(passport) && passport.trim().equalsIgnoreCase(person.getPassport()))
      {
        row.score = 1;
        row.counter = 1;
        row.identified = true;
      }
      addScore(row, name, person.getName(), false);
      addScore(row, surname1, person.getFirstSurname(), false);
      addScore(row, surname2, person.getSecondSurname(), false);
      addScore(row, birthDate, person.getBirthDate(), true);

      rows.add(row);
    }

    Collections.sort(rows, new Comparator() {
      public int compare(Object o1, Object o2)
      {
        Option p1 = (Option)o1;
        Option p2 = (Option)o2;

        if (!p1.identified && p2.identified)
          return -1;
        else if (p1.identified && !p2.identified)
          return 1;
        else
        {
          double result = (p2.score - p1.score) * 100;
          if (result == 0)
            return (int) (((p2.score / p2.counter) - (p1.score / p1.counter)) * 10000);
          else
            return (int) result;
        }
      }
    });

    if (rows.size() > maxResults)
      rows = new ArrayList(rows.subList(0, maxResults - 1));
  }

  private void addScore(Option row, String s1, String s2,
    boolean discrete)
  {
    if (!StringUtils.isBlank(s1) && !StringUtils.isBlank(s2))
    {
      row.counter++;
      s1 = s1.trim().toUpperCase();
      s2 = s2.trim().toUpperCase();

      if (s1.equals(s2))
        row.score++;
      else if (!discrete)
      {
        String shortest = s1.length() <= s2.length() ? s1 : s2;
        String longest = s1.length() <= s2.length() ? s2 : s1;
        int longestLength = longest.length();
        int shortestLength = shortest.length();

        char[] array = shortest.toCharArray();
        int match = 0;
        for (int i = 0; i < shortest.length(); i++)
        {
          char ch = array[i];
          int index = longest.indexOf(ch);
          if (index >= 0)
          {
            match++;
            longest = longest.substring(index + 1);
          }
        }
        row.score += (0.5 * ((double)match / (double)longestLength)
          + 0.5 * ((double)match / (double)shortestLength)) ;
      }
    }
  }

  private Option getRow(List<Option> rows, String personId)
  {
    if (rows != null && !rows.isEmpty())

    for (Option row : rows)
    {
      if (row.person.getPersonId().equals(personId))
        return row;
    }

    return null;
  }

  private void loadPreviousVariables()
  {
    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    Map variables = instanceBean.getVariables();

    String varPrefix = "";
    if (prefix != null)
      varPrefix = this.prefix + "_";

    personId = (String)variables.get(varPrefix + PersonForm.PERSON_ID);
    name = (String)variables.get(varPrefix + PersonForm.NAME);
    surname1 = (String)variables.get(varPrefix + PersonForm.SURNAME1);
    surname2 = (String)variables.get(varPrefix + PersonForm.SURNAME2);
    sex = (Sex)variables.get(varPrefix + PersonForm.SEX);
    NIF = (String)variables.get(varPrefix + PersonForm.NIF);
    passport = (String)variables.get(varPrefix + PersonForm.PASSPORT);
    nationalityId = (String)variables.get(varPrefix + PersonForm.NATIONALITY_ID);
    CIF = (String)variables.get(varPrefix + PersonForm.CIF);
    phone = (String)variables.get(varPrefix + PersonForm.PHONE);
    email = (String)variables.get(varPrefix + PersonForm.EMAIL);
  }

  private void loadUserSessionBeanValues()
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();

    if (name == null)
      name = userSessionBean.getGivenName();

    if (surname1 == null)
    {
      String surname = userSessionBean.getSurname();
      if (surname != null)
      {
        int idx = surname.trim().indexOf(" ");
        if (idx > -1)
        {
          surname1 = surname.substring(0, idx);
          surname2 = surname.substring(idx + 1, surname.length());
        }
        else
        {
          surname1 = surname;
        }
      }
    }

    if (NIF == null)
      NIF = userSessionBean.getNIF();
    if (CIF == null)
      CIF = userSessionBean.getCIF();
    if (email == null)
      email = userSessionBean.getEmail();
  }

  private KernelManagerPort getKernelManagerPort()
  {
    try
    {
      WSDirectory dir = WSDirectory.getInstance();
      WSEndpoint endpoint = dir.getEndpoint(KernelManagerService.class);
      return endpoint.getPort(KernelManagerPort.class);
    }
    catch (Exception ex)
    {
      throw new RuntimeException();
    }
  }

  public class Option implements Serializable
  {
    private Person person;
    private boolean identified;
    private double score;
    private double counter;
    private String label;
    private OptionType optionType;
    private boolean showInfo = false;

    Option(Person person)
    {
      this.person = person;
      this.score = 0;
      this.counter = 0;
      this.identified = false;
      this.optionType = OptionType.NORMAL;
    }

    Option(String description, OptionType OptionType)
    {
      this.label = description;
      this.optionType = OptionType;
    }

    public String getSex()
    {
      ResourceBundle bundle = ResourceBundle.getBundle(
       "org.santfeliu.kernel.web.resources.KernelBundle", getLocale());
      if (person.getSex() != null)
        return bundle.getString(person.getSex().getClass().getName() + "." + person.getSex());
      else
        return "org.matrix.kernel.Sex.NONE";
    }

    public double getPercent()
    {
      return (score / counter) * 100;
    }

    public double getCounter()
    {
      return counter;
    }

    public void setCounter(double counter)
    {
      this.counter = counter;
    }

    public boolean isIdentified()
    {
      return identified;
    }

    public void setIdentified(boolean identified)
    {
      this.identified = identified;
    }

    public Person getPerson()
    {
      return person;
    }

    public void setPerson(Person person)
    {
      this.person = person;
    }

    public double getScore()
    {
      return score;
    }

    public void setScore(double score)
    {
      this.score = score;
    }

    public String getLabel()
    {
      return label;
    }

    public void setLabel(String label)
    {
      this.label = label;
    }

    public OptionType getOptionType()
    {
      return optionType;
    }

    public void setOptionType(OptionType optionType)
    {
      this.optionType = optionType;
    }

    public boolean isShowInfo()
    {
      return showInfo;
    }

    public void setShowInfo(boolean showInfo)
    {
      this.showInfo = showInfo;
    }

    private String getFullName()
    {
      String fullName = person.getName();
      if (person.getFirstParticle() != null)
      {
        fullName += " " + person.getFirstParticle();
      }
      if (person.getFirstSurname() != null)
      {
        fullName += " " + person.getFirstSurname();
      }
      if (person.getSecondParticle() != null)
      {
        fullName += " " + person.getSecondParticle();
      }
      if (person.getSecondSurname() != null)
      {
        fullName += " " + person.getSecondSurname();
      }
      if (person.getNif() != null && INTERNAL_SCOPE.equals(scope))
      {
        fullName += " (" + person.getNif() + ")";
      }
      else if (person.getPassport() != null && INTERNAL_SCOPE.equals(scope))
      {
        fullName += " (" + person.getPassport() + ")";
      }
      return fullName;
    }

    public String getFormattedScore()
    {
      if (personType != null && "F".equals(personType))
      {
        return new DecimalFormat("##0.00").format(score) + "/" +
          new DecimalFormat("##0.00").format(counter) +
          " (" + new DecimalFormat("##0.00%").format(score/counter) + ")";
      }
      else
        return "";
    }

    @Override
    public String toString()
    {
      if (person == null)
        return label;

      if (INTERNAL_SCOPE.equals(scope))
        return getFullName() + " " + getFormattedScore() + (identified ? "*" : "");
      else
        return getFullName();
    }
  }
}

