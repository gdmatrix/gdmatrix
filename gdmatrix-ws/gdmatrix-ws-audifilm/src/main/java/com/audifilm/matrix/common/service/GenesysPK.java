/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.audifilm.matrix.common.service;

/**
 *
 * @author comasfc
 */
public class GenesysPK implements GenesysPKInterface
{
  String [] ids;

  public GenesysPK() {

  }

  public GenesysPK(DBGenesysEntity entity) {
    this.ids = entity.getIds();
  }
  

  public GenesysPK(String... ids) {
    this.ids = ids;
  }

  public GenesysPK(String ids) {
    this.ids = PKUtil.decomposePK(ids);
  }

  public String [] getIds() {
    return ids;
  }

  @Override
  public String toString() {
   return PKUtil.composePK(ids);
  }

  @Override
  public int hashCode()
  {
    return PKUtil.composePK(ids).hashCode();
  }

  @Override
  public boolean equals(Object o)
  {
    if (o == this) return true;
    if (!(o instanceof GenesysPK)) return false;

    return ((GenesysPK)o).toString().equals(toString());
  }

  public int compareTo(GenesysPKInterface o)
  {
    return this.getIds().toString().compareTo( o.getIds().toString() );
  }
  
}
