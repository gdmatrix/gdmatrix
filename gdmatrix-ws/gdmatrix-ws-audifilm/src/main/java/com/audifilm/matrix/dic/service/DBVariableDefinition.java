package com.audifilm.matrix.dic.service;

import com.audifilm.matrix.common.service.DBGenesysEntity;
import org.matrix.dic.PropertyDefinition;
import org.matrix.dic.PropertyType;
import org.matrix.util.WSEndpoint;

/**
 *
 * @author comasfc
 */
public class DBVariableDefinition extends DBGenesysEntity
{
  String caseTypeId;
  String varcod;
  String varlabel;
  String vartip;
  int varsize;
  String vardecim;
  String vardesc;
  String varambit;
  String varopc;
  String agrupcod;
  String swseg;
/*
  String stdugr;
  String stdumod;
  String stddgr;
  String stdhgr;
  String stddmod;
  String stdhmod;
*/
  public DBVariableDefinitionPK getPrimaryKey()
  {
    DBVariableDefinitionPK pk = new DBVariableDefinitionPK();
    pk.setCaseTypeId(caseTypeId);
    pk.setVarcod(varcod);
    return pk;
  }

  @Override
  public String[] getIds()
  {
    return getPrimaryKey().getIds();
  }

  public String getAgrupcod()
  {
    return agrupcod;
  }

  public void setAgrupcod(String agrupcod)
  {
    this.agrupcod = agrupcod;
  }

  public String getSwseg()
  {
    return swseg;
  }

  public void setSwseg(String swseg)
  {
    this.swseg = swseg;
  }

  public String getCaseTypeId()
  {
    return caseTypeId;
  }

  public void setCaseTypeId(String caseTypeId)
  {
    this.caseTypeId = caseTypeId;
  }

  public String getVarambit()
  {
    return varambit;
  }

  public void setVarambit(String varambit)
  {
    this.varambit = varambit;
  }

  public String getVarcod()
  {
    return varcod;
  }

  public void setVarcod(String varcod)
  {
    this.varcod = varcod;
  }

  public String getVardecim()
  {
    return vardecim;
  }

  public void setVardecim(String vardecim)
  {
    this.vardecim = vardecim;
  }

  public String getVardesc()
  {
    return vardesc;
  }

  public void setVardesc(String vardesc)
  {
    this.vardesc = vardesc;
  }

  public String getVarlabel()
  {
    return varlabel;
  }

  public void setVarlabel(String varlabel)
  {
    this.varlabel = varlabel;
  }

  public String getVaropc()
  {
    return varopc;
  }

  public void setVaropc(String varopc)
  {
    this.varopc = varopc;
  }

  public int getVarsize()
  {
    return varsize;
  }

  public void setVarsize(int varsize)
  {
    this.varsize = varsize;
  }

  public String getVartip()
  {
    return vartip;
  }

  public void setVartip(String vartip)
  {
    this.vartip = vartip;
  }


  public void copyTo(WSEndpoint endpoint, PropertyDefinition propertyDef)
  {
    propertyDef.setName(endpoint.toGlobalId(PropertyDefinition.class,"VAR" + varcod));
    propertyDef.setDescription(vardesc);
    propertyDef.setSize(varsize);

    propertyDef.setHidden(false);
    propertyDef.setMinOccurs(0);
    propertyDef.setMaxOccurs(1);
    propertyDef.getValue().clear();
    propertyDef.setReadOnly(true);

    //PropertyType
    if (vartip.equalsIgnoreCase("A")
            || vartip.equalsIgnoreCase("TE"))
    {
      //ALFANUMERIC I TEXT
      propertyDef.setType(PropertyType.TEXT);
    } 
    else if (vartip.equalsIgnoreCase("D"))
    {
      //DATA
      propertyDef.setType(PropertyType.DATE);
    }
    else if (vartip.equalsIgnoreCase("T"))
    {
      //TEMPS 00:00
      propertyDef.setType(PropertyType.TEXT);
    }
    else if (vartip.equalsIgnoreCase("N"))
    {
      //NUMERO
      propertyDef.setType(PropertyType.NUMERIC);
    }
    else if (vartip.equalsIgnoreCase("FG"))
    {
      propertyDef.setType(PropertyType.TEXT);
      propertyDef.setHidden(true);
    }
    else {
      //DC - DOCUMENTS, FG - FLAGS
      propertyDef.setType(PropertyType.TEXT);
    }

    
    

  }

}
