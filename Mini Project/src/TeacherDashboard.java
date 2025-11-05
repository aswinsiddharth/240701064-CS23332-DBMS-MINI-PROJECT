import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TeacherDashboard extends JFrame {
    
    private int teacherId;
    private String username;
    private String fullName;
    private String email;
    private String subject;
    
    private JLabel lblWelcome;
    private JButton btnCreateExam, btnViewExams, btnViewResults, btnChangePassword, btnLogout;
    private JTable examsTable;
    private DefaultTableModel tableModel;
    
    public TeacherDashboard(int teacherId, String username, String fullName, String email, String subject) {
        this.teacherId = teacherId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.subject = subject;
        
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Teacher Dashboard - " + fullName);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
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
        gbc.weighty = 0.3;
        
        // Quick Actions Panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        JPanel actionsPanel = createQuickActionsPanel();
        mainPanel.add(actionsPanel, gbc);
        
        // Exams Table Panel
        gbc.gridy = 1;
        gbc.weighty = 0.7;
        JPanel tablePanel = createExamsTablePanel();
        mainPanel.add(tablePanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Footer Panel
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
        
        // Load exams data
        loadExamsData();
        
        setVisible(true);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(60, 179, 113));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        lblWelcome = new JLabel("Teacher Dashboard - Welcome, " + fullName + "!");
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 28));
        lblWelcome.setForeground(Color.WHITE);
        
        String subjectText = subject != null ? "Subject: " + subject : "Faculty Portal";
        JLabel lblSubtitle = new JLabel(subjectText);
        lblSubtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(230, 230, 230));
        
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(lblWelcome, BorderLayout.CENTER);
        textPanel.add(lblSubtitle, BorderLayout.SOUTH);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 15));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 179, 113), 2),
                "Quick Actions",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                new Color(60, 179, 113)
            ),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        btnCreateExam = createActionButton("➕ Create MCQ Exam", new Color(70, 130, 180));
        btnViewExams = createActionButton("📋 View All Exams", new Color(60, 179, 113));
        btnViewResults = createActionButton("📊 View Results", new Color(147, 112, 219));
        btnChangePassword = createActionButton("🔒 Change Password", new Color(255, 165, 0));
        
        panel.add(btnCreateExam);
        panel.add(btnViewExams);
        panel.add(btnViewResults);
        panel.add(btnChangePassword);
        
        // Event listeners
        btnCreateExam.addActionListener(e -> createExam());
        btnViewExams.addActionListener(e -> loadExamsData());
        btnViewResults.addActionListener(e -> viewResults());
        btnChangePassword.addActionListener(e -> changePassword());
        
        return panel;
    }
    
    private JPanel createExamsTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 179, 113), 2),
                "My Exams",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                new Color(60, 179, 113)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Create table
        String[] columnNames = {"Exam ID", "Exam Name", "Duration (min)", "Total Marks", "Questions", "Active", "Created At"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        examsTable = new JTable(tableModel);
        examsTable.setFont(new Font("Arial", Font.PLAIN, 13));
        examsTable.setRowHeight(25);
        examsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        examsTable.getTableHeader().setBackground(new Color(60, 179, 113));
        examsTable.getTableHeader().setForeground(Color.WHITE);
        examsTable.setSelectionBackground(new Color(200, 255, 200));
        
        JScrollPane scrollPane = new JScrollPane(examsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel for table actions
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 12));
        btnRefresh.addActionListener(e -> loadExamsData());
        
        JButton btnViewQuestions = new JButton("📝 View Questions");
        btnViewQuestions.setFont(new Font("Arial", Font.BOLD, 12));
        btnViewQuestions.setBackground(new Color(70, 130, 180));
        btnViewQuestions.setForeground(Color.WHITE);
        btnViewQuestions.setFocusPainted(false);
        btnViewQuestions.addActionListener(e -> viewQuestions());
        
        JButton btnToggleActive = new JButton("⏸️ Toggle Active");
        btnToggleActive.setFont(new Font("Arial", Font.BOLD, 12));
        btnToggleActive.setBackground(new Color(255, 165, 0));
        btnToggleActive.setForeground(Color.WHITE);
        btnToggleActive.setFocusPainted(false);
        btnToggleActive.addActionListener(e -> toggleExamActive());
        
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnViewQuestions);
        buttonPanel.add(btnToggleActive);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
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
    
    private void loadExamsData() {
        tableModel.setRowCount(0);
        
        // Use subquery to avoid GROUP BY issues with TIMESTAMP
        String sql = "SELECT e.exam_id, e.exam_name, e.duration_minutes, e.total_marks, e.is_active, " +
                     "TO_CHAR(e.created_at, 'YYYY-MM-DD HH24:MI:SS') as created_at_str, " +
                     "(SELECT COUNT(*) FROM questions q WHERE q.exam_id = e.exam_id) as question_count " +
                     "FROM exams e " +
                     "WHERE e.teacher_id = ? " +
                     "ORDER BY e.created_at DESC";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, teacherId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("exam_id"),
                    rs.getString("exam_name"),
                    rs.getInt("duration_minutes"),
                    rs.getInt("total_marks"),
                    rs.getInt("question_count"),
                    rs.getInt("is_active") == 1 ? "Yes" : "No",
                    rs.getString("created_at_str")
                };
                tableModel.addRow(row);
            }
            
            rs.close();
            ps.close();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading exams: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
    }
    
    private void createExam() {
        new CreateExamDialog(this, teacherId);
        loadExamsData();
    }
    
    private void viewQuestions() {
        int selectedRow = examsTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an exam to view questions.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int examId = (int) tableModel.getValueAt(selectedRow, 0);
        String examName = (String) tableModel.getValueAt(selectedRow, 1);
        
        new ViewQuestionsDialog(this, examId, examName);
    }
    
    private void toggleExamActive() {
        int selectedRow = examsTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an exam to toggle active status.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int examId = (int) tableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 5);
        int newStatus = currentStatus.equals("Yes") ? 0 : 1;
        
        String sql = "UPDATE exams SET is_active = ? WHERE exam_id = ?";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, newStatus);
            ps.setInt(2, examId);
            
            ps.executeUpdate();
            ps.close();
            
            JOptionPane.showMessageDialog(this,
                "Exam status updated!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            loadExamsData();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error updating exam: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            DBConnection.closeConnection(conn);
        }
    }
    
    private void viewResults() {
        new TeacherResultsDialog(this, teacherId);
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
        
        String sqlCheck = "SELECT password FROM teachers WHERE teacher_id = ?";
        String sqlUpdate = "UPDATE teachers SET password = ? WHERE teacher_id = ?";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            
            PreparedStatement psCheck = conn.prepareStatement(sqlCheck);
            psCheck.setInt(1, teacherId);
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
            
            PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate);
            psUpdate.setString(1, newHash);
            psUpdate.setInt(2, teacherId);
            
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