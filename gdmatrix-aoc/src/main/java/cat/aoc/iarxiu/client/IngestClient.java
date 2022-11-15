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
package cat.aoc.iarxiu.client;

import java.io.File;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import com.hp.iarxiu.core.schemas._2_0.ingest.ContentTypeHandlingType;
import com.hp.iarxiu.core.schemas._2_0.ingest.GetOfflineIngestStatusRequestDocument;
import com.hp.iarxiu.core.schemas._2_0.ingest.GetOfflineIngestStatusResponseDocument;
import com.hp.iarxiu.core.schemas._2_0.ingest.GetUploadTicketRequestDocument;
import com.hp.iarxiu.core.schemas._2_0.ingest.GetUploadTicketResponseDocument;
import com.hp.iarxiu.core.schemas._2_0.ingest.OfflineIngestInfoType;
import com.hp.iarxiu.core.schemas._2_0.ingest.OfflineUploadIngestRequestDocument;
import com.hp.iarxiu.core.schemas._2_0.ingest.OfflineUploadIngestResponseDocument;
import com.hp.iarxiu.core.schemas._2_0.ingest.GetUploadTicketResponseDocument.GetUploadTicketResponse;
import com.hp.iarxiu.core.schemas._2_0.ingest.OfflineUploadIngestRequestDocument.OfflineUploadIngestRequest;
import java.util.List;

/**
 *
 * @author blanquepa
 */
public class IngestClient
{

  private ProxyClient proxyClient;
  private String uploadServletUrl;

  public ProxyClient getProxyClient()
  {
    return proxyClient;
  }

  public void setProxyClient(ProxyClient proxyClient)
  {
    this.proxyClient = proxyClient;
  }

  public String getUploadServletUrl()
  {
    return uploadServletUrl;
  }

  public void setUploadServletUrl(String uploadServletUrl)
  {
    this.uploadServletUrl = uploadServletUrl;
  }

  public String getUploadTicket()
  {
    GetUploadTicketResponseDocument responseDocument
      = getUploadTicket(proxyClient);
    GetUploadTicketResponse response
      = responseDocument.getGetUploadTicketResponse();

    return response.getTicket();
  }

  public boolean uploadFiles(String ticket, File metsFile, List<File> attachedFiles)
  {
    boolean uploaded = false;
    if (attachedFiles != null && attachedFiles.size() > 0)
    {
      File[] files = new File[attachedFiles.size()];
      attachedFiles.toArray(files);
      uploaded = upload(ticket, metsFile, files, uploadServletUrl);
    }

    return uploaded;
  }

  public String offlineUploadIngest(String ticket, boolean preservation,
    ContentTypeHandlingType.Enum contentTypeHandlingType)
  {
    OfflineUploadIngestResponseDocument offlineUploadIngestResponseDocument
      = offlineUploadIngest(proxyClient, ticket, preservation,
        contentTypeHandlingType);

    return offlineUploadIngestResponseDocument.getOfflineUploadIngestResponse();
  }

  public OfflineIngestInfoType getOfflineIngestStatus(
    String ticket)
    throws Exception
  {
    return getOfflineIngestStatus(ticket, 0, 0);
  }

  public OfflineIngestInfoType getOfflineIngestStatus(
    String ticket, int counter, long time)
    throws InterruptedException
  {
    if (time == 0)
    {
      time = 10000; //default 10 seconds
    }
    if (counter == 0)
    {
      counter = 10; //default 10 times
    }
    OfflineIngestInfoType.Status.Enum status
      = OfflineIngestInfoType.Status.IN_PROCESS;
    OfflineIngestInfoType ingestInfo = null;

    while (status.equals(OfflineIngestInfoType.Status.IN_PROCESS) && counter != 0)
    {
      //esperem n segons entre cada consulta
      Thread.sleep(time);
      ingestInfo = getOfflineIngestInfo(proxyClient, ticket);
      status = ingestInfo.getStatus();
      if (counter > 0)
      {
        counter--;
      }
    }

    return ingestInfo;
  }

//******* Private **********************************************************
  /**
   * Upload del fitxer METS i dels fitxers adjunts al servidor.
   *
   * @param ticket tiquet
   * @param metsFilePath path absolut del fitxer METS
   * @param attachedFilesPaths paths absoluts dels fitxers adjuntats
   * @return true -> els fitxers s'han pujat correctament al servidor; false ->
   * hi ha hagut algun problema
   */
  private static boolean upload(String ticket, File metsFile,
    File[] attachedFiles, String uploadServletUrl)
  {
    try
    {
      HttpClient httpClient = new HttpClient();
      PostMethod post = new PostMethod(uploadServletUrl);

      //identificador del tiquet
      Part ticketPart = new StringPart("ticket", ticket);

      //arxiu METS
      System.out.println("Uploading mets.xml");
      Part metsPart = new FilePart("mets", "mets.xml", metsFile);

      //arxius adjunts
      Part[] filesParts = null;
      if (attachedFiles != null && attachedFiles.length > 0)
      {
        filesParts = new Part[attachedFiles.length];
        int i = 0;
        for (File file : attachedFiles)
        {
          System.out.println("Uploading " + file.getName());
          Part filePart = new FilePart("file", file);
          filesParts[i] = filePart;
          i++;
        }
      }

      Part[] parts = null;
      if (filesParts == null)
      {
        parts = new Part[]
        {
          ticketPart, metsPart
        };
      } else
      {
        parts = new Part[2 + filesParts.length];
        parts[0] = ticketPart;
        parts[1] = metsPart;
        for (int i = 0; i < filesParts.length; i++)
        {
          parts[i + 2] = filesParts[i];
        }
      }

      post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));

      int result = httpClient.executeMethod(post);

      if (result != 200)
      {
        return false;
      }

      return true;
    } catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }
  }

  private static GetUploadTicketResponseDocument getUploadTicket(ProxyClient proxy)
  {
    //petició de tiquet
    GetUploadTicketRequestDocument requestDocument
      = GetUploadTicketRequestDocument.Factory.newInstance();
    requestDocument.addNewGetUploadTicketRequest();

    //pintem la petició
    Utils.printXmlObject(requestDocument);
    //guardem la petició en un fitxer
//		requestDocument.save(new File(
//      System.getProperty("user.dir")+"\\samples\\GetUploadTicketRequest.xml"));

    //enviament de la petició
    long time = System.currentTimeMillis();
    GetUploadTicketResponseDocument responseDocument
      = (GetUploadTicketResponseDocument) proxy.send(requestDocument);
    time = System.currentTimeMillis() - time;

    //pintem la resposta
    Utils.printXmlObject(responseDocument);
    //guardem la resposta en un fitxer
//		responseDocument.save(new File(
//      System.getProperty("user.dir")+"\\samples\\GetUploadTicketResponse.xml"));

    return responseDocument;
  }

  private static OfflineUploadIngestResponseDocument offlineUploadIngest(
    ProxyClient proxy, String ticket, boolean preservation,
    ContentTypeHandlingType.Enum handlingType)
  {
    //petició d'ingrés
    OfflineUploadIngestRequestDocument offlineUploadIngestRequestDocument
      = OfflineUploadIngestRequestDocument.Factory.newInstance();
    OfflineUploadIngestRequest offlineUploadIngestRequest
      = offlineUploadIngestRequestDocument.addNewOfflineUploadIngestRequest();

    //tiquet
    offlineUploadIngestRequest.setUploadTicket(ticket);

    //preservació d'evidència:
    //true -> preservació d'evidència i de continguts
    //false -> només preservació dels continguts
    offlineUploadIngestRequest.setPreservation(preservation);

    //tipus del document:
    //replaceWithIntrospection ->  Si DROID identifica un format i és diferent a l'indicat pel client,
    //							   aleshores es substitueix pel detectat.
    //completeWithIntrospection -> Si DROID identifica un format i és diferent a l'indicat pel client,
    //   						   només s'informa el PREMIS amb les dades obtingudes amb DROID. Quan
    //   						   no hi ha coincidència, es respecten les dades que posa el client.
    //checkAndReject ->			   Si DROID identifica un format i és diferent a l'indicat pel client,
    //							   aleshores es rebutja el paquet.
    offlineUploadIngestRequest.setContentTypeHandling(handlingType);

    //pintem la petició
    Utils.printXmlObject(offlineUploadIngestRequestDocument);

    //enviament de la petició
    long time = System.currentTimeMillis();
    OfflineUploadIngestResponseDocument offlineUploadIngestResponseDocument
      = (OfflineUploadIngestResponseDocument) proxy.send(offlineUploadIngestRequestDocument);
    time = System.currentTimeMillis() - time;

    //pintem la resposta
    Utils.printXmlObject(offlineUploadIngestResponseDocument);

    return offlineUploadIngestResponseDocument;
  }

  private static OfflineIngestInfoType
    getOfflineIngestInfo(ProxyClient proxy, String ticket)
  {
    //petició d'ingrés
    GetOfflineIngestStatusRequestDocument getOfflineIngestStatusRequestDocument
      = GetOfflineIngestStatusRequestDocument.Factory.newInstance();
    getOfflineIngestStatusRequestDocument.setGetOfflineIngestStatusRequest(ticket);

    //pintem la petició
    Utils.printXmlObject(getOfflineIngestStatusRequestDocument);

    //enviament de la petició
    GetOfflineIngestStatusResponseDocument getOfflineIngestStatusResponseDocument
      = (GetOfflineIngestStatusResponseDocument) proxy.send(getOfflineIngestStatusRequestDocument);

    //pintem la resposta
    Utils.printXmlObject(getOfflineIngestStatusResponseDocument);

    OfflineIngestInfoType statusInfo
      = getOfflineIngestStatusResponseDocument.getGetOfflineIngestStatusResponse().
        getOfflineIngestInfo();

    return statusInfo;
  }

}
