package com.audifilm.matrix.dic.service.cases;

import com.audifilm.matrix.dic.service.DictionaryManager;
import com.audifilm.matrix.dic.service.types.DicType;
import com.audifilm.matrix.dic.service.types.DicTypeInterface;
import com.audifilm.matrix.util.TextUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.matrix.dic.PropertyDefinition;
import org.matrix.dic.PropertyType;
import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;
import org.matrix.security.AccessControl;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author comasfc
 */
public class CaseExpedient extends DicType<Case>
{

  //public final static String PREFIX = "X";
  public final static String DESCRIPCIO = "Expedient SDE";
  public final static String TYPEID = "SDE";
  static List<DicTypeInterface> childs;

  long lastModifiedTimestamp = 0;

  public CaseExpedient(Case parent)
  {
    super(parent);
  }

  @Override
  public List<DicTypeInterface> getChildDicTypes()
  {
    if (childs == null)
    {
      childs = new ArrayList<DicTypeInterface>();
      childs.add(new CaseExpedientType(this));
      childs.add(new CaseActaType(this));
    }
    return childs;
  }

  public String getDescription()
  {
    return DESCRIPCIO;
  }

  public Type loadType(DictionaryManager dicManager, String id)
  {
    Type type = new Type();
    type.setInstantiable(false);
    type.setDescription(DESCRIPCIO);
    type.setSuperTypeId(getParentDicType().getRootTypeId());
    type.setTypeId(dicManager.getEndpoint().toGlobalId(Type.class, TYPEID));
    type.setTypePath(getGlobalTypePath(dicManager.getEndpoint(), TYPEID));
    type.setRestricted(true);

    type.getPropertyDefinition().addAll(loadPropertyDefinitionList(dicManager, TYPEID));
    type.getAccessControl().addAll(getAccessControlList(dicManager, TYPEID));

    return type;
  }

  public List<PropertyDefinition> loadPropertyDefinitionList(DictionaryManager dicManager, String id)
  {
    List<PropertyDefinition> localList = CaseExpedientProperty.getPropertyDefinitionList();
    List<PropertyDefinition> globalList = new ArrayList<PropertyDefinition>();
    for(PropertyDefinition localProp : localList)
    {
      globalList.add(dicManager.getEndpoint().toGlobal(PropertyDefinition.class, localProp));
    }
    return globalList;
  }



  public Type storeType(DictionaryManager dicManager, Type type)
  {
    throw new UnsupportedOperationException("Not supported.");
  }

  public int countTypes(DictionaryManager dicManager, TypeFilter globalFilter)
  {
    TypeFilter filter = dicManager.getEndpoint().toLocal(TypeFilter.class, globalFilter);
    if (match(dicManager.getEndpoint(), filter))
    {
      return 1;
    }
    return 0;
  }

  public List<Type> findTypes(DictionaryManager dicManager, TypeFilter globalFilter)
  {
    TypeFilter filter = dicManager.getEndpoint().toLocal(TypeFilter.class, globalFilter);      
    if (match(dicManager.getEndpoint(), filter))
      return Collections.singletonList(loadType(dicManager, null));
    else
      return Collections.emptyList();
  }

  boolean match(WSEndpoint endpoint, TypeFilter filter)
  {
    return ((TextUtil.matchesFilter( TextUtil.likePattern(filter.getDescription()) , DESCRIPCIO))
            && (TextUtil.matchesFilter(filter.getSuperTypeId(), getSuperTypeId()))
            && (TextUtil.matchesFilter(filter.getTypeId(), TYPEID))
            && (TextUtil.matchesFilter(filter.getTypePath(), getGlobalTypePath(endpoint, TYPEID))));
  }


  public List<DicTypeInterface> findDicTypesByPath(String filterPath)
  {
    if (filterPath == null)
    {
      return Collections.emptyList();
    }
    List<DicTypeInterface> list = new ArrayList<DicTypeInterface>();
    if (filterPath.equals("") || filterPath.equals("/")
            || filterPath.equals("/" + getParentDicType().getRootTypeId())
            || filterPath.equals("/" + getParentDicType().getRootTypeId() + "/%"))
    {
      list.add(this);
    }
    if (filterPath.startsWith("/" + getParentDicType().getRootTypeId() + "/"))
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
    if (typeId==null) return false;
    return (typeId.equals(TYPEID));
  }

  @Override
  public String toGlobalId(WSEndpoint endpoint, String localTypeId)
  {
    return endpoint.toGlobalId(Type.class, TYPEID);
  }

  @Override
  public String toLocalId(WSEndpoint endpoint, String globalId)
  {
    return TYPEID;
  }


  private enum CaseExpedientProperty {
    sdenum("Número d'expedient general", 20, true),
    texpcod("Tipus d'expedient", 8, true),
    sdenumtexp("Núm tipus expedient específic",20,true),
    extrcod("Codi extracte", 4, true),
    sdetext("Text", 255, false),
    arxsigtop("arxsigtop", PropertyType.TEXT, true, 20, 0,1,true),
    identval("identval", PropertyType.TEXT, true, 20,0,1,true),
    idiomacod("idiomacod",PropertyType.TEXT, true, 1,0,1,true),
    eventexpr("eventexpr", 999, true),
    resracodec("resracodec", 10, true),
    perscod("Codi persona", PropertyType.TEXT, true, 20, 0, 1, false),
    persnd("Codi domicili", PropertyType.TEXT, true, 20, 0, 1, false),
    reprcod("Codi Representant ", PropertyType.TEXT, true, 20, 0, 1, false),
    reprnd("Codi domicili Representant", PropertyType.TEXT, true, 20, 0, 1, false),
    sdedomcod("Adreça", PropertyType.TEXT, true, 20, 0, 1, false),
    sderel("sderel", PropertyType.TEXT, true, 20, 0, 1, true),
    persnom("Persona Nom", 100, true),
    perscog1("Persona Cognom 1", 100, true),
    perscog2("Persona Cognom 2", 100, true),
    domicili("Domicili", 100, true),
    repnom("Representant Nom", 100, true),
    repcog1("Representant Cognom", 100, true),
    repcog2("Representant Cognom 2", 100, true),
    repdomi("Representant Domicili", 100, true),
    persnif("Persona NIF", 20, true),
    repnif("Representant NIF", 20, true),
    resrorg("resrorg", 4, true),
    sdedreg("registryDate", PropertyType.DATE, true, 8, 0,1, false),
    sdehreg("registryTime", PropertyType.DATE, true,6, 0,1, false),
    resrdata("resrdata", PropertyType.DATE, true, 8, 0, 1, false),
    assumcod("assumcod", 4, true),
    subassumcod("subassumcod", 4, true),
    tipocod("tipocod",2, true),
    licitacion("licitacion", 10, true),
    descassumpte("descassumpte", 40, true),
    descsubassumpte("descsubassumpte", 40, true),
    transcod("transcod", 11, true),
    transcodarea("transcodarea", 20, true),
    assumcodorg("assumcodorg", 4, true),
    subassumcodorg("subassumcodorg", 4, true),
    seccod("seccod",2, true),
    subseccod("subseccod",2, true),
    sercod("sercod",2, true),
    subsercod("subsercod",2, true),
    stateid("Codi estat", 255, true),
    state_startd("Data inici estat", PropertyType.DATE, true, 8, 0,1, false),
    state_startt("Hora inici estat", PropertyType.TEXT, true, 6, 0,1, false),
    state_endd("Data fi estat", PropertyType.DATE, true, 8, 0,1, false),
    state_endh("Hora fi estat", PropertyType.TEXT, true, 6, 0,1, false),
    state_description("Descripció estat", 40, true),
    treccod("treccod", PropertyType.TEXT, true, 20, 0, 1, true),
    plataforma("plataforma", PropertyType.TEXT, true, 20, 0, 1, true),
    sdenumcont("sdenumcont", PropertyType.TEXT, true, 20, 0, 1, true),
    fcontacn("fcontacn", PropertyType.TEXT, true, 20, 0, 1, true),
    numconordre("numconordre", PropertyType.TEXT, true, 20, 0, 1, true),
    situacio("Situació de l'expedient", PropertyType.TEXT, true, 1000, 0, 0, false),
    responsables("Responsables", PropertyType.TEXT, true, 1000, 0, 0, false)
    ;


    PropertyDefinition propDef;
    static private List<PropertyDefinition> propertyDefinitionList;

    static public List<PropertyDefinition> getPropertyDefinitionList()
    {
      if(propertyDefinitionList == null)
      {
        propertyDefinitionList = new ArrayList<PropertyDefinition>();
        for(CaseExpedientProperty prop:values())
        {
          propertyDefinitionList.add(prop.getPropertyDefinition());
        }
      }
      return propertyDefinitionList;
    }

    CaseExpedientProperty(String description,PropertyType type, boolean readOnly, int size, int minOccurs, int maxOccurs, boolean hidden)
    {
      propDef = new PropertyDefinition();
      propDef.setName(name());
      propDef.setDescription(description);
      propDef.setType(type);
      propDef.setHidden(hidden);
      propDef.setMinOccurs(minOccurs);
      propDef.setMaxOccurs(maxOccurs);
      propDef.setSize(size);
      propDef.setReadOnly(readOnly);

    }

    CaseExpedientProperty(String description, int size, boolean readOnly)
    {
      propDef = new PropertyDefinition();
      propDef.setName(name());
      propDef.setDescription(description);
      propDef.setType(PropertyType.TEXT);
      propDef.setHidden(false);
      propDef.setMinOccurs(0);
      propDef.setMaxOccurs(1);
      propDef.setSize(size);
      propDef.setReadOnly(readOnly);
    }

    public PropertyDefinition getPropertyDefinition()
    {
      return propDef;
    }
  }

  @Override
  public List<String> getTypeActions(DictionaryManager entityManager, String typeId)
  {
    return entityManager.loadTypeActions(org.matrix.cases.Case.class.getName());
  }

  public List<AccessControl> getAccessControlList(DictionaryManager dicManager, String typeId)
  {
    return dicManager.loadAccessControlList(org.matrix.cases.Case.class.getName());
  }

  @Override
  public List<String> listModifiedTypes(DictionaryManager dicManager, String dateTime1, String dateTime2)
  {
    if (dicManager.getFixedProperties().isModified(lastModifiedTimestamp)
            || dicManager.getFixedProperties().isModified(dateTime1, dateTime2))
    {
      lastModifiedTimestamp = System.currentTimeMillis();
      return Collections.singletonList(
        dicManager.getEndpoint().toGlobalId(Type.class, TYPEID));
    }
    return Collections.emptyList();
  }



}
