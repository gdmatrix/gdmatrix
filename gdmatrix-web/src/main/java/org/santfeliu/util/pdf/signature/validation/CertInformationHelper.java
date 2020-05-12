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
package org.santfeliu.util.pdf.signature.validation;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.util.Hex;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.santfeliu.util.pdf.signature.validation.CertInformationCollector.CertSignatureInformation;


/**
 * Copied from PDFBox 2.0.17 examples, initial version:
 * https://svn.apache.org/viewvc/pdfbox/trunk/examples/src/main/java/org/apache/pdfbox/examples/signature/validation/CertInformationHelper.java?view=co
 * 
 */
/**
 *
 * @author unknown
 */
public class CertInformationHelper
{
    private static final Log LOG = LogFactory.getLog(CertInformationHelper.class);

    private CertInformationHelper()
    {
    }

    /**
     * Gets the SHA-1-Hash has of given byte[]-content.
     * 
     * @param content to be hashed
     * @return SHA-1 hash String
     */
    protected static String getSha1Hash(byte[] content)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return Hex.getString(md.digest(content));
        }
        catch (NoSuchAlgorithmException e)
        {
            LOG.error("No SHA-1 Algorithm found", e);
        }
        return null;
    }

    /**
     * Extracts authority information access extension values from the given data. The Data
     * structure has to be implemented as described in RFC 2459, 4.2.2.1.
     *
     * @param extensionValue byte[] of the extension value.
     * @param certInfo where to put the found values
     * @throws IOException when there is a problem with the extensionValue
     */
    protected static void getAuthorityInfoExtensionValue(byte[] extensionValue,
            CertSignatureInformation certInfo) throws IOException
    {
        ASN1Sequence asn1Seq = (ASN1Sequence) JcaX509ExtensionUtils.parseExtensionValue(extensionValue);
        Enumeration<?> objects = asn1Seq.getObjects();
        while (objects.hasMoreElements())
        {
            // AccessDescription
            ASN1Sequence obj = (ASN1Sequence) objects.nextElement();
            ASN1Encodable oid = obj.getObjectAt(0);
            // accessLocation
            ASN1TaggedObject location = (ASN1TaggedObject) obj.getObjectAt(1);

            if (X509ObjectIdentifiers.id_ad_ocsp.equals(oid)
                    && location.getTagNo() == GeneralName.uniformResourceIdentifier)
            {
                ASN1OctetString url = (ASN1OctetString) location.getObject();
                certInfo.setOcspUrl(new String(url.getOctets()));
            }
            else if (X509ObjectIdentifiers.id_ad_caIssuers.equals(oid))
            {
                ASN1OctetString uri = (ASN1OctetString) location.getObject();
                certInfo.setIssuerUrl(new String(uri.getOctets()));
            }
        }
    }

    /**
     * Gets the first CRL URL from given extension value. Structure has to be
     * built as in 4.2.1.14 CRL Distribution Points of RFC 2459.
     *
     * @param extensionValue to get the extension value from
     * @return first CRL- URL or null
     * @throws IOException when there is a problem with the extensionValue
     */
    protected static String getCrlUrlFromExtensionValue(byte[] extensionValue) throws IOException
    {
        ASN1Sequence asn1Seq = (ASN1Sequence) JcaX509ExtensionUtils.parseExtensionValue(extensionValue);
        Enumeration<?> objects = asn1Seq.getObjects();

        while (objects.hasMoreElements())
        {
            DLSequence obj = (DLSequence) objects.nextElement();

            ASN1TaggedObject taggedObject = (ASN1TaggedObject) obj.getObjectAt(0);
            taggedObject = (ASN1TaggedObject) taggedObject.getObject();
            if (taggedObject.getObject() instanceof ASN1TaggedObject)
              taggedObject = (ASN1TaggedObject) taggedObject.getObject(); // yes stmt is twice
            if (!(taggedObject.getObject() instanceof ASN1OctetString))
            {
                // happens with http://blogs.adobe.com/security/SampleSignedPDFDocument.pdf
                continue;
            }
            ASN1OctetString uri = (ASN1OctetString) taggedObject.getObject();
            String url = new String(uri.getOctets());
            // TODO Check for: DistributionPoint ::= SEQUENCE (see RFC 2459), multiples can be possible.

            // return first http(s)-Url for crl
            if (url.startsWith("http"))
            {
                return url;
            }
        }
        return null;
    }
}
