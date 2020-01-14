/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.audifilm.matrix.dic.service.caseperson;

import com.audifilm.matrix.dic.service.DictionaryManager;
import com.audifilm.matrix.dic.service.types.DicTypeInterface;
import com.audifilm.matrix.dic.service.types.DicTypeRoot;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author comasfc
 */
public class CasePerson extends DicTypeRoot<CasePersonType>
{

  final static public String TYPEID = org.matrix.cases.CasePerson.class.getSimpleName();
  final static public String DESCRIPTION = "Tipus de persona";
  static List<DicTypeInterface> childInstances;

  public List<DicTypeInterface> getChildDicTypes()
  {
    if (childInstances == null)
    {
      childInstances = new ArrayList<DicTypeInterface>();
      childInstances.add(new CasePersonType(this));
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
    return entityManager.loadTypeActions(org.matrix.cases.CasePerson.class.getName());
  }


  public List getAccessControlList(DictionaryManager dicManager, String typeId)
  {
    return dicManager.loadAccessControlList(org.matrix.cases.CasePerson.class.getName());
  }

}
