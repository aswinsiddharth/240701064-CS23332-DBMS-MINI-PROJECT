import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.*;

public class ViewStudentAnswersDialog extends JDialog {
    
    private JTable answersTable;
    private DefaultTableModel tableModel;
    private int resultId;
    private String examName;
    private String studentName;
    
    public ViewStudentAnswersDialog(JDialog parent, int resultId, String examName, String studentName) {
        super(parent, "Student Answers - " + studentName, true);
        this.resultId = resultId;
        this.examName = examName;
        this.studentName = studentName;
        
        setSize(1100, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(70, 130, 180));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel lblTitle = new JLabel("📝 " + examName + " - " + studentName);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        titlePanel.add(lblTitle);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] columnNames = {"Q#", "Question", "Correct Answer", "Student Answer", "Status", "Marks"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        answersTable = new JTable(tableModel);
        answersTable.setFont(new Font("Arial", Font.PLAIN, 12));
        answersTable.setRowHeight(35);
        answersTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        answersTable.getTableHeader().setBackground(new Color(70, 130, 180));
        answersTable.getTableHeader().setForeground(Color.WHITE);
        
        // Set column widths
        answersTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        answersTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        answersTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        answersTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        answersTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        answersTable.getColumnModel().getColumn(5).setPreferredWidth(60);
        
        // Custom cell renderer for status column
        answersTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                String status = (String) value;
                if (status.equals("✓ Correct")) {
                    c.setForeground(new Color(0, 150, 0));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (status.equals("✗ Wrong")) {
                    c.setForeground(Color.RED);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setForeground(Color.GRAY);
                }
                
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(answersTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Arial", Font.BOLD, 13));
        btnClose.addActionListener(e -> dispose());
        buttonPanel.add(btnClose);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        loadAnswers();
        setVisible(true);
    }
    
    private void loadAnswers() {
        tableModel.setRowCount(0);
        
        String sql = "SELECT q.question_text, q.correct_answer, q.marks, sa.selected_answer, sa.is_correct " +
                     "FROM student_answers sa " +
                     "JOIN questions q ON sa.question_id = q.question_id " +
                     "WHERE sa.result_id = ? " +
                     "ORDER BY q.question_id";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, resultId);
            ResultSet rs = ps.executeQuery();
            
            int qNum = 1;
            while (rs.next()) {
                String correctAnswer = rs.getString("correct_answer");
                String studentAnswer = rs.getString("selected_answer");
                int isCorrect = rs.getInt("is_correct");
                int marks = rs.getInt("marks");
                
                String status;
                String marksAwarded;
                
                if (studentAnswer == null || studentAnswer.isEmpty()) {
                    status = "Not Answered";
                    marksAwarded = "0 / " + marks;
                } else if (isCorrect == 1) {
                    status = "✓ Correct";
                    marksAwarded = marks + " / " + marks;
                } else {
                    status = "✗ Wrong";
                    marksAwarded = "0 / " + marks;
                }
                
                Object[] row = {
                    qNum++,
                    rs.getString("question_text"),
                    correctAnswer,
                    studentAnswer != null ? studentAnswer : "-",
                    status,
                    marksAwarded
                };
                tableModel.addRow(row);
            }
            
            rs.close();
            ps.close();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading answers: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
    }
}