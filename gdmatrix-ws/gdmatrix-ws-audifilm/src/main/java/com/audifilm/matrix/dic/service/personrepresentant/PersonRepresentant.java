/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.audifilm.matrix.dic.service.personrepresentant;

import com.audifilm.matrix.dic.service.DictionaryManager;
import com.audifilm.matrix.dic.service.types.DicTypeInterface;
import com.audifilm.matrix.dic.service.types.DicTypeRoot;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author blanquepa
 */
public class PersonRepresentant extends DicTypeRoot<PersonRepresentantType>
{

  final static public String TYPEID =
    org.matrix.kernel.PersonRepresentant.class.getSimpleName();
  final static public String DESCRIPTION = "Tipus de representant";
  static List<DicTypeInterface> childInstances;

  public List<DicTypeInterface> getChildDicTypes()
  {
    if (childInstances == null)
    {
      childInstances = new ArrayList<DicTypeInterface>();
      childInstances.add(new PersonRepresentantType(this));
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
    return entityManager.loadTypeActions(org.matrix.kernel.PersonRepresentant.class.getName());
  }


  public List getAccessControlList(DictionaryManager dicManager, String typeId)
  {
    return dicManager.loadAccessControlList(org.matrix.kernel.PersonRepresentant.class.getName());
  }

}
