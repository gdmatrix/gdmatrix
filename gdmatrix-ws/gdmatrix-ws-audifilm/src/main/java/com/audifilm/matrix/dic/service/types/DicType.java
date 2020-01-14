package com.audifilm.matrix.dic.service.types;

import com.audifilm.matrix.dic.service.DictionaryManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.Type;
import org.matrix.util.WSEndpoint;


abstract public class DicType<T extends DicTypeInterface> implements DicTypeInterface<DicTypeInterface>
{
  static public String PREFIX_SEPARATOR = "_";

  static String [] typeActions = new String[] {
          DictionaryConstants.READ_ACTION,
          DictionaryConstants.PRINT_ACTION,
          DictionaryConstants.WRITE_ACTION,
          DictionaryConstants.CREATE_ACTION,
          DictionaryConstants.DERIVE_DEFINITION_ACTION,
          DictionaryConstants.MODIFY_DEFINITION_ACTION
        };

  abstract public List<DicTypeInterface> getChildDicTypes();
  
  

  DicTypeInterface parent;

  public <T> DicType()
  {
    throw new RuntimeException();
  }
  
  public <T> DicType(DicTypeInterface parent)
  {
    this.parent = parent;
  }

  public T getParentDicType()
  {
    return (T)parent;
  }

  
  public List<String> getTypeActions(DictionaryManager dicManager, String typeId)
  {
    return Arrays.asList(typeActions);
  }

  abstract public List<String> listModifiedTypes(DictionaryManager dicManager, String dateTime1, String dateTime2);


  public String toGlobalId(WSEndpoint endpoint, String localTypeId)
  {
    return endpoint.toGlobalId(Type.class, localTypeId);
  }

  public String toLocalId(WSEndpoint endpoint, String globalId)
  {
    return endpoint.toLocalId(Type.class, globalId);
  }

  public DicTypeInterface getChildDicType(String typeId) {
    for(DicTypeInterface child : getChildDicTypes())
    {
      if (child.isThisDicType(typeId)) return child;
    }
    return null;
  }


  public String getGlobalTypePath(WSEndpoint endpoint, String typeId)
  {
    T myParent = getParentDicType();
    String parentPath  = ((myParent==null)?"":myParent.getGlobalTypePath(endpoint, typeId));
    return parentPath + (parentPath.endsWith("/")?"":"/") + toGlobalId(endpoint, typeId) + "/";
  }

  public String getLocalTypePath(WSEndpoint endpoint, String typeId)
  {
    T myParent = getParentDicType();
    String parentPath  = ((myParent==null)?"":myParent.getLocalTypePath(endpoint, typeId));
    return parentPath + (parentPath.endsWith("/")?"":"/") + toLocalId(endpoint, typeId) + "/";
  }
  public DicTypeInterface getDicTypeByPath(String typePath)
  {
    if (getLocalTypePath(null, typePath).equals(typePath)) {
      int pos = typePath.lastIndexOf("/") + 1;
      String typeId = typePath.substring(pos);
      if (isThisDicType(typeId)) return this;
      for(DicTypeInterface child : getChildDicTypes())
      {
        if (child.isThisDicType(typeId)) {
          return child;
        }
        DicTypeInterface dicTypeFound = child.getDicTypeByPath(typePath);
        if (dicTypeFound!=null) return dicTypeFound;
      }

    }
    return null;
  }

  public DicTypeInterface getDicType(String typeId)
  {
    if (isThisDicType(typeId)) return this;
    for(DicTypeInterface child : getChildDicTypes())
    {
      if (child.isThisDicType(typeId)) {
        return child;
      }

      DicTypeInterface dicTypeFound = child.getDicType(typeId);
      if (dicTypeFound!=null)
        return dicTypeFound;
    }
    return null;
  }

  public List<DicTypeInterface> findDicTypes(String filterTypeId)
  {
    if (filterTypeId == null) return Collections.emptyList();

    List<DicTypeInterface> list = new ArrayList<DicTypeInterface>();
    if (isThisDicType(filterTypeId)) {
      list.add(this);
    } else {
      for(DicTypeInterface child: getChildDicTypes())
      {
        list.addAll(child.findDicTypes(filterTypeId));
      }
    }
    return list;
  }

  public String getSuperTypeId()
  {
    DicTypeInterface myparent = getParentDicType();
    if (myparent!=null)
    {
      if (myparent instanceof DicTypeRoot)
        return ((DicTypeRoot)myparent).getRootTypeId();
      else
        return myparent.getSuperTypeId();
    }
    return "";
  }
}
