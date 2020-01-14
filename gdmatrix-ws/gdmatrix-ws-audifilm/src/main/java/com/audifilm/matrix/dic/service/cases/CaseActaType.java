package com.audifilm.matrix.dic.service.cases;

import com.audifilm.matrix.dic.service.*;
import com.audifilm.matrix.dic.service.types.DicType;
import com.audifilm.matrix.dic.service.types.DicTypeLeave;
import com.audifilm.matrix.util.TextUtil;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Query;
import javax.xml.ws.WebServiceException;
import org.matrix.dic.PropertyDefinition;
import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author comasfc
 */
public class CaseActaType extends DicTypeLeave
{

  final static String CaseActaTypePREFIX = "A" + DicType.PREFIX_SEPARATOR;

  public CaseActaType(CaseExpedient parent)
  {
    super(parent);
  }

  public String getTypeIdPrefix()
  {
    return CaseActaTypePREFIX;
  }


  public List<Type> findTypes(DictionaryManager dictionaryManager, TypeFilter filter)
  {
    Query query = dictionaryManager.entityManager.createNamedQuery("findActaTypes");
    query.setParameter("typeId", toLocalId(dictionaryManager.getEndpoint(), filter.getTypeId()));
    query.setParameter("description", TextUtil.likePattern(filter.getDescription()));
    query.setParameter("minChangeDateTime", filter.getMinChangeDateTime());
    query.setParameter("maxChangeDateTime", filter.getMaxChangeDateTime());
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());

    List<DBCaseType> dbCaseTypeList = query.getResultList();

    List<Type> typesList = new ArrayList<Type>();
    for (DBCaseType dbCaseType : dbCaseTypeList)
    {
      Type type = new Type();
      copyTo(dictionaryManager.getEndpoint(), dbCaseType, type);

      typesList.add(type);
    }
    return typesList;
  }

  public int countTypes(DictionaryManager dictionaryManager, TypeFilter filter)
  {
    Query query = dictionaryManager.entityManager.createNamedQuery("countActaTypes");
    query.setParameter("typeId", toLocalId(dictionaryManager.getEndpoint(), filter.getTypeId()));
    query.setParameter("description", TextUtil.likePattern(filter.getDescription()));
    query.setParameter("minChangeDateTime", filter.getMinChangeDateTime());
    query.setParameter("maxChangeDateTime", filter.getMaxChangeDateTime());

    Number count = (Number) query.getSingleResult();
    return count.intValue();
  }

  public List<String> getTypeActions(DictionaryManager entityManager, String typeId)
  {
    return entityManager.loadTypeActions(org.matrix.cases.Case.class.getName());
  }

  @Override
  public List listModifiedTypes(DictionaryManager dicManager, String dateTime1, String dateTime2)
  {
    Query query = dicManager.entityManager.createNamedQuery("listModifiedCaseTypes");
    query.setParameter("dateTime1", dateTime1);
    query.setParameter("dateTime2", dateTime2);
    @SuppressWarnings("unchecked")
    List<String> dbCaseIdList = query.getResultList();

    List<String> globalTypeIdList = new ArrayList<String>();
    for (String caseId : dbCaseIdList)
    {
      globalTypeIdList.add(toGlobalId(dicManager.getEndpoint(), caseId));
    }
    return globalTypeIdList;
  }

  public Type loadType(DictionaryManager dicManager, String composedIdType)
  {
    Type type = new Type();

    String idType = toLocalId(dicManager.getEndpoint(), composedIdType);
    DBCaseType dbType = dicManager.entityManager.find(DBCaseType.class, idType);
    if (dbType == null)
    {
      throw new WebServiceException("dic:TYPE_NOT_FOUND");
    }
    copyTo(dicManager.getEndpoint(), dbType, type);

    //PropertyDefinition
    //Les propietats son els camps fixos d'un expedient
    type.getPropertyDefinition().addAll(loadPropertyDefinitionList
            (dicManager, idType));
    type.getAccessControl().addAll(
            getAccessControlList(dicManager, idType)

            );



    return type;
  }

  public Type storeType(DictionaryManager dicManager, Type type)
  {
    if (type.getTypeId() == null)
    {
      throw new WebServiceException("Not supported");
    }
    //NO VULL FER UN STORE PER TANT FAIG UN LOAD I RETORNO EL TROBAT
    return loadType(dicManager, type.getTypeId());
  }

  public List getAccessControlList(DictionaryManager dicManager, String typeId)
  {
    return dicManager.loadAccessControlList(org.matrix.cases.Case.class.getName());
  }

  public void copyTo(WSEndpoint endpoint, DBCaseType dbCaseType, Type type)
  {
    type.setTypeId(toGlobalId(endpoint, dbCaseType.getTypeId()));
    type.setSuperTypeId(getParentDicType().toGlobalId(endpoint, null));
    type.setDescription(dbCaseType.getDescription());
    type.setTypePath(getGlobalTypePath(endpoint, dbCaseType.getTypeId()));

    type.setInstantiable(true);
    type.setRestricted(false);

    type.setChangeDateTime(dbCaseType.getChangeDateTime());
    type.setChangeUserId(dbCaseType.getStdumod());
    type.setCreationDateTime(dbCaseType.getCreationDateTime());
    type.setCreationUserId(dbCaseType.getStdugr());
  }

  private List<PropertyDefinition> loadPropertyDefinitionList(DictionaryManager dictionaryManager, String caseTypeId)
  {
    List<PropertyDefinition> propertyList = new ArrayList<PropertyDefinition>();

    Query query = dictionaryManager.entityManager.createNamedQuery("listCaseTypeVariables");
    query.setParameter("caseTypeId", caseTypeId);
    query.setFirstResult(0);
    query.setMaxResults(0);

    List<DBVariableDefinition> dbVariableList = query.getResultList();
    for(DBVariableDefinition dbVariable : dbVariableList)
    {
      PropertyDefinition prop = new PropertyDefinition();
      dbVariable.copyTo(dictionaryManager.getEndpoint(), prop);

      propertyList.add(prop);
    }
    return propertyList;
  }
}
