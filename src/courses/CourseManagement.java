package courses;

import database.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.sql.*;

public class CourseManagement extends JFrame implements ActionListener {

    JTextField tfId, tfName, tfDuration, tfFees, tfSearch;
    JButton btnAdd, btnUpdate, btnDelete, btnSearch, btnLoad;
    JTable table;
    DefaultTableModel model;

    public CourseManagement() {

        setTitle("Course Management");
        setSize(900, 600);
        setLayout(null);
        setLocationRelativeTo(null);

        tfId = new JTextField(); tfId.setBounds(130,30,200,30); add(tfId);
        tfName = new JTextField(); tfName.setBounds(130,80,200,30); add(tfName);
        tfDuration = new JTextField(); tfDuration.setBounds(130,130,200,30); add(tfDuration);
        tfFees = new JTextField(); tfFees.setBounds(130,180,200,30); add(tfFees);

        addLabel("ID",30,30);
        addLabel("Name",30,80);
        addLabel("Duration",30,130);
        addLabel("Fees",30,180);

        btnAdd = createBtn("Add",30,250);
        btnUpdate = createBtn("Update",180,250);
        btnDelete = createBtn("Delete",30,320);
        btnLoad = createBtn("Load",180,320);
        btnSearch = createBtn("Search",670,30);

        tfSearch = new JTextField();
        tfSearch.setBounds(450,30,200,30);
        add(tfSearch);

        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"ID","Name","Duration","Fees"});

        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(380,90,470,400);
        add(sp);

        loadData();

        setVisible(true);
    }

    void addLabel(String txt,int x,int y){
        JLabel l = new JLabel(txt);
        l.setBounds(x,y,100,30);
        add(l);
    }

    JButton createBtn(String t,int x,int y){
        JButton b = new JButton(t);
        b.setBounds(x,y,120,40);
        b.addActionListener(this);
        add(b);
        return b;
    }

    public void actionPerformed(ActionEvent e){

        if(e.getSource()==btnAdd){
            try{
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO courses(course_name,duration,fees) VALUES(?,?,?)"
                );
                ps.setString(1,tfName.getText());
                ps.setString(2,tfDuration.getText());
                ps.setDouble(3,Double.parseDouble(tfFees.getText()));
                ps.executeUpdate();
                loadData();
            }catch(Exception ex){ex.printStackTrace();}
        }

        if(e.getSource()==btnUpdate){
            try{
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE courses SET course_name=?,duration=?,fees=? WHERE id=?"
                );
                ps.setString(1,tfName.getText());
                ps.setString(2,tfDuration.getText());
                ps.setDouble(3,Double.parseDouble(tfFees.getText()));
                ps.setInt(4,Integer.parseInt(tfId.getText()));
                ps.executeUpdate();
                loadData();
            }catch(Exception ex){ex.printStackTrace();}
        }

        if(e.getSource()==btnDelete){
            try{
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM courses WHERE id=?"
                );
                ps.setInt(1,Integer.parseInt(tfId.getText()));
                ps.executeUpdate();
                loadData();
            }catch(Exception ex){ex.printStackTrace();}
        }

        if(e.getSource()==btnSearch){
            try{
                model.setRowCount(0);
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "SELECT * FROM courses WHERE course_name LIKE ?"
                );
                ps.setString(1,"%"+tfSearch.getText()+"%");
                ResultSet rs = ps.executeQuery();
                while(rs.next()){
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("course_name"),
                            rs.getString("duration"),
                            rs.getDouble("fees")
                    });
                }
            }catch(Exception ex){ex.printStackTrace();}
        }

        if(e.getSource()==btnLoad){
            loadData();
        }
    }

    void loadData(){
        try{
            model.setRowCount(0);
            Connection con = DBConnection.getConnection();
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM courses");
            while(rs.next()){
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("course_name"),
                        rs.getString("duration"),
                        rs.getDouble("fees")
                });
            }
        }catch(Exception e){e.printStackTrace();}
    }
}