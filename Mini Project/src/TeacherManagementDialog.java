import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class TeacherManagementDialog extends JDialog {
    
    private JTable teachersTable;
    private DefaultTableModel tableModel;
    private JButton btnAddTeacher, btnDeleteTeacher, btnRefresh, btnClose;
    private JFrame parent;
    
    public TeacherManagementDialog(JFrame parent) {
        super(parent, "Manage Teachers", true);
        this.parent = parent;
        
        setSize(900, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(255, 165, 0));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel lblTitle = new JLabel("👨‍🏫 Teacher Management");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        titlePanel.add(lblTitle);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] columnNames = {"ID", "Username", "Full Name", "Email", "Subject", "Created At"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        teachersTable = new JTable(tableModel);
        teachersTable.setFont(new Font("Arial", Font.PLAIN, 13));
        teachersTable.setRowHeight(25);
        teachersTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        teachersTable.getTableHeader().setBackground(new Color(255, 165, 0));
        teachersTable.getTableHeader().setForeground(Color.WHITE);
        teachersTable.setSelectionBackground(new Color(255, 230, 200));
        
        JScrollPane scrollPane = new JScrollPane(teachersTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        btnAddTeacher = new JButton("➕ Add Teacher");
        btnAddTeacher.setBackground(new Color(60, 179, 113));
        btnAddTeacher.setForeground(Color.WHITE);
        btnAddTeacher.setFocusPainted(false);
        btnAddTeacher.setFont(new Font("Arial", Font.BOLD, 13));
        
        btnDeleteTeacher = new JButton("🗑️ Delete Selected");
        btnDeleteTeacher.setBackground(new Color(220, 53, 69));
        btnDeleteTeacher.setForeground(Color.WHITE);
        btnDeleteTeacher.setFocusPainted(false);
        btnDeleteTeacher.setFont(new Font("Arial", Font.BOLD, 13));
        
        btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 13));
        
        btnClose = new JButton("Close");
        btnClose.setFont(new Font("Arial", Font.BOLD, 13));
        
        buttonPanel.add(btnAddTeacher);
        buttonPanel.add(btnDeleteTeacher);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnClose);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Event listeners
        btnAddTeacher.addActionListener(e -> addTeacher());
        btnDeleteTeacher.addActionListener(e -> deleteTeacher());
        btnRefresh.addActionListener(e -> loadTeachers());
        btnClose.addActionListener(e -> dispose());
        
        loadTeachers();
        setVisible(true);
    }
    
    private void loadTeachers() {
        tableModel.setRowCount(0);
        
        String sql = "SELECT teacher_id, username, full_name, email, subject, created_at FROM teachers ORDER BY teacher_id";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("teacher_id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("email") != null ? rs.getString("email") : "N/A",
                    rs.getString("subject") != null ? rs.getString("subject") : "N/A",
                    rs.getTimestamp("created_at")
                };
                tableModel.addRow(row);
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading teachers: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
    }
    
    private void addTeacher() {
        JTextField tfUsername = new JTextField(20);
        JTextField tfFullName = new JTextField(20);
        JTextField tfEmail = new JTextField(20);
        JTextField tfSubject = new JTextField(20);
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.add(new JLabel("Username:"));
        panel.add(tfUsername);
        panel.add(new JLabel("Full Name:"));
        panel.add(tfFullName);
        panel.add(new JLabel("Email:"));
        panel.add(tfEmail);
        panel.add(new JLabel("Subject:"));
        panel.add(tfSubject);
        
        JLabel lblPassword = new JLabel("Default Password: Changeme@123");
        lblPassword.setFont(new Font("Arial", Font.ITALIC, 11));
        lblPassword.setForeground(new Color(100, 100, 100));
        panel.add(lblPassword);
        panel.add(new JLabel());
        
        int result = JOptionPane.showConfirmDialog(this, panel,
            "Add New Teacher", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String username = tfUsername.getText().trim();
            String fullName = tfFullName.getText().trim();
            String email = tfEmail.getText().trim();
            String subject = tfSubject.getText().trim();
            
            if (username.isEmpty() || fullName.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Username and Full Name are required!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String defaultPassword = "Changeme@123";
            String hashedPassword = hashPassword(defaultPassword);
            
            String sql = "INSERT INTO teachers (teacher_id, username, password, full_name, email, subject) " +
                        "VALUES (teacher_id_seq.NEXTVAL, ?, ?, ?, ?, ?)";
            
            Connection conn = null;
            try {
                conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, username);
                ps.setString(2, hashedPassword);
                ps.setString(3, fullName);
                ps.setString(4, email.isEmpty() ? null : email);
                ps.setString(5, subject.isEmpty() ? null : subject);
                
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this,
                        "Teacher added successfully!\n\n" +
                        "Username: " + username + "\n" +
                        "Default Password: Changeme@123\n\n" +
                        "Teacher should change password after first login.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadTeachers();
                }
                
                ps.close();
                
            } catch (SQLException ex) {
                if (ex.getMessage().contains("unique constraint")) {
                    JOptionPane.showMessageDialog(this,
                        "Username already exists!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Error adding teacher: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                }
                ex.printStackTrace();
            } finally {
                DBConnection.closeConnection(conn);
            }
        }
    }
    
    private void deleteTeacher() {
        int selectedRow = teachersTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a teacher to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int teacherId = (int) tableModel.getValueAt(selectedRow, 0);
        String teacherName = (String) tableModel.getValueAt(selectedRow, 2);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete teacher:\n" + teacherName + "?",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM teachers WHERE teacher_id = ?";
            
            Connection conn = null;
            try {
                conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, teacherId);
                
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this,
                        "Teacher deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadTeachers();
                }
                
                ps.close();
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error deleting teacher: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                DBConnection.closeConnection(conn);
            }
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
            
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}