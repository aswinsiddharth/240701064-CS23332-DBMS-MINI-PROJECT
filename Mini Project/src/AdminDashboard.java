import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminDashboard extends JFrame {
    
    private int adminId;
    private String username;
    private String fullName;
    private String email;
    
    private JLabel lblWelcome;
    private JButton btnViewStudents, btnAddStudent, btnManageAdmins, btnReports, btnLogout;
    private JTable studentsTable;
    private DefaultTableModel tableModel;
    
    public AdminDashboard(int adminId, String username, String fullName, String email) {
        this.adminId = adminId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Admin Dashboard - " + fullName);
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
        
        // Students Table Panel
        gbc.gridy = 1;
        gbc.weighty = 0.7;
        JPanel tablePanel = createStudentsTablePanel();
        mainPanel.add(tablePanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Footer Panel
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
        
        // Load students data
        loadStudentsData();
        
        setVisible(true);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(220, 53, 69));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        lblWelcome = new JLabel("Admin Dashboard - Welcome, " + fullName + "!");
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 28));
        lblWelcome.setForeground(Color.WHITE);
        
        JLabel lblSubtitle = new JLabel("System Administration Panel");
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
                BorderFactory.createLineBorder(new Color(220, 53, 69), 2),
                "Quick Actions",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                new Color(220, 53, 69)
            ),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        btnViewStudents = createActionButton("👥 View Students", new Color(70, 130, 180));
        btnAddStudent = createActionButton("➕ Add Student", new Color(60, 179, 113));
        btnManageAdmins = createActionButton("👨‍🏫 Manage Teachers", new Color(255, 165, 0));
        btnReports = createActionButton("📊 Reports", new Color(147, 112, 219));
        
        panel.add(btnViewStudents);
        panel.add(btnAddStudent);
        panel.add(btnManageAdmins);
        panel.add(btnReports);
        
        // Event listeners
        btnViewStudents.addActionListener(e -> loadStudentsData());
        btnAddStudent.addActionListener(e -> addStudent());
        btnManageAdmins.addActionListener(e -> manageTeachers());
        btnReports.addActionListener(e -> viewReports());
        
        return panel;
    }
    
    private JPanel createStudentsTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 53, 69), 2),
                "All Students",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                new Color(220, 53, 69)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Create table
        String[] columnNames = {"Roll No", "Username", "Full Name", "Email", "Created At"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        studentsTable = new JTable(tableModel);
        studentsTable.setFont(new Font("Arial", Font.PLAIN, 13));
        studentsTable.setRowHeight(25);
        studentsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        studentsTable.getTableHeader().setBackground(new Color(220, 53, 69));
        studentsTable.getTableHeader().setForeground(Color.WHITE);
        studentsTable.setSelectionBackground(new Color(255, 200, 200));
        
        JScrollPane scrollPane = new JScrollPane(studentsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel for table actions
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 12));
        btnRefresh.addActionListener(e -> loadStudentsData());
        
        JButton btnDelete = new JButton("🗑️ Delete Selected");
        btnDelete.setFont(new Font("Arial", Font.BOLD, 12));
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnDelete.addActionListener(e -> deleteSelectedStudent());
        
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnDelete);
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
    
    private void loadStudentsData() {
        tableModel.setRowCount(0); // Clear existing data
        
        String sql = "SELECT roll_number, username, full_name, email, created_at FROM students ORDER BY roll_number";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("roll_number"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("email") != null ? rs.getString("email") : "N/A",
                    rs.getTimestamp("created_at")
                };
                tableModel.addRow(row);
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading students: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
    }
    
    private void addStudent() {
        // Open registration form from admin dashboard
        JFrame tempFrame = new JFrame();
        tempFrame.setVisible(false);
        
        RegistrationForm regForm = new RegistrationForm(null) {
            @Override
            public void dispose() {
                super.dispose();
                tempFrame.dispose();
                loadStudentsData(); // Refresh the table after adding
            }
        };
        
        // Override the goBackToLogin method to return to admin dashboard
        regForm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        regForm.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                loadStudentsData();
            }
        });
    }
    
    private void deleteSelectedStudent() {
        int selectedRow = studentsTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a student to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int rollNumber = (int) tableModel.getValueAt(selectedRow, 0);
        String studentName = (String) tableModel.getValueAt(selectedRow, 2);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete student:\n" + studentName + " (Roll: " + rollNumber + ")?",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM students WHERE roll_number = ?";
            
            Connection conn = null;
            try {
                conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, rollNumber);
                
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this,
                        "Student deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadStudentsData();
                }
                
                ps.close();
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error deleting student: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                DBConnection.closeConnection(conn);
            }
        }
    }
    
    private void manageTeachers() {
        new TeacherManagementDialog(this);
    }
    
    private void viewReports() {
        int totalStudents = tableModel.getRowCount();
        
        String report = String.format(
            "SYSTEM REPORTS\n\n" +
            "Total Students: %d\n" +
            "Active Admins: 1\n" +
            "Database Status: Connected\n\n" +
            "Recent Activity:\n" +
            "• Last login: Now\n" +
            "• Students registered today: 0\n",
            totalStudents
        );
        
        JOptionPane.showMessageDialog(this,
            report,
            "System Reports",
            JOptionPane.INFORMATION_MESSAGE);
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