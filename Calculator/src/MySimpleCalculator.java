import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;

public class MySimpleCalculator {

    private JFrame mainFrame;
    private JTextField displayField;
    private DefaultListModel<String> historyModel;
    private JList<String> historyList;
    private boolean calculationDone = false;
    private boolean isDegreeMode = true;

    public MySimpleCalculator() {
        initUI();
        setupKeyBindings();
    }

    private void initUI() {
        // Step 1: Create Frame
        mainFrame = new JFrame("My Simple Calculator");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(600, 600);
        mainFrame.setLayout(new BorderLayout(10, 10));

        // Step 2: Create Components
        displayField = new JTextField("0");
        displayField.setFont(new Font("Arial", Font.BOLD, 32));
        displayField.setHorizontalAlignment(JTextField.RIGHT);
        displayField.setEditable(true);
        displayField.setCaretColor(Color.BLACK);
        displayField.setFocusable(true);
        displayField.setBackground(new Color(225, 250, 255));
        displayField.setForeground(Color.BLACK);
        displayField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 3),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.add(displayField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(7, 5, 5, 5));

        String[] buttonLabels = {
                "C", "CE", "B", "(", ")",
                "7", "8", "9", "+", "-",
                "4", "5", "6", "*", "/",
                "1", "2", "3", ".", "(-)",
                "0", "√", "%", "x^y", "n!",
                "sin", "cos", "tan", "cot",
                "ln", "log", "Deg↔Rad", "→", "←", "=",
        };

        for (String label : buttonLabels) {
            if (label.isEmpty()) {
                buttonPanel.add(new JLabel());
            } else {
                buttonPanel.add(createButton(label));
            }
        }

        historyModel = new DefaultListModel<>();
        historyList = new JList<>(historyModel);
        historyList.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane historyScrollPane = new JScrollPane(historyList);
        historyScrollPane.setPreferredSize(new Dimension(200, 0));

        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel historyLabel = new JLabel("Calculation History");
        historyLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        historyPanel.add(historyLabel, BorderLayout.NORTH);
        historyPanel.add(historyScrollPane, BorderLayout.CENTER);

        // Step 3: Add to frame
        mainFrame.add(displayPanel, BorderLayout.NORTH);
        mainFrame.add(buttonPanel, BorderLayout.CENTER);
        mainFrame.add(historyPanel, BorderLayout.EAST);
        mainFrame.setVisible(true);

        // Step 4: Focus display field after UI is visible
        SwingUtilities.invokeLater(() -> displayField.requestFocusInWindow());
    }

    private JButton createButton(String label) {
        JButton button = new JButton(label);
        button.setFont(new Font("Tahoma", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(60, 60));
        button.setBackground(label.equals("C") || label.equals("CE") || label.equals("B")
                ? new Color(255, 160, 122) : new Color(230, 230, 230));
        button.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));
        button.addActionListener(e -> handleButtonEvent(label));
        return button;
    }

    private void toggleAngleMode() {
        isDegreeMode = !isDegreeMode;
        JOptionPane.showMessageDialog(mainFrame, "Chế độ góc: " + (isDegreeMode ? "Độ" : "Radian"));
    }

    private void handleButtonEvent(String label) {
        if (calculationDone && Character.isDigit(label.charAt(0))) {
            displayField.setText("");
            calculationDone = false;
        }

        switch (label) {
            case "C" -> displayField.setText("0");
            case "CE" -> clearCurrentEntry();
            case "←" -> moveCaretLeft();
            case "→" -> moveCaretRight();
            case "B" -> deleteLastChar();
            case "=" -> performCalculation();
            case "(-)" -> toggleSign();
            case "√" -> appendToDisplay("√");
            case "x^y" -> appendToDisplay("^");
            case "sin", "cos", "tan", "cot", "ln", "log" -> appendToDisplay(label + "(");
            case "n!" -> appendToDisplay("!");
            case "Deg↔Rad" -> toggleAngleMode();
            default -> appendToDisplay(label);
        }
    }

    private void moveCaretLeft() {
        int pos = displayField.getCaretPosition();
        if (pos > 0) {
            displayField.requestFocusInWindow();
            displayField.setCaretPosition(pos - 1);
        }
    }

    private void moveCaretRight() {
        int pos = displayField.getCaretPosition();
        if (pos < displayField.getText().length()) {
            displayField.requestFocusInWindow();
            displayField.setCaretPosition(pos + 1);
        }
    }

    private void clearCurrentEntry() {
        String text = displayField.getText();
        int pos = displayField.getCaretPosition();
        int left = pos - 1;

        while (left >= 0 && (Character.isDigit(text.charAt(left)) || text.charAt(left) == '.')) {
            left--;
        }

        int right = pos;
        while (right < text.length() && (Character.isDigit(text.charAt(right)) || text.charAt(right) == '.')) {
            right++;
        }

        String newText = text.substring(0, left + 1) + text.substring(right);
        displayField.setText(newText.isEmpty() ? "0" : newText);
        displayField.setCaretPosition(Math.min(left + 1, newText.length()));
        displayField.requestFocusInWindow();
    }

    private void deleteLastChar() {
        int pos = displayField.getCaretPosition();
        String text = displayField.getText();
        if (pos > 0 && !text.isEmpty()) {
            displayField.setText(text.substring(0, pos - 1) + text.substring(pos));
            displayField.setCaretPosition(pos - 1);
        }
        displayField.requestFocusInWindow();
    }

    private void toggleSign() {
        String currentText = displayField.getText();
        if (currentText.equals("0") || currentText.isEmpty()) {
            displayField.setText("-");
        } else if (currentText.startsWith("-")) {
            displayField.setText(currentText.substring(1));
        } else {
            displayField.setText("-" + currentText);
        }
    }

    private void appendToDisplay(String text) {
        if (calculationDone) {
            displayField.setText("");
            calculationDone = false;
        }

        int pos = displayField.getCaretPosition();
        String currentText = displayField.getText();

        if (currentText.equals("0")) {
            displayField.setText(text);
            displayField.setCaretPosition(text.length());
        } else {
            String newText = currentText.substring(0, pos) + text + currentText.substring(pos);
            displayField.setText(newText);
            displayField.setCaretPosition(pos + text.length());
        }
        displayField.requestFocusInWindow();
    }

    private void performCalculation() {
        try {
            String expression = displayField.getText();
            if (expression.isEmpty()) {
                return;
            }
            validateExpression(expression);
            double result = evaluateExpression(expression);
            checkNumberLimits(result);
            String resultStr = formatResult(result);
            historyModel.addElement(expression + " = " + resultStr);
            displayField.setText(resultStr);
            displayField.setCaretPosition(resultStr.length());
            calculationDone = true;
        } catch (ArithmeticException ex) {
            JOptionPane.showMessageDialog(mainFrame, "Math error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(mainFrame, "Invalid number format: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(mainFrame, "Expression error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double evaluateExpression(String expression) {
        return new ExpressionParser(expression).parse();
    }

    private String formatResult(double value) {
        return (value == (long) value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private void setupKeyBindings() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_TYPED) {
                char c = e.getKeyChar();
                if (Character.isDigit(c) || "+-*/().^%".indexOf(c) != -1) {
                    appendToDisplay(String.valueOf(c));
                    return true;
                } else if (c == '\n') {
                    performCalculation();
                    return true;
                }
            } else if (e.getID() == KeyEvent.KEY_PRESSED) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_BACK_SPACE -> {
                        deleteLastChar();
                        return true;
                    }
                    case KeyEvent.VK_LEFT -> {
                        moveCaretLeft();
                        return true;
                    }
                    case KeyEvent.VK_RIGHT -> {
                        moveCaretRight();
                        return true;
                    }
                }
            }
            return false;
        });
    }

    private class ExpressionParser {

        private final String input;
        private int pos = -1;
        private int currentChar;


        public ExpressionParser(String input) {
            this.input = input;
            nextChar();
        }

        private void nextChar() {
            pos++;
            currentChar = (pos < input.length()) ? input.charAt(pos) : -1;
        }

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

        public double parse() {
            double result = parseExpression();
            if (pos < input.length()) {
                throw new RuntimeException("Unexpected: " + (char) currentChar);
            }
            return result;
        }

        private double parseExpression() {
            double result = parseTerm();
            while (true) {
                if (eat('+')) {
                    result += parseTerm();
                } else if (eat('-')) {
                    result -= parseTerm();
                } else {
                    return result;
                }
            }
        }

        private double parseTerm() {
            double result = parseFactor();
            while (true) {
                if (eat('*')) {
                    result *= parseFactor();
                } else if (eat('/')) {
                    double denom = parseFactor();
                    if (denom == 0) {
                        throw new ArithmeticException("Divide by zero");
                    }
                    result /= denom;
                } else {
                    return result;
                }
            }
        }

        private double parseFactor() {
            if (eat('+')) {
                return parseFactor();
            }
            if (eat('-')) {
                return -parseFactor();
            }

            double result;
            int startPos = pos;

            String func = null;
            if (Character.isLetter(currentChar)) {
                int startFunc = pos;
                while (Character.isLetter(currentChar)) nextChar();
                func = input.substring(startFunc, pos);
            }

            if (func != null) {
                if (!eat('(')) throw new RuntimeException("Missing '(' after " + func);
                double arg = parseExpression();
                if (!eat(')')) throw new RuntimeException("Missing ')'");

                if (isDegreeMode && (func.equals("sin") || func.equals("cos") || func.equals("tan") || func.equals("cot"))) {
                    arg = Math.toRadians(arg);
                }

                switch (func) {
                    case "sin" -> {
                        result = Math.sin(arg);
                        checkNumberLimits(result);
                    }
                    case "cos" -> {
                        result = Math.cos(arg);
                        checkNumberLimits(result);
                    }
                    case "tan" -> {
                        if (Math.abs(Math.cos(arg)) < 1e-10) {
                            throw new ArithmeticException("Unknown value (tan)");
                        }
                        result = Math.tan(arg);
                        checkNumberLimits(result);
                    }
                    case "cot" -> {
                        if (Math.abs(Math.sin(arg)) < 1e-10) {
                            throw new ArithmeticException("Unknown value (cot)");
                        }
                        result = 1.0 / Math.tan(arg);
                        checkNumberLimits(result);
                    }
                    case "log" -> {
                        if (arg <= 0) {
                            throw new ArithmeticException("Invalid logarithm");
                        }
                        result = Math.log10(arg);
                        checkNumberLimits(result);
                    }
                    case "ln" -> {
                        if (arg <= 0) {
                            throw new ArithmeticException("Invalid natural logarithm");
                        }
                        result = Math.log(arg);
                        checkNumberLimits(result);
                    }
                    default -> throw new RuntimeException("Unknown function: " + func);
                }
                return result;
            }

            if (eat('√')) {
                result = eat('(') ? parseExpression() : parseFactor();
                if (result < 0) {
                    throw new RuntimeException("Square root of a negative number is undefined");
                }
                result = Math.sqrt(result);
                if (!eat(')')) {
                    throw new RuntimeException("Missing ')'");
                }
                result = Math.sqrt(result);
                if (eat('%')) {
                    result /= 100;
                }
            } else if (eat('(')) {
                result = parseExpression();
                if (!eat(')')) {
                    throw new RuntimeException("Missing ')'");
                }
            } else if ((currentChar >= '0' && currentChar <= '9') || currentChar == '.') {
                while ((currentChar >= '0' && currentChar <= '9') || currentChar == '.') {
                    nextChar();
                }
                result = Double.parseDouble(input.substring(startPos, pos));
                if (eat('%')) {
                    result /= 100;
                }
            } else {
                throw new RuntimeException("Unexpected: " + (char) currentChar);
            }

            if (eat('^')) {
                double exponent = parseFactor();
                if (result == 0 && exponent == 0) {
                    throw new ArithmeticException("Indeterminate form: 0^0");
                }
                result = Math.pow(result, exponent);
                checkNumberLimits(result);
            }
            if (eat('!')) {
                if (result < 0 || result != Math.floor(result)) {
                    throw new ArithmeticException("Factorial is only defined for non-negative integers");
                }
                result = factorial((int) result);
                checkNumberLimits(result); // Validate result
            }
            return result;
        }

        private double factorial(int n) {
            double res = 1;
            for (int i = 2; i <= n; i++) res *= i;
            return res;
        }
    }

    private void checkNumberLimits(double value) {
        if (value > Double.MAX_VALUE) {
            throw new ArithmeticException("Value exceeding the maximum limit of a real number");
        }
        if (value < -Double.MAX_VALUE) {
            throw new ArithmeticException("Value exceeding the minimum limit of a real number");
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new ArithmeticException("Invalid value (NaN or Infinity)");
        }
    }
    private void validateExpression(String expression) {
        if (expression.contains(" ")) {
            throw new RuntimeException("Expression contains invalid spaces");
        }
    }

    public static void main(String[] args) {
        new MySimpleCalculator();
    }
}
