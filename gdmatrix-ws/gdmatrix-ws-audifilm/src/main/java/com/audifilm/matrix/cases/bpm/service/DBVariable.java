package com.audifilm.matrix.cases.bpm.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;
import com.audifilm.matrix.util.DateFormat;
import com.audifilm.matrix.util.NumberFormat;

/**
 *
 * @author comasfc
 */
public class DBVariable extends DBGenesysEntity
{

  String caseTypeId;
  String variableId;
  String label;
  String type;
  int size;
  int decimals;
  String description;
  String ambit;
  String opcional;
  String agrupacioId;
  String swseg;

  public String getAgrupacioId()
  {
    return agrupacioId;
  }

  public void setAgrupacioId(String agrupacioId)
  {
    this.agrupacioId = agrupacioId;
  }

  public String getAmbit()
  {
    return ambit;
  }

  public void setAmbit(String ambit)
  {
    this.ambit = ambit;
  }

  public String getCaseTypeId()
  {
    return caseTypeId;
  }

  public void setCaseTypeId(String caseTypeId)
  {
    this.caseTypeId = caseTypeId;
  }

  public int getDecimals()
  {
    return decimals;
  }

  public void setDecimals(int decimals)
  {
    this.decimals = decimals;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public String getOpcional()
  {
    return opcional;
  }

  public void setOpcional(String opcional)
  {
    this.opcional = opcional;
  }

  public int getSize()
  {
    return size;
  }

  public void setSize(int size)
  {
    this.size = size;
  }

  public String getSwseg()
  {
    return swseg;
  }

  public void setSwseg(String swseg)
  {
    this.swseg = swseg;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getVariableId()
  {
    return variableId;
  }

  public void setVariableId(String variableId)
  {
    this.variableId = variableId;
  }

  public String[] getIds()
  {
    return new String[] {caseTypeId, variableId};
  }

  public String toPropertyValue(String value) {
    if (this.type.equalsIgnoreCase("N")) {
      return NumberFormat.formatPropertyValue(size, decimals, value);
    }
    if (this.type.equalsIgnoreCase("D")) {
      return DateFormat.getInstance().formatVariableToPropertyValue(value);
    }
    return value;
  }

  public String fromPropertyValue(String value) {
    if (this.type.equalsIgnoreCase("N")) {
      return NumberFormat.parsePropertyValue(size, decimals, value);
    }
    if (this.type.equalsIgnoreCase("D")) {
      return DateFormat.getInstance().parsePropertyToVariableValue(value);
    }
    return value;
  }
  
}
