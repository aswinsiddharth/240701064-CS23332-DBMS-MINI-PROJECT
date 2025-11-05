import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class TeacherLoginForm extends JFrame {

    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JButton btnLogin, btnBackToStudent;
    private LoginForm studentLoginForm;

    public TeacherLoginForm(LoginForm studentLoginForm) {
        this.studentLoginForm = studentLoginForm;
        
        setTitle("Teacher Login");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Main panel to center everything
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(240, 240, 240));
        
        // Container for the form
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBackground(new Color(250, 255, 250));
        formContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 179, 113), 2),
            BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));
        formContainer.setPreferredSize(new Dimension(500, 400));
        formContainer.setMaximumSize(new Dimension(500, 400));

        // Title with icon
        JLabel lblTitle = new JLabel("👨‍🏫 Teacher Login");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 32));
        lblTitle.setForeground(new Color(60, 179, 113));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        formContainer.add(lblTitle);
        
        // Subtitle
        JLabel lblSubtitle = new JLabel("Faculty Access Portal");
        lblSubtitle.setFont(new Font("Arial", Font.ITALIC, 12));
        lblSubtitle.setForeground(new Color(150, 150, 150));
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        formContainer.add(lblSubtitle);
        
        formContainer.add(Box.createRigidArea(new Dimension(0, 40)));

        // Username
        JLabel lblUsername = new JLabel("Teacher Username:");
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
        btnLogin = new JButton("Teacher Login");
        btnLogin.setBackground(new Color(60, 179, 113));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 16));
        btnLogin.setMaximumSize(new Dimension(400, 40));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(btnLogin);

        formContainer.add(Box.createRigidArea(new Dimension(0, 20)));

        // Back to Student Login
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        backPanel.setBackground(new Color(250, 255, 250));
        backPanel.setMaximumSize(new Dimension(400, 30));
        backPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        btnBackToStudent = new JButton("← Back to Student Login");
        btnBackToStudent.setForeground(new Color(70, 130, 180));
        btnBackToStudent.setBorderPainted(false);
        btnBackToStudent.setContentAreaFilled(false);
        btnBackToStudent.setFocusPainted(false);
        btnBackToStudent.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBackToStudent.setFont(new Font("Arial", Font.BOLD, 13));
        
        backPanel.add(btnBackToStudent);
        formContainer.add(backPanel);

        // Add formContainer to mainPanel
        mainPanel.add(formContainer);
        add(mainPanel, BorderLayout.CENTER);

        // Event listeners
        btnLogin.addActionListener(e -> performLogin());
        btnBackToStudent.addActionListener(e -> backToStudentLogin());
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
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter both username and password.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String hashedPassword = hashPassword(password);
        String sql = "SELECT teacher_id, full_name, email, subject FROM teachers WHERE username = ? AND password = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, hashedPassword);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int teacherId = rs.getInt("teacher_id");
                String fullName = rs.getString("full_name");
                String email = rs.getString("email");
                String subject = rs.getString("subject");

                // Update last login time
                updateLastLogin(teacherId);

                // Open teacher dashboard
                new TeacherDashboard(teacherId, username, fullName, email, subject);
                this.dispose();

            } else {
                JOptionPane.showMessageDialog(this,
                    "Invalid teacher credentials.",
                    "Authentication Failed",
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

    private void updateLastLogin(int teacherId) {
        String sql = "UPDATE teachers SET last_login = CURRENT_TIMESTAMP WHERE teacher_id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, teacherId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
    }

    private void backToStudentLogin() {
        this.dispose();
        if (studentLoginForm != null) {
            studentLoginForm.setVisible(true);
        } else {
            new LoginForm();
        }
    }
}