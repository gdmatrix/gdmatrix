package com.audifilm.matrix.security.service;

import com.audifilm.matrix.security.Action;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.santfeliu.jpa.JPA;
import com.audifilm.matrix.security.Mode;
import com.audifilm.matrix.security.ModulAplicacio;
import com.audifilm.matrix.util.ConfigProperties;
import java.util.Collections;
import java.util.Hashtable;
import org.matrix.security.AccessControl;
import org.santfeliu.jpa.JPAUtils;


@JPA
public class SecurityManager 
{
  final static public String NIVELL_ADMINISTRADOR = "999";
  
  @PersistenceContext(unitName = "security_g5")
  public EntityManager entityManager;


  public SecurityManager() throws Exception
  {
    entityManager = JPAUtils.createEntityManager("security_g5");
    try
    {
      EntityTransaction tx = entityManager.getTransaction();
      tx.begin();
    }
    catch(Exception ex)
    {
      entityManager.close();
      throw ex;
    }
  }

  
  private DBNivellUsuari findNivellUsuari(String userId, String appId)
  {
    Query query = entityManager.createNamedQuery("findNivellUsuari");
    query.setParameter("userId", userId);
    query.setParameter("appId", appId);

    List<DBNivellUsuari> resultList = (List<DBNivellUsuari>)query.getResultList();

    if (resultList == null || resultList.equals(0)) return null;
    return resultList.get(0);
  }


  private DBNivellGrup findNivellGrup(String areaId, String departamentId, String grupId, String appId)
  {
    Query query = entityManager.createNamedQuery("findNivellGrup");
    query.setParameter("areaId", areaId);
    query.setParameter("departamentId", departamentId);
    query.setParameter("grupId", grupId);
    query.setParameter("appId", appId);

    List<DBNivellGrup> resultList = (List<DBNivellGrup>)query.getResultList();
    
    if (resultList == null || resultList.equals(0)) return null;
    return resultList.get(0);
  }

  private DBNivellDepartament findNivellDepartament(String areaId, String departamentId, String appId)
  {
    Query query = entityManager.createNamedQuery("findNivellDepartament");
    query.setParameter("areaId", areaId);
    query.setParameter("departamentId", departamentId);
    query.setParameter("appId", appId);

    List<DBNivellDepartament> resultList = (List<DBNivellDepartament>)query.getResultList();

    if (resultList == null || resultList.equals(0)) return null;
    return resultList.get(0);
  }

  private DBNivellArea findNivellArea(String areaId, String appId)
  {
    Query query = entityManager.createNamedQuery("findNivellDepartament");
    query.setParameter("areaId", areaId);
    query.setParameter("appId", appId);

    List<DBNivellArea> resultList = (List<DBNivellArea>)query.getResultList();

    if (resultList == null || resultList.equals(0)) return null;
    return resultList.get(0);
  }

  private List<List<String>> calculaNivellUsuariAplicacio(String userId, String appId)
  {
    Query query = entityManager.createNamedQuery("findNivellsUsuari");
    query.setParameter("appId", appId);
    query.setParameter("userId", userId);

    List<List<String>> resultList = new ArrayList<List<String>>();

    List<DBNivellUsuari> nuList = query.getResultList();
    List<String> list = new ArrayList<String>();
    if (nuList != null && !nuList.isEmpty())
    {
      for(DBNivellUsuari nu : nuList)
      {
        list.add(nu.getNivellId());
      }
      nuList.clear();
      resultList.add(list);
    }

    query = entityManager.createNamedQuery("findNivellsGrupUsuari");
    query.setParameter("appId", appId);
    query.setParameter("userId", userId);

    List<DBNivellGrup> nivells = query.getResultList();
    list = new ArrayList<String>();
    for(DBNivellGrup n : nivells)
    {
      list.add(n.getNivellId());
    }
    nivells.clear();
    resultList.add(list);



    query = entityManager.createNamedQuery("findNivellsGrupUsuari");
    query.setParameter("appId", appId);
    query.setParameter("userId", userId);

    List<DBNivellGrup> nivellsGrup = query.getResultList();
    list = new ArrayList<String>();
    for(DBNivellGrup n : nivellsGrup)
    {
      list.add(n.getNivellId());
    }
    nivellsGrup.clear();
    resultList.add(list);

    
    query = entityManager.createNamedQuery("findNivellsDepartamentUsuari");
    query.setParameter("appId", appId);
    query.setParameter("userId", userId);

    List<DBNivellDepartament> nivellsDept = query.getResultList();
    list = new ArrayList<String>();
    for(DBNivellDepartament n : nivellsDept)
    {
      list.add(n.getNivellId());
    }
    nivellsDept.clear();
    resultList.add(list);


    query = entityManager.createNamedQuery("findNivellsAreaUsuari");
    query.setParameter("appId", appId);
    query.setParameter("userId", userId);

    List<DBNivellArea> nivellsArea = query.getResultList();
    for(DBNivellArea n : nivellsArea)
    {
      list.add(n.getNivellId());
    }
    nivellsArea.clear();
    resultList.add(list);

    return resultList;
  }

  private Mode calcularMode(ModulAplicacio modul,  String userId)
  {
    return calcularMode(modul, userId, null, null, null);
  }
  

  private Mode calcularMode(ModulAplicacio modul, String userId, String areaId, String departamentId, String grupId)
  {
    boolean nivellTrobat = false;

    String aplId = (modul!=null)?modul.getAplId():null;
    String itemId = (modul!=null)?modul.getItemId():null;

    Query query = entityManager.createNamedQuery("findAccessItemNivellsUsuari");
    query.setParameter("aplId", aplId);
    query.setParameter("itemId", itemId);
    query.setParameter("userId", userId);

    Mode resultMode = new Mode();

    List<Object[]> itemList = query.getResultList();
    for(Object [] row : itemList)
    {
      DBAccessMenuItem item = (DBAccessMenuItem)row[0];
      DBNivellUsuari nivell = (DBNivellUsuari)row[1];

      if (nivell!=null && !nivellTrobat) nivellTrobat = true;
      if (nivell.getNivellId().equals(NIVELL_ADMINISTRADOR)) return Mode.getModeSuperusuari();

      Mode mode = new Mode(item.getMode());
      resultMode = resultMode.mergeModes(mode);
    }
    itemList.clear();

    if (resultMode.equals(Mode.getModeDenegat()) || resultMode.equals(Mode.getModeSuperusuari())) return resultMode;

    query = entityManager.createNamedQuery("findAccessItemNivellsGrup");
    query.setParameter("aplId", aplId);
    query.setParameter("itemId", itemId);
    query.setParameter("userId", userId);
    query.setParameter("areaId", areaId);
    query.setParameter("departamentId", departamentId);
    query.setParameter("grupId", grupId);
    itemList = query.getResultList();
    for(Object [] row : itemList)
    {
      DBAccessMenuItem item = (DBAccessMenuItem)row[0];
      DBNivellGrup nivell = (DBNivellGrup)row[1];

      if (nivell!=null && !nivellTrobat) nivellTrobat = true;
      if (nivell.getNivellId().equals(NIVELL_ADMINISTRADOR)) return Mode.getModeSuperusuari();

      Mode mode = new Mode(item.getMode());
      resultMode = resultMode.mergeModes(mode);
    }
    itemList.clear();

    if (resultMode.equals(Mode.getModeDenegat()) || resultMode.equals(Mode.getModeSuperusuari())) return resultMode;

    query = entityManager.createNamedQuery("findAccessItemNivellsDepartament");
    query.setParameter("aplId", aplId);
    query.setParameter("itemId", itemId);
    query.setParameter("userId", userId);
    query.setParameter("areaId", areaId);
    query.setParameter("departamentId", departamentId);
    itemList = query.getResultList();
    for(Object [] row : itemList)
    {
      DBAccessMenuItem item = (DBAccessMenuItem)row[0];
      DBNivellDepartament nivell = (DBNivellDepartament)row[1];
      if (nivell!=null && !nivellTrobat) nivellTrobat = true;
      if (nivell.getNivellId().equals(NIVELL_ADMINISTRADOR)) return Mode.getModeSuperusuari();

      Mode mode = new Mode(item.getMode());
      resultMode = resultMode.mergeModes(mode);
    }
    itemList.clear();

    if (resultMode.equals(Mode.getModeDenegat()) || resultMode.equals(Mode.getModeSuperusuari())) return resultMode;

    query = entityManager.createNamedQuery("findAccessItemNivellsArea");
    query.setParameter("aplId", aplId);
    query.setParameter("itemId", itemId);
    query.setParameter("userId", userId);
    query.setParameter("areaId", areaId);

    itemList = query.getResultList();
    for(Object [] row : itemList)
    {
      DBAccessMenuItem item = (DBAccessMenuItem)row[0];
      DBNivellArea nivell = (DBNivellArea)row[1];
      if (nivell!=null && !nivellTrobat) nivellTrobat = true;
      if (nivell.getNivellId().equals(NIVELL_ADMINISTRADOR)) return Mode.getModeSuperusuari();

      Mode mode = new Mode(item.getMode());
      resultMode = resultMode.mergeModes(mode);
    }
    itemList.clear();

    if (!nivellTrobat) return Mode.getModeDenegat();
    
    return resultMode;
  }

  public List<String> findActionsList(String matrixModule, String userId)
  {
    List<String> list = new ArrayList<String>();
    ModulAplicacio modul = ModulAplicacio.getByModul(matrixModule);

    Mode mode = calcularMode(modul , userId);
    return (List<String>) ((mode != null) ? mode.getActionsList() : Collections.emptyList());
  }

  public boolean canDoAction(String userId, String matrixModule, Action action)
  {
    Mode mode = calcularMode(ModulAplicacio.getByModul(matrixModule), userId);
    return (mode==null) || mode.canDoAction(action);
  }

  public boolean canDoAction(String userId, String matrixModule, String action)
  {
    Mode mode = calcularMode(ModulAplicacio.getByModul(matrixModule), userId);
    return (mode==null) || mode.canDoAction(Action.getAction(action));
  }


  public List<AccessControl> findAccessControlList(String matrixModule, String userId)
  {
    String defaultRole = ConfigProperties.getProperty(
        "com.audifilm.matrix.security.adminRoleId", "CASE_ADMIN");

    List<AccessControl> list = new ArrayList<AccessControl>();
    ModulAplicacio modul = ModulAplicacio.getByModul(matrixModule);

    Query query = entityManager.createNamedQuery("findAccesItem");
    query.setParameter("aplId", modul.getAplId());
    query.setParameter("itemId", modul.getItemId());
    query.setParameter("nivellId", null);
    query.setParameter("roleId", null);

    query.setMaxResults(0);
    query.setFirstResult(0);

    List<DBAccessMenuItem> itemList = query.getResultList();
    Hashtable<String,Boolean> rolesActions = new Hashtable<String,Boolean>();
    for(DBAccessMenuItem item : itemList)
    {
      //DBAccessMenuItem item = (DBAccessMenuItem)row[0];
      //DBNivellUsuari nivell = (DBNivellUsuari)row[1];
      Mode mode = new Mode(item.getMode());

      //PER CADA TIPUS D'ACCES CREAR UN OBJECTE ACCESSCONTROL AMB
      // item.aplid + ":" + item.nivellid

      String roleId = item.getRoleId();
      if (roleId==null || roleId.trim().equals("")) roleId = defaultRole;
      for(Action action : Action.values())
      {
        String roleAction = roleId + ";" + action.getActionName();
        Boolean value = rolesActions.get(roleAction);
        if (value!=null && value.booleanValue()) continue;

        if (mode.canDoAction(action))
        {
          rolesActions.put(roleAction, Boolean.TRUE);
          list.add(getAccessControlInstance(roleId, action.getActionName()));
        }
      }
    }
    rolesActions.clear();
    return list;
  }
  

  private AccessControl getAccessControlInstance(String roleId, String action)
  {
    AccessControl accessControl = new AccessControl();
    accessControl.setRoleId(roleId);
    accessControl.setAction(action);
    return accessControl;
  }


  public List<DBArea> findUserAreas(String userId, String areaId)
  {
    Query query = entityManager.createNamedQuery("findUserAreas");
    query.setParameter("userId", userId);
    query.setParameter("areaId", areaId);

    return query.getResultList();
  }
  
  public List<DBDepartament> findUserDepartaments(String userId, String areaId, String departamentId)
  {
    Query query = entityManager.createNamedQuery("findUserDepartaments");
    query.setParameter("userId", userId);
    query.setParameter("areaId", areaId);
    query.setParameter("departamentId", departamentId);

    return query.getResultList();
  }
  
  public List<DBGrup> findUserGrups(String userId, String areaId, String departamentId, String grupId)
  {
    Query query = entityManager.createNamedQuery("findUserGrups");
    query.setParameter("userId", userId);
    query.setParameter("areaId", areaId);
    query.setParameter("departamentId", departamentId);
    query.setParameter("grupId", grupId);

    return query.getResultList();
  }

    public List<DBGrupUsuari> findGrupsUsuari(String userId, String areaId, String departamentId, String grupId)
  {
    Query query = entityManager.createNamedQuery("findGrupsDelUsuari");
    query.setParameter("userId", userId);
    query.setParameter("areaId", areaId);
    query.setParameter("departamentId", departamentId);
    query.setParameter("grupId", grupId);

    return query.getResultList();
  }
  
  
}
