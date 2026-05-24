package dashboard;

import auth.Login;
import students.StudentManagement;
import teachers.TeacherManagement;
import courses.CourseManagement;
import fees.FeesManagement;
import attendance.AttendanceManagement;
import results.ResultManagement;
import library.LibraryManagement;
import profile.Profile;
import about.About;
import database.DBConnection;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard.java — College Management Dashboard
 * All stat cards, attendance bars, and activity feed are live from MySQL.
 *
 * Schema used:
 * students(id, name, course, phone)
 * teachers(id, name, subject, phone)
 * courses(id, course_name, duration, fees)
 * fees(id, student_name, amount, payment_date)
 * attendance(id, student_name, attendance_date, status)
 * results(id, student_name, subject, marks)
 * library(id, book_name, author, quantity)
 */
public class Dashboard extends JFrame implements ActionListener {

    /* ── Palette ─────────────────────────────────────────────────── */
    private static final Color BG = new Color(245, 246, 250);
    private static final Color SIDEBAR_BG = new Color(255, 255, 255);
    private static final Color HEADER_BG = new Color(255, 255, 255);
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color SURFACE = new Color(246, 247, 250);
    private static final Color ACCENT = new Color(55, 138, 221);
    private static final Color ACCENT_LIGHT = new Color(230, 241, 251);
    private static final Color ACCENT_TEXT = new Color(12, 68, 124);
    private static final Color SUCCESS = new Color(99, 153, 34);
    private static final Color DANGER = new Color(226, 75, 74);
    private static final Color AMBER = new Color(186, 117, 23);
    private static final Color PURPLE = new Color(127, 119, 221);
    private static final Color BORDER = new Color(220, 222, 228);
    private static final Color TEXT_MAIN = new Color(20, 20, 30);
    private static final Color TEXT_MID = new Color(80, 85, 105);
    private static final Color TEXT_MUTED = new Color(140, 145, 165);
    private static final Color NAV_HOVER = new Color(243, 244, 248);
    private static final Color DANGER_LIGHT = new Color(255, 240, 240);

    /* ── Layout ──────────────────────────────────────────────────── */
    private static final int SIDEBAR_W = 210;
    private static final int HEADER_H = 52;
    private static final int W = 1100;
    private static final int H = 700;

    /* ── Nav buttons ─────────────────────────────────────────────── */
    private JButton btnStudents, btnTeachers, btnCourses, btnFees,
            btnAttendance, btnResults, btnLibrary,
            btnProfile, btnAbout, btnLogout;
    private JButton activeBtn = null;

    /* ── Header title ────────────────────────────────────────────── */
    private JLabel lblPageTitle;

    /* ── Stat card value / delta labels (filled by SwingWorker) ─── */
    private JLabel valStudents, dltStudents;
    private JLabel valTeachers, dltTeachers;
    private JLabel valCourses, dltCourses;
    private JLabel valFees, dltFees;

    /* ── Attendance bar data (filled by SwingWorker) ─────────────── */
    // Actually store per-category counts for the bar chart
    // Keys: "Present", "Absent", "Late"
    private int attTotalPresent = 0, attTotalAbsent = 0, attTotalLate = 0, attGrandTotal = 0;
    private final JPanel[] barPanels = new JPanel[3]; // present / absent / late bars

    /* ── Activity feed panel ─────────────────────────────────────── */
    private JPanel activityPanel;

    /*
     * ══════════════════════════════════════════════════════════════
     * CONSTRUCTOR
     * ═══════════════════════════════════════════════════════════════
     */
    public Dashboard() {
        setTitle("College Management System");
        setSize(W, H);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildRightSide(), BorderLayout.CENTER);

        setVisible(true);
        loadDashboardData(); // kick off background DB fetch
    }

    /*
     * ══════════════════════════════════════════════════════════════
     * BACKGROUND DATA LOAD (SwingWorker keeps UI responsive)
     * ═══════════════════════════════════════════════════════════════
     */

    /** All data fetched in one DB pass. */
    private static class DashData {
        // stat cards
        int studentCount, teacherCount, courseCount;
        double feesTotal, feesPaid, feesPending;
        // attendance summary from status column
        int attPresent, attAbsent, attLate, attTotal;
        // recent activity: rows of {icon, message, time}
        List<String[]> activity = new ArrayList<>();
        // error message if DB unreachable
        String error = null;
    }

    private void loadDashboardData() {
        // Show loading state
        valStudents.setText("…");
        dltStudents.setText("loading");
        valTeachers.setText("…");
        dltTeachers.setText("loading");
        valCourses.setText("…");
        dltCourses.setText("loading");
        valFees.setText("…");
        dltFees.setText("loading");

        new SwingWorker<DashData, Void>() {
            @Override
            protected DashData doInBackground() {
                DashData d = new DashData();
                try (Connection con = DBConnection.getConnection()) {

                    // ── students ──────────────────────────────────
                    try (Statement st = con.createStatement();
                            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM students")) {
                        if (rs.next())
                            d.studentCount = rs.getInt(1);
                    }

                    // ── teachers ──────────────────────────────────
                    try (Statement st = con.createStatement();
                            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM teachers")) {
                        if (rs.next())
                            d.teacherCount = rs.getInt(1);
                    }

                    // ── courses ───────────────────────────────────
                    try (Statement st = con.createStatement();
                            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM courses")) {
                        if (rs.next())
                            d.courseCount = rs.getInt(1);
                    }

                    // ── fees: total collected, pending ────────────
                    // We treat ALL rows as collected (no status column).
                    // Pending = courses.fees SUM minus fees.amount SUM
                    try (Statement st = con.createStatement();
                            ResultSet rs = st.executeQuery(
                                    "SELECT COALESCE(SUM(amount),0) AS paid FROM fees")) {
                        if (rs.next())
                            d.feesPaid = rs.getDouble("paid");
                    }
                    try (Statement st = con.createStatement();
                            ResultSet rs = st.executeQuery(
                                    "SELECT COALESCE(SUM(fees),0) AS total FROM courses")) {
                        if (rs.next())
                            d.feesTotal = rs.getDouble("total");
                    }
                    d.feesPending = Math.max(0, d.feesTotal - d.feesPaid);

                    // ── attendance: count by status ───────────────
                    // status values expected: 'Present', 'Absent', 'Late'
                    try (Statement st = con.createStatement();
                            ResultSet rs = st.executeQuery(
                                    "SELECT status, COUNT(*) AS cnt " +
                                            "FROM attendance GROUP BY status")) {
                        while (rs.next()) {
                            String s = rs.getString("status");
                            int cnt = rs.getInt("cnt");
                            d.attTotal += cnt;
                            if (s != null) {
                                switch (s.toLowerCase()) {
                                    case "present" -> d.attPresent += cnt;
                                    case "absent" -> d.attAbsent += cnt;
                                    case "late" -> d.attLate += cnt;
                                }
                            }
                        }
                    }

                    // ── recent activity feed ──────────────────────
                    // Combine recent records from fees, attendance, results, library
                    // into a unified timeline using UNION ALL + ORDER BY date DESC
                    String actSQL = "SELECT 'fee' AS src, student_name AS name, " +
                            "  CONCAT('Fee payment recorded — ₹', FORMAT(amount,0)) AS msg, " +
                            "  payment_date AS dt FROM fees " +
                            "UNION ALL " +
                            "SELECT 'attendance', student_name, " +
                            "  CONCAT('Attendance marked: ', status, ' on ', attendance_date) AS msg, " +
                            "  attendance_date FROM attendance " +
                            "UNION ALL " +
                            "SELECT 'result', student_name, " +
                            "  CONCAT('Result recorded — ', subject, ': ', marks, ' marks') AS msg, " +
                            "  CURDATE() FROM results " +
                            "UNION ALL " +
                            "SELECT 'library', book_name, " +
                            "  CONCAT('Library: ', book_name, ' (', quantity, ' copies)') AS msg, " +
                            "  CURDATE() FROM library " +
                            "ORDER BY dt DESC LIMIT 6";

                    try (Statement st = con.createStatement();
                            ResultSet rs = st.executeQuery(actSQL)) {
                        while (rs.next()) {
                            d.activity.add(new String[] {
                                    rs.getString("src"),
                                    rs.getString("msg"),
                                    rs.getString("dt") == null ? "" : rs.getString("dt")
                            });
                        }
                    }

                } catch (SQLException ex) {
                    d.error = ex.getMessage();
                }
                return d;
            }

            @Override
            protected void done() {
                try {
                    applyData(get());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    /** Push fetched data into UI on the EDT. */
    private void applyData(DashData d) {
        if (d.error != null) {
            JOptionPane.showMessageDialog(this,
                    "<html><b>Database connection failed:</b><br>" + d.error + "<br><br>" +
                            "Check DBConnection.java settings and ensure MySQL is running.</html>",
                    "DB Error", JOptionPane.ERROR_MESSAGE);
            valStudents.setText("—");
            valTeachers.setText("—");
            valCourses.setText("—");
            valFees.setText("—");
            dltStudents.setText("DB error");
            dltTeachers.setText("DB error");
            dltCourses.setText("DB error");
            dltFees.setText("DB error");
            return;
        }

        // ── Stat cards ─────────────────────────────────────────────
        valStudents.setText(String.valueOf(d.studentCount));
        dltStudents.setText(d.studentCount + " enrolled");
        dltStudents.setForeground(SUCCESS);

        valTeachers.setText(String.valueOf(d.teacherCount));
        dltTeachers.setText(d.teacherCount + " on staff");
        dltTeachers.setForeground(SUCCESS);

        valCourses.setText(String.valueOf(d.courseCount));
        dltCourses.setText("Active courses");
        dltCourses.setForeground(TEXT_MUTED);

        valFees.setText(formatRupee(d.feesPaid));
        double pendingPct = (d.feesTotal > 0) ? (d.feesPending / d.feesTotal * 100) : 0;
        dltFees.setText(String.format("%.0f%% pending", pendingPct));
        dltFees.setForeground(pendingPct > 20 ? DANGER : SUCCESS);

        // ── Attendance bars ─────────────────────────────────────────
        attTotalPresent = d.attPresent;
        attTotalAbsent = d.attAbsent;
        attTotalLate = d.attLate;
        attGrandTotal = Math.max(1, d.attTotal);
        for (JPanel bp : barPanels)
            if (bp != null)
                bp.repaint();

        // ── Activity feed ───────────────────────────────────────────
        activityPanel.removeAll();

        JLabel title = new JLabel("Recent activity");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(TEXT_MAIN);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        activityPanel.add(title);

        if (d.activity.isEmpty()) {
            JLabel none = new JLabel("No recent records found in the database.");
            none.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            none.setForeground(TEXT_MUTED);
            none.setAlignmentX(Component.LEFT_ALIGNMENT);
            activityPanel.add(none);
        } else {
            for (String[] row : d.activity) {
                Color dot = srcColor(row[0]);
                activityPanel.add(activityRow(dot, row[1], row[2]));
            }
        }
        activityPanel.revalidate();
        activityPanel.repaint();
    }

    private Color srcColor(String src) {
        return switch (src == null ? "" : src) {
            case "fee" -> ACCENT;
            case "attendance" -> AMBER;
            case "result" -> PURPLE;
            case "library" -> SUCCESS;
            default -> TEXT_MID;
        };
    }

    /** ₹840000 → ₹8.4L, ₹12000 → ₹12K, else ₹n */
    private static String formatRupee(double v) {
        if (v >= 100_000)
            return String.format("₹%.1fL", v / 100_000);
        if (v >= 1_000)
            return String.format("₹%.1fK", v / 1_000);
        return "₹" + (long) v;
    }

    /*
     * ══════════════════════════════════════════════════════════════
     * SIDEBAR
     * ═══════════════════════════════════════════════════════════════
     */
    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(SIDEBAR_W, H));
        side.setBackground(SIDEBAR_BG);
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new MatteBorder(0, 0, 0, 1, BORDER));

        side.add(buildLogoBlock());
        side.add(sideSection("MAIN"));

        btnStudents = navBtn("Students", "icons/student.png");
        btnTeachers = navBtn("Teachers", "icons/teacher.png");
        btnCourses = navBtn("Courses", "icons/course.png");

        side.add(btnStudents);
        side.add(btnTeachers);
        side.add(btnCourses);
        side.add(sideSection("OPERATIONS"));

        btnFees = navBtn("Fees", "icons/fees.png");
        btnAttendance = navBtn("Attendance", "icons/attendance.png");
        btnResults = navBtn("Results", "icons/result.png");
        btnLibrary = navBtn("Library", "icons/library.png");

        side.add(btnFees);
        side.add(btnAttendance);
        side.add(btnResults);
        side.add(btnLibrary);
        side.add(sideSection("ACCOUNT"));

        btnProfile = navBtn("Profile", "icons/user.png");
        btnAbout = navBtn("About", "icons/about.png");

        side.add(btnProfile);
        side.add(btnAbout);
        side.add(Box.createVerticalGlue());
        side.add(buildLogoutBtn());

        setActiveNav(btnStudents);
        return side;
    }

    private JPanel buildLogoBlock() {
        JPanel p = new JPanel(null);
        p.setBackground(SIDEBAR_BG);
        p.setMaximumSize(new Dimension(SIDEBAR_W, 72));
        p.setPreferredSize(new Dimension(SIDEBAR_W, 72));
        p.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));

        JPanel icon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        icon.setOpaque(false);
        icon.setLayout(new BorderLayout());
        JLabel icoLbl = new JLabel("\uD83C\uDFEB", SwingConstants.CENTER);
        icoLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        icon.add(icoLbl);
        icon.setBounds(14, 18, 32, 32);
        p.add(icon);

        JLabel name = new JLabel("College Management");
        name.setFont(new Font("Segoe UI", Font.BOLD, 11));
        name.setForeground(TEXT_MAIN);
        name.setBounds(54, 17, 148, 16);
        p.add(name);

        JLabel sub = new JLabel("Admin Portal");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(TEXT_MUTED);
        sub.setBounds(54, 35, 148, 14);
        p.add(sub);

        return p;
    }

    private JLabel sideSection(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 9));
        l.setForeground(TEXT_MUTED);
        l.setBorder(new EmptyBorder(10, 16, 4, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(SIDEBAR_W, 26));
        return l;
    }

    private JButton navBtn(String label, String iconPath) {

        final boolean[] hover = { false };

        ImageIcon icon = new ImageIcon(iconPath);

        Image img = icon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);

        JButton btn = new JButton(label, new ImageIcon(img)) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                if (activeBtn == this) {

                    g2.setColor(ACCENT_LIGHT);

                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                } else if (hover[0]) {

                    g2.setColor(NAV_HOVER);

                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }

                g2.dispose();

                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        btn.setForeground(TEXT_MID);

        btn.setHorizontalAlignment(SwingConstants.LEFT);

        btn.setIconTextGap(12);

        btn.setContentAreaFilled(false);

        btn.setBorderPainted(false);

        btn.setFocusPainted(false);

        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.setBorder(new EmptyBorder(8, 12, 8, 12));

        btn.setMaximumSize(new Dimension(SIDEBAR_W - 12, 40));

        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.setMargin(new Insets(0, 6, 0, 6));

        btn.addMouseListener(new MouseAdapter() {

            public void mouseEntered(MouseEvent e) {

                hover[0] = true;

                if (activeBtn != btn)
                    btn.repaint();
            }

            public void mouseExited(MouseEvent e) {

                hover[0] = false;

                btn.repaint();
            }
        });

        btn.addActionListener(this);

        return btn;
    }

    private JPanel buildLogoutBtn() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        p.setBackground(SIDEBAR_BG);
        p.setMaximumSize(new Dimension(SIDEBAR_W, 50));
        p.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));

        btnLogout = new JButton("\uD83D\uDEAA  Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnLogout.setForeground(DANGER);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(this);
        btnLogout.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                p.setBackground(DANGER_LIGHT);
            }

            public void mouseExited(MouseEvent e) {
                p.setBackground(SIDEBAR_BG);
            }
        });
        p.add(btnLogout);
        return p;
    }

    private void setActiveNav(JButton btn) {
        if (activeBtn != null)
            activeBtn.setForeground(TEXT_MID);
        activeBtn = btn;
        btn.setForeground(ACCENT_TEXT);
        btn.repaint();
    }

    /*
     * ══════════════════════════════════════════════════════════════
     * RIGHT SIDE
     * ═══════════════════════════════════════════════════════════════
     */
    private JPanel buildRightSide() {
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(BG);
        right.add(buildHeader(), BorderLayout.NORTH);
        right.add(buildScrollContent(), BorderLayout.CENTER);
        return right;
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(null);
        h.setPreferredSize(new Dimension(W - SIDEBAR_W, HEADER_H));
        h.setBackground(HEADER_BG);
        h.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));

        lblPageTitle = new JLabel("Dashboard");
        lblPageTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblPageTitle.setForeground(TEXT_MAIN);
        lblPageTitle.setBounds(20, 14, 300, 24);
        h.add(lblPageTitle);

        String dateStr = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy"));
        JLabel dateLbl = new JLabel("\uD83D\uDCC5  " + dateStr);
        dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLbl.setForeground(TEXT_MID);
        dateLbl.setBackground(SURFACE);
        dateLbl.setOpaque(true);
        dateLbl.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(4, 10, 4, 10)));
        dateLbl.setBounds(W - SIDEBAR_W - 300, 12, 170, 28);
        h.add(dateLbl);

        JLabel av = new JLabel("AD", SwingConstants.CENTER);
        av.setFont(new Font("Segoe UI", Font.BOLD, 11));
        av.setForeground(ACCENT_TEXT);
        av.setBackground(ACCENT_LIGHT);
        av.setOpaque(true);
        av.setBorder(BorderFactory.createLineBorder(ACCENT, 1));
        av.setBounds(W - SIDEBAR_W - 52, 12, 32, 28);
        h.add(av);

        return h;
    }

    private JScrollPane buildScrollContent() {
        JPanel content = new JPanel();
        content.setBackground(BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        content.add(buildWelcomeRow());
        content.add(Box.createVerticalStrut(16));
        content.add(buildStatCards());
        content.add(Box.createVerticalStrut(14));
        content.add(buildMidRow());
        content.add(Box.createVerticalStrut(14));
        content.add(buildActivityCard());
        content.add(Box.createVerticalStrut(10));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        scroll.setBackground(BG);
        return scroll;
    }

    /* ── Welcome row ──────────────────────────────────────────────── */
    private JPanel buildWelcomeRow() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setMaximumSize(new Dimension(Short.MAX_VALUE, 52));

        JLabel h = new JLabel("Welcome back, Admin");
        h.setFont(new Font("Segoe UI", Font.BOLD, 18));
        h.setForeground(TEXT_MAIN);

        JLabel sub = new JLabel("Live data from college_db · MySQL");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MID);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setBackground(BG);
        left.add(h);
        left.add(sub);
        p.add(left, BorderLayout.CENTER);

        JButton refresh = new JButton("\uD83D\uDD04  Refresh");
        refresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refresh.setForeground(ACCENT_TEXT);
        refresh.setBackground(ACCENT_LIGHT);
        refresh.setBorder(new CompoundBorder(
                new LineBorder(ACCENT, 1, true),
                new EmptyBorder(5, 12, 5, 12)));
        refresh.setFocusPainted(false);
        refresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refresh.addActionListener(e -> loadDashboardData());

        JPanel rp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        rp.setBackground(BG);
        rp.add(refresh);
        p.add(rp, BorderLayout.EAST);
        return p;
    }

    /* ── Stat cards ───────────────────────────────────────────────── */
    private JPanel buildStatCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 90));

        // Students
        JPanel sc = statCard("Students");
        valStudents = getValLabel(sc);
        dltStudents = getDltLabel(sc);
        row.add(sc);

        // Teachers
        JPanel tc = statCard("Teachers");
        valTeachers = getValLabel(tc);
        dltTeachers = getDltLabel(tc);
        row.add(tc);

        // Courses
        JPanel cc = statCard("Courses");
        valCourses = getValLabel(cc);
        dltCourses = getDltLabel(cc);
        row.add(cc);

        // Fees
        JPanel fc = statCard("Fees Collected");
        valFees = getValLabel(fc);
        dltFees = getDltLabel(fc);
        row.add(fc);

        return row;
    }

    private JPanel statCard(String label) {
        JPanel card = new JPanel(null);
        card.setBackground(SURFACE);
        card.setPreferredSize(new Dimension(0, 90));
        card.setBorder(new LineBorder(BORDER, 1, true));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(TEXT_MID);
        lbl.setBounds(14, 14, 160, 16);
        card.add(lbl);

        JLabel val = new JLabel("…");
        val.setName("val");
        val.setFont(new Font("Segoe UI", Font.BOLD, 22));
        val.setForeground(TEXT_MAIN);
        val.setBounds(14, 32, 160, 28);
        card.add(val);

        JLabel dlt = new JLabel("…");
        dlt.setName("dlt");
        dlt.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dlt.setForeground(TEXT_MUTED);
        dlt.setBounds(14, 62, 160, 16);
        card.add(dlt);

        return card;
    }

    private JLabel getValLabel(JPanel card) {
        for (Component c : card.getComponents())
            if ("val".equals(c.getName()))
                return (JLabel) c;
        return new JLabel();
    }

    private JLabel getDltLabel(JPanel card) {
        for (Component c : card.getComponents())
            if ("dlt".equals(c.getName()))
                return (JLabel) c;
        return new JLabel();
    }

    /* ── Mid row: Quick Access + Attendance ───────────────────────── */
    private JPanel buildMidRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 220));
        row.add(buildQuickAccess());
        row.add(buildAttendanceCard());
        return row;
    }

    private JPanel buildQuickAccess() {
        JPanel card = styledCard();
        card.setLayout(new BorderLayout(0, 8));

        JLabel title = new JLabel("Quick access");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(TEXT_MAIN);

        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(CARD_BG);
        head.setBorder(new EmptyBorder(0, 0, 10, 0));
        head.add(title, BorderLayout.WEST);
        head.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));

        String[][] mods = {
                { "\uD83D\uDC65 Students", "\uD83D\uDC64 Teachers" },
                { "\uD83D\uDCDA Courses", "\uD83D\uDCB3 Fees" },
                { "\uD83D\uDCC5 Attendance", "\uD83D\uDCCC Results" }
        };

        JPanel grid = new JPanel(new GridLayout(3, 2, 8, 8));
        grid.setBackground(CARD_BG);
        for (String[] pair : mods) {
            for (String m : pair) {
                JPanel tile = new JPanel(new BorderLayout());
                tile.setBackground(SURFACE);
                tile.setBorder(new LineBorder(BORDER, 1, true));
                tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                JLabel lbl = new JLabel(m, SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
                lbl.setForeground(TEXT_MID);
                lbl.setBorder(new EmptyBorder(10, 4, 10, 4));
                tile.add(lbl);
                tile.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        tile.setBorder(new LineBorder(ACCENT, 1, true));
                    }

                    public void mouseExited(MouseEvent e) {
                        tile.setBorder(new LineBorder(BORDER, 1, true));
                    }
                });
                grid.add(tile);
            }
        }
        card.add(head, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    /**
     * Attendance card showing 3 horizontal bars:
     * Present / Absent / Late
     * Values come from attendance.status column via SwingWorker.
     */
    private JPanel buildAttendanceCard() {
        JPanel card = styledCard();
        card.setLayout(new BorderLayout(0, 10));

        JLabel title = new JLabel("Attendance overview (live)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(TEXT_MAIN);

        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(CARD_BG);
        head.add(title, BorderLayout.WEST);
        card.add(head, BorderLayout.NORTH);

        String[] labels = { "Present", "Absent", "Late" };
        Color[] colors = { SUCCESS, DANGER, AMBER };

        JPanel bars = new JPanel();
        bars.setBackground(CARD_BG);
        bars.setLayout(new BoxLayout(bars, BoxLayout.Y_AXIS));

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            final Color barColor = colors[i];
            final String rowLabel = labels[i];

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBackground(CARD_BG);
            row.setMaximumSize(new Dimension(Short.MAX_VALUE, 24));

            JLabel lbl = new JLabel(rowLabel);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lbl.setForeground(TEXT_MID);
            lbl.setPreferredSize(new Dimension(52, 14));
            row.add(lbl, BorderLayout.WEST);

            JPanel track = new JPanel(null) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(SURFACE);
                    g2.fillRoundRect(0, 4, getWidth(), 8, 6, 6);
                    int count = (idx == 0) ? attTotalPresent
                            : (idx == 1) ? attTotalAbsent : attTotalLate;
                    int fillW = (attGrandTotal > 0)
                            ? (int) (getWidth() * count / (double) attGrandTotal)
                            : 0;
                    g2.setColor(barColor);
                    if (fillW > 0)
                        g2.fillRoundRect(0, 4, fillW, 8, 6, 6);
                    g2.dispose();
                }
            };
            track.setBackground(CARD_BG);
            barPanels[i] = track;
            row.add(track, BorderLayout.CENTER);

            JLabel pctLbl = new JLabel("…") {
                @Override
                public void paint(Graphics g) {
                    int count = (idx == 0) ? attTotalPresent
                            : (idx == 1) ? attTotalAbsent : attTotalLate;
                    int pct = (attGrandTotal > 0) ? (int) (count * 100.0 / attGrandTotal) : 0;
                    setText(pct + "%");
                    super.paint(g);
                }
            };
            pctLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            pctLbl.setForeground(TEXT_MID);
            pctLbl.setPreferredSize(new Dimension(34, 14));
            pctLbl.setHorizontalAlignment(SwingConstants.RIGHT);
            row.add(pctLbl, BorderLayout.EAST);

            bars.add(row);
            if (i < 2)
                bars.add(Box.createVerticalStrut(12));
        }

        // Total count label at the bottom
        JLabel totalRow = new JLabel("Total records: loading…") {
            @Override
            public void paint(Graphics g) {
                setText("Total records: " + attGrandTotal);
                super.paint(g);
            }
        };
        totalRow.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        totalRow.setForeground(TEXT_MUTED);
        totalRow.setBorder(new EmptyBorder(8, 0, 0, 0));

        card.add(bars, BorderLayout.CENTER);
        card.add(totalRow, BorderLayout.SOUTH);
        return card;
    }

    /* ── Activity feed ────────────────────────────────────────────── */
    private JPanel buildActivityCard() {
        activityPanel = styledCard();
        activityPanel.setLayout(new BoxLayout(activityPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Recent activity");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(TEXT_MAIN);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        activityPanel.add(title);

        JLabel loading = new JLabel("Fetching from database…");
        loading.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        loading.setForeground(TEXT_MUTED);
        loading.setAlignmentX(Component.LEFT_ALIGNMENT);
        activityPanel.add(loading);

        return activityPanel;
    }

    private JPanel activityRow(Color dotColor, String text, String date) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(CARD_BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 46));
        row.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));

        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dotColor);
                g2.fillOval(2, 6, 8, 8);
                g2.dispose();
            }
        };
        dot.setBackground(CARD_BG);
        dot.setPreferredSize(new Dimension(14, 20));
        row.add(dot, BorderLayout.WEST);

        JLabel msg = new JLabel("<html>" + text + "</html>");
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        msg.setForeground(TEXT_MAIN);

        JLabel dt = new JLabel(date);
        dt.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dt.setForeground(TEXT_MUTED);

        JPanel col = new JPanel(new GridLayout(2, 1, 0, 1));
        col.setBackground(CARD_BG);
        col.setBorder(new EmptyBorder(5, 0, 5, 0));
        col.add(msg);
        col.add(dt);
        row.add(col, BorderLayout.CENTER);

        return row;
    }

    /* ── Card helper ──────────────────────────────────────────────── */
    private JPanel styledCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)));
        return card;
    }

    /*
     * ══════════════════════════════════════════════════════════════
     * ACTIONS
     * ═══════════════════════════════════════════════════════════════
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btnStudents) {
            setActiveNav(btnStudents);
            lblPageTitle.setText("Students");
            new StudentManagement();
        } else if (src == btnTeachers) {
            setActiveNav(btnTeachers);
            lblPageTitle.setText("Teachers");
            new TeacherManagement();
        } else if (src == btnCourses) {
            setActiveNav(btnCourses);
            lblPageTitle.setText("Courses");
            new CourseManagement();
        } else if (src == btnFees) {
            setActiveNav(btnFees);
            lblPageTitle.setText("Fees");
            new FeesManagement();
        } else if (src == btnAttendance) {
            setActiveNav(btnAttendance);
            lblPageTitle.setText("Attendance");
            new AttendanceManagement();
        } else if (src == btnResults) {
            setActiveNav(btnResults);
            lblPageTitle.setText("Results");
            new ResultManagement();
        } else if (src == btnLibrary) {
            setActiveNav(btnLibrary);
            lblPageTitle.setText("Library");
            new LibraryManagement();
        } else if (src == btnProfile) {
            setActiveNav(btnProfile);
            lblPageTitle.setText("Profile");
            new Profile();
        } else if (src == btnAbout) {
            setActiveNav(btnAbout);
            lblPageTitle.setText("About");
            new About();
        } else if (src == btnLogout) {
            int c = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?",
                    "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                dispose();
                new Login();
            }
        }
    }

    /* ── Entry point ──────────────────────────────────────────────── */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(Dashboard::new);
    }
}