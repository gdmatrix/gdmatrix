package com.audifilm.matrix.common.service;

import org.matrix.util.Entity;


/**
 *
 * @author comasfc
 */
public class VersionIdentifier extends GenesysPK
{
  String versionTypeId;
  String [] versionId;
  String id;

  public VersionIdentifier(VersionType versionEntity, String [] ids, String [] versionId) {
    this.versionTypeId = versionEntity.getVersionTypeId();
    this.versionId = versionId;
    this.ids = ids;
  }

  public VersionIdentifier(VersionType versionEntity, String id, String [] versionId) {
    this(versionEntity, (id==null?null:new String[] {id}), versionId);
  }

  /*
  public VersionIdentifier(VersionType versionEntity, String id, String versionId) {
    this(versionEntity, (id==null?null:new String[] {id}), (versionId==null?null:new String[] {versionId}));
  }*/


  public VersionIdentifier(Entity entity, String globalId) {
    initialize(entity.toLocalId(globalId));
  }
  
  public VersionIdentifier(String localId) {
    initialize(localId);
  }

  private void initialize(String localId)
  {
    if (localId==null) return;

    this.versionId = PKUtil.decomposePK(PKUtil.extractVersionId(localId));
    this.versionTypeId = PKUtil.extractVersionTypeId(localId);
    this.id = PKUtil.extractIds(localId);
    this.ids = PKUtil.decomposePK(this.id);
  }

  public boolean hasVersion()
  {
    return !(versionId==null || (versionId.length<1) || (versionId.length==1 && versionId[0].equals("")));
  }

  public String getId()
  {
    return id;
  }

  public String [] getVersionId()
  {
    return versionId;
  }

  public String getVersionTypeId()
  {
    return versionTypeId;
  }


  @Override
  public String toString() {
   return getLocalVersionedId();
  }

  @Override
  public int hashCode()
  {
    return getLocalVersionedId().hashCode();
  }

  @Override
  public boolean equals(Object o)
  {
    if (o == this) return true;
    if (!(o instanceof VersionIdentifier)) return false;

    VersionIdentifier pk =(VersionIdentifier)o;
    return (pk!=null)?pk.toString().equals(toString()):false;
  }

  public String getLocalVersionedId()
  {
    if (ids==null || ids.length==0) return null;
    if (versionId==null) {
      return PKUtil.composePK(ids);
    }
    return PKUtil.addVersion(PKUtil.composePK(ids), versionTypeId, PKUtil.composePK(versionId));
  }

  public String toGlobalId(Entity entity)
  {
    return PKUtil.makeMatrixPK(entity,getLocalVersionedId());
  }



}
