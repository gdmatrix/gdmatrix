package com.audifilm.matrix.dic.service;

import com.audifilm.matrix.common.service.GenesysPK;

/**
 *
 * @author comasfc
 */
public class DBVariableDefinitionPK extends GenesysPK
{

  private String caseTypeId;
  private String varcod;

  public String getCaseTypeId()
  {
    return caseTypeId;
  }

  public void setCaseTypeId(String typeId)
  {
    this.caseTypeId = typeId;
  }

  public String getVarcod()
  {
    return varcod;
  }

  public void setVarcod(String variableId)
  {
    this.varcod = variableId;
  }
}
