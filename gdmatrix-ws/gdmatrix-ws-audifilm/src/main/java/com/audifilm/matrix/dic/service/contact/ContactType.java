package com.audifilm.matrix.dic.service.contact;


import com.audifilm.matrix.dic.service.DictionaryManager;
import com.audifilm.matrix.dic.service.types.DicTypeTaulaConfig;
import java.util.ArrayList;
import java.util.List;
import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;

/**
 *
 * @author blanquepa
 */
public class ContactType extends DicTypeTaulaConfig<Contact>
{

  final static private String TCCODI0 = "TCON";

  public ContactType(Contact parent)
  {
    super(parent);
  }

  @Override
  public Type loadType(DictionaryManager dictionaryManager, String composedIdType)
  {
    return super.loadType(dictionaryManager, composedIdType);
  }

  @Override
  public List<Type> findTypes(DictionaryManager dictionaryManager, TypeFilter filter)
  {
    List<Type> typesList = new ArrayList<Type>();
    typesList.addAll(super.findTypes(dictionaryManager, filter));
    return typesList;
  }

  @Override
  public int countTypes(DictionaryManager dictionaryManager, TypeFilter filter)
  {
    return super.countTypes(dictionaryManager, filter);
  }

  @Override
  public String getTCCODI0()
  {
    return TCCODI0;
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

  


