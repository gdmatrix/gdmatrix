package com.audifilm.matrix.cases.bpm.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;
import com.audifilm.matrix.util.TextUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.matrix.dic.Property;

/**
 *
 * @author comasfc
 */
public enum DBCaseProperty
{
  sdenum("caseId"),
  texpcod("caseTypeId"),
  sdenumtexp("caseTypeNum"),
  extrcod("extrcod"),
  sdetext("sdetext"),
  arxsigtop("arxsigtop"),
  identval("identval"),
  idiomacod("idiomacod"),
  eventexpr("eventexpr"),
  perscod("persId"),
  persnd("persnd"),
  reprcod("reprId"),
  reprnd("reprnd"),
  sdedomcod("addressId"),
  sderel("sderel"),
  resracodec("resracodec"),
  resrorg("resrorg"),
  sdedreg("registryDate"),
  sdehreg("registryTime"),
  resrdata("resrdata"),
  treccod("treccod"),
  persnom("persnom"),
  perscog1("perscog1"),
  perscog2("perscog2"),
  domicili("domicili"),
  repnom("repnom"),
  repcog1("repcog1"),
  repcog2("repcog2"),
  repdomi("repdomi"),
  persnif("persnif"),
  repnif("repnif"),
  assumcod("assumcod"),
  subassumcod("subassumcod"),
  sdenumcont("sdenumcont"),
  fcontacn("fcontacn"),
  numconordre("numconordre"),
  tipocod("tipocod"),
  licitacion("licitacion"),
  descassumpte("assumpte"),
  descsubassumpte("subassumpte"),
  transcod("transcod"),
  transcodarea("transcodarea"),
  assumcodorg("assumcodorg"),
  subassumcodorg("subassumcodorg"),
  seccod("seccod"),
  subseccod("subseccod"),
  sercod("sercod"),
  subsercod("subsercod"),
  plataforma("plataforma"),

  stateid("caseStateReference", DBCaseState.class.getName(), "cs"),
  state_startd("startDate", DBCaseState.class.getName(), "cs"),
  state_start("startTime", DBCaseState.class.getName(), "cs"),
  state_endd("endDate", DBCaseState.class.getName(), "cs"),
  state_endh("endTime", DBCaseState.class.getName(), "cs"),
  state_description("description", DBCaseState.class.getName(), "cs"),

  situacio("propertyValue", DBSituacio.class.getName(), "s")
  ;
  

  String fieldName;
  String className;
  String filterJPAObjectAlias;

  DBCaseProperty(String fieldName, String className,String filterJPAObjectAlias)
  {
    this.fieldName = fieldName;
    this.className = className;
    this.filterJPAObjectAlias = filterJPAObjectAlias;
  }
  DBCaseProperty(String fieldName)
  {
    this.fieldName = fieldName;
    this.className = DBCase.class.getName();
    this.filterJPAObjectAlias = "e";
  }


  public Property getProperty(String value)
  {
    Property property = new Property();
    property.setName(name());
    property.getValue().clear();
    List<String> valuesList = property.getValue();
    valuesList.clear();
    valuesList.add(TextUtil.encodeEmpty(value));
    return property;
  }


  static public Property getInstance(String name , String value)
  {
    Property property = new Property();
    property.setName(name);
    property.getValue().clear();
    List<String> valuesList = property.getValue();
    valuesList.clear();
    valuesList.add(TextUtil.encodeEmpty(value));
    return property;
  }


  public Property getProperty(DBGenesysEntity dbCase) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
  {
    //String methodName = "get" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);

    try {
      String methodName = "get" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
      Method method = dbCase.getClass().getMethod(methodName);
      return getProperty((String)method.invoke(dbCase));
    }
    catch(Exception ex)
    {
      CaseManager.log.log(Level.WARNING, ex.getMessage());
      return null;
    }

  }

  static public List<Property> getPropertyList(Class cls, DBGenesysEntity dbEntity)
  {
    List<Property> propertyList = new ArrayList<Property>();
    for(DBCaseProperty dbCaseProperty : values())
    {
      try {
        if (cls ==null || cls.getName().equals(dbCaseProperty.className))
        { 
          Property property = dbCaseProperty.getProperty(dbEntity);
          if (property!=null) propertyList.add(property);
        }
      }
      catch(Exception ex)
      {
        CaseManager.log.log(Level.SEVERE, ex.getMessage());
      }
    }
    return propertyList;
  }

  static public void copyPropertyList(List<Property> caseProperties, Class cls, Object dbCase)
  {
    for(Property property : caseProperties)
    {
      String name = property.getName();
      List<String> values = property.getValue();
      String value = values==null?null:values.get(0);

      DBCaseProperty dbCaseProperty = DBCaseProperty.valueOf(name);
      if (cls == null || dbCaseProperty.className.equals(cls.getName()))
      {
        try {
          Field method = dbCase.getClass().getField(dbCaseProperty.fieldName);
          method.set(dbCase, value);
        }
        catch(Exception ex)
        {
          CaseManager.log.log(Level.SEVERE, ex.getMessage());
        }
      }
    }
  }

  static protected  List<Property> loadCaseExtendedProperties(EntityManager entityManager, DBCase dbCase)
  {
    List<Property> propertiesList = new ArrayList<Property>();

    //Load responsable de l'expedient
    Property propResponsables = new Property();
    propResponsables.setName("responsables");

    DBCaseState caseState = dbCase.getCaseState();
    if (caseState!=null)
    {
      int respIndex=0;
      propResponsables.getValue().add(
              String.format("%04d;%s", ++respIndex,caseState.getPropertyValue()));

      List<DBCaseStateResponsible> list = caseState.getResponsibles();
      if (list!=null) {
        for(DBCaseStateResponsible csr: caseState.getResponsibles()) {
          propResponsables.getValue().add(
                String.format("%04d;%s", ++respIndex,csr.getPropertyValue()));
        }
      }
    }
    propertiesList.add(propResponsables);

    //Load variables
    Query queryVariables = entityManager.createNamedQuery("findCaseVariables");
    queryVariables.setFirstResult(0);
    queryVariables.setMaxResults(0);
    queryVariables.setParameter("caseId", dbCase.getCaseId());

    List<Object []> caseVarList = queryVariables.getResultList();

    for(Object [] row : caseVarList)
    {
      DBCaseVariable caseVariable = (DBCaseVariable)row[0];
      DBVariable variableDef = (DBVariable)row[1];

      Property property = new Property();
      property.setName("VAR" + caseVariable.getVariableId());
      property.getValue().add(
              variableDef.toPropertyValue(caseVariable.getValue()));
      propertiesList.add(property);
    }


    //Load situaci? de l'expedent: findSituacioExpedient
    Query querySituacio = entityManager.createNamedQuery("findSituacions");
    querySituacio.setFirstResult(0);
    querySituacio.setMaxResults(1);
    querySituacio.setParameter("caseId", dbCase.getCaseId());
    querySituacio.setParameter("tot", null);

    List<DBSituacio> resultListSituacio = querySituacio.getResultList();
    if (resultListSituacio.size()>0) {
      propertiesList.addAll(
              DBCaseProperty.getPropertyList(DBSituacio.class, resultListSituacio.get(0)));
    }

    //Load alarmes
    //propertiesList.add(propAlarmes);

    return propertiesList;
  }

}
