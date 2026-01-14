package at.tgm.client.dashboard;

import at.tgm.client.ClientNetworkController;
import at.tgm.client.GuiController;
import at.tgm.client.profile.ProfilePanel;
import at.tgm.client.quiz.QuizPanel;
import at.tgm.network.packets.C2SGETStats;
import at.tgm.network.packets.S2CPOSTStats;
import at.tgm.objects.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class DashboardFrame extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(DashboardFrame.class);
    
    private Nutzer nutzer;
    private final GuiController controller;

    // Panel in der Mitte (für Quiz, Profil, Statistiken, etc.)
    private final JPanel contentPanel;

    private QuizPanel quizPanel;
    private ProfilePanel profilePanel;
    private StatsPanel statsPanel;
    private SchuelerListPanel schuelerListPanel;
    private SchuelerDashboardPanel schuelerDashboardPanel;
    private SchuelerAnlegenPanel schuelerAnlegenPanel;
    private Schueler currentViewedSchueler; // Für Navigation zurück

    // Neue Panels für Lehrer
    private FachbegriffeVerwaltungPanel fachbegriffeVerwaltungPanel;
    private QuizTemplatesVerwaltungPanel quizTemplatesVerwaltungPanel;

    // =========================
    // Öffentliche API-Methoden
    // =========================

    public void showStats(Quiz[] quizzes) {
        if (statsPanel != null) {
            contentPanel.remove(statsPanel);
        }

        statsPanel = new StatsPanel(quizzes, this);
        contentPanel.add(statsPanel, "STATS_VIEW");

        showCard("STATS_VIEW");
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    /**
     * Lädt Statistiken synchron vom Server.
     */
    private void loadStats() {
        // In separatem Thread ausführen, um UI nicht zu blockieren
        new Thread(() -> {
            try {
                C2SGETStats request = new C2SGETStats();
                S2CPOSTStats response = ClientNetworkController.socketClient
                    .getChannel()
                    .sendAndWait(
                        request,
                        S2CPOSTStats.class,
                        5,
                        TimeUnit.SECONDS
                    );
                
                // UI-Update im EDT (Event Dispatch Thread)
                SwingUtilities.invokeLater(() -> {
                    Quiz[] quizzes = response.getQuizzes();
                    showStats(quizzes);
                });
                
            } catch (TimeoutException e) {
                logger.error("Timeout beim Laden der Statistiken", e);
                SwingUtilities.invokeLater(() -> {
                    showCard("STATS_VIEW");
                    JOptionPane.showMessageDialog(this, 
                        "Statistiken konnten nicht geladen werden (Timeout).", 
                        "Fehler", 
                        JOptionPane.ERROR_MESSAGE);
                });
            } catch (IOException e) {
                logger.error("Fehler beim Laden der Statistiken", e);
                SwingUtilities.invokeLater(() -> {
                    showCard("STATS_VIEW");
                    JOptionPane.showMessageDialog(this, 
                        "Fehler beim Laden der Statistiken: " + e.getMessage(), 
                        "Fehler", 
                        JOptionPane.ERROR_MESSAGE);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Unterbrochen beim Laden der Statistiken", e);
            }
        }).start();
    }

    // Wird von außen (Netzwerk/GuiController) aufgerufen, wenn Schueler[] da ist
    public void showSchuelerList(Schueler[] schueler) {
        if (schuelerListPanel != null) {
            contentPanel.remove(schuelerListPanel);
        }

        schuelerListPanel = new SchuelerListPanel(schueler, this);
        contentPanel.add(schuelerListPanel, "SCHUELER_LIST");

        showCard("SCHUELER_LIST");
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // Wird von SchuelerListPanel aufgerufen, wenn man auf einen Schüler klickt
    public void showSchuelerDashboard(Schueler schueler) {
        this.currentViewedSchueler = schueler;
        if (schuelerDashboardPanel == null) {
            schuelerDashboardPanel = new SchuelerDashboardPanel(this);
            contentPanel.add(schuelerDashboardPanel, "SCHUELER_DASHBOARD");
        }

        schuelerDashboardPanel.setSchueler(schueler);
        showCard("SCHUELER_DASHBOARD");
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    public Schueler getCurrentViewedSchueler() {
        return currentViewedSchueler;
    }
    
    public GuiController getController() {
        return controller;
    }

    // ======================================================
    // Fachbegriffe-Verwaltung
    // ======================================================

    public void showFachbegriffeVerwaltung() {
        if (fachbegriffeVerwaltungPanel == null) {
            fachbegriffeVerwaltungPanel = new FachbegriffeVerwaltungPanel(this);
            contentPanel.add(fachbegriffeVerwaltungPanel, "FACHBEGRIFFE_VERWALTUNG");
        }

        showCard("FACHBEGRIFFE_VERWALTUNG");
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // ======================================================
    // Quiz-Templates-Verwaltung
    // ======================================================

    public void showQuizTemplatesVerwaltung() {
        if (quizTemplatesVerwaltungPanel == null) {
            quizTemplatesVerwaltungPanel = new QuizTemplatesVerwaltungPanel(this);
            contentPanel.add(quizTemplatesVerwaltungPanel, "QUIZ_TEMPLATES_VERWALTUNG");
        }

        showCard("QUIZ_TEMPLATES_VERWALTUNG");
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    /**
     * Lädt die Quizes eines bestimmten Schülers (für Lehrer).
     */
    public void loadSchuelerQuizes(String schuelerUsername) {
        showCard("STATS_LOADING");
        if (controller != null) {
            controller.loadSchuelerQuizes(schuelerUsername);
        }
    }

    // Zeigt das Panel zum Anlegen eines neuen Schülers
    public void showSchuelerAnlegen() {
        if (schuelerAnlegenPanel == null) {
            schuelerAnlegenPanel = new SchuelerAnlegenPanel(this, controller);
            contentPanel.add(schuelerAnlegenPanel, "SCHUELER_ANLEGEN");
        }

        showCard("SCHUELER_ANLEGEN");
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // =========================

    public DashboardFrame(Nutzer nutzer) {
        this(nutzer, null);
    }

    public void showQuizResults(FachbegriffItem[] correctedItems, int points, int maxPoints) {
        // Erstelle QuizPanel falls es noch nicht existiert (z.B. nach neuem Login)
        if (quizPanel == null) {
            // Erstelle ein leeres QuizPanel - wir brauchen nur die showResults-Funktionalität
            quizPanel = new QuizPanel(new FachbegriffItem[0]);
            contentPanel.add(quizPanel, "QUIZ");
        }
        
        quizPanel.showResults(correctedItems, points, maxPoints);
        showCard("QUIZ"); // sicherstellen, dass die Quiz-Card sichtbar ist
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public DashboardFrame(Nutzer nutzer, GuiController controller) {
        this.nutzer = nutzer;
        this.controller = controller;

        setTitle("Fachbegrifftrainer – Dashboard");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildHeaderPanel(), BorderLayout.NORTH);

        contentPanel = buildContentPanel();
        add(contentPanel, BorderLayout.CENTER);

        add(buildSidebar(), BorderLayout.WEST);

        setVisible(true);
    }

    public void setNutzer(Nutzer nutzer) {
        this.nutzer = nutzer;
        // Optional: Header neu aufbauen, falls nötig
    }

    public QuizPanel getQuizPanel() {
        return quizPanel;
    }

    // =======================================================
    // HEADER
    // =======================================================
    private JComponent buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        header.setBackground(new Color(240, 240, 240));

        JLabel title = new JLabel("Fachbegrifftrainer – Dashboard");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        header.add(title, BorderLayout.WEST);

        JPanel profileMini = new JPanel();
        profileMini.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        profileMini.setOpaque(false);
        profileMini.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel avatarLabel = new JLabel();
        ImageIcon avatarIcon = loadAvatarIcon(nutzer.getProfilePictureUrl(), 32, 32);
        avatarLabel.setIcon(avatarIcon);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        textPanel.setOpaque(false);

        String displayName =
                nutzer.getDisplayName() != null && !nutzer.getDisplayName().isEmpty()
                        ? nutzer.getDisplayName()
                        : nutzer.getUsername();

        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel statusLabel = new JLabel();
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        NutzerStatus status = nutzer.getStatus();
        String statusText = (status != null) ? status.name() : "OFFLINE";
        Color statusColor = colorForStatus(status);

        statusLabel.setText("● " + statusText);
        statusLabel.setForeground(statusColor);

        textPanel.add(nameLabel);
        textPanel.add(statusLabel);

        profileMini.add(textPanel);
        profileMini.add(avatarLabel);

        profileMini.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (controller != null) {
                    controller.showProfile();
                } else {
                    showProfile();
                }
            }
        });

        header.add(profileMini, BorderLayout.EAST);
        return header;
    }

    // =======================================================
    // SIDEBAR – role-based
    // =======================================================
    private JComponent buildSidebar() {
        if (nutzer instanceof Schueler) {
            return buildSchuelerSidebar();
        } else if (nutzer instanceof Lehrer) {
            return buildLehrerSidebar();
        } else {
            return buildDefaultSidebar();
        }
    }

    private JPanel createSidebarBase() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(200, 0));
        side.setBorder(BorderFactory.createEmptyBorder(16, 8, 16, 8));
        side.setBackground(new Color(250, 250, 250));

        JLabel menuLabel = new JLabel("Menü");
        menuLabel.setFont(new Font("Arial", Font.BOLD, 16));
        menuLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        side.add(menuLabel);
        side.add(Box.createRigidArea(new Dimension(0, 16)));
        return side;
    }

    // Sidebar für Schüler: nur Quiz + Statistiken + Beenden
    private JComponent buildSchuelerSidebar() {
        JPanel side = createSidebarBase();

        side.add(createMenuButton("Quiz starten", () -> {
            if (controller != null) {
                controller.onQuizMenuClicked();
            }
            showCard("QUIZ_LOADING");
        }));
        side.add(Box.createRigidArea(new Dimension(0, 8)));

        side.add(createMenuButton("Statistiken", () -> {
            showCard("STATS_LOADING");
            loadStats();
        }));
        side.add(Box.createVerticalGlue());

        side.add(createMenuButton("Beenden", this::exitApp));
        return side;
    }

    // Sidebar für Lehrer: Schülerliste + Lernkarten + Quiz-Templates + Beenden
    private JComponent buildLehrerSidebar() {
        JPanel side = createSidebarBase();

        side.add(createMenuButton("Schüler", () -> {
            showCard("SCHUELER_LOADING");
            if (controller != null) {
                // Controller schickt Request an Server, Antwort: Schueler[]
                controller.onSchuelerMenuClicked();
            }
        }));
        side.add(Box.createRigidArea(new Dimension(0, 8)));

        side.add(createMenuButton("Schüler anlegen", () -> {
            showSchuelerAnlegen();
        }));
        side.add(Box.createRigidArea(new Dimension(0, 8)));

        side.add(createMenuButton("Lernkarten", () -> {
            showFachbegriffeVerwaltung();
        }));
        side.add(Box.createRigidArea(new Dimension(0, 8)));

        side.add(createMenuButton("Quiz-Templates", () -> {
            showQuizTemplatesVerwaltung();
        }));
        side.add(Box.createVerticalGlue());

        side.add(createMenuButton("Beenden", this::exitApp));
        return side;
    }

    // Fallback, falls mal ein anderer Nutzertyp kommt
    private JComponent buildDefaultSidebar() {
        JPanel side = createSidebarBase();
        side.add(createMenuButton("Beenden", this::exitApp));
        return side;
    }

    private JButton createMenuButton(String text, Runnable onClick) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setFocusPainted(false);
        btn.addActionListener(e -> onClick.run());
        return btn;
    }

    // =======================================================
    // CONTENT – CardLayout
    // =======================================================
    private JPanel buildContentPanel() {
        JPanel panel = new JPanel(new CardLayout());

        panel.add(buildPlaceholderPanel("Willkommen, " + nutzer.getUsername() + "!", """
                Wähle links im Menü eine Aktion aus, um zu starten.
                """), "HOME");

        panel.add(buildPlaceholderPanel("Quiz wird geladen...", """
                Die Fachbegriffe werden gerade vom Server geladen.
                """), "QUIZ_LOADING");

        panel.add(buildPlaceholderPanel("Statistiken werden geladen...", """
                Bitte warten...
                """), "STATS_LOADING");

        panel.add(buildPlaceholderPanel("Schülerliste wird geladen...", """
                Bitte warten...
                """), "SCHUELER_LOADING");

        ((CardLayout) panel.getLayout()).show(panel, "HOME");
        return panel;
    }

    private JPanel buildPlaceholderPanel(String title, String body) {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JTextArea text = new JTextArea(body);
        text.setEditable(false);
        text.setFont(new Font("Arial", Font.PLAIN, 14));
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setOpaque(false);

        p.add(titleLabel, BorderLayout.NORTH);
        p.add(text, BorderLayout.CENTER);

        return p;
    }

    private void showCard(String name) {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, name);
    }
    
    public void showCardPublic(String name) {
        showCard(name);
    }
    
    public Nutzer getCurrentNutzer() {
        return nutzer;
    }

    // Vom GuiController aufgerufen
    public void showQuiz(FachbegriffItem[] items) {
        if (quizPanel != null) {
            contentPanel.remove(quizPanel);
        }
        quizPanel = new QuizPanel(items);
        contentPanel.add(quizPanel, "QUIZ");
        showCard("QUIZ");
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // Vom GuiController aufgerufen
    public void showProfile() {
        // Entferne altes Panel, damit es mit neuen Daten neu erstellt wird
        if (profilePanel != null) {
            contentPanel.remove(profilePanel);
            profilePanel = null;
        }
        
        profilePanel = new ProfilePanel(nutzer);
        contentPanel.add(profilePanel, "PROFILE");
        
        showCard("PROFILE");
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void refreshProfile() {
        if (profilePanel != null) {
            // Entferne altes Panel
            contentPanel.remove(profilePanel);
            profilePanel = null;
            // Erstelle neues Panel mit aktualisierten Daten
            showProfile();
        }
    }

    public boolean isProfileVisible() {
        // Prüfe ob profilePanel existiert und im contentPanel ist
        if (profilePanel == null) {
            return false;
        }
        for (java.awt.Component comp : contentPanel.getComponents()) {
            if (comp == profilePanel) {
                return true;
            }
        }
        return false;
    }

    private void exitApp() {
        System.exit(0);
    }

    // =======================================================
    // Avatar + Statusfarbe
    // =======================================================
    private ImageIcon loadAvatarIcon(String url, int w, int h) {
        try {
            if (url != null && !url.isEmpty()) {
                URL u = new URL(url);
                URLConnection conn = u.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.connect();

                Image img = ImageIO.read(conn.getInputStream());
                if (img != null) {
                    Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
            }
        } catch (Exception ignored) {
        }
        Image img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.LIGHT_GRAY);
        g.fillOval(0, 0, w, h);
        g.dispose();
        return new ImageIcon(img);
    }

    private Color colorForStatus(NutzerStatus s) {
        if (s == null) return Color.GRAY;
        switch (s) {
            case ONLINE: return new Color(0, 180, 0);
            case AWAY: return new Color(255, 170, 0);
            case BUSY: return new Color(200, 0, 0);
            case BANNED: return Color.BLACK;
            case OFFLINE:
            default: return Color.GRAY;
        }
    }
}
