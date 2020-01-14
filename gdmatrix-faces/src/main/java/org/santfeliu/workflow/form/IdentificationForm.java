package org.santfeliu.workflow.form;

import java.util.HashSet;
import java.util.Set;

public class IdentificationForm extends Form
{
  public static final String NAME = "name";
  public static final String SURNAME1 = "surname1";
  public static final String SURNAME2 = "surname2";
  public static final String DOCUMENT_TYPE = "documentType";
  public static final String DOCUMENT_NUMBER = "documentNumber";
  public static final String WAY_TYPE = "wayType";
  public static final String ADDRESS_NAME = "addressName";
  public static final String ADDRESS_NUMBER = "addressNumber";
  public static final String ADDRESS_BLOCK = "addressBlock";
  public static final String ADDRESS_STAIR = "addressStair";
  public static final String ADDRESS_FLOOR = "addressFloor";
  public static final String ADDRESS_DOOR = "addressDoor";
  public static final String ADDRESS = "address";
  public static final String POSTAL_CODE = "zipCode";
  public static final String CITY = "city";
  public static final String PROVINCE = "province";
  public static final String PHONE = "phone";
  public static final String EMAIL = "email";

  private static final String[] variables =
  {
    NAME,
    SURNAME1,
    SURNAME2,
    DOCUMENT_TYPE,
    DOCUMENT_NUMBER,
    WAY_TYPE,
    ADDRESS_NAME,
    ADDRESS_NUMBER,
    ADDRESS_BLOCK,
    ADDRESS_STAIR,
    ADDRESS_FLOOR,
    ADDRESS_DOOR,
    ADDRESS,
    POSTAL_CODE,
    CITY,
    PROVINCE,
    PHONE,
    EMAIL
  };

  @Override
  public Set getWriteVariables()
  {
    HashSet set = new HashSet();
    String varPrefix = (String)parameters.get("prefix");
    if (varPrefix == null) varPrefix = "";
    else varPrefix += "_";
    for (String var : variables)
    {
      set.add(varPrefix + var);
    }
    return set;
  }
}
