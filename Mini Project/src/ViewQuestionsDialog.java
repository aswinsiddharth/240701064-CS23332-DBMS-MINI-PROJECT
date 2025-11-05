import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ViewQuestionsDialog extends JDialog {
    
    private JTable questionsTable;
    private DefaultTableModel tableModel;
    private int examId;
    private String examName;
    
    public ViewQuestionsDialog(JFrame parent, int examId, String examName) {
        super(parent, "View Questions - " + examName, true);
        this.examId = examId;
        this.examName = examName;
        
        setSize(1000, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(70, 130, 180));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel lblTitle = new JLabel("📝 Questions - " + examName);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        titlePanel.add(lblTitle);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] columnNames = {"Q#", "Question", "A", "B", "C", "D", "Correct", "Marks"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        questionsTable = new JTable(tableModel);
        questionsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        questionsTable.setRowHeight(30);
        questionsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        questionsTable.getTableHeader().setBackground(new Color(70, 130, 180));
        questionsTable.getTableHeader().setForeground(Color.WHITE);
        
        // Set column widths
        questionsTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        questionsTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        questionsTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        questionsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        questionsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        questionsTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        questionsTable.getColumnModel().getColumn(6).setPreferredWidth(60);
        questionsTable.getColumnModel().getColumn(7).setPreferredWidth(60);
        
        JScrollPane scrollPane = new JScrollPane(questionsTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Arial", Font.BOLD, 13));
        btnClose.addActionListener(e -> dispose());
        buttonPanel.add(btnClose);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        loadQuestions();
        setVisible(true);
    }
    
    private void loadQuestions() {
        tableModel.setRowCount(0);
        
        String sql = "SELECT question_id, question_text, option_a, option_b, option_c, option_d, correct_answer, marks " +
                     "FROM questions WHERE exam_id = ? ORDER BY question_id";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();
            
            int qNum = 1;
            while (rs.next()) {
                Object[] row = {
                    qNum++,
                    rs.getString("question_text"),
                    rs.getString("option_a"),
                    rs.getString("option_b"),
                    rs.getString("option_c"),
                    rs.getString("option_d"),
                    rs.getString("correct_answer"),
                    rs.getInt("marks")
                };
                tableModel.addRow(row);
            }
            
            rs.close();
            ps.close();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading questions: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
    }
}