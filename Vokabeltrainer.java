import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Vokabeltrainer - Alles in einer Datei!
 * Mit SQLite Datenbank und Prüfungsmodus
 */
public class Vokabeltrainer {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VokabeltrainerApp app = new VokabeltrainerApp();
            app.setVisible(true);
        });
    }
}

// ==================== HAUPTFENSTER ====================
class VokabeltrainerApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private ManageSetPanel manageSetPanel;
    private QuizPanel quizPanel;
    private DatabaseManager dbManager;

    public VokabeltrainerApp() {
        dbManager = new DatabaseManager();
        dbManager.initialize();

        setTitle("Vokabeltrainer");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        manageSetPanel = new ManageSetPanel(dbManager, this);
        quizPanel = new QuizPanel(dbManager, this);

        mainPanel.add(manageSetPanel, "MANAGE");
        mainPanel.add(quizPanel, "QUIZ");

        add(mainPanel);
        showManagePanel();
    }

    public void showManagePanel() {
        manageSetPanel.refreshSets();
        cardLayout.show(mainPanel, "MANAGE");
    }

    public void showQuizPanel() {
        quizPanel.refreshSets();
        cardLayout.show(mainPanel, "QUIZ");
    }
}

// ==================== DATENBANK MANAGER (SQLite) ====================
class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:vokabeltrainer.db";
    private Connection conn;

    public void initialize() {
        try {
            // SQLite JDBC Treiber explizit laden
            Class.forName("org.sqlite.JDBC");
            
            // Verbindung herstellen
            conn = DriverManager.getConnection(DB_URL);
            
            // Tabellen erstellen
            Statement stmt = conn.createStatement();
            
            stmt.execute("CREATE TABLE IF NOT EXISTS vokabel_sets (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL UNIQUE)");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS vokabeln (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "set_id INTEGER NOT NULL, " +
                    "original TEXT NOT NULL, " +
                    "translation TEXT NOT NULL, " +
                    "FOREIGN KEY (set_id) REFERENCES vokabel_sets(id) ON DELETE CASCADE)");
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Fehler bei Datenbankinitialisierung: " + e.getMessage());
        }
    }

    // Vokabelset-Operationen
    public void createSet(String name) throws SQLException {
        String sql = "INSERT INTO vokabel_sets (name) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        }
    }

    public List<VokabelSet> getAllSets() {
        List<VokabelSet> sets = new ArrayList<>();
        String sql = "SELECT id, name FROM vokabel_sets ORDER BY name";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                sets.add(new VokabelSet(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sets;
    }

    public void deleteSet(int setId) throws SQLException {
        String sql = "DELETE FROM vokabel_sets WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, setId);
            pstmt.executeUpdate();
        }
    }

    // Vokabel-Operationen
    public void addVokabel(int setId, String original, String translation) throws SQLException {
        String sql = "INSERT INTO vokabeln (set_id, original, translation) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, setId);
            pstmt.setString(2, original);
            pstmt.setString(3, translation);
            pstmt.executeUpdate();
        }
    }

    public List<Vokabel> getVokabelnBySet(int setId) {
        List<Vokabel> vokabeln = new ArrayList<>();
        String sql = "SELECT id, original, translation FROM vokabeln WHERE set_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, setId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                vokabeln.add(new Vokabel(
                    rs.getInt("id"),
                    rs.getString("original"),
                    rs.getString("translation")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vokabeln;
    }

    public void deleteVokabel(int vokabelId) throws SQLException {
        String sql = "DELETE FROM vokabeln WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vokabelId);
            pstmt.executeUpdate();
        }
    }
}

// ==================== DATENMODELLE ====================
class VokabelSet {
    private int id;
    private String name;

    public VokabelSet(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() { return name; }
}

class Vokabel {
    private int id;
    private String original;
    private String translation;

    public Vokabel(int id, String original, String translation) {
        this.id = id;
        this.original = original;
        this.translation = translation;
    }

    public int getId() { return id; }
    public String getOriginal() { return original; }
    public String getTranslation() { return translation; }
}

// ==================== VERWALTUNGS-PANEL ====================
class ManageSetPanel extends JPanel {
    private DatabaseManager dbManager;
    private VokabeltrainerApp app;
    private JComboBox<VokabelSet> setComboBox;
    private JTable vokabelTable;
    private DefaultTableModel tableModel;

    public ManageSetPanel(DatabaseManager dbManager, VokabeltrainerApp app) {
        this.dbManager = dbManager;
        this.app = app;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("📚 Vokabelsets verwalten", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JButton switchButton = new JButton("➡ Zum Abfragen");
        switchButton.addActionListener(e -> app.showQuizPanel());
        headerPanel.add(switchButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Set-Management
        JPanel setPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        setPanel.add(new JLabel("Set:"));
        
        setComboBox = new JComboBox<>();
        setComboBox.setPreferredSize(new Dimension(200, 25));
        setComboBox.addActionListener(e -> loadVokabeln());
        setPanel.add(setComboBox);

        JButton newSetButton = new JButton("➕ Neues Set");
        newSetButton.addActionListener(e -> createNewSet());
        setPanel.add(newSetButton);

        JButton deleteSetButton = new JButton("🗑 Set löschen");
        deleteSetButton.addActionListener(e -> deleteCurrentSet());
        setPanel.add(deleteSetButton);

        // Vokabel Table
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(setPanel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Original", "Übersetzung"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        vokabelTable = new JTable(tableModel);
        vokabelTable.getColumnModel().getColumn(0).setMaxWidth(50);
        JScrollPane scrollPane = new JScrollPane(vokabelTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton addButton = new JButton("➕ Vokabel hinzufügen");
        addButton.addActionListener(e -> addVokabel());
        buttonPanel.add(addButton);

        JButton deleteButton = new JButton("🗑 Vokabel löschen");
        deleteButton.addActionListener(e -> deleteSelectedVokabel());
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void refreshSets() {
        setComboBox.removeAllItems();
        List<VokabelSet> sets = dbManager.getAllSets();
        for (VokabelSet set : sets) {
            setComboBox.addItem(set);
        }
        if (!sets.isEmpty()) {
            loadVokabeln();
        }
    }

    private void createNewSet() {
        String name = JOptionPane.showInputDialog(this, "Name des neuen Sets:");
        if (name != null && !name.trim().isEmpty()) {
            try {
                dbManager.createSet(name.trim());
                refreshSets();
                JOptionPane.showMessageDialog(this, "✓ Set erfolgreich erstellt!");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Fehler: " + e.getMessage(), 
                    "Fehler", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteCurrentSet() {
        VokabelSet selectedSet = (VokabelSet) setComboBox.getSelectedItem();
        if (selectedSet != null) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Set '" + selectedSet.getName() + "' wirklich löschen?",
                "Bestätigung", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    dbManager.deleteSet(selectedSet.getId());
                    refreshSets();
                    JOptionPane.showMessageDialog(this, "✓ Set gelöscht!");
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Fehler: " + e.getMessage(),
                        "Fehler", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void loadVokabeln() {
        tableModel.setRowCount(0);
        VokabelSet selectedSet = (VokabelSet) setComboBox.getSelectedItem();
        if (selectedSet != null) {
            List<Vokabel> vokabeln = dbManager.getVokabelnBySet(selectedSet.getId());
            for (Vokabel v : vokabeln) {
                tableModel.addRow(new Object[]{v.getId(), v.getOriginal(), v.getTranslation()});
            }
        }
    }

    private void addVokabel() {
        VokabelSet selectedSet = (VokabelSet) setComboBox.getSelectedItem();
        if (selectedSet == null) {
            JOptionPane.showMessageDialog(this, "Bitte zuerst ein Set auswählen!");
            return;
        }

        JTextField originalField = new JTextField();
        JTextField translationField = new JTextField();
        
        Object[] message = {
            "Original:", originalField,
            "Übersetzung:", translationField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Neue Vokabel", 
            JOptionPane.OK_CANCEL_OPTION);
        
        if (option == JOptionPane.OK_OPTION) {
            String original = originalField.getText().trim();
            String translation = translationField.getText().trim();
            
            if (!original.isEmpty() && !translation.isEmpty()) {
                try {
                    dbManager.addVokabel(selectedSet.getId(), original, translation);
                    loadVokabeln();
                    JOptionPane.showMessageDialog(this, "✓ Vokabel hinzugefügt!");
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Fehler: " + e.getMessage(),
                        "Fehler", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Bitte beide Felder ausfüllen!");
            }
        }
    }

    private void deleteSelectedVokabel() {
        int selectedRow = vokabelTable.getSelectedRow();
        
        if (selectedRow >= 0) {
            int vokabelId = (int) tableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Diese Vokabel wirklich löschen?",
                "Bestätigung", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    dbManager.deleteVokabel(vokabelId);
                    loadVokabeln();
                    JOptionPane.showMessageDialog(this, "✓ Vokabel gelöscht!");
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Fehler: " + e.getMessage(),
                        "Fehler", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Bitte eine Vokabel auswählen!");
        }
    }
}

// ==================== QUIZ/PRÜFUNGS-PANEL ====================
class QuizPanel extends JPanel {
    private DatabaseManager dbManager;
    private VokabeltrainerApp app;
    private JComboBox<VokabelSet> setComboBox;
    private JComboBox<String> modeComboBox;
    private JLabel questionLabel;
    private JTextField answerField;
    private JLabel resultLabel;
    private JLabel scoreLabel;
    private JButton checkButton;
    private JButton nextButton;

    private List<Vokabel> currentVokabeln;
    private int currentIndex = 0;
    private int correctCount = 0;
    private int totalCount = 0;
    private boolean reverseMode = false; // false = Original→Übersetzung, true = Übersetzung→Original

    public QuizPanel(DatabaseManager dbManager, VokabeltrainerApp app) {
        this.dbManager = dbManager;
        this.app = app;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("🎯 Vokabeln abfragen", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JButton switchButton = new JButton("⬅ Zur Verwaltung");
        switchButton.addActionListener(e -> app.showManagePanel());
        headerPanel.add(switchButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Set & Mode Selection
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Set:"));
        
        setComboBox = new JComboBox<>();
        setComboBox.setPreferredSize(new Dimension(200, 25));
        controlPanel.add(setComboBox);

        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(new JLabel("Modus:"));
        
        modeComboBox = new JComboBox<>(new String[]{
            "📖 Original → Übersetzung",
            "🔄 Übersetzung → Original",
            "🎲 Gemischt"
        });
        modeComboBox.setPreferredSize(new Dimension(220, 25));
        controlPanel.add(modeComboBox);

        JButton startButton = new JButton("▶ Quiz starten");
        startButton.addActionListener(e -> startQuiz());
        controlPanel.add(startButton);

        // Quiz Area
        JPanel quizPanel = new JPanel();
        quizPanel.setLayout(new BoxLayout(quizPanel, BoxLayout.Y_AXIS));
        quizPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        scoreLabel = new JLabel(" ");
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        quizPanel.add(scoreLabel);

        quizPanel.add(Box.createVerticalStrut(30));

        questionLabel = new JLabel("Bitte ein Set auswählen und Quiz starten");
        questionLabel.setFont(new Font("Arial", Font.BOLD, 20));
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        quizPanel.add(questionLabel);

        quizPanel.add(Box.createVerticalStrut(30));

        answerField = new JTextField(20);
        answerField.setMaximumSize(new Dimension(400, 35));
        answerField.setFont(new Font("Arial", Font.PLAIN, 18));
        answerField.setEnabled(false);
        answerField.addActionListener(e -> checkAnswer());
        quizPanel.add(answerField);

        quizPanel.add(Box.createVerticalStrut(20));

        resultLabel = new JLabel(" ");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 16));
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        quizPanel.add(resultLabel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(quizPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        checkButton = new JButton("✓ Antwort prüfen");
        checkButton.setEnabled(false);
        checkButton.addActionListener(e -> checkAnswer());
        buttonPanel.add(checkButton);

        nextButton = new JButton("➡ Nächste Vokabel");
        nextButton.setEnabled(false);
        nextButton.addActionListener(e -> nextVokabel());
        buttonPanel.add(nextButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void refreshSets() {
        setComboBox.removeAllItems();
        List<VokabelSet> sets = dbManager.getAllSets();
        for (VokabelSet set : sets) {
            setComboBox.addItem(set);
        }
    }

    private void startQuiz() {
        VokabelSet selectedSet = (VokabelSet) setComboBox.getSelectedItem();
        if (selectedSet == null) {
            JOptionPane.showMessageDialog(this, "Bitte ein Set auswählen!");
            return;
        }

        currentVokabeln = dbManager.getVokabelnBySet(selectedSet.getId());
        
        if (currentVokabeln.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Dieses Set enthält keine Vokabeln!");
            return;
        }

        Collections.shuffle(currentVokabeln);
        currentIndex = 0;
        correctCount = 0;
        totalCount = 0;

        answerField.setEnabled(true);
        checkButton.setEnabled(true);
        nextButton.setEnabled(false);
        
        showCurrentVokabel();
    }

    private void showCurrentVokabel() {
        if (currentIndex < currentVokabeln.size()) {
            Vokabel vokabel = currentVokabeln.get(currentIndex);
            
            // Modus bestimmen
            int mode = modeComboBox.getSelectedIndex();
            if (mode == 2) { // Gemischt
                reverseMode = Math.random() < 0.5;
            } else {
                reverseMode = (mode == 1);
            }
            
            if (reverseMode) {
                questionLabel.setText("🔄 Übersetze: " + vokabel.getTranslation());
            } else {
                questionLabel.setText("📖 Übersetze: " + vokabel.getOriginal());
            }
            
            answerField.setText("");
            answerField.setEnabled(true);
            answerField.requestFocus();
            resultLabel.setText(" ");
            resultLabel.setForeground(Color.BLACK);
            checkButton.setEnabled(true);
            nextButton.setEnabled(false);
            updateScore();
        } else {
            finishQuiz();
        }
    }

    private void checkAnswer() {
        if (currentIndex >= currentVokabeln.size()) return;

        Vokabel vokabel = currentVokabeln.get(currentIndex);
        String userAnswer = answerField.getText().trim();
        String correctAnswer = reverseMode ? vokabel.getOriginal() : vokabel.getTranslation();

        totalCount++;
        
        if (userAnswer.equalsIgnoreCase(correctAnswer)) {
            correctCount++;
            resultLabel.setText("✓ Richtig!");
            resultLabel.setForeground(new Color(0, 150, 0));
        } else {
            resultLabel.setText("✗ Falsch! Richtig: " + correctAnswer);
            resultLabel.setForeground(new Color(200, 0, 0));
        }

        answerField.setEnabled(false);
        checkButton.setEnabled(false);
        nextButton.setEnabled(true);
        nextButton.requestFocus();
        updateScore();
    }

    private void nextVokabel() {
        currentIndex++;
        showCurrentVokabel();
    }

    private void updateScore() {
        if (totalCount > 0) {
            int percentage = (int) ((double) correctCount / totalCount * 100);
            scoreLabel.setText(String.format("📊 Score: %d / %d (%d%%)", correctCount, totalCount, percentage));
        } else {
            scoreLabel.setText("Vokabel " + (currentIndex + 1) + " von " + currentVokabeln.size());
        }
    }

    private void finishQuiz() {
        questionLabel.setText("🎉 Quiz beendet!");
        answerField.setEnabled(false);
        checkButton.setEnabled(false);
        nextButton.setEnabled(false);
        
        int percentage = totalCount > 0 ? (int) ((double) correctCount / totalCount * 100) : 0;
        resultLabel.setText(String.format("Endergebnis: %d / %d (%d%%)", correctCount, totalCount, percentage));
        resultLabel.setForeground(new Color(0, 100, 200));

        String message = String.format("Quiz beendet!\n\n✓ Richtig: %d\n✗ Falsch: %d\n📊 Ergebnis: %d%%", 
            correctCount, totalCount - correctCount, percentage);
        JOptionPane.showMessageDialog(this, message, "Quiz beendet", JOptionPane.INFORMATION_MESSAGE);
    }
}
