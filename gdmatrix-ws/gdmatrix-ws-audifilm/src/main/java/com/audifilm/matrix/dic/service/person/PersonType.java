package com.audifilm.matrix.dic.service.person;

import com.audifilm.matrix.dic.service.DictionaryManager;
import com.audifilm.matrix.dic.service.types.DicType;
import com.audifilm.matrix.dic.service.types.EnumeratedDicType;
import com.audifilm.matrix.dic.service.types.TypeEnumElement;
import java.util.List;
import org.matrix.security.AccessControl;


/**
 *
 * @author comasfc
 */
public class PersonType extends EnumeratedDicType<Person>
{
  final static public String PREFIX = "P" + DicType.PREFIX_SEPARATOR;

  @Override
  public TypeEnumElement getElement(String id)
  {
    return Types.getType(id);
  }

  @Override
  public TypeEnumElement[] getElements()
  {
    return Types.values();
  }


  static public enum Types implements TypeEnumElement
  {

    E("Entitat"),
    F("Persona física"),
    J("Persona jurídica"),
    P("Passaport"),
    R("Permís de residència"),
    X("Persona jurídica extrangera"),
    Y("Persona jurídica amb CIF desconegut"),
    Z("Persona física amb document desconegut");
    String typeId;
    String description;

    Types(String description)
    {
      this.typeId = PREFIX + name();
      this.description = description;
    }

    public String getTypeId()
    {
      return typeId;
    }

    public String getDescription()
    {
      return description;
    }

    static public Types getType(String typeId)
    {
      if (typeId.startsWith(PREFIX))
      {
        return valueOf(typeId.substring(PREFIX.length()));
      }
      return valueOf(typeId);
    }
  }

  public PersonType(Person parent)
  {
    super(parent);
  }

  public String getTypeIdPrefix()
  {
    return PREFIX;
  }

  @Override
  public List<String> getTypeActions(DictionaryManager entityManager, String typeId)
  {
    return entityManager.loadTypeActions(org.matrix.kernel.Person.class.getName());
  }

  public List<AccessControl> getAccessControlList(DictionaryManager dicManager, String typeId)
  {
    return dicManager.loadAccessControlList(org.matrix.kernel.Person.class.getName());
  }

}
