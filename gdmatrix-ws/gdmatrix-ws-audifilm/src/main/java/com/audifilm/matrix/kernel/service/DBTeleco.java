package com.audifilm.matrix.kernel.service;

import com.audifilm.matrix.dic.service.contact.ContactType;
import com.audifilm.matrix.dic.service.types.DicTypeAdmin;
import org.matrix.kernel.Contact;
import org.matrix.util.WSEndpoint;


public class DBTeleco extends DBEntityBase
{
  private String personId;
  private int contactNumber;
  private String contactTypeId;
  private String value;
  private String comments;
  private String valdata;

  public DBTeleco()
  {
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public String getPersonId()
  {
    return personId;
  }

  public void setContactNumber(int contactNumber)
  {
    this.contactNumber = contactNumber;
  }

  public int getContactNumber()
  {
    return contactNumber;
  }

  public void setContactTypeId(String contactTypeId)
  {
    // Warning: contactTypeId.length() must be less or equal to 4. 
    // But sometimes contactTypeId.length() > 4, so it must be truncated.
    if (contactTypeId.length() > 4)
    {
      contactTypeId = contactTypeId.substring(0, 4);
    }
    this.contactTypeId = contactTypeId;
  }

  public String getContactTypeId()
  {
    return contactTypeId;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }

  public void setComments(String comments)
  {
    this.comments = comments;
  }

  public String getComments()
  {
    return comments;
  }

  public void setValdata(String valdata)
  {
    this.valdata = valdata;
  }

  public String getValdata()
  {
    return valdata;
  }
  
  public void copyFrom(WSEndpoint endpoint, Contact contact)
  {
    this.personId = contact.getPersonId();
    this.contactTypeId = DicTypeAdmin.getInstance(ContactType.class)
      .toLocalId(endpoint, contact.getContactTypeId());
    this.value = contact.getValue();
    this.comments = contact.getComments();
  }
  
  public void copyTo(WSEndpoint endpoint, Contact contact)
  {
    contact.setPersonId(personId);
    contact.setContactTypeId(
      DicTypeAdmin.getInstance(ContactType.class)
      .toGlobalId(endpoint, contactTypeId));
    
    contact.setValue(value);
    contact.setComments(comments);
  }
}
