/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.audifilm.matrix.dic.service.contact;

import com.audifilm.matrix.dic.service.DictionaryManager;
import com.audifilm.matrix.dic.service.types.DicTypeInterface;
import com.audifilm.matrix.dic.service.types.DicTypeRoot;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author blanquepa
 */
public class Contact extends DicTypeRoot<ContactType>
{

  final static public String TYPEID =
    org.matrix.kernel.Contact.class.getSimpleName();
  final static public String DESCRIPTION = "Tipus de contacte";
  static List<DicTypeInterface> childInstances;

  public List<DicTypeInterface> getChildDicTypes()
  {
    if (childInstances == null)
    {
      childInstances = new ArrayList<DicTypeInterface>();
      childInstances.add(new ContactType(this));
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
    return entityManager.loadTypeActions(org.matrix.kernel.Contact.class.getName());
  }


  public List getAccessControlList(DictionaryManager dicManager, String typeId)
  {
    return dicManager.loadAccessControlList(org.matrix.kernel.Contact.class.getName());
  }

}
