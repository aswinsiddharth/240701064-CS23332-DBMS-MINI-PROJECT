import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CreateExamDialog extends JDialog {
    
    private JTextField tfExamName, tfDuration;
    private JButton btnAddQuestions, btnSaveExam, btnCancel;
    private JLabel lblQuestionCount, lblTotalMarks;
    private int teacherId;
    private List<QuestionData> questions;
    
    public CreateExamDialog(JFrame parent, int teacherId) {
        super(parent, "Create New MCQ Exam", true);
        this.teacherId = teacherId;
        this.questions = new ArrayList<>();
        
        setSize(600, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(70, 130, 180));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel lblTitle = new JLabel("📝 Create New MCQ Exam");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        titlePanel.add(lblTitle);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Exam Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Exam Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        tfExamName = new JTextField(20);
        formPanel.add(tfExamName, gbc);
        
        // Duration
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Duration (minutes):"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        tfDuration = new JTextField(20);
        formPanel.add(tfDuration, gbc);
        
        // Question Count
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Questions Added:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        lblQuestionCount = new JLabel("0");
        lblQuestionCount.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(lblQuestionCount, gbc);
        
        // Total Marks
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Total Marks:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        lblTotalMarks = new JLabel("0");
        lblTotalMarks.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(lblTotalMarks, gbc);
        
        // Add Questions Button
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        btnAddQuestions = new JButton("➕ Add Questions");
        btnAddQuestions.setBackground(new Color(60, 179, 113));
        btnAddQuestions.setForeground(Color.WHITE);
        btnAddQuestions.setFocusPainted(false);
        btnAddQuestions.setFont(new Font("Arial", Font.BOLD, 14));
        btnAddQuestions.addActionListener(e -> addQuestion());
        formPanel.add(btnAddQuestions, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        btnSaveExam = new JButton("💾 Save Exam");
        btnSaveExam.setBackground(new Color(70, 130, 180));
        btnSaveExam.setForeground(Color.WHITE);
        btnSaveExam.setFocusPainted(false);
        btnSaveExam.setFont(new Font("Arial", Font.BOLD, 14));
        btnSaveExam.addActionListener(e -> saveExam());
        
        btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Arial", Font.BOLD, 14));
        btnCancel.addActionListener(e -> dispose());
        
        buttonPanel.add(btnSaveExam);
        buttonPanel.add(btnCancel);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    private void addQuestion() {
        JTextField tfQuestion = new JTextField(30);
        JTextField tfOptionA = new JTextField(30);
        JTextField tfOptionB = new JTextField(30);
        JTextField tfOptionC = new JTextField(30);
        JTextField tfOptionD = new JTextField(30);
        JComboBox<String> cbCorrectAnswer = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        JTextField tfMarks = new JTextField(5);
        
        JPanel panel = new JPanel(new GridLayout(8, 2, 10, 10));
        panel.add(new JLabel("Question:"));
        panel.add(tfQuestion);
        panel.add(new JLabel("Option A:"));
        panel.add(tfOptionA);
        panel.add(new JLabel("Option B:"));
        panel.add(tfOptionB);
        panel.add(new JLabel("Option C:"));
        panel.add(tfOptionC);
        panel.add(new JLabel("Option D:"));
        panel.add(tfOptionD);
        panel.add(new JLabel("Correct Answer:"));
        panel.add(cbCorrectAnswer);
        panel.add(new JLabel("Marks:"));
        panel.add(tfMarks);
        
        int result = JOptionPane.showConfirmDialog(this, panel,
            "Add MCQ Question", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String question = tfQuestion.getText().trim();
                String optionA = tfOptionA.getText().trim();
                String optionB = tfOptionB.getText().trim();
                String optionC = tfOptionC.getText().trim();
                String optionD = tfOptionD.getText().trim();
                String correctAnswer = (String) cbCorrectAnswer.getSelectedItem();
                int marks = Integer.parseInt(tfMarks.getText().trim());
                
                if (question.isEmpty() || optionA.isEmpty() || optionB.isEmpty() || 
                    optionC.isEmpty() || optionD.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "All fields are required!",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                QuestionData qData = new QuestionData(question, optionA, optionB, optionC, optionD, correctAnswer, marks);
                questions.add(qData);
                
                updateStats();
                
                JOptionPane.showMessageDialog(this,
                    "Question added successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                    "Marks must be a valid number!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    private void updateStats() {
        lblQuestionCount.setText(String.valueOf(questions.size()));
        int totalMarks = questions.stream().mapToInt(q -> q.marks).sum();
        lblTotalMarks.setText(String.valueOf(totalMarks));
    }
    
    private void saveExam() {
        String examName = tfExamName.getText().trim();
        String durationStr = tfDuration.getText().trim();
        
        if (examName.isEmpty() || durationStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill in exam name and duration!",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please add at least one question!",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int duration = Integer.parseInt(durationStr);
            int totalMarks = questions.stream().mapToInt(q -> q.marks).sum();
            
            Connection conn = null;
            try {
                conn = DBConnection.getConnection();
                conn.setAutoCommit(false);
                
                // Insert exam
                String sqlExam = "INSERT INTO exams (exam_id, exam_name, teacher_id, duration_minutes, total_marks) " +
                                "VALUES (exam_id_seq.NEXTVAL, ?, ?, ?, ?)";
                PreparedStatement psExam = conn.prepareStatement(sqlExam, new String[]{"exam_id"});
                psExam.setString(1, examName);
                psExam.setInt(2, teacherId);
                psExam.setInt(3, duration);
                psExam.setInt(4, totalMarks);
                psExam.executeUpdate();
                
                // Get generated exam_id
                ResultSet rs = psExam.getGeneratedKeys();
                int examId = 0;
                if (rs.next()) {
                    examId = rs.getInt(1);
                }
                rs.close();
                psExam.close();
                
                // Insert questions
                String sqlQuestion = "INSERT INTO questions (question_id, exam_id, question_text, option_a, option_b, option_c, option_d, correct_answer, marks) " +
                                    "VALUES (question_id_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement psQuestion = conn.prepareStatement(sqlQuestion);
                
                for (QuestionData q : questions) {
                    psQuestion.setInt(1, examId);
                    psQuestion.setString(2, q.questionText);
                    psQuestion.setString(3, q.optionA);
                    psQuestion.setString(4, q.optionB);
                    psQuestion.setString(5, q.optionC);
                    psQuestion.setString(6, q.optionD);
                    psQuestion.setString(7, q.correctAnswer);
                    psQuestion.setInt(8, q.marks);
                    psQuestion.addBatch();
                }
                
                psQuestion.executeBatch();
                psQuestion.close();
                
                conn.commit();
                
                JOptionPane.showMessageDialog(this,
                    "Exam created successfully!\n\n" +
                    "Exam: " + examName + "\n" +
                    "Questions: " + questions.size() + "\n" +
                    "Total Marks: " + totalMarks,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
                dispose();
                
            } catch (SQLException ex) {
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                JOptionPane.showMessageDialog(this,
                    "Error creating exam: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                DBConnection.closeConnection(conn);
            }
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Duration must be a valid number!",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    // Inner class to hold question data
    private static class QuestionData {
        String questionText;
        String optionA, optionB, optionC, optionD;
        String correctAnswer;
        int marks;
        
        QuestionData(String questionText, String optionA, String optionB, String optionC, 
                    String optionD, String correctAnswer, int marks) {
            this.questionText = questionText;
            this.optionA = optionA;
            this.optionB = optionB;
            this.optionC = optionC;
            this.optionD = optionD;
            this.correctAnswer = correctAnswer;
            this.marks = marks;
        }
    }
}