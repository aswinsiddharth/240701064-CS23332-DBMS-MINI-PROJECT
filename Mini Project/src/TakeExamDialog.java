import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

public class TakeExamDialog extends JDialog {
    
    private int examId;
    private String examName;
    private int rollNumber;
    private String studentName;
    private int durationMinutes;
    private int totalMarks;
    
    private List<QuestionData> questions;
    private int currentQuestionIndex = 0;
    private Map<Integer, String> studentAnswers;
    
    private JLabel lblTimer, lblQuestionNumber, lblQuestion;
    private JRadioButton rbOptionA, rbOptionB, rbOptionC, rbOptionD;
    private ButtonGroup optionsGroup;
    private JButton btnPrevious, btnNext, btnSubmit;
    private JProgressBar progressBar;
    
    private javax.swing.Timer examTimer;  // Explicitly use Swing Timer
    private int remainingSeconds;
    
    public TakeExamDialog(JFrame parent, int examId, String examName, int rollNumber, 
                          String studentName, int durationMinutes, int totalMarks) {
        super(parent, "Taking Exam: " + examName, true);
        this.examId = examId;
        this.examName = examName;
        this.rollNumber = rollNumber;
        this.studentName = studentName;
        this.durationMinutes = durationMinutes;
        this.totalMarks = totalMarks;
        this.remainingSeconds = durationMinutes * 60;
        this.studentAnswers = new HashMap<>();
        this.questions = new ArrayList<>();
        
        // Set dialog to maximized - use setSize with screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);
        setLocationRelativeTo(parent);
        
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
        
        setLayout(new BorderLayout(10, 10));
        
        // Load questions
        loadQuestions();
        
        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No questions found!", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Question Panel
        JPanel questionPanel = createQuestionPanel();
        add(questionPanel, BorderLayout.CENTER);
        
        // Navigation Panel
        JPanel navPanel = createNavigationPanel();
        add(navPanel, BorderLayout.SOUTH);
        
        // Start timer
        startTimer();
        
        // Display first question
        displayQuestion();
        
        setVisible(true);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(70, 130, 180));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        
        JLabel lblExamName = new JLabel(examName);
        lblExamName.setFont(new Font("Arial", Font.BOLD, 24));
        lblExamName.setForeground(Color.WHITE);
        
        lblTimer = new JLabel(formatTime(remainingSeconds));
        lblTimer.setFont(new Font("Arial", Font.BOLD, 28));
        lblTimer.setForeground(Color.YELLOW);
        
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);
        
        JLabel lblStudent = new JLabel("Student: " + studentName + " (Roll: " + rollNumber + ")");
        lblStudent.setFont(new Font("Arial", Font.PLAIN, 14));
        lblStudent.setForeground(Color.WHITE);
        
        JLabel lblMarks = new JLabel("Total Marks: " + totalMarks + " | Questions: " + questions.size());
        lblMarks.setFont(new Font("Arial", Font.PLAIN, 14));
        lblMarks.setForeground(Color.WHITE);
        
        infoPanel.add(lblStudent);
        infoPanel.add(lblMarks);
        
        panel.add(lblExamName, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(lblTimer, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createQuestionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        // Question header
        JPanel questionHeader = new JPanel(new BorderLayout());
        
        lblQuestionNumber = new JLabel();
        lblQuestionNumber.setFont(new Font("Arial", Font.BOLD, 18));
        
        progressBar = new JProgressBar(0, questions.size());
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(200, 25));
        
        questionHeader.add(lblQuestionNumber, BorderLayout.WEST);
        questionHeader.add(progressBar, BorderLayout.EAST);
        
        panel.add(questionHeader, BorderLayout.NORTH);
        
        // Question text
        JPanel questionTextPanel = new JPanel(new BorderLayout());
        questionTextPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        lblQuestion = new JLabel();
        lblQuestion.setFont(new Font("Arial", Font.PLAIN, 16));
        lblQuestion.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        questionTextPanel.add(lblQuestion, BorderLayout.CENTER);
        panel.add(questionTextPanel, BorderLayout.CENTER);
        
        // Options
        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        optionsGroup = new ButtonGroup();
        
        rbOptionA = createOptionButton("A");
        rbOptionB = createOptionButton("B");
        rbOptionC = createOptionButton("C");
        rbOptionD = createOptionButton("D");
        
        optionsGroup.add(rbOptionA);
        optionsGroup.add(rbOptionB);
        optionsGroup.add(rbOptionC);
        optionsGroup.add(rbOptionD);
        
        optionsPanel.add(rbOptionA);
        optionsPanel.add(rbOptionB);
        optionsPanel.add(rbOptionC);
        optionsPanel.add(rbOptionD);
        
        panel.add(optionsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JRadioButton createOptionButton(String option) {
        JRadioButton rb = new JRadioButton();
        rb.setFont(new Font("Arial", Font.PLAIN, 14));
        rb.setActionCommand(option);
        rb.addActionListener(e -> saveCurrentAnswer());
        return rb;
    }
    
    private JPanel createNavigationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        btnPrevious = new JButton("⬅️ Previous");
        btnPrevious.setFont(new Font("Arial", Font.BOLD, 14));
        btnPrevious.setPreferredSize(new Dimension(150, 40));
        btnPrevious.addActionListener(e -> previousQuestion());
        
        btnNext = new JButton("Next ➡️");
        btnNext.setFont(new Font("Arial", Font.BOLD, 14));
        btnNext.setPreferredSize(new Dimension(150, 40));
        btnNext.addActionListener(e -> nextQuestion());
        
        btnSubmit = new JButton("✅ Submit Exam");
        btnSubmit.setBackground(new Color(60, 179, 113));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFocusPainted(false);
        btnSubmit.setFont(new Font("Arial", Font.BOLD, 14));
        btnSubmit.setPreferredSize(new Dimension(150, 40));
        btnSubmit.addActionListener(e -> submitExam());
        
        panel.add(btnPrevious);
        panel.add(btnNext);
        panel.add(btnSubmit);
        
        return panel;
    }
    
    private void loadQuestions() {
        String sql = "SELECT question_id, question_text, option_a, option_b, option_c, option_d, correct_answer, marks " +
                     "FROM questions WHERE exam_id = ? ORDER BY question_id";
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                QuestionData q = new QuestionData();
                q.questionId = rs.getInt("question_id");
                q.questionText = rs.getString("question_text");
                q.optionA = rs.getString("option_a");
                q.optionB = rs.getString("option_b");
                q.optionC = rs.getString("option_c");
                q.optionD = rs.getString("option_d");
                q.correctAnswer = rs.getString("correct_answer");
                q.marks = rs.getInt("marks");
                questions.add(q);
            }
            
            rs.close();
            ps.close();
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
    }
    
    private void displayQuestion() {
        if (currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) {
            return;
        }
        
        QuestionData q = questions.get(currentQuestionIndex);
        
        lblQuestionNumber.setText("Question " + (currentQuestionIndex + 1) + " of " + questions.size() + 
                                  " (Marks: " + q.marks + ")");
        lblQuestion.setText("<html>" + q.questionText + "</html>");
        
        rbOptionA.setText("<html>A. " + q.optionA + "</html>");
        rbOptionB.setText("<html>B. " + q.optionB + "</html>");
        rbOptionC.setText("<html>C. " + q.optionC + "</html>");
        rbOptionD.setText("<html>D. " + q.optionD + "</html>");
        
        // Load saved answer if exists
        String savedAnswer = studentAnswers.get(q.questionId);
        optionsGroup.clearSelection();
        if (savedAnswer != null) {
            switch (savedAnswer) {
                case "A": rbOptionA.setSelected(true); break;
                case "B": rbOptionB.setSelected(true); break;
                case "C": rbOptionC.setSelected(true); break;
                case "D": rbOptionD.setSelected(true); break;
            }
        }
        
        // Update progress
        progressBar.setValue(studentAnswers.size());
        progressBar.setString(studentAnswers.size() + " / " + questions.size() + " answered");
        
        // Update button states
        btnPrevious.setEnabled(currentQuestionIndex > 0);
        btnNext.setEnabled(currentQuestionIndex < questions.size() - 1);
    }
    
    private void saveCurrentAnswer() {
        if (currentQuestionIndex >= 0 && currentQuestionIndex < questions.size()) {
            ButtonModel selectedModel = optionsGroup.getSelection();
            if (selectedModel != null) {
                String answer = selectedModel.getActionCommand();
                QuestionData q = questions.get(currentQuestionIndex);
                studentAnswers.put(q.questionId, answer);
                progressBar.setValue(studentAnswers.size());
                progressBar.setString(studentAnswers.size() + " / " + questions.size() + " answered");
            }
        }
    }
    
    private void previousQuestion() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            displayQuestion();
        }
    }
    
    private void nextQuestion() {
        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            displayQuestion();
        }
    }
    
    private void startTimer() {
        examTimer = new javax.swing.Timer(1000, e -> {
            remainingSeconds--;
            lblTimer.setText(formatTime(remainingSeconds));
            
            // Change color when time is running out
            if (remainingSeconds <= 300) { // 5 minutes
                lblTimer.setForeground(Color.RED);
            } else if (remainingSeconds <= 600) { // 10 minutes
                lblTimer.setForeground(Color.ORANGE);
            }
            
            if (remainingSeconds <= 0) {
                examTimer.stop();
                JOptionPane.showMessageDialog(this,
                    "Time's up! Exam will be submitted automatically.",
                    "Time Over",
                    JOptionPane.WARNING_MESSAGE);
                submitExam();
            }
        });
        examTimer.start();
    }
    
    private String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("⏰ %02d:%02d", mins, secs);
    }
    
    private void confirmExit() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to exit?\nYour progress will be lost!",
            "Confirm Exit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (examTimer != null) {
                examTimer.stop();
            }
            dispose();
        }
    }
    
    private void submitExam() {
        int unanswered = questions.size() - studentAnswers.size();
        
        if (unanswered > 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "You have " + unanswered + " unanswered question(s).\n" +
                "Do you want to submit anyway?",
                "Unanswered Questions",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        if (examTimer != null) {
            examTimer.stop();
        }
        
        // Calculate score
        int score = 0;
        for (QuestionData q : questions) {
            String studentAnswer = studentAnswers.get(q.questionId);
            if (studentAnswer != null && studentAnswer.equals(q.correctAnswer)) {
                score += q.marks;
            }
        }
        
        double percentage = (score * 100.0) / totalMarks;
        
        // Save results to database
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Insert exam result
            String sqlResult = "INSERT INTO exam_results (result_id, exam_id, roll_number, score, total_marks, percentage) " +
                              "VALUES (result_id_seq.NEXTVAL, ?, ?, ?, ?, ?)";
            PreparedStatement psResult = conn.prepareStatement(sqlResult, new String[]{"result_id"});
            psResult.setInt(1, examId);
            psResult.setInt(2, rollNumber);
            psResult.setInt(3, score);
            psResult.setInt(4, totalMarks);
            psResult.setDouble(5, percentage);
            psResult.executeUpdate();
            
            // Get generated result_id
            ResultSet rs = psResult.getGeneratedKeys();
            int resultId = 0;
            if (rs.next()) {
                resultId = rs.getInt(1);
            }
            rs.close();
            psResult.close();
            
            // Insert student answers
            String sqlAnswer = "INSERT INTO student_answers (answer_id, result_id, question_id, selected_answer, is_correct) " +
                              "VALUES (answer_id_seq.NEXTVAL, ?, ?, ?, ?)";
            PreparedStatement psAnswer = conn.prepareStatement(sqlAnswer);
            
            for (QuestionData q : questions) {
                String studentAnswer = studentAnswers.get(q.questionId);
                int isCorrect = (studentAnswer != null && studentAnswer.equals(q.correctAnswer)) ? 1 : 0;
                
                psAnswer.setInt(1, resultId);
                psAnswer.setInt(2, q.questionId);
                psAnswer.setString(3, studentAnswer);
                psAnswer.setInt(4, isCorrect);
                psAnswer.addBatch();
            }
            
            psAnswer.executeBatch();
            psAnswer.close();
            
            conn.commit();
            
            // Show result
            String grade = getGrade(percentage);
            JOptionPane.showMessageDialog(this,
                "Exam Submitted Successfully!\n\n" +
                "Score: " + score + " / " + totalMarks + "\n" +
                "Percentage: " + String.format("%.2f%%", percentage) + "\n" +
                "Grade: " + grade,
                "Exam Result",
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
                "Error submitting exam: " + ex.getMessage(),
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
    
    // Inner class to hold question data
    private static class QuestionData {
        int questionId;
        String questionText;
        String optionA, optionB, optionC, optionD;
        String correctAnswer;
        int marks;
    }
}