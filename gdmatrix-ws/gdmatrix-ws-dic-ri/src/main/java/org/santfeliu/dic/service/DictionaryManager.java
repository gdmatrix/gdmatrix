/*
 * GDMatrix
 *
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *
 * This program is licensed and may be used, modified and redistributed under
 * the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European
 * Commission.
 *
 * Alternatively, you may redistribute and/or modify this program under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either  version 3 of the License, or (at your option)
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the licenses for the specific language governing permissions, limitations
 * and more details.
 *
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along
 * with this program; if not, you may find them at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/
 * and
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.dic.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.*;
import org.matrix.security.AccessControl;
import org.matrix.security.SecurityConstants;
import org.santfeliu.dic.RootTypeFactory;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.security.User;
import org.santfeliu.security.UserCache;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.TextUtils;
import org.santfeliu.ws.annotations.Initializer;
import org.santfeliu.ws.annotations.MultiInstance;

/**
 *
 * @author realor
 */
@WebService(endpointInterface = "org.matrix.dic.DictionaryManagerPort")
@HandlerChain(file="handlers.xml")
@MultiInstance
public class DictionaryManager implements DictionaryManagerPort
{
  private static final Logger LOGGER = Logger.getLogger("Dictionary");

  private static final int MAX_TYPEID_LENGTH = 64;
  private static final int MAX_TYPEDESCRIPTION_LENGTH = 1000;

  private static final int MAX_ENUMTYPEID_LENGTH = 64;
  private static final int MAX_ENUMTYPE_NAME_LENGTH = 200;
  private static final int MAX_ENUMTYPEITEM_VALUE_LENGTH = 200;
  private static final int MAX_ENUMTYPEITEM_LABEL_LENGTH = 1000;
  private static final int MAX_ENUMTYPEITEM_DESCRIPTION_LENGTH = 2000;
  private static final int MAX_ENUMTYPEITEM_INDEX_LENGTH = 5;

  @Resource
  WebServiceContext wsContext;

  @PersistenceContext(unitName="dic_ri")
  EntityManager entityManager;

  //typeActions method
  private static HashMap<String, List<String>> typeActionsMap = new HashMap<>();
  private static long lastModified;

  @Initializer
  public void initialize(String endpointName)
  {
    createRootTypes();
  }

  @Override
  public Type loadType(String typeId)
  {
    LOGGER.log(Level.INFO, "loadType {0}", new Object[]{typeId});

    if (typeId == null)
      throw new WebServiceException("dic:TYPEID_IS_MANDATORY");

    Type type;
    DBType dbType = entityManager.find(DBType.class, typeId);
    if (dbType == null || dbType.isRemoved())
    {
      throw new WebServiceException("dic:TYPE_NOT_FOUND");
    }
    else // type found
    {
      type = new Type();
      dbType.copyTo(type);

      List<DBPropertyDefinition> dbPropertyDefinitionList =
         loadPropertyDefinitionList(typeId);
      for (DBPropertyDefinition dbPropertyDefinition : dbPropertyDefinitionList)
      {
        PropertyDefinition property = new PropertyDefinition();
        dbPropertyDefinition.copyTo(property);
        type.getPropertyDefinition().add(property);
      }
      List<DBAccessControl> dbAccessControlList = loadAccessControlList(typeId);
      for (DBAccessControl dbAccessControl : dbAccessControlList)
      {
        AccessControl accessControl = new AccessControl();
        dbAccessControl.copyTo(accessControl);
        type.getAccessControl().add(accessControl);
      }
    }
    return type;
  }

  @Override
  public Type storeType(Type type)
  {
    User user = UserCache.getUser(wsContext);

    String typeId = type.getTypeId();
    LOGGER.log(Level.INFO, "storeType {0}, userId: {1}",
      new Object[]{typeId, user.getUserId()});

    if (typeId == null || typeId.trim().length() == 0)
      throw new WebServiceException("dic:TYPEID_IS_MANDATORY");

    DBType dbType = entityManager.find(DBType.class, typeId);
    List<DBPropertyDefinition> dbPropertyDefinitionList;
    List<DBAccessControl> dbAccessControlList;

    if (dbType == null || dbType.isRemoved()) // new Type
    {
      if (!canUserCreateTypeDefinition(user, type))
        throw new WebServiceException("dic:NOT_AUTHORIZED_TO_CREATE_TYPE");

      checkType(type);
      dbPropertyDefinitionList = null;
      dbAccessControlList = null;
      addNominalRoles(user, type);
      if (dbType == null) // New Type
      {
        dbType = new DBType(type);
        auditType(dbType, user, true);
        setSuperType(dbType, user, true);
        entityManager.persist(dbType);
      }
      else // Type has removed mark
      {
        dbType.copyFrom(type);
        dbType.setRemoved("F");
        auditType(dbType, user, true);
        setSuperType(dbType, user, true);
        entityManager.merge(dbType);
      }
    }
    else // Type update
    {
      dbPropertyDefinitionList = loadPropertyDefinitionList(typeId);
      dbAccessControlList = loadAccessControlList(typeId);

      if (!canUserModifyTypeDefinition(user, type, dbType, dbAccessControlList))
        throw new WebServiceException("dic:NOT_AUTHORIZED_TO_MODIFY_TYPE");

      checkType(type);
      dbType.copyFrom(type);
      auditType(dbType, user, false);
      setSuperType(dbType, user, false);
      entityManager.merge(dbType);
    }
    entityManager.flush();

    // --- store data in DIC_PROPDEF ---
    storePropertyDefinitionList(type, dbPropertyDefinitionList);
    // --- store data in DIC_ACL ---
    storeAccessControlList(type, dbAccessControlList);

    dbType.copyTo(type);
    return type;
  }

  @Override
  public boolean removeType(String typeId)
  {
    User user = UserCache.getUser(wsContext);

    LOGGER.log(Level.INFO, "removeType {0}", new Object[]{typeId});

    if (typeId == null)
      throw new WebServiceException("dic:TYPEID_IS_MANDATORY");

    DBType dbType = null;
    try
    {
      Query query;
      dbType = entityManager.find(DBType.class, typeId);
      if (dbType == null || dbType.isRemoved()) return false;

      if (!canUserRemoveTypeDefinition(user, dbType))
        throw new WebServiceException("dic:NOT_AUTHORIZED_TO_REMOVE_TYPE");

      query = entityManager.createNamedQuery("isDerivedType");
      query.setParameter("typeId", typeId);
      Number number = (Number)query.getSingleResult();
      if (number.intValue() > 0)
        throw new WebServiceException("dic:TYPE_HAS_DERIVED_TYPES");

      query = entityManager.createNamedQuery("removePropertyDefinition");
      query.setParameter("typeId", typeId);
      query.executeUpdate();

      query = entityManager.createNamedQuery("removeAccessControl");
      query.setParameter("typeId", typeId);
      query.executeUpdate();

      entityManager.flush();
      String dateTime = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");
      dbType.setChangeDateTime(dateTime);
      dbType.setChangeUserId(user.getUserId());
      dbType.setRemoved("T");
      entityManager.persist(dbType);
    }
    catch (PersistenceException ex)
    {
      entityManager.getTransaction().rollback();
      throw new WebServiceException("dic:TYPE_CAN_NOT_BE_REMOVED");
    }
    return true;
  }

  @Override
  public int countTypes(TypeFilter filter)
  {
    User user = UserCache.getUser(wsContext);

    LOGGER.log(Level.INFO, "countTypes");

    if (isRootTypeFilter(filter))
    {
      return 1;
    }
    else
    {
      Query query = entityManager.createNamedQuery("countTypes");
      applyFilter(query, filter, user);
      Number number = (Number)query.getSingleResult();
      return number.intValue();
    }
  }

  @Override
  public List<Type> findTypes(TypeFilter filter)
  {
    User user = UserCache.getUser(wsContext);

    LOGGER.log(Level.INFO, "findTypes");

    if (StringUtils.isBlank(filter.getTypeId()) &&
        StringUtils.isBlank(filter.getSuperTypeId()) &&
        StringUtils.isBlank(filter.getTypePath()) &&
        StringUtils.isBlank(filter.getDescription()) &&
        StringUtils.isBlank(filter.getAction()) &&
        StringUtils.isBlank(filter.getMinChangeDateTime()) &&
        StringUtils.isBlank(filter.getMaxChangeDateTime()) &&
        filter.getMaxResults() == 0)
      throw new WebServiceException("FILTER_NOT_ALLOWED");

    ArrayList<Type> types = new ArrayList<Type>();
    Query query = entityManager.createNamedQuery("findTypes");
    applyFilter(query, filter, user);
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
    List<DBType> dbTypes = query.getResultList();
    for (DBType dbType : dbTypes)
    {
      Type type = new Type();
      dbType.copyTo(type);
      types.add(type);
    }
    if (types.isEmpty() && isRootTypeFilter(filter))
    {
      types.add(RootTypeFactory.getRootType(filter.getTypeId()));
    }
    return types;
  }

  @Override
  public List<Property> initProperties(String typeId, List<Property> property)
  {
    return property;
  }

  @Override
  public List<Property> completeProperties(String typeId, List<Property> property)
  {
    return property;
  }

  @Override
  public List<String> getTypeActions(String typeId)
  {
    String path =
      MatrixConfig.getPathProperty("org.santfeliu.dic.typeActionsPath");
    File file = new File(path);
    if (file.exists())
    {
      if (lastModified != file.lastModified())
      {
        loadTypeActions(file);
        lastModified = file.lastModified();
      }
    }
    List<String> typeActionsList = typeActionsMap.get(typeId);
    if (typeActionsList == null) typeActionsList = Collections.EMPTY_LIST;
    return typeActionsList;
  }

  @Override
  public List<String> listModifiedTypes(String dateTime1, String dateTime2)
  {
    Query query = entityManager.createNamedQuery("listModifiedTypes");
    query.setParameter("dateTime1", dateTime1);
    query.setParameter("dateTime2", dateTime2);
    return query.getResultList();
  }

  @Override
  public EnumType loadEnumType(String enumTypeId)
  {
    LOGGER.log(Level.INFO, "loadEnumType {0}", new Object[]{enumTypeId});

    if (enumTypeId == null)
      throw new WebServiceException("dic:TYPEID_IS_MANDATORY");

    DBEnumType dbEnumType = entityManager.find(DBEnumType.class, enumTypeId);
    if (dbEnumType == null)
    {
      throw new WebServiceException("dic:ENUM_TYPE_NOT_FOUND");
    }
    EnumType enumType = new EnumType();
    dbEnumType.copyTo(enumType);
    return enumType;
  }

  @Override
  public EnumType storeEnumType(EnumType enumType)
  {
    String enumTypeId = enumType.getEnumTypeId();
    LOGGER.log(Level.INFO, "storeEnumType {0}", new Object[]{enumTypeId});
    checkEnumType(enumType);

    User user = UserCache.getUser(wsContext);
    DBEnumType dbEnumType = entityManager.find(DBEnumType.class, enumTypeId);
    if (dbEnumType == null) //insert
    {
      dbEnumType = new DBEnumType(enumType);
      auditEnumType(dbEnumType, user, true);
      entityManager.persist(dbEnumType);
    }
    else
    {
      dbEnumType.copyFrom(enumType);
      auditEnumType(dbEnumType, user, false);
      entityManager.merge(dbEnumType);
    }
    dbEnumType.copyTo(enumType);
    return enumType;
  }

  @Override
  public boolean removeEnumType(String enumTypeId)
  {
    LOGGER.log(Level.INFO, "removeEnumType {0}", new Object[]{enumTypeId});

    if (enumTypeId == null)
      throw new WebServiceException("dic:ENUMTYPEID_IS_MANDATORY");

    try
    {
      DBEnumType dbEnumType =
        entityManager.getReference(DBEnumType.class, enumTypeId);

      Query query = entityManager.createNamedQuery("isDerivedEnumType");
      query.setParameter("enumTypeId", enumTypeId);
      Number number = (Number)query.getSingleResult();
      if (number.intValue() > 0)
        throw new WebServiceException("dic:TYPE_HAS_DERIVED_TYPES");

      query = entityManager.createNamedQuery("removeEnumTypeItems");
      query.setParameter("enumTypeId", enumTypeId);
      query.executeUpdate();
      entityManager.remove(dbEnumType);
      return true;
    }
    catch (EntityNotFoundException ex)
    {
      return false;
    }
  }

  @Override
  public int countEnumTypes(EnumTypeFilter filter)
  {
    LOGGER.log(Level.INFO, "countEnumTypes");
    Query query = entityManager.createNamedQuery("countEnumTypes");
    applyEnumTypeFilter(query, filter);
    Number number = (Number)query.getSingleResult();
    return number.intValue();
  }

  @Override
  public List<EnumType> findEnumTypes(EnumTypeFilter filter)
  {
    LOGGER.log(Level.INFO, "findEnumTypes");
    List<EnumType> result = new ArrayList<EnumType>();
    Query query = entityManager.createNamedQuery("findEnumTypes");
    applyEnumTypeFilter(query, filter);
    query.setFirstResult(filter.getFirstResult());
    query.setMaxResults(filter.getMaxResults());
    List<DBEnumType> dbEnumTypeList = query.getResultList();
    for (DBEnumType dbEnumType : dbEnumTypeList)
    {
      EnumType enumType = new EnumType();
      dbEnumType.copyTo(enumType);
      result.add(enumType);
    }
    return result;
  }

  @Override
  public EnumTypeItem loadEnumTypeItem(String enumTypeItemId)
  {
    LOGGER.log(Level.INFO, "loadEnumTypeItem {0}", enumTypeItemId);

    if (enumTypeItemId == null)
      throw new WebServiceException("dic:ENUMTYPEITEMID_IS_MANDATORY");

    DBEnumTypeItem dbEnumTypeItem = entityManager.find(DBEnumTypeItem.class,
      enumTypeItemId);

    if (dbEnumTypeItem == null)
    {
      throw new WebServiceException("dic:ENUM_TYPE_ITEM_NOT_FOUND");
    }
    EnumTypeItem enumTypeItem = new EnumTypeItem();
    dbEnumTypeItem.copyTo(enumTypeItem);
    return enumTypeItem;
  }

  @Override
  public EnumTypeItem storeEnumTypeItem(EnumTypeItem enumTypeItem)
  {
    LOGGER.log(Level.INFO, "storeEnumTypeItem enumTypeId {0}, value {1}",
      new String[]{enumTypeItem.getEnumTypeId(), enumTypeItem.getValue()});
    checkEnumTypeItem(enumTypeItem);

    int currentIndex = getCurrentIndex(enumTypeItem.getEnumTypeItemId());
    int newIndex = processIndexes(enumTypeItem, currentIndex);
    DBEnumTypeItem dbEnumTypeItem = new DBEnumTypeItem(enumTypeItem);
    dbEnumTypeItem.setIndex(newIndex);
    if (currentIndex < 0) //Insert
    {
      entityManager.persist(dbEnumTypeItem);
    }
    else //Update
    {
      entityManager.merge(dbEnumTypeItem);
    }
    touchEnumType(enumTypeItem.getEnumTypeId());
    dbEnumTypeItem.copyTo(enumTypeItem);
    return enumTypeItem;
  }

  @Override
  public boolean removeEnumTypeItem(String enumTypeItemId)
  {
    LOGGER.log(Level.INFO, "removeEnumTypeItem {0}", new String[]{enumTypeItemId});

    if (enumTypeItemId == null)
      throw new WebServiceException("dic:ENUMTYPEITEMID_IS_MANDATORY");

    try
    {
      DBEnumTypeItem dbEnumTypeItem =
        entityManager.getReference(DBEnumTypeItem.class, enumTypeItemId);
      decrementIndexes(dbEnumTypeItem.getEnumTypeId(),
        dbEnumTypeItem.getIndex() + 1, null);
      entityManager.remove(dbEnumTypeItem);
      return true;
    }
    catch (EntityNotFoundException ex)
    {
      return false;
    }
  }

  @Override
  public int countEnumTypeItems(EnumTypeItemFilter filter)
  {
    LOGGER.log(Level.INFO, "countEnumTypeItems");
    Query query = entityManager.createNamedQuery("countEnumTypeItems");
    applyEnumTypeItemFilter(query, filter);
    Number number = (Number)query.getSingleResult();
    return number.intValue();
  }

  @Override
  public List<EnumTypeItem> findEnumTypeItems(EnumTypeItemFilter filter)
  {
    LOGGER.log(Level.INFO, "findEnumTypeItems");
    List<EnumTypeItem> result = new ArrayList<EnumTypeItem>();
    boolean sorted = true;
    if (filter.getEnumTypeId() != null)
    {
      DBEnumType dbEnumType =
        entityManager.find(DBEnumType.class, filter.getEnumTypeId());
      if (dbEnumType != null)
      {
        EnumType enumType = new EnumType();
        dbEnumType.copyTo(enumType);
        sorted = enumType.isSorted();
      }
    }
    Query query = entityManager.createNamedQuery("findEnumTypeItems");
    applyEnumTypeItemFilter(query, filter);
    List<DBEnumTypeItem> dbEnumTypeItemList = query.getResultList();
    if (!sorted) //sort by label
    {
      Collections.sort(dbEnumTypeItemList, new Comparator()
        {
          public int compare(Object o1, Object o2)
          {
            DBEnumTypeItem i1 = (DBEnumTypeItem)o1;
            DBEnumTypeItem i2 = (DBEnumTypeItem)o2;
            String label1 = (i1.getLabel() == null ? "" : i1.getLabel());
            String label2 = (i2.getLabel() == null ? "" : i2.getLabel());
            return label1.compareToIgnoreCase(label2);
          }
        }
      );
    }
    int firstResult = filter.getFirstResult();
    int maxResults = filter.getMaxResults();
    if (maxResults == 0) maxResults = Integer.MAX_VALUE;
    int index = 0;
    int added = 0;
    for (DBEnumTypeItem dbEnumTypeItem : dbEnumTypeItemList)
    {
      if (index++ >= firstResult && added < maxResults)
      {
        EnumTypeItem enumTypeItem = new EnumTypeItem();
        dbEnumTypeItem.copyTo(enumTypeItem);
        result.add(enumTypeItem);
        if (++added == maxResults) return result;
      }
    }
    return result;
  }

  // ********** private methods **********

  private void applyEnumTypeFilter(Query query, EnumTypeFilter filter)
  {
    query.setParameter("enumTypeId", listToString(filter.getEnumTypeId()));
    query.setParameter("superEnumTypeId", filter.getSuperEnumTypeId());
    query.setParameter("name", conditionalUpperCase(filter.getName(), false));
  }

  private void applyEnumTypeItemFilter(Query query, EnumTypeItemFilter filter)
  {
    query.setParameter("enumTypeId", filter.getEnumTypeId());
    query.setParameter("label", conditionalUpperCase(filter.getLabel(), false));
    query.setParameter("description", conditionalUpperCase(filter.getDescription(), false));
    query.setParameter("value", filter.getValue());
  }

  private int processIndexes(EnumTypeItem enumTypeItem, int currentIndex)
  {
    String enumTypeId = enumTypeItem.getEnumTypeId();
    int maxIndex = getMaxIndexInEnumType(enumTypeId);
    Integer newIndex = enumTypeItem.getIndex();
    if (currentIndex < 0) //Insert
    {
      if (newIndex == null || newIndex > maxIndex)
      {
        newIndex = maxIndex + 1;
      }
      else
      {
        incrementIndexes(enumTypeId, newIndex, null);
      }
    }
    else //Update
    {
      if (newIndex == null ||
        (newIndex > currentIndex && newIndex > maxIndex))
      {
        decrementIndexes(enumTypeId, currentIndex + 1, null);
        newIndex = maxIndex;
      }
      else if (newIndex > currentIndex && newIndex <= maxIndex)
      {
        decrementIndexes(enumTypeId, currentIndex + 1, newIndex - 1);
        newIndex = newIndex - 1;
      }
      else if(newIndex < currentIndex)
      {
        incrementIndexes(enumTypeId, newIndex, currentIndex - 1);
      }
    }
    return newIndex;
  }

  private void incrementIndexes(String enumTypeId, Integer minIndex,
    Integer maxIndex)
  {
    shiftIndexes(enumTypeId, 1, minIndex, maxIndex);
  }

  private void decrementIndexes(String enumTypeId, Integer minIndex,
    Integer maxIndex)
  {
    shiftIndexes(enumTypeId, -1, minIndex, maxIndex);
  }

  private void shiftIndexes(String enumTypeId, int increment, Integer minIndex,
    Integer maxIndex)
  {
    Query query = entityManager.createNamedQuery("shiftIndexesInEnumType");
    query.setParameter("enumTypeId", enumTypeId);
    query.setParameter("increment", increment);
    query.setParameter("minIndex", minIndex);
    query.setParameter("maxIndex", maxIndex);
    query.executeUpdate();
  }

  private int getCurrentIndex(String enumTypeItemId)
  {
    try
    {
      if (enumTypeItemId != null)
      {
        DBEnumTypeItem dbEnumTypeItem =
          entityManager.getReference(DBEnumTypeItem.class, enumTypeItemId);
        return dbEnumTypeItem.getIndex();
      }
    }
    catch (EntityNotFoundException ex)
    {
      //nothing here
    }
    return -1;
  }

  private String conditionalUpperCase(String value, boolean caseSensitive)
  {
    if (value == null || value.length() == 0)
    {
      return null;
    }
    else if (caseSensitive)
    {
      return "%" + value + "%";
    }
    else
    {
      return "%" + value.toUpperCase() + "%";
    }
  }

  private String listToString(List<String> list)
  {
    String result = TextUtils.collectionToString(list, ",");
    if (result != null) result = "," + result + ",";
    return result;
  }

  private void touchEnumType(String enumTypeId)
  {
    User user = UserCache.getUser(wsContext);
    DBEnumType dbEnumType =
      entityManager.find(DBEnumType.class, enumTypeId);
    if (dbEnumType != null)
    {
      auditEnumType(dbEnumType, user, false);
    }
  }

  private boolean isRootTypeFilter(TypeFilter filter)
  {
    // returns true if filter selects only one root type
    String typeId = filter.getTypeId();
    if (DictionaryConstants.rootTypeIds.contains(typeId))
    {
      String superTypeId = filter.getSuperTypeId();
      if (superTypeId == null || superTypeId.length() == 0)
      {
        String description = filter.getDescription();
        if (description == null || description.length() == 0)
        {
          String typePath = filter.getTypePath();
          if (typePath == null || typePath.length() == 0 ||
              typePath.equals(DictionaryConstants.TYPE_PATH_SEPARATOR +
              typeId + DictionaryConstants.TYPE_PATH_SEPARATOR + "%"))
          {
            if (filter.getFirstResult() == 0)
            {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  private void loadTypeActions(File file)
  {
    typeActionsMap.clear();
    try
    {
      BufferedReader reader = new BufferedReader(
         new InputStreamReader(new FileInputStream(file)));
      try
      {
        String line = reader.readLine();
        while (line != null)
        {
          String sLine[] = line.split(":");
          if (sLine.length == 2)
          {
            String typeId = sLine[0];
            String actions = sLine[1];
            String[] typeActionArray = actions.split(",");
            ArrayList<String> typeActionList = new ArrayList<String>();
            for (int i = 0; i < typeActionArray.length; i++)
            {
              typeActionList.add(typeActionArray[i].trim());
            }
            typeActionsMap.put(typeId, typeActionList);
          }
          line = reader.readLine();
        }
      }
      finally
      {
        reader.close();
      }
    }
    catch (IOException ex)
    {
    }
  }

  private List<DBPropertyDefinition> loadPropertyDefinitionList(String typeId)
  {
    Query query = entityManager.createNamedQuery("findPropertyDefinition");
    query.setParameter("typeId", typeId);
    return query.getResultList();
  }

  private List<DBAccessControl> loadAccessControlList(String typeId)
  {
    Query query = entityManager.createNamedQuery("findAccessControl");
    query.setParameter("typeId", typeId);
    return query.getResultList();
  }

  private void storePropertyDefinitionList(Type type,
    List<DBPropertyDefinition> dbPropertyDefinitionList)
  {
    String typeId = type.getTypeId();
    ArrayList<PropertyDefinition> pdList = new ArrayList<>();
    pdList.addAll(type.getPropertyDefinition());

    if (dbPropertyDefinitionList != null)
    {
      for (DBPropertyDefinition dbPropertyDefinition : dbPropertyDefinitionList)
      {
        PropertyDefinition propertyDefinition =
           extractPropertyDefinition(dbPropertyDefinition, pdList);
        if (propertyDefinition == null)
        {
          // remove property
          entityManager.remove(dbPropertyDefinition);
        }
        else
        {
          // update property
          dbPropertyDefinition.copyFrom(propertyDefinition);
          entityManager.merge(dbPropertyDefinition);
        }
      }
    }
    // insert new properties
    for (PropertyDefinition propertyDefinition : pdList)
    {
      DBPropertyDefinition dbPropertyDefinition =
        new DBPropertyDefinition(propertyDefinition);
      dbPropertyDefinition.setTypeId(typeId);
      entityManager.persist(dbPropertyDefinition);
    }
  }

  private void storeAccessControlList(Type type,
    List<DBAccessControl> dbAccessControlList)
  {
    String typeId = type.getTypeId();
    ArrayList<AccessControl> ACL = new ArrayList<>();
    ACL.addAll(type.getAccessControl());

    if (dbAccessControlList != null)
    {
      for (DBAccessControl dbAccessControl : dbAccessControlList)
      {
        AccessControl accessControl =
           extractAccessControl(dbAccessControl, ACL);
        if (accessControl == null)
        {
          // remove ac
          entityManager.remove(dbAccessControl);
        }
        else
        {
          // update ac
          dbAccessControl.copyFrom(accessControl);
          entityManager.merge(dbAccessControl);
        }
      }
    }
    // insert new ac
    for (AccessControl accessControl : ACL)
    {
      DBAccessControl dbAccessControl =
        new DBAccessControl(accessControl);
      dbAccessControl.setTypeId(typeId);
      entityManager.persist(dbAccessControl);
    }
  }

  private void applyFilter(Query query, TypeFilter filter, User user)
  {
    query.setParameter("typeId", filter.getTypeId());
    query.setParameter("superTypeId", filter.getSuperTypeId());
    query.setParameter("typePath", filter.getTypePath());
    query.setParameter("description", addWildcards(filter.getDescription()));
    query.setParameter("action", filter.getAction());
    query.setParameter("userRoles", "," + user.getRolesString() + ",");
    query.setParameter("minChangeDateTime", filter.getMinChangeDateTime());
    query.setParameter("maxChangeDateTime", filter.getMaxChangeDateTime());
  }

  private boolean canUserCreateTypeDefinition(User user, Type type)
  {
    if (user.isInRole(DictionaryConstants.DIC_ADMIN_ROLE)) return true;
    String superTypeId = type.getSuperTypeId();
    if (superTypeId == null) return false;

    // check for 'DeriveDefinition' role in super Type
    List<DBAccessControl> dbAccessControlList =
      loadAccessControlList(superTypeId);
    return canUserDoAction(user,
      DictionaryConstants.DERIVE_DEFINITION_ACTION, dbAccessControlList);
  }

  private boolean canUserModifyTypeDefinition(User user, Type type,
    DBType dbType, List<DBAccessControl> dbAccessControlList)
  {
    if (user.isInRole(DictionaryConstants.DIC_ADMIN_ROLE)) return true;
    String dbSuperTypeId = dbType.getSuperTypeId();
    if (dbSuperTypeId == null) return false;
    String superTypeId = type.getSuperTypeId();
    if (superTypeId == null) return false;
    return canUserDoAction(user,
      DictionaryConstants.MODIFY_DEFINITION_ACTION, dbAccessControlList);
  }

  private boolean canUserRemoveTypeDefinition(User user, DBType dbType)
  {
    if (user.isInRole(DictionaryConstants.DIC_ADMIN_ROLE)) return true;
    String dbSuperTypeId = dbType.getSuperTypeId();
    if (dbSuperTypeId == null) return false;

    String typeId = dbType.getTypeId();
    List<DBAccessControl> dbAccessControlList = loadAccessControlList(typeId);
    return canUserDoAction(user, DictionaryConstants.MODIFY_DEFINITION_ACTION,
      dbAccessControlList);
  }

  private void checkType(Type type)
  {
    String typeId = type.getTypeId();
    if (typeId.length() > MAX_TYPEID_LENGTH)
      throw new WebServiceException("dic:TYPEID_TOO_LONG");

    if (!isValidIdentifier(typeId))
      throw new WebServiceException("dic:INVALID_TYPEID");

    String description = type.getDescription();
    if (description == null || description.trim().length() == 0)
      throw new WebServiceException("dic:DESCRIPTION_IS_MANDATORY");

    if (description.length() > MAX_TYPEDESCRIPTION_LENGTH)
      throw new WebServiceException("dic:DESCRIPTION_TOO_LONG");
  }

  private void checkEnumType(EnumType enumType)
  {
    String enumTypeId = enumType.getEnumTypeId();
    if (enumTypeId != null)
    {
      if (enumTypeId.length() > MAX_ENUMTYPEID_LENGTH)
        throw new WebServiceException("dic:ENUMTYPEID_TOO_LONG");
      if (!isValidIdentifier(enumTypeId))
        throw new WebServiceException("dic:INVALID_ENUMTYPEID");
    }
    else
    {
      throw new WebServiceException("dic:ENUMTYPEID_IS_MANDATORY");
    }
    String superEnumTypeId = enumType.getSuperEnumTypeId();
    if (superEnumTypeId != null)
    {
      if (superEnumTypeId.length() > MAX_ENUMTYPEID_LENGTH)
        throw new WebServiceException("dic:SUPERENUMTYPEID_TOO_LONG");
      if (!isValidIdentifier(superEnumTypeId))
        throw new WebServiceException("dic:INVALID_SUPERENUMTYPEID");
      DBEnumType dbSuperEnumType = entityManager.find(DBEnumType.class,
        superEnumTypeId);
      if (dbSuperEnumType == null)
         throw new WebServiceException("dic:SUPER_TYPE_NOT_FOUND");
    }
    String name = enumType.getName();
    if (name == null || name.trim().length() == 0)
      throw new WebServiceException("dic:NAME_IS_MANDATORY");
    if (name.length() > MAX_ENUMTYPE_NAME_LENGTH)
      throw new WebServiceException("dic:NAME_TOO_LONG");
  }

  private void checkEnumTypeItem(EnumTypeItem enumTypeItem)
  {
    String enumTypeId = enumTypeItem.getEnumTypeId();
    if (enumTypeId != null)
    {
      if (enumTypeId.length() > MAX_ENUMTYPEID_LENGTH)
        throw new WebServiceException("dic:ENUMTYPEID_TOO_LONG");
      if (!isValidIdentifier(enumTypeId))
        throw new WebServiceException("dic:INVALID_ENUMTYPEID");
    }
    else
    {
      throw new WebServiceException("dic:ENUMTYPEID_IS_MANDATORY");
    }
    String label = enumTypeItem.getLabel();
    if (label != null)
    {
      if (label.length() > MAX_ENUMTYPEITEM_LABEL_LENGTH)
        throw new WebServiceException("dic:LABEL_TOO_LONG");
    }
    String description = enumTypeItem.getDescription();
    if (description != null)
    {
      if (description.length() > MAX_ENUMTYPEITEM_DESCRIPTION_LENGTH)
        throw new WebServiceException("dic:DESCRIPTION_TOO_LONG");
    }
    Integer index = enumTypeItem.getIndex();
    if (index != null)
    {
      if (index <= 0)
        throw new WebServiceException("dic:INVALID_INDEX");
      else if (String.valueOf(index).length() > MAX_ENUMTYPEITEM_INDEX_LENGTH)
        throw new WebServiceException("dic:INDEX_TOO_LONG");
    }
    String value = enumTypeItem.getValue();
    if (value != null && value.length() > 0)
    {
      if (value.length() > MAX_ENUMTYPEITEM_VALUE_LENGTH)
        throw new WebServiceException("dic:VALUE_TOO_LONG");
      checkItemValue(enumTypeItem);
    }
  }

  private void checkItemValue(EnumTypeItem enumTypeItem)
  {
    PropertyType itemType = null;
    DBEnumType dbEnumType = entityManager.find(DBEnumType.class,
      enumTypeItem.getEnumTypeId());
    if (dbEnumType == null)
    {
      throw new WebServiceException("dic:ENUM_TYPE_NOT_FOUND");
    }
    else
    {
      EnumType enumType = new EnumType();
      dbEnumType.copyTo(enumType);
      itemType = enumType.getItemType();
    }
    try
    {
      String itemValue = enumTypeItem.getValue();
      if (PropertyType.BOOLEAN.equals(itemType))
      {
        if (!"true".equals(itemValue) && !"false".equals(itemValue))
          throw new Exception();
      }
      else if (PropertyType.DATE.equals(itemType))
      {
        SimpleDateFormat sysFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        sysFormat.parse(itemValue);
      }
      else if (PropertyType.NUMERIC.equals(itemType))
      {
        Double.parseDouble(itemValue);
      }
    }
    catch (Exception ex)
    {
      throw new WebServiceException("dic:INVALID_VALUE_TYPE");
    }
  }

  private int getMaxIndexInEnumType(String enumTypeId)
  {
    Query query = entityManager.createNamedQuery("getMaxIndexInEnumType");
    query.setParameter("enumTypeId", enumTypeId);
    Object value = query.getSingleResult();
    return (value == null ? 0 : ((Number)value).intValue());
  }

  private void setSuperType(DBType dbType, User user, boolean isNew)
  {
    // check superType
    String typeId = dbType.getTypeId();
    String superTypeId = dbType.getSuperTypeId();
    if (isNew) // new Type
    {
      if (superTypeId == null) // root Type
      {
        dbType.setTypePath(DictionaryConstants.TYPE_PATH_SEPARATOR + typeId +
          DictionaryConstants.TYPE_PATH_SEPARATOR);
      }
      else // derived Type
      {
        if (superTypeId.equals(typeId))
          throw new WebServiceException("dic:RECURSIVE_TYPE_DEFINITION");

        DBType dbSuperType = entityManager.find(DBType.class, superTypeId);
        if (dbSuperType == null || dbSuperType.isRemoved())
             throw new WebServiceException("dic:SUPER_TYPE_NOT_FOUND");

          dbType.setTypePath(dbSuperType.getTypePath() +
            typeId + DictionaryConstants.TYPE_PATH_SEPARATOR);
      }
    }
    else // Type update
    {
      String oldTypePath = dbType.getTypePath();
      if (superTypeId == null) // root Type
      {
        String newTypePath = DictionaryConstants.TYPE_PATH_SEPARATOR + typeId +
          DictionaryConstants.TYPE_PATH_SEPARATOR;
        if (!oldTypePath.equals(newTypePath))
        {
          dbType.setTypePath(newTypePath);

          // change derived types path
          changeTypePaths(oldTypePath, newTypePath);
        }
      }
      else // derived Type
      {
        if (oldTypePath != null && oldTypePath.endsWith(
          DictionaryConstants.TYPE_PATH_SEPARATOR + superTypeId +
          DictionaryConstants.TYPE_PATH_SEPARATOR +
          typeId + DictionaryConstants.TYPE_PATH_SEPARATOR))
        {
          // no type path change, nothing to do
        }
        else
        {
          // type path change, get new superType
          DBType dbSuperType = entityManager.find(DBType.class, superTypeId);
          if (dbSuperType == null || dbSuperType.isRemoved())
            throw new WebServiceException("dic:SUPER_TYPE_NOT_FOUND");

          String superTypePath = dbSuperType.getTypePath();
          if (superTypePath.indexOf(
            DictionaryConstants.TYPE_PATH_SEPARATOR + typeId +
            DictionaryConstants.TYPE_PATH_SEPARATOR) != -1)
            throw new WebServiceException("dic:RECURSIVE_TYPE_DEFINITION");

          String newTypePath = superTypePath + typeId +
            DictionaryConstants.TYPE_PATH_SEPARATOR;
          dbType.setTypePath(newTypePath);

          // change derived types path
          changeTypePaths(oldTypePath, newTypePath);
        }
      }
    }
  }

  private void auditType(DBType dbType, User user, boolean isNew)
  {
    Date date = new Date();
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    String dateTime = df.format(date);
    if (isNew)
    {
      dbType.setCreationDateTime(dateTime);
      dbType.setCreationUserId(user.getUserId());
    }
    dbType.setChangeDateTime(dateTime);
    dbType.setChangeUserId(user.getUserId());
  }

  private void auditEnumType(DBEnumType dbEnumType, User user, boolean isNew)
  {
    Date date = new Date();
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    String dateTime = df.format(date);
    if (isNew)
    {
      dbEnumType.setCreationDateTime(dateTime);
      dbEnumType.setCreationUserId(user.getUserId());
    }
    dbEnumType.setChangeDateTime(dateTime);
    dbEnumType.setChangeUserId(user.getUserId());
  }

  private void changeTypePaths(String oldTypePath, String newTypePath)
  {
    Query query = entityManager.createNamedQuery("changeTypePaths");
    query.setParameter("newTypePath", newTypePath == null ? "" : newTypePath);
    query.setParameter("oldTypePath", oldTypePath);
    query.setParameter("oldTypePathPattern",
      oldTypePath.replaceAll("_", "\\\\_") + "%");
    query.executeUpdate();
  }

  private PropertyDefinition extractPropertyDefinition(
    DBPropertyDefinition dbPropertyDefinition, List<PropertyDefinition> pdList)
  {
    PropertyDefinition pd = null;
    int i = 0;
    while (i < pdList.size() && pd == null)
    {
      PropertyDefinition pdi = pdList.get(i);
      if (pdi.getName().equals(dbPropertyDefinition.getName()))
      {
        pd = pdList.remove(i);
      }
      else i++;
    }
    return pd;
  }

  private AccessControl extractAccessControl(
    DBAccessControl dbAccessControl, List<AccessControl> ACL)
  {
    AccessControl ac = null;
    int i = 0;
    while (i < ACL.size() && ac == null)
    {
      AccessControl aci = ACL.get(i);
      if (aci.getRoleId().equals(dbAccessControl.getRoleId()) &&
          aci.getAction().equals(dbAccessControl.getAction()))
      {
        ac = ACL.remove(i);
      }
      else i++;
    }
    return ac;
  }

  private boolean canUserDoAction(User user, String action, List acl)
  {
    return DictionaryUtils.canPerformAction(action, user.getRoles(), acl);
  }

  private boolean isValidIdentifier(String id)
  {
    boolean valid = true;
    int i = 0;
    while (i < id.length())
    {
      char ch = id.charAt(i);
      if (!Character.isJavaIdentifierPart(ch)) valid = false;
      i++;
    }
    return valid;
  }

  private String addWildcards(String value)
  {
    if (value == null || value.length() == 0) return null;
    return "%" + value.toUpperCase() + "%";
  }

  private void addNominalRoles(User user, Type typeObject)
  {
    AccessControl ac = new AccessControl();
    ac.setRoleId(SecurityConstants.SELF_ROLE_PREFIX + user.getUserId().trim() +
      SecurityConstants.SELF_ROLE_SUFFIX);
    ac.setAction(DictionaryConstants.MODIFY_DEFINITION_ACTION);
    typeObject.getAccessControl().add(ac);
    ac = new AccessControl();
    ac.setRoleId(SecurityConstants.SELF_ROLE_PREFIX + user.getUserId().trim() +
      SecurityConstants.SELF_ROLE_SUFFIX);
    ac.setAction(DictionaryConstants.DERIVE_DEFINITION_ACTION);
    typeObject.getAccessControl().add(ac);
  }

  private void createRootTypes()
  {
    String adminUserId = MatrixConfig.getProperty("adminCredentials.userId");
    String now = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");
    HashSet<String> rootTypeIds = DictionaryConstants.rootTypeIds;
    for (String rootTypeId : rootTypeIds)
    {
      Type type = RootTypeFactory.getRootType(rootTypeId);

      String typeId = type.getTypeId();
      LOGGER.log(Level.INFO, "Looking for root type [{0}]", typeId);
      DBType dbType = entityManager.find(DBType.class, typeId);
      if (dbType == null) // does not exist
      {
        LOGGER.log(Level.INFO, "Creating root type [{0}]", typeId);
        dbType = new DBType(type);
        dbType.setTypePath(DictionaryConstants.TYPE_PATH_SEPARATOR + typeId +
          DictionaryConstants.TYPE_PATH_SEPARATOR);
        dbType.setCreationDateTime(now);
        dbType.setChangeDateTime(now);
        dbType.setCreationUserId(adminUserId);
        dbType.setChangeUserId(adminUserId);
        entityManager.persist(dbType);
        entityManager.flush();

        List<PropertyDefinition> pdList = type.getPropertyDefinition();
        for (PropertyDefinition propertyDefinition : pdList)
        {
          DBPropertyDefinition dbPropertyDefinition =
            new DBPropertyDefinition(propertyDefinition);
          dbPropertyDefinition.setTypeId(typeId);
          entityManager.persist(dbPropertyDefinition);
        }
        entityManager.flush();
      }
    }
  }
}
