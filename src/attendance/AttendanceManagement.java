package attendance;

import database.DBConnection;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AttendanceManagement extends JFrame implements ActionListener {

    /* ── Palette ─────────────────────────────────────────────────── */
    private static final Color BG          = new Color(245, 246, 250);
    private static final Color CARD        = new Color(255, 255, 255);
    private static final Color BORDER_COL  = new Color(220, 222, 228);
    private static final Color TEXT_MAIN   = new Color(20,  20,  30);
    private static final Color TEXT_MID    = new Color(80,  85,  105);
    private static final Color TEXT_MUTED  = new Color(140, 145, 165);
    private static final Color ACCENT      = new Color(55,  138, 221);
    private static final Color ACCENT_DARK = new Color(24,  95,  165);
    private static final Color SUCCESS     = new Color(99,  153, 34);
    private static final Color SUCCESS_BG  = new Color(234, 243, 222);
    private static final Color DANGER      = new Color(226, 75,  74);
    private static final Color DANGER_BG   = new Color(255, 235, 235);
    private static final Color AMBER       = new Color(186, 117, 23);
    private static final Color AMBER_BG    = new Color(250, 238, 218);
    @SuppressWarnings("unused")
    private static final Color ROW_HOVER   = new Color(245, 247, 252);
    private static final Color ROW_SELECT  = new Color(230, 241, 251);
    private static final Color SURFACE     = new Color(246, 247, 250);

    /* ── Form fields ─────────────────────────────────────────────── */
    private JTextField  tfId, tfName, tfDate, tfSearch;
    private JComboBox<String> cbStatus;

    /* ── Buttons ─────────────────────────────────────────────────── */
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnLoad;

    /* ── Table ───────────────────────────────────────────────────── */
    private JTable table;
    private DefaultTableModel model;
    private JLabel lblCount;

    /* ══════════════════════════════════════════════════════════════
       CONSTRUCTOR
    ═══════════════════════════════════════════════════════════════ */
    public AttendanceManagement() {
        setTitle("Attendance Management");
        setSize(920, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        root.add(buildTopBar(),   BorderLayout.NORTH);
        root.add(buildBody(),     BorderLayout.CENTER);

        loadData();
        setVisible(true);
    }

    /* ── Top bar ──────────────────────────────────────────────────── */
    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_COL),
            new EmptyBorder(12, 20, 12, 20)));

        JLabel title = new JLabel("Attendance Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_MAIN);
        p.add(title, BorderLayout.WEST);

        lblCount = new JLabel("0 records");
        lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCount.setForeground(TEXT_MUTED);
        lblCount.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COL, 1, true),
            new EmptyBorder(3, 10, 3, 10)));
        lblCount.setBackground(SURFACE);
        lblCount.setOpaque(true);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setBackground(Color.WHITE);
        right.add(lblCount);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    /* ── Body: form card (left) + table card (right) ──────────────── */
    private JPanel buildBody() {
        JPanel body = new JPanel(new GridLayout(1, 2, 12, 0));
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(16, 16, 16, 16));
        body.add(buildFormCard());
        body.add(buildTableCard());
        return body;
    }

    /* ── Form card ────────────────────────────────────────────────── */
    private JPanel buildFormCard() {
        JPanel card = card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(sectionLabel("Record Details"));
        card.add(Box.createVerticalStrut(10));

        // ID (read-only)
        card.add(fieldLabel("Record ID"));
        tfId = field("Auto — filled on row selection");
        tfId.setEditable(false);
        tfId.setForeground(TEXT_MUTED);
        tfId.setBackground(SURFACE);
        card.add(tfId);
        card.add(Box.createVerticalStrut(10));

        // Name
        card.add(fieldLabel("Student Name"));
        tfName = field("e.g. Priya Sharma");
        card.add(tfName);
        card.add(Box.createVerticalStrut(10));

        // Date
        card.add(fieldLabel("Date (YYYY-MM-DD)"));
        tfDate = field("e.g. 2025-05-24");
        card.add(tfDate);
        card.add(Box.createVerticalStrut(10));

        // Status dropdown
        card.add(fieldLabel("Status"));
        cbStatus = new JComboBox<>(new String[]{"", "Present", "Absent", "Late"});
        styleCombo(cbStatus);
        card.add(cbStatus);
        card.add(Box.createVerticalStrut(20));

        // Divider
        card.add(divider());
        card.add(Box.createVerticalStrut(14));

        // Action buttons
        btnAdd    = actionBtn("Add",    ACCENT,      Color.WHITE);
        btnUpdate = actionBtn("Update", SURFACE,     TEXT_MAIN);
        btnDelete = actionBtn("Delete", DANGER_BG,   DANGER);
        btnClear  = actionBtn("Clear",  SURFACE,     TEXT_MID);

        // Style borders
        btnUpdate.setBorder(new CompoundBorder(new LineBorder(BORDER_COL, 1, true), new EmptyBorder(7, 14, 7, 14)));
        btnDelete.setBorder(new CompoundBorder(new LineBorder(DANGER,     1, true), new EmptyBorder(7, 14, 7, 14)));
        btnClear.setBorder (new CompoundBorder(new LineBorder(BORDER_COL, 1, true), new EmptyBorder(7, 14, 7, 14)));

        JPanel btnRow1 = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow1.setBackground(CARD);
        btnRow1.setMaximumSize(new Dimension(Short.MAX_VALUE, 38));
        btnRow1.add(btnAdd);
        btnRow1.add(btnUpdate);
        card.add(btnRow1);
        card.add(Box.createVerticalStrut(8));

        JPanel btnRow2 = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow2.setBackground(CARD);
        btnRow2.setMaximumSize(new Dimension(Short.MAX_VALUE, 38));
        btnRow2.add(btnDelete);
        btnRow2.add(btnClear);
        card.add(btnRow2);

        card.add(Box.createVerticalGlue());
        return card;
    }

    /* ── Table card ───────────────────────────────────────────────── */
    private JPanel buildTableCard() {
        JPanel card = card();
        card.setLayout(new BorderLayout(0, 10));

        // Search bar
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setBackground(CARD);

        tfSearch = new JTextField();
        tfSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tfSearch.setForeground(TEXT_MAIN);
        tfSearch.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COL, 1, true),
            new EmptyBorder(5, 10, 5, 10)));
        tfSearch.setPreferredSize(new Dimension(0, 34));
        // Live filter on type
        tfSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { filterTable(); }
        });

        btnLoad = new JButton("Load All");
        btnLoad.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLoad.setForeground(TEXT_MID);
        btnLoad.setBackground(SURFACE);
        btnLoad.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COL, 1, true),
            new EmptyBorder(5, 12, 5, 12)));
        btnLoad.setFocusPainted(false);
        btnLoad.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLoad.addActionListener(this);

        searchRow.add(tfSearch,  BorderLayout.CENTER);
        searchRow.add(btnLoad,   BorderLayout.EAST);

        // Table
        model = new DefaultTableModel(
            new String[]{"ID", "Student Name", "Date", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(ROW_SELECT);
                    c.setForeground(TEXT_MAIN);
                } else {
                    c.setBackground(row % 2 == 0 ? CARD : SURFACE);
                    c.setForeground(TEXT_MAIN);
                }
                return c;
            }
        };

        styleTable();

        // Status column custom renderer (colored pills)
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = new JLabel(val == null ? "" : val.toString());
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lbl.setBorder(new EmptyBorder(3, 8, 3, 8));
                lbl.setOpaque(true);
                String s = val == null ? "" : val.toString().toLowerCase();
                switch (s) {
                    case "present" -> { lbl.setBackground(SUCCESS_BG); lbl.setForeground(SUCCESS); }
                    case "absent"  -> { lbl.setBackground(DANGER_BG);  lbl.setForeground(DANGER);  }
                    case "late"    -> { lbl.setBackground(AMBER_BG);   lbl.setForeground(AMBER);   }
                    default        -> { lbl.setBackground(SURFACE);    lbl.setForeground(TEXT_MUTED); }
                }
                if (sel) { lbl.setBorder(new CompoundBorder(new LineBorder(BORDER_COL, 1, true), new EmptyBorder(2, 7, 2, 7))); }
                return lbl;
            }
        });

        // Row click → fill form
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { fillFormFromSelectedRow(); }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(BORDER_COL, 1));
        sp.getViewport().setBackground(CARD);

        card.add(searchRow,  BorderLayout.NORTH);
        card.add(sp,         BorderLayout.CENTER);
        return card;
    }

    /* ══════════════════════════════════════════════════════════════
       TABLE STYLING
    ═══════════════════════════════════════════════════════════════ */
    private void styleTable() {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(ROW_SELECT);
        table.setSelectionForeground(TEXT_MAIN);
        table.setFocusable(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBackground(SURFACE);
        header.setForeground(TEXT_MID);
        header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COL));
        header.setPreferredSize(new Dimension(0, 34));
        header.setReorderingAllowed(false);

        // Column widths
        int[] widths = {45, 180, 120, 100};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Center ID column
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
    }

    /* ══════════════════════════════════════════════════════════════
       ACTIONS
    ═══════════════════════════════════════════════════════════════ */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == btnAdd)    doAdd();
        if (src == btnUpdate) doUpdate();
        if (src == btnDelete) doDelete();
        if (src == btnClear)  clearForm();
        if (src == btnLoad)   { tfSearch.setText(""); loadData(); }
    }

    private void doAdd() {
        String name   = tfName.getText().trim();
        String date   = tfDate.getText().trim();
        String status = (String) cbStatus.getSelectedItem();

        if (name.isEmpty() || date.isEmpty() || status == null || status.isEmpty()) {
            showMsg("Please fill in Student Name, Date, and Status.", "Validation", false);
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "INSERT INTO attendance(student_name, attendance_date, status) VALUES(?,?,?)")) {

            ps.setString(1, name);
            ps.setString(2, date);
            ps.setString(3, status);
            ps.executeUpdate();

            loadData();
            clearForm();
            showMsg("Attendance record added successfully.", "Success", true);

        } catch (SQLException ex) {
            showSQLError(ex);
        }
    }

    private void doUpdate() {
        String idStr = tfId.getText().trim();
        if (idStr.isEmpty()) {
            showMsg("Select a row from the table to update.", "Select a Row", false);
            return;
        }

        String name   = tfName.getText().trim();
        String date   = tfDate.getText().trim();
        String status = (String) cbStatus.getSelectedItem();

        if (name.isEmpty() || date.isEmpty() || status == null || status.isEmpty()) {
            showMsg("Please fill in all fields before updating.", "Validation", false);
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "UPDATE attendance SET student_name=?, attendance_date=?, status=? WHERE id=?")) {

            ps.setString(1, name);
            ps.setString(2, date);
            ps.setString(3, status);
            ps.setInt(4, Integer.parseInt(idStr));
            ps.executeUpdate();

            loadData();
            showMsg("Attendance record updated.", "Updated", true);

        } catch (SQLException ex) {
            showSQLError(ex);
        }
    }

    private void doDelete() {
        String idStr = tfId.getText().trim();
        if (idStr.isEmpty()) {
            showMsg("Select a row from the table to delete.", "Select a Row", false);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete attendance record ID " + idStr + "?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "DELETE FROM attendance WHERE id=?")) {

            ps.setInt(1, Integer.parseInt(idStr));
            ps.executeUpdate();

            loadData();
            clearForm();
            showMsg("Attendance record deleted.", "Deleted", true);

        } catch (SQLException ex) {
            showSQLError(ex);
        }
    }

    /* ══════════════════════════════════════════════════════════════
       DATA
    ═══════════════════════════════════════════════════════════════ */
    private void loadData() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection();
             ResultSet rs = con.createStatement().executeQuery(
                 "SELECT * FROM attendance ORDER BY id DESC")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("student_name"),
                    rs.getString("attendance_date"),
                    rs.getString("status")
                });
            }
            updateCount();

        } catch (SQLException ex) {
            showSQLError(ex);
        }
    }

    /** Live filter — searches student_name in DB */
    private void filterTable() {
        String q = tfSearch.getText().trim();
        if (q.isEmpty()) { loadData(); return; }

        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT * FROM attendance WHERE student_name LIKE ? ORDER BY id DESC")) {

            ps.setString(1, "%" + q + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("student_name"),
                    rs.getString("attendance_date"),
                    rs.getString("status")
                });
            }
            updateCount();

        } catch (SQLException ex) {
            showSQLError(ex);
        }
    }

    private void fillFormFromSelectedRow() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        tfId.setText(model.getValueAt(row, 0).toString());
        tfName.setText(model.getValueAt(row, 1).toString());
        tfDate.setText(model.getValueAt(row, 2).toString());
        String s = model.getValueAt(row, 3) == null ? "" : model.getValueAt(row, 3).toString();
        cbStatus.setSelectedItem(s);
    }

    private void clearForm() {
        tfId.setText("");
        tfName.setText("");
        tfDate.setText("");
        cbStatus.setSelectedIndex(0);
        table.clearSelection();
    }

    private void updateCount() {
        int n = model.getRowCount();
        lblCount.setText(n + " record" + (n != 1 ? "s" : ""));
    }

    /* ══════════════════════════════════════════════════════════════
       HELPERS
    ═══════════════════════════════════════════════════════════════ */
    private JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD);
        p.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COL, 1, true),
            new EmptyBorder(16, 18, 16, 18)));
        return p;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Short.MAX_VALUE, 18));
        return l;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_MID);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Short.MAX_VALUE, 16));
        return l;
    }

    private JTextField field(String placeholder) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    g2.drawString(placeholder, 10, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(TEXT_MAIN);
        tf.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COL, 1, true),
            new EmptyBorder(6, 10, 6, 10)));
        tf.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tf;
    }

    private void styleCombo(JComboBox<String> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBackground(CARD);
        cb.setForeground(TEXT_MAIN);
        cb.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COL, 1, true),
            new EmptyBorder(2, 6, 2, 6)));
        cb.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
        cb.setFocusable(false);
    }

    private JButton actionBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);

        if (text.equals("Add")) {
            b.setBorder(new CompoundBorder(
                new LineBorder(ACCENT, 1, true),
                new EmptyBorder(7, 14, 7, 14)));
            b.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { b.setBackground(ACCENT_DARK); }
                public void mouseExited (MouseEvent e) { b.setBackground(ACCENT); }
            });
        } else {
            b.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { b.setBackground(b.getBackground().darker()); }
                public void mouseExited (MouseEvent e) {
                    if (text.equals("Delete"))      b.setBackground(DANGER_BG);
                    else if (text.equals("Update")) b.setBackground(SURFACE);
                    else                            b.setBackground(SURFACE);
                }
            });
        }

        b.addActionListener(this);
        return b;
    }

    private JSeparator divider() {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
        sep.setForeground(BORDER_COL);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    private void showMsg(String msg, String title, boolean success) {
        JOptionPane.showMessageDialog(this, msg, title,
            success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }

    private void showSQLError(SQLException ex) {
        JOptionPane.showMessageDialog(this,
            "Database error:\n" + ex.getMessage(),
            "SQL Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(AttendanceManagement::new);
    }
}