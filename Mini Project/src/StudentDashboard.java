import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;

public class StudentDashboard extends JFrame {
    
    private int rollNumber;
    private String username;
    private String fullName;
    private String email;
    
    private JLabel lblWelcome, lblRollNo, lblUsername, lblEmail;
    private JButton btnViewProfile, btnEditProfile, btnChangePassword, btnLogout;
    
    public StudentDashboard(int rollNumber, String username, String fullName, String email) {
        this.rollNumber = rollNumber;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Student Dashboard - " + fullName);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen mode
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main Content Panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1;
        gbc.weighty = 1;
        
        // Left Panel - Student Info
        gbc.gridx = 0;
        gbc.gridy = 0;
        JPanel leftPanel = createStudentInfoPanel();
        mainPanel.add(leftPanel, gbc);
        
        // Right Panel - Quick Actions only
        gbc.gridx = 1;
        gbc.gridy = 0;
        JPanel rightPanel = createQuickActionsPanel();
        mainPanel.add(rightPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Footer Panel
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(70, 130, 180));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        lblWelcome = new JLabel("Welcome, " + fullName + "!");
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 28));
        lblWelcome.setForeground(Color.WHITE);
        
        JLabel lblSubtitle = new JLabel("Student Dashboard");
        lblSubtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(230, 230, 230));
        
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(lblWelcome, BorderLayout.CENTER);
        textPanel.add(lblSubtitle, BorderLayout.SOUTH);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createStudentInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Student Information",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 16),
                new Color(70, 130, 180)
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Profile icon placeholder
        JPanel iconPanel = new JPanel();
        iconPanel.setPreferredSize(new Dimension(100, 100));
        iconPanel.setBackground(new Color(200, 200, 200));
        JLabel iconLabel = new JLabel("👤", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 60));
        iconPanel.add(iconLabel);
        
        // Info Panel
        JPanel infoPanel = new JPanel(new GridLayout(4, 2, 10, 15));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Roll Number
        infoPanel.add(createInfoLabel("Roll Number:"));
        lblRollNo = createValueLabel(String.valueOf(rollNumber));
        infoPanel.add(lblRollNo);
        
        // Username
        infoPanel.add(createInfoLabel("Username:"));
        lblUsername = createValueLabel(username);
        infoPanel.add(lblUsername);
        
        // Full Name
        infoPanel.add(createInfoLabel("Full Name:"));
        JLabel lblFullName = createValueLabel(fullName);
        infoPanel.add(lblFullName);
        
        // Email
        infoPanel.add(createInfoLabel("Email:"));
        lblEmail = createValueLabel(email != null ? email : "Not provided");
        infoPanel.add(lblEmail);
        
        panel.add(iconPanel, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        
        // Quick Actions Panel
        JPanel actionsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        actionsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Quick Actions",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                new Color(70, 130, 180)
            ),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        btnViewProfile = createActionButton("📝 Attend Exam", new Color(70, 130, 180));
        btnEditProfile = createActionButton("✏️ Edit Profile", new Color(60, 179, 113));
        btnChangePassword = createActionButton("🔒 Change Password", new Color(255, 165, 0));
        JButton btnViewGrades = createActionButton("📊 View Results", new Color(147, 112, 219));
        
        actionsPanel.add(btnViewProfile);
        actionsPanel.add(btnEditProfile);
        actionsPanel.add(btnChangePassword);
        actionsPanel.add(btnViewGrades);
        
        // Event listeners
        btnViewProfile.addActionListener(e -> attendExam());
        btnEditProfile.addActionListener(e -> editProfile());
        btnChangePassword.addActionListener(e -> changePassword());
        btnViewGrades.addActionListener(e -> viewResults());
        
        panel.add(actionsPanel);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        btnLogout = new JButton("🚪 Logout");
        btnLogout.setBackground(new Color(220, 53, 69));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogout.setPreferredSize(new Dimension(120, 35));
        btnLogout.addActionListener(e -> logout());
        
        panel.add(btnLogout);
        
        return panel;
    }
    
    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(70, 70, 70));
        return label;
    }
    
    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(new Color(50, 50, 50));
        return label;
    }
    
    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private void attendExam() {
        new ExamSelectionDialog(this, rollNumber, fullName);
    }
    
    private void viewResults() {
        new StudentResultsDialog(this, rollNumber, fullName);
    }
    
    private void editProfile() {
        JTextField tfNewFullName = new JTextField(fullName);
        JTextField tfNewEmail = new JTextField(email != null ? email : "");
        
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.add(new JLabel("Full Name:"));
        panel.add(tfNewFullName);
        panel.add(new JLabel("Email:"));
        panel.add(tfNewEmail);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Edit Profile", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String newFullName = tfNewFullName.getText().trim();
            String newEmail = tfNewEmail.getText().trim();
            
            if (newFullName.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Full name cannot be empty!", 
                    "Validation Error", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            updateProfile(newFullName, newEmail);
        }
    }
    
    private void updateProfile(String newFullName, String newEmail) {
        String sql = "UPDATE students SET full_name = ?, email = ? WHERE roll_number = ?";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, newFullName);
            ps.setString(2, newEmail.isEmpty() ? null : newEmail);
            ps.setInt(3, rollNumber);
            
            int rows = ps.executeUpdate();
            if (rows > 0) {
                this.fullName = newFullName;
                this.email = newEmail.isEmpty() ? null : newEmail;
                
                lblWelcome.setText("Welcome, " + fullName + "!");
                lblEmail.setText(email != null ? email : "Not provided");
                
                JOptionPane.showMessageDialog(this,
                    "Profile updated successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
            ps.close();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error updating profile: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
    }
    
    private void changePassword() {
        JPasswordField pfOldPassword = new JPasswordField();
        JPasswordField pfNewPassword = new JPasswordField();
        JPasswordField pfConfirmPassword = new JPasswordField();
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Old Password:"));
        panel.add(pfOldPassword);
        panel.add(new JLabel("New Password:"));
        panel.add(pfNewPassword);
        panel.add(new JLabel("Confirm Password:"));
        panel.add(pfConfirmPassword);
        
        int result = JOptionPane.showConfirmDialog(this, panel,
            "Change Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String oldPassword = new String(pfOldPassword.getPassword());
            String newPassword = new String(pfNewPassword.getPassword());
            String confirmPassword = new String(pfConfirmPassword.getPassword());
            
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this,
                    "New passwords do not match!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (newPassword.length() < 6) {
                JOptionPane.showMessageDialog(this,
                    "Password must be at least 6 characters long.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            updatePassword(oldPassword, newPassword);
        }
    }
    
    private void updatePassword(String oldPassword, String newPassword) {
        String oldHash = hashPassword(oldPassword);
        String newHash = hashPassword(newPassword);
        
        String sqlCheck = "SELECT password FROM students WHERE roll_number = ?";
        String sqlUpdate = "UPDATE students SET password = ? WHERE roll_number = ?";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            
            // Verify old password
            PreparedStatement psCheck = conn.prepareStatement(sqlCheck);
            psCheck.setInt(1, rollNumber);
            ResultSet rs = psCheck.executeQuery();
            
            if (rs.next()) {
                String currentHash = rs.getString("password");
                
                if (!currentHash.equals(oldHash)) {
                    JOptionPane.showMessageDialog(this,
                        "Old password is incorrect!",
                        "Authentication Error",
                        JOptionPane.ERROR_MESSAGE);
                    rs.close();
                    psCheck.close();
                    return;
                }
            }
            
            rs.close();
            psCheck.close();
            
            // Update password
            PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate);
            psUpdate.setString(1, newHash);
            psUpdate.setInt(2, rollNumber);
            
            int rows = psUpdate.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this,
                    "Password changed successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
            psUpdate.close();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error changing password: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
    }
    
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    private void viewGrades() {
        viewResults(); // Redirect to view results
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            new LoginForm();
        }
    }
}