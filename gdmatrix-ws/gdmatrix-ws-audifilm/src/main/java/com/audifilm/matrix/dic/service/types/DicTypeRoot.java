package com.audifilm.matrix.dic.service.types;

import com.audifilm.matrix.dic.service.*;
import com.audifilm.matrix.util.TextUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author comasfc
 */
abstract public class DicTypeRoot<T extends DicTypeInterface> extends DicType<DicTypeInterface>
{

  static public String CHANGEDATETIME = "00000000";
  static public String CHANGEURSERID = "G5Admin";
  //static public String TYPE_PATH = DictionaryConstants.TYPE_PATH_SEPARATOR +  TYPEID + DictionaryConstants.TYPE_PATH_SEPARATOR;

  long lastModifiedTimestamp =0;

  abstract public String getRootTypeId();

  abstract public String getDescription();

  public DicTypeRoot()
  {
    super(null);
  }

  private Type loadType(DictionaryManager entityManager)
  {
    Type type = new Type();

    type.setInstantiable(true);
    type.setChangeDateTime(CHANGEDATETIME);
    type.setChangeUserId(CHANGEURSERID);
    type.setCreationDateTime(CHANGEDATETIME);
    type.setCreationUserId(CHANGEURSERID);
    type.setDescription(getDescription());
    type.setSuperTypeId(null);
    type.setTypeId(getRootTypeId());
    type.setTypePath(getGlobalTypePath(entityManager.getEndpoint(), ""));
    return type;
  }

  public int countTypes(DictionaryManager entityManager, TypeFilter filter)
  {
    return match(entityManager.getEndpoint(), filter) ? 1 : 0;
  }

  public Type loadType(DictionaryManager entityManager, String id)
  {
    return loadType(entityManager);
  }

  public Type storeType(DictionaryManager entityManager, Type type)
  {
    return loadType(entityManager);
  }

  @Override
  public List<String> getTypeActions(DictionaryManager entityManager, String typeId)
  {
    if (!typeId.equals(getRootTypeId()))
    {
      return null;
    }

    List<String> actionsList = new ArrayList<String>();
    actionsList.add(DictionaryConstants.DERIVE_DEFINITION_ACTION);
    return actionsList;

  }

  public List<Type> findTypes(DictionaryManager entityManager, TypeFilter filter)
  {
    List<Type> listType = new ArrayList<Type>(1);
    if (match(entityManager.getEndpoint(), filter))
    {
      listType.add(loadType(entityManager));
    }
    return listType;
  }

  boolean match(WSEndpoint endpoint, TypeFilter filter)
  {
    return (filter.getDescription() == null || 
            (TextUtil.matchesFilter(filter.getDescription(), getDescription()))) 
            && (filter.getSuperTypeId() == null || 
                  (filter.getSuperTypeId().equals(""))) 
            && (filter.getTypeId() == null || 
                  (filter.getTypeId().equals(getRootTypeId()))) 
            && (filter.getTypePath() == null ||
                  (TextUtil.matchesFilter(filter.getTypePath(), getGlobalTypePath(endpoint, ""))));
  }

  @Override
  public String getGlobalTypePath(WSEndpoint endpoint, String typeId)
  {
    return "/" + getRootTypeId() + "/";
  }

  @Override
  public String getLocalTypePath(WSEndpoint endpoint, String typeId)
  {
    return "/" + getRootTypeId() + "/";
  }

  @Override
  public String toGlobalId(WSEndpoint endpoint, String localTypeId)
  {
    return getRootTypeId();
  }

  @Override
  public String toLocalId(WSEndpoint endpoint, String globalId)
  {
    return getRootTypeId();
  }

  @Override
  public List<DicTypeInterface> findDicTypes(String filterTypeId)
  {
    if (filterTypeId == null)
    {
      return Collections.emptyList();
    }

    List<DicTypeInterface> list = new ArrayList<DicTypeInterface>();
    if (filterTypeId.equals(getRootTypeId()))
    {
      list.add(this);
    }
    else
    {
      for (DicTypeInterface child : getChildDicTypes())
      {
        list.addAll(child.findDicTypes(filterTypeId));
      }
    }
    return list;
  }

  public List<DicTypeInterface> findDicTypesByPath(String filterPath)
  {
    if (filterPath == null)
    {
      return Collections.emptyList();
    }
    List<DicTypeInterface> list = new ArrayList<DicTypeInterface>();
    if (filterPath.equals("") || filterPath.equals("/") || filterPath.equals("/" + getRootTypeId()) || filterPath.equals("/" + getRootTypeId() + "/%"))
    {
      list.add(this);
    }
    if (filterPath.startsWith("/" + getRootTypeId() + "/"))
    {
      for (DicTypeInterface child : getChildDicTypes())
      {
        list.addAll(child.findDicTypesByPath(filterPath));
      }
    }
    return list;
  }

  public boolean isThisDicType(String typeId)
  {
    return typeId != null && typeId.equals(getRootTypeId());
  }

  @Override
  public String getSuperTypeId()
  {
    return getRootTypeId();
  }

  @Override
  public List<String> listModifiedTypes(DictionaryManager dicManager, String dateTime1, String dateTime2)
  {
    
    if (dicManager.getFixedProperties().isModified(lastModifiedTimestamp)
           || dicManager.getFixedProperties().isModified(dateTime1, dateTime2))   {
      lastModifiedTimestamp = System.currentTimeMillis();
      return Collections.singletonList(getRootTypeId());
    }
    return Collections.emptyList();
  }
}
