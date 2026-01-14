package at.tgm.client.profile;

import at.tgm.objects.Note;
import at.tgm.objects.Nutzer;
import at.tgm.objects.NutzerStatus;
import at.tgm.objects.Schueler;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Modernes Profil-Panel für die Anzeige von Nutzerinformationen.
 * Zeigt Avatar, Status, persönliche Daten und ggf. Noten (für Schüler) an.
 */
public class ProfilePanel extends JPanel {

    private static final int AVATAR_SIZE = 120;
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color HEADER_BACKGROUND = new Color(248, 248, 248);
    private static final Color TEXT_COLOR = new Color(50, 50, 50);
    private static final Color LABEL_COLOR = new Color(100, 100, 100);
    private static final Font NAME_FONT = new Font("Arial", Font.BOLD, 24);
    private static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 13);
    private static final Font VALUE_FONT = new Font("Arial", Font.PLAIN, 13);
    private static final Font STATUS_FONT = new Font("Arial", Font.PLAIN, 12);
    private static final Font FOOTER_FONT = new Font("Arial", Font.ITALIC, 11);

    private final Nutzer nutzer;

    public ProfilePanel(Nutzer nutzer) {
        this.nutzer = nutzer;
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Header mit Avatar, Name und Status
        add(buildHeaderSection(), BorderLayout.NORTH);
        
        // Hauptinhalt mit Nutzerinformationen (in ScrollPane)
        JPanel contentPanel = buildContentSection();
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Sanfteres Scrollen
        scrollPane.setBackground(BACKGROUND_COLOR);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        add(scrollPane, BorderLayout.CENTER);
        
        // Footer
        add(buildFooterSection(), BorderLayout.SOUTH);
    }

    /**
     * Erstellt den Header-Bereich mit Avatar, Name und Status.
     */
    private JPanel buildHeaderSection() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BACKGROUND);
        header.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Zentrierter Container für Avatar, Name und Status
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);
        centerContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Avatar
        JLabel avatarLabel = createAvatarLabel(AVATAR_SIZE);
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerContainer.add(avatarLabel);
        centerContainer.add(Box.createRigidArea(new Dimension(0, 16)));

        // Name
        String displayName = getDisplayName();
        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setFont(NAME_FONT);
        nameLabel.setForeground(TEXT_COLOR);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerContainer.add(nameLabel);
        centerContainer.add(Box.createRigidArea(new Dimension(0, 8)));

        // Status
        JLabel statusLabel = createStatusLabel();
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerContainer.add(statusLabel);

        header.add(centerContainer, BorderLayout.CENTER);
        return header;
    }

    /**
     * Erstellt das Avatar-Label mit Bild oder Standard-Avatar.
     */
    private JLabel createAvatarLabel(int size) {
        JLabel avatarLabel = new JLabel();
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setVerticalAlignment(SwingConstants.CENTER);

        ImageIcon avatarIcon = loadAvatarIcon(nutzer.getProfilePictureUrl(), size);
        if (avatarIcon == null) {
            avatarIcon = createDefaultAvatar(size);
        }
        avatarLabel.setIcon(avatarIcon);

        return avatarLabel;
    }

    /**
     * Erstellt das Status-Label mit farbigem Punkt und Text.
     */
    private JLabel createStatusLabel() {
        NutzerStatus status = nutzer.getStatus();
        String statusText = getStatusText(status);
        Color statusColor = getStatusColor(status);

        JLabel statusLabel = new JLabel("● " + statusText);
        statusLabel.setFont(STATUS_FONT);
        statusLabel.setForeground(statusColor);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        return statusLabel;
    }

    /**
     * Erstellt den Hauptinhalt mit allen Nutzerinformationen.
     */
    private JPanel buildContentSection() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BACKGROUND_COLOR);
        content.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Note-Sektion (nur für Schüler)
        if (nutzer instanceof Schueler) {
            addNoteSection(content);
            content.add(Box.createRigidArea(new Dimension(0, 20)));
        }

        // Persönliche Informationen
        addInfoRow(content, "Vorname:", nutzer.getFirstName());
        addInfoRow(content, "Nachname:", nutzer.getLastName());
        addInfoRow(content, "E-Mail:", nutzer.getEmail());
        addInfoRow(content, "Telefon:", nutzer.getPhoneNumber());
        addInfoRow(content, "UUID:", nutzer.getUuid());
        addInfoRow(content, "Account erstellt:", formatTimestamp(nutzer.getCreatedAt()));
        addInfoRow(content, "Letzter Login:", formatTimestamp(nutzer.getLastLoginTimestamp()));
        
        // Beschreibung (kann mehrzeilig sein)
        String beschreibung = nutzer.getBeschreibung();
        if (beschreibung != null && !beschreibung.trim().isEmpty()) {
            content.add(Box.createRigidArea(new Dimension(0, 12)));
            addMultilineInfoRow(content, "Beschreibung:", beschreibung);
        }

        // Füllt verbleibenden Platz
        content.add(Box.createVerticalGlue());

        return content;
    }

    /**
     * Fügt die Note-Sektion für Schüler hinzu.
     */
    private void addNoteSection(JPanel parent) {
            Schueler schueler = (Schueler) nutzer;
                Note note = schueler.getNote();

        if (note != null && note.getNotenwert() != null) {
            String noteText = note.getNotenwert().getDisplayName();
            String reason = note.getReason();

            // Note mit hervorgehobener Darstellung
            JPanel notePanel = createHighlightedInfoRow("Note:", noteText);
            parent.add(notePanel);
            parent.add(Box.createRigidArea(new Dimension(0, 8)));

            // Begründung, falls vorhanden
            if (reason != null && !reason.trim().isEmpty()) {
                addInfoRow(parent, "Begründung:", reason);
            }
        } else {
            addInfoRow(parent, "Note:", "Keine Note vergeben");
        }
    }

    /**
     * Fügt eine einfache Info-Zeile hinzu.
     */
    private void addInfoRow(JPanel parent, String label, String value) {
        JPanel row = createInfoRow(label, value, false);
        parent.add(row);
        parent.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    /**
     * Fügt eine mehrzeilige Info-Zeile hinzu (z.B. für Beschreibung).
     */
    private void addMultilineInfoRow(JPanel parent, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(20, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Label
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(LABEL_FONT);
        labelComponent.setForeground(LABEL_COLOR);
        labelComponent.setVerticalAlignment(SwingConstants.TOP);
        labelComponent.setPreferredSize(new Dimension(160, 20));
        row.add(labelComponent, BorderLayout.WEST);

        // Wert (mehrzeilig)
        JTextArea valueComponent = new JTextArea(value);
        valueComponent.setFont(VALUE_FONT);
        valueComponent.setForeground(TEXT_COLOR);
        valueComponent.setEditable(false);
        valueComponent.setOpaque(false);
        valueComponent.setLineWrap(true);
        valueComponent.setWrapStyleWord(true);
        valueComponent.setFocusable(false);
        valueComponent.setBackground(null);
        valueComponent.setBorder(null);
        
        // Berechne optimale Höhe
        valueComponent.setSize(new Dimension(400, Short.MAX_VALUE));
        valueComponent.setSize(valueComponent.getPreferredSize());

        row.add(valueComponent, BorderLayout.CENTER);
        parent.add(row);
        parent.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    /**
     * Erstellt eine normale Info-Zeile.
     */
    private JPanel createInfoRow(String label, String value, boolean highlight) {
        JPanel row = new JPanel(new BorderLayout(20, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Label
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(LABEL_FONT);
        labelComponent.setForeground(LABEL_COLOR);
        labelComponent.setPreferredSize(new Dimension(160, 20));
        row.add(labelComponent, BorderLayout.WEST);

        // Wert
        String displayValue = (value != null && !value.isEmpty()) ? value : "—";
        JLabel valueComponent = new JLabel(displayValue);
        valueComponent.setFont(highlight ? LABEL_FONT : VALUE_FONT);
        valueComponent.setForeground(highlight ? new Color(0, 100, 200) : TEXT_COLOR);
        row.add(valueComponent, BorderLayout.CENTER);

        return row;
    }

    /**
     * Erstellt eine hervorgehobene Info-Zeile (z.B. für Note).
     */
    private JPanel createHighlightedInfoRow(String label, String value) {
        return createInfoRow(label, value, true);
    }

    /**
     * Erstellt den Footer-Bereich.
     */
    private JPanel buildFooterSection() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(BACKGROUND_COLOR);
        footer.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel footerLabel = new JLabel("Profil wird im Dashboard angezeigt.");
        footerLabel.setFont(FOOTER_FONT);
        footerLabel.setForeground(new Color(150, 150, 150));
        footer.add(footerLabel);

        return footer;
    }

    /**
     * Lädt ein Avatar-Bild von einer URL.
     */
    private ImageIcon loadAvatarIcon(String url, int size) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        try {
            URL imageUrl = new URL(url);
            URLConnection connection = imageUrl.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();

            BufferedImage image = ImageIO.read(connection.getInputStream());
            if (image == null) {
                return null;
            }

            // Skaliere das Bild auf die gewünschte Größe
            Image scaledImage = image.getScaledInstance(
                size, 
                size, 
                Image.SCALE_SMOOTH
            );

            return new ImageIcon(scaledImage);
        } catch (Exception e) {
            // Fehler beim Laden - verwende Standard-Avatar
            return null;
        }
    }

    /**
     * Erstellt einen Standard-Avatar (grauer Kreis).
     */
    private ImageIcon createDefaultAvatar(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        
        // Antialiasing für bessere Qualität
        g.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING, 
            RenderingHints.VALUE_ANTIALIAS_ON
        );
        
        // Zeichne grauen Kreis
        g.setColor(new Color(220, 220, 220));
        g.fillOval(0, 0, size, size);
        
        // Optional: Zeichne Initialen
        String initials = getInitials();
        if (initials != null && !initials.isEmpty()) {
            g.setColor(new Color(150, 150, 150));
            g.setFont(new Font("Arial", Font.BOLD, size / 3));
            FontMetrics fm = g.getFontMetrics();
            int x = (size - fm.stringWidth(initials)) / 2;
            int y = (size + fm.getAscent() - fm.getDescent()) / 2;
            g.drawString(initials, x, y);
        }
        
        g.dispose();
        return new ImageIcon(image);
    }

    /**
     * Gibt die Initialen des Nutzers zurück (z.B. "FR" für "Felix Riemer").
     */
    private String getInitials() {
        String firstName = nutzer.getFirstName();
        String lastName = nutzer.getLastName();
        
        StringBuilder initials = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            initials.append(firstName.charAt(0));
        }
        if (lastName != null && !lastName.isEmpty()) {
            initials.append(lastName.charAt(0));
        }
        
        return initials.length() > 0 ? initials.toString().toUpperCase() : null;
    }

    /**
     * Gibt den Anzeigenamen zurück (DisplayName oder Username).
     */
    private String getDisplayName() {
        String displayName = nutzer.getDisplayName();
        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName;
        }
        return nutzer.getUsername();
    }

    /**
     * Gibt den Status-Text zurück.
     */
    private String getStatusText(NutzerStatus status) {
        if (status == null) {
            return "OFFLINE";
        }
        
        switch (status) {
            case ONLINE:
                return "ONLINE";
            case AWAY:
                return "ABWESEND";
            case BUSY:
                return "BESCHÄFTIGT";
            case BANNED:
                return "GESPERRT";
            case OFFLINE:
            default:
                return "OFFLINE";
        }
    }

    /**
     * Gibt die Farbe für einen Status zurück.
     */
    private Color getStatusColor(NutzerStatus status) {
        if (status == null) {
            return Color.GRAY;
        }
        
        switch (status) {
            case ONLINE:
                return new Color(0, 180, 0);
            case AWAY:
                return new Color(255, 170, 0);
            case BUSY:
                return new Color(200, 0, 0);
            case BANNED:
                return Color.BLACK;
            case OFFLINE:
            default:
                return Color.GRAY;
        }
    }

    /**
     * Formatiert einen Timestamp als lesbares Datum.
     */
    private String formatTimestamp(long timestamp) {
        if (timestamp == 0 || timestamp < 0) {
            return "Nie";
        }
        
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd.MM.yyyy HH:mm:ss", 
                Locale.GERMAN
            );
            return dateFormat.format(new Date(timestamp));
        } catch (Exception e) {
            return "Ungültiges Datum";
        }
    }
}
