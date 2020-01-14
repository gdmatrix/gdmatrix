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
public class CaseExpedientType extends DicTypeLeave
{

  final static String PREFIX = "X" + DicType.PREFIX_SEPARATOR;

  public CaseExpedientType(CaseExpedient parent)
  {
    super(parent);
  }

  public String getTypeIdPrefix()
  {
    return PREFIX;
  }

  public String getTypePathFilterPattern(WSEndpoint endpoint)
  {
    return getParentDicType().getGlobalTypePath(endpoint, "") + ".*";
  }

  public Type loadType(DictionaryManager dictionaryManager, String composedIdType)
  {
    Type type = new Type();

    String idType = toLocalId(dictionaryManager.getEndpoint(), composedIdType);
    //DBCaseType dbType = dictionaryManager.entityManager.find(DBCaseType.class, idType);
    Query query = dictionaryManager.entityManager.createNamedQuery("findCaseType");
    query.setParameter("typeId", toLocalId(dictionaryManager.getEndpoint(), idType));
    query.setFirstResult(0);
    query.setMaxResults(1);

    List<DBCaseType> dbCaseTypeList = (List<DBCaseType>)query.getResultList();

    if (dbCaseTypeList.size() < 1)
    {
      throw new WebServiceException("dic:TYPE_NOT_FOUND");
    }
    DBCaseType dbType = dbCaseTypeList.get(0);
    copyTo(dictionaryManager.getEndpoint(), dbType, type);

    //PropertyDefinition
    //Les propietats son els camps fixos d'un expedient
    type.getPropertyDefinition().addAll(loadPropertyDefinitionList(dictionaryManager, idType));
    type.getAccessControl().addAll(
            getAccessControlList(dictionaryManager, idType)

            );

    return type;
  }

  public Type storeType(DictionaryManager dictionaryManager, Type type)
  {
    if (type.getTypeId() == null)
    {
      throw new WebServiceException("Not supported");
    }
    //NO VULL FER UN STORE PER TANT FAIG UN LOAD I RETORNO EL TROBAT
    return loadType(dictionaryManager, type.getTypeId());

  }

  public List<Type> findTypes(DictionaryManager dictionaryManager, TypeFilter filter)
  {


    Query query = dictionaryManager.entityManager.createNamedQuery("findCaseTypes");
    query.setParameter("typeId", toLocalId(dictionaryManager.getEndpoint(), filter.getTypeId()));
    query.setParameter("description", TextUtil.likePattern(filter.getDescription()));
    query.setParameter("minChangeDateTime", filter.getMinChangeDateTime());
    query.setParameter("maxChangeDateTime", filter.getMaxChangeDateTime());
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());

    @SuppressWarnings("unchecked")
    List<DBCaseType> dbCaseTypeList = (List<DBCaseType>)query.getResultList();

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


    Query query = dictionaryManager.entityManager.createNamedQuery("countCaseTypes");
    query.setParameter("typeId", toLocalId(dictionaryManager.getEndpoint(), filter.getTypeId()));
    query.setParameter("description", TextUtil.likePattern(filter.getDescription()));
    query.setParameter("minChangeDateTime", filter.getMinChangeDateTime());
    query.setParameter("maxChangeDateTime", filter.getMaxChangeDateTime());

    Number count = (Number) query.getSingleResult();
    return count.intValue();
  }

  @Override
  public List<String> listModifiedTypes(DictionaryManager dictionaryManager, String dateTime1, String dateTime2)
  {
    Query query = dictionaryManager.entityManager.createNamedQuery("listModifiedCaseTypes");
    query.setParameter("dateTime1", dateTime1);
    query.setParameter("dateTime2", dateTime2);
    @SuppressWarnings("unchecked")
    List<String> dbCaseIdList = query.getResultList();

    List<String> globalTypeIdList = new ArrayList<String>();
    for (String caseId : dbCaseIdList)
    {
      globalTypeIdList.add(toGlobalId(dictionaryManager.getEndpoint(), caseId));
    }
    return globalTypeIdList;
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

  @Override
  public List<String> getTypeActions(DictionaryManager entityManager, String typeId)
  {
    return entityManager.loadTypeActions(org.matrix.cases.Case.class.getName());
  }

  public List getAccessControlList(DictionaryManager dicManager, String typeId)
  {
    return dicManager.loadAccessControlList(org.matrix.cases.Case.class.getName());
  }



}
