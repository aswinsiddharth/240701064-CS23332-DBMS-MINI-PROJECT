import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentResultsDialog extends JDialog {
    
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private int rollNumber;
    private String studentName;
    
    public StudentResultsDialog(JFrame parent, int rollNumber, String studentName) {
        super(parent, "My Exam Results", true);
        this.rollNumber = rollNumber;
        this.studentName = studentName;
        
        setSize(900, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(147, 112, 219));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel lblTitle = new JLabel("📊 My Exam Results - " + studentName);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        titlePanel.add(lblTitle);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] columnNames = {"Exam Name", "Teacher", "Score", "Total Marks", "Percentage", "Grade", "Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        resultsTable = new JTable(tableModel);
        resultsTable.setFont(new Font("Arial", Font.PLAIN, 13));
        resultsTable.setRowHeight(25);
        resultsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        resultsTable.getTableHeader().setBackground(new Color(147, 112, 219));
        resultsTable.getTableHeader().setForeground(Color.WHITE);
        resultsTable.setSelectionBackground(new Color(220, 200, 255));
        
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
        
        // Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel lblTotalExams = new JLabel("Total Exams: 0", SwingConstants.CENTER);
        lblTotalExams.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel lblAvgScore = new JLabel("Avg Score: 0%", SwingConstants.CENTER);
        lblAvgScore.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel lblHighest = new JLabel("Highest: 0%", SwingConstants.CENTER);
        lblHighest.setFont(new Font("Arial", Font.BOLD, 14));
        
        statsPanel.add(lblTotalExams);
        statsPanel.add(lblAvgScore);
        statsPanel.add(lblHighest);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(statsPanel, BorderLayout.CENTER);
        
        JPanel btnSubPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton btnViewDetails = new JButton("📝 View Details");
        btnViewDetails.setBackground(new Color(70, 130, 180));
        btnViewDetails.setForeground(Color.WHITE);
        btnViewDetails.setFocusPainted(false);
        btnViewDetails.setFont(new Font("Arial", Font.BOLD, 13));
        btnViewDetails.addActionListener(e -> viewDetails());
        
        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 13));
        btnRefresh.addActionListener(e -> loadResults(lblTotalExams, lblAvgScore, lblHighest));
        
        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Arial", Font.BOLD, 13));
        btnClose.addActionListener(e -> dispose());
        
        btnSubPanel.add(btnViewDetails);
        btnSubPanel.add(btnRefresh);
        btnSubPanel.add(btnClose);
        
        buttonPanel.add(btnSubPanel, BorderLayout.SOUTH);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        loadResults(lblTotalExams, lblAvgScore, lblHighest);
        setVisible(true);
    }
    
    private void loadResults(JLabel lblTotalExams, JLabel lblAvgScore, JLabel lblHighest) {
        tableModel.setRowCount(0);
        
        String sql = "SELECT e.exam_name, t.full_name as teacher_name, r.score, r.total_marks, " +
                     "r.percentage, r.exam_date, r.result_id " +
                     "FROM exam_results r " +
                     "JOIN exams e ON r.exam_id = e.exam_id " +
                     "JOIN teachers t ON e.teacher_id = t.teacher_id " +
                     "WHERE r.roll_number = ? " +
                     "ORDER BY r.exam_date DESC";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, rollNumber);
            ResultSet rs = ps.executeQuery();
            
            int totalExams = 0;
            double totalPercentage = 0;
            double highest = 0;
            
            while (rs.next()) {
                double percentage = rs.getDouble("percentage");
                String grade = getGrade(percentage);
                
                Object[] row = {
                    rs.getString("exam_name"),
                    rs.getString("teacher_name"),
                    rs.getInt("score"),
                    rs.getInt("total_marks"),
                    String.format("%.2f%%", percentage),
                    grade,
                    rs.getTimestamp("exam_date")
                };
                tableModel.addRow(row);
                
                totalExams++;
                totalPercentage += percentage;
                if (percentage > highest) {
                    highest = percentage;
                }
            }
            
            // Update stats
            lblTotalExams.setText("Total Exams: " + totalExams);
            if (totalExams > 0) {
                lblAvgScore.setText(String.format("Avg Score: %.2f%%", totalPercentage / totalExams));
                lblHighest.setText(String.format("Highest: %.2f%%", highest));
            }
            
            rs.close();
            ps.close();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading results: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
    }
    
    private String getGrade(double percentage) {
        if (percentage >= 90) return "A+";
        else if (percentage >= 80) return "A";
        else if (percentage >= 70) return "B+";
        else if (percentage >= 60) return "B";
        else if (percentage >= 50) return "C";
        else if (percentage >= 40) return "D";
        else return "F";
    }
    
    private void viewDetails() {
        int selectedRow = resultsTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a result to view details.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String examName = (String) tableModel.getValueAt(selectedRow, 0);
        int score = (int) tableModel.getValueAt(selectedRow, 2);
        int totalMarks = (int) tableModel.getValueAt(selectedRow, 3);
        String percentage = (String) tableModel.getValueAt(selectedRow, 4);
        String grade = (String) tableModel.getValueAt(selectedRow, 5);
        
        String message = String.format(
            "EXAM RESULT DETAILS\n\n" +
            "Exam: %s\n" +
            "Score: %d / %d\n" +
            "Percentage: %s\n" +
            "Grade: %s\n\n" +
            "Performance: %s",
            examName, score, totalMarks, percentage, grade,
            getPerformanceMessage(Double.parseDouble(percentage.replace("%", "")))
        );
        
        JOptionPane.showMessageDialog(this,
            message,
            "Result Details",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private String getPerformanceMessage(double percentage) {
        if (percentage >= 90) return "Excellent! Outstanding performance!";
        else if (percentage >= 80) return "Very Good! Keep it up!";
        else if (percentage >= 70) return "Good work! You're doing well.";
        else if (percentage >= 60) return "Satisfactory. Room for improvement.";
        else if (percentage >= 50) return "Average. Need to work harder.";
        else if (percentage >= 40) return "Below average. Please study more.";
        else return "Failed. Need significant improvement.";
    }
}