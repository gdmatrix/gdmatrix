package com.audifilm.matrix.common.service;

import com.audifilm.matrix.util.ConfigProperties;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.matrix.util.Entity;

/**
 *
 * @author comasfc
 */
public class PKUtil
{
  private String ENTITY_PREFIX_SEPARATOR = "*";
  private String PK_SEPARATOR = ";";
  private String VERSION_SEPARATOR = "$";

  final static private PKUtil pkUtil = new PKUtil();

  protected PKUtil() {
    
    ENTITY_PREFIX_SEPARATOR = ConfigProperties.getProperty(
            "com.audifilm.matrix.common.service.entitySeparator", ENTITY_PREFIX_SEPARATOR);

    PK_SEPARATOR = ConfigProperties.getProperty(
            "com.audifilm.matrix.common.service.pkSeparator", PK_SEPARATOR);
 
    VERSION_SEPARATOR = ConfigProperties.getProperty(
            "com.audifilm.matrix.common.service.versionSeparator", VERSION_SEPARATOR);

  }

  static public String makeMatrixPK(Entity entity, String... pk) {
    return entity.toGlobalId(composePK(pk));
  }

  static public String extractFromMatrixPK(Entity entity, String globalPK) {
    return entity.toLocalId(globalPK);
  }

  static public String composePK(String... ids)
  {
    if (ids==null) return "";
    return StringUtils.join(ids, pkUtil.PK_SEPARATOR);
  }

  static public String [] decomposePK(String pk)
  {
    if (pk==null) return null;
    if (pk.length()==0) return new String[]{""};
    return pk.split(pkUtil.PK_SEPARATOR);
  }

  static public String globalIdListToLocalString(Entity entity, List<String> idList)
  {
    StringBuilder strBuff = new StringBuilder();
    for(String globalId : idList)
    {
      strBuff.append("," + entity.toLocalId(globalId) + ",");
    }
    return strBuff.toString();
  }

    static protected String extractVersionId(String pk)
  {
    if (pk==null || pk.length()==0) return null;

    int pos1 = pk.indexOf(pkUtil.VERSION_SEPARATOR);
    if (pos1 < 0) return null;

    int posPrefix = pk.indexOf(pkUtil.ENTITY_PREFIX_SEPARATOR, pos1);
    pos1 = (posPrefix<0)?pos1 + pkUtil.VERSION_SEPARATOR.length():posPrefix + pkUtil.ENTITY_PREFIX_SEPARATOR.length();

    int pos2 = pk.indexOf(pkUtil.VERSION_SEPARATOR, pos1);
    return pk.substring(pos1, (pos2<0?pk.length():pos2));
  }

  static protected String addVersion(String pk, String versionEntity, String version)
  {
    if (version==null || version.equals("")) return pk;
    return (pk==null?"":pk)
            + pkUtil.VERSION_SEPARATOR
            + versionEntity
            + pkUtil.ENTITY_PREFIX_SEPARATOR
            + version + pkUtil.VERSION_SEPARATOR;
  }

  static protected String extractVersionTypeId(String pk)
  {
    if (pk==null || pk.length()==0) return null;

    int pos1 = pk.indexOf(pkUtil.VERSION_SEPARATOR);
    if (pos1<0) return null;

    pos1 += pkUtil.VERSION_SEPARATOR.length();

    int pos2 = pk.indexOf(pkUtil.ENTITY_PREFIX_SEPARATOR, pos1);
    if (pos2<0 || pos2<=pos1) return null;

    return pk.substring(pos1, pos2);
  }

  /*
   * Extrau la part de l'identificador de la clau donada
   *
   */
  static public String extractIds(String pk) {
    if (pk==null || pk.length()==0) return "";
    int posVersion = pk.indexOf(pkUtil.VERSION_SEPARATOR);
    return (posVersion<0)? pk:pk.substring(0, posVersion);
  }
}
