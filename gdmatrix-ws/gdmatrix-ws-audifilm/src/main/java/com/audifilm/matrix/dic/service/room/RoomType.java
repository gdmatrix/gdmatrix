package com.audifilm.matrix.dic.service.room;


import com.audifilm.matrix.dic.service.DictionaryManager;
import com.audifilm.matrix.dic.service.types.DicTypeTaulaConfigMatrix;
import java.util.ArrayList;
import java.util.List;
import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;

/**
 *
 * @author blanquepa
 */
public class RoomType extends DicTypeTaulaConfigMatrix<Room>
{

  final static public String TCCODI1 = "ROOMTYPE";


  public RoomType(Room parent)
  {
    super(parent);
  }

  @Override
  public Type loadType(DictionaryManager dictionaryManager, String composedIdType)
  {
    return super.loadType(dictionaryManager, composedIdType);
  }

  @Override
  public List<Type> findTypes(DictionaryManager dictionaryManager, TypeFilter filter)
  {
    List<Type> typesList = new ArrayList<Type>();
    typesList.addAll(super.findTypes(dictionaryManager, filter));
    return typesList;
  }

  @Override
  public int countTypes(DictionaryManager dictionaryManager, TypeFilter filter)
  {
    return super.countTypes(dictionaryManager, filter);
  }


  @Override
  public String getTCCODI1Prefix()
  {
    return TCCODI1;
  }


  @Override
  public List<String> getTypeActions(DictionaryManager entityManager, String typeId)
  {
    return entityManager.loadTypeActions(org.matrix.kernel.Contact.class.getName());
  }

  public List getAccessControlList(DictionaryManager dicManager, String typeId)
  {
    return dicManager.loadAccessControlList(org.matrix.kernel.Contact.class.getName());
  }

}

  


