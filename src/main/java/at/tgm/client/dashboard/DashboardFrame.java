package at.tgm.client.dashboard;

import at.tgm.client.GuiController;
import at.tgm.client.profile.ProfilePanel;
import at.tgm.client.quiz.QuizPanel;
import at.tgm.objects.FachbegriffItem;
import at.tgm.objects.Nutzer;
import at.tgm.objects.NutzerStatus;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLConnection;

public class DashboardFrame extends JFrame {

    private Nutzer nutzer;
    private final GuiController controller;

    // Panel in der Mitte (für Quiz, Profil, Statistiken, etc.)
    private final JPanel contentPanel;

    private QuizPanel quizPanel;
    private ProfilePanel profilePanel;

    public DashboardFrame(Nutzer nutzer) {
        this(nutzer, null);
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
        add(buildSidebar(), BorderLayout.WEST);

        contentPanel = buildContentPanel();
        add(contentPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    public void setNutzer(Nutzer nutzer) {
        this.nutzer = nutzer;
        // Optional: Header neu aufbauen, falls nötig
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
    // SIDEBAR
    // =======================================================
    private JComponent buildSidebar() {
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

        side.add(createMenuButton("Quiz starten", () -> {
            // Netzwerk anfragen
            if (controller != null) {
                controller.onQuizMenuClicked();
            }
            // optional: Lade-Ansicht anzeigen
            showCard("QUIZ_LOADING");
        }));
        side.add(Box.createRigidArea(new Dimension(0, 8)));

        side.add(createMenuButton("Statistiken", () -> showCard("STATS")));
        side.add(Box.createRigidArea(new Dimension(0, 8)));

        side.add(createMenuButton("Einstellungen", () -> showCard("SETTINGS")));
        side.add(Box.createVerticalGlue());

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

        panel.add(buildPlaceholderPanel("Statistiken", """
                Hier könntest du Statistiken anzeigen:
                - Anzahl beantworteter Fragen
                - Trefferquote
                - Schwierigkeit, etc.
                """), "STATS");

        panel.add(buildPlaceholderPanel("Einstellungen", """
                Einstellungen für deinen Fachbegrifftrainer:
                - Sprache
                - Schwierigkeitsgrad
                - Theme, usw.
                """), "SETTINGS");

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
        if (profilePanel == null) {
            profilePanel = new ProfilePanel(nutzer);
            contentPanel.add(profilePanel, "PROFILE");
        }
        showCard("PROFILE");
        contentPanel.revalidate();
        contentPanel.repaint();
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
