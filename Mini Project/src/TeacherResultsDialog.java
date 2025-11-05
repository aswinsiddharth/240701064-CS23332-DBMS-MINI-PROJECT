import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TeacherResultsDialog extends JDialog {
    
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private int teacherId;
    private JComboBox<String> cbExamFilter;
    
    public TeacherResultsDialog(JFrame parent, int teacherId) {
        super(parent, "View All Student Results", true);
        this.teacherId = teacherId;
        
        setSize(1000, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        // Title Panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(147, 112, 219));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel lblTitle = new JLabel("📊 Student Results");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        titlePanel.add(lblTitle, BorderLayout.WEST);
        
        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setOpaque(false);
        
        JLabel lblFilter = new JLabel("Filter by Exam:");
        lblFilter.setForeground(Color.WHITE);
        lblFilter.setFont(new Font("Arial", Font.BOLD, 13));
        
        cbExamFilter = new JComboBox<>();
        cbExamFilter.setPreferredSize(new Dimension(200, 25));
        cbExamFilter.addActionListener(e -> loadResults());
        
        filterPanel.add(lblFilter);
        filterPanel.add(cbExamFilter);
        titlePanel.add(filterPanel, BorderLayout.EAST);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] columnNames = {"Result ID", "Exam Name", "Roll No", "Student Name", "Score", "Total", "Percentage", "Date"};
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
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        JButton btnViewAnswers = new JButton("📝 View Answers");
        btnViewAnswers.setBackground(new Color(70, 130, 180));
        btnViewAnswers.setForeground(Color.WHITE);
        btnViewAnswers.setFocusPainted(false);
        btnViewAnswers.setFont(new Font("Arial", Font.BOLD, 13));
        btnViewAnswers.addActionListener(e -> viewStudentAnswers());
        
        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 13));
        btnRefresh.addActionListener(e -> {
            loadExamFilter();
            loadResults();
        });
        
        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Arial", Font.BOLD, 13));
        btnClose.addActionListener(e -> dispose());
        
        buttonPanel.add(btnViewAnswers);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnClose);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        loadExamFilter();
        loadResults();
        setVisible(true);
    }
    
    private void loadExamFilter() {
        cbExamFilter.removeAllItems();
        cbExamFilter.addItem("All Exams");
        
        String sql = "SELECT exam_id, exam_name FROM exams WHERE teacher_id = ? ORDER BY exam_name";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, teacherId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                cbExamFilter.addItem(rs.getInt("exam_id") + " - " + rs.getString("exam_name"));
            }
            
            rs.close();
            ps.close();
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
    }
    
    private void loadResults() {
        tableModel.setRowCount(0);
        
        String selectedExam = (String) cbExamFilter.getSelectedItem();
        Integer examIdFilter = null;
        
        if (selectedExam != null && !selectedExam.equals("All Exams")) {
            examIdFilter = Integer.parseInt(selectedExam.split(" - ")[0]);
        }
        
        String sql = "SELECT r.result_id, e.exam_name, r.roll_number, s.full_name, r.score, r.total_marks, r.percentage, r.exam_date " +
                     "FROM exam_results r " +
                     "JOIN exams e ON r.exam_id = e.exam_id " +
                     "JOIN students s ON r.roll_number = s.roll_number " +
                     "WHERE e.teacher_id = ? " +
                     (examIdFilter != null ? "AND e.exam_id = ? " : "") +
                     "ORDER BY r.exam_date DESC";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, teacherId);
            if (examIdFilter != null) {
                ps.setInt(2, examIdFilter);
            }
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("result_id"),
                    rs.getString("exam_name"),
                    rs.getInt("roll_number"),
                    rs.getString("full_name"),
                    rs.getInt("score"),
                    rs.getInt("total_marks"),
                    String.format("%.2f%%", rs.getDouble("percentage")),
                    rs.getTimestamp("exam_date")
                };
                tableModel.addRow(row);
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
    
    private void viewStudentAnswers() {
        int selectedRow = resultsTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a result to view detailed answers.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int resultId = (int) tableModel.getValueAt(selectedRow, 0);
        String examName = (String) tableModel.getValueAt(selectedRow, 1);
        String studentName = (String) tableModel.getValueAt(selectedRow, 3);
        
        new ViewStudentAnswersDialog(this, resultId, examName, studentName);
    }
}