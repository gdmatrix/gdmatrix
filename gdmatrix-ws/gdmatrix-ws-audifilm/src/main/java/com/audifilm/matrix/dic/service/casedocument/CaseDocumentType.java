package com.audifilm.matrix.dic.service.casedocument;

import com.audifilm.matrix.dic.service.DictionaryManager;
import com.audifilm.matrix.dic.service.types.DicType;
import com.audifilm.matrix.dic.service.types.EnumeratedDicType;
import com.audifilm.matrix.dic.service.types.TypeEnumElement;
import java.util.List;

/**
 *
 * @author blanquepa
 */
public class CaseDocumentType extends EnumeratedDicType<CaseDocument>
{
  final static private String PREFIX = "CD" +  DicType.PREFIX_SEPARATOR;

  static public enum Types implements TypeEnumElement
  {
    RegistreEntrada("Document de registre d'entrada"),
    RegistreSortida("Document de registre de sortida");
    String typeId;
    String description;

    Types(String description)
    {
      this.typeId = CaseDocumentType.PREFIX + name();
      this.description = description;
    }

    public String getTypeId()
    {
      return typeId;
    }

    public String getDescription()
    {
      return description;
    }

    static public Types getType(String typeId)
    {
      if (typeId.startsWith(PREFIX))
      {
        return valueOf(typeId.substring(PREFIX.length()));
      }
      return valueOf(typeId);
    }
  }  
  
  public CaseDocumentType(CaseDocument parent)
  {
    super(parent);
  }
  
  public TypeEnumElement getElement(String id)
  {
    return Types.getType(id);
  }

  public TypeEnumElement[] getElements()
  {
    return Types.values();
  }

  @Override
  public String getTypeIdPrefix()
  {
    return PREFIX;
  }

  @Override
  public List<String> getTypeActions(DictionaryManager entityManager, String typeId)
  {
    return entityManager.loadTypeActions(org.matrix.cases.CaseAddress.class.getName());
  }

  public List getAccessControlList(DictionaryManager dicManager, String typeId)
  {
    return dicManager.loadAccessControlList(org.matrix.cases.CaseAddress.class.getName());
  }
  
}
