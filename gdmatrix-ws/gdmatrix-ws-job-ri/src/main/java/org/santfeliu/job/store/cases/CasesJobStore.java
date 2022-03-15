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
package org.santfeliu.job.store.cases;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.activation.DataHandler;
import org.matrix.cases.Case;
import org.matrix.cases.CaseConstants;
import org.matrix.cases.CaseDocument;
import org.matrix.cases.CaseFilter;
import org.matrix.cases.CaseManagerPort;
import org.matrix.cases.CaseManagerService;
import org.matrix.cases.Intervention;
import org.matrix.cases.InterventionFilter;
import org.matrix.cases.InterventionView;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.DictionaryManagerPort;
import org.matrix.dic.DictionaryManagerService;
import org.matrix.dic.TypeFilter;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.job.Job;
import org.matrix.job.JobFilter;
import org.matrix.job.LogType;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.job.service.JobException;
import org.santfeliu.job.service.JobFiring;
import org.santfeliu.job.store.JobStore;
import org.santfeliu.util.FileDataSource;
import org.santfeliu.util.IOUtils;
import org.santfeliu.util.MatrixConfig;

/**
 *
 * @author blanquepa
 */
public class CasesJobStore implements JobStore
{
  public static final String JOB_TYPE = "JobCase";
  public static final String JOB_INTERVENTION_TYPE = "JobIntervention";  
  public static final String SUCCESS_INTERVENTION_TYPE = 
    "SuccessJobIntervention";
  public static final String ERROR_INTERVENTION_TYPE = 
    "ErrorJobIntervention";   
  public static final String JOB_CASE_DOCUMENT_TYPE = "JobCaseDocument";
  
  public CasesJobStore()
  {
  }
  
  @Override
  public void init() throws JobException
  {
    try
    {
      Thread thread = new Thread()
      {
        @Override
        public void run()
        {          
          createType(JOB_TYPE, 
            DictionaryConstants.CASE_TYPE, 
            "Scheduled task");
          
          createType(JOB_INTERVENTION_TYPE, 
            DictionaryConstants.INTERVENTION_TYPE, 
            "Execution of a scheduled task"); 
          
          createType(SUCCESS_INTERVENTION_TYPE, 
            JOB_INTERVENTION_TYPE, 
            "Successful execution of a scheduled task");
          
          createType(ERROR_INTERVENTION_TYPE, 
            JOB_INTERVENTION_TYPE, 
            "Erroneous execution of a scheduled task"); 
          
          createType(JOB_CASE_DOCUMENT_TYPE, 
            DictionaryConstants.CASE_DOCUMENT_TYPE, 
            "Execution log document of a scheduled task");             
        }
      };
      thread.start();
    }
    catch (Exception ex)
    {
      throw new JobException(ex);
    }  
  }
  
  
  @Override
  public Job storeJob(Job job) throws JobException
  {
    try
    {
      if (job != null)
      {
        Case cas = JobCaseConverter.jobToCase(job);
        String typeId = cas.getCaseTypeId();
        String prefix = "";
        if (typeId.contains(":"))
          prefix = typeId.substring(0, typeId.indexOf(":") + 1);
        Type type = TypeCache.getInstance().getType(typeId);
        if (type != null && type.isDerivedFrom(prefix + JOB_TYPE))
        {
          cas = getCaseManagerPort().storeCase(cas);
          if (job.getJobId() == null)
          {
            job.setJobId(cas.getCaseId());
          }
        } 
        else
          throw new JobException("INVALID_JOB_TYPE", job);
      }
      return job;
    } 
    catch (Exception ex)
    {
      throw new JobException(ex, job);
    }
  }
  
  @Override
  public Job loadJob(String jobId) throws JobException
  {
    try
    {    
      Case cas = getCaseManagerPort().loadCase(jobId);
      String typeId = cas.getCaseTypeId();
      String prefix = "";
      if (typeId.contains(":"))
        prefix = typeId.substring(0, typeId.indexOf(":") + 1);
      Type type = TypeCache.getInstance().getType(typeId);
      if (type.isDerivedFrom(prefix + JOB_TYPE))
      {
        Job job = JobCaseConverter.caseToJob(cas);
        return job;
      } 
      else
      {
        return null;
      }
    }
    catch (Exception ex)
    {
      throw new JobException(ex);
    }
  }

  @Override
  public boolean removeJob(String jobId) throws JobException
  {
    try
    {
      if (jobId != null)
      {
        Job job = loadJob(jobId);
        if (job != null)
        {
          return getCaseManagerPort().removeCase(jobId);
        }
      }
      return false;
    }
    catch (Exception ex)
    {
      throw new JobException(ex);
    }    
  }

  @Override
  public List<Job> findJobs(JobFilter jobFilter) throws JobException
  {
    try
    {
      List<Job> results = new ArrayList();
      
      CaseFilter caseFilter = new CaseFilter();
      String jobTypeId = jobFilter.getJobTypeId() != null ?
        jobFilter.getJobTypeId() : JOB_TYPE;
      caseFilter.setCaseTypeId(jobTypeId);
      caseFilter.setFromDate(jobFilter.getFromDate());
      caseFilter.setToDate(jobFilter.getToDate());
      caseFilter.setDateComparator(CaseConstants.ACTIVE_DATE_COMPARATOR);
      caseFilter.setFirstResult(jobFilter.getFirstResult());
      caseFilter.setMaxResults(jobFilter.getMaxResults());
      
      List<Case> cases = getCaseManagerPort().findCases(caseFilter);
      for (Case cas : cases)
      {
        cas = getCaseManagerPort().loadCase(cas.getCaseId());
        results.add(JobCaseConverter.caseToJob(cas));
      }
      
      return results;        
    }
    catch (Exception ex)
    {
      throw new JobException(ex);
    }
  }

  @Override
  public void storeJobFiring(JobFiring jobFiring) 
    throws JobException
  {
    try
    {
      CaseManagerPort port = getCaseManagerPort();      
      Document document = null; 
      LogType logType = jobFiring.getLogType();
      File logFile = jobFiring.getLogFile();          
      if (logFile != null)
      {
        boolean reuseLog = !logType.equals(LogType.MULTIPLE);        
        if (reuseLog)
          document = getLastLogDocument(jobFiring.getJobId());
       
        boolean createCaseDocument = logType.equals(LogType.MULTIPLE);        
        if (document == null)
        {
          document = new Document();
          document.setDocTypeId("Document");
          String startDateTime = jobFiring.getStartDateTime();
          String title = "Job " + jobFiring.getJobId() + "_" 
            + startDateTime + " log";
          document.setTitle(title); 
          createCaseDocument = true;
        }        
        
        FileDataSource fds = new FileDataSource(logFile, "text/plain");
        document.setContent(null);
        DocumentUtils.setContentData(document, new DataHandler(fds));
        document = getDocumentManagerClient().storeDocument(document);
                
        if (createCaseDocument)
        {
          CaseDocument caseDocument = new CaseDocument();
          caseDocument.setCaseId(jobFiring.getJobId());
          caseDocument.setDocId(document.getDocId());
          caseDocument.setCaseDocTypeId(JOB_CASE_DOCUMENT_TYPE); 
          port.storeCaseDocument(caseDocument);
        }
      }
      
      Intervention intervention = null;
      
      if (logType.equals(LogType.LAST))
      {
        JobFiring last = getLastJobFiring(jobFiring.getJobId());
        if (last != null)
        {
          last.setStartDateTime(jobFiring.getStartDateTime());
          last.setEndDateTime(jobFiring.getEndDateTime());
          last.setResponseType(jobFiring.getResponseType());
          last.setMessage(jobFiring.getMessage());
          intervention = JobFiringConverter.jobFiringToInt(last);   
          intervention.setIntId(last.getJobFiringId());
        }
      }
      
      if (intervention == null)
      {
        intervention = JobFiringConverter.jobFiringToInt(jobFiring);
        if (document != null)
        {
          DictionaryUtils.setProperty(intervention, "logDocId", 
            document.getDocId());
          DictionaryUtils.setProperty(intervention, "logTitle",
            document.getTitle());
        }        
      }
      
      port.storeIntervention(intervention);      
    }
    catch (Exception ex)
    {
      throw new JobException(ex);
    }
  }
  
  @Override
  public List<JobFiring> findJobFirings(String jobId, String fromDate, 
    String toDate) throws JobException
  {
    List<JobFiring> result = new ArrayList<JobFiring>();
    try
    {
      result = findJobFirings(jobId, fromDate, toDate, 0);
    }
    catch (Exception ex)
    {
      throw new JobException(ex);
    }
    return result;
  }
   
  @Override
  public JobFiring loadJobFiring(String jobFiringId) throws JobException
  {
    JobFiring jobFiring = null;
    try
    {
      CaseManagerPort port = getCaseManagerPort();
      Intervention inv = port.loadIntervention(jobFiringId);
      
      jobFiring = JobFiringConverter.intToJobFiring(inv);      
      
      String logDocId = jobFiring.getLogId();
      if (logDocId != null)
      {
        DocumentManagerClient client = getDocumentManagerClient();
        Document doc = client.loadDocument(logDocId, 0, ContentInfo.ALL);
        if (doc != null)
        {
          DataHandler dh = doc.getContent().getData();
          jobFiring.setLogFile(IOUtils.writeToFile(dh));
        }        
      }      
    } 
    catch (IOException ex)
    {
      throw new JobException(ex);
    }
    
    return jobFiring;
  }
  
  @Override
  public JobFiring getLastJobFiring(String jobId) throws JobException
  {
    try
    {
      List<JobFiring> firings = findJobFirings(jobId, null, null, 1);
      if (firings != null && !firings.isEmpty())
      {
        JobFiring lastFiring = firings.get(0);
        return loadJobFiring(lastFiring.getJobFiringId());
      }
      else
        return null;
    }
    catch (Exception ex)
    {
      throw new JobException(ex);
    }
  }
  
  private Document getLastLogDocument(String jobId) throws Exception
  {
    List<JobFiring> firings = findJobFirings(jobId, null, null, 1);
    if (firings != null && !firings.isEmpty())
    {    
      JobFiring lastFiring = firings.get(0);
      String docId = lastFiring.getLogId();
      if (docId != null)
      {
        DocumentManagerClient client = getDocumentManagerClient();
        return client.loadDocument(docId);
      }
      else
        return null;
    }
    else 
      return null;
  }
  
  private List<JobFiring> findJobFirings(String jobId, String fromDate, 
    String toDate, int maxResults) throws Exception
  {
    List<JobFiring> result = new ArrayList<JobFiring>();

    CaseManagerPort port = getCaseManagerPort();

    InterventionFilter filter = new InterventionFilter(); 
    filter.setDateComparator("1");
    filter.setCaseId(jobId);
    if (fromDate != null)
      filter.setFromDate(fromDate);
    if (toDate != null)
      filter.setToDate(toDate);
    //Assumes results sorted by startDate desc, startTime desc 
    filter.setMaxResults(maxResults); 
    List<InterventionView> intViews = port.findInterventionViews(filter);

    if (intViews != null && !intViews.isEmpty())
    {
      for (InterventionView intView : intViews)
      {
        JobFiring jobFiring = 
          JobFiringConverter.intToJobFiring(intView);
        result.add(jobFiring);
      }
    }

    return result;
  }  
  
  private DocumentManagerClient getDocumentManagerClient() 
    throws MalformedURLException
  {
    DocumentManagerClient client;

    String wsDirectoryURL =
      MatrixConfig.getClassProperty(getClass(), "wsDirectoryURL");

    if (wsDirectoryURL == null)
      wsDirectoryURL = MatrixConfig.getProperty("wsdirectory.url");   

    client = new DocumentManagerClient(new URL(wsDirectoryURL), 
      MatrixConfig.getProperty("adminCredentials.userId"), 
      MatrixConfig.getProperty("adminCredentials.password"));
    
    return client;    
  }
  
  private DictionaryManagerPort getDicManagerPort() 
    throws MalformedURLException
  {
    DictionaryManagerPort port = null;    

    String wsDirectoryURL =
      MatrixConfig.getClassProperty(getClass(), "wsDirectoryURL");

    if (wsDirectoryURL == null)
      wsDirectoryURL = MatrixConfig.getProperty("wsdirectory.url");

    WSDirectory wsDirectory =
      WSDirectory.getInstance(new URL(wsDirectoryURL));
    WSEndpoint endpoint = 
      wsDirectory.getEndpoint(DictionaryManagerService.class);
    port = endpoint.getPort(DictionaryManagerPort.class,
      MatrixConfig.getProperty("adminCredentials.userId"),
      MatrixConfig.getProperty("adminCredentials.password"));
    
    return port;
  }  
  
  private CaseManagerPort getCaseManagerPort() throws MalformedURLException
  {
    CaseManagerPort port;

    String wsDirectoryURL =
      MatrixConfig.getClassProperty(getClass(), "wsDirectoryURL");

    if (wsDirectoryURL == null)
      wsDirectoryURL = MatrixConfig.getProperty("wsdirectory.url");

    WSDirectory wsDirectory = 
      WSDirectory.getInstance(new URL(wsDirectoryURL));      
    WSEndpoint endpoint = wsDirectory.getEndpoint(CaseManagerService.class);
    port = endpoint.getPort(CaseManagerPort.class,
      MatrixConfig.getProperty("adminCredentials.userId"),
      MatrixConfig.getProperty("adminCredentials.password"));      

    return port;
  }

  private void createType(String typeId, String superTypeId, String description)
  { 
    try
    {
      DictionaryManagerPort port = getDicManagerPort();
      TypeFilter filter = new TypeFilter();
      filter.setTypeId(typeId);
      filter.setSuperTypeId(superTypeId);
      int counter = port.countTypes(filter);
      if (counter == 0)
      {
        org.matrix.dic.Type t = new org.matrix.dic.Type();
        t.setSuperTypeId(superTypeId);
        t.setTypeId(typeId);
        t.setDescription(description);
        t.setInstantiable(true);
        port.storeType(t);
      }    
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
