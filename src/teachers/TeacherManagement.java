package teachers;

import database.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class TeacherManagement extends JFrame implements ActionListener {

    // ───────── UI COMPONENTS ─────────
    JTextField tfId, tfName, tfSubject, tfPhone, tfSearch;
    JButton addBtn, updateBtn, deleteBtn, searchBtn, loadBtn;
    JTable table;
    DefaultTableModel model;

    public TeacherManagement() {

        setTitle("Teacher Management");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());

        add(buildFormPanel(), BorderLayout.WEST);
        add(buildTablePanel(), BorderLayout.CENTER);

        loadData();

        setVisible(true);
    }

    // ───────── FORM PANEL ─────────
    private JPanel buildFormPanel() {

        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(350, 600));
        p.setLayout(null);

        JLabel title = new JLabel("Teacher Entry");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setBounds(20, 10, 200, 30);
        p.add(title);

        addLabel(p, "ID", 20, 60);
        addLabel(p, "Name", 20, 110);
        addLabel(p, "Subject", 20, 160);
        addLabel(p, "Phone", 20, 210);

        tfId = addField(p, 120, 60);
        tfName = addField(p, 120, 110);
        tfSubject = addField(p, 120, 160);
        tfPhone = addField(p, 120, 210);

        addBtn = addButton(p, "Add", 20, 270);
        updateBtn = addButton(p, "Update", 120, 270);
        deleteBtn = addButton(p, "Delete", 220, 270);

        addBtn.addActionListener(this);
        updateBtn.addActionListener(this);
        deleteBtn.addActionListener(this);

        addBtn.setBackground(new Color(76, 175, 80));
        updateBtn.setBackground(new Color(33, 150, 243));
        deleteBtn.setBackground(new Color(244, 67, 54));
        addBtn.setForeground(Color.WHITE);
        updateBtn.setForeground(Color.WHITE);
        deleteBtn.setForeground(Color.WHITE);

        // SEARCH
        tfSearch = new JTextField();
        tfSearch.setBounds(20, 340, 200, 30);
        p.add(tfSearch);

        searchBtn = new JButton("Search");
        searchBtn.setBounds(230, 340, 90, 30);
        searchBtn.addActionListener(this);
        p.add(searchBtn);

        loadBtn = new JButton("Load All");
        loadBtn.setBounds(20, 390, 300, 35);
        loadBtn.addActionListener(this);
        p.add(loadBtn);

        return p;
    }

    // ───────── TABLE PANEL ─────────
    private JPanel buildTablePanel() {

        JPanel p = new JPanel(new BorderLayout());

        model = new DefaultTableModel(new String[]{
                "ID", "Name", "Subject", "Phone"
        }, 0);

        table = new JTable(model);
        table.setRowHeight(25);

        JScrollPane sp = new JScrollPane(table);
        p.add(sp, BorderLayout.CENTER);

        return p;
    }

    // ───────── UI HELPERS ─────────
    private void addLabel(JPanel p, String text, int x, int y) {
        JLabel l = new JLabel(text);
        l.setBounds(x, y, 100, 25);
        p.add(l);
    }

    private JTextField addField(JPanel p, int x, int y) {
        JTextField tf = new JTextField();
        tf.setBounds(x, y, 200, 30);
        p.add(tf);
        return tf;
    }

    private JButton addButton(JPanel p, String text, int x, int y) {
        JButton b = new JButton(text);
        b.setBounds(x, y, 90, 30);
        p.add(b);
        return b;
    }

    // ───────── ACTION HANDLER ─────────
    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == addBtn) {
            insertTeacher();
        }
        else if (e.getSource() == updateBtn) {
            updateTeacher();
        }
        else if (e.getSource() == deleteBtn) {
            deleteTeacher();
        }
        else if (e.getSource() == searchBtn) {
            searchTeacher();
        }
        else if (e.getSource() == loadBtn) {
            loadData();
        }
    }

    // ───────── DB OPERATIONS ─────────

    private void insertTeacher() {
        if (tfName.getText().isEmpty()) return;

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO teachers(name, subject, phone) VALUES (?, ?, ?)");

            ps.setString(1, tfName.getText());
            ps.setString(2, tfSubject.getText());
            ps.setString(3, tfPhone.getText());

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Teacher Added!");
            clearFields();
            loadData();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateTeacher() {
        if (tfId.getText().isEmpty()) return;

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE teachers SET name=?, subject=?, phone=? WHERE id=?");

            ps.setString(1, tfName.getText());
            ps.setString(2, tfSubject.getText());
            ps.setString(3, tfPhone.getText());
            ps.setInt(4, Integer.parseInt(tfId.getText()));

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Updated!");
            clearFields();
            loadData();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deleteTeacher() {
        if (tfId.getText().isEmpty()) return;

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM teachers WHERE id=?");

            ps.setInt(1, Integer.parseInt(tfId.getText()));

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Deleted!");
            clearFields();
            loadData();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void searchTeacher() {

        model.setRowCount(0);

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM teachers WHERE name LIKE ?");

            ps.setString(1, "%" + tfSearch.getText() + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("subject"),
                        rs.getString("phone")
                });
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadData() {

        model.setRowCount(0);

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM teachers")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("subject"),
                        rs.getString("phone")
                });
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void clearFields() {
        tfId.setText("");
        tfName.setText("");
        tfSubject.setText("");
        tfPhone.setText("");
    }
}