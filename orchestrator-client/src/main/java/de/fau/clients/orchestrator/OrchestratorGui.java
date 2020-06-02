package de.fau.clients.orchestrator;

import de.fau.clients.orchestrator.file_loader.TaskQueueData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fau.clients.orchestrator.feature_explorer.TypeDefLut;
import de.fau.clients.orchestrator.file_loader.TaskEntry;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;
import lombok.extern.slf4j.Slf4j;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import sila_java.library.core.models.Feature;
import sila_java.library.core.models.Feature.Command;
import sila_java.library.core.models.Feature.Property;
import sila_java.library.manager.ServerAdditionException;
import sila_java.library.manager.ServerManager;
import sila_java.library.manager.models.Server;
import javax.swing.tree.TreeSelectionModel;
import sila_java.library.core.models.Feature.Metadata;
import sila_java.library.manager.ServerFinder;

@Slf4j
@SuppressWarnings("serial")
public class OrchestratorGui extends javax.swing.JFrame {

    private static ServerManager serverManager;
    private static int taskRowId = 0;
    private final TaskQueueTable taskQueueTable = new TaskQueueTable();
    private boolean wasSaved = false;
    private String outFilePath = "";

    private void addSpecificServer() {
        String addr = serverAddressTextField.getText();
        int port;
        try {
            port = Integer.parseUnsignedInt(serverPortFormattedTextField.getText());
        } catch (NumberFormatException ex) {
            // do not accept invalid input
            return;
        } catch (Exception ex) {
            log.warn(OrchestratorGui.class.getName(), ex);
            return;
        }

        try {
            serverManager.addServer(addr, port);
        } catch (ServerAdditionException ex) {
            log.warn(OrchestratorGui.class.getName(), ex);
            return;
        }

        for (final Server server : serverManager.getServers().values()) {
            if (server.getHost().equals(addr) && server.getPort() == port) {
                addFeaturesToTree(List.of(server));
                break;
            }
        }

        addServerDialog.setVisible(false);
        addServerDialog.dispose();
    }

    private void addFeaturesToTree(final Collection<Server> serverList) {
        DefaultTreeModel model = (DefaultTreeModel) featureTree.getModel();
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();
        rootNode.removeAllChildren();

        for (final Server server : serverList) {
            DefaultMutableTreeNode serverNode = new DefaultMutableTreeNode();
            serverNode.setUserObject(new FeatureTreeType(server));
            rootNode.add(serverNode);

            for (final Feature feature : server.getFeatures()) {
                DefaultMutableTreeNode featureNode = new DefaultMutableTreeNode();
                featureNode.setUserObject(new FeatureTreeType(feature));
                serverNode.add(featureNode);

                final TypeDefLut typeDefs = new TypeDefLut(feature);
                if (feature.getCommand() != null && !feature.getCommand().isEmpty()) {
                    DefaultMutableTreeNode commandNode = new DefaultMutableTreeNode("Commands");
                    featureNode.add(commandNode);
                    for (final Command command : feature.getCommand()) {
                        final CommandTreeNode ctn = new CommandTreeNode(
                                server.getConfiguration().getUuid(),
                                feature.getIdentifier(),
                                typeDefs,
                                command);
                        ctn.setUserObject(new FeatureTreeType(command));
                        commandNode.add(ctn);
                    }
                }

                if (feature.getProperty() != null && !feature.getProperty().isEmpty()) {
                    DefaultMutableTreeNode propertyNode = new DefaultMutableTreeNode("Properties");
                    featureNode.add(propertyNode);
                    for (final Property prop : feature.getProperty()) {
                        final PropertyTreeNode ptn = new PropertyTreeNode(
                                server.getConfiguration().getUuid(),
                                feature.getIdentifier(),
                                typeDefs,
                                prop);
                        ptn.setUserObject(new FeatureTreeType(prop));
                        propertyNode.add(ptn);
                    }
                }

                if (feature.getMetadata() != null && !feature.getMetadata().isEmpty()) {
                    DefaultMutableTreeNode metaNode = new DefaultMutableTreeNode("Metadata");
                    featureNode.add(metaNode);
                    for (final Metadata meta : feature.getMetadata()) {
                        DefaultMutableTreeNode mtn = new DefaultMutableTreeNode();
                        mtn.setUserObject(new FeatureTreeType(meta));
                        metaNode.add(mtn);
                    }
                }
            }
        }
        model.reload();
        // expand all nodes in the tree
        for (int i = 0; i < featureTree.getRowCount(); i++) {
            featureTree.expandRow(i);
        }
    }

    /**
     * Creates new form OrchestratorGui
     */
    public OrchestratorGui() {
        try {
            serverManager = ServerManager.getInstance();
        } catch (Exception ex) {
            log.error(ex.getMessage());
            System.exit(1);
        }
        initComponents();
        initTaskQueueTable();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        addServerDialog = new javax.swing.JDialog();
        serverAddressLabel = new javax.swing.JLabel();
        serverAddressTextField = new javax.swing.JTextField();
        serverPortLabel = new javax.swing.JLabel();
        serverDialogOkBtn = new javax.swing.JButton();
        serverDialogCancelBtn = new javax.swing.JButton();
        serverPortFormattedTextField = new javax.swing.JFormattedTextField();
        aboutDialog = new javax.swing.JDialog();
        aboutLabel = new javax.swing.JLabel();
        taskQueuePopupMenu = new javax.swing.JPopupMenu();
        removeTaskFromQueueMenuItem = new javax.swing.JMenuItem();
        execRowEntryMenuItem = new javax.swing.JMenuItem();
        fileSaveAsChooser = new javax.swing.JFileChooser();
        fileOpenChooser = new javax.swing.JFileChooser();
        serverSplitPane = new javax.swing.JSplitPane();
        serverPanel = new javax.swing.JPanel();
        featureScrollPane = new javax.swing.JScrollPane();
        featureTree = new javax.swing.JTree();
        addServerBtn = new javax.swing.JButton();
        scanServerBtn = new javax.swing.JButton();
        mainPanel = new javax.swing.JPanel();
        mainPanelSplitPane = new javax.swing.JSplitPane();
        taskQueuePanel = new javax.swing.JPanel();
        addTaskToQueueBtn = new javax.swing.JButton();
        executeAllBtn = new javax.swing.JButton();
        taskQueueScrollPane = new javax.swing.JScrollPane();
        moveTaskUpBtn = new javax.swing.JButton();
        moveTaskDownBtn = new javax.swing.JButton();
        removeTaskFromQueueBtn = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        contentsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        addServerDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addServerDialog.setTitle("Add Server");
        addServerDialog.setModal(true);
        addServerDialog.setName("addServerDialog"); // NOI18N
        addServerDialog.setPreferredSize(new java.awt.Dimension(300, 200));
        addServerDialog.setSize(new java.awt.Dimension(300, 200));
        addServerDialog.setLocationRelativeTo(null);
        java.awt.GridBagLayout addServerDialogLayout = new java.awt.GridBagLayout();
        addServerDialogLayout.columnWidths = new int[] {2};
        addServerDialogLayout.rowHeights = new int[] {5};
        addServerDialogLayout.columnWeights = new double[] {0.5, 0.5};
        addServerDialog.getContentPane().setLayout(addServerDialogLayout);

        serverAddressLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        serverAddressLabel.setText("Server Address:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        addServerDialog.getContentPane().add(serverAddressLabel, gridBagConstraints);

        serverAddressTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverAddressTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        addServerDialog.getContentPane().add(serverAddressTextField, gridBagConstraints);
        serverAddressTextField.getAccessibleContext().setAccessibleParent(this);

        serverPortLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        serverPortLabel.setText("Server Port:");
        serverPortLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 10);
        addServerDialog.getContentPane().add(serverPortLabel, gridBagConstraints);

        serverDialogOkBtn.setMnemonic('o');
        serverDialogOkBtn.setText("Ok");
        serverDialogOkBtn.setPreferredSize(new java.awt.Dimension(80, 30));
        serverDialogOkBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverDialogOkBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 5);
        addServerDialog.getContentPane().add(serverDialogOkBtn, gridBagConstraints);

        serverDialogCancelBtn.setMnemonic('c');
        serverDialogCancelBtn.setText("Cancel");
        serverDialogCancelBtn.setPreferredSize(new java.awt.Dimension(80, 30));
        serverDialogCancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverDialogCancelBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 10);
        addServerDialog.getContentPane().add(serverDialogCancelBtn, gridBagConstraints);

        try {
            serverPortFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("#####")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }
        serverPortFormattedTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverPortFormattedTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        addServerDialog.getContentPane().add(serverPortFormattedTextField, gridBagConstraints);

        addServerDialog.getAccessibleContext().setAccessibleParent(this);

        aboutDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        aboutDialog.setTitle("About");
        aboutDialog.setMinimumSize(new java.awt.Dimension(300, 256));
        aboutDialog.setModal(true);
        aboutDialog.setName("aboutDialog"); // NOI18N
        aboutDialog.setLocationRelativeTo(null);

        aboutLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        aboutLabel.setText("<html>\n<center>\n<h1>sila-orchestrator</h1>\n<p>Copyright © 2020 Florian Bauer</p>\n</center>\n<p></p>\n<p>E-Mail: florian.bauer.dev@gmail.com</p>\n<p>License: Apache-2.0</p>\n<html>");
        aboutLabel.setName(""); // NOI18N
        aboutDialog.getContentPane().add(aboutLabel, java.awt.BorderLayout.CENTER);

        aboutDialog.getAccessibleContext().setAccessibleParent(this);

        removeTaskFromQueueMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/list-remove-16px.png"))); // NOI18N
        removeTaskFromQueueMenuItem.setMnemonic('r');
        removeTaskFromQueueMenuItem.setText("Remove Entry");
        removeTaskFromQueueMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeTaskFromQueue(evt);
            }
        });
        taskQueuePopupMenu.add(removeTaskFromQueueMenuItem);

        execRowEntryMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/execute-16px.png"))); // NOI18N
        execRowEntryMenuItem.setMnemonic('x');
        execRowEntryMenuItem.setText("Execute Entry");
        execRowEntryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                execRowEntryMenuItemActionPerformed(evt);
            }
        });
        taskQueuePopupMenu.add(execRowEntryMenuItem);

        fileSaveAsChooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        fileSaveAsChooser.setDialogTitle("Save");
        fileSaveAsChooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);

        fileOpenChooser.setFileFilter(new SiloFileFilter());

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SiLA Orchestrator");
        setLocationByPlatform(true);
        setPreferredSize(new java.awt.Dimension(1200, 600));
        setSize(new java.awt.Dimension(0, 0));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        serverSplitPane.setContinuousLayout(true);

        serverPanel.setPreferredSize(new java.awt.Dimension(384, 220));
        java.awt.GridBagLayout jPanel1Layout = new java.awt.GridBagLayout();
        jPanel1Layout.columnWidths = new int[] {3};
        jPanel1Layout.rowHeights = new int[] {2};
        serverPanel.setLayout(jPanel1Layout);

        ToolTipManager.sharedInstance().registerComponent(featureTree);
        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("No Server Available");
        featureTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        featureTree.setCellRenderer(new FeatureTreeRenderer());
        featureTree.setVisibleRowCount(10);
        featureTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        featureTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                featureTreeValueChanged(evt);
            }
        });
        featureScrollPane.setViewportView(featureTree);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        serverPanel.add(featureScrollPane, gridBagConstraints);

        addServerBtn.setMnemonic('a');
        addServerBtn.setText("Add");
        addServerBtn.setPreferredSize(new java.awt.Dimension(80, 30));
        addServerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addServerBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        serverPanel.add(addServerBtn, gridBagConstraints);

        scanServerBtn.setMnemonic('s');
        scanServerBtn.setText("Scan");
        scanServerBtn.setPreferredSize(new java.awt.Dimension(80, 30));
        scanServerBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scanServerBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        serverPanel.add(scanServerBtn, gridBagConstraints);

        serverSplitPane.setLeftComponent(serverPanel);

        mainPanel.setPreferredSize(new java.awt.Dimension(512, 409));
        mainPanel.setLayout(new javax.swing.BoxLayout(mainPanel, javax.swing.BoxLayout.PAGE_AXIS));

        mainPanelSplitPane.setDividerLocation(300);
        mainPanelSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        mainPanelSplitPane.setContinuousLayout(true);

        java.awt.GridBagLayout taskQueuePanelLayout = new java.awt.GridBagLayout();
        taskQueuePanelLayout.columnWidths = new int[] {3};
        taskQueuePanelLayout.rowHeights = new int[] {3};
        taskQueuePanel.setLayout(taskQueuePanelLayout);

        addTaskToQueueBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/add-entry.png"))); // NOI18N
        addTaskToQueueBtn.setToolTipText("Add command to task-queue");
        addTaskToQueueBtn.setEnabled(false);
        addTaskToQueueBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTaskToQueueBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        taskQueuePanel.add(addTaskToQueueBtn, gridBagConstraints);

        executeAllBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/execute-all.png"))); // NOI18N
        executeAllBtn.setText("Execute All");
        executeAllBtn.setToolTipText("Execute all tasks in queue");
        executeAllBtn.setEnabled(false);
        executeAllBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                executeAllBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        taskQueuePanel.add(executeAllBtn, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        taskQueuePanel.add(taskQueueScrollPane, gridBagConstraints);

        moveTaskUpBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/move-up.png"))); // NOI18N
        moveTaskUpBtn.setToolTipText("Move selcted task one place up in the queue order");
        moveTaskUpBtn.setEnabled(false);
        moveTaskUpBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveTaskUpBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 0.5;
        taskQueuePanel.add(moveTaskUpBtn, gridBagConstraints);

        moveTaskDownBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/move-down.png"))); // NOI18N
        moveTaskDownBtn.setToolTipText("Move selected task one place down in the queue order");
        moveTaskDownBtn.setEnabled(false);
        moveTaskDownBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveTaskDownBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 0.5;
        taskQueuePanel.add(moveTaskDownBtn, gridBagConstraints);

        removeTaskFromQueueBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/list-remove.png"))); // NOI18N
        removeTaskFromQueueBtn.setToolTipText("Remove selected task from queue");
        removeTaskFromQueueBtn.setEnabled(false);
        removeTaskFromQueueBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeTaskFromQueue(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        taskQueuePanel.add(removeTaskFromQueueBtn, gridBagConstraints);

        mainPanelSplitPane.setLeftComponent(taskQueuePanel);
        mainPanelSplitPane.setRightComponent(commandScrollPane);

        mainPanel.add(mainPanelSplitPane);

        serverSplitPane.setRightComponent(mainPanel);

        getContentPane().add(serverSplitPane, java.awt.BorderLayout.CENTER);

        fileMenu.setMnemonic('f');
        fileMenu.setText("File");

        openMenuItem.setMnemonic('o');
        openMenuItem.setText("Open");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);

        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.setMnemonic('s');
        saveMenuItem.setText("Save");
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setMnemonic('a');
        saveAsMenuItem.setText("Save As ...");
        saveAsMenuItem.setDisplayedMnemonicIndex(5);
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsActionPerformed(evt);
            }
        });
        fileMenu.add(saveAsMenuItem);

        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setMnemonic('e');
        editMenu.setText("Edit");

        cutMenuItem.setMnemonic('t');
        cutMenuItem.setText("Cut");
        editMenu.add(cutMenuItem);

        copyMenuItem.setMnemonic('y');
        copyMenuItem.setText("Copy");
        editMenu.add(copyMenuItem);

        pasteMenuItem.setMnemonic('p');
        pasteMenuItem.setText("Paste");
        editMenu.add(pasteMenuItem);

        deleteMenuItem.setMnemonic('d');
        deleteMenuItem.setText("Delete");
        editMenu.add(deleteMenuItem);

        menuBar.add(editMenu);

        helpMenu.setMnemonic('h');
        helpMenu.setText("Help");

        contentsMenuItem.setMnemonic('c');
        contentsMenuItem.setText("Contents");
        helpMenu.add(contentsMenuItem);

        aboutMenuItem.setMnemonic('a');
        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void initTaskQueueTable() {
        taskQueueTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                taskQueueTableMouseClicked(evt);
            }
        });
        taskQueueScrollPane.setViewportView(taskQueueTable);
    }

    private void taskQueueTableMouseClicked(final MouseEvent evt) {
        int selectedRowIdx = taskQueueTable.getSelectedRow();
        if (selectedRowIdx < 0) {
            return;
        }

        // show popup-menu on right-click
        if (evt.getButton() == MouseEvent.BUTTON3) {
            taskQueuePopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
            return;
        }

        final TaskQueueTableModel model = taskQueueTable.getModel();
        CommandTableEntry entry = (CommandTableEntry) model.getValueAt(selectedRowIdx, TaskQueueTable.COLUMN_COMMAND_IDX);
        if (entry == null) {
            return;
        }

        int rowCount = model.getRowCount();
        if (rowCount > 1) {
            moveTaskUpBtn.setEnabled(selectedRowIdx > 0);
            moveTaskDownBtn.setEnabled(selectedRowIdx < rowCount - 1);
        } else {
            moveTaskUpBtn.setEnabled(false);
            moveTaskDownBtn.setEnabled(false);
        }

        final boolean isTaskRemoveEnabled = (rowCount > 0);
        removeTaskFromQueueBtn.setEnabled(isTaskRemoveEnabled);
        removeTaskFromQueueMenuItem.setEnabled(isTaskRemoveEnabled);
        commandScrollPane.setViewportView(entry.getPanel());
    }

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        serverManager.close();
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void addServerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addServerBtnActionPerformed
        addServerDialog.pack();
        addServerDialog.setVisible(true);
    }//GEN-LAST:event_addServerBtnActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        aboutDialog.pack();
        aboutDialog.setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void serverDialogCancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverDialogCancelBtnActionPerformed
        addServerDialog.setVisible(false);
        addServerDialog.dispose();
    }//GEN-LAST:event_serverDialogCancelBtnActionPerformed

    private void serverDialogOkBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverDialogOkBtnActionPerformed
        addSpecificServer();
    }//GEN-LAST:event_serverDialogOkBtnActionPerformed

    private void scanServerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scanServerBtnActionPerformed
        serverManager.getDiscovery().scanNetwork();
        final List<Server> serverList = ServerFinder.filterBy(ServerFinder.Filter.status(Server.Status.ONLINE)).find();
        if (!serverList.isEmpty()) {
            // hide the "No Server Available" string.
            featureTree.setRootVisible(false);
            addFeaturesToTree(serverList);
        } else {
            // show the "No Server Available" string.
            featureTree.setRootVisible(true);
        }
    }//GEN-LAST:event_scanServerBtnActionPerformed

    private void serverPortFormattedTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverPortFormattedTextFieldActionPerformed
        addSpecificServer();
    }//GEN-LAST:event_serverPortFormattedTextFieldActionPerformed

    private void serverAddressTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverAddressTextFieldActionPerformed
        // set cursor to the next text field when enter was pressed
        serverPortFormattedTextField.requestFocusInWindow();
    }//GEN-LAST:event_serverAddressTextFieldActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        serverManager.close();
    }//GEN-LAST:event_formWindowClosing

    private void featureTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_featureTreeValueChanged
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) featureTree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }

        if (node.isLeaf()) {
            if (node instanceof CommandTreeNode) {
                addTaskToQueueBtn.setEnabled(true);
                commandScrollPane.setViewportView(null);
            } else if (node instanceof PropertyTreeNode) {
                PropertyTreeNode propNode = (PropertyTreeNode) node;
                if (!propNode.isPanelBuild()) {
                    propNode.requestPropertyData();
                }
                commandScrollPane.setViewportView(propNode.getPanel());
            }
        } else {
            addTaskToQueueBtn.setEnabled(false);
            commandScrollPane.setViewportView(null);
        }
    }//GEN-LAST:event_featureTreeValueChanged

    private void addTaskToQueueBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTaskToQueueBtnActionPerformed
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) featureTree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }

        if (node.isLeaf()) {
            if (node instanceof CommandTreeNode) {
                CommandTreeNode cmdNode = (CommandTreeNode) node;
                // use the selected node to create a new table entry.
                CommandTableEntry cmdEntry = cmdNode.createTableEntry();
                final TaskQueueTableModel model = taskQueueTable.getModel();
                model.addCommandTableEntry(++taskRowId, cmdEntry);
                executeAllBtn.setEnabled(true);
            }
        }
    }//GEN-LAST:event_addTaskToQueueBtnActionPerformed

    private void executeAllBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_executeAllBtnActionPerformed
        executeAllBtn.setEnabled(false);

        Runnable queueRunner = () -> {
            Thread entryThread;
            final TaskQueueTableModel model = taskQueueTable.getModel();
            for (int i = 0; i < taskQueueTable.getRowCount(); i++) {
                entryThread = new Thread((CommandTableEntry) model.getValueAt(i, TaskQueueTable.COLUMN_COMMAND_IDX));
                entryThread.start();
                try {
                    entryThread.join();
                } catch (InterruptedException ex) {
                    log.error(ex.getMessage());
                }
            }
            executeAllBtn.setEnabled(true);
        };
        new Thread(queueRunner).start();
    }//GEN-LAST:event_executeAllBtnActionPerformed

    private void execRowEntryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_execRowEntryMenuItemActionPerformed
        int selectedRowIdx = taskQueueTable.getSelectedRow();
        if (selectedRowIdx < 0) {
            return;
        }
        final TaskQueueTableModel model = taskQueueTable.getModel();
        CommandTableEntry entry = (CommandTableEntry) model.getValueAt(selectedRowIdx, TaskQueueTable.COLUMN_COMMAND_IDX);
        new Thread(entry).start();
    }//GEN-LAST:event_execRowEntryMenuItemActionPerformed

    private void moveTaskUpBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveTaskUpBtnActionPerformed
        int selectedRowIdx = taskQueueTable.getSelectedRow();
        if (selectedRowIdx < 0) {
            return;
        } else if (selectedRowIdx <= 1) {
            moveTaskUpBtn.setEnabled(false);
        }
        final TaskQueueTableModel model = taskQueueTable.getModel();
        model.moveRow(selectedRowIdx, selectedRowIdx, selectedRowIdx - 1);
        taskQueueTable.changeSelection(selectedRowIdx - 1, TaskQueueTable.COLUMN_TASK_ID_IDX, false, false);
        moveTaskDownBtn.setEnabled(true);
    }//GEN-LAST:event_moveTaskUpBtnActionPerformed

    private void moveTaskDownBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveTaskDownBtnActionPerformed
        int selectedRowIdx = taskQueueTable.getSelectedRow();
        int rowCount = taskQueueTable.getRowCount();
        if (selectedRowIdx < 0 || selectedRowIdx >= rowCount - 1) {
            return;
        } else if (selectedRowIdx >= rowCount - 2) {
            moveTaskDownBtn.setEnabled(false);
        }
        final TaskQueueTableModel model = taskQueueTable.getModel();
        model.moveRow(selectedRowIdx, selectedRowIdx, selectedRowIdx + 1);
        taskQueueTable.changeSelection(selectedRowIdx + 1, TaskQueueTable.COLUMN_TASK_ID_IDX, false, false);
        moveTaskUpBtn.setEnabled(true);
    }//GEN-LAST:event_moveTaskDownBtnActionPerformed

    private void removeTaskFromQueue(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeTaskFromQueue
        int selectedRowIdx = taskQueueTable.getSelectedRow();
        if (selectedRowIdx < 0) {
            return;
        }
        moveTaskUpBtn.setEnabled(false);
        moveTaskDownBtn.setEnabled(false);
        removeTaskFromQueueBtn.setEnabled(false);
        removeTaskFromQueueMenuItem.setEnabled(false);
        final TaskQueueTableModel model = taskQueueTable.getModel();
        model.removeRow(selectedRowIdx);
        commandScrollPane.setViewportView(null);
    }//GEN-LAST:event_removeTaskFromQueue

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        if (!wasSaved || outFilePath.isEmpty()) {
            saveAsActionPerformed(evt);
        } else {
            // TODO: give the user some kind of notificatin that the file was saved
            String outData = getSaveData();
            if (outData.isEmpty()) {
                log.warn("Empty save!");
                return;
            }

            try {
                Files.writeString(Paths.get(outFilePath), outData, StandardCharsets.UTF_8);
                log.info("Saved " + outFilePath);
            } catch (IOException ex) {
                log.error(ex.getMessage());
            }
        }
    }//GEN-LAST:event_saveMenuItemActionPerformed

    private void saveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsActionPerformed
        String outData = getSaveData();
        if (outData.isEmpty()) {
            log.warn("Empty save!");
            return;
        }

        fileSaveAsChooser.setSelectedFile(new File(LocalDate.now().toString() + ".silo"));
        int retVal = fileSaveAsChooser.showSaveDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File outFile = fileSaveAsChooser.getSelectedFile();
            outFilePath = outFile.getAbsolutePath();
            File tmpFile = new File(outFilePath);
            int userDesition = JOptionPane.OK_OPTION;
            if (tmpFile.exists() && tmpFile.isFile()) {
                userDesition = JOptionPane.showConfirmDialog(this,
                        "File \"" + tmpFile.getName() + "\" already exists in \""
                        + tmpFile.getParent() + "\"!\n"
                        + "Do you want to overwrite the existing file?",
                        null,
                        JOptionPane.INFORMATION_MESSAGE,
                        JOptionPane.YES_NO_CANCEL_OPTION);
            }

            if (userDesition == JOptionPane.OK_OPTION) {
                try {
                    Files.writeString(Paths.get(outFilePath), outData, StandardCharsets.UTF_8);
                    wasSaved = true;
                    log.info("Saved as file " + outFilePath);
                } catch (IOException ex) {
                    log.error(ex.getMessage());
                }
            }
        }
    }//GEN-LAST:event_saveAsActionPerformed

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        int retVal = fileOpenChooser.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            final File file = fileOpenChooser.getSelectedFile();
            log.info("Opend file: " + file.getAbsolutePath());
            ObjectMapper mapper = new ObjectMapper();
            TaskQueueData tqd = null;
            try {
                tqd = mapper.readValue(file, TaskQueueData.class);
            } catch (IOException ex) {
                log.error(ex.getMessage());
            }

            if (tqd != null) {
                log.info("File Version: " + tqd.getSiloFileVersion());
                final TaskQueueTableModel model = taskQueueTable.getModel();
                for (TaskEntry entry : tqd.getTasks()) {
                    log.info("Import task: " + entry);
                    model.importTaskEntry(entry);
                }
                // FIXME: only enable the ExecuteAll-Button when the task import was successfull.
                executeAllBtn.setEnabled(true);
            }
        } else {
            log.warn("File access cancelled by user.");
        }
    }//GEN-LAST:event_openMenuItemActionPerformed

    private String getSaveData() {
        String outData = "";
        ObjectMapper mapper = new ObjectMapper();
        TaskQueueData tqd = TaskQueueData.createFromTaskQueue(taskQueueTable);
        try {
            outData = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tqd);
        } catch (JsonProcessingException ex) {
            log.error(ex.getMessage());
        }
        return outData;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        // Set the GTK+ look and feel
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("GTK+".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException
                | InstantiationException
                | IllegalAccessException
                | javax.swing.UnsupportedLookAndFeelException ex) {
            log.error(OrchestratorGui.class.getName(), ex);
        }
        //</editor-fold>

        // Create and display the form
        java.awt.EventQueue.invokeLater(() -> {
            new OrchestratorGui().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog aboutDialog;
    private javax.swing.JLabel aboutLabel;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JButton addServerBtn;
    private javax.swing.JDialog addServerDialog;
    private javax.swing.JButton addTaskToQueueBtn;
    private final javax.swing.JScrollPane commandScrollPane = new javax.swing.JScrollPane();
    private javax.swing.JMenuItem contentsMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem execRowEntryMenuItem;
    private javax.swing.JButton executeAllBtn;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JScrollPane featureScrollPane;
    private javax.swing.JTree featureTree;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JFileChooser fileOpenChooser;
    private javax.swing.JFileChooser fileSaveAsChooser;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JSplitPane mainPanelSplitPane;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JButton moveTaskDownBtn;
    private javax.swing.JButton moveTaskUpBtn;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JButton removeTaskFromQueueBtn;
    private javax.swing.JMenuItem removeTaskFromQueueMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JButton scanServerBtn;
    private javax.swing.JLabel serverAddressLabel;
    private javax.swing.JTextField serverAddressTextField;
    private javax.swing.JButton serverDialogCancelBtn;
    private javax.swing.JButton serverDialogOkBtn;
    private javax.swing.JPanel serverPanel;
    private javax.swing.JFormattedTextField serverPortFormattedTextField;
    private javax.swing.JLabel serverPortLabel;
    private javax.swing.JSplitPane serverSplitPane;
    private javax.swing.JPanel taskQueuePanel;
    private javax.swing.JPopupMenu taskQueuePopupMenu;
    private javax.swing.JScrollPane taskQueueScrollPane;
    // End of variables declaration//GEN-END:variables
}
