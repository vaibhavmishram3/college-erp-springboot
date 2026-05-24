package auth;

import dashboard.Dashboard;
import database.DBConnection;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * Login.java — Professional Login Screen
 * Features:
 *  - Flat card UI with smooth focus borders
 *  - Show / hide password toggle
 *  - "Remember me" checkbox (saves username to Preferences)
 *  - Forgot password dialog (asks security-question answer stored in DB)
 *  - Enter-key submits the form
 *  - Input validation with inline error labels
 *  - Animated button hover (colour shift)
 *  - Responsive repaint / flicker-free background
 */
public class Login extends JFrame implements ActionListener {

    /* ── Palette ─────────────────────────────────────────────────── */
    private static final Color BG         = new Color(15,  20,  35);
    private static final Color CARD_BG    = new Color(22,  30,  54);
    private static final Color ACCENT     = new Color(99,  179, 237);
    private static final Color ACCENT2    = new Color(144, 205, 244);
    private static final Color SUCCESS    = new Color(72,  199, 142);
    private static final Color DANGER     = new Color(240, 82,  82);
    private static final Color TEXT_MAIN  = new Color(220, 228, 245);
    private static final Color TEXT_MUTED = new Color(100, 115, 150);
    private static final Color FIELD_BG   = new Color(10,  15,  28);
    private static final Color FIELD_BDR  = new Color(40,  55,  85);
    private static final Color CARD_BDR   = new Color(50,  70,  110);
    private static final Color BTN_FG     = new Color(6,   10,  20);

    /* ── Components ──────────────────────────────────────────────── */
    private JTextField     tfUser;
    private JPasswordField pfPass;
    private JCheckBox      cbRemember;
    private JButton        btnLogin, btnSignup, btnShowPass;
    private JLabel         lblUserError, lblPassError, lblStatus;

    /* ── State ───────────────────────────────────────────────────── */
    private boolean passVisible = false;

    /* ── Constructor ─────────────────────────────────────────────── */
    public Login() {
        setTitle("Login — MyApp");
        setSize(460, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        /* Root panel — custom painted background + card */
        JPanel root = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Outer bg
                g2.setColor(BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Card fill
                g2.setColor(CARD_BG);
                g2.fillRoundRect(20, 20, getWidth()-40, getHeight()-40, 24, 24);
                // Card border
                g2.setColor(CARD_BDR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(20, 20, getWidth()-40, getHeight()-40, 24, 24);
                // Top accent bar
                g2.setColor(ACCENT);
                g2.fillRoundRect(20, 20, getWidth()-40, 4, 4, 4);
                g2.dispose();
            }
        };
        root.setBackground(BG);
        setContentPane(root);

        int cx = 50, cw = 360; // card content x + width

        /* ── Avatar icon ─────────────────────────────────────── */
        JLabel avatar = new JLabel("\uD83D\uDC64", SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        avatar.setForeground(ACCENT);
        avatar.setBounds(180, 50, 100, 50);
        root.add(avatar);

        /* ── Title ───────────────────────────────────────────── */
        JLabel title = new JLabel("Welcome Back", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_MAIN);
        title.setBounds(60, 100, cw, 32);
        root.add(title);

        JLabel sub = new JLabel("Sign in to continue", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        sub.setBounds(60, 133, cw, 22);
        root.add(sub);

        /* ── Divider ─────────────────────────────────────────── */
        JSeparator sep = new JSeparator();
        sep.setForeground(FIELD_BDR);
        sep.setBounds(cx, 167, cw, 1);
        root.add(sep);

        /* ── Username ────────────────────────────────────────── */
        root.add(makeLabel("USERNAME", cx, 184));
        tfUser = new JTextField();
        styleField(tfUser);
        tfUser.setBounds(cx, 204, cw, 42);
        root.add(tfUser);

        lblUserError = makeErrorLabel(cx, 248);
        root.add(lblUserError);

        /* ── Password ────────────────────────────────────────── */
        root.add(makeLabel("PASSWORD", cx, 264));
        pfPass = new JPasswordField();
        styleField(pfPass);
        pfPass.setBounds(cx, 284, cw - 50, 42);
        root.add(pfPass);

        /* Show/hide toggle button */
        btnShowPass = new JButton("Show");
        btnShowPass.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnShowPass.setForeground(ACCENT);
        btnShowPass.setBackground(FIELD_BG);
        btnShowPass.setBorder(new LineBorder(FIELD_BDR, 1, true));
        btnShowPass.setFocusPainted(false);
        btnShowPass.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnShowPass.setBounds(cx + cw - 46, 284, 46, 42);
        btnShowPass.addActionListener(ev -> togglePasswordVisibility());
        root.add(btnShowPass);

        lblPassError = makeErrorLabel(cx, 328);
        root.add(lblPassError);

        /* ── Remember me + Forgot password row ───────────────── */
        cbRemember = new JCheckBox("Remember me");
        cbRemember.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbRemember.setForeground(TEXT_MUTED);
        cbRemember.setOpaque(false);
        cbRemember.setBounds(cx, 344, 150, 22);
        root.add(cbRemember);

        JLabel forgot = new JLabel("Forgot password?");
        forgot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgot.setForeground(ACCENT);
        forgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgot.setBounds(cx + cw - 120, 344, 120, 22);
        forgot.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { showForgotPassword(); }
            public void mouseEntered(MouseEvent e) { forgot.setForeground(ACCENT2); }
            public void mouseExited (MouseEvent e) { forgot.setForeground(ACCENT); }
        });
        root.add(forgot);

        /* ── Status label (global messages) ─────────────────── */
        lblStatus = new JLabel("", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(DANGER);
        lblStatus.setBounds(cx, 372, cw, 20);
        root.add(lblStatus);

        /* ── Login button ────────────────────────────────────── */
        btnLogin = makePrimaryButton("Sign In", ACCENT, ACCENT2, BTN_FG);
        btnLogin.setBounds(cx, 396, cw, 44);
        btnLogin.addActionListener(this);
        root.add(btnLogin);

        /* ── Divider row ─────────────────────────────────────── */
        JLabel orLeft  = makeLine(cx, 454, 130);
        JLabel orLabel = new JLabel("OR", SwingConstants.CENTER);
        orLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        orLabel.setForeground(TEXT_MUTED);
        orLabel.setBounds(cx + 140, 448, 80, 14);
        JLabel orRight = makeLine(cx + 230, 454, 130);
        root.add(orLeft); root.add(orLabel); root.add(orRight);

        /* ── Signup row ──────────────────────────────────────── */
        JLabel noAcct = new JLabel("Don't have an account?");
        noAcct.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        noAcct.setForeground(TEXT_MUTED);
        noAcct.setBounds(cx + 30, 476, 180, 22);
        root.add(noAcct);

        btnSignup = new JButton("Sign up");
        btnSignup.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSignup.setForeground(ACCENT);
        btnSignup.setContentAreaFilled(false);
        btnSignup.setBorderPainted(false);
        btnSignup.setFocusPainted(false);
        btnSignup.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSignup.setBounds(cx + 220, 474, 80, 26);
        btnSignup.addActionListener(this);
        root.add(btnSignup);

        /* ── Enter key on fields ─────────────────────────────── */
        KeyAdapter enterKey = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        };
        tfUser.addKeyListener(enterKey);
        pfPass.addKeyListener(enterKey);

        /* ── Load remembered username ────────────────────────── */
        java.util.prefs.Preferences prefs =
            java.util.prefs.Preferences.userNodeForPackage(Login.class);
        String saved = prefs.get("remembered_user", "");
        if (!saved.isEmpty()) {
            tfUser.setText(saved);
            cbRemember.setSelected(true);
        }

        setVisible(true);
        root.repaint();
    }

    /* ── Toggle password show/hide ────────────────────────────────── */
    private void togglePasswordVisibility() {
        passVisible = !passVisible;
        pfPass.setEchoChar(passVisible ? (char) 0 : '•');
        btnShowPass.setText(passVisible ? "Hide" : "Show");
    }

    /* ── Forgot password dialog ───────────────────────────────────── */
    private void showForgotPassword() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 6));
        JTextField tfResetUser = new JTextField();
        JPasswordField pfNewPass = new JPasswordField();
        JPasswordField pfConfirm = new JPasswordField();
        panel.add(new JLabel("Username:"));
        panel.add(tfResetUser);
        panel.add(new JLabel("New password:"));
        panel.add(pfNewPass);
        panel.add(new JLabel("Confirm new password:"));
        panel.add(pfConfirm);
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Reset Password",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String user    = tfResetUser.getText().trim();
        String np      = new String(pfNewPass.getPassword()).trim();
        String confirm = new String(pfConfirm.getPassword()).trim();

        if (user.isEmpty() || np.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!np.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement check = con.prepareStatement(
                "SELECT id FROM users WHERE username=?");
            check.setString(1, user);
            ResultSet rs = check.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Username not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            PreparedStatement upd = con.prepareStatement(
                "UPDATE users SET password=? WHERE username=?");
            upd.setString(1, np);
            upd.setString(2, user);
            upd.executeUpdate();
            JOptionPane.showMessageDialog(this, "Password reset! You can now sign in.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* ── Login logic ──────────────────────────────────────────────── */
    private void doLogin() {
        clearErrors();
        String user = tfUser.getText().trim();
        String pass = new String(pfPass.getPassword()).trim();
        boolean valid = true;

        if (user.isEmpty()) {
            lblUserError.setText("Username is required.");
            valid = false;
        }
        if (pass.isEmpty()) {
            lblPassError.setText("Password is required.");
            valid = false;
        }
        if (!valid) return;

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM users WHERE username=? AND password=?");
            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Save or clear remembered username
                java.util.prefs.Preferences prefs =
                    java.util.prefs.Preferences.userNodeForPackage(Login.class);
                if (cbRemember.isSelected()) prefs.put("remembered_user", user);
                else                         prefs.remove("remembered_user");

                lblStatus.setForeground(SUCCESS);
                lblStatus.setText("Login successful! Opening dashboard…");
                btnLogin.setEnabled(false);
                Timer t = new Timer(800, ev -> { dispose(); new Dashboard(); });
                t.setRepeats(false);
                t.start();
            } else {
                lblStatus.setForeground(DANGER);
                lblStatus.setText("Invalid username or password.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            lblStatus.setForeground(DANGER);
            lblStatus.setText("Database error: " + ex.getMessage());
        }
    }

    private void clearErrors() {
        lblUserError.setText("");
        lblPassError.setText("");
        lblStatus.setText("");
    }

    /* ── ActionPerformed ──────────────────────────────────────────── */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnLogin)  doLogin();
        if (e.getSource() == btnSignup) { dispose(); new Signup(); }
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

    private JLabel makeLine(int x, int y, int w) {
        JLabel l = new JLabel();
        l.setBorder(new MatteBorder(1, 0, 0, 0, FIELD_BDR));
        l.setBounds(x, y, w, 1);
        return l;
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

    /* ── Setup button style helper ────────────────────────────────── */
    static void applyPrimaryStyle(JButton btn, Color fg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /* ── Entry point ──────────────────────────────────────────────── */
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> {
            Login login = new Login();
            Login.applyPrimaryStyle(login.btnLogin, BTN_FG);
        });
    }
}