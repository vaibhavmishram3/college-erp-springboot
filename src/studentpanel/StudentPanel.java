package studentpanel;

import javax.swing.*;
import java.awt.*;
import auth.Login;

public class StudentPanel extends JFrame {

    public StudentPanel(String username) {

        setTitle("Student Panel");
        setSize(600, 400);
        setLayout(null);
        setLocationRelativeTo(null);

        JLabel lbl = new JLabel("Welcome Student: " + username);
        lbl.setFont(new Font("Arial", Font.BOLD, 18));
        lbl.setBounds(150, 50, 400, 30);
        add(lbl);

        JLabel info = new JLabel("You can only view your data here.");
        info.setBounds(150, 100, 400, 30);
        add(info);

        JButton logout = new JButton("Logout");
        logout.setBounds(220, 200, 120, 40);

        logout.addActionListener(e -> {
            dispose();
            new Login();
        });

        add(logout);

        setVisible(true);
    }
}