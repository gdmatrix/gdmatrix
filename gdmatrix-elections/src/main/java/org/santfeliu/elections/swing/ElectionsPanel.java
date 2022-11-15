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
package org.santfeliu.elections.swing;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import org.matrix.elections.Call;
import org.matrix.elections.ElectionsManagerPort;
import org.santfeliu.elections.Results;
import org.santfeliu.swing.ErrorMessagePanel;
import org.santfeliu.swing.InfoBrowser;
import org.santfeliu.swing.Utilities;
import org.santfeliu.ws.WSPortFactory;

/**
 *
 * @author realor
 */
public class ElectionsPanel extends JPanel
{
  private Results results = null;
  private Results prevResults = null;

  private JPanel cardPanel = new JPanel();
  private CardLayout cardLayout = new CardLayout();
  private JPanel toolbarPanel = new JPanel();
  private JPanel presentationPanel = new JPanel();
  private JSplitPane splitPane = new JSplitPane();
  private JTabbedPane tabbedPane = new JTabbedPane();
  private JScrollPane councillScrollPane = new JScrollPane();
  private JScrollPane boardsScrollPane = new JScrollPane();
  private BoardsPanel boardsPanel = new BoardsPanel();
  private JLabel callLabel = new JLabel();
  private JComboBox callsComboBox = new JComboBox();
  private JLabel stateLabel = new JLabel();
  private CouncillorsPanel councillorsPanel = new CouncillorsPanel();
  private HemicyclePanel hemicyclePanel = new HemicyclePanel();
  private PieChartPanel pieChartPanel = new PieChartPanel();
  private BarChartPanel barChartPanel = new BarChartPanel();
  private JPanel rightPanel = new JPanel();
  private ScopePanel scopePanel = new ScopePanel();
  private BorderLayout borderLayout1 = new BorderLayout();
  private BorderLayout borderLayout2 = new BorderLayout();
  private BorderLayout borderLayout3 = new BorderLayout();
  private BorderLayout borderLayout4 = new BorderLayout();
  private JLabel statusLabel = new JLabel();
  private JLabel presentationLabel = new JLabel();
  private JButton helpButton = new JButton();
  private JLabel logoLabel = new JLabel();
  private JPanel leftPanel = new JPanel();
  private ImageIcon helpIcon;
  private ImageIcon logoIcon;
  private JCheckBox reloadCheckBox = new JCheckBox();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private InfoBrowser infoBrowser = null;
  private List<Call> calls;
  private DataUpdater dataUpdater = new DataUpdater();

  /* Renderers */
  private CallCellRenderer callCellRenderer = new CallCellRenderer();

  private static final String LOGOURL = "logoURL";
  private static final String HEADERURL = "headerURL";

  private Properties configProperties;
  private int numScruBoards = 0;

  public ElectionsPanel()
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public void setConfigProperties(Properties configProperties)
  {
    this.configProperties = configProperties;
    setLogo(configProperties.getProperty(LOGOURL));
    scopePanel.setHeader(configProperties.getProperty(HEADERURL));
  }

  private void jbInit()
    throws Exception
  {
    this.setSize(new Dimension(701, 595));
    this.setLayout(borderLayout1);
    callLabel.setText("Convocatòria:");

    cardPanel.setLayout(cardLayout);

    stateLabel.setText(" ");
    stateLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    stateLabel.setForeground(new Color(198, 0, 0));
    stateLabel.setMinimumSize(new Dimension(0, 0));
    stateLabel.setFont(new Font("Arial", Font.BOLD, 14));
    toolbarPanel.setLayout(gridBagLayout1);
    toolbarPanel.add(logoLabel,
      new GridBagConstraints(0, 0, 1, GridBagConstraints.REMAINDER,
      0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
      new Insets(0, 0, 0, 0), 0, 0));
    toolbarPanel.add(callLabel,
      new GridBagConstraints(1, 0, 1, 1,
      0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
      new Insets(2, 0, 2, 4), 0, 0));

    callsComboBox.setRenderer(callCellRenderer);
    callsComboBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent event)
          {
            numScruBoards = -1;
            dataUpdater.setCall((Call)callsComboBox.getSelectedItem());
          }
        });
    toolbarPanel.add(callsComboBox,
      new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
      GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
      new Insets(2, 0, 2, 4), 0, 0));
    toolbarPanel.add(reloadCheckBox,
      new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0,
      GridBagConstraints.WEST, GridBagConstraints.NONE,
      new Insets(2, 0, 2, 0), 0, 0));
    try
    {
      helpIcon =
          new ImageIcon(getClass().getResource("/org/santfeliu/elections/swing/resources/icon/help.png"));
      helpButton.setIcon(helpIcon);
      helpButton.setOpaque(false);
    }
    catch (Exception e)
    {
      System.out.println("No help icon found");
    }
    helpButton.setToolTipText("Ajuda");
    helpButton.setText("Ajuda");
    helpButton.setRolloverEnabled(true);
    helpButton.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            helpButton_actionPerformed(e);
          }
        });

    logoLabel.setToolTipText("null");
    reloadCheckBox.setText("Actualitza automàticament");
    reloadCheckBox.setSelected(true);
    reloadCheckBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            reloadCheckBox_actionPerformed(e);
          }
        });
    toolbarPanel.add(helpButton,
      new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
      GridBagConstraints.CENTER, GridBagConstraints.NONE,
      new Insets(4, 0, 4, 4), 0, 0));

    this.add(cardPanel, BorderLayout.NORTH);

    presentationPanel.setLayout(borderLayout4);
    presentationPanel.add(presentationLabel, BorderLayout.WEST);
    presentationLabel.setFont(new Font("Arial", Font.BOLD, 18));

    cardPanel.add(toolbarPanel, "toolBar");
    cardPanel.add(presentationPanel, "presentation");

    councillScrollPane.getViewport().add(councillorsPanel, null);
    councillScrollPane.addComponentListener(new ComponentAdapter()
    {
      @Override
      public void componentShown(ComponentEvent e)
      {
        councillorsPanel.animate();
      }
    });
    tabbedPane.addTab("Pastís", pieChartPanel);
    tabbedPane.addTab("Barres", barChartPanel);
    tabbedPane.addTab("Regidors", councillScrollPane);
    tabbedPane.addTab("Hemicicle", hemicyclePanel);
    boardsScrollPane.getViewport().add(boardsPanel, null);

    splitPane.setDividerLocation(240);
    splitPane.add(leftPanel, JSplitPane.LEFT);
    splitPane.add(rightPanel, JSplitPane.RIGHT);
    this.add(splitPane, BorderLayout.CENTER);

    leftPanel.setLayout(borderLayout3);
    leftPanel.add(stateLabel, BorderLayout.NORTH);
    leftPanel.add(boardsScrollPane, BorderLayout.CENTER);

    this.add(statusLabel, BorderLayout.SOUTH);
    rightPanel.setLayout(borderLayout2);
    statusLabel.setText("  ");
    statusLabel.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseReleased(MouseEvent e)
      {
        if (e.getClickCount() > 1)
        {
          if (!e.isShiftDown() && !e.isAltDown())
          {
            int value = JOptionPane.showConfirmDialog(
              SwingUtilities.getWindowAncestor(ElectionsPanel.this),
              "Vols activar el mode de presentació?", "Configuració",
              JOptionPane.YES_NO_OPTION);
            if (value == JOptionPane.YES_OPTION)
            {
              dataUpdater.setCycle(true);
              String text = configProperties.getProperty("appTitle",
                "Resultats electorals");
              Call call = (Call)callsComboBox.getSelectedItem();
              if (call != null)
              {
                int year = call.getDate().getYear();
                text += ": " + call.getDescription() + " " + year;
              }
              presentationLabel.setText(text);
              cardLayout.show(cardPanel, "presentation");
            }
            else if (value == JOptionPane.NO_OPTION)
            {
              dataUpdater.setCycle(false);
              cardLayout.show(cardPanel, "toolBar");
            }
          }
          else
          {
            scopePanel.setVisible(!scopePanel.isVisible());
          }
        }
      }
    });
    rightPanel.add(scopePanel, BorderLayout.NORTH);
    rightPanel.add(tabbedPane, BorderLayout.CENTER);
    boardsPanel.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent event)
      {
        results.setScope(
          boardsPanel.getCurrentDistrict(),
          boardsPanel.getCurrentSection(),
          boardsPanel.getCurrentBoardName());

        scopePanel.updateData(results);
        pieChartPanel.updateData(results);
        barChartPanel.updateData(results);
      }
    });
  }

  public void start()
  {
    dataUpdater.start();
  }

  private void helpButton_actionPerformed(ActionEvent e)
  {
    if (infoBrowser != null)
    {
      infoBrowser.setVisible(true);
    }
    else
    {
      infoBrowser = new InfoBrowser();
      URL helpDocument;
      try
      {
        helpDocument = new URL(configProperties.getProperty("helpDocumentURL"));
        infoBrowser.setURL(helpDocument);
        infoBrowser.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        infoBrowser.setSize(600, 500);
        infoBrowser.setTitle("Ajuda");
        Utilities.centerWindow(null, infoBrowser);
        infoBrowser.setVisible(true);
      }
      catch (MalformedURLException f)
      {
        ErrorMessagePanel.showErrorMessage(
          SwingUtilities.getWindowAncestor(this),
          new Dimension(500, 400), f);
      }
    }
  }

  private void setLogo(String logoURL)
  {
    try
    {
      if (logoURL == null) return;
      logoIcon = new ImageIcon(new URL(logoURL));
      logoLabel.setIcon(logoIcon);
      presentationLabel.setIcon(logoIcon);
    }
    catch (Exception e)
    {
      System.out.println("Logo not found !");
    }
  }

  private void reloadCheckBox_actionPerformed(ActionEvent e)
  {
    dataUpdater.setReload(reloadCheckBox.isSelected());
  }

  private Call findPreviousCall(Calendar calendar, String callId)
  {
    Call prevCall = null;
    int i = 0;
    while (i < calls.size() && prevCall == null)
    {
      Call call = calls.get(i);
      if (call.getCallId().equals(callId))
      {
        Calendar c1 = call.getDate().toGregorianCalendar();
        if (c1.compareTo(calendar) < 0)
        {
          prevCall = call;
        }
      }
      i++;
    }
    return prevCall;
  }

  class DataUpdater extends Thread
  {
    private Call call;
    private boolean reload = true;
    private boolean cycle = false;

    public Call getCall()
    {
      return call;
    }

    public void setCall(Call call)
    {
      this.call = call;
      reload = true;
    }

    public boolean isReload()
    {
      return reload;
    }

    public void setReload(boolean reload)
    {
      this.reload = reload;
    }

    public boolean isCycle()
    {
      return cycle;
    }

    public void setCycle(boolean cycle)
    {
      this.cycle = cycle;
    }

    @Override
    public void run()
    {
      while (true)
      {
        try
        {
          if (calls == null)
          {
            statusLabel.setText("Obtenint llista de convocatòries...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            ElectionsManagerPort port = WSPortFactory.getPort(
              ElectionsManagerPort.class,
              configProperties.getProperty("wsdlLocation"));

            calls = port.listCalls();
            if (!calls.isEmpty())
            {
              call = calls.get(0);
            }
            SwingUtilities.invokeLater(new Runnable()
            {
              public void run()
              {
                callsComboBox.removeAllItems();
                for (Call c : calls)
                {
                  callsComboBox.addItem(c);
                }
                statusLabel.setText(" ");
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
              }
            });
          }

          Call callA = call;
          final String type = callA.getCallId();

          if (reload)
          {
            // get new data
            try
            {
              statusLabel.setText("Obtenint dades...");
              setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

              // get data
              String district = null;
              String section = null;
              String boardName = null;
              if (results != null && numScruBoards != -1)
              {
                district = results.getCurrentDistrict();
                section = results.getCurrentSection();
                boardName = results.getCurrentBoardName();
              }

              String wsdl = configProperties.getProperty("wsdlLocation");
              Results newResults = new Results();
              newResults.setScope(district, section, boardName);
              newResults.setWsdlLocation(wsdl);
              Calendar calendar = callA.getDate().toGregorianCalendar();
              String callId = callA.getCallId().trim();
              int councillorsCount = callA.getCouncillorsCount();
              newResults.setCouncillorsCount(councillorsCount);
              newResults.loadData(calendar, callId);

              if (callId.equals("1")) // load previous call
              {
                Call callB = findPreviousCall(calendar, callId);
                if (callB != null)
                {
                  Results newPrevResults = new Results();
                  newPrevResults.setScope(district, section, boardName);
                  newPrevResults.setWsdlLocation(wsdl);

                  calendar = callB.getDate().toGregorianCalendar();
                  callId = callB.getCallId().trim();
                  councillorsCount = callB.getCouncillorsCount();
                  newPrevResults.setCouncillorsCount(councillorsCount);
                  newPrevResults.loadData(calendar, callId);
                  prevResults = newPrevResults;
                }
                else
                {
                  prevResults = null;
                }
              }
              results = newResults;

              // put new data on panels
              SwingUtilities.invokeAndWait(new Runnable()
              {
                public void run()
                {
                  if (results.getScrutinizedBoardsCount() != numScruBoards)
                  {
                    // update panels & components
                    boardsPanel.updateData(results);
                    scopePanel.updateData(results);
                    pieChartPanel.updateData(results);
                    barChartPanel.updateData(results);
                    councillorsPanel.updateData(results);
                    hemicyclePanel.updateData(results, prevResults);
                    numScruBoards = results.getScrutinizedBoardsCount();
                    if ("1".equals(type))
                    {
                      tabbedPane.setEnabledAt(2, true);
                      tabbedPane.setEnabledAt(3, true);
                    }
                    else
                    {
                      tabbedPane.setEnabledAt(2, false);
                      tabbedPane.setEnabledAt(3, false);
                      if (tabbedPane.getSelectedIndex() > 1)
                      {
                        tabbedPane.setSelectedIndex(0);
                      }
                    }
                  }
                  int sb = results.getScrutinizedBoardsCount();
                  int tb = results.getTotalBoardsCount();
                  String text = "" + sb + " / " + tb + " meses escrutades: ";
                  if (sb < tb)
                  {
                    text += "(" + (int)(Math.round(100.0 *
                      (double)sb / (double)tb)) + "%)";
                  }
                  stateLabel.setText(text);
                  if (sb < tb)
                  {
                    reload = true;
                    reloadCheckBox.setSelected(true);
                  }
                  else
                  {
                    reload = false;
                    reloadCheckBox.setSelected(false);
                  }
                  statusLabel.setText(" ");
                  repaint();
                }
              });
            }
            catch (Exception ex)
            {
              statusLabel.setText("Error (1)");
            }
            finally
            {
              setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
          }

          if (cycle)
          {
            int maxTabs = "1".equals(type) ? 4 : 2;
            int i = 0;
            while (i < maxTabs && callA == call)
            {
              tabbedPane.setSelectedIndex(i);
              int seconds = getTime("tab" + i + "Time");
              if (seconds > 0)
              {
                int j = 0;
                while (callA == call && j < seconds * 10)
                {
                  Thread.sleep(100);
                  j++;
                }
              }
              i = tabbedPane.getSelectedIndex() + 1;
            }
          }
          else
          {
            int seconds = getTime("reloadTime");
            int j = 0;
            while (callA == call && j < seconds * 10)
            {
              Thread.sleep(100);
              j++;
            }
          }
        }
        catch (Exception ex)
        {
          statusLabel.setText("Error (2)");
          try
          {
            Thread.sleep(10000);
          }
          catch (InterruptedException iex)
          {
          }
        }
      }
    }
  }

  private int getTime(String key)
  {
    String s = (String)configProperties.get(key);
    int seconds = 0;
    if (s != null)
    {
      try
      {
        seconds = Integer.parseInt(s);
      }
      catch (NumberFormatException e)
      {
      }
    }
    return seconds;
  }
}
