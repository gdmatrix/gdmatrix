package com.audifilm.matrix.common.service;

import java.io.Serializable;

/**
 *
 * @author comasfc
 */
public interface GenesysPKInterface extends Comparable<GenesysPKInterface>, Serializable
{
  public String[] getIds();
  //public String getMatrixIdentifier();
}
