package fees;

import database.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class FeesManagement extends JFrame implements ActionListener {

    // ── Fields ─────────────────────────────
    JTextField tfId, tfName, tfAmount, tfSearch;
    JButton add, update, delete, search, load;
    JTable table;
    DefaultTableModel model;

    public FeesManagement() {

        setTitle("Fees Management");
        setSize(900, 600);
        setLayout(null);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ── Labels ─────────────────────────
        addLabel("ID", 30, 30);
        addLabel("Student", 30, 80);
        addLabel("Amount", 30, 130);

        // ── Fields ─────────────────────────
        tfId = createField(130, 30);
        tfName = createField(130, 80);
        tfAmount = createField(130, 130);

        // ── Buttons ────────────────────────
        add = createButton("Add", 30, 220);
        update = createButton("Update", 160, 220);
        delete = createButton("Delete", 290, 220);
        load = createButton("Load", 420, 220);

        // ── Search ─────────────────────────
        tfSearch = createField(450, 30);
        search = createButton("Search", 670, 30);

        // ── Table ──────────────────────────
        model = new DefaultTableModel(
                new String[]{"ID", "Student", "Amount"}, 0
        );

        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(380, 90, 470, 400);
        add(sp);

        // row click fill
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int r = table.getSelectedRow();
                tfId.setText(model.getValueAt(r, 0).toString());
                tfName.setText(model.getValueAt(r, 1).toString());
                tfAmount.setText(model.getValueAt(r, 2).toString());
            }
        });

        loadData();
        setVisible(true);
    }

    // ── UI HELPERS ─────────────────────────

    JTextField createField(int x, int y) {
        JTextField tf = new JTextField();
        tf.setBounds(x, y, 200, 30);
        add(tf);
        return tf;
    }

    JButton createButton(String text, int x, int y) {
        JButton b = new JButton(text);
        b.setBounds(x, y, 120, 40);
        b.addActionListener(this);
        b.setFocusPainted(false);
        add(b);
        return b;
    }

    void addLabel(String text, int x, int y) {
        JLabel l = new JLabel(text);
        l.setBounds(x, y, 100, 30);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        add(l);
    }

    // ── ACTION ─────────────────────────────
    @Override
    public void actionPerformed(ActionEvent e) {

        try (Connection con = DBConnection.getConnection()) {

            if (e.getSource() == add) {
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO fees(student_name,amount) VALUES(?,?)"
                );
                ps.setString(1, tfName.getText());
                ps.setDouble(2, Double.parseDouble(tfAmount.getText()));
                ps.executeUpdate();
            }

            else if (e.getSource() == update) {
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE fees SET student_name=?,amount=? WHERE id=?"
                );
                ps.setString(1, tfName.getText());
                ps.setDouble(2, Double.parseDouble(tfAmount.getText()));
                ps.setInt(3, Integer.parseInt(tfId.getText()));
                ps.executeUpdate();
            }

            else if (e.getSource() == delete) {
                PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM fees WHERE id=?"
                );
                ps.setInt(1, Integer.parseInt(tfId.getText()));
                ps.executeUpdate();
            }

            else if (e.getSource() == search) {
                searchData(con);
                return;
            }

            loadData();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ── SEARCH ─────────────────────────────
    void searchData(Connection con) throws Exception {

        model.setRowCount(0);

        PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM fees WHERE student_name LIKE ?"
        );

        ps.setString(1, "%" + tfSearch.getText() + "%");

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("student_name"),
                    rs.getDouble("amount")
            });
        }
    }

    // ── LOAD ───────────────────────────────
    void loadData() {

        try (Connection con = DBConnection.getConnection()) {

            model.setRowCount(0);

            ResultSet rs = con.createStatement()
                    .executeQuery("SELECT * FROM fees");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("student_name"),
                        rs.getDouble("amount")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}