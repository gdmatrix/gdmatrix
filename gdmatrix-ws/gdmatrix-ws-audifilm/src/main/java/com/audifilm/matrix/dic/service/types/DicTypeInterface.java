package com.audifilm.matrix.dic.service.types;

import com.audifilm.matrix.dic.service.DictionaryManager;
import java.util.List;
import org.matrix.security.AccessControl;
import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author comasfc
 */
public interface DicTypeInterface<PARENT extends DicTypeInterface>
{
 
  public Type loadType(DictionaryManager dicManager, String id);
  public Type storeType(DictionaryManager dicManager, Type type);
  public int countTypes(DictionaryManager dicManager, TypeFilter filter);
  public List<Type> findTypes(DictionaryManager dicManager, TypeFilter filter);
  public List<String> getTypeActions(DictionaryManager dicManager, String typeId);
  public List<String> listModifiedTypes(DictionaryManager dicManager, String dateTime1, String dateTime2);
  public List<AccessControl> getAccessControlList(DictionaryManager dicManager, String typeId);
  public String getGlobalTypePath(WSEndpoint endpoint, String typeId);
  public String getLocalTypePath(WSEndpoint endpoint, String typeId);
  public String toGlobalId(WSEndpoint endpoint, String localTypeId);
  public String toLocalId(WSEndpoint endpoint, String globalId);
  public String getSuperTypeId();

  public PARENT getParentDicType();
  public List<DicTypeInterface> findDicTypes(String fiterTypeId);
  public DicTypeInterface getDicType(String typeId);
  public List<DicTypeInterface> findDicTypesByPath(String filterPath);
  public DicTypeInterface getDicTypeByPath(String typePath);
  public List<DicTypeInterface> getChildDicTypes();

  public boolean isThisDicType(String typeId);
}
