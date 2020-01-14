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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlSelectOneListbox;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.santfeliu.util.Properties;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.workflow.form.Form;
import org.santfeliu.workflow.form.IdentificationForm;


/**
 *
 * @author unknown
 */
public class IdentificationFormBean extends FormBean implements Serializable
{
  private String message;
  private String prefix;
  
  private String name;
  private String surname1;
  private String surname2;
  
  private String documentType;
  private String documentNumber;
  
  private String wayType;
  private String addressName;
  private String addressNumber;
  private String addressBlock;  
  private String addressStair; 
  private String addressFloor; 
  private String addressDoor;  
  private String zipCode;  
  private String city;
  private String province;  
  
  private String phone;  
  private String email;  
  
  private transient HtmlSelectOneListbox documentTypeComponent;
  public static final String INVALID_NIF = "INVALID_NIF";
  public static final String INVALID_NIE = "INVALID_NIE";  
  public static final String INVALID_PASSPORT = "INVALID_PASSPORT";  
  
  public IdentificationFormBean()
  {
    if (documentTypeComponent == null) 
      documentTypeComponent = new HtmlSelectOneListbox();
  }
  
  public void setMessage(String message)
  {
    this.message = message;
  }

  public String getMessage()
  {
    return message;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public void setSurname1(String surname1)
  {
    this.surname1 = surname1;
  }

  public String getSurname1()
  {
    return surname1;
  }

  public void setSurname2(String surname2)
  {
    this.surname2 = surname2;
  }

  public String getSurname2()
  {
    return surname2;
  }

  public void setDocumentNumber(String id)
  {
    this.documentNumber = id;
  }

  public String getDocumentNumber()
  {
    return documentNumber;
  }

  public void setDocumentType(String documentType)
  {
    this.documentType = documentType;
  }

  public String getDocumentType()
  {
    return documentType;
  }

  public void setWayType(String wayType)
  {
    this.wayType = wayType;
  }

  public String getWayType()
  {
    return wayType;
  }

  public void setAddressName(String addressName)
  {
    this.addressName = addressName;
  }

  public String getAddressName()
  {
    return addressName;
  }

  public void setAddressNumber(String addressNumber)
  {
    this.addressNumber = addressNumber;
  }

  public String getAddressNumber()
  {
    return addressNumber;
  }

  public void setAddressBlock(String addressBlock)
  {
    this.addressBlock = addressBlock;
  }

  public String getAddressBlock()
  {
    return addressBlock;
  }

  public void setAddressStair(String addressStair)
  {
    this.addressStair = addressStair;
  }

  public String getAddressStair()
  {
    return addressStair;
  }

  public void setAddressFloor(String addressFloor)
  {
    this.addressFloor = addressFloor;
  }

  public String getAddressFloor()
  {
    return addressFloor;
  }

  public void setAddressDoor(String addressDoor)
  {
    this.addressDoor = addressDoor;
  }

  public String getAddressDoor()
  {
    return addressDoor;
  }

  public void setZipCode(String zipCode)
  {
    this.zipCode = zipCode;
  }

  public String getZipCode()
  {
    return zipCode;
  }

  public void setCity(String city)
  {
    this.city = city;
  }

  public String getCity()
  {
    return city;
  }

  public void setProvince(String province)
  {
    this.province = province;
  }

  public String getProvince()
  {
    return province;
  }

  public void setPhone(String phone)
  {
    this.phone = phone;
  }

  public String getPhone()
  {
    return phone;
  }

  public void setEmail(String email)
  {
    this.email = email;
  }

  public String getEmail()
  {
    return email;
  }
  
  public void setDocumentTypeComponent(HtmlSelectOneListbox documentTypeComponent)
  {
    this.documentTypeComponent = documentTypeComponent;
  }

  public HtmlSelectOneListbox getDocumentTypeComponent()
  {
    return documentTypeComponent;
  }
  
  //Validators
  public void validateDocumentNumber(FacesContext context, 
    UIComponent component, Object value)
    throws ValidatorException
  {
    String docType = (String)documentTypeComponent.getValue();
    if ("NIF".equals(docType))
      validateNif(context, value);
    else if ("PASSPORT".equals(docType))
      validatePassport(context, value);
    else if ("NIE".equals(docType))
      validateNie(context, value);
    else
      throw new ValidatorException(new FacesMessage("INVALID_OPTION"));
  }

  //Actions
  public String show(Form form)
  {
    Properties parameters = form.getParameters();
    Object value;
    value = parameters.get("prefix");
    if (value != null) prefix = String.valueOf(value);
    value = parameters.get("message");
    if (value != null) message = String.valueOf(value);
      
    loadPreviousVariables();
    loadUserSessionBeanValues();
    return "identification_form";
  }

  public Map submit()
  {
    return submitVariables();
  }
  
  private void loadPreviousVariables()
  {
    InstanceBean instanceBean = (InstanceBean)getBean("instanceBean");
    Map variables = instanceBean.getVariables();

    String varPrefix = "";
    if (prefix != null)
      varPrefix = this.prefix + "_";

    name = (String)variables.get(varPrefix + IdentificationForm.NAME);
    surname1 = (String)variables.get(varPrefix + IdentificationForm.SURNAME1);
    surname2 = (String)variables.get(varPrefix + IdentificationForm.SURNAME2);
    documentType = (String)variables.get(varPrefix + IdentificationForm.DOCUMENT_TYPE);
    documentNumber = (String)variables.get(varPrefix + IdentificationForm.DOCUMENT_NUMBER);
    wayType = (String)variables.get(varPrefix + IdentificationForm.WAY_TYPE);
    addressName = (String)variables.get(varPrefix + IdentificationForm.ADDRESS_NAME);
    addressNumber = (String)variables.get(varPrefix + IdentificationForm.ADDRESS_NUMBER);
    addressBlock = (String)variables.get(varPrefix + IdentificationForm.ADDRESS_BLOCK);
    addressStair = (String)variables.get(varPrefix + IdentificationForm.ADDRESS_STAIR);
    addressFloor = (String)variables.get(varPrefix + IdentificationForm.ADDRESS_FLOOR);
    addressDoor = (String)variables.get(varPrefix + IdentificationForm.ADDRESS_FLOOR);
    zipCode = (String)variables.get(varPrefix + IdentificationForm.POSTAL_CODE);
    city = (String)variables.get(varPrefix + IdentificationForm.CITY);
    province = (String)variables.get(varPrefix + IdentificationForm.PROVINCE);
    phone = (String)variables.get(varPrefix + IdentificationForm.PHONE);
    email = (String)variables.get(varPrefix + IdentificationForm.EMAIL);
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
    
    if (documentNumber == null)
    {
      documentNumber = userSessionBean.getNIF();
      documentType = "NIF";
    }
    
    if (email == null)
      email = userSessionBean.getEmail();
  }
  
  private Map submitVariables()
  {
    HashMap variables = new HashMap();
    String varPrefix = "";

    if (prefix != null)
      varPrefix = this.prefix + "_";

    variables.put(varPrefix + IdentificationForm.NAME, name);
    variables.put(varPrefix + IdentificationForm.SURNAME1, surname1);
    variables.put(varPrefix + IdentificationForm.SURNAME2, surname2);
    variables.put(varPrefix + IdentificationForm.DOCUMENT_TYPE, documentType);
    variables.put(varPrefix + IdentificationForm.DOCUMENT_NUMBER, documentNumber);
    variables.put(varPrefix + IdentificationForm.WAY_TYPE, wayType);
    variables.put(varPrefix + IdentificationForm.ADDRESS_NAME, addressName);
    variables.put(varPrefix + IdentificationForm.ADDRESS_NUMBER, addressNumber);
    variables.put(varPrefix + IdentificationForm.ADDRESS_BLOCK, addressBlock);
    variables.put(varPrefix + IdentificationForm.ADDRESS_STAIR, addressStair);
    variables.put(varPrefix + IdentificationForm.ADDRESS_FLOOR, addressFloor);
    variables.put(varPrefix + IdentificationForm.ADDRESS_DOOR, addressDoor);
    String address = wayType + " " + addressName + " " + addressNumber + " " + 
      addressBlock + " " + addressStair + " " + addressFloor + " " + addressDoor;
    variables.put(varPrefix + IdentificationForm.ADDRESS, address.trim());
    variables.put(varPrefix + IdentificationForm.POSTAL_CODE, zipCode);
    variables.put(varPrefix + IdentificationForm.CITY, city);
    variables.put(varPrefix + IdentificationForm.PROVINCE, province);
    variables.put(varPrefix + IdentificationForm.PHONE, phone);
    variables.put(varPrefix + IdentificationForm.EMAIL, email);
    return variables;
  }
  
  private void validateNif(FacesContext context, Object value)
    throws ValidatorException
  {
    Pattern pattern = Pattern.compile("[0-9]{8}[a-zA-Z]");
    Matcher matcher = pattern.matcher(((String)value));
    if (!matcher.matches())
    {
      String message = getLocalizedMessage(context, INVALID_NIF);
      throw new ValidatorException(new FacesMessage(message));        
    }
  }

  private void validateNie(FacesContext context, Object value)
    throws ValidatorException
  {
    Pattern pattern = Pattern.compile("(x|X|y|Y|z|Z)[0-9]{7}[a-zA-Z]");
    Matcher matcher = pattern.matcher(((String)value));
    if (!matcher.matches())
    {
      String message = getLocalizedMessage(context, INVALID_NIE);    
      throw new ValidatorException(new FacesMessage(message));  
    }
  }
  
  private void validatePassport(FacesContext context, Object value)
    throws ValidatorException
  {
  }
  
  private String getLocalizedMessage(FacesContext context, String key)
  {
    String messageBundleName = context.getApplication().getMessageBundle();
    Locale locale = context.getViewRoot().getLocale();
    ResourceBundle messageBundle = 
      ResourceBundle.getBundle(messageBundleName, locale);
    String message = messageBundle.getString(key);

    return (message != null ? message : key);
  }
}
