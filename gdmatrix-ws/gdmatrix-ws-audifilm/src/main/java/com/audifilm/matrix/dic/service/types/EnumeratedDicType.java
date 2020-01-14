package com.audifilm.matrix.dic.service.types;

import com.audifilm.matrix.dic.service.DictionaryManager;
import com.audifilm.matrix.util.TextUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author comasfc
 */
abstract public class EnumeratedDicType<P extends DicTypeInterface> extends DicTypeLeave<P>
{

  public abstract TypeEnumElement getElement(String id);
  public abstract TypeEnumElement[] getElements();

  long lastModifiedTimestamp = 0;

  public <P> EnumeratedDicType(DicTypeInterface parent)  
  {
    super(parent);
  }

  public Type loadType(DictionaryManager dicManager, String id)
  {
    TypeEnumElement enumType = getElement(id);
    if (enumType == null)
    {
      return null;
    }

    Type type = new Type();

    type.setTypeId(dicManager.getEndpoint().toGlobalId(Type.class, enumType.getTypeId()));
    type.setSuperTypeId((getParentDicType()).toGlobalId(dicManager.getEndpoint(), ""));
    type.setTypePath(getGlobalTypePath(dicManager.getEndpoint(), id));
    type.setDescription(enumType.getDescription());
    type.setInstantiable(true);

    type.getAccessControl().addAll(getAccessControlList(dicManager, id));

    return type;
  }

  public Type storeType(DictionaryManager dicManager, Type type)
  {
    
    return loadType(dicManager, type.getTypeId());
  }

  public int countTypes(DictionaryManager dicManager, TypeFilter filter)
  {
    WSEndpoint endpoint = dicManager.getEndpoint();
    int count = 0;
    for (TypeEnumElement type : getElements())
    {
      if (matches(endpoint, filter, type))
      {
        count++;
      }
    }
    return count;
  }

  public List<Type> findTypes(DictionaryManager dicManager, TypeFilter globalFilter)
  {
    WSEndpoint endpoint = dicManager.getEndpoint();
    TypeFilter filter = endpoint.toLocal(TypeFilter.class, globalFilter);

    List<Type> list = new ArrayList<Type>();
    for (TypeEnumElement element : getElements())
    {
      if (matches(endpoint, filter, element))
      {
        Type type = loadType(dicManager, element.getTypeId());
        list.add(type);
      }
    }
    return list;
  }

  boolean matches(WSEndpoint endpoint, TypeFilter filter, TypeEnumElement element)
  {
    return (TextUtil.matchesFilter(filter.getDescription(), element.getDescription())) && (TextUtil.matchesFilter(filter.getSuperTypeId(), getSuperTypeId())) && (TextUtil.matchesFilter(filter.getTypeId(), element.getTypeId())) && (TextUtil.matchesFilter(filter.getTypePath(), getGlobalTypePath(endpoint, element.getTypeId())));
  }

  @Override
  public List<String> listModifiedTypes(DictionaryManager dicManager, String dateTime1, String dateTime2)
  {

    if (
            dicManager.getFixedProperties().isModified(lastModifiedTimestamp)
            ||
            dicManager.getFixedProperties().isModified(dateTime1, dateTime2))
    {
      List<String> list = new ArrayList<String>();
      for (TypeEnumElement element : getElements())
      {
        list.add(dicManager.getEndpoint().toGlobalId(Type.class, element.getTypeId()));
      }
      lastModifiedTimestamp = System.currentTimeMillis();
      return list;
    }
    return Collections.emptyList();
  }
}
