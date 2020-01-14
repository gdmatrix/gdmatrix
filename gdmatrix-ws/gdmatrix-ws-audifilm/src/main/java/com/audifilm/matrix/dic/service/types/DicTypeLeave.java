package com.audifilm.matrix.dic.service.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.matrix.dic.Type;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author comasfc
 */
abstract public class DicTypeLeave<P extends DicTypeInterface> extends DicType<P>
{

  abstract public String getTypeIdPrefix();

  public <P> DicTypeLeave(DicTypeInterface parent)
  {
    super(parent);
  }

  public List<DicTypeInterface> getChildDicTypes()
  {
    return Collections.emptyList();
  }

  @Override
  public DicTypeInterface getChildDicType(String typeId)
  {
    return null;
  }

  @Override
  public String toGlobalId(WSEndpoint endpoint, String typeId)
  {
    if (typeId == null || endpoint == null)
    {
      return typeId;
    }

    if (typeId != null && !typeId.startsWith(getTypeIdPrefix()))
    {
      return endpoint.toGlobalId(Type.class, getTypeIdPrefix() + typeId);
    }
    else
    {
      return endpoint.toGlobalId(Type.class, typeId);
    }
  }

  @Override
  public String toLocalId(WSEndpoint endpoint, String globalId)
  {
    if (globalId == null || globalId.equals("") || endpoint == null)
    {
      return globalId;
    }

    String localId = endpoint.toLocalId(Type.class, globalId);
    if (localId != null && localId.startsWith(getTypeIdPrefix()))
    {
      return localId.substring(getTypeIdPrefix().length());
    }
    else
    {
      return localId;
    }

  }

  public DicTypeInterface getChildDicTypeByPath(String typePath)
  {
    return null;
  }

  public boolean isThisDicType(String typeId)
  {
    return typeId != null && typeId.startsWith(getTypeIdPrefix()) && (typeId.length() > getTypeIdPrefix().length());
  }

  public List<DicTypeInterface> findDicTypesByPath(String filterPath)
  {
    if (filterPath == null)
    {
      return null;
    }

    int pos = filterPath.lastIndexOf("/") + 1;
    String leave = filterPath.substring(pos);

    List<DicTypeInterface> list = new ArrayList<DicTypeInterface>();
    if ("%".equals(leave) || isThisDicType(leave))
    {
      list.add(this);
    }
    return list;
  }
}
