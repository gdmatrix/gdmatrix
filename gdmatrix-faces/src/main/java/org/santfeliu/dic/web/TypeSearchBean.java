package org.santfeliu.dic.web;

import java.util.List;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.obj.BasicSearchBean;

/**
 *
 * @author realor
 */
@CMSManagedBean
public class TypeSearchBean extends BasicSearchBean
{
  private TypeFilter filter = new TypeFilter();
  private String rootTypeId;

  public TypeFilter getFilter()
  {
    return filter;
  }

  public void setFilter(TypeFilter filter)
  {
    this.filter = filter;
  }

  public String getRootTypeId()
  {
    return rootTypeId;
  }

  public void setRootTypeId(String rootTypeId)
  {
    this.rootTypeId = rootTypeId;
  }

  @Override
  public int countResults()
  {
    try
    {
      String typePath = (rootTypeId != null && rootTypeId.trim().length() > 0) ?
       DictionaryConstants.TYPE_PATH_SEPARATOR + rootTypeId +
       DictionaryConstants.TYPE_PATH_SEPARATOR + "%" : null;
      filter.setTypePath(typePath);
      DictionaryManagerPort port = DictionaryConfigBean.getPort();
      return port.countTypes(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  @Override
  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      String typePath = (rootTypeId != null && rootTypeId.trim().length() > 0) ?
       DictionaryConstants.TYPE_PATH_SEPARATOR + rootTypeId +
       DictionaryConstants.TYPE_PATH_SEPARATOR + "%" : null;
      filter.setTypePath(typePath);
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      DictionaryManagerPort port = DictionaryConfigBean.getPort();
      return port.findTypes(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  @Override
  @CMSAction
  public String show()
  {
    return "type_search";
  }

  public boolean isLeafType()
  {
    Type row = (Type)getValue("#{row}");
    TypeCache cache = TypeCache.getInstance();
    org.santfeliu.dic.Type type = cache.getType(row.getTypeId());
    if (type != null) return type.isLeaf();
    return false;
  }

  public String findDerivedTypes()
  {
    Type row = (Type)getValue("#{row}");
    String typeId = row.getTypeId();
    filter.setSuperTypeId(typeId);
    filter.setTypeId(null);
    filter.setDescription(null);
    return search();
  }

  public String showInTree()
  {
    Type row = (Type)getValue("#{row}");
    String typeId = row.getTypeId();
    TypeBean typeBean = (TypeBean)getBean("typeBean");
    typeBean.setObjectId(typeId);
    TypeTreeBean typeTreeBean = (TypeTreeBean)getBean("typeTreeBean");
    return typeTreeBean.expandType(typeId);
  }

  public String findDerivedTypesFromPath()
  {
    String typeId = (String)getValue("#{typeId}");
    filter.setSuperTypeId(typeId);
    filter.setTypeId(null);
    filter.setDescription(null);
    return search();
  }

  public boolean isNavigationEnabled()
  {
    String superTypeId = filter.getSuperTypeId();
    return (superTypeId != null && superTypeId.trim().length() > 0 &&
      !superTypeId.equals("-"));
  }

  public List<String> getTypePathList()
  {
    TypeCache cache = TypeCache.getInstance();
    org.santfeliu.dic.Type superType = cache.getType(filter.getSuperTypeId());
    if (superType != null) return superType.getTypePathList();
    return null;
  }

  public String showType()
  {
    return getControllerBean().showObject("Type",
      (String)getValue("#{row.typeId}"));
  }

  public String selectType()
  {
    Type row = (Type)getValue("#{row}");
    String typeId = row.getTypeId();
    return getControllerBean().select(typeId);
  }
}
