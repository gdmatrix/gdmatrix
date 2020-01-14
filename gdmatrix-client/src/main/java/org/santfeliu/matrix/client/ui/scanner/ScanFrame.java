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
package org.santfeliu.matrix.client.ui.scanner;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.ResourceBundle;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.santfeliu.matrix.client.Command;
import org.santfeliu.matrix.client.cmd.doc.ScanDocumentCommand;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.XmpSerializer;

/**
 *
 * @author realor
 */
public class ScanFrame extends javax.swing.JFrame
{
  int pageCounter = 0;
  CmdTwain cmdTwain = new CmdTwain();
  ResourceBundle bundle;
  ScanDocumentCommand command;
  Runnable callback;
  File pdfFile;
  /**
   * Creates new form ScanFrame
   */
  public ScanFrame()
  {
    initComponents();
    addWindowListener(new java.awt.event.WindowAdapter()
    {
      @Override
      public void windowClosing(java.awt.event.WindowEvent evt)
      {
        pdfFile = null;
        if (command != null)
        {
          command.setAcquired(true);
        }
        else if (callback != null)
        {
          callback.run();
        }        
        setVisible(false);
        dispose();
      }
    });
    try
    {
      bundle = java.util.ResourceBundle.getBundle(
        "org/santfeliu/matrix/client/ui/scanner/resources/ScanFrame"); // NOI18N
      setIconImage(ImageIO.read(getClass().getResource("resources/images/source.png")));
    }
    catch (Exception ex)
    {
    }
  }

  public Command getCommand()
  {
    return command;
  }

  public void setCommand(ScanDocumentCommand command)
  {
    this.command = command;
  }

  public Runnable getCallback()
  {
    return callback;
  }

  public void setCallback(Runnable callback)
  {
    this.callback = callback;
  }

  public File getPdfFile()
  {
    return pdfFile;
  }

  public void showFrame()
  {
    addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowActivated(WindowEvent e)
      {
        setAlwaysOnTop(false);
      }
    });
    setLocationRelativeTo(null);
    setVisible(true);
    setAlwaysOnTop(true);
  }
  
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    toolBar = new javax.swing.JToolBar();
    sourceButton = new javax.swing.JButton();
    optionsButton = new javax.swing.JButton();
    scanBlackWhiteButton = new javax.swing.JButton();
    scanGrayButton = new javax.swing.JButton();
    scanColorButton = new javax.swing.JButton();
    zoomInButton = new javax.swing.JButton();
    zoomOutButton = new javax.swing.JButton();
    rotateLeftButton = new javax.swing.JButton();
    rotateRightButton = new javax.swing.JButton();
    moveLeft = new javax.swing.JButton();
    moveRight = new javax.swing.JButton();
    clearAllButton = new javax.swing.JButton();
    pdfButton = new javax.swing.JButton();
    tabbedPane = new javax.swing.JTabbedPane();

    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/santfeliu/matrix/client/ui/scanner/resources/ScanFrame"); // NOI18N
    setTitle(bundle.getString("title")); // NOI18N
    setPreferredSize(new java.awt.Dimension(860, 600));

    toolBar.setFloatable(false);
    toolBar.setRollover(true);

    sourceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/santfeliu/matrix/client/ui/scanner/resources/images/source.png"))); // NOI18N
    sourceButton.setText(bundle.getString("selectSource")); // NOI18N
    sourceButton.setToolTipText(bundle.getString("selectSource")); // NOI18N
    sourceButton.setFocusable(false);
    sourceButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    sourceButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    sourceButton.setMaximumSize(new java.awt.Dimension(64, 64));
    sourceButton.setMinimumSize(new java.awt.Dimension(64, 64));
    sourceButton.setPreferredSize(new java.awt.Dimension(64, 64));
    sourceButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    sourceButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        sourceButtonActionPerformed(evt);
      }
    });
    toolBar.add(sourceButton);

    optionsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/santfeliu/matrix/client/ui/scanner/resources/images/options.png"))); // NOI18N
    optionsButton.setText(bundle.getString("options")); // NOI18N
    optionsButton.setToolTipText(bundle.getString("options")); // NOI18N
    optionsButton.setFocusable(false);
    optionsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    optionsButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    optionsButton.setMaximumSize(new java.awt.Dimension(64, 64));
    optionsButton.setMinimumSize(new java.awt.Dimension(64, 64));
    optionsButton.setPreferredSize(new java.awt.Dimension(64, 64));
    optionsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    optionsButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        optionsButtonActionPerformed(evt);
      }
    });
    toolBar.add(optionsButton);

    scanBlackWhiteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/santfeliu/matrix/client/ui/scanner/resources/images/bw.png"))); // NOI18N
    scanBlackWhiteButton.setText(bundle.getString("scanBW")); // NOI18N
    scanBlackWhiteButton.setToolTipText(bundle.getString("scanBW")); // NOI18N
    scanBlackWhiteButton.setFocusable(false);
    scanBlackWhiteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    scanBlackWhiteButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    scanBlackWhiteButton.setMaximumSize(new java.awt.Dimension(64, 64));
    scanBlackWhiteButton.setMinimumSize(new java.awt.Dimension(64, 64));
    scanBlackWhiteButton.setPreferredSize(new java.awt.Dimension(64, 64));
    scanBlackWhiteButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    scanBlackWhiteButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        scanBlackWhiteButtonActionPerformed(evt);
      }
    });
    toolBar.add(scanBlackWhiteButton);

    scanGrayButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/santfeliu/matrix/client/ui/scanner/resources/images/gray.png"))); // NOI18N
    scanGrayButton.setText(bundle.getString("scanGray")); // NOI18N
    scanGrayButton.setToolTipText(bundle.getString("scanGray")); // NOI18N
    scanGrayButton.setFocusable(false);
    scanGrayButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    scanGrayButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    scanGrayButton.setMaximumSize(new java.awt.Dimension(64, 64));
    scanGrayButton.setMinimumSize(new java.awt.Dimension(64, 64));
    scanGrayButton.setPreferredSize(new java.awt.Dimension(64, 64));
    scanGrayButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    scanGrayButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        scanGrayButtonActionPerformed(evt);
      }
    });
    toolBar.add(scanGrayButton);

    scanColorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/santfeliu/matrix/client/ui/scanner/resources/images/color.png"))); // NOI18N
    scanColorButton.setText(bundle.getString("scanColor")); // NOI18N
    scanColorButton.setToolTipText(bundle.getString("scanColor")); // NOI18N
    scanColorButton.setFocusable(false);
    scanColorButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    scanColorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    scanColorButton.setMaximumSize(new java.awt.Dimension(64, 64));
    scanColorButton.setMinimumSize(new java.awt.Dimension(64, 64));
    scanColorButton.setPreferredSize(new java.awt.Dimension(64, 64));
    scanColorButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    scanColorButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        scanColorButtonActionPerformed(evt);
      }
    });
    toolBar.add(scanColorButton);

    zoomInButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/santfeliu/matrix/client/ui/scanner/resources/images/zoomin.png"))); // NOI18N
    zoomInButton.setText(bundle.getString("zoomIn")); // NOI18N
    zoomInButton.setToolTipText(bundle.getString("zoomIn")); // NOI18N
    zoomInButton.setFocusable(false);
    zoomInButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    zoomInButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    zoomInButton.setMaximumSize(new java.awt.Dimension(64, 64));
    zoomInButton.setMinimumSize(new java.awt.Dimension(64, 64));
    zoomInButton.setPreferredSize(new java.awt.Dimension(64, 64));
    zoomInButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    zoomInButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        zoomInButtonActionPerformed(evt);
      }
    });
    toolBar.add(zoomInButton);

    zoomOutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/santfeliu/matrix/client/ui/scanner/resources/images/zoomout.png"))); // NOI18N
    zoomOutButton.setText(bundle.getString("zoomOut")); // NOI18N
    zoomOutButton.setToolTipText(bundle.getString("zoomOut")); // NOI18N
    zoomOutButton.setFocusable(false);
    zoomOutButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    zoomOutButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    zoomOutButton.setMaximumSize(new java.awt.Dimension(64, 64));
    zoomOutButton.setMinimumSize(new java.awt.Dimension(64, 64));
    zoomOutButton.setPreferredSize(new java.awt.Dimension(64, 64));
    zoomOutButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    zoomOutButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        zoomOutButtonActionPerformed(evt);
      }
    });
    toolBar.add(zoomOutButton);

    rotateLeftButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/santfeliu/matrix/client/ui/scanner/resources/images/rotate_left.png"))); // NOI18N
    rotateLeftButton.setText(bundle.getString("rotateLeft")); // NOI18N
    rotateLeftButton.setToolTipText(bundle.getString("rotateLeft")); // NOI18N
    rotateLeftButton.setFocusable(false);
    rotateLeftButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    rotateLeftButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    rotateLeftButton.setMaximumSize(new java.awt.Dimension(64, 64));
    rotateLeftButton.setMinimumSize(new java.awt.Dimension(64, 64));
    rotateLeftButton.setPreferredSize(new java.awt.Dimension(64, 64));
    rotateLeftButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    rotateLeftButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        rotateLeftButtonActionPerformed(evt);
      }
    });
    toolBar.add(rotateLeftButton);

    rotateRightButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/santfeliu/matrix/client/ui/scanner/resources/images/rotate_right.png"))); // NOI18N
    rotateRightButton.setText(bundle.getString("rotateRight")); // NOI18N
    rotateRightButton.setToolTipText(bundle.getString("rotateRight")); // NOI18N
    rotateRightButton.setFocusable(false);
    rotateRightButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    rotateRightButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    rotateRightButton.setMaximumSize(new java.awt.Dimension(64, 64));
    rotateRightButton.setMinimumSize(new java.awt.Dimension(64, 64));
    rotateRightButton.setPreferredSize(new java.awt.Dimension(64, 64));
    rotateRightButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    rotateRightButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        rotateRightButtonActionPerformed(evt);
      }
    });
    toolBar.add(rotateRightButton);

    moveLeft.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/santfeliu/matrix/client/ui/scanner/resources/images/move_left.png"))); // NOI18N
    moveLeft.setText(bundle.getString("moveLeft")); // NOI18N
    moveLeft.setToolTipText(bundle.getString("moveLeft")); // NOI18N
    moveLeft.setFocusable(false);
    moveLeft.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    moveLeft.setMargin(new java.awt.Insets(0, 0, 0, 0));
    moveLeft.setMaximumSize(new java.awt.Dimension(64, 64));
    moveLeft.setMinimumSize(new java.awt.Dimension(64, 64));
    moveLeft.setPreferredSize(new java.awt.Dimension(64, 64));
    moveLeft.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    moveLeft.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        moveLeftActionPerformed(evt);
      }
    });
    toolBar.add(moveLeft);

    moveRight.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/santfeliu/matrix/client/ui/scanner/resources/images/move_right.png"))); // NOI18N
    moveRight.setText(bundle.getString("moveRight")); // NOI18N
    moveRight.setToolTipText(bundle.getString("moveRight")); // NOI18N
    moveRight.setFocusable(false);
    moveRight.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    moveRight.setMargin(new java.awt.Insets(0, 0, 0, 0));
    moveRight.setMaximumSize(new java.awt.Dimension(64, 64));
    moveRight.setMinimumSize(new java.awt.Dimension(64, 64));
    moveRight.setPreferredSize(new java.awt.Dimension(64, 64));
    moveRight.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    moveRight.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        moveRightActionPerformed(evt);
      }
    });
    toolBar.add(moveRight);

    clearAllButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/santfeliu/matrix/client/ui/scanner/resources/images/clear.png"))); // NOI18N
    clearAllButton.setText(bundle.getString("clearAll")); // NOI18N
    clearAllButton.setToolTipText(bundle.getString("clearAll")); // NOI18N
    clearAllButton.setFocusable(false);
    clearAllButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    clearAllButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    clearAllButton.setMaximumSize(new java.awt.Dimension(64, 64));
    clearAllButton.setMinimumSize(new java.awt.Dimension(64, 64));
    clearAllButton.setPreferredSize(new java.awt.Dimension(64, 64));
    clearAllButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    clearAllButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        clearAllButtonActionPerformed(evt);
      }
    });
    toolBar.add(clearAllButton);

    pdfButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/santfeliu/matrix/client/ui/scanner/resources/images/pdf.png"))); // NOI18N
    pdfButton.setText(bundle.getString("makePdf")); // NOI18N
    pdfButton.setToolTipText(bundle.getString("makePdf")); // NOI18N
    pdfButton.setFocusable(false);
    pdfButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    pdfButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    pdfButton.setMaximumSize(new java.awt.Dimension(64, 64));
    pdfButton.setMinimumSize(new java.awt.Dimension(64, 64));
    pdfButton.setPreferredSize(new java.awt.Dimension(64, 64));
    pdfButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    pdfButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        pdfButtonActionPerformed(evt);
      }
    });
    toolBar.add(pdfButton);

    getContentPane().add(toolBar, java.awt.BorderLayout.PAGE_START);

    tabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
    getContentPane().add(tabbedPane, java.awt.BorderLayout.CENTER);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void scanBlackWhiteButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_scanBlackWhiteButtonActionPerformed
  {//GEN-HEADEREND:event_scanBlackWhiteButtonActionPerformed
    scan(CmdTwain.BW_MODE, 300);
  }//GEN-LAST:event_scanBlackWhiteButtonActionPerformed

  private void pdfButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_pdfButtonActionPerformed
  {//GEN-HEADEREND:event_pdfButtonActionPerformed
    try
    {
      int count = tabbedPane.getTabCount();
      if (count == 0) return;
      
      float compression = 0.4f;
      JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
      jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
      jpegParams.setCompressionQuality(compression);

      File[] imageFiles = new File[count];
      for (int i = 0; i < count; i++)
      {
        ImagePanel imagePanel = (ImagePanel)tabbedPane.getComponentAt(i);
        BufferedImage image = imagePanel.getImage();
        if (image.getColorModel().getPixelSize() == 1)
        {
          imageFiles[i] = File.createTempFile("image", ".png");
          ImageIO.write(image, "png", imageFiles[i]);
        }
        else
        {
          imageFiles[i] = File.createTempFile("image", ".jpg");
          ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
          FileImageOutputStream os = new FileImageOutputStream(
            imageFiles[i]);
          try
          {
            writer.setOutput(os);
            writer.write(null, new IIOImage(image, null, null), jpegParams);
          }
          finally
          {
            os.close();
          }
        }
      }
      
      try (PDDocument doc = new PDDocument())
      {

        pdfFile = File.createTempFile("authcopy_", ".pdf");

        PDDocumentInformation info = new PDDocumentInformation();
        info.setAuthor(System.getProperty("user.name"));
        info.setCreator("MatrixClient");
        info.setProducer("Apache PDFBox " + org.apache.pdfbox.util.Version.getVersion());
        info.setCreationDate(Calendar.getInstance());
        info.setCustomMetadataValue("scan_mode", cmdTwain.getMode());   
        info.setCustomMetadataValue("scan_dpi", String.valueOf(cmdTwain.getDpi()));             
        info.setCustomMetadataValue("scan_jpeg_compression", String.valueOf(compression));   

        // add XMP metadata
        XMPMetadata xmp = XMPMetadata.createXMPMetadata();          
        try
        {
            DublinCoreSchema dc = xmp.createAndAddDublinCoreSchema();
            dc.setTitle(pdfFile.getName());

            PDFAIdentificationSchema id = xmp.createAndAddPFAIdentificationSchema();
            id.setPart(3);
            id.setConformance("B");
            xmp.addSchema(id);              

            XmpSerializer serializer = new XmpSerializer();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            serializer.serialize(xmp, baos, true);

            PDMetadata metadata = new PDMetadata(doc);
            metadata.importXMPMetadata(baos.toByteArray());
            doc.getDocumentCatalog().setMetadata(metadata);

        }
        catch(BadFieldValueException e)
        {
            // won't happen here, as the provided value is valid
            throw new IllegalArgumentException(e);
        }  

        for (int i = 0; i < count; i++)
        {      
          PDPage page = new PDPage();
          doc.addPage(page);

          String image = imageFiles[i].getAbsolutePath();
          PDImageXObject pdImage = PDImageXObject.createFromFile(image, doc);

          PDPageContentStream contents = new PDPageContentStream(doc, page);
          PDRectangle mediaBox = page.getMediaBox();
          contents.drawImage(pdImage, 
            0, 0, mediaBox.getWidth(), mediaBox.getHeight());

          contents.close();
        }

        doc.setDocumentInformation(info);
        doc.save(new FileOutputStream(pdfFile));
      }      
      
      
      for (int i = 0; i < count; i++)
      {
        imageFiles[i].delete();
      }
      if (command == null)
      {
        if (callback != null)
        {
          callback.run();
          setVisible(false);
          dispose();
        }
        try
        {
          Desktop.getDesktop().open(pdfFile);
        }
        catch (Exception ex)
        {          
        }
      }
      else
      {
        ServletConnection conn = command.getConnection();
        OutputStream os = conn.getOutputStream();
        try
        {
          FileInputStream is = new FileInputStream(pdfFile);
          byte data[] = new byte[1024];
          try
          {
            int nr = is.read(data);
            while (nr != -1)
            {
              os.write(data, 0, nr);
              nr = is.read(data);
            }
            conn.flush();
            conn.parseResponse(command);
          }
          finally
          {
            is.close();
          }
        }
        finally
        {
          os.close();
        }
        command.setAcquired(true);
        setVisible(false);
        dispose();
        pdfFile.delete();
        pdfFile = null;
      }
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(this, ex.toString(),
        "ERROR", JOptionPane.ERROR_MESSAGE);
    }
  }//GEN-LAST:event_pdfButtonActionPerformed

  private void sourceButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_sourceButtonActionPerformed
  {//GEN-HEADEREND:event_sourceButtonActionPerformed
    try
    {
      cmdTwain.selectSource();
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(this, ex.toString(),
        "ERROR", JOptionPane.ERROR_MESSAGE);
    }
  }//GEN-LAST:event_sourceButtonActionPerformed

  private void scanGrayButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_scanGrayButtonActionPerformed
  {//GEN-HEADEREND:event_scanGrayButtonActionPerformed
    scan(CmdTwain.GRAY_MODE, 200);
  }//GEN-LAST:event_scanGrayButtonActionPerformed

  private void scanColorButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_scanColorButtonActionPerformed
  {//GEN-HEADEREND:event_scanColorButtonActionPerformed
    scan(CmdTwain.RGB_MODE, 200);
  }//GEN-LAST:event_scanColorButtonActionPerformed

  private void clearAllButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_clearAllButtonActionPerformed
  {//GEN-HEADEREND:event_clearAllButtonActionPerformed
    tabbedPane.removeAll();
    pageCounter = 0;
  }//GEN-LAST:event_clearAllButtonActionPerformed

  private void zoomInButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_zoomInButtonActionPerformed
  {//GEN-HEADEREND:event_zoomInButtonActionPerformed
    ImagePanel imagePanel = (ImagePanel)tabbedPane.getSelectedComponent();
    if (imagePanel != null)
    {
      imagePanel.zoomIn();
    }
  }//GEN-LAST:event_zoomInButtonActionPerformed

  private void zoomOutButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_zoomOutButtonActionPerformed
  {//GEN-HEADEREND:event_zoomOutButtonActionPerformed
    ImagePanel imagePanel = (ImagePanel)tabbedPane.getSelectedComponent();
    if (imagePanel != null)
    {
      imagePanel.zoomOut();
    }
  }//GEN-LAST:event_zoomOutButtonActionPerformed

  private void rotateLeftButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_rotateLeftButtonActionPerformed
  {//GEN-HEADEREND:event_rotateLeftButtonActionPerformed
    ImagePanel imagePanel = (ImagePanel)tabbedPane.getSelectedComponent();
    if (imagePanel != null)
    {
      imagePanel.rotateLeft();
    }
  }//GEN-LAST:event_rotateLeftButtonActionPerformed

  private void rotateRightButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_rotateRightButtonActionPerformed
  {//GEN-HEADEREND:event_rotateRightButtonActionPerformed
    ImagePanel imagePanel = (ImagePanel)tabbedPane.getSelectedComponent();
    if (imagePanel != null)
    {
      imagePanel.rotateRight();
    }
  }//GEN-LAST:event_rotateRightButtonActionPerformed

  private void moveLeftActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_moveLeftActionPerformed
  {//GEN-HEADEREND:event_moveLeftActionPerformed
    int index = tabbedPane.getSelectedIndex();
    if (index >= 1)
    {
      ImagePanel imagePanel = (ImagePanel)tabbedPane.getSelectedComponent();
      TabPanel tabPanel = (TabPanel)tabbedPane.getTabComponentAt(index);
      tabbedPane.remove(imagePanel);

      tabbedPane.insertTab(tabPanel.getTitle(), null, imagePanel, null, index - 1);
      tabbedPane.setTabComponentAt(index - 1, tabPanel);
      tabbedPane.setSelectedIndex(index - 1);
    }
  }//GEN-LAST:event_moveLeftActionPerformed

  private void moveRightActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_moveRightActionPerformed
  {//GEN-HEADEREND:event_moveRightActionPerformed
    int index = tabbedPane.getSelectedIndex();
    if (index >= 0 && index < tabbedPane.getTabCount() - 1)
    {
      ImagePanel imagePanel = (ImagePanel)tabbedPane.getSelectedComponent();
      TabPanel tabPanel = (TabPanel)tabbedPane.getTabComponentAt(index);
      tabbedPane.remove(imagePanel);

      tabbedPane.insertTab(tabPanel.getTitle(), null, imagePanel, null, index + 1);
      tabbedPane.setTabComponentAt(index + 1, tabPanel);
      tabbedPane.setSelectedIndex(index + 1);
    }
  }//GEN-LAST:event_moveRightActionPerformed

  private void optionsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_optionsButtonActionPerformed
  {//GEN-HEADEREND:event_optionsButtonActionPerformed
    ScanOptionsDialog dialog = new ScanOptionsDialog(this, cmdTwain);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }//GEN-LAST:event_optionsButtonActionPerformed

  private void scan(String mode, int dpi)
  {
    try
    {
      cmdTwain.setDpi(dpi);
      cmdTwain.setMode(mode);
      BufferedImage[] images = cmdTwain.scan();
      if (images != null)
      {
        int tab = tabbedPane.getTabCount();
        for (int i = 0; i < images.length; i++)
        {
          pageCounter++;
          String tabTitle = bundle.getString("page") + "-" + pageCounter;
          ImagePanel imagePanel = new ImagePanel(images[i]);
          tabbedPane.add(tabTitle, imagePanel);
          int index = tabbedPane.getTabCount() - 1;
          tabbedPane.setTabComponentAt(index,
            new TabPanel(tabTitle, new CloseAction(imagePanel)));
        }
        tabbedPane.setSelectedIndex(tab);
      }
    }
    catch (Exception ex)
    {
      JOptionPane.showMessageDialog(this, ex.toString(),
        "ERROR", JOptionPane.ERROR_MESSAGE);
    }
  }

  class CloseAction extends AbstractAction
  {
    Component component;

    CloseAction(Component component)
    {
      this.component = component;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
      String text = bundle.getString("removePage");
      int result = JOptionPane.showConfirmDialog(ScanFrame.this, text,
        text, JOptionPane.YES_NO_OPTION);
      if (result == JOptionPane.YES_OPTION)
      {
        tabbedPane.remove(component);
      }
    }
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String args[])
  {
    try
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (Exception ex)
    {
    }
    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        ScanFrame frame = new ScanFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.showFrame();
      }
    });
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton clearAllButton;
  private javax.swing.JButton moveLeft;
  private javax.swing.JButton moveRight;
  private javax.swing.JButton optionsButton;
  private javax.swing.JButton pdfButton;
  private javax.swing.JButton rotateLeftButton;
  private javax.swing.JButton rotateRightButton;
  private javax.swing.JButton scanBlackWhiteButton;
  private javax.swing.JButton scanColorButton;
  private javax.swing.JButton scanGrayButton;
  private javax.swing.JButton sourceButton;
  private javax.swing.JTabbedPane tabbedPane;
  private javax.swing.JToolBar toolBar;
  private javax.swing.JButton zoomInButton;
  private javax.swing.JButton zoomOutButton;
  // End of variables declaration//GEN-END:variables
}
