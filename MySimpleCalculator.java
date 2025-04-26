
import java.awt.*;                      // Import AWT classes for GUI components
import java.awt.event.*;                // Import classes for event handling (keyboard, mouse, etc.)
import javax.swing.*;                   // Import Swing classes for building the GUI
import javax.swing.border.EmptyBorder; // Import EmptyBorder to add empty space around components

/**
 * SimpleCalculator simulates a basic calculator with a GUI. This code follows
 * clean code principles with clear variable and method names.
 */
public class MySimpleCalculator {

    // GUI component declarations
    private JFrame mainFrame;                 // The main application window
    private JTextField displayField;          // Text field to show expressions and results
    private DefaultListModel<String> historyModel; // Model for storing calculation history
    private JList<String> historyList;        // List to display the calculation history
    private boolean calculationDone = false;  // Flag to indicate if a calculation was just performed

    /**
     * Constructor: Initializes the calculator by setting up the GUI and key
     * bindings.
     */
    public MySimpleCalculator() {
        setupUI();          // Set up the user interface
        setupKeyBindings(); // Set up keyboard shortcuts
    }

    /**
     * Sets up the user interface. Creates the main frame, display area, button
     * panel, and history panel.
     */
    private void setupUI() {
        // Create the main frame with a title and set it to exit when closed
        mainFrame = new JFrame("My Simple Calculator");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(600, 600); // Set the window size
        mainFrame.setLayout(new BorderLayout(10, 10)); // Use BorderLayout with 10-pixel gaps

        // --- Create the display field ---
        displayField = new JTextField("0");               // Initialize with "0"
        displayField.setFont(new Font("Arial", Font.BOLD, 32)); // Set a large, bold font
        displayField.setHorizontalAlignment(JTextField.RIGHT);  // Align text to the right
        displayField.setEditable(false);                  // Make the display non-editable
        displayField.setBackground(new Color(225, 250, 255)); // Set a light blue background
        displayField.setForeground(Color.BLACK);          // Set text color to black
        // Set a compound border with a colored line and padding
        displayField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 3),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.add(displayField, BorderLayout.CENTER);

        // --- Create the button panel ---
        // Use a GridLayout with 6 rows and 4 columns with 5-pixel gaps
        JPanel buttonPanel = new JPanel(new GridLayout(6, 4, 5, 5));
        // Array of button labels (numbers, operators, and special functions)
        String[] buttonLabels = {
            "C", "(", ")", "B", // "C": clear, "B": backspace
            "7", "8", "9", "/",
            "4", "5", "6", "*",
            "1", "2", "3", "-",
            "0", ".", "=", "+",
            "x^y", "√", "%", "(-)" // "x^y": exponentiation, "√": square root, "(-)": toggle sign
        };
        // Loop through each label, create a button, and add it to the panel
        for (String label : buttonLabels) {
            buttonPanel.add(createButton(label));
        }

        // --- Create the history panel ---
        historyModel = new DefaultListModel<>();          // Create a model for history
        historyList = new JList<>(historyModel);            // Create a list to display the history
        historyList.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane historyScrollPane = new JScrollPane(historyList); // Put the list in a scroll pane
        historyScrollPane.setPreferredSize(new Dimension(200, 0));    // Set preferred width
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel historyLabel = new JLabel("Calculation History");
        historyLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        historyPanel.add(historyLabel, BorderLayout.NORTH);
        historyPanel.add(historyScrollPane, BorderLayout.CENTER);

        // --- Add all panels to the main frame ---
        mainFrame.add(displayPanel, BorderLayout.NORTH);
        mainFrame.add(buttonPanel, BorderLayout.CENTER);
        mainFrame.add(historyPanel, BorderLayout.EAST);

        mainFrame.setVisible(true); // Display the main frame
    }

    /**
     * Creates a JButton with the specified label. Sets the font, background
     * color, and registers an action listener.
     *
     * @param label The text to display on the button
     * @return A configured JButton object
     */
    private JButton createButton(String label) {
        JButton button = new JButton(label);         // Create a new button with the given label
        button.setFont(new Font("Tahoma", Font.BOLD, 16)); // Set the button's font
        button.setFocusPainted(false);                 // Disable focus painting
        button.setPreferredSize(new Dimension(60, 60));
        button.setBackground(new Color(230, 230, 230));  // Set a light gray background
        button.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));
        if (label.equals("C") || label.equals("B")) {
            button.setBackground(new Color(255, 160, 122));
        } else {
            button.setBackground(new Color(230, 230, 230));
        }
        // Register an action listener to handle button clicks
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleButtonEvent(label);            // Handle the event based on the button label
            }
        });
        return button;                                 // Return the created button
    }

    /**
     * Handles button click events. For the "(-)" button, toggles the sign of
     * the current value. Now, if the display is "0" or empty, pressing "(-)"
     * will insert a "-" so that the user can type the negative number.
     *
     * @param label The label of the button that was pressed
     */
    private void handleButtonEvent(String label) {
        // If a calculation was done and the user presses a digit, clear the display for new input
        if (calculationDone && Character.isDigit(label.charAt(0))) {
            displayField.setText("");
            calculationDone = false;
        }
        // Process the button based on its label
        if (label.equals("C")) {
            displayField.setText("0");               // Clear the display
        } else if (label.equals("B")) {
            deleteLastChar();                        // Delete the last character
        } else if (label.equals("=")) {
            performCalculation();                    // Calculate the expression
        } else if (label.equals("(-)")) {
            // Updated behavior: if the display is "0" or empty, insert "-" to allow negative input
            String currentText = displayField.getText();
            if (currentText.equals("0") || currentText.isEmpty()) {
                displayField.setText("-");
            } else {
                // Otherwise, toggle the sign of the current number
                if (currentText.startsWith("-")) {
                    displayField.setText(currentText.substring(1));
                } else {
                    displayField.setText("-" + currentText);
                }
            }
        } else if (label.equals("√")) {
            appendToDisplay("√");                    // Append the square root symbol
        } else if (label.equals("x^y")) {
            appendToDisplay("^");                    // Append the exponentiation symbol
        } else {
            appendToDisplay(label);                  // Append the button label (numbers/operators)
        }
    }

    /**
     * Sets up keyboard shortcuts using the KeyboardFocusManager.
     */
    private void setupKeyBindings() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            public boolean dispatchKeyEvent(KeyEvent e) {
                // Process key typed events
                if (e.getID() == KeyEvent.KEY_TYPED) {
                    char c = e.getKeyChar();         // Get the typed character
                    // If the character is a digit or a valid operator, append it to the display
                    if (Character.isDigit(c) || "+-*/().^%".indexOf(c) != -1) {
                        appendToDisplay(String.valueOf(c));
                        return true;
                    } else if (c == '\n') {          // If Enter is pressed
                        performCalculation();        // Calculate the expression
                        return true;
                    }
                } else if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    // If Backspace is pressed, delete the last character
                    deleteLastChar();
                    return true;
                }
                return false; // For unhandled keys, return false
            }
        });
    }

    /**
     * Appends the given text to the display field.
     *
     * @param text The text to append
     */
    private void appendToDisplay(String text) {
        // If a calculation was just performed, clear the display first
        if (calculationDone) {
            displayField.setText("");
            calculationDone = false;
        }
        // If the display currently shows "0", replace it; otherwise, append the text
        if (displayField.getText().equals("0")) {
            displayField.setText(text);
        } else {
            displayField.setText(displayField.getText() + text);
        }
    }

    /**
     * Deletes the last character from the display field.
     */
    private void deleteLastChar() {
        String currentText = displayField.getText(); // Get the current display text
        if (currentText.length() > 1) {
            displayField.setText(currentText.substring(0, currentText.length() - 1));
        } else {
            displayField.setText("0");
        }
    }

    /**
     * Performs the calculation by reading the expression from the display,
     * evaluating it, and then updating the display and history.
     */
    private void performCalculation() {
        try {
            String expression = displayField.getText(); // Get the expression from the display
            if (expression.isEmpty()) {
                return;
            }
            double result = evaluateExpression(expression); // Evaluate the expression using the parser
            String resultStr = formatResult(result);          // Format the result for display
            historyModel.addElement(expression + " = " + resultStr); // Add the calculation to history
            displayField.setText(resultStr);   // Update the display with the result
            calculationDone = true;            // Mark that a calculation was completed
        } catch (ArithmeticException ex) {
            // Handle division by zero
            if ("Divide by zero".equals(ex.getMessage())) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Cannot divide by zero!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(mainFrame,
                        "Arithmetic error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (RuntimeException ex) {
            // Handle any other errors (like invalid expressions)
            JOptionPane.showMessageDialog(mainFrame,
                    "Invalid expression!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Evaluates a mathematical expression using a recursive descent parser.
     *
     * @param expression The expression as a string
     * @return The result of the evaluation
     */
    private double evaluateExpression(String expression) {
        ExpressionParser parser = new ExpressionParser(expression);
        return parser.parse();
    }

    /**
     * Formats the result so that if the result is an integer, the decimal part
     * is not shown.
     *
     * @param value The value to format
     * @return A string representation of the result
     */
    private String formatResult(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        } else {
            return String.valueOf(value);
        }
    }

    /**
     * ExpressionParser uses recursive descent parsing to evaluate mathematical
     * expressions. Supported operators: +, -, *, /, exponentiation (^), square
     * root (√), and percentage (%).
     */
    private class ExpressionParser {

        private final String input; // The input expression
        private int pos = -1;       // Current position in the input string
        private int currentChar;    // Current character as an ASCII value

        /**
         * Constructor: Initializes the parser with the given expression.
         *
         * @param input The mathematical expression as a string
         */
        public ExpressionParser(String input) {
            this.input = input;
            nextChar(); // Move to the first character
        }

        /**
         * Advances to the next character in the input.
         */
        private void nextChar() {
            pos++;
            currentChar = (pos < input.length()) ? input.charAt(pos) : -1;
        }

        /**
         * Consumes the current character if it matches the expected character.
         *
         * @param charToEat The character we expect
         * @return true if the character was consumed, false otherwise
         */
        private boolean eat(int charToEat) {
            while (currentChar == ' ') {
                nextChar();
            }
            if (currentChar == charToEat) {
                nextChar();
                return true;
            }
            return false;
        }

        /**
         * Parses the full expression and returns its value.
         *
         * @return The evaluated result of the expression
         */
        public double parse() {
            double result = parseExpression();
            if (pos < input.length()) {
                throw new RuntimeException("Unexpected character: " + (char) currentChar);
            }
            return result;
        }

        /**
         * Parses an expression. Expression = Term { ('+' | '-') Term }
         *
         * @return The evaluated value of the expression
         */
        private double parseExpression() {
            double result = parseTerm();
            while (true) {
                if (eat('+')) {
                    result += parseTerm(); // Addition
                } else if (eat('-')) {
                    result -= parseTerm(); // Subtraction
                } else {
                    return result;
                }
            }
        }

        /**
         * Parses a term. Term = Factor { ('*' | '/') Factor }
         *
         * @return The evaluated value of the term
         */
        private double parseTerm() {
            double result = parseFactor();
            while (true) {
                if (eat('*')) {
                    result *= parseFactor(); // Multiplication
                } else if (eat('/')) {
                    double denominator = parseFactor(); // Division
                    if (denominator == 0.0) {
                        throw new ArithmeticException("Divide by zero");
                    }
                    result /= denominator;
                } else {
                    return result;
                }
            }
        }

        /**
         * Parses a factor. Factor = (unary operators) { '^' Factor } Supports
         * numbers, parentheses, square root (√), and percentage (%).
         *
         * @return The evaluated value of the factor
         */
        private double parseFactor() {
            // Handle unary plus and minus
            if (eat('+')) {
                return parseFactor();
            }
            if (eat('-')) {
                return -parseFactor();
            }

            double result;
            int startPos = pos; // Remember start position for numbers

            // Handle square root: symbol '√'
            if (eat('√')) {
                if (eat('(')) {
                    result = parseExpression();
                    if (!eat(')')) {
                        throw new RuntimeException("Missing ')' after square root");
                    }
                } else {
                    result = parseFactor();
                }
                result = Math.sqrt(result);
                if (eat('%')) {
                    result /= 100.0;
                }
            } // Handle parentheses
            else if (eat('(')) {
                result = parseExpression();
                if (!eat(')')) {
                    throw new RuntimeException("Missing ')'");
                }
            } // Handle numbers
            else if ((currentChar >= '0' && currentChar <= '9') || currentChar == '.') {
                while ((currentChar >= '0' && currentChar <= '9') || currentChar == '.') {
                    nextChar();
                }
                String numberStr = input.substring(startPos, pos);
                result = Double.parseDouble(numberStr);
                if (eat('%')) {
                    result /= 100.0;
                }
            } else {
                throw new RuntimeException("Unexpected character: " + (char) currentChar);
            }

            // Handle exponentiation '^'
            if (eat('^')) {
                result = Math.pow(result, parseFactor());
            }
            return result;
        }
    }

    /**
     * The main method to start the application.
     */
    public static void main(String[] args) {
        new MySimpleCalculator(); // Create an instance to launch the calculator
    }
}
