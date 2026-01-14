package at.tgm.client;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ServerDiscoveryLauncherFrame extends JFrame {
    
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    // Komponenten für "Suche läuft"
    private JLabel searchingLabel;
    
    // Komponenten für "Keine Server gefunden"
    private JTextField hostField;
    private JTextField portField;
    private JButton connectButton;
    private JButton retryButton;
    
    // Komponenten für "Server-Liste"
    private JList<ServerDiscoveryLauncher.ServerInfo> serverList;
    private DefaultListModel<ServerDiscoveryLauncher.ServerInfo> serverListModel;
    private JButton selectButton;
    private JButton refreshButton;
    
    public ServerDiscoveryLauncherFrame() {
        setTitle("Server-Verbindung");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        // Erstelle die verschiedenen Panels
        createSearchingPanel();
        createManualInputPanel();
        createServerListPanel();
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Zeige zuerst das "Suche läuft" Panel
        cardLayout.show(contentPanel, "searching");
    }
    
    private void createSearchingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        searchingLabel = new JLabel("Suche nach Servern...", JLabel.CENTER);
        searchingLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        
        panel.add(searchingLabel, BorderLayout.CENTER);
        
        contentPanel.add(panel, "searching");
    }
    
    private void createManualInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Fehlermeldung
        JLabel errorLabel = new JLabel(
            "<html><center>Kein Server gefunden.<br>Bitte geben Sie die Server-Adresse manuell ein.</center></html>",
            JLabel.CENTER
        );
        errorLabel.setForeground(Color.RED);
        panel.add(errorLabel, BorderLayout.NORTH);
        
        // Eingabefelder
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        inputPanel.add(new JLabel("Server-Adresse:"));
        hostField = new JTextField("localhost");
        inputPanel.add(hostField);
        
        inputPanel.add(new JLabel("Port:"));
        portField = new JTextField("5123");
        inputPanel.add(portField);
        
        panel.add(inputPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        connectButton = new JButton("Verbinden");
        connectButton.addActionListener(e -> {
            String host = hostField.getText().trim();
            String portStr = portField.getText().trim();
            
            if (host.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Bitte geben Sie eine Server-Adresse ein.",
                    "Fehler", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int port;
            try {
                port = Integer.parseInt(portStr);
                if (port < 1 || port > 65535) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ungültiger Port. Port muss zwischen 1 und 65535 liegen.",
                    "Fehler", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            dispose();
            ServerDiscoveryLauncher.connectToServer(host, port);
        });
        
        retryButton = new JButton("Erneut suchen");
        retryButton.addActionListener(e -> {
            cardLayout.show(contentPanel, "searching");
            // Starte neue Suche
            new Thread(() -> {
                List<ServerDiscoveryLauncher.ServerInfo> servers = 
                    ServerDiscoveryLauncher.discoverServers();
                
                SwingUtilities.invokeLater(() -> {
                    if (servers.isEmpty()) {
                        cardLayout.show(contentPanel, "manual");
                    } else if (servers.size() == 1) {
                        dispose();
                        ServerDiscoveryLauncher.connectToServer(
                            servers.get(0).getAddress(), 
                            servers.get(0).getPort()
                        );
                    } else {
                        serverListModel.clear();
                        for (ServerDiscoveryLauncher.ServerInfo server : servers) {
                            serverListModel.addElement(server);
                        }
                        cardLayout.show(contentPanel, "list");
                    }
                });
            }).start();
        });
        
        buttonPanel.add(connectButton);
        buttonPanel.add(retryButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        contentPanel.add(panel, "manual");
    }
    
    private void createServerListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Mehrere Server gefunden. Bitte wählen Sie einen aus:",
            JLabel.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        serverListModel = new DefaultListModel<>();
        serverList = new JList<>(serverListModel);
        serverList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        serverList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ServerDiscoveryLauncher.ServerInfo) {
                    ServerDiscoveryLauncher.ServerInfo server = (ServerDiscoveryLauncher.ServerInfo) value;
                    setText(server.getAddress() + ":" + server.getPort());
                }
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(serverList);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        selectButton = new JButton("Verbinden");
        selectButton.addActionListener(e -> {
            ServerDiscoveryLauncher.ServerInfo selected = serverList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Bitte wählen Sie einen Server aus.",
                    "Fehler", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            dispose();
            ServerDiscoveryLauncher.connectToServer(selected.getAddress(), selected.getPort());
        });
        
        refreshButton = new JButton("Aktualisieren");
        refreshButton.addActionListener(e -> {
            cardLayout.show(contentPanel, "searching");
            // Starte neue Suche
            new Thread(() -> {
                List<ServerDiscoveryLauncher.ServerInfo> servers = 
                    ServerDiscoveryLauncher.discoverServers();
                
                SwingUtilities.invokeLater(() -> {
                    if (servers.isEmpty()) {
                        cardLayout.show(contentPanel, "manual");
                    } else if (servers.size() == 1) {
                        dispose();
                        ServerDiscoveryLauncher.connectToServer(
                            servers.get(0).getAddress(), 
                            servers.get(0).getPort()
                        );
                    } else {
                        serverListModel.clear();
                        for (ServerDiscoveryLauncher.ServerInfo server : servers) {
                            serverListModel.addElement(server);
                        }
                        cardLayout.show(contentPanel, "list");
                    }
                });
            }).start();
        });
        
        buttonPanel.add(selectButton);
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Doppelklick auf Server-Liste
        serverList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    ServerDiscoveryLauncher.ServerInfo selected = serverList.getSelectedValue();
                    if (selected != null) {
                        dispose();
                        ServerDiscoveryLauncher.connectToServer(selected.getAddress(), selected.getPort());
                    }
                }
            }
        });
        
        contentPanel.add(panel, "list");
    }
    
    public void showManualInput() {
        cardLayout.show(contentPanel, "manual");
    }
    
    public void showServerList(List<ServerDiscoveryLauncher.ServerInfo> servers) {
        serverListModel.clear();
        for (ServerDiscoveryLauncher.ServerInfo server : servers) {
            serverListModel.addElement(server);
        }
        // Wähle ersten Server automatisch aus
        if (!servers.isEmpty()) {
            serverList.setSelectedIndex(0);
        }
        cardLayout.show(contentPanel, "list");
    }
}
