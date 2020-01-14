package org.santfeliu.workflow.form;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author realor
 */
public class PersonForm extends Form
{
  public static final String PERSON_ID = "personId";
  public static final String NAME = "name";
  public static final String SURNAME1 = "surname1";
  public static final String SURNAME2 = "surname2";
  public static final String SEX = "sex";
  public static final String NIF = "NIF";
  public static final String PASSPORT = "passport";
  public static final String NATIONALITY_ID = "nationalityId"; // countryId
  public static final String CIF = "CIF";
  public static final String PHONE = "phone";
  public static final String EMAIL = "email";

  private static final String[] variables =
  {
    PERSON_ID,
    NAME,
    SURNAME1,
    SURNAME2,
    SEX,
    NIF,
    PASSPORT,
    NATIONALITY_ID,
    CIF,
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
