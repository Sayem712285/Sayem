package com.vox.drei;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuestionDatabase {
    private static final String DB_URL = "jdbc:sqlite:quiz_app.db";
    private static final Logger logger = Logger.getLogger(QuestionDatabase.class.getName());

    static {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS quizzes (id TEXT PRIMARY KEY, quiz_number INTEGER, name TEXT, category TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS questions (id TEXT PRIMARY KEY, quiz_id TEXT, question_number INTEGER, question TEXT, type TEXT, correct_answer TEXT, FOREIGN KEY(quiz_id) REFERENCES quizzes(id))");
            stmt.execute("CREATE TABLE IF NOT EXISTS answers (id TEXT PRIMARY KEY, question_id TEXT, answer TEXT, FOREIGN KEY(question_id) REFERENCES questions(id))");

            // Add quiz_number column if it doesn't exist
            try {
                stmt.execute("ALTER TABLE quizzes ADD COLUMN quiz_number INTEGER");
            } catch (SQLException e) {
                // Column might already exist, ignore
            }

            try {
                stmt.execute("ALTER TABLE questions ADD COLUMN question_number INTEGER");
            } catch (SQLException e) {
                // Column might already exist, ignore
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addQuiz(Quiz quiz) {
        String sql = "INSERT INTO quizzes(id, quiz_number, name, category) VALUES(?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, quiz.getId());
            pstmt.setInt(2, getNextQuizNumber());
            pstmt.setString(3, quiz.getName());
            pstmt.setString(4, quiz.getCategory());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding quiz to database", e);
            throw new RuntimeException("Failed to add quiz to database", e);
        }
    }


    public static void updateQuestion(Question question) {
        String sql = "UPDATE questions SET question = ?, type = ?, correct_answer = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, question.getQuestion());
            pstmt.setString(2, question.getType());
            pstmt.setString(3, question.getCorrectAnswer());
            pstmt.setString(4, question.getId());
            pstmt.executeUpdate();

            // Update answers
            deleteAnswers(question.getId());
            for (String answer : question.getAnswers()) {
                addAnswer(question.getId(), answer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void deleteAnswers(String questionId) {
        String sql = "DELETE FROM answers WHERE question_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, questionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int getNextQuizNumber() {
        String sql = "SELECT MAX(quiz_number) FROM quizzes";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static void addQuestion(Question question, String quizId) {
        String sql = "INSERT INTO questions(id, quiz_id, question_number, question, type, correct_answer) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, question.getId());
            pstmt.setString(2, quizId);
            pstmt.setInt(3, getNextQuestionNumber(quizId));
            pstmt.setString(4, question.getQuestion());
            pstmt.setString(5, question.getType());
            pstmt.setString(6, question.getCorrectAnswer());
            pstmt.executeUpdate();

            for (String answer : question.getAnswers()) {
                addAnswer(question.getId(), answer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int getNextQuestionNumber(String quizId) {
        String sql = "SELECT MAX(question_number) FROM questions WHERE quiz_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, quizId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private static void addAnswer(String questionId, String answer) {
        String sql = "INSERT INTO answers(id, question_id, answer) VALUES(?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, UUID.randomUUID().toString());
            pstmt.setString(2, questionId);
            pstmt.setString(3, answer);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteQuestion(String questionId) {
        String getQuizIdSql = "SELECT quiz_id, question_number FROM questions WHERE id = ?";
        String deleteAnswersSql = "DELETE FROM answers WHERE question_id = ?";
        String deleteQuestionSql = "DELETE FROM questions WHERE id = ?";
        String updateQuestionNumbersSql = "UPDATE questions SET question_number = question_number - 1 WHERE quiz_id = ? AND question_number > ?";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            String quizId = null;
            int questionNumber = 0;

            // Get quiz_id and question_number of the question to be deleted
            try (PreparedStatement pstmt = conn.prepareStatement(getQuizIdSql)) {
                pstmt.setString(1, questionId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    quizId = rs.getString("quiz_id");
                    questionNumber = rs.getInt("question_number");
                }
            }

            // Delete answers
            try (PreparedStatement pstmt = conn.prepareStatement(deleteAnswersSql)) {
                pstmt.setString(1, questionId);
                pstmt.executeUpdate();
            }

            // Delete question
            try (PreparedStatement pstmt = conn.prepareStatement(deleteQuestionSql)) {
                pstmt.setString(1, questionId);
                pstmt.executeUpdate();
            }

            // Update question numbers for remaining questions
            if (quizId != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(updateQuestionNumbersSql)) {
                    pstmt.setString(1, quizId);
                    pstmt.setInt(2, questionNumber);
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Quiz> loadQuizzes() {
        List<Quiz> quizzes = new ArrayList<>();
        String sql = "SELECT * FROM quizzes ORDER BY quiz_number";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Quiz quiz = new Quiz();
                quiz.setId(rs.getString("id"));
                quiz.setQuizNumber(rs.getInt("quiz_number"));
                quiz.setName(rs.getString("name"));
                quiz.setCategory(rs.getString("category"));
                quiz.setQuestions(getQuestionsForQuiz(quiz.getId()));
                quizzes.add(quiz);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return quizzes;
    }

    public static List<Question> getQuestionsForQuiz(String quizId) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE quiz_id = ? ORDER BY question_number";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, quizId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Question question = new Question();
                question.setId(rs.getString("id"));
                question.setQuestionNumber(rs.getInt("question_number"));
                question.setQuestion(rs.getString("question"));
                question.setType(rs.getString("type"));
                question.setCorrectAnswer(rs.getString("correct_answer"));
                question.setAnswers(getAnswersForQuestion(question.getId()));
                questions.add(question);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }

    private static List<String> getAnswersForQuestion(String questionId) {
        List<String> answers = new ArrayList<>();
        String sql = "SELECT answer FROM answers WHERE question_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                answers.add(rs.getString("answer"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answers;
    }

    public static void updateQuiz(Quiz quiz) {
        String sql = "UPDATE quizzes SET name = ?, category = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, quiz.getName());
            pstmt.setString(2, quiz.getCategory());
            pstmt.setString(3, quiz.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteQuiz(String quizId) {
        String deleteQuizSql = "DELETE FROM quizzes WHERE id = ?";
        String deleteQuestionsSql = "DELETE FROM questions WHERE quiz_id = ?";
        String deleteAnswersSql = "DELETE FROM answers WHERE question_id IN (SELECT id FROM questions WHERE quiz_id = ?)";
        String updateQuizNumbersSql = "UPDATE quizzes SET quiz_number = quiz_number - 1 WHERE quiz_number > ?";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            int deletedQuizNumber = 0;

            // Get the quiz number of the quiz to be deleted
            String getQuizNumberSql = "SELECT quiz_number FROM quizzes WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(getQuizNumberSql)) {
                pstmt.setString(1, quizId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    deletedQuizNumber = rs.getInt("quiz_number");
                }
            }

            // Delete answers
            try (PreparedStatement pstmt = conn.prepareStatement(deleteAnswersSql)) {
                pstmt.setString(1, quizId);
                pstmt.executeUpdate();
            }

            // Delete questions
            try (PreparedStatement pstmt = conn.prepareStatement(deleteQuestionsSql)) {
                pstmt.setString(1, quizId);
                pstmt.executeUpdate();
            }

            // Delete quiz
            try (PreparedStatement pstmt = conn.prepareStatement(deleteQuizSql)) {
                pstmt.setString(1, quizId);
                pstmt.executeUpdate();
            }

            // Update quiz numbers for remaining quizzes
            if (deletedQuizNumber > 0) {
                try (PreparedStatement pstmt = conn.prepareStatement(updateQuizNumbersSql)) {
                    pstmt.setInt(1, deletedQuizNumber);
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}