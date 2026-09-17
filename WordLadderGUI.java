import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;

public class WordLadderGUI extends JFrame {
    private JTextField startField;
    private JTextField endField;
    private LadderPanel visualizationPanel;
    private JLabel statusLabel;
    private Set<String> dictionary;
    private JScrollPane scrollPane;

    // Dashboard Labels
    private JLabel dictSizeVal;
    private JLabel timeVal;
    private JLabel exploredVal;
    private JLabel stepsVal;

    public WordLadderGUI() {
        dictionary = loadDictionary("dictionary.txt");

        setTitle("Word Ladder Puzzle Solver");
        setSize(900, 600); // Made the window wider to fit the new sidebar
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0)); // Removed gap for flush sidebar
        setContentPane(mainPanel);

        // --- LEFT AREA (Input + Ladder) ---
        JPanel leftArea = new JPanel(new BorderLayout(15, 15));
        leftArea.setBackground(new Color(245, 247, 250));
        leftArea.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. Top Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 15));
        inputPanel.setOpaque(false);
        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        inputPanel.add(createStyledLabel("Start Word (Bottom):", labelFont));
        startField = createStyledTextField(fieldFont);
        inputPanel.add(startField);

        inputPanel.add(createStyledLabel("Target Word (Top):", labelFont));
        endField = createStyledTextField(fieldFont);
        inputPanel.add(endField);

        ActionListener enterKeyListener = e -> runSolver();
        startField.addActionListener(enterKeyListener);
        endField.addActionListener(enterKeyListener);
        leftArea.add(inputPanel, BorderLayout.NORTH);

        // 2. Center Visualization Area
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setOpaque(false);

        statusLabel = new JLabel("Enter words to build the ladder...");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        centerContainer.add(statusLabel, BorderLayout.NORTH);

        visualizationPanel = new LadderPanel();
        scrollPane = new JScrollPane(visualizationPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scrollPane.getViewport().setBackground(new Color(250, 252, 255));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        centerContainer.add(scrollPane, BorderLayout.CENTER);
        leftArea.add(centerContainer, BorderLayout.CENTER);

        // 3. Bottom Button
        JButton solveButton = new JButton("Build Ladder");
        solveButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        solveButton.setBackground(new Color(74, 144, 226));
        solveButton.setForeground(Color.WHITE);
        solveButton.setFocusPainted(false);
        solveButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        solveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        solveButton.addActionListener(e -> runSolver());
        leftArea.add(solveButton, BorderLayout.SOUTH);

        mainPanel.add(leftArea, BorderLayout.CENTER);

        // --- RIGHT AREA (Algorithm Dashboard) ---
        mainPanel.add(createDashboard(), BorderLayout.EAST);
    }

    private JPanel createDashboard() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(new Color(40, 44, 52)); // Sleek dark theme
        sidebar.setBorder(new EmptyBorder(25, 20, 20, 20));

        JLabel dashTitle = new JLabel("SEARCH ALYTICS");
        dashTitle.setForeground(new Color(97, 175, 239)); // Neon blue accent
        dashTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        dashTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(dashTitle);
        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));

        // Initialize stat values
        dictSizeVal = createStatValue(String.format("%,d", dictionary.size()));
        timeVal = createStatValue("0 ms");
        exploredVal = createStatValue("0");
        stepsVal = createStatValue("0");

        // Add Stat Blocks
        sidebar.add(createStatBlock("TOTAL DICTIONARY", dictSizeVal));
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(createStatBlock("WORDS EXPLORED (BFS)", exploredVal));
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(createStatBlock("SEARCH DURATION", timeVal));
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(createStatBlock("LADDER RUNGS", stepsVal));

        // Pushes everything to the top
        sidebar.add(Box.createVerticalGlue()); 
        return sidebar;
    }

    private JPanel createStatBlock(String title, JLabel valueLabel) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(171, 178, 191));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(valueLabel);
        return panel;
    }

    private JLabel createStatValue(String initialText) {
        JLabel label = new JLabel(initialText);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Consolas", Font.PLAIN, 22));
        return label;
    }

    private JLabel createStyledLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(new Color(50, 50, 50));
        return label;
    }

    private JTextField createStyledTextField(Font font) {
        JTextField field = new JTextField();
        field.setFont(font);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return field;
    }

    private Set<String> loadDictionary(String filename) {
        Set<String> words = new HashSet<>();
        try {
            Scanner scanner = new Scanner(new File(filename));
            while (scanner.hasNextLine()) {
                String word = scanner.nextLine().trim().toLowerCase();
                if (!word.isEmpty()) words.add(word);
            }
            scanner.close();
        } catch (FileNotFoundException e) {}
        return words;
    }

    private void runSolver() {
        String start = startField.getText().trim().toLowerCase();
        String end = endField.getText().trim().toLowerCase();

        if (start.isEmpty() || end.isEmpty() || start.length() != end.length()) {
            statusLabel.setText("Error: Words must be valid and of the same length.");
            return;
        }

        statusLabel.setText("Searching...");
        statusLabel.paintImmediately(statusLabel.getVisibleRect());

        long startTime = System.currentTimeMillis();
        // NOW RETURNS OUR CUSTOM LadderResult OBJECT
        LadderResult result = WordLadderSolver.findShortestLadder(start, end, dictionary);
        long duration = System.currentTimeMillis() - startTime;

        // --- UPDATE DASHBOARD ---
        timeVal.setText(duration + " ms");
        exploredVal.setText(String.format("%,d", result.wordsExplored)); // Formats with commas
        stepsVal.setText(String.valueOf(result.path.size()));

        if (result.path.isEmpty()) {
            statusLabel.setText("No path found!");
        } else {
            statusLabel.setText("Ladder built successfully! Scroll up to climb.");
        }
        
        visualizationPanel.setPath(result.path);

        if (!result.path.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum()); 
            });
        }
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new WordLadderGUI().setVisible(true));
    }
}

// --- TRUE LADDER GRAPHICS PANEL (Unchanged) ---
class LadderPanel extends JPanel {
    private List<String> path = new ArrayList<>();

    public void setPath(List<String> path) {
        this.path = path;
        setPreferredSize(calculateSize());
        revalidate();
        repaint();
    }

    private Dimension calculateSize() {
        if (path.isEmpty()) return new Dimension(500, 300);
        int height = 60 + (path.size() * 75); 
        return new Dimension(500, Math.max(height, 400));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (path.isEmpty()) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int stepWidth = 160;
        int stepHeight = 45;
        int yOffset = 75; 
        
        int totalWidth = getWidth();
        int totalHeight = calculateSize().height;
        int x = (totalWidth - stepWidth) / 2;

        int railWidth = 12;
        int leftRailX = x + 20;
        int rightRailX = x + stepWidth - 20 - railWidth;
        int topY = totalHeight - 40 - stepHeight - ((path.size() - 1) * yOffset);
        int bottomY = totalHeight - 40 + stepHeight;
        
        g2d.setColor(new Color(144, 164, 174));
        g2d.fillRoundRect(leftRailX, topY, railWidth, bottomY - topY, 8, 8);
        g2d.fillRoundRect(rightRailX, topY, railWidth, bottomY - topY, 8, 8);
        
        for (int i = 0; i < path.size(); i++) {
            int y = totalHeight - 40 - stepHeight - (i * yOffset);

            g2d.setColor(new Color(0, 0, 0, 25));
            g2d.fillRoundRect(x + 3, y + 4, stepWidth, stepHeight, 15, 15);

            if (i == path.size() - 1) {
                g2d.setColor(new Color(255, 152, 0)); 
            } else if (i == 0) {
                g2d.setColor(new Color(76, 175, 80)); 
            } else {
                g2d.setColor(new Color(92, 107, 192)); 
            }
            g2d.fillRoundRect(x, y, stepWidth, stepHeight, 15, 15);

            int circleSize = 26;
            int circleX = x + stepWidth - circleSize - 10;
            int circleY = y + (stepHeight - circleSize) / 2;
            g2d.setColor(Color.WHITE);
            g2d.fillOval(circleX, circleY, circleSize, circleSize);

            g2d.setColor(new Color(50, 50, 50));
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 13));
            String num = String.valueOf(i);
            FontMetrics fmNum = g2d.getFontMetrics();
            int numX = circleX + (circleSize - fmNum.stringWidth(num)) / 2;
            int numY = circleY + ((circleSize - fmNum.getHeight()) / 2) + fmNum.getAscent();
            g2d.drawString(num, numX, numY);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
            String word = path.get(i).toUpperCase();
            FontMetrics fmWord = g2d.getFontMetrics();
            int wordX = x + (stepWidth - 35 - fmWord.stringWidth(word)) / 2;
            int wordY = y + ((stepHeight - fmWord.getHeight()) / 2) + fmWord.getAscent();
            g2d.drawString(word, wordX, wordY);
        }
    }
}

// --- NEW DATA OBJECT ---
class LadderResult {
    public List<String> path;
    public int wordsExplored;
    
    public LadderResult(List<String> path, int wordsExplored) {
        this.path = path;
        this.wordsExplored = wordsExplored;
    }
}

// --- OPTIMIZED BFS ALGORITHM ---
class WordLadderSolver {
    public static LadderResult findShortestLadder(String beginWord, String endWord, Set<String> dictionary) {
        if (!dictionary.contains(endWord)) return new LadderResult(new ArrayList<>(), 0); 
        
        Queue<String> queue = new LinkedList<>();
        queue.add(beginWord);
        Map<String, String> parentMap = new HashMap<>();
        parentMap.put(beginWord, null);
        
        int exploredCount = 0; // We now track exactly how many nodes BFS expands

        while (!queue.isEmpty()) {
            String currentWord = queue.poll();
            exploredCount++; // Increment every time we pull a word off the queue

            if (currentWord.equals(endWord)) {
                List<String> path = new ArrayList<>();
                String curr = endWord;
                while (curr != null) {
                    path.add(curr);
                    curr = parentMap.get(curr);
                }
                Collections.reverse(path);
                return new LadderResult(path, exploredCount); // Return BOTH pieces of data
            }

            char[] wordChars = currentWord.toCharArray();
            for (int i = 0; i < wordChars.length; i++) {
                char originalChar = wordChars[i];
                for (char c = 'a'; c <= 'z'; c++) {
                    if (wordChars[i] == c) continue;
                    wordChars[i] = c;
                    String newWord = new String(wordChars);
                    if (dictionary.contains(newWord) && !parentMap.containsKey(newWord)) {
                        parentMap.put(newWord, currentWord);
                        queue.add(newWord);
                    }
                }
                wordChars[i] = originalChar;
            }
        }
        return new LadderResult(new ArrayList<>(), exploredCount);
    }
}