package org.santfeliu.faces.matrixclient.model;

/**
 *
 * @author blanquepa
 */
public class SignatureMatrixClientModel extends ServletMatrixClientModel
{
  public SignatureMatrixClientModel()
  {
    super();
    putParameter("signatureServletUrl", getServletUrl());
  }

  @Override
  protected String getServletName()
  {
    return "signatures";
  }
}
