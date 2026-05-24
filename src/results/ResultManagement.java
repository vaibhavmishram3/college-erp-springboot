package results;

import database.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ResultManagement extends JFrame implements ActionListener {

    private JTextField tfId, tfName, tfSubject, tfMarks, tfSearch;
    private JButton btnAdd, btnUpdate, btnDelete, btnSearch, btnLoad;
    private JTable table;
    private DefaultTableModel model;

    public ResultManagement() {

        setTitle("Result Management");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildFormPanel(), BorderLayout.WEST);
        add(buildTablePanel(), BorderLayout.CENTER);

        loadData();
        setVisible(true);
    }

    /* ================= UI ================= */

    private JPanel buildFormPanel() {

        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(350, 600));
        p.setLayout(null);

        addLabel(p, "ID", 30, 30);
        addLabel(p, "Student Name", 30, 90);
        addLabel(p, "Subject", 30, 150);
        addLabel(p, "Marks", 30, 210);

        tfId = addField(p, 150, 30);
        tfName = addField(p, 150, 90);
        tfSubject = addField(p, 150, 150);
        tfMarks = addField(p, 150, 210);

        btnAdd = addButton(p, "Add", 30, 280);
        btnUpdate = addButton(p, "Update", 150, 280);
        btnDelete = addButton(p, "Delete", 30, 340);
        btnLoad = addButton(p, "Load", 150, 340);

        tfSearch = addField(p, 150, 420);
        btnSearch = addButton(p, "Search", 150, 460);

        return p;
    }

    private JPanel buildTablePanel() {

        JPanel p = new JPanel(new BorderLayout());

        model = new DefaultTableModel(
                new String[]{"ID", "Student", "Subject", "Marks"}, 0
        );

        table = new JTable(model);
        table.setRowHeight(22);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int r = table.getSelectedRow();
                tfId.setText(model.getValueAt(r, 0).toString());
                tfName.setText(model.getValueAt(r, 1).toString());
                tfSubject.setText(model.getValueAt(r, 2).toString());
                tfMarks.setText(model.getValueAt(r, 3).toString());
            }
        });

        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    /* ================= Helpers ================= */

    private JTextField addField(JPanel p, int x, int y) {
        JTextField tf = new JTextField();
        tf.setBounds(x, y, 160, 30);
        p.add(tf);
        return tf;
    }

    private JButton addButton(JPanel p, String text, int x, int y) {
        JButton b = new JButton(text);
        b.setBounds(x, y, 120, 35);
        b.addActionListener(this);
        p.add(b);
        return b;
    }

    private void addLabel(JPanel p, String text, int x, int y) {
        JLabel l = new JLabel(text);
        l.setBounds(x, y, 120, 30);
        p.add(l);
    }

    /* ================= CRUD ================= */

    private void loadData() {

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM results")) {

            model.setRowCount(0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("student_name"),
                        rs.getString("subject"),
                        rs.getInt("marks")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addResult() {
        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO results(student_name,subject,marks) VALUES(?,?,?)"
            );

            ps.setString(1, tfName.getText());
            ps.setString(2, tfSubject.getText());
            ps.setInt(3, Integer.parseInt(tfMarks.getText()));

            ps.executeUpdate();
            loadData();

        } catch (Exception e) {
            showError(e);
        }
    }

    private void updateResult() {
        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE results SET student_name=?,subject=?,marks=? WHERE id=?"
            );

            ps.setString(1, tfName.getText());
            ps.setString(2, tfSubject.getText());
            ps.setInt(3, Integer.parseInt(tfMarks.getText()));
            ps.setInt(4, Integer.parseInt(tfId.getText()));

            ps.executeUpdate();
            loadData();

        } catch (Exception e) {
            showError(e);
        }
    }

    private void deleteResult() {
        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM results WHERE id=?"
            );

            ps.setInt(1, Integer.parseInt(tfId.getText()));

            ps.executeUpdate();
            loadData();

        } catch (Exception e) {
            showError(e);
        }
    }

    private void searchResult() {

        try (Connection con = DBConnection.getConnection()) {

            model.setRowCount(0);

            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM results WHERE student_name LIKE ?"
            );

            ps.setString(1, "%" + tfSearch.getText() + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("student_name"),
                        rs.getString("subject"),
                        rs.getInt("marks")
                });
            }

        } catch (Exception e) {
            showError(e);
        }
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
    }

    /* ================= Events ================= */

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == btnAdd) addResult();
        if (e.getSource() == btnUpdate) updateResult();
        if (e.getSource() == btnDelete) deleteResult();
        if (e.getSource() == btnLoad) loadData();
        if (e.getSource() == btnSearch) searchResult();
    }

    public static void main(String[] args) {
        new ResultManagement();
    }
}