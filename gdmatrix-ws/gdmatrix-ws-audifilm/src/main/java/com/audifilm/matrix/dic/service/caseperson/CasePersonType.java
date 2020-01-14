package com.audifilm.matrix.dic.service.caseperson;

import com.audifilm.matrix.dic.service.DictionaryManager;
import com.audifilm.matrix.dic.service.types.DicTypeTaulaConfig;
import com.audifilm.matrix.util.TextUtil;
import java.util.ArrayList;
import java.util.List;
import org.matrix.dic.PropertyDefinition;
import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author comasfc
 */
public class CasePersonType extends DicTypeTaulaConfig<CasePerson>
{

  final static private String TCCODI0 = "TIPI";
  final static public String InteressatPrincipal = TCCODI0;
  final static public String InteressatPrincipalDesc = "INTERESSAT";

  public CasePersonType(CasePerson parent)
  {
    super(parent);
  }

  @Override
  public Type loadType(DictionaryManager dictionaryManager, String composedIdType)
  {

    String typeId = toLocalId(dictionaryManager.getEndpoint(), composedIdType);
    if (typeId != null && typeId.equalsIgnoreCase(InteressatPrincipal))
    {
      Type type = new Type();
      copyInteressatPrincipalTo(dictionaryManager.getEndpoint(), type);
      //PropertyDefinition
      //Les propietats son els camps fixos d'un expedient
      PropertyDefinition prop = new PropertyDefinition();
      type.getPropertyDefinition().add(prop);
      type.getAccessControl().addAll(getAccessControlList(dictionaryManager, composedIdType));

      return type;
    }
    else
    {
      return super.loadType(dictionaryManager, composedIdType);
    }
  }

  @Override
  public List<Type> findTypes(DictionaryManager dictionaryManager, TypeFilter filter)
  {
    String typeId = toLocalId(dictionaryManager.getEndpoint(), filter.getTypeId());
    List<Type> typesList = new ArrayList<Type>();
    if (matchInteressat(typeId, filter.getDescription()))
    {
      Type type = new Type();
      copyInteressatPrincipalTo(dictionaryManager.getEndpoint(), type);
      //PropertyDefinition
      //Les propietats son els camps fixos d'un expedient
      PropertyDefinition prop = new PropertyDefinition();
      type.getPropertyDefinition().add(prop);
      type.getAccessControl().addAll(getAccessControlList(dictionaryManager, toGlobalId(dictionaryManager.getEndpoint(), type.getTypeId())));

      typesList.add(type);
    }
    typesList.addAll(super.findTypes(dictionaryManager, filter));
    return typesList;
  }

  @Override
  public int countTypes(DictionaryManager dictionaryManager, TypeFilter filter)
  {
    String typeId = toLocalId(dictionaryManager.getEndpoint(), filter.getTypeId());
    int count = (matchInteressat(typeId, filter.getDescription()))? 1 : 0;
    return count + super.countTypes(dictionaryManager, filter);
  }

  public void copyInteressatPrincipalTo(WSEndpoint endpoint, Type type)
  {
    type.setTypeId(toGlobalId(endpoint, InteressatPrincipal));
    type.setSuperTypeId(getParentDicType().toGlobalId(endpoint, null));
    type.setDescription(InteressatPrincipalDesc);
    type.setTypePath(getGlobalTypePath(endpoint, InteressatPrincipal));
    type.setInstantiable(true);
  }

  @Override
  public String getTCCODI0()
  {
    return TCCODI0;
  }

  boolean matchInteressat(String localFilterTypeId, String filterDescription)
  {
    return (filterDescription == null || (TextUtil.matchesFilter(filterDescription, InteressatPrincipalDesc))) 
            && (localFilterTypeId == null || "".equals(localFilterTypeId) || (localFilterTypeId.equals(InteressatPrincipal)));
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

  


