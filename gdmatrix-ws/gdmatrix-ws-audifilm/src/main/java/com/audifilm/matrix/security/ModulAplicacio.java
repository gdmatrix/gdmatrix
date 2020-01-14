package com.audifilm.matrix.security;

import java.util.Hashtable;

/**
 *
 * @author comasfc
 */
public class ModulAplicacio {
  
  static private Hashtable<String,ModulAplicacio> aplicacions = new Hashtable<String, ModulAplicacio>();
  
  String aplId;
  String itemId;
  String controlId;
  String modulMatrix;

  static {
    new ModulAplicacio(org.matrix.kernel.Person.class.getPackage().getName(), "NCL", "frmNCL_PERSONA");
    new ModulAplicacio(org.matrix.cases.Case.class.getPackage().getName(), "SDE", "mnuExpedients");
    new ModulAplicacio(org.matrix.cases.Case.class.getName(), "SDE", "mnuExpedients");
    new ModulAplicacio(org.matrix.cases.CasePerson.class.getName(), "SDE", "frmSDE_INTERESSAT");
    new ModulAplicacio(org.matrix.dic.Property.class.getName(), "SDE", "frmSDE_VARIABLE");
    new ModulAplicacio(org.matrix.cases.CaseDocument.class.getName(), "SDE", "frmAPL_DOCSAPL");
  }

  public ModulAplicacio(String modulMatrix, String aplId, String itemId)
  {
    this(modulMatrix, aplId, itemId, null);
  }

  public ModulAplicacio(String modulMatrix, String aplId)
  {
    this(modulMatrix, aplId, null, null);
  }

  public ModulAplicacio(String modulMatrix, String aplId, String itemId, String controlId)
  {
    this.modulMatrix = modulMatrix;
    this.aplId = aplId;
    this.itemId = itemId;
    
    aplicacions.put(modulMatrix, this);
  }

  public String getAplId()
  {
    return aplId;
  }

  public void setAplId(String aplId)
  {
    this.aplId = aplId;
  }

  public String getControlId()
  {
    return controlId;
  }

  public void setControlId(String controlId)
  {
    this.controlId = controlId;
  }

  public String getItemId()
  {
    return itemId;
  }

  public void setItemId(String itemId)
  {
    this.itemId = itemId;
  }

  public String getModulMatrix()
  {
    return modulMatrix;
  }

  public void setModulMatrix(String modulMatrix)
  {
    this.modulMatrix = modulMatrix;
  }

  public String getMatrixModule()
  {
    return modulMatrix;
  }

  public void setMatrixModule(String modulMatrix)
  {
    this.modulMatrix = modulMatrix;
  }

  static public ModulAplicacio getByModul(String modulMatrix)
  {
    if (modulMatrix==null || modulMatrix.length()==0) return null;

    ModulAplicacio modul = aplicacions.get(modulMatrix);
    if (modul!=null) return modul;

    int index = modulMatrix.lastIndexOf(".");
    return (index<1)?null:getByModul(modulMatrix.substring(0,index));
  }


}
