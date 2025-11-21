import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

/**
 * 🚀 Vokabeltrainer Pro - Modern UI Edition
 * Grok-Style Dark Theme
 */
public class Vokabeltrainer_Modern {
    // Modern Color Scheme
    public static final Color BG_DARK = new Color(15, 23, 42);
    public static final Color BG_DARKER = new Color(8, 14, 28);
    public static final Color BG_CARD = new Color(30, 41, 59);
    public static final Color ACCENT_BLUE = new Color(59, 130, 246);
    public static final Color ACCENT_PURPLE = new Color(147, 51, 234);
    public static final Color ACCENT_GREEN = new Color(34, 197, 94);
    public static final Color ACCENT_RED = new Color(239, 68, 68);
    public static final Color ACCENT_ORANGE = new Color(251, 146, 60);
    public static final Color TEXT_PRIMARY = new Color(248, 250, 252);
    public static final Color TEXT_SECONDARY = new Color(148, 163, 184);
    public static final Color BORDER_COLOR = new Color(51, 65, 85);
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            VokabeltrainerApp app = new VokabeltrainerApp();
            app.setVisible(true);
        });
    }
    
    public static JButton createModernButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(Math.max(button.getPreferredSize().width + 25, 120), 40));
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color original = bgColor;
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(original);
            }
        });
        
        return button;
    }
    
    public static JTextField createModernTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(BG_DARKER);
        field.setCaretColor(ACCENT_BLUE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        return field;
    }
    
    public static <T> JComboBox<T> createModernComboBox() {
        JComboBox<T> combo = new JComboBox<>();
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setForeground(TEXT_PRIMARY);
        combo.setBackground(BG_CARD);
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return combo;
    }
    
    public static JLabel createLabel(String text, int fontSize, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, fontSize));
        label.setForeground(color);
        return label;
    }
}

class VokabeltrainerApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private ManageSetPanel manageSetPanel;
    private QuizPanel quizPanel;
    private DatabaseManager dbManager;

    public VokabeltrainerApp() {
        dbManager = new DatabaseManager();
        dbManager.initialize();

        setTitle("🚀 Vokabeltrainer Pro");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Vokabeltrainer_Modern.BG_DARK);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(Vokabeltrainer_Modern.BG_DARK);

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

class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:vokabeltrainer.db";
    private Connection conn;

    public void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(DB_URL);
            
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS vokabel_sets (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS vokabeln (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, set_id INTEGER NOT NULL, " +
                    "original TEXT NOT NULL, translation TEXT NOT NULL, " +
                    "FOREIGN KEY (set_id) REFERENCES vokabel_sets(id) ON DELETE CASCADE)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createSet(String name) throws SQLException {
        String sql = "INSERT INTO vokabel_sets (name) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        }
    }

    public List<VokabelSet> getAllSets() {
        List<VokabelSet> sets = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name FROM vokabel_sets ORDER BY name")) {
            while (rs.next()) {
                sets.add(new VokabelSet(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sets;
    }

    public void deleteSet(int setId) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM vokabel_sets WHERE id = ?")) {
            pstmt.setInt(1, setId);
            pstmt.executeUpdate();
        }
    }

    public void addVokabel(int setId, String original, String translation) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO vokabeln (set_id, original, translation) VALUES (?, ?, ?)")) {
            pstmt.setInt(1, setId);
            pstmt.setString(2, original);
            pstmt.setString(3, translation);
            pstmt.executeUpdate();
        }
    }

    public List<Vokabel> getVokabelnBySet(int setId) {
        List<Vokabel> vokabeln = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT id, original, translation FROM vokabeln WHERE set_id = ?")) {
            pstmt.setInt(1, setId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                vokabeln.add(new Vokabel(rs.getInt("id"), rs.getString("original"), rs.getString("translation")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vokabeln;
    }

    public void deleteVokabel(int vokabelId) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM vokabeln WHERE id = ?")) {
            pstmt.setInt(1, vokabelId);
            pstmt.executeUpdate();
        }
    }
}

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

class ManageSetPanel extends JPanel {
    private DatabaseManager dbManager;
    private VokabeltrainerApp app;
    private JComboBox<VokabelSet> setComboBox;
    private JTable vokabelTable;
    private DefaultTableModel tableModel;

    public ManageSetPanel(DatabaseManager dbManager, VokabeltrainerApp app) {
        this.dbManager = dbManager;
        this.app = app;
        
        setLayout(new BorderLayout(20, 20));
        setBackground(Vokabeltrainer_Modern.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(15, 15));
        headerPanel.setBackground(Vokabeltrainer_Modern.BG_CARD);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Vokabeltrainer_Modern.ACCENT_BLUE, 3),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        
        JLabel titleLabel = new JLabel("📚 Vokabelsets verwalten");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Vokabeltrainer_Modern.TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton switchButton = Vokabeltrainer_Modern.createModernButton("🎯 Zum Quiz", Vokabeltrainer_Modern.ACCENT_PURPLE);
        switchButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        switchButton.addActionListener(e -> app.showQuizPanel());
        headerPanel.add(switchButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Main Card
        JPanel cardPanel = new JPanel(new BorderLayout(15, 15));
        cardPanel.setBackground(Vokabeltrainer_Modern.BG_CARD);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Vokabeltrainer_Modern.BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        controlPanel.setBackground(Vokabeltrainer_Modern.BG_CARD);
        
        JLabel setLabel = new JLabel("Set:");
        setLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        setLabel.setForeground(Vokabeltrainer_Modern.TEXT_SECONDARY);
        controlPanel.add(setLabel);
        
        setComboBox = Vokabeltrainer_Modern.createModernComboBox();
        setComboBox.setPreferredSize(new Dimension(280, 42));
        setComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        setComboBox.addActionListener(e -> loadVokabeln());
        controlPanel.add(setComboBox);

        controlPanel.add(Box.createHorizontalStrut(10));

        JButton newSetButton = Vokabeltrainer_Modern.createModernButton("➕ Neues Set", Vokabeltrainer_Modern.ACCENT_BLUE);
        newSetButton.addActionListener(e -> createNewSet());
        controlPanel.add(newSetButton);

        JButton deleteSetButton = Vokabeltrainer_Modern.createModernButton("🗑 Set löschen", Vokabeltrainer_Modern.ACCENT_RED);
        deleteSetButton.addActionListener(e -> deleteCurrentSet());
        controlPanel.add(deleteSetButton);

        cardPanel.add(controlPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "Original", "Übersetzung"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        vokabelTable = new JTable(tableModel);
        vokabelTable.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        vokabelTable.setRowHeight(40);
        vokabelTable.setBackground(Vokabeltrainer_Modern.BG_DARKER);
        vokabelTable.setForeground(Vokabeltrainer_Modern.TEXT_PRIMARY);
        vokabelTable.setSelectionBackground(Vokabeltrainer_Modern.ACCENT_BLUE);
        vokabelTable.setSelectionForeground(Color.WHITE);
        vokabelTable.setGridColor(Vokabeltrainer_Modern.BORDER_COLOR);
        vokabelTable.setShowGrid(true);
        vokabelTable.setIntercellSpacing(new Dimension(1, 1));
        vokabelTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        vokabelTable.getColumnModel().getColumn(0).setMaxWidth(80);
        
        JTableHeader header = vokabelTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(Vokabeltrainer_Modern.BG_CARD);
        header.setForeground(Vokabeltrainer_Modern.ACCENT_BLUE);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Vokabeltrainer_Modern.ACCENT_BLUE));
        
        JScrollPane scrollPane = new JScrollPane(vokabelTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Vokabeltrainer_Modern.BORDER_COLOR, 2));
        scrollPane.getViewport().setBackground(Vokabeltrainer_Modern.BG_DARKER);
        
        cardPanel.add(scrollPane, BorderLayout.CENTER);
        add(cardPanel, BorderLayout.CENTER);

        // Bottom Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonPanel.setBackground(Vokabeltrainer_Modern.BG_DARK);
        
        JButton addButton = Vokabeltrainer_Modern.createModernButton("➕ Vokabel hinzufügen", Vokabeltrainer_Modern.ACCENT_GREEN);
        addButton.addActionListener(e -> addVokabel());
        buttonPanel.add(addButton);

        JButton deleteButton = Vokabeltrainer_Modern.createModernButton("🗑 Vokabel löschen", Vokabeltrainer_Modern.ACCENT_ORANGE);
        deleteButton.addActionListener(e -> deleteSelectedVokabel());
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void refreshSets() {
        setComboBox.removeAllItems();
        for (VokabelSet set : dbManager.getAllSets()) {
            setComboBox.addItem(set);
        }
        if (setComboBox.getItemCount() > 0) loadVokabeln();
    }

    private void createNewSet() {
        JTextField nameField = Vokabeltrainer_Modern.createModernTextField();
        nameField.setPreferredSize(new Dimension(300, 45));
        
        Object[] message = {
            Vokabeltrainer_Modern.createLabel("Name des neuen Sets:", 14, Vokabeltrainer_Modern.TEXT_PRIMARY),
            nameField
        };
        
        int option = JOptionPane.showConfirmDialog(this, message, "Neues Set erstellen", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION && !nameField.getText().trim().isEmpty()) {
            try {
                dbManager.createSet(nameField.getText().trim());
                refreshSets();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Fehler: " + e.getMessage());
            }
        }
    }

    private void deleteCurrentSet() {
        VokabelSet set = (VokabelSet) setComboBox.getSelectedItem();
        if (set != null && JOptionPane.showConfirmDialog(this, 
                "Set '" + set.getName() + "' wirklich löschen?", "Bestätigung", 
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                dbManager.deleteSet(set.getId());
                refreshSets();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Fehler: " + e.getMessage());
            }
        }
    }

    private void loadVokabeln() {
        tableModel.setRowCount(0);
        VokabelSet set = (VokabelSet) setComboBox.getSelectedItem();
        if (set != null) {
            for (Vokabel v : dbManager.getVokabelnBySet(set.getId())) {
                tableModel.addRow(new Object[]{v.getId(), v.getOriginal(), v.getTranslation()});
            }
        }
    }

    private void addVokabel() {
        VokabelSet set = (VokabelSet) setComboBox.getSelectedItem();
        if (set == null) {
            JOptionPane.showMessageDialog(this, "Bitte zuerst ein Set auswählen!");
            return;
        }

        JTextField originalField = Vokabeltrainer_Modern.createModernTextField();
        JTextField translationField = Vokabeltrainer_Modern.createModernTextField();
        originalField.setPreferredSize(new Dimension(300, 45));
        translationField.setPreferredSize(new Dimension(300, 45));
        
        Object[] message = {
            Vokabeltrainer_Modern.createLabel("Original:", 14, Vokabeltrainer_Modern.TEXT_PRIMARY), originalField,
            Vokabeltrainer_Modern.createLabel("Übersetzung:", 14, Vokabeltrainer_Modern.TEXT_PRIMARY), translationField
        };

        if (JOptionPane.showConfirmDialog(this, message, "Neue Vokabel", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String original = originalField.getText().trim();
            String translation = translationField.getText().trim();
            if (!original.isEmpty() && !translation.isEmpty()) {
                try {
                    dbManager.addVokabel(set.getId(), original, translation);
                    loadVokabeln();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Fehler: " + e.getMessage());
                }
            }
        }
    }

    private void deleteSelectedVokabel() {
        int row = vokabelTable.getSelectedRow();
        if (row >= 0 && JOptionPane.showConfirmDialog(this, "Vokabel löschen?", 
                "Bestätigung", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                dbManager.deleteVokabel((int) tableModel.getValueAt(row, 0));
                loadVokabeln();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Fehler: " + e.getMessage());
            }
        }
    }
}

class QuizPanel extends JPanel {
    private DatabaseManager dbManager;
    private VokabeltrainerApp app;
    private JComboBox<VokabelSet> setComboBox;
    private JComboBox<String> modeComboBox;
    private JLabel questionLabel, resultLabel, scoreLabel;
    private JTextField answerField;
    private JButton checkButton, nextButton;

    private List<Vokabel> currentVokabeln;
    private int currentIndex = 0, correctCount = 0, totalCount = 0;
    private boolean reverseMode = false;

    public QuizPanel(DatabaseManager dbManager, VokabeltrainerApp app) {
        this.dbManager = dbManager;
        this.app = app;
        
        setLayout(new BorderLayout(20, 20));
        setBackground(Vokabeltrainer_Modern.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(15, 15));
        headerPanel.setBackground(Vokabeltrainer_Modern.BG_CARD);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Vokabeltrainer_Modern.ACCENT_PURPLE, 3),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        
        JLabel titleLabel = new JLabel("🎯 Quiz-Modus");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Vokabeltrainer_Modern.TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton switchButton = Vokabeltrainer_Modern.createModernButton("📚 Zur Verwaltung", Vokabeltrainer_Modern.ACCENT_BLUE);
        switchButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        switchButton.addActionListener(e -> app.showManagePanel());
        headerPanel.add(switchButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        controlPanel.setBackground(Vokabeltrainer_Modern.BG_CARD);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Vokabeltrainer_Modern.BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        controlPanel.add(Vokabeltrainer_Modern.createLabel("Set:", 15, Vokabeltrainer_Modern.TEXT_SECONDARY));
        setComboBox = Vokabeltrainer_Modern.createModernComboBox();
        setComboBox.setPreferredSize(new Dimension(250, 42));
        setComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        controlPanel.add(setComboBox);

        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(Vokabeltrainer_Modern.createLabel("Modus:", 15, Vokabeltrainer_Modern.TEXT_SECONDARY));
        
        modeComboBox = Vokabeltrainer_Modern.createModernComboBox();
        modeComboBox.addItem("📖 Original → Übersetzung");
        modeComboBox.addItem("🔄 Übersetzung → Original");
        modeComboBox.addItem("🎲 Gemischt");
        modeComboBox.setPreferredSize(new Dimension(250, 42));
        modeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        controlPanel.add(modeComboBox);

        controlPanel.add(Box.createHorizontalStrut(20));
        JButton startButton = Vokabeltrainer_Modern.createModernButton("▶ Quiz starten", Vokabeltrainer_Modern.ACCENT_GREEN);
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        startButton.addActionListener(e -> startQuiz());
        controlPanel.add(startButton);

        // Quiz Card
        JPanel quizCard = new JPanel();
        quizCard.setLayout(new BoxLayout(quizCard, BoxLayout.Y_AXIS));
        quizCard.setBackground(Vokabeltrainer_Modern.BG_CARD);
        quizCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Vokabeltrainer_Modern.BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));

        scoreLabel = new JLabel(" ");
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        scoreLabel.setForeground(Vokabeltrainer_Modern.ACCENT_BLUE);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        quizCard.add(scoreLabel);

        quizCard.add(Box.createVerticalStrut(40));

        questionLabel = new JLabel("Wähle ein Set und starte das Quiz!");
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        questionLabel.setForeground(Vokabeltrainer_Modern.TEXT_PRIMARY);
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        quizCard.add(questionLabel);

        quizCard.add(Box.createVerticalStrut(35));

        answerField = Vokabeltrainer_Modern.createModernTextField();
        answerField.setMaximumSize(new Dimension(500, 50));
        answerField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        answerField.setEnabled(false);
        answerField.addActionListener(e -> checkAnswer());
        answerField.setAlignmentX(Component.CENTER_ALIGNMENT);
        quizCard.add(answerField);

        quizCard.add(Box.createVerticalStrut(25));

        resultLabel = new JLabel(" ");
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        quizCard.add(resultLabel);

        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setBackground(Vokabeltrainer_Modern.BG_DARK);
        centerPanel.add(controlPanel, BorderLayout.NORTH);
        centerPanel.add(quizCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Vokabeltrainer_Modern.BG_DARK);
        
        checkButton = Vokabeltrainer_Modern.createModernButton("✓ Prüfen", Vokabeltrainer_Modern.ACCENT_GREEN);
        checkButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        checkButton.setEnabled(false);
        checkButton.addActionListener(e -> checkAnswer());
        buttonPanel.add(checkButton);

        nextButton = Vokabeltrainer_Modern.createModernButton("➡ Weiter", Vokabeltrainer_Modern.ACCENT_BLUE);
        nextButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nextButton.setEnabled(false);
        nextButton.addActionListener(e -> nextVokabel());
        buttonPanel.add(nextButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void refreshSets() {
        setComboBox.removeAllItems();
        for (VokabelSet set : dbManager.getAllSets()) {
            setComboBox.addItem(set);
        }
    }

    private void startQuiz() {
        VokabelSet set = (VokabelSet) setComboBox.getSelectedItem();
        if (set == null) {
            JOptionPane.showMessageDialog(this, "Bitte ein Set auswählen!");
            return;
        }

        currentVokabeln = dbManager.getVokabelnBySet(set.getId());
        if (currentVokabeln.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Dieses Set enthält keine Vokabeln!");
            return;
        }

        Collections.shuffle(currentVokabeln);
        currentIndex = correctCount = totalCount = 0;
        answerField.setEnabled(true);
        checkButton.setEnabled(true);
        nextButton.setEnabled(false);
        showCurrentVokabel();
    }

    private void showCurrentVokabel() {
        if (currentIndex < currentVokabeln.size()) {
            Vokabel v = currentVokabeln.get(currentIndex);
            int mode = modeComboBox.getSelectedIndex();
            reverseMode = (mode == 2) ? (Math.random() < 0.5) : (mode == 1);
            
            questionLabel.setText(reverseMode ? "🔄 " + v.getTranslation() : "📖 " + v.getOriginal());
            answerField.setText("");
            answerField.setEnabled(true);
            answerField.requestFocus();
            resultLabel.setText(" ");
            checkButton.setEnabled(true);
            nextButton.setEnabled(false);
            updateScore();
        } else {
            finishQuiz();
        }
    }

    private void checkAnswer() {
        if (currentIndex >= currentVokabeln.size()) return;

        Vokabel v = currentVokabeln.get(currentIndex);
        String correct = reverseMode ? v.getOriginal() : v.getTranslation();
        totalCount++;
        
        if (answerField.getText().trim().equalsIgnoreCase(correct)) {
            correctCount++;
            resultLabel.setText("✓ Richtig!");
            resultLabel.setForeground(Vokabeltrainer_Modern.ACCENT_GREEN);
        } else {
            resultLabel.setText("✗ Falsch! Richtig: " + correct);
            resultLabel.setForeground(Vokabeltrainer_Modern.ACCENT_RED);
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
            int pct = (int) ((double) correctCount / totalCount * 100);
            scoreLabel.setText(String.format("📊 Score: %d / %d (%d%%)", correctCount, totalCount, pct));
        } else {
            scoreLabel.setText("Vokabel " + (currentIndex + 1) + " von " + currentVokabeln.size());
        }
    }

    private void finishQuiz() {
        questionLabel.setText("🎉 Quiz beendet!");
        answerField.setEnabled(false);
        checkButton.setEnabled(false);
        nextButton.setEnabled(false);
        
        int pct = totalCount > 0 ? (int) ((double) correctCount / totalCount * 100) : 0;
        resultLabel.setText(String.format("Endergebnis: %d / %d (%d%%)", correctCount, totalCount, pct));
        resultLabel.setForeground(Vokabeltrainer_Modern.ACCENT_PURPLE);

        JOptionPane.showMessageDialog(this, 
            String.format("Quiz beendet!\n\n✓ Richtig: %d\n✗ Falsch: %d\n📊 Ergebnis: %d%%", 
                correctCount, totalCount - correctCount, pct));
    }
}
