package at.tgm.client.profile;

import at.tgm.objects.Nutzer;
import at.tgm.objects.NutzerStatus;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLConnection;

public class ProfilePanel extends JPanel {

    private final Nutzer nutzer;

    public ProfilePanel(Nutzer n) {
        this.nutzer = n;

        setLayout(new BorderLayout());

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildInfoPanel(), BorderLayout.CENTER);
        add(buildFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        header.setBackground(new Color(240, 240, 240));

        JLabel avatar = new JLabel();
        avatar.setHorizontalAlignment(SwingConstants.CENTER);

        ImageIcon img = null;
        if (nutzer.getProfilePictureUrl() != null && !nutzer.getProfilePictureUrl().isEmpty()) {
            img = loadImageFromURL(nutzer.getProfilePictureUrl());
        }
        if (img == null) {
            img = (ImageIcon) defaultAvatar();
        }
        avatar.setIcon(img);

        JLabel name = new JLabel(
                nutzer.getDisplayName() != null ? nutzer.getDisplayName() : nutzer.getUsername()
        );
        name.setFont(new Font("Arial", Font.BOLD, 22));
        name.setHorizontalAlignment(SwingConstants.CENTER);

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
        int size = 100;
        Image img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setColor(Color.LIGHT_GRAY);
        g.fillOval(0, 0, size, size);
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

    private JPanel buildInfoPanel() {
        JPanel info = new JPanel(new GridLayout(0, 1, 0, 8));
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

    private JPanel buildFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel hint = new JLabel("Profil wird im Dashboard angezeigt.");
        hint.setFont(new Font("Arial", Font.ITALIC, 12));

        footer.add(hint);
        return footer;
    }

    private ImageIcon loadImageFromURL(String url) {
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.connect();

            Image img = ImageIO.read(conn.getInputStream());
            if (img == null) return null;

            Image scaled = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
