/*
 * GDMatrix
 *
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *
 * This program is licensed and may be used, modified and redistributed under
 * the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European
 * Commission.
 *
 * Alternatively, you may redistribute and/or modify this program under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either  version 3 of the License, or (at your option)
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the licenses for the specific language governing permissions, limitations
 * and more details.
 *
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along
 * with this program; if not, you may find them at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/
 * and
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.signature;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import org.matrix.signature.DataHash;
import org.matrix.signature.DocumentValidation;


/**
 *
 * @author realor
 */
public interface SignedDocument
{
  public static String SHA1_RSA_ALGO = "SHA1withRSA";
  public static String SHA256_RSA_ALGO = "SHA256withRSA";

  // option codes for validateDocument method
  public static String PRESERVE_OPTION = "preserve";
  public static String VALIDATE_DATA_HASHES_OPTION = "validate_data_hashes";

  // ValidationDetail codes
  public static String SIGNATURE_PRESERVED_CODE = "info:signature_preserved";
  public static String CERTIFICATE_NOT_PRESENT_CODE = "warn:certificate_not_present";
  public static String DATA_NOT_INTACT_CODE = "error:data_not_intact";
  public static String INVALID_FORMAT_CODE = "error:invalid_format";
  public static String UNTRUSTED_SIGNATURE_CODE = "error:untrusted_signature";
  public static String UNTRUSTED_CERTIFICATE_CODE = "error:untrusted_certificate";
  public static String UNEXPECTED_EXCEPTION_CODE = "error:unexpected_exception";

  public void newDocument() throws Exception;

  public void parseDocument(InputStream is) throws Exception;

  public String addData(String dataType, byte[] data, Map properties)
    throws Exception;

  public byte[] addSignature(X509Certificate cert, String signAlgorithm,
    String policyId, String policyDigest) throws Exception;

  public void setSignatureValue(byte[] signatureData)
    throws Exception;

  public void removeSignature()
    throws Exception;

  public List<DataHash> digestData() throws Exception;

  public void addExternalSignature(byte[] signature) throws Exception;

  public DocumentValidation validate(List<String> options) throws Exception;

  public void writeDocument(OutputStream os) throws Exception;

  public void setId(String id);

  public String getId();

  public String getMimeType();

  public Map getProperties();
}
