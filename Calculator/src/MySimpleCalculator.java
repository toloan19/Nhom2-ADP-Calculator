import java.awt.*;                      // Import thư viện AWT cho các thành phần giao diện
import java.awt.event.*;                // Import thư viện xử lý sự kiện (bàn phím, chuột)
import javax.swing.*;                   // Import thư viện Swing để xây dựng giao diện
import javax.swing.border.EmptyBorder;  // Import EmptyBorder để tạo khoảng trống xung quanh thành phần
import javax.swing.text.DefaultCaret;   // Import DefaultCaret để tùy chỉnh con trỏ nhấp nháy
import java.io.*; // Thêm import cho thao tác file

public class MySimpleCalculator {

    // Khai báo các thành phần giao diện người dùng
    private JFrame mainFrame;                 // Cửa sổ chính của ứng dụng
    private JTextField displayField;          // Trường văn bản hiển thị biểu thức và kết quả
    private DefaultListModel<String> historyModel; // Model lưu trữ lịch sử tính toán
    private JList<String> historyList;        // Danh sách hiển thị lịch sử tính toán
    private boolean calculationDone = false;  // Cờ đánh dấu phép tính vừa được thực hiện xong
    private java.util.List<String> fullHistory = new java.util.ArrayList<>();
    private boolean isDegreeMode = true;

    /**
     * Constructor: Khởi tạo máy tính bằng cách thiết lập giao diện và phím tắt.
     */
    public MySimpleCalculator() {
        setupUI();          // Thiết lập giao diện người dùng
        setupKeyBindings(); // Thiết lập phím tắt bàn phím
    }

    /**
     * Thiết lập giao diện người dùng. Tạo khung chính, vùng hiển thị, bảng nút,
     * và bảng lịch sử.
     */
    private void setupUI() {
        // === Tạo khung chính ===
        mainFrame = new JFrame("My Simple Calculator");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(600, 600); // Đặt kích thước cửa sổ
        mainFrame.setLayout(new BorderLayout(10, 10)); // Sử dụng BorderLayout với khoảng cách 10 pixel

        // === Tạo trường hiển thị ===
        displayField = new JTextField("0");
        displayField.setFont(new Font("Arial", Font.BOLD, 32));
        displayField.setHorizontalAlignment(JTextField.RIGHT);
        displayField.setEditable(true);

        // Sửa lỗi cú pháp trong việc thiết lập DefaultCaret
        DefaultCaret caret = new DefaultCaret();
        caret.setBlinkRate(500); // Tốc độ nhấp nháy 500ms
        displayField.setCaret(caret);

        displayField.setBackground(new Color(225, 250, 255));
        displayField.setForeground(Color.BLACK);          // Đặt màu chữ đen
        // Đặt viền kép với đường màu và padding
        displayField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 3),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.add(displayField, BorderLayout.CENTER);

        // === Tạo bảng nút ===
        JPanel buttonPanel = new JPanel(new GridLayout(7, 5, 5, 5));
        // Mảng nhãn nút (số, toán tử và chức năng đặc biệt)
        String[] buttonLabels = {
            	"C", "CE", "B","(", ")",
            	"7", "8", "9","+","-",  
            	"4", "5", "6", "*", "/", 
            	"1", "2", "3", ".", "(-)",
            	"0", "√", "%", "x^y","n!",
            	"sin", "cos", "tan", "cot", 
            	"ln", "log", "Deg↔Rad",	"→", "←", "=",
            };
        // Vòng lặp qua mỗi nhãn, tạo nút và thêm vào bảng
        for (String label : buttonLabels) {
            if (label.isEmpty()) {
                buttonPanel.add(new JLabel());
            } else {
                buttonPanel.add(createButton(label));
            }
        }

        // === Tạo bảng lịch sử ===
        historyModel = new DefaultListModel<>();          // Tạo model cho lịch sử
        historyList = new JList<>(historyModel);            // Tạo danh sách hiển thị lịch sử
        historyList.setFont(new Font("Arial", Font.PLAIN, 14));
        historyList.setCellRenderer(new HistoryCellRenderer()); // Gán renderer mới
        JScrollPane historyScrollPane = new JScrollPane(historyList); // Đặt danh sách trong thanh cuộn
        historyScrollPane.setPreferredSize(new Dimension(200, 0));    // Đặt chiều rộng ưu tiên
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel historyLabel = new JLabel("Calculation History");
        historyLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        historyPanel.add(historyLabel, BorderLayout.NORTH);
        historyPanel.add(historyScrollPane, BorderLayout.CENTER);

        // Thêm ô tìm kiếm và nút search
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        JTextField searchField = new JTextField();
        JButton searchButton = new JButton("🔍"); // Có thể thay bằng icon nếu muốn
        JButton resetButton = new JButton("Reset");
        searchPanel.add(resetButton, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        historyPanel.add(searchPanel, BorderLayout.SOUTH);

        // === Thêm tất cả các bảng vào khung chính ===
        mainFrame.add(displayPanel, BorderLayout.NORTH);
        mainFrame.add(buttonPanel, BorderLayout.CENTER);
        mainFrame.add(historyPanel, BorderLayout.EAST);

        mainFrame.setVisible(true); // Hiển thị khung chính

        // Bắt sự kiện click vào nút X
        historyList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = historyList.locationToIndex(e.getPoint());
                if (index != -1) {
                    Rectangle cellBounds = historyList.getCellBounds(index, index);
                    int xInCell = e.getX() - cellBounds.x;
                    int width = cellBounds.width;
                    // Giả sử nút X nằm ở bên phải, rộng 40px
                    if (xInCell > width - 40) {
                        historyModel.remove(index);
                        fullHistory.remove(index);
                    }
                }
            }
        });

        // Sự kiện tìm kiếm
        searchButton.addActionListener(e -> {
            String keyword = searchField.getText().trim().toLowerCase();
            historyModel.clear();
            for (String s : fullHistory) {
                if (s.toLowerCase().contains(keyword)) {
                    historyModel.addElement(s);
                }
            }
        });

        // Sự kiện reset (hiện lại toàn bộ lịch sử)
        resetButton.addActionListener(e -> {
            searchField.setText("");
            historyModel.clear();
            for (String s : fullHistory) {
                historyModel.addElement(s);
            }
        });
    }

    /**
     * Tạo một JButton với nhãn chỉ định. Đặt font, màu nền và đăng ký người
     * lắng nghe sự kiện.
     *
     * @param label Văn bản hiển thị trên nút
     * @return Đối tượng JButton đã được cấu hình
     */
    private JButton createButton(String label) {
        JButton button = new JButton(label);         // Tạo nút mới với nhãn đã cho
        button.setFont(new Font("Tahoma", Font.BOLD, 16)); // Đặt font cho nút
        button.setFocusPainted(false);                 // Tắt hiển thị focus
        button.setPreferredSize(new Dimension(60, 60));
        button.setBackground(new Color(230, 230, 230));  // Đặt màu nền xám nhạt
        button.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));

        // Đặt màu đặc biệt cho các nút chức năng
        if (label.equals("C") || label.equals("B") || label.equals("CE")
                || label.equals("←") || label.equals("→")) {
            button.setBackground(new Color(255, 160, 122)); // Màu cam nhạt cho các nút chức năng
        } else {
            button.setBackground(new Color(230, 230, 230)); // Màu xám nhạt cho các nút số
        }

        // Đăng ký người lắng nghe sự kiện để xử lý khi nhấp nút
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleButtonEvent(label);            // Xử lý sự kiện dựa trên nhãn nút
            }
        });
        return button;                                 // Trả về nút đã tạo
    }
    
    private void toggleAngleMode() {
        isDegreeMode = !isDegreeMode;
        JOptionPane.showMessageDialog(mainFrame, "Chế độ góc: " + (isDegreeMode ? "Độ" : "Radian"));
    }

    /**
     * Xử lý sự kiện nhấp nút. Đối với nút "(-)", đảo dấu của giá trị hiện tại.
     * Nếu màn hình đang hiển thị "0" hoặc trống, nhấn "(-)" sẽ chèn "-" để
     * người dùng có thể nhập số âm.
     *
     * @param label Nhãn của nút đã được nhấn
     */
    private void handleButtonEvent(String label) {
        // Nếu một phép tính vừa được thực hiện và người dùng nhấn một chữ số, xóa màn hình để nhập mới
        if (calculationDone && Character.isDigit(label.charAt(0))) {
            displayField.setText("");
            calculationDone = false;
        }

        // Xử lý nút dựa trên nhãn của nó
        if (label.equals("C")) {
            displayField.setText("0");
        } else if (label.equals("CE")) {
            clearEntry();
        } else if (label.equals("B")) {
            deleteLastChar();
        } else if (label.equals("←")) {
            moveCaretLeft();                        // Sử dụng phương thức mới
        } else if (label.equals("→")) {
            moveCursorForward();
        } else if (label.equals("=")) {
            performCalculation();
        } else if (label.equals("(-)")) {
            // Hành vi cập nhật: nếu màn hình đang hiển thị "0" hoặc trống, chèn "-" để cho phép nhập số âm
            String currentText = displayField.getText();
            if (currentText.equals("0") || currentText.isEmpty()) {
                displayField.setText("-");
            } else {
                // Nếu không, đảo dấu của số hiện tại
                if (currentText.startsWith("-")) {
                    displayField.setText(currentText.substring(1));
                } else {
                    displayField.setText("-" + currentText);
                }
            }
        } else if (label.equals("√")) {
            appendToDisplay("√");                    // Thêm ký hiệu căn bậc hai
        } else if (label.equals("x^y")) {
            appendToDisplay("^");                    // Thêm ký hiệu lũy thừa
        } else if (label.equals("sin") || label.equals("cos") || label.equals("tan") || label.equals("cot") || label.equals("ln") || label.equals("log")) {
            appendToDisplay(label + "(");  // Thêm dấu "(" cho các hàm lượng giác và logarit
        } else if (label.equals("n!")) {
            appendToDisplay("!");  // Thêm dấu "!" cho phép tính giai thừa
        } else if (label.equals("Deg↔Rad")) {
            toggleAngleMode();  // Chuyển đổi giữa độ và radian
        }  else {
            appendToDisplay(label);  // Thêm các nhãn của các nút khác vào màn hình
        }
    }

    /**
     * Thiết lập phím tắt sử dụng KeyboardFocusManager. Cập nhật để hỗ trợ các
     * phím mũi tên.
     */
    private void setupKeyBindings() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            public boolean dispatchKeyEvent(KeyEvent e) {
                // Chỉ xử lý các sự kiện khi cửa sổ máy tính đang được focus
                if (!mainFrame.isFocused() && !displayField.hasFocus()) {
                    return false;
                }

                // Xử lý sự kiện gõ phím
                if (e.getID() == KeyEvent.KEY_TYPED) {
                    char c = e.getKeyChar();         // Lấy ký tự đã gõ
                    // Nếu ký tự là một chữ số hoặc toán tử hợp lệ, thêm vào màn hình
                    if (Character.isDigit(c) || "+-*/().^%".indexOf(c) != -1) {
                        appendToDisplay(String.valueOf(c));
                        return true;
                    } else if (c == '\n') {          // Nếu Enter được nhấn
                        performCalculation();        // Tính toán biểu thức
                        return true;
                    }
                } else if (e.getID() == KeyEvent.KEY_PRESSED) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_BACK_SPACE:
                            deleteLastChar();
                            return true;
                        case KeyEvent.VK_LEFT:
                            moveCaretLeft();
                            return true;
                        case KeyEvent.VK_RIGHT:
                            moveCursorForward();
                            return true;
                    }
                }

                return false; // Với các phím không được xử lý, trả về false
            }
        });
    }

    /**
     * Thêm văn bản đã cho vào trường hiển thị, xem xét vị trí con trỏ.
     *
     * @param text Văn bản để thêm
     */
    private void appendToDisplay(String text) {
        // Nếu một phép tính vừa được thực hiện, xóa màn hình trước
        if (calculationDone) {
            displayField.setText("");
            calculationDone = false;
        }

        int cursorPos = displayField.getCaretPosition();
        String currentText = displayField.getText();

        // Đảm bảo cursorPos không vượt quá độ dài chuỗi
        cursorPos = Math.min(cursorPos, currentText.length());

        if (currentText.equals("0")) {
            displayField.setText(text);
            displayField.setCaretPosition(text.length());
        } else {
            // Chèn text tại vị trí con trỏ thay vì luôn thêm vào cuối
            String newText = currentText.substring(0, cursorPos) + text + currentText.substring(cursorPos);
            displayField.setText(newText);
            displayField.setCaretPosition(cursorPos + text.length());
        }

        // Đảm bảo con trỏ hiển thị và có focus
        displayField.getCaret().setVisible(true);
        SwingUtilities.invokeLater(() -> {
            displayField.requestFocusInWindow();
        });
    }

    /**
     * Xóa ký tự cuối cùng khỏi trường hiển thị. Đã cập nhật để tính đến vị trí
     * con trỏ.
     */
    private void deleteLastChar() {
        String currentText = displayField.getText();
        int cursorPos = displayField.getCaretPosition();

        // Đảm bảo cursorPos không vượt quá độ dài chuỗi
        cursorPos = Math.min(cursorPos, currentText.length());

        // Luôn đảm bảo cho phép chỉnh sửa
        displayField.setEditable(true);

        if (currentText.length() > 1) {
            if (cursorPos > 0) {
                // Xóa ký tự trước con trỏ
                String newText = currentText.substring(0, cursorPos - 1)
                        + currentText.substring(cursorPos);
                displayField.setText(newText);

                // Đảm bảo vị trí con trỏ không bị âm
                int newPos = Math.max(0, cursorPos - 1);
                displayField.setCaretPosition(newPos);

                // Đảm bảo con trỏ hiển thị và nhấp nháy
                displayField.getCaret().setVisible(true);
            }
        } else {
            displayField.setText("0");
            displayField.setCaretPosition(1);
        }

        // Yêu cầu focus và đảm bảo thực hiện sau khi UI cập nhật
        SwingUtilities.invokeLater(() -> {
            displayField.requestFocusInWindow();
        });
    }

    /**
     * Xóa mục nhập hiện tại mà không ảnh hưởng đến phần còn lại của biểu thức.
     * Chức năng này được sử dụng cho nút "CE" (Clear Entry).
     */
    private void clearEntry() {
        String currentText = displayField.getText();

        // Nếu một phép tính vừa được thực hiện xong, xóa tất cả
        if (calculationDone) {
            displayField.setText("0");
            calculationDone = false;
            return;
        }

        // Tìm toán tử cuối cùng trong biểu thức
        int lastOperatorIndex = -1;
        for (int i = currentText.length() - 1; i >= 0; i--) {
            char c = currentText.charAt(i);
            if (c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == '√') {
                lastOperatorIndex = i;
                break;
            }
        }

        if (lastOperatorIndex == -1) {
            // Không tìm thấy toán tử, xóa tất cả
            displayField.setText("0");
        } else {
            // Xóa chỉ phần sau toán tử cuối cùng
            displayField.setText(currentText.substring(0, lastOperatorIndex + 1));
        }
    }

    /**
     * Di chuyển con trỏ về phía trước trong trường hiển thị. Chức năng này được
     * sử dụng cho nút "→".
     */
    private void moveCursorForward() {
        // Luôn đảm bảo cho phép chỉnh sửa 
        displayField.setEditable(true);
        int position = displayField.getCaretPosition();

        // Di chuyển con trỏ về phía trước nếu chưa ở cuối
        if (position < displayField.getText().length()) {
            displayField.setCaretPosition(position + 1);

            // Đảm bảo con trỏ hiển thị và nhấp nháy
            displayField.getCaret().setVisible(true);
        }

        // Yêu cầu focus và đảm bảo thực hiện sau khi UI cập nhật
        SwingUtilities.invokeLater(() -> {
            displayField.requestFocusInWindow();
        });
    }

    /**
     * Di chuyển con trỏ về phía trái trong trường hiển thị. Chức năng này được
     * sử dụng cho nút "←".
     */
    private void moveCaretLeft() {
        // Luôn đảm bảo cho phép chỉnh sửa
        displayField.setEditable(true);
        int position = displayField.getCaretPosition();

        // Di chuyển con trỏ về phía trái nếu chưa ở đầu
        if (position > 0) {
            displayField.setCaretPosition(position - 1);

            // Đảm bảo con trỏ hiển thị và nhấp nháy
            displayField.getCaret().setVisible(true);
        }

        // Yêu cầu focus và đảm bảo thực hiện sau khi UI cập nhật
        SwingUtilities.invokeLater(() -> {
            displayField.requestFocusInWindow();
        });
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
    

    /**
     * Thực hiện phép tính bằng cách đọc biểu thức từ màn hình, đánh giá nó, sau
     * đó cập nhật màn hình và lịch sử.
     */
    private void performCalculation() {
        try {
            String expression = displayField.getText(); // Lấy biểu thức từ màn hình
            if (expression.isEmpty()) {
                return;
            }
            double result = evaluateExpression(expression); // Đánh giá biểu thức bằng bộ phân tích cú pháp
            String resultStr = formatResult(result);        // Định dạng kết quả để hiển thị
            String historyLine = expression + " = " + resultStr;
            historyModel.addElement(historyLine); // Thêm phép tính vào lịch sử (bộ nhớ)
            displayField.setText(resultStr);   // Cập nhật màn hình với kết quả
            calculationDone = true;            // Đánh dấu rằng một phép tính đã hoàn thành
            fullHistory.add(historyLine);
        } catch (ArithmeticException ex) {
            // Xử lý chia cho không
            if ("Divide by zero".equals(ex.getMessage())) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Không thể chia cho số không!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(mainFrame,
                        "Lỗi số học: " + ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (RuntimeException ex) {
            // Xử lý bất kỳ lỗi nào khác (như biểu thức không hợp lệ)
            JOptionPane.showMessageDialog(mainFrame,
                    "Biểu thức không hợp lệ!: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Đánh giá một biểu thức toán học bằng bộ phân tích cú pháp giảm đệ quy.
     *
     * @param expression Biểu thức dưới dạng chuỗi
     * @return Kết quả của việc đánh giá
     */
    private double evaluateExpression(String expression) {
        ExpressionParser parser = new ExpressionParser(expression);
        return parser.parse();
    }

    /**
     * Định dạng kết quả sao cho nếu kết quả là một số nguyên, phần thập phân
     * không được hiển thị.
     *
     * @param value Giá trị để định dạng
     * @return Biểu diễn chuỗi của kết quả
     */
    private String formatResult(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        } else {
            return String.valueOf(value);
        }
    }
    

    /**
     * ExpressionParser sử dụng phân tích cú pháp giảm đệ quy để đánh giá các
     * biểu thức toán học. Hỗ trợ các toán tử: +, -, *, /, lũy thừa (^), căn bậc
     * hai (√), và phần trăm (%).
     */
    private class ExpressionParser {

        private final String input; // Biểu thức đầu vào
        private int pos = -1;       // Vị trí hiện tại trong chuỗi đầu vào
        private int currentChar;    // Ký tự hiện tại dưới dạng giá trị ASCII

        /**
         * Constructor: Khởi tạo bộ phân tích với biểu thức đã cho.
         *
         * @param input Biểu thức toán học dưới dạng chuỗi
         */
        public ExpressionParser(String input) {
            this.input = input;
            nextChar(); // Di chuyển đến ký tự đầu tiên
        }

        /**
         * Tiến đến ký tự tiếp theo trong đầu vào.
         */
        private void nextChar() {
            pos++;
            currentChar = (pos < input.length()) ? input.charAt(pos) : -1;
        }

        /**
         * Tiêu thụ ký tự hiện tại nếu nó khớp với ký tự mong đợi.
         *
         * @param charToEat Ký tự mà chúng ta mong đợi
         * @return true nếu ký tự đã được tiêu thụ, false nếu ngược lại
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
         * Phân tích biểu thức đầy đủ và trả về giá trị của nó.
         *
         * @return Kết quả đánh giá của biểu thức
         */
        public double parse() {
            double result = parseExpression();
            if (pos < input.length()) {
                throw new RuntimeException("Ký tự không mong đợi: " + (char) currentChar);
            }
            return result;
        }

        /**
         * Phân tích một biểu thức. Biểu thức = Số hạng { ('+' | '-') Số hạng }
         *
         * @return Giá trị đánh giá của biểu thức
         */
        private double parseExpression() {
            double result = parseTerm();
            while (true) {
                if (eat('+')) {
                    result += parseTerm(); // Phép cộng
                } else if (eat('-')) {
                    result -= parseTerm(); // Phép trừ
                } else {
                    return result;
                }
            }
        }

        /**
         * Phân tích một số hạng. Số hạng = Nhân tử { ('*' | '/') Nhân tử }
         *
         * @return Giá trị đánh giá của số hạng
         */
        private double parseTerm() {
            double result = parseFactor();
            while (true) {
                if (eat('*')) {
                    result *= parseFactor(); // Phép nhân
                } else if (eat('/')) {
                    double denominator = parseFactor(); // Phép chia
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
         * Phân tích một nhân tử. Nhân tử = (toán tử một ngôi) { '^' Nhân tử }
         * Hỗ trợ số, dấu ngoặc đơn, căn bậc hai (√), và phần trăm (%).
         *
         * @return Giá trị đánh giá của nhân tử
         */
        private double parseFactor() {
            // Xử lý dấu cộng và trừ một ngôi
            if (eat('+')) {
                return parseFactor();
            }
            if (eat('-')) {
                return -parseFactor();
            }

            double result;
            int startPos = pos; // Ghi nhớ vị trí bắt đầu cho các số

         // Hàm lượng giác & logarit
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
                    case "sin" -> result = Math.sin(arg);
                    case "cos" -> result = Math.cos(arg);
                    case "tan" -> result = Math.tan(arg);
                    case "cot" -> result = 1.0 / Math.tan(arg);
                    case "log" -> result = Math.log10(arg);
                    case "ln" -> result = Math.log(arg);
                    default -> throw new RuntimeException("Unknown function: " + func);
                }
                return result;
            }
            // Xử lý căn bậc hai: ký hiệu '√'
            if (eat('√')) {
                double value;
                if (eat('(')) {
                    value = parseExpression();
                    if (!eat(')')) {
                        throw new RuntimeException("Thiếu ')' sau căn bậc hai");
                    }
                } else {
                    value = parseFactor();
                }
                if (value < 0) {
                    throw new ArithmeticException("Không thể tính căn bậc hai của số âm: √" + value);
                }
                result = Math.sqrt(value);
                if (eat('%')) {
                    result /= 100.0;
                }
            } // Xử lý dấu ngoặc đơn
            else if (eat('(')) {
                result = parseExpression();
                if (!eat(')')) {
                    throw new RuntimeException("Thiếu ')'");
                }
            } // Xử lý số
            else if ((currentChar >= '0' && currentChar <= '9') || currentChar == '.') {
                boolean hasDot = false;
                while ((currentChar >= '0' && currentChar <= '9') || currentChar == '.') {
                    if (currentChar == '.') {
                        if (hasDot) throw new RuntimeException("Số không hợp lệ: nhiều dấu '.' liên tiếp");
                        hasDot = true;
                    }
                    nextChar();
                }
                String numberStr = input.substring(startPos, pos);
                if (numberStr.equals(".") || numberStr.isEmpty()) {
                    throw new RuntimeException("Số không hợp lệ: " + numberStr);
                }
                result = Double.parseDouble(numberStr);
                if (eat('%')) {
                    result /= 100.0;
                }
            } else {
                throw new RuntimeException("Ký tự không mong đợi: " + (char) currentChar);
            }

            // Xử lý lũy thừa '^'
            if (eat('^')) {
                double exponent = parseFactor();
                if (result < 0 && exponent != Math.floor(exponent)) {
                    throw new ArithmeticException("Không hỗ trợ lũy thừa số âm với số mũ không nguyên: " + result + "^" + exponent);
                }
                result = Math.pow(result, exponent);
            }
            if (eat('!')) {
                if (result < 0 || result != Math.floor(result)) throw new RuntimeException("Giai thừa không hợp lệ");
                result = factorial((int) result);
            }
            return result;
        }
        private double factorial(int n) {
            double res = 1;
            for (int i = 2; i <= n; i++) res *= i;
            return res;
        }
    }

    // Custom renderer cho từng dòng lịch sử với nút X
    private class HistoryCellRenderer extends JPanel implements ListCellRenderer<String> {
        JLabel label;
        JButton deleteButton;

        public HistoryCellRenderer() {
            setLayout(new BorderLayout());
            label = new JLabel();
            deleteButton = new JButton("X");
            deleteButton.setMargin(new Insets(2, 6, 2, 6));
            deleteButton.setFocusable(false);
            deleteButton.setForeground(Color.RED);
            add(label, BorderLayout.CENTER);
            add(deleteButton, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            label.setText(value);
            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            return this;
        }
    }

    public static void main(String[] args) {
        MySimpleCalculator c = new MySimpleCalculator();
    }
}
