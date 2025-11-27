package at.tgm.client;

import at.tgm.objects.Nutzer;
import at.tgm.network.packets.NutzerStatus;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLConnection;

public class ProfileFrame extends JFrame {

    private final Nutzer nutzer;

    public ProfileFrame(Nutzer n) {
        this.nutzer = n;

        setTitle("Profil – " + n.getUsername());
        setSize(450, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildInfoPanel(), BorderLayout.CENTER);
        add(buildFooterPanel(), BorderLayout.SOUTH);

        setVisible(true);
    }

    // =======================================================
    // HEADER: Avatar + Name + Status
    // =======================================================
    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel();
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        header.setBackground(new Color(240, 240, 240));

        // Avatar
        JLabel avatar = new JLabel();
        avatar.setHorizontalAlignment(SwingConstants.CENTER);

        if (nutzer.getProfilePictureUrl() != null && !nutzer.getProfilePictureUrl().isEmpty()) {
            try {
                ImageIcon img = loadImageFromURL(nutzer.getProfilePictureUrl());
                if (img != null) {
                    avatar.setIcon(img);
                } else {
                    avatar.setIcon(defaultAvatar());
                }

            } catch (Exception e) {
                avatar.setIcon(defaultAvatar());
            }
        } else {
            avatar.setIcon(defaultAvatar());
        }

        // Name
        JLabel name = new JLabel(nutzer.getDisplayName() != null ? nutzer.getDisplayName() : nutzer.getUsername());
        name.setFont(new Font("Arial", Font.BOLD, 22));
        name.setHorizontalAlignment(SwingConstants.CENTER);

        // Status-Punkt
        JLabel statusDot = new JLabel("●");
        statusDot.setFont(new Font("Arial", Font.BOLD, 22));
        statusDot.setForeground(colorForStatus(nutzer.getStatus()));
        statusDot.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel top = new JPanel(new GridLayout(3, 1));
        top.setOpaque(false);
        top.add(avatar);
        top.add(name);
        top.add(statusDot);

        header.add(top, BorderLayout.CENTER);
        return header;
    }

    private Icon defaultAvatar() {
        Image img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setColor(Color.LIGHT_GRAY);
        g.fillOval(0, 0, 100, 100);
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
            default: return Color.GRAY;
        }
    }

    // =======================================================
    // MITTELTEIL: Detailinformationen
    // =======================================================
    private JPanel buildInfoPanel() {
        JPanel info = new JPanel();
        info.setLayout(new GridLayout(0, 1, 0, 8));
        info.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        info.add(infoRow("Vorname:", nutzer.getFirstName()));
        info.add(infoRow("Nachname:", nutzer.getLastName()));
        info.add(infoRow("E-Mail:", nutzer.getEmail()));
        info.add(infoRow("Telefon:", nutzer.getPhoneNumber()));
        info.add(infoRow("UUID:", nutzer.getUuid()));

        info.add(infoRow("Account erstellt:", String.valueOf(nutzer.getCreatedAt())));
        info.add(infoRow("Letzter Login:", String.valueOf(nutzer.getLastLoginTimestamp())));

        info.add(infoRow("Beschreibung:", nutzer.getBeschreibung()));

        return info;
    }

    private JPanel infoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel val = new JLabel(value != null ? value : "—");
        val.setFont(new Font("Arial", Font.PLAIN, 14));

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.CENTER);

        return row;
    }

    // =======================================================
    // FOOTER
    // =======================================================
    private JPanel buildFooterPanel() {
        JPanel footer = new JPanel();
        footer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JButton close = new JButton("Schließen");
        close.addActionListener(e -> dispose());

        footer.add(close);
        return footer;
    }
    private ImageIcon loadImageFromURL(String url) {
        try {
            // Wikipedia & viele CDNs verlangen einen User-Agent
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.connect();

            Image img = ImageIO.read(conn.getInputStream());
            if (img == null) return null;

            // Skalieren
            Image scaled = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
