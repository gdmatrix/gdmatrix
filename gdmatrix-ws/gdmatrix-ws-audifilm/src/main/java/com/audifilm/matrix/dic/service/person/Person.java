package com.audifilm.matrix.dic.service.person;

import com.audifilm.matrix.dic.service.DictionaryManager;
import com.audifilm.matrix.dic.service.types.DicTypeInterface;
import com.audifilm.matrix.dic.service.types.DicTypeRoot;
import java.util.ArrayList;
import java.util.List;
import org.matrix.security.AccessControl;

/**
 *
 * @author comasfc
 */
public class Person extends DicTypeRoot<PersonType>
{

  final static private String TYPEID = org.matrix.kernel.Person.class.getSimpleName();
  final static private String DESCRIPTION = "Persona";
  static List<DicTypeInterface> childInstances;

  public List<DicTypeInterface> getChildDicTypes()
  {
    if (childInstances == null)
    {
      childInstances = new ArrayList<DicTypeInterface>();
      childInstances.add(new PersonType(this));
    }
    return childInstances;
  }
// 

  @Override
  public String getRootTypeId()
  {
    return TYPEID;
  }

  @Override
  public String getDescription()
  {
    return DESCRIPTION;
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
