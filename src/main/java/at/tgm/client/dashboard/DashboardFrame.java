package at.tgm.client.dashboard;

import at.tgm.client.ClientNetworkController;
import at.tgm.client.profile.ProfileFrame;
import at.tgm.network.packets.C2SPlayQuiz;
import at.tgm.objects.Nutzer;
import at.tgm.objects.NutzerStatus;

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

    private final Nutzer nutzer;

    // Panel in der Mitte (für Quiz, Statistiken, etc.)
    private final JPanel contentPanel;

    public DashboardFrame(Nutzer nutzer) {
        this.nutzer = nutzer;

        setTitle("Fachbegrifftrainer – Dashboard");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Dashboard zu -> ganze App zu
        setLayout(new BorderLayout());

        // Aufbau
        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);

        contentPanel = buildContentPanel();
        add(contentPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    // =======================================================
    // HEADER: App-Titel + Mini-Profil rechts
    // =======================================================
    private JComponent buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        header.setBackground(new Color(240, 240, 240));

        // Titel links
        JLabel title = new JLabel("Fachbegrifftrainer – Dashboard");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        header.add(title, BorderLayout.WEST);

        // Mini-Profil rechts (klickbar)
        JPanel profileMini = new JPanel();
        profileMini.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        profileMini.setOpaque(false);
        profileMini.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Avatar klein
        JLabel avatarLabel = new JLabel();
        ImageIcon avatarIcon = loadAvatarIcon(nutzer.getProfilePictureUrl(), 32, 32);
        avatarLabel.setIcon(avatarIcon);

        // Name + Status
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        textPanel.setOpaque(false);

        String displayName =
                nutzer.getDisplayName() != null && !nutzer.getDisplayName().isEmpty()
                        ? nutzer.getDisplayName()
                        : nutzer.getUsername();

        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        // Status-Linie: ● ONLINE
        JLabel statusLabel = new JLabel();
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        NutzerStatus status = nutzer.getStatus();
        String statusText = (status != null) ? status.name() : "OFFLINE";
        Color statusColor = colorForStatus(status);

        // farbiger Punkt + Text
        statusLabel.setText("● " + statusText);
        statusLabel.setForeground(statusColor);

        textPanel.add(nameLabel);
        textPanel.add(statusLabel);

        profileMini.add(textPanel);
        profileMini.add(avatarLabel);

        // Klick -> Profilfenster öffnen
        profileMini.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new ProfileFrame(nutzer);
            }
        });

        header.add(profileMini, BorderLayout.EAST);
        return header;
    }

    // =======================================================
    // SIDEBAR: Buttons links
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

        // Buttons anlegen
        side.add(createMenuButton("Quiz starten", () -> {
            try {
                showCard("QUIZ");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        side.add(Box.createRigidArea(new Dimension(0, 8)));
        side.add(createMenuButton("Statistiken", () -> {
            try {
                showCard("STATS");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        side.add(Box.createRigidArea(new Dimension(0, 8)));
        side.add(createMenuButton("Einstellungen", () -> {
            try {
                showCard("SETTINGS");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
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
    // CONTENT-BEREICH (Mitte) – CardLayout für spätere Inhalte
    // =======================================================
    private JPanel buildContentPanel() {
        JPanel panel = new JPanel(new CardLayout());

        // Placeholder-Panels
        panel.add(buildPlaceholderPanel("Willkommen, " + nutzer.getUsername() + "!", """
                Wähle links im Menü eine Aktion aus, um zu starten.
                """), "HOME");

        panel.add(buildPlaceholderPanel("Quiz starten", """
                Hier könnte dein Quiz-Panel stehen (Fragen, Timer, Punkte, ...).
                """), "QUIZ");

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

        // Standard-Ansicht: HOME
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

    private void showCard(String name) throws IOException {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, name);

        System.out.println("Selected: " + name);

        if (name.equals("QUIZ")){
            C2SPlayQuiz packet = new C2SPlayQuiz();
            ClientNetworkController.socketClient.send(packet);
        }

    }

    private void exitApp() {
        // Hier könntest du vorher noch "Logout" Packet an den Server schicken
        System.exit(0);
    }

    // =======================================================
    // HILFSMETHODEN: Avatar + Statusfarbe
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
        // Fallback: einfacher grauer Kreis
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
