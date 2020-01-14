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
package org.santfeliu.workflow.swing.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.StyledEditorKit;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.santfeliu.security.util.BasicAuthorization;

import org.santfeliu.swing.PropertiesPanel;
import org.santfeliu.swing.SwingWorker;
import org.santfeliu.swing.text.XMLEditorKit;
import org.santfeliu.util.XMLPrinter;
import org.santfeliu.util.net.HttpClient;
import org.santfeliu.util.template.Template;
import org.santfeliu.workflow.node.WebServiceNode;

import org.w3c.dom.Document;


/**
 *
 * @author unknown
 */
public class WebServiceNodeTester extends JDialog
{
  private JPanel northPanel = new JPanel();
  private JPanel southPanel = new JPanel();
  private BorderLayout northBorderLayout = new BorderLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JButton testButton = new JButton();
  private JSplitPane centralSplitPane = new JSplitPane();
  private JSplitPane requestSplitPane = new JSplitPane();
  private JSplitPane responseSplitPane = new JSplitPane();
  private JPanel requestMessagePanel = new JPanel();
  private JPanel responseMessagePanel = new JPanel();
  private JLabel requestMessageLabel = new JLabel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private JScrollPane requestScrollPane = new JScrollPane();
  private JTextPane requestMessageTextPane = new JTextPane();
  private BorderLayout borderLayout3 = new BorderLayout();
  private JLabel responseMessageLabel = new JLabel();
  private JScrollPane responseScrollPane = new JScrollPane();
  private JTextPane responseMessageTextPane = new JTextPane();
  private JPanel requestPanel = new JPanel();
  private JPanel responsePanel = new JPanel();
  private BorderLayout borderLayout4 = new BorderLayout();
  private BorderLayout borderLayout5 = new BorderLayout();
  private JLabel inputVarsLabel = new JLabel();
  private JLabel outputVariablesLabel = new JLabel();
  private JLabel endpointLabel = new JLabel();
  private PropertiesPanel inputVariablesPanel = new PropertiesPanel();
  private PropertiesPanel outputVariablesPanel = new PropertiesPanel();
  private JPanel authoPanel = new JPanel();
  private FlowLayout flowLayout = new FlowLayout(FlowLayout.RIGHT, 2, 1);
  private JLabel usernameLabel = new JLabel();
  private JTextField usernameTextField = new JTextField();
  private JLabel passwordLabel = new JLabel();
  private JPasswordField passwordTextField = new JPasswordField();
  
  private String endpoint;
  private String requestMessage;
  private Map requestProperties;
  private Map expressions;
  private int connectTimeout;
  private int readTimeout;
  private StyledEditorKit textEditorKit = new StyledEditorKit();
  private XMLEditorKit xmlEditorKit = new XMLEditorKit();

  private static HashMap testValues = new HashMap();

  public WebServiceNodeTester(Dialog parent, String endpoint, 
    String requestMessage, Map requestProperties, Map expressions,
    int connectTimeout, int readTimeout)
  {
    super(parent, true);
    this.endpoint = endpoint;
    this.requestMessage = requestMessage;
    this.requestProperties = requestProperties;
    this.expressions = expressions;
    this.connectTimeout = connectTimeout;
    this.readTimeout = readTimeout;
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    this.setSize(new Dimension(800, 600));
    this.getContentPane().setLayout(borderLayout1);
    this.setTitle("WebService Tester");
    centralSplitPane.setDividerSize(8);
    centralSplitPane.setOneTouchExpandable(true);
    requestSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    requestSplitPane.setDividerLocation(200);
    responseSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    responseSplitPane.setDividerLocation(200);
    requestMessagePanel.setLayout(borderLayout2);
    requestMessagePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    responseMessagePanel.setLayout(borderLayout3);
    responseMessagePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    requestMessageLabel.setText("Request message:");
    responseMessageLabel.setText("Response message:");
    requestPanel.setLayout(borderLayout5);
    requestPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    responsePanel.setLayout(borderLayout4);
    responsePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    inputVarsLabel.setText("Input variables:");
    inputVarsLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
    outputVariablesLabel.setText("Output variables:");
    outputVariablesLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
    endpointLabel.setText("Endpoint: " + endpoint +
      " (" + connectTimeout + "/" + readTimeout + ")");
    endpointLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    requestMessagePanel.add(requestMessageLabel, BorderLayout.NORTH);
    requestScrollPane.getViewport().add(requestMessageTextPane, null);
    requestMessagePanel.add(requestScrollPane, BorderLayout.CENTER);
    requestSplitPane.add(requestMessagePanel, JSplitPane.BOTTOM);
    requestPanel.add(inputVarsLabel, BorderLayout.NORTH);
    requestPanel.add(inputVariablesPanel, BorderLayout.CENTER);
    requestSplitPane.add(requestPanel, JSplitPane.TOP);
    centralSplitPane.add(requestSplitPane, JSplitPane.LEFT);
    responseMessagePanel.add(responseMessageLabel, BorderLayout.NORTH);
    responseScrollPane.getViewport().add(responseMessageTextPane, null);
    responseMessagePanel.add(responseScrollPane, BorderLayout.CENTER);
    responseSplitPane.add(responseMessagePanel, JSplitPane.BOTTOM);
    responsePanel.add(outputVariablesLabel, BorderLayout.NORTH);
    responsePanel.add(outputVariablesPanel, BorderLayout.CENTER);
    responseSplitPane.add(responsePanel, JSplitPane.TOP);
    centralSplitPane.add(responseSplitPane, JSplitPane.RIGHT);
    centralSplitPane.setDividerLocation(390);
    centralSplitPane.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
    northPanel.setLayout(northBorderLayout);

    this.getContentPane().add(northPanel, BorderLayout.NORTH);
    this.getContentPane().add(centralSplitPane, BorderLayout.CENTER);
    this.getContentPane().add(southPanel, BorderLayout.SOUTH);

    northPanel.add(endpointLabel, BorderLayout.CENTER);
    northPanel.add(authoPanel, BorderLayout.EAST);
    usernameLabel.setText("Username:");
    authoPanel.add(usernameLabel);
    usernameTextField.setPreferredSize(new Dimension(100, 
      usernameTextField.getPreferredSize().height));
    authoPanel.add(usernameTextField);
    passwordTextField.setPreferredSize(new Dimension(100, 
      passwordTextField.getPreferredSize().height));
    passwordLabel.setText("Password:");
    authoPanel.add(passwordLabel);
    authoPanel.add(passwordTextField);

    southPanel.add(testButton, null);
    testButton.setText("Test");
    testButton.setActionCommand("testButton");
    testButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            testButton_actionPerformed(e);
          }
        });
    requestMessageTextPane.setEditable(false);
    requestMessageTextPane.setFont(new Font("Monospaced", 0, 14));
    requestMessageTextPane.setEditorKitForContentType(
      "text/plain", textEditorKit);
    requestMessageTextPane.setEditorKitForContentType(
      "text/xml", xmlEditorKit);
    requestMessageTextPane.setContentType("text/xml");

    requestMessageTextPane.setSelectionColor(new Color(181, 181, 181));
    responseMessageTextPane.setEditable(false);
    responseMessageTextPane.setFont(new Font("Monospaced", 0, 14));
    responseMessageTextPane.setEditorKitForContentType(
      "text/plain", textEditorKit);
    responseMessageTextPane.setEditorKitForContentType(
      "text/xml", xmlEditorKit);
    responseMessageTextPane.setContentType("text/xml");
    responseMessageTextPane.setSelectionColor(new Color(181, 181, 181));
    inputVariablesPanel.setPropertyLabel("Variable");
    outputVariablesPanel.setPropertyLabel("Variable");
    outputVariablesPanel.setEditable(false);
    fillInputVariables();
  }

  private void fillInputVariables()
  {
    HashMap map = new HashMap();  
    Set<String> variables = 
      Template.create(endpoint + requestMessage).getReferencedVariables();
    for (String variable : variables)
    {
      map.put(variable, testValues.get(variable));
    }
    inputVariablesPanel.setProperties(map);
  }

  private void testButton_actionPerformed(ActionEvent e)
  {
    testButton.setEnabled(false);
    requestMessageTextPane.setText("");
    responseMessageTextPane.setText("");

    Map inputMap = inputVariablesPanel.getProperties();
    Map outputMap = new HashMap();
    outputVariablesPanel.setProperties(outputMap);
    Invoker invoker = new Invoker(inputMap, outputMap);
    invoker.construct();
  }

  private class Invoker extends SwingWorker
  {
    private Map inputMap;
    private Map outputMap;
  
    public Invoker(Map inputMap, Map outputMap)
    {
      this.inputMap = inputMap;
      this.outputMap = outputMap;
    }

    @Override
    public void doWork()
    {
      HttpClient client = new HttpClient();
      try
      {
        testValues.putAll(inputMap);
        
        String url = Template.create(endpoint).merge(inputMap);
        final String message = Template.create(requestMessage).merge(inputMap);

        notify(0, message);

        client.setMaxContentLength(4096); //4kb
        client.setForceHttp(false);
        client.setURL(url);
        client.setConnectTimeout(connectTimeout * 1000);
        client.setReadTimeout(readTimeout * 1000);
        client.setRequestProperty("Content-Type", 
          "text/xml;charset=\"" + WebServiceNode.ENCODING + "\"");

        String username = usernameTextField.getText();
        char pass[] = passwordTextField.getPassword();
        if (username != null && username.length() > 0 && pass != null)
        {
          BasicAuthorization autho = new BasicAuthorization();
          autho.setUserId(username);
          autho.setPassword(new String(pass));
          String authoValue = autho.toString();
          System.out.println("Authorization:" + authoValue);
          client.setRequestProperty("Authorization", authoValue);
        }

        for (Object o : requestProperties.entrySet())
        {
          Map.Entry entry = (Map.Entry)o;
          String property = (String)entry.getKey();
          String value = (String)entry.getValue();
          client.setRequestProperty(property, value);
        }
        client.doPost(message.getBytes(WebServiceNode.ENCODING));
        
        Document document = client.getContentAsXML(true);
        if (document == null)
        {
          String error = client.getHeaderProperty(null);
          if (error == null)
          {
            throw new IOException("Can't read response.");
          }
          else
          {
            throw new IOException(error);
          }
        }
        XPath xPath = XPathFactory.newInstance().newXPath();
        Set set = expressions.entrySet();
        Iterator iter = set.iterator();
        while (iter.hasNext())
        {
          Map.Entry entry = (Map.Entry)iter.next();
          String variable = (String)entry.getKey();
          String xpathExpression = (String)entry.getValue();
          xpathExpression = Template.create(xpathExpression).merge(inputMap);
          String result = xPath.evaluate(xpathExpression, document);
          outputMap.put(variable, result);
        }
        XMLPrinter xmlPrinter = new XMLPrinter();
        notify(1, xmlPrinter.format(document.getDocumentElement()));
      }
      catch (final Exception ex)
      {
        String output = ex.toString();
        String content = client.getContentAsString();
        if (content != null) output += "\n\n" + content; 
        notify(2, output);
      }
    }
    
    protected void doNotify(int code, Object message)
    {
      switch (code)
      {
        case 0:
          requestMessageTextPane.setText((String)message);     
          requestMessageTextPane.setCaretPosition(0);
          responseMessageTextPane.setContentType("text/plain");
          responseMessageTextPane.setEditorKit(textEditorKit);
          responseMessageTextPane.setForeground(Color.BLACK);
          responseMessageTextPane.setText("Waiting for response...");
          responseMessageTextPane.setCaretPosition(0);
          break;
        case 1:
          responseMessageTextPane.setContentType("text/xml");
          responseMessageTextPane.setEditorKit(xmlEditorKit);
          responseMessageTextPane.setText((String)message);
          outputVariablesPanel.setProperties(outputMap);
          responseMessageTextPane.setCaretPosition(0);
          break;
        case 2:
          responseMessageTextPane.setContentType("text/plain");
          responseMessageTextPane.setEditorKit(textEditorKit);
          responseMessageTextPane.setForeground(new Color(200, 0, 0));
          responseMessageTextPane.setText((String)message);
          responseMessageTextPane.setCaretPosition(0);
      }
    }

    protected void doFinished()
    {
      testButton.setEnabled(true); 
    }
  }
}
