package com.audifilm.matrix.dic.service.caseaddress;

import com.audifilm.matrix.dic.service.DictionaryManager;
import com.audifilm.matrix.dic.service.types.DicTypeInterface;
import com.audifilm.matrix.dic.service.types.DicTypeRoot;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author comasfc
 */
public class CaseAddress extends DicTypeRoot<CaseAddressType>
{

  final static private String TYPEID = org.matrix.cases.CaseAddress.class.getSimpleName();
  final static private String DESCRIPTION = "Tipus de domicilis de l'expedient";
  static List<DicTypeInterface> childInstances;

  public List<DicTypeInterface> getChildDicTypes()
  {
    if (childInstances == null)
    {
      childInstances = new ArrayList<DicTypeInterface>();
      childInstances.add(new CaseAddressType(this));
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
    return entityManager.loadTypeActions(org.matrix.cases.CaseAddress.class.getName());
  }

  public List getAccessControlList(DictionaryManager dicManager, String typeId)
  {
    return dicManager.loadAccessControlList(org.matrix.cases.CaseAddress.class.getName());
  }


}
