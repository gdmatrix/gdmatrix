package com.audifilm.matrix.dic.service.types;

import com.audifilm.matrix.dic.service.address.Address;
import com.audifilm.matrix.dic.service.person.Person;
import com.audifilm.matrix.dic.service.caseperson.CasePerson;
import com.audifilm.matrix.dic.service.caseaddress.CaseAddress;
import com.audifilm.matrix.dic.service.cases.Case;
import com.audifilm.matrix.dic.service.contact.Contact;
import com.audifilm.matrix.dic.service.personrepresentant.PersonRepresentant;
import com.audifilm.matrix.dic.service.room.Room;
import com.audifilm.matrix.dic.service.casedocument.CaseDocument;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;


import java.util.Map;
import org.matrix.dic.Type;
import org.matrix.dic.TypeFilter;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author comasfc
 */
public class DicTypeAdmin
{

  private List<DicTypeRoot> rootTypes = null;
  private Map<String, DicTypeInterface> allTypes = null;
  static DicTypeAdmin instance;
  static private boolean initialized = false;

  private DicTypeAdmin()
  {
  }

  synchronized static private void initialize()
  {
    if (!initialized)
    {
      Case root = new Case();
      //
      instance = new DicTypeAdmin();
      instance.rootTypes = new ArrayList<DicTypeRoot>();
      instance.rootTypes.add(new Case());
      instance.rootTypes.add(new CasePerson());
      instance.rootTypes.add(new CaseAddress());
      instance.rootTypes.add(new CaseDocument());            
      instance.rootTypes.add(new Person());
      instance.rootTypes.add(new PersonRepresentant());
      instance.rootTypes.add(new Contact());
      instance.rootTypes.add(new Address());
      instance.rootTypes.add(new Room());

      
      instance.allTypes = new Hashtable<String, DicTypeInterface>();

      for (DicType rootType : instance.rootTypes)
      {
        instance.allTypes.put(rootType.getClass().getName(), rootType);
        putAllChildDicTypes(rootType);
      }
      initialized = true;

      System.out.println(" DIC AUDIFILM TYPES: " + instance.getAllTypes());
    }
  }

  static public <T> T getInstance(Class<T> classInstance)
  {
    System.out.println("Carregant " + classInstance.getName());
    if (instance == null) initialize();
    return (T) instance.allTypes.get(classInstance.getName());
  }

  static private void putAllChildDicTypes(DicType superType)
  {
    List<DicType> childTypes = superType.getChildDicTypes();
    if (childTypes != null)
    {
      for (DicType childType : childTypes)
      {
        instance.allTypes.put(childType.getClass().getName(), childType);
        putAllChildDicTypes(childType);
      }
    }
  }

  static public DicTypeInterface getTypeInstance(WSEndpoint endpoint, String typeId)
  {
    if (typeId == null)
    {
      return null;
    }

    String localTypeId = endpoint.toLocalId(Type.class, typeId);
    for (DicType dicTypeRoot : getRootTypes())
    {
      DicTypeInterface dicType = dicTypeRoot.getDicType(localTypeId);
      if (dicType != null)
      {
        return dicType;
      }
    }
    return null;
  }

  static public List<DicTypeInterface> getTypeInstanceBySuperTypeId(WSEndpoint endpoint, String superTypeId)
  {
    if (superTypeId == null || superTypeId.trim().equals(""))
    {
      return Collections.emptyList();
    }
    String localSuperTypeId = endpoint.toLocalId(Type.class, superTypeId);

    List<DicTypeInterface> typesList = new ArrayList<DicTypeInterface>();
    for (DicTypeInterface dicTypeRoot : getRootTypes())
    {
      DicTypeInterface dicType = dicTypeRoot.getDicType(localSuperTypeId);
      if (dicType != null)
      {
        typesList.addAll(dicType.getChildDicTypes());
      }
    }
    return typesList;
  }

  static public DicTypeInterface getTypeInstanceByType(WSEndpoint endpoint, Type type)
  {
    return getTypeInstance(endpoint, type.getTypeId());
  }

  static public List<DicTypeInterface> getTypeInstanceByTypeFilter(WSEndpoint endpoint, TypeFilter typeFilter)
  {
    List<DicTypeInterface> typesList = new ArrayList<DicTypeInterface>();
    String superTypeId = typeFilter.getSuperTypeId();
    if (superTypeId != null && !superTypeId.equals(""))
    {
      return getTypeInstanceBySuperTypeId(endpoint, superTypeId);
    }

    String path = typeFilter.getTypePath();
    if (path != null && !path.equals(""))
    {
      return getTypeInstanceByTypePath(endpoint, path);
    }

    String typeId = endpoint.toLocalId(Type.class, typeFilter.getTypeId());
    for (DicType rootType : getRootTypes())
    {
      typesList.addAll(rootType.findDicTypes(typeId));
    }

    return typesList;
  }

  static public List<DicTypeInterface> getTypeInstanceByTypePath(WSEndpoint endpoint, String path)
  {
    List<DicTypeInterface> typesList = new ArrayList<DicTypeInterface>();
    if (path != null)
    {
      for (DicTypeInterface rootType : getRootTypes())
      {
        typesList.addAll(rootType.findDicTypesByPath(path));
      }
    }
    return typesList;
  }

  static public Collection<DicTypeInterface> getAllTypes()
  {
    initialize();
    return instance.allTypes.values();
  }

  static public List<DicTypeRoot> getRootTypes()
  {
    initialize();
    return instance.rootTypes;
  }

  static public void main(String [] args)
  {

    List<Object> allTypes = Arrays.asList(getAllTypes().toArray());

    Collections.sort(allTypes, new Comparator() {
      public int compare(Object o1, Object o2)
      {
        return o1.getClass().getName().compareTo(o2.getClass().getName());
      }
    });
    for(Object dicType : allTypes)
    {
      System.out.println(((DicTypeInterface)dicType).getLocalTypePath(null, null));
    }
    

  }
}
