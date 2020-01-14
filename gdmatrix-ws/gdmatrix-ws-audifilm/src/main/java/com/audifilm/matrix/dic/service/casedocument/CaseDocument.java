package com.audifilm.matrix.dic.service.casedocument;


import com.audifilm.matrix.dic.service.DictionaryManager;
import com.audifilm.matrix.dic.service.types.DicTypeInterface;
import com.audifilm.matrix.dic.service.types.DicTypeRoot;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author blanquepa
 */
public class CaseDocument extends DicTypeRoot<CaseDocumentType>
{
  final static private String TYPEID = org.matrix.cases.CaseDocument.class.getSimpleName();
  final static private String DESCRIPTION = "Tipus de documents de l'expedient";
  static List<DicTypeInterface> childInstances;

  public List<DicTypeInterface> getChildDicTypes()
  {
    if (childInstances == null)
    {
      childInstances = new ArrayList<DicTypeInterface>();
      childInstances.add(new CaseDocumentType(this));
    }
    return childInstances;
  }  

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
    return entityManager.loadTypeActions(org.matrix.cases.CaseDocument.class.getName());
  }

  public List getAccessControlList(DictionaryManager dicManager, String typeId)
  {
    return dicManager.loadAccessControlList(org.matrix.cases.CaseDocument.class.getName());
  }
  
}
