package com.audifilm.matrix.kernel.service;

import com.audifilm.matrix.common.service.GenesysPK;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author blanquepa
 */
public class DBPersonDocumentPK extends GenesysPK
{
  protected String aplcod;
  protected String docorigen;
  protected String doccod;
  protected String modelcod;
  protected String docnompc;
  protected String personId;

  public DBPersonDocumentPK(String aplcod, String docorigen, String doccod,
    String modelcod, String docnompc, String personId)
  {
    super(aplcod, docorigen, doccod, modelcod, docnompc, personId);
    this.aplcod = StringUtils.rightPad(aplcod, 4);
    this.docorigen = StringUtils.rightPad(docorigen, 10);
    this.doccod = StringUtils.rightPad(doccod, 4);
    this.modelcod = StringUtils.rightPad(modelcod, 4);
    this.docnompc = docnompc;
    this.personId = personId;
  }

  public DBPersonDocumentPK(String pk)
  {
    super(pk);
    String ids[] = getIds();
    this.aplcod = StringUtils.rightPad(ids[0], 4);
    this.docorigen = StringUtils.rightPad(ids[1], 10);
    this.doccod = StringUtils.rightPad(ids[2], 4);
    this.modelcod = StringUtils.rightPad(ids[3], 4);
    this.docnompc = ids[4];
    this.personId = ids[5];
  }

  public String getAplcod()
  {
    return aplcod;
  }

  public void setAplcod(String aplcod)
  {
    this.aplcod = aplcod;
  }

  public String getDoccod()
  {
    return doccod;
  }

  public void setDoccod(String doccod)
  {
    this.doccod = doccod;
  }

  public String getDocnompc()
  {
    return docnompc;
  }

  public void setDocnompc(String docnompc)
  {
    this.docnompc = docnompc;
  }

  public String getDocorigen()
  {
    return docorigen;
  }

  public void setDocorigen(String docorigen)
  {
    this.docorigen = docorigen;
  }

  public String getPersonId()
  {
    return personId;
  }

  public void setPersonId(String personId)
  {
    this.personId = personId;
  }

  public String getModelcod()
  {
    return modelcod;
  }

  public void setModelcod(String modelcod)
  {
    this.modelcod = modelcod;
  }
}
