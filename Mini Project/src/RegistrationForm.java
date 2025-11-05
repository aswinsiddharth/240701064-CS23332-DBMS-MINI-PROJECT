import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class RegistrationForm extends JFrame {

    private JTextField tfUsername, tfFullName, tfEmail, tfRollNo;
    private JButton btnRegister, btnBackToLogin;
    private LoginForm loginForm; // Reference to return to login

    // Constructor with LoginForm reference
    public RegistrationForm(LoginForm loginForm) {
        this.loginForm = loginForm;
        initComponents();
    }

    // Constructor without LoginForm (standalone mode)
    public RegistrationForm() {
        this.loginForm = null;
        initComponents();
    }

    private void initComponents() {
        setTitle("Student Registration");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen mode
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        JLabel lblTitle = new JLabel("Create New Account", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        titlePanel.add(lblTitle);
        add(titlePanel, BorderLayout.NORTH);

        // Center container for the form
        JPanel centerContainer = new JPanel(new GridBagLayout());
        
        // Main form panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        // Roll Number
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        mainPanel.add(new JLabel("Roll Number:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        tfRollNo = new JTextField(20);
        tfRollNo.setEditable(false);
        tfRollNo.setBackground(new Color(240, 240, 240));
        mainPanel.add(tfRollNo, gbc);

        // Username
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        mainPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        tfUsername = new JTextField(20);
        mainPanel.add(tfUsername, gbc);

        // Full Name
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        mainPanel.add(new JLabel("Full Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        tfFullName = new JTextField(20);
        mainPanel.add(tfFullName, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        mainPanel.add(new JLabel("Email (Optional):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        tfEmail = new JTextField(20);
        mainPanel.add(tfEmail, gbc);

        // Password info label
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 8, 5);
        JLabel lblPasswordInfo = new JLabel("Default Password: Changeme@123 (Students can change later)");
        lblPasswordInfo.setFont(new Font("Arial", Font.ITALIC, 12));
        lblPasswordInfo.setForeground(new Color(100, 100, 100));
        mainPanel.add(lblPasswordInfo, gbc);

        // Reset insets for next components
        gbc.insets = new Insets(8, 5, 8, 5);

        // Add mainPanel to center container
        centerContainer.add(mainPanel);
        add(centerContainer, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        btnRegister = new JButton("Register");
        btnRegister.setBackground(new Color(70, 130, 180));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        btnRegister.setPreferredSize(new Dimension(120, 35));

        btnBackToLogin = new JButton("Back to Login");
        btnBackToLogin.setBackground(new Color(128, 128, 128));
        btnBackToLogin.setForeground(Color.WHITE);
        btnBackToLogin.setFocusPainted(false);
        btnBackToLogin.setPreferredSize(new Dimension(120, 35));

        buttonPanel.add(btnRegister);
        buttonPanel.add(btnBackToLogin);
        add(buttonPanel, BorderLayout.SOUTH);

        // Generate roll number when form loads
        generateRollNumber();

        // Event listeners
        btnRegister.addActionListener(e -> registerStudent());
        btnBackToLogin.addActionListener(e -> goBackToLogin());

        setVisible(true);
    }

    private void generateRollNumber() {
        String sql = "SELECT student_roll_seq.NEXTVAL FROM dual";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                int rollNo = rs.getInt(1);
                tfRollNo.setText(String.valueOf(rollNo));
            }
            
            rs.close();
            stmt.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error generating roll number: " + ex.getMessage(),
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private void registerStudent() {
        String username = tfUsername.getText().trim();
        String fullName = tfFullName.getText().trim();
        String email = tfEmail.getText().trim();
        String rollNoText = tfRollNo.getText();

        // Default password
        String defaultPassword = "Changeme@123";

        // Validation
        if(username.isEmpty() || fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please fill all required fields (Username, Full Name).",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if(rollNoText.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Roll number not generated. Please restart the form.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        int rollNo = Integer.parseInt(rollNoText);
        String hashedPassword = hashPassword(defaultPassword);

        String sql = "INSERT INTO students (roll_number, username, password, full_name, email) " +
                     "VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, rollNo);
            ps.setString(2, username);
            ps.setString(3, hashedPassword);
            ps.setString(4, fullName);
            ps.setString(5, email.isEmpty() ? null : email);

            int rows = ps.executeUpdate();
            if(rows > 0) {
                JOptionPane.showMessageDialog(this, 
                    "Student registered successfully!\n\n" +
                    "Roll Number: " + rollNo + "\n" + 
                    "Username: " + username + "\n" +
                    "Default Password: Changeme@123\n\n" +
                    "⚠️ Student should change password after first login.",
                    "Registration Successful",
                    JOptionPane.INFORMATION_MESSAGE);

                // Go back to login form or close if opened by admin
                goBackToLogin();
            }
            
            ps.close();

        } catch (SQLException ex) {
            String message = ex.getMessage();
            if(message.contains("unique constraint")) {
                JOptionPane.showMessageDialog(this, 
                    "Username already exists. Please choose a different username.",
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Registration failed: " + message,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            }
            ex.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    private void goBackToLogin() {
        this.dispose(); // Close registration form
        
        // Only go back to login if loginForm reference exists (called from student side)
        // If called from admin dashboard, just close
        if (loginForm != null) {
            loginForm.setVisible(true); // Show existing login form
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegistrationForm());
    }
}