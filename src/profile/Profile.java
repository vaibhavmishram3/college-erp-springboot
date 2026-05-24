package profile;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.net.URI;
import java.net.URL;

public class Profile extends JFrame {

    private static final Color BG = new Color(245, 246, 250);
    private static final Color CARD = Color.WHITE;
    private static final Color TEXT = new Color(30, 30, 40);
    private static final Color MUTED = new Color(120, 120, 140);
    private static final Color ACCENT = new Color(55, 138, 221);
    private static final Color BORDER = new Color(220, 223, 230);

    public Profile() {

        setTitle("Admin Profile");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildBody(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        setVisible(true);
    }

    /* ================= HEADER ================= */
    private JPanel buildHeader() {

        JPanel p = new JPanel();
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel title = new JLabel("ADMIN PROFILE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT);

        JLabel sub = new JLabel("Account details & system access");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(MUTED);

        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(BG);
        col.add(title);
        col.add(Box.createVerticalStrut(4));
        col.add(sub);

        p.setLayout(new BorderLayout());
        p.add(col, BorderLayout.WEST);

        return p;
    }

    /* ================= BODY ================= */
    private JPanel buildBody() {

        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(BG);

        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(500, 280));
        card.setBackground(CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setLayout(new BorderLayout(20, 0));

        /* LEFT AVATAR */
        JLabel avatar = new JLabel();
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(140, 140));

        try {
            URL url = URI.create("https://cdn-icons-png.flaticon.com/512/3135/3135715.png").toURL();
            ImageIcon icon = new ImageIcon(url);
            Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            avatar.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            avatar.setText("USER");
            avatar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        }

        /* RIGHT INFO */
        JPanel info = new JPanel();
        info.setBackground(CARD);
        info.setLayout(new GridLayout(4, 1, 0, 12));

        info.add(row("Name", "Vaibhav Mishra"));
        info.add(row("Role", "Administrator"));
        info.add(row("Email", "admin@college.com"));
        info.add(row("Phone", "+91 9876543210"));

        card.add(avatar, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);

        wrap.add(card);

        return wrap;
    }

    private JPanel row(String label, String value) {

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CARD);

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(MUTED);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 13));
        v.setForeground(TEXT);

        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.EAST);

        return p;
    }

    /* ================= FOOTER ================= */
    private JPanel buildFooter() {

        JPanel p = new JPanel();
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(10, 10, 20, 10));

        JButton close = new JButton("Close");
        close.setPreferredSize(new Dimension(120, 35));
        close.setFocusPainted(false);
        close.setCursor(new Cursor(Cursor.HAND_CURSOR));

        close.setBackground(ACCENT);
        close.setForeground(Color.WHITE);
        close.setBorder(new EmptyBorder(5, 10, 5, 10));

        close.addActionListener(e -> dispose());

        p.add(close);
        return p;
    }

    public static void main(String[] args) {
        new Profile();
    }
}