import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class NumberGuessingGameGUI extends JFrame implements ActionListener {
    
    // Game Variables
    private int secretNumber;
    private int attempts;
    private int maxAttempts;
    private int totalRounds = 0;
    private int totalScore = 0;
    private boolean gameOver = false;
    private String currentDifficulty = "Medium";
    
    // Timer Variables
    private int timeLeft = 30;
    private Timer timer;
    
    // GUI Components
    private JLabel titleLabel, messageLabel, attemptsLabel, scoreLabel, timerLabel;
    private JTextField guessField;
    private JButton guessButton, playAgainButton, newGameButton, exitButton;
    private JComboBox<String> difficultyCombo;
    private JPanel mainPanel, buttonPanel;
    
    public NumberGuessingGameGUI() {
        setTitle("Number Guessing Game");
        setSize(550, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);
        setLocationRelativeTo(null);
        
        initComponents();
        startNewGame();
        setVisible(true);
    }
    
    private void initComponents() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(8, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(new Color(240, 248, 255));
        
        // Difficulty Selection Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        topPanel.setBackground(new Color(240, 248, 255));
        
        JLabel diffLabel = new JLabel("Select Difficulty:");
        diffLabel.setFont(new Font("Arial", Font.BOLD, 14));
        topPanel.add(diffLabel);
        
        String[] levels = {"Easy (1-50, 10 tries)", "Medium (1-100, 7 tries)", "Hard (1-200, 5 tries)"};
        difficultyCombo = new JComboBox<>(levels);
        difficultyCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        difficultyCombo.addActionListener(this);
        topPanel.add(difficultyCombo);
        
        mainPanel.add(topPanel);
        
        // Title
        titleLabel = new JLabel("Number Guessing Game", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 102, 204));
        mainPanel.add(titleLabel);
        
        // Timer Label
        timerLabel = new JLabel("Time Left: 30 seconds", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        timerLabel.setForeground(new Color(0, 153, 76));
        mainPanel.add(timerLabel);
        
        // Message Label
        messageLabel = new JLabel("Guess a number", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        mainPanel.add(messageLabel);
        
        // Input Panel
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.setBackground(new Color(240, 248, 255));
        
        JLabel enterLabel = new JLabel("Enter your guess: ");
        enterLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        inputPanel.add(enterLabel);
        
        guessField = new JTextField(10);
        guessField.setFont(new Font("Arial", Font.PLAIN, 14));
        guessField.addActionListener(this);
        inputPanel.add(guessField);
        
        mainPanel.add(inputPanel);
        
        // Attempts Label
        attemptsLabel = new JLabel("Attempts: 0 / 7", SwingConstants.CENTER);
        attemptsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        mainPanel.add(attemptsLabel);
        
        // Score Label
        scoreLabel = new JLabel("Rounds: 0 | Total Score: 0", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        scoreLabel.setForeground(new Color(0, 102, 204));
        mainPanel.add(scoreLabel);
        
        // Buttons Panel
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(240, 248, 255));
        
        guessButton = new JButton("Guess");
        guessButton.setFont(new Font("Arial", Font.BOLD, 14));
        guessButton.setBackground(new Color(0, 153, 76));
        guessButton.setForeground(Color.WHITE);
        guessButton.addActionListener(this);
        buttonPanel.add(guessButton);
        
        playAgainButton = new JButton("Play Again");
        playAgainButton.setFont(new Font("Arial", Font.BOLD, 14));
        playAgainButton.setBackground(new Color(255, 165, 0));
        playAgainButton.setForeground(Color.WHITE);
        playAgainButton.addActionListener(this);
        playAgainButton.setEnabled(false);
        buttonPanel.add(playAgainButton);
        
        newGameButton = new JButton("New Game");
        newGameButton.setFont(new Font("Arial", Font.BOLD, 14));
        newGameButton.setBackground(new Color(0, 102, 204));
        newGameButton.setForeground(Color.WHITE);
        newGameButton.addActionListener(this);
        buttonPanel.add(newGameButton);
        
        exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Arial", Font.BOLD, 14));
        exitButton.setBackground(new Color(204, 0, 0));
        exitButton.setForeground(Color.WHITE);
        exitButton.addActionListener(this);
        buttonPanel.add(exitButton);
        
        mainPanel.add(buttonPanel);
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void startNewGame() {
        // Get difficulty settings
        int choice = difficultyCombo.getSelectedIndex();
        int range, attempts;
        if (choice == 0) { // Easy
            range = 50;
            attempts = 10;
            currentDifficulty = "Easy";
        } else if (choice == 1) { // Medium
            range = 100;
            attempts = 7;
            currentDifficulty = "Medium";
        } else { // Hard
            range = 200;
            attempts = 5;
            currentDifficulty = "Hard";
        }
        
        maxAttempts = attempts;
        
        // Generate random number
        Random random = new Random();
        secretNumber = random.nextInt(range) + 1;
        this.attempts = 0;
        gameOver = false;
        
        // Reset timer
        timeLeft = 30;
        timerLabel.setText("Time Left: 30 seconds");
        timerLabel.setForeground(new Color(0, 153, 76));
        
        // Enable input
        guessField.setEnabled(true);
        guessField.setEditable(true);
        guessButton.setEnabled(true);
        playAgainButton.setEnabled(false);
        difficultyCombo.setEnabled(false);
        
        // Reset labels
        messageLabel.setText("Guess a number between 1 and " + range);
        messageLabel.setForeground(Color.BLACK);
        attemptsLabel.setText("Attempts: 0 / " + maxAttempts);
        guessField.setText("");
        guessField.requestFocus();
        
        // Start timer
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeLeft--;
                timerLabel.setText("Time Left: " + timeLeft + " seconds");
                
                if (timeLeft <= 10) {
                    timerLabel.setForeground(Color.RED);
                } else if (timeLeft <= 20) {
                    timerLabel.setForeground(new Color(204, 102, 0));
                }
                
                if (timeLeft <= 0) {
                    timer.stop();
                    timerLabel.setText("Time's Up!");
                    timerLabel.setForeground(Color.RED);
                    gameOver = true;
                    guessField.setEnabled(false);
                    guessField.setEditable(false);
                    guessButton.setEnabled(false);
                    playAgainButton.setEnabled(true);
                    difficultyCombo.setEnabled(true);
                    messageLabel.setText("Time's Up! The number was: " + secretNumber);
                    messageLabel.setForeground(Color.RED);
                    
                    JOptionPane.showMessageDialog(NumberGuessingGameGUI.this,
                        "Time's Up!\nThe number was: " + secretNumber,
                        "Time Over!",
                        JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        timer.start();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == guessButton || e.getSource() == guessField) {
            if (!gameOver && timeLeft > 0) {
                makeGuess();
            }
        } else if (e.getSource() == playAgainButton) {
            // Same difficulty, new round
            startNewGame();
        } else if (e.getSource() == newGameButton) {
            // Reset all statistics
            totalRounds = 0;
            totalScore = 0;
            startNewGame();
            scoreLabel.setText("Rounds: 0 | Total Score: 0");
        } else if (e.getSource() == exitButton) {
            System.exit(0);
        } else if (e.getSource() == difficultyCombo) {
            if (gameOver) {
                startNewGame();
            } else {
                int current = difficultyCombo.getSelectedIndex();
                String[] levels = {"Easy (1-50, 10 tries)", "Medium (1-100, 7 tries)", "Hard (1-200, 5 tries)"};
                difficultyCombo.setSelectedIndex(current);
                JOptionPane.showMessageDialog(this,
                    "Finish current round first!\nClick 'Play Again' after game ends.",
                    "Game in Progress",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private void makeGuess() {
        try {
            String input = guessField.getText().trim();
            if (input.isEmpty()) {
                messageLabel.setText("Please enter a number!");
                return;
            }
            
            int guess = Integer.parseInt(input);
            int range = difficultyCombo.getSelectedIndex() == 0 ? 50 : 
                       difficultyCombo.getSelectedIndex() == 1 ? 100 : 200;
            
            if (guess < 1 || guess > range) {
                messageLabel.setText("Please enter a number between 1 and " + range + "!");
                return;
            }
            
            attempts++;
            attemptsLabel.setText("Attempts: " + attempts + " / " + maxAttempts);
            
            if (guess == secretNumber) {
                int score = (maxAttempts - attempts + 1) * 10 + (timeLeft / 2);
                totalScore += score;
                totalRounds++;
                
                messageLabel.setText("Correct! You got it in " + attempts + " attempts!");
                messageLabel.setForeground(new Color(0, 153, 76));
                gameOver = true;
                guessField.setEnabled(false);
                guessField.setEditable(false);
                guessButton.setEnabled(false);
                playAgainButton.setEnabled(true);
                difficultyCombo.setEnabled(true);
                timer.stop();
                scoreLabel.setText("Rounds: " + totalRounds + " | Total Score: " + totalScore);
                
                String info = "Difficulty: " + currentDifficulty + 
                             "\nRange: 1-" + range + 
                             "\nAttempts: " + attempts + "/" + maxAttempts +
                             "\nTime Left: " + timeLeft + " seconds" +
                             "\nScore: " + score + " points";
                
                JOptionPane.showMessageDialog(this, 
                    "Congratulations!\n" + info,
                    "You Won!",
                    JOptionPane.INFORMATION_MESSAGE);
            } else if (guess < secretNumber) {
                messageLabel.setText("Too Low! Try again.");
                messageLabel.setForeground(new Color(204, 102, 0));
            } else {
                messageLabel.setText("Too High! Try again.");
                messageLabel.setForeground(new Color(204, 0, 0));
            }
            
            if (attempts >= maxAttempts && !gameOver) {
                messageLabel.setText("Game Over! The number was: " + secretNumber);
                messageLabel.setForeground(Color.RED);
                gameOver = true;
                guessField.setEnabled(false);
                guessField.setEditable(false);
                guessButton.setEnabled(false);
                playAgainButton.setEnabled(true);
                difficultyCombo.setEnabled(true);
                timer.stop();
                
                JOptionPane.showMessageDialog(this,
                    "Game Over!\nThe number was: " + secretNumber +
                    "\nDifficulty: " + currentDifficulty +
                    "\nAttempts used: " + attempts + "/" + maxAttempts,
                    "Game Over",
                    JOptionPane.ERROR_MESSAGE);
            }
            
            guessField.setText("");
            guessField.requestFocus();
            
        } catch (NumberFormatException ex) {
            messageLabel.setText("Invalid input! Please enter a number.");
            guessField.setText("");
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new NumberGuessingGameGUI();
            }
        });
    }
}