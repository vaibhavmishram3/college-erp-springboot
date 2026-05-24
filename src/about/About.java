package about;

import javax.swing.*;
import java.awt.*;

public class About extends JFrame {

    public About() {

        setTitle("About - College Management System");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(new Color(245, 248, 250));

        // ===== TITLE =====
        JLabel title = new JLabel("COLLEGE MANAGEMENT SYSTEM");
        title.setBounds(120, 20, 500, 40);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(new Color(0, 51, 102));
        add(title);

        // ===== SUBTITLE =====
        JLabel subtitle = new JLabel("Java AWT & Swing Project with MySQL Database");
        subtitle.setBounds(170, 60, 400, 25);
        subtitle.setFont(new Font("Tahoma", Font.PLAIN, 16));
        subtitle.setForeground(Color.DARK_GRAY);
        add(subtitle);

        // ===== DESCRIPTION AREA =====
        JTextArea aboutText = new JTextArea();

        aboutText.setText(
                "This College Management System is developed using Java Swing,\n"
              + "AWT and MySQL Database.\n\n"

              + "The purpose of this project is to automate the management\n"
              + "of college activities and reduce manual paperwork.\n\n"

              + "MODULES INCLUDED:\n"
              + "-----------------------------------------\n"
              + "• Student Management\n"
              + "• Teacher Management\n"
              + "• Course Management\n"
              + "• Attendance Management\n"
              + "• Fees Management\n"
              + "• Result Management\n"
              + "• Library Management\n"
              + "• Login & Signup System\n"
              + "• Admin Dashboard\n\n"

              + "TECHNOLOGIES USED:\n"
              + "-----------------------------------------\n"
              + "• Java\n"
              + "• Swing & AWT\n"
              + "• JDBC\n"
              + "• MySQL Database\n"
              + "• VS Code / Eclipse\n\n"

              + "FEATURES:\n"
              + "-----------------------------------------\n"
              + "✔ User Friendly Interface\n"
              + "✔ Fast Data Management\n"
              + "✔ Secure Login System\n"
              + "✔ CRUD Operations\n"
              + "✔ Professional Dashboard\n"
              + "✔ Database Connectivity\n\n"

              + "Developed For Educational Purpose."
        );

        aboutText.setBounds(40, 110, 600, 260);
        aboutText.setFont(new Font("Monospaced", Font.PLAIN, 15));
        aboutText.setEditable(false);
        aboutText.setBackground(new Color(245, 248, 250));
        add(aboutText);

        // ===== FOOTER =====
        JLabel footer = new JLabel("Developed by Vaibhav Mishra");
        footer.setBounds(240, 400, 250, 30);
        footer.setFont(new Font("Arial", Font.BOLD, 16));
        footer.setForeground(new Color(0, 102, 204));
        add(footer);

        // ===== CLOSE BUTTON =====
        JButton closeBtn = new JButton("Close");
        closeBtn.setBounds(280, 435, 120, 35);
        closeBtn.setBackground(new Color(0, 102, 204));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);

        closeBtn.addActionListener(e -> dispose());

        add(closeBtn);

        setVisible(true);
    }

    public static void main(String[] args) {
        new About();
    }
}