import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class LoginForm extends JFrame {

    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JButton btnLogin;

    public LoginForm() {
        setTitle("Student Login");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen mode
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Main panel to center everything
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(240, 240, 240));
        
        // Container for the form
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));
        formContainer.setPreferredSize(new Dimension(500, 400));
        formContainer.setMaximumSize(new Dimension(500, 400));

        // Title
        JLabel lblTitle = new JLabel("Student Login");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 32));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        formContainer.add(lblTitle);
        
        formContainer.add(Box.createRigidArea(new Dimension(0, 40)));

        // Username or Email
        JLabel lblUsername = new JLabel("Username or Email:");
        lblUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        lblUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(lblUsername);
        
        formContainer.add(Box.createRigidArea(new Dimension(0, 8)));
        
        tfUsername = new JTextField();
        tfUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        tfUsername.setMaximumSize(new Dimension(400, 35));
        tfUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(tfUsername);

        formContainer.add(Box.createRigidArea(new Dimension(0, 20)));

        // Password
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        lblPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(lblPassword);
        
        formContainer.add(Box.createRigidArea(new Dimension(0, 8)));
        
        pfPassword = new JPasswordField();
        pfPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        pfPassword.setMaximumSize(new Dimension(400, 35));
        pfPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(pfPassword);

        formContainer.add(Box.createRigidArea(new Dimension(0, 30)));

        // Login Button
        btnLogin = new JButton("Login");
        btnLogin.setBackground(new Color(70, 130, 180));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 16));
        btnLogin.setMaximumSize(new Dimension(400, 40));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(btnLogin);

        formContainer.add(Box.createRigidArea(new Dimension(0, 30)));

        // Links Panel
        JPanel linksPanel = new JPanel();
        linksPanel.setLayout(new BoxLayout(linksPanel, BoxLayout.Y_AXIS));
        linksPanel.setBackground(Color.WHITE);
        linksPanel.setMaximumSize(new Dimension(400, 60));
        linksPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Teacher Login Link
        JPanel teacherPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        teacherPanel.setBackground(Color.WHITE);
        JButton btnTeacherLogin = new JButton("Teacher Login");
        btnTeacherLogin.setForeground(new Color(60, 179, 113));
        btnTeacherLogin.setBorderPainted(false);
        btnTeacherLogin.setContentAreaFilled(false);
        btnTeacherLogin.setFocusPainted(false);
        btnTeacherLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTeacherLogin.setFont(new Font("Arial", Font.BOLD, 12));
        btnTeacherLogin.addActionListener(e -> openTeacherLogin());
        teacherPanel.add(btnTeacherLogin);
        linksPanel.add(teacherPanel);
        
        // Admin Login Link
        JPanel adminPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        adminPanel.setBackground(Color.WHITE);
        JButton btnAdminLogin = new JButton("Admin Login");
        btnAdminLogin.setForeground(new Color(220, 53, 69));
        btnAdminLogin.setBorderPainted(false);
        btnAdminLogin.setContentAreaFilled(false);
        btnAdminLogin.setFocusPainted(false);
        btnAdminLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdminLogin.setFont(new Font("Arial", Font.BOLD, 12));
        btnAdminLogin.addActionListener(e -> openAdminLogin());
        adminPanel.add(btnAdminLogin);
        linksPanel.add(adminPanel);
        
        formContainer.add(linksPanel);

        // Add formContainer to mainPanel
        mainPanel.add(formContainer);
        add(mainPanel, BorderLayout.CENTER);

        // Event listeners
        btnLogin.addActionListener(e -> performLogin());
        
        // Enter key support
        pfPassword.addActionListener(e -> performLogin());

        setVisible(true);
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

    private void performLogin() {
        String usernameOrEmail = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword());

        if (usernameOrEmail.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter both username/email and password.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String hashedPassword = hashPassword(password);
        String sql = "SELECT roll_number, username, full_name, email FROM students " +
                     "WHERE (username = ? OR email = ?) AND password = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, usernameOrEmail);
            ps.setString(2, usernameOrEmail);
            ps.setString(3, hashedPassword);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int rollNumber = rs.getInt("roll_number");
                String username = rs.getString("username");
                String fullName = rs.getString("full_name");
                String email = rs.getString("email");

                // Open dashboard
                new StudentDashboard(rollNumber, username, fullName, email);
                this.dispose(); // Close login form

            } else {
                JOptionPane.showMessageDialog(this,
                    "Invalid username/email or password.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
                pfPassword.setText("");
            }

            rs.close();
            ps.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Database error: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    private void openAdminLogin() {
        // Hide student login form
        this.setVisible(false);
        
        // Open admin login form
        new AdminLoginForm(this);
    }

    private void openTeacherLogin() {
        // Hide student login form
        this.setVisible(false);
        
        // Open teacher login form
        new TeacherLoginForm(this);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm());
    }
}