package auth;

import database.DBConnection;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Signup.java — Professional Registration Screen
 * Features:
 *  - Flat card UI matching Login style
 *  - Show / hide password toggles (both fields)
 *  - Live password strength meter (Weak / Fair / Strong / Very Strong)
 *  - Password match indicator
 *  - Inline field-level validation with error labels
 *  - Username length + character rules enforced
 *  - Duplicate username check via DB
 *  - Terms-of-service checkbox (required to submit)
 *  - Enter-key submits the form
 *  - Status label for success / error feedback
 */
public class Signup extends JFrame implements ActionListener {

    /* ── Palette ─────────────────────────────────────────────────── */
    private static final Color BG         = new Color(15,  20,  35);
    private static final Color CARD_BG    = new Color(22,  30,  54);
    private static final Color ACCENT     = new Color(99,  179, 237);
    private static final Color SUCCESS    = new Color(72,  199, 142);
    private static final Color DANGER     = new Color(240, 82,  82);
    private static final Color WARN       = new Color(246, 173, 85);
    private static final Color TEXT_MAIN  = new Color(220, 228, 245);
    private static final Color TEXT_MUTED = new Color(100, 115, 150);
    private static final Color FIELD_BG   = new Color(10,  15,  28);
    private static final Color FIELD_BDR  = new Color(40,  55,  85);
    private static final Color CARD_BDR   = new Color(50,  70,  110);
    private static final Color BTN_FG     = new Color(6,   10,  20);

    /* ── Components ──────────────────────────────────────────────── */
    private JTextField     tfUser;
    private JPasswordField pfPass, pfConfirm;
    private JButton        btnSignup, btnLogin, btnShowPass, btnShowConfirm;
    private JLabel         lblUserError, lblPassError, lblConfirmError, lblStatus;
    private JLabel[]       strengthBars = new JLabel[4];
    private JLabel         lblStrengthText, lblMatchIcon;
    private JCheckBox      cbTerms;

    /* ── State ───────────────────────────────────────────────────── */
    private boolean passVisible    = false;
    private boolean confirmVisible = false;

    /* ── Constructor ─────────────────────────────────────────────── */
    public Signup() {
        setTitle("Sign Up — MyApp");
        setSize(460, 660);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(CARD_BG);
                g2.fillRoundRect(20, 20, getWidth()-40, getHeight()-40, 24, 24);
                g2.setColor(CARD_BDR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(20, 20, getWidth()-40, getHeight()-40, 24, 24);
                // Top accent bar — green for signup
                g2.setColor(SUCCESS);
                g2.fillRoundRect(20, 20, getWidth()-40, 4, 4, 4);
                g2.dispose();
            }
        };
        root.setBackground(BG);
        setContentPane(root);

        int cx = 50, cw = 360;

        /* ── Avatar ──────────────────────────────────────────── */
        JLabel avatar = new JLabel("\uD83D\uDC64", SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        avatar.setForeground(SUCCESS);
        avatar.setBounds(180, 42, 100, 50);
        root.add(avatar);

        /* ── Title ───────────────────────────────────────────── */
        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_MAIN);
        title.setBounds(cx, 92, cw, 32);
        root.add(title);

        JLabel sub = new JLabel("Fill in the details below to get started", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        sub.setBounds(cx, 124, cw, 22);
        root.add(sub);

        /* ── Divider ─────────────────────────────────────────── */
        JSeparator sep = new JSeparator();
        sep.setForeground(FIELD_BDR);
        sep.setBounds(cx, 156, cw, 1);
        root.add(sep);

        /* ── Username ────────────────────────────────────────── */
        root.add(makeLabel("USERNAME", cx, 168));
        tfUser = new JTextField();
        styleField(tfUser);
        tfUser.setBounds(cx, 186, cw, 42);
        root.add(tfUser);
        // Show character count hint
        JLabel lblUserHint = new JLabel("3–20 characters, letters/numbers/underscores");
        lblUserHint.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblUserHint.setForeground(TEXT_MUTED);
        lblUserHint.setBounds(cx, 230, cw, 14);
        root.add(lblUserHint);

        lblUserError = makeErrorLabel(cx, 244);
        root.add(lblUserError);

        /* ── Password ────────────────────────────────────────── */
        root.add(makeLabel("PASSWORD", cx, 258));
        pfPass = new JPasswordField();
        styleField(pfPass);
        pfPass.setBounds(cx, 276, cw - 50, 42);
        root.add(pfPass);

        btnShowPass = makeToggleBtn(cx + cw - 46, 276);
        btnShowPass.addActionListener(ev -> {
            passVisible = !passVisible;
            pfPass.setEchoChar(passVisible ? (char) 0 : '•');
            btnShowPass.setText(passVisible ? "Hide" : "Show");
        });
        root.add(btnShowPass);

        /* Strength meter bars */
        int barY = 323, barW = 78, barH = 5, gap = 6;
        for (int i = 0; i < 4; i++) {
            strengthBars[i] = new JLabel();
            strengthBars[i].setOpaque(true);
            strengthBars[i].setBackground(FIELD_BDR);
            strengthBars[i].setBounds(cx + i * (barW + gap), barY, barW, barH);
            root.add(strengthBars[i]);
        }
        lblStrengthText = new JLabel("Strength: —");
        lblStrengthText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStrengthText.setForeground(TEXT_MUTED);
        lblStrengthText.setBounds(cx, 332, 200, 16);
        root.add(lblStrengthText);

        lblPassError = makeErrorLabel(cx, 348);
        root.add(lblPassError);

        pfPass.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { updateStrength(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { updateStrength(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateStrength(); }
        });

        /* ── Confirm Password ────────────────────────────────── */
        root.add(makeLabel("CONFIRM PASSWORD", cx, 362));
        pfConfirm = new JPasswordField();
        styleField(pfConfirm);
        pfConfirm.setBounds(cx, 380, cw - 50, 42);
        root.add(pfConfirm);

        btnShowConfirm = makeToggleBtn(cx + cw - 46, 380);
        btnShowConfirm.addActionListener(ev -> {
            confirmVisible = !confirmVisible;
            pfConfirm.setEchoChar(confirmVisible ? (char) 0 : '•');
            btnShowConfirm.setText(confirmVisible ? "Hide" : "Show");
        });
        root.add(btnShowConfirm);

        /* Match icon */
        lblMatchIcon = new JLabel();
        lblMatchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblMatchIcon.setForeground(TEXT_MUTED);
        lblMatchIcon.setBounds(cx, 424, 200, 16);
        root.add(lblMatchIcon);

        lblConfirmError = makeErrorLabel(cx, 440);
        root.add(lblConfirmError);

        pfConfirm.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { updateMatch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { updateMatch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateMatch(); }
        });

        /* ── Terms checkbox ──────────────────────────────────── */
        cbTerms = new JCheckBox("<html>I agree to the <u><font color='#63B3ED'>Terms of Service</font></u> and <u><font color='#63B3ED'>Privacy Policy</font></u></html>");
        cbTerms.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbTerms.setForeground(TEXT_MUTED);
        cbTerms.setOpaque(false);
        cbTerms.setBounds(cx - 4, 454, cw + 4, 22);
        root.add(cbTerms);

        /* ── Status label ────────────────────────────────────── */
        lblStatus = new JLabel("", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(DANGER);
        lblStatus.setBounds(cx, 480, cw, 18);
        root.add(lblStatus);

        /* ── Create Account button ───────────────────────────── */
        btnSignup = makePrimaryButton("Create Account", SUCCESS, SUCCESS.brighter(), BTN_FG);
        Login.applyPrimaryStyle(btnSignup, BTN_FG);
        btnSignup.setBounds(cx, 502, cw, 44);
        btnSignup.addActionListener(this);
        root.add(btnSignup);

        /* ── Sign in row ─────────────────────────────────────── */
        JLabel hasAcct = new JLabel("Already have an account?");
        hasAcct.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hasAcct.setForeground(TEXT_MUTED);
        hasAcct.setBounds(cx + 24, 560, 185, 22);
        root.add(hasAcct);

        btnLogin = new JButton("Sign in");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.setForeground(ACCENT);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setBounds(cx + 218, 558, 85, 26);
        btnLogin.addActionListener(this);
        root.add(btnLogin);

        /* ── Enter key ───────────────────────────────────────── */
        KeyAdapter enter = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doSignup();
            }
        };
        tfUser.addKeyListener(enter);
        pfPass.addKeyListener(enter);
        pfConfirm.addKeyListener(enter);

        setVisible(true);
        root.repaint();
    }

    /* ── Password strength meter ──────────────────────────────────── */
    private void updateStrength() {
        String p = new String(pfPass.getPassword());
        int score = 0;
        if (p.length() >= 8)                          score++;
        if (p.matches(".*[A-Z].*"))                   score++;
        if (p.matches(".*[0-9].*"))                   score++;
        if (p.matches(".*[^A-Za-z0-9].*"))            score++;

        Color[] colours = { DANGER, WARN, ACCENT, SUCCESS };
        String[] labels = { "Weak", "Fair", "Strong", "Very Strong" };

        for (int i = 0; i < 4; i++) {
            strengthBars[i].setBackground(i < score ? colours[score - 1] : FIELD_BDR);
        }
        if (p.isEmpty()) {
            lblStrengthText.setText("Strength: —");
            lblStrengthText.setForeground(TEXT_MUTED);
        } else {
            lblStrengthText.setText("Strength: " + labels[score == 0 ? 0 : score - 1]);
            lblStrengthText.setForeground(colours[score == 0 ? 0 : score - 1]);
        }
        updateMatch();
    }

    /* ── Password match indicator ─────────────────────────────────── */
    private void updateMatch() {
        String p = new String(pfPass.getPassword());
        String c = new String(pfConfirm.getPassword());
        if (c.isEmpty()) {
            lblMatchIcon.setText("");
            return;
        }
        if (p.equals(c)) {
            lblMatchIcon.setForeground(SUCCESS);
            lblMatchIcon.setText("✔  Passwords match");
        } else {
            lblMatchIcon.setForeground(DANGER);
            lblMatchIcon.setText("✘  Passwords do not match");
        }
    }

    /* ── Signup logic ─────────────────────────────────────────────── */
    private void doSignup() {
        clearErrors();
        String user    = tfUser.getText().trim();
        String pass    = new String(pfPass.getPassword()).trim();
        String confirm = new String(pfConfirm.getPassword()).trim();
        boolean valid  = true;

        // Username rules
        if (user.isEmpty()) {
            lblUserError.setText("Username is required.");
            valid = false;
        } else if (user.length() < 3 || user.length() > 20) {
            lblUserError.setText("Username must be 3–20 characters.");
            valid = false;
        } else if (!user.matches("[A-Za-z0-9_]+")) {
            lblUserError.setText("Only letters, numbers, underscores allowed.");
            valid = false;
        }

        // Password rules
        if (pass.isEmpty()) {
            lblPassError.setText("Password is required.");
            valid = false;
        } else if (pass.length() < 6) {
            lblPassError.setText("Password must be at least 6 characters.");
            valid = false;
        }

        // Confirm rules
        if (confirm.isEmpty()) {
            lblConfirmError.setText("Please confirm your password.");
            valid = false;
        } else if (!pass.equals(confirm)) {
            lblConfirmError.setText("Passwords do not match.");
            valid = false;
        }

        // Terms
        if (!cbTerms.isSelected()) {
            lblStatus.setForeground(DANGER);
            lblStatus.setText("You must agree to the Terms of Service.");
            valid = false;
        }

        if (!valid) return;

        try {
            Connection con = DBConnection.getConnection();

            // Check duplicate
            PreparedStatement chk = con.prepareStatement(
                "SELECT id FROM users WHERE username=?");
            chk.setString(1, user);
            if (chk.executeQuery().next()) {
                lblUserError.setText("Username already taken.");
                return;
            }

            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO users(username, password) VALUES(?, ?)");
            ps.setString(1, user);
            ps.setString(2, pass);
            ps.executeUpdate();

            lblStatus.setForeground(SUCCESS);
            lblStatus.setText("Account created! Redirecting to login…");
            btnSignup.setEnabled(false);
            Timer t = new Timer(900, ev -> { dispose(); new Login(); });
            t.setRepeats(false);
            t.start();

        } catch (Exception ex) {
            ex.printStackTrace();
            lblStatus.setForeground(DANGER);
            lblStatus.setText("Database error: " + ex.getMessage());
        }
    }

    private void clearErrors() {
        lblUserError.setText("");
        lblPassError.setText("");
        lblConfirmError.setText("");
        lblStatus.setText("");
    }

    /* ── ActionPerformed ──────────────────────────────────────────── */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnSignup) doSignup();
        if (e.getSource() == btnLogin)  { dispose(); new Login(); }
    }

    /* ── UI helpers ───────────────────────────────────────────────── */
    private JLabel makeLabel(String text, int x, int y) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(ACCENT);
        l.setBounds(x, y, 340, 16);
        return l;
    }

    private JLabel makeErrorLabel(int x, int y) {
        JLabel l = new JLabel();
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(DANGER);
        l.setBounds(x, y, 340, 16);
        return l;
    }

    private JButton makeToggleBtn(int x, int y) {
        JButton b = new JButton("Show");
        b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        b.setForeground(ACCENT);
        b.setBackground(FIELD_BG);
        b.setBorder(new LineBorder(FIELD_BDR, 1, true));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBounds(x, y, 46, 42);
        return b;
    }

    private void styleField(JTextField tf) {
        tf.setBackground(FIELD_BG);
        tf.setForeground(TEXT_MAIN);
        tf.setCaretColor(ACCENT);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setOpaque(true);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(FIELD_BDR, 1, true),
            BorderFactory.createEmptyBorder(0, 12, 0, 12)));
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(ACCENT, 1, true),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
            }
            public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(FIELD_BDR, 1, true),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
            }
        });
    }

    private JButton makePrimaryButton(String text, Color normal, Color hover, Color fg) {
        return new JButton(text) {
            private boolean hov = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? (hov ? hover : normal) : FIELD_BDR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
    }

    /* ── Entry point ──────────────────────────────────────────────── */
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(Signup::new);
    }
}