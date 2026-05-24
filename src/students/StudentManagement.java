package students;

import database.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class StudentManagement extends JFrame implements ActionListener {

    JTextField tfId, tfName, tfCourse, tfPhone, tfSearch;
    JButton btnAdd, btnUpdate, btnDelete, btnSearch, btnLoad;
    JTable table;
    DefaultTableModel model;

    public StudentManagement() {

        setTitle("Student Management System");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());

        add(buildFormPanel(), BorderLayout.WEST);
        add(buildTablePanel(), BorderLayout.CENTER);

        loadStudents();

        setVisible(true);
    }

    // ───────── FORM PANEL ─────────
    private JPanel buildFormPanel() {

        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(350, 600));
        p.setLayout(null);

        JLabel title = new JLabel("Student Entry");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setBounds(20, 10, 200, 30);
        p.add(title);

        addLabel(p, "ID", 20, 60);
        addLabel(p, "Name", 20, 110);
        addLabel(p, "Course", 20, 160);
        addLabel(p, "Phone", 20, 210);

        tfId = addField(p, 120, 60);
        tfName = addField(p, 120, 110);
        tfCourse = addField(p, 120, 160);
        tfPhone = addField(p, 120, 210);

        btnAdd = addButton(p, "Add", 20, 270);
        btnUpdate = addButton(p, "Update", 120, 270);
        btnDelete = addButton(p, "Delete", 220, 270);

        btnSearch = addButton(p, "Search", 20, 340);
        btnLoad = addButton(p, "Load All", 120, 340);

        tfSearch = new JTextField();
        tfSearch.setBounds(20, 390, 300, 30);
        p.add(tfSearch);

        btnAdd.addActionListener(this);
        btnUpdate.addActionListener(this);
        btnDelete.addActionListener(this);
        btnSearch.addActionListener(this);
        btnLoad.addActionListener(this);

        return p;
    }

    // ───────── TABLE PANEL ─────────
    private JPanel buildTablePanel() {

        JPanel p = new JPanel(new BorderLayout());

        model = new DefaultTableModel(new String[]{
                "ID", "Name", "Course", "Phone"
        }, 0);

        table = new JTable(model);
        table.setRowHeight(25);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    tfId.setText(model.getValueAt(row, 0).toString());
                    tfName.setText(model.getValueAt(row, 1).toString());
                    tfCourse.setText(model.getValueAt(row, 2).toString());
                    tfPhone.setText(model.getValueAt(row, 3).toString());
                }
            }
        });

        p.add(new JScrollPane(table), BorderLayout.CENTER);

        return p;
    }

    // ───────── HELPERS ─────────
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

    // ───────── ACTIONS ─────────
    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == btnAdd) addStudent();
        else if (e.getSource() == btnUpdate) updateStudent();
        else if (e.getSource() == btnDelete) deleteStudent();
        else if (e.getSource() == btnSearch) searchStudent();
        else if (e.getSource() == btnLoad) loadStudents();
    }

    // ───────── DB OPERATIONS ─────────
    private void loadStudents() {

        model.setRowCount(0);

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM students")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("course"),
                        rs.getString("phone")
                });
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addStudent() {

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO students(name, course, phone) VALUES(?,?,?)");

            ps.setString(1, tfName.getText());
            ps.setString(2, tfCourse.getText());
            ps.setString(3, tfPhone.getText());

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Student Added!");
            clearFields();
            loadStudents();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateStudent() {

        if (tfId.getText().isEmpty()) return;

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE students SET name=?, course=?, phone=? WHERE id=?");

            ps.setString(1, tfName.getText());
            ps.setString(2, tfCourse.getText());
            ps.setString(3, tfPhone.getText());
            ps.setInt(4, Integer.parseInt(tfId.getText()));

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Student Updated!");
            clearFields();
            loadStudents();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deleteStudent() {

        if (tfId.getText().isEmpty()) return;

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM students WHERE id=?");

            ps.setInt(1, Integer.parseInt(tfId.getText()));

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Student Deleted!");
            clearFields();
            loadStudents();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void searchStudent() {

        model.setRowCount(0);

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM students WHERE name LIKE ?");

            ps.setString(1, "%" + tfSearch.getText() + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("course"),
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
        tfCourse.setText("");
        tfPhone.setText("");
    }

    public static void main(String[] args) {
        new StudentManagement();
    }
}