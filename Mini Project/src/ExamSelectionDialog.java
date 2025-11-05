import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ExamSelectionDialog extends JDialog {
    
    private JTable examsTable;
    private DefaultTableModel tableModel;
    private int rollNumber;
    private String studentName;
    private JFrame parent;
    
    public ExamSelectionDialog(JFrame parent, int rollNumber, String studentName) {
        super(parent, "Available Exams", true);
        this.parent = parent;
        this.rollNumber = rollNumber;
        this.studentName = studentName;
        
        setSize(900, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(70, 130, 180));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel lblTitle = new JLabel("📝 Select Exam to Attend");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        titlePanel.add(lblTitle);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] columnNames = {"Exam ID", "Exam Name", "Teacher", "Duration (min)", "Total Marks", "Questions", "Status"};
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
        examsTable.getTableHeader().setBackground(new Color(70, 130, 180));
        examsTable.getTableHeader().setForeground(Color.WHITE);
        examsTable.setSelectionBackground(new Color(200, 220, 255));
        
        JScrollPane scrollPane = new JScrollPane(examsTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton btnStartExam = new JButton("▶️ Start Exam");
        btnStartExam.setBackground(new Color(60, 179, 113));
        btnStartExam.setForeground(Color.WHITE);
        btnStartExam.setFocusPainted(false);
        btnStartExam.setFont(new Font("Arial", Font.BOLD, 14));
        btnStartExam.addActionListener(e -> startExam());
        
        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 13));
        btnRefresh.addActionListener(e -> loadExams());
        
        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Arial", Font.BOLD, 13));
        btnClose.addActionListener(e -> dispose());
        
        buttonPanel.add(btnStartExam);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnClose);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        loadExams();
        setVisible(true);
    }
    
    private void loadExams() {
        tableModel.setRowCount(0);
        
        // FIXED: Use subquery to avoid GROUP BY issues with TIMESTAMP
        String sql = "SELECT e.exam_id, e.exam_name, t.full_name as teacher_name, " +
                     "e.duration_minutes, e.total_marks, " +
                     "(SELECT COUNT(*) FROM questions q WHERE q.exam_id = e.exam_id) as question_count, " +
                     "CASE WHEN EXISTS (SELECT 1 FROM exam_results er WHERE er.exam_id = e.exam_id AND er.roll_number = ?) " +
                     "THEN 'Completed' ELSE 'Available' END as status " +
                     "FROM exams e " +
                     "JOIN teachers t ON e.teacher_id = t.teacher_id " +
                     "WHERE e.is_active = 1 " +
                     "ORDER BY e.created_at DESC";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, rollNumber);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("exam_id"),
                    rs.getString("exam_name"),
                    rs.getString("teacher_name"),
                    rs.getInt("duration_minutes"),
                    rs.getInt("total_marks"),
                    rs.getInt("question_count"),
                    rs.getString("status")
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
    
    private void startExam() {
        int selectedRow = examsTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an exam to start.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String status = (String) tableModel.getValueAt(selectedRow, 6);
        if (status.equals("Completed")) {
            JOptionPane.showMessageDialog(this,
                "You have already completed this exam!",
                "Already Completed",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int examId = (int) tableModel.getValueAt(selectedRow, 0);
        String examName = (String) tableModel.getValueAt(selectedRow, 1);
        int duration = (int) tableModel.getValueAt(selectedRow, 3);
        int totalMarks = (int) tableModel.getValueAt(selectedRow, 4);
        int questionCount = (int) tableModel.getValueAt(selectedRow, 5);
        
        if (questionCount == 0) {
            JOptionPane.showMessageDialog(this,
                "This exam has no questions yet!",
                "No Questions",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Start Exam?\n\n" +
            "Exam: " + examName + "\n" +
            "Duration: " + duration + " minutes\n" +
            "Questions: " + questionCount + "\n" +
            "Total Marks: " + totalMarks + "\n\n" +
            "Once started, timer cannot be paused!",
            "Confirm Start Exam",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            new TakeExamDialog(parent, examId, examName, rollNumber, studentName, duration, totalMarks);
        }
    }
}