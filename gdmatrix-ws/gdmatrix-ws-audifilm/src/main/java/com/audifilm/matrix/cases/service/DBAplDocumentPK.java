package com.audifilm.matrix.cases.service;

import com.audifilm.matrix.common.service.GenesysPK;

/**
 *
 * @author comasfc
 */
public class DBAplDocumentPK extends GenesysPK
{
  String aplId;
  String docorigen;
  String docId;

  public DBAplDocumentPK()
  {

  }

  public DBAplDocumentPK(String aplId, String docOrigen, String docId)
  {
    super(aplId, docOrigen, docId);
    this.aplId = aplId;
    this.docorigen = docOrigen;
    this.docId = docId;
  }

  public DBAplDocumentPK(String pk)
  {
    super(pk);
    String ids[] = getIds();
    this.aplId = ids[0];
    this.docorigen = ids[1];
    this.docId = ids[2];
  }

  public String getAplId()
  {
    return aplId;
  }

  public void setAplId(String aplId)
  {
    this.aplId = aplId;
  }

  public String getDocId()
  {
    return docId;
  }

  public void setDocId(String docId)
  {
    this.docId = docId;
  }

  public String getDocorigen()
  {
    return docorigen;
  }

  public void setDocorigen(String docorigen)
  {
    this.docorigen = docorigen;
  }




}
