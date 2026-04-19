/*
    By team Code Wariors
    Group Leader: Lau Kaixuan 1221103162@student.mmu.edu.my (011-64442810)
    Group members:Iusupov Alimzhan 1231301318@student.mmu.edu.my (013-2963272)
                  Wong Yong Kit wong.yong.kit@student.mmu.edu.my (017-3538188)
                  Wong Cii Voon Wong.cii.voon@student.mmu.edu.my (010-9003292)
 */
import javax.swing.*;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;


/**
 * Main class that handles the chess game GUI and user interactions.
 * Manages the game window, controls, and event handling for the chess application.
 */

// By: Lau Kaixuan
public class KwazamChess extends JFrame implements MouseListener {
    private JFrame frame;
    private ChessBoardView boardView;
    private ChessGameModel model;
    private JLabel statusLabel;
    private JButton saveButton;
    private JLabel timerLabel;
    private Timer timer;
    private int timeElapsed;
    private String endMessage = null;

    public KwazamChess() {
        JButton button = new JButton("Start Game");
        frame = new JFrame("Kwazam Chess");
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        frame.setBounds(400, 200, 500, 600);
        frame.add(button, BorderLayout.CENTER);

        frame.setVisible(true);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                gamePage();
            }
        });
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F11) {
                    toggleFullScreen();
                }
            }
        });
    }

    private void gamePage() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        statusLabel = new JLabel("White's turn");
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));

        model = new ChessGameModel(this);
        boardView = new ChessBoardView(model, this);
        boardView.addMouseListener(this); // KwazamChess will handle mouse events

        saveButton = new JButton("Save History");
        saveButton.setBounds(0, 0, 100, 50);
        saveButton.setFont(new Font("Arial", Font.BOLD, 16));
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setEndMessage("End");
                ChessGameModel.moveHistory.add(getEndMessage());
                setEndMessage(null);
                model.saveHistoryToFile();
                JOptionPane.showMessageDialog(frame, "History saved to history.txt");
            }
        });
        // Create a new JButton to restart the game
        //override the actionPerformed method of the JButton
        JButton restartButton = new JButton("Restart Game");
        restartButton.setFont(new Font("Arial", Font.BOLD, 16));
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                new KwazamChess();
            }
        });

        timerLabel = new JLabel("Time Elapsed: 0 s");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timerLabel.setHorizontalAlignment(JLabel.CENTER);

        timeElapsed = 0;
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeElapsed++;
                timerLabel.setText("Time Elapsed: " + timeElapsed + " s");
            }
        });
        timer.start();

        JPanel timerPanel = new JPanel();
        timerPanel.setLayout(new BorderLayout());
        timerPanel.add(timerLabel, BorderLayout.NORTH);
        timerPanel.add(saveButton);

        frame.add(statusLabel, BorderLayout.NORTH);
        frame.add(boardView, BorderLayout.CENTER);
        frame.add(timerPanel, BorderLayout.EAST);
        frame.add(restartButton, BorderLayout.SOUTH);
        frame.revalidate();
        frame.repaint();
        updateStatusLabel();
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        frame.setBounds(400, 200, 500, 600);
        frame.setVisible(true);
    }

    // reset the time of the game times, 1 time as user move 1 steps
    public void resetTimeMoved() {
        ChessGameModel.timesMoved = 0;
    }

    //set the end message of the game
    public void setEndMessage(String message) {
        endMessage = message;
    }
    //get the end message of the game
    public String getEndMessage() {
        return endMessage;
    }

    //update the status label of the game
    public void updateStatusLabel() {
        String turnText = ChessGameModel.isWhiteTurn() ? "Blue's/White" : "Red's/Black";
        statusLabel.setText(turnText + " turn");
    }

    //set the frame of the game
    private void toggleFullScreen() {
        if (frame.getExtendedState() == MAXIMIZED_BOTH) {
            frame.setExtendedState(NORMAL);
            frame.setSize(1000, 600);
        } else {
            frame.setExtendedState(MAXIMIZED_BOTH);
        }
    }

    /**
     * Handles mouse click events for piece selection and movement.
     * Calculates board positions, validates moves, and updates the game state.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        int squareSize = Math.min(boardView.getWidth() / ChessGameModel.BOARD_SIZE,
                boardView.getHeight() / ChessGameModel.BOARD_ROWS);
        int col = e.getX() / squareSize;
        int row = e.getY() / squareSize;

        if (boardView.isRotated()) {
            col = ChessGameModel.BOARD_SIZE - 1 - col;
            row = ChessGameModel.BOARD_ROWS - 1 - row;
        }

        if (boardView.selectedSquare == null) {
            if (model.getPieces()[row][col] != null && model.canMove(model.getPieces()[row][col].isWhite())) {
                boardView.selectedSquare = new Point(col, row);
                ChessGameModel.ChessPiece.calculateValidMoves(row, col);
                boardView.repaint();
            }
        } else {
            if (model.getValidMoves()[row][col]) {
                ChessGameModel.ChessPiece.movePiece(boardView.selectedSquare.y, boardView.selectedSquare.x, row, col);
                model.nextTurn();
                boardView.rotateBoard(); // Rotate the board after each move
            }
            boardView.selectedSquare = null;
            model.clearValidMoves();
            boardView.repaint();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        new KwazamChess();
    }
}

/**
 * Model class that manages the game logic, board state, and move validation.
 * Handles piece positions, turn management, and game rules.
 */

// By: Lau Kaixuan
class ChessGameModel {
    public static final int BOARD_SIZE = 5;
    public static final int BOARD_ROWS = 8;
    private static ChessPiece[][] pieces;
    public static boolean[][] validMoves;
    public static boolean isWhiteTurn = true;
    public static java.util.List<String> moveHistory;
    private static KwazamChess controller;
    static int timesMoved;

    public ChessGameModel(KwazamChess controller) {
        this.controller = controller;
        pieces = new ChessPiece[BOARD_ROWS][BOARD_SIZE];
        validMoves = new boolean[BOARD_ROWS][BOARD_SIZE];
        moveHistory = new ArrayList<>();
        timesMoved = 0;
        initializeBoard();
    }

    //record the move history of the game when the chess is move
    public static void recordMove(int fromRow, int fromCol, int toRow, int toCol) {
        String start = "start";
        if(timesMoved == 0) {
            moveHistory.add(start);
        }
        String move = String.format("%s: (%d, %d) -> (%d, %d)",
                isWhiteTurn ? "Blue/White" : "Red/Black", fromRow, fromCol, toRow, toCol);
        timesMoved++;
        moveHistory.add(move);
    }

    //get the move history of the game when the chess is move and save it into the history.txt file
    public void saveHistoryToFile() {
        try (java.io.BufferedWriter writer = new BufferedWriter(
                new FileWriter("history.txt"))) {
            for (String move : moveHistory) {
                writer.write(move);
                writer.newLine();
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }


    // to indicate the chess turn
    public static boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    //update the chess turn
    public static void nextTurn() {
        isWhiteTurn = !isWhiteTurn;
        if (controller != null) {
            controller.updateStatusLabel();
        }
    }

    public static boolean canMove(boolean isWhitePiece) {
        return isWhitePiece == isWhiteTurn;
    }

    public static ChessPiece[][] getPieces() {
        return pieces;
    }

    public boolean[][] getValidMoves() {
        return validMoves;
    }

    public static void clearValidMoves() {
        for (int i = 0; i < BOARD_ROWS; i++) {
            java.util.Arrays.fill(validMoves[i], false);
        }
    }

    // Enum to represent a chess piece
    enum PieceType {
        RAM, TOR, BIZ, XOR, SAU
    }


    /**
     * Represents a chess piece in the game with its type, color, and movement capabilities.
     * Handles piece-specific behaviors including movement validation, transformation,
     * visual representation, and state tracking.
     */
    class ChessPiece {
        private PieceType type;
        private boolean isWhite;
        private boolean hasMoved;
        private int round;
        private int ramDirection;
        private static int turn;

        private boolean isRAMTransformed;

        public ChessPiece(PieceType type, boolean isWhite) {
            this.type = type;
            this.isWhite = isWhite;
            this.hasMoved = false;
            this.round = 0;
            turn = 0;
            if (type == PieceType.RAM) {
                ramDirection = isWhite ? -1 : 1;
            }
        }

        public boolean isWhite() {
            return isWhite;
        }

        public void setMoved() {
            this.hasMoved = true;
        }

        public int getRound() {
            return round;
        }

        public void incrementRounds() {
            round++;
        }

        public void setType(PieceType type) {
            this.type = type;
        }

        public PieceType getType() {
            return type;
        }

        public static void setTurn() {
            turn++;
        }

        public static int getTurn() {
            return turn;
        }

        //calculate the movement that able to be done by the chess piece
        public static void calculateValidMoves(int row, int col) {
            clearValidMoves();
            ChessPiece piece = getPieces()[row][col];
            if (piece != null) {
                piece.getValidMoves(row, col, getPieces(), validMoves);
            }
        }

        //perform the movement of the chess piece
        public static void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
            ChessPiece[][] pieces = getPieces();
            ChessPiece movingPiece = pieces[fromRow][fromCol];
            if (pieces[toRow][toCol] != null &&
                    pieces[toRow][toCol].getType() == PieceType.SAU) {
                // Show the dialog without referencing a null boardView
                JOptionPane.showMessageDialog(null, "Game Over! " + (movingPiece.isWhite() ? "Blue/White" : "Red/Black") + " wins!");
            }
            pieces[toRow][toCol] = movingPiece;
            pieces[fromRow][fromCol] = null;
            movingPiece.setMoved();
            setTurn();
            recordMove(fromRow, fromCol, toRow, toCol);
            if (movingPiece.getType() == PieceType.RAM) {
                if (movingPiece.isWhite()) {
                    if (toRow == 0) {
                        movingPiece.setRamTransformed(true);
                    }
                    else if (toRow == 7) {
                        movingPiece.setRamTransformed(false);
                    }
                } else {

                    if (toRow == 7) {
                        movingPiece.setRamTransformed(true);
                    } else if (toRow == 0) {
                        movingPiece.setRamTransformed(false);
                    }
                }
            }
            //update the chess piece for TOR and XOR in every round
            //1 round = 2 turn
            if (getTurn() % 2 == 0) {
                updateAllTORAndXOR();
            }
        }

        public void setRamTransformed(boolean transformed) {
            this.isRAMTransformed = transformed;
        }

        public boolean isRamTransformed() {
            return this.isRAMTransformed;
        }

        //function to ouput the chess piece using img or symbol
        //chess piece will use symbol to represent when image path is not found
        public void draw(Graphics g, int x, int y, int size) {
            String imagePath;
            switch (type) {
                case SAU:
                    imagePath = isWhite ? "/img material/SauBlue.png" : "/img material/SauRed.png";
                    break;
                case XOR:
                    imagePath = isWhite ? "/img material/XorBlue.png" : "/img material/XorRed.png";
                    break;
                case TOR:
                    imagePath = isWhite ? "/img material/TorBlue.png" : "/img material/TorRed.png";
                    break;
                case BIZ:
                    imagePath = isWhite ? "/img material/BizBlue.png" : "/img material/BizRed.png";
                    break;
                case RAM:
                    if(isRamTransformed()) {
                        imagePath = isWhite ? "/img material/RamBlue_Flipped.png" : "/img material/RamRed_Flipped.png";
                    } else {
                        imagePath = isWhite ? "/img material/RamBlue.png" : "/img material/RamRed.png";
                    }
                    break;
                default:
                    imagePath = null;
            }

            Image pieceImage = null;
            if (imagePath != null) {
                try {
                    pieceImage = new ImageIcon(imagePath).getImage();
                } catch (Exception e) {
                    pieceImage = null;
                }
            }

            //check for the image path
            Image tmpImg = new ImageIcon(imagePath).getImage();
            if (tmpImg.getWidth(null) <= 0 || tmpImg.getHeight(null) <= 0) {
                // Image is invalid or not found
                pieceImage = null;
            } else {
                pieceImage = tmpImg;
            }

            if (pieceImage != null) {
                g.drawImage(pieceImage, x, y, size, size, null);
            } else {
                g.setColor(isWhite ? Color.WHITE : Color.BLACK); //indicate whether the color of the sumbol
                String symbol = "";
                switch (type) {
                    case SAU:
                        symbol = "♔";
                        break;
                    case XOR:
                        symbol = "♕";
                        break;
                    case TOR:
                        symbol = "♖";
                        break;
                    case BIZ:
                        symbol = "♘";
                        break;
                    case RAM:
                        symbol = "♙";
                        break;
                }
                g.setFont(new Font("Dialog", Font.PLAIN, size * 2 / 3));
                FontMetrics metrics = g.getFontMetrics();
                int ascent = metrics.getAscent();
                int symbolWidth = metrics.stringWidth(symbol);
                int symbolHeight = metrics.getHeight();
                g.drawString(symbol,
                        x + (size - symbolWidth) / 2,
                        y + (size + ascent - symbolHeight / 2) / 2
                );
            }
        }

        //function to get the valid moves for each type of chess piece
        public void getValidMoves(int row, int col, ChessPiece[][] board, boolean[][] validMoves) {
            switch (type) {
                case RAM:
                    getRAMMoves(row, col, board, validMoves);
                    break;
                case TOR:
                    getTORMoves(row, col, board, validMoves);
                    break;
                case BIZ:
                    getBIZMoves(row, col, board, validMoves);
                    break;
                case XOR:
                    getXORMoves(row, col, board, validMoves);
                    break;
                case SAU:
                    getSAUMoves(row, col, board, validMoves);
                    break;
            }
        }

        //RAM piece moves logic
        private void getRAMMoves(int row, int col, ChessPiece[][] board, boolean[][] validMoves) {
            // Move forward
            if (row == 0 || row == 7) {
                ramDirection = -ramDirection;
            }
            int newRow = row + ramDirection;
            int newCol = col + 1;
            if (newRow >= 0 && newRow < 8) {
                if (board[newRow][col] == null || board[newRow][col].isWhite != isWhite || ((board[newRow][newCol].getType() == PieceType.SAU) && board[newRow][newCol].isWhite != isWhite)) {
                    validMoves[newRow][col] = true;
                }
            }
        }

        //TOR moves logic
        private void getTORMoves(int row, int col, ChessPiece[][] board, boolean[][] validMoves) {
            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] dir : directions) {
                int r = row + dir[0];
                int c = col + dir[1];
                while (r >= 0 && r < 8 && c >= 0 && c < 5) {
                    if (board[r][c] == null) {
                        validMoves[r][c] = true;
                    } else {
                        if (board[r][c].isWhite != isWhite) {
                            validMoves[r][c] = true;
                        }
                        break;
                    }
                    r += dir[0];
                    c += dir[1];
                }
            }
        }

        //BIZ moves logic
        private void getBIZMoves(int row, int col, ChessPiece[][] board, boolean[][] validMoves) {
            int[][] moves = {
                    {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                    {1, -2},  {1, 2},  {2, -1},  {2, 1}
            };
            for (int[] move : moves) {
                int r = row + move[0];
                int c = col + move[1];
                if (r >= 0 && r < 8 && c >= 0 && c < 5) {
                    if (board[r][c] == null || board[r][c].isWhite != isWhite) {
                        validMoves[r][c] = true;
                    }
                }
            }
        }

        //XOR moves logic
        private void getXORMoves(int row, int col, ChessPiece[][] board, boolean[][] validMoves) {
            int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
            for (int[] dir : directions) {
                int r = row + dir[0];
                int c = col + dir[1];
                while (r >= 0 && r < 8 && c >= 0 && c < 5) {
                    if (board[r][c] == null) {
                        validMoves[r][c] = true;
                    } else {
                        if (board[r][c].isWhite != isWhite) {
                            validMoves[r][c] = true;
                        }
                        break;
                    }
                    r += dir[0];
                    c += dir[1];
                }
            }
        }

        //SAU moves logic
        private void getSAUMoves(int row, int col, ChessPiece[][] board, boolean[][] validMoves) {
            int[][] moves = {
                    {-1, -1}, {-1, 0}, {-1, 1},
                    {0, -1},           {0, 1},
                    {1, -1},  {1, 0},  {1, 1}
            };
            for (int[] move : moves) {
                int r = row + move[0];
                int c = col + move[1];
                if (r >= 0 && r < 8 && c >= 0 && c < 5) {
                    if (board[r][c] == null || board[r][c].isWhite != isWhite) {
                        validMoves[r][c] = true;
                    }
                }
            }
        }
    }

    //initialize the board with the pieces in their starting positions
    public void initializeBoard() {
        pieces[0][0] = new ChessPiece(PieceType.TOR, false);
        pieces[0][1] = new ChessPiece(PieceType.BIZ, false);
        pieces[0][2] = new ChessPiece(PieceType.SAU, false);
        pieces[0][3] = new ChessPiece(PieceType.BIZ, false);
        pieces[0][4] = new ChessPiece(PieceType.XOR, false);
        for (int i = 0; i < BOARD_SIZE; i++) {
            pieces[1][i] = new ChessPiece(PieceType.RAM, false);
        }

        pieces[7][0] = new ChessPiece(PieceType.XOR, true);
        pieces[7][1] = new ChessPiece(PieceType.BIZ, true);
        pieces[7][2] = new ChessPiece(PieceType.SAU, true);
        pieces[7][3] = new ChessPiece(PieceType.BIZ, true);
        pieces[7][4] = new ChessPiece(PieceType.TOR, true);
        for (int i = 0; i < BOARD_SIZE; i++) {
            pieces[6][i] = new ChessPiece(PieceType.RAM, true);
        }
    }

    //function to swap the condition between XOR and TOR
    public static void updateAllTORAndXOR() {
        for (int row = 0; row < BOARD_ROWS; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPiece piece = pieces[row][col];
                if (piece != null) {
                    piece.incrementRounds();
                    if (piece.getType() == PieceType.TOR && piece.getRound() % 2 == 0) {
                        piece.setType(PieceType.XOR);
                    } else if (piece.getType() == PieceType.XOR && piece.getRound() % 2 == 0) {
                        piece.setType(PieceType.TOR);
                    }
                }
            }
        }
    }
}

/**
 * View class responsible for rendering the chess board and pieces.
 * Handles the visual representation of the game board, piece placement,
 * board rotation, and move highlighting.
 */
//By: Isupov Alimzhan, Wong Yong Kit and Wong Cii Voon
class ChessBoardView extends JPanel {
    private static final int BOARD_SIZE = 5;
    private static final int BOARD_ROWS = 8;
    private ChessGameModel model;
    private KwazamChess controller;
    Point selectedSquare;
    private boolean isRotated = false;

    public ChessBoardView(ChessGameModel model, KwazamChess controller) {
        this.model = model;
        this.controller = controller;
        setPreferredSize(new Dimension(1000, 600));
    }

    //paint the chess board with color
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int squareSize = Math.min(getWidth() / BOARD_SIZE, getHeight() / BOARD_ROWS);
        drawBoard(g, squareSize);
        drawPieces(g, squareSize);
        if (selectedSquare != null) {
            
            highlightValidMoves(g, squareSize);
        }
    }

    //draw the board
    private void drawBoard(Graphics g, int squareSize) {
        for (int row = 0; row < BOARD_ROWS; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if ((row + col) % 2 == 0) {
                    g.setColor(new Color(240, 217, 181));
                } else {
                    g.setColor(new Color(181, 136, 99));
                }
                g.fillRect(col * squareSize, row * squareSize, squareSize, squareSize);
            }
        }
    }

    //draw the pieces
    private void drawPieces(Graphics g, int squareSize) {
        ChessGameModel.ChessPiece[][] pieces = model.getPieces();
        for (int row = 0; row < BOARD_ROWS; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                int drawRow = isRotated ? BOARD_ROWS - 1 - row : row;
                int drawCol = isRotated ? BOARD_SIZE - 1 - col : col;
                if (pieces[row][col] != null) {
                    pieces[row][col].draw(g, drawCol * squareSize, drawRow * squareSize, squareSize);
                }
            }
        }
    }

    //highlight the valid moves
    private void highlightValidMoves(Graphics g, int squareSize) {
        g.setColor(new Color(0, 255, 0, 128));
        boolean[][] validMoves = model.getValidMoves();
        for (int row = 0; row < BOARD_ROWS; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                int drawRow = isRotated ? BOARD_ROWS - 1 - row : row;
                int drawCol = isRotated ? BOARD_SIZE - 1 - col : col;
                if (validMoves[row][col]) {
                    g.fillRect(drawCol * squareSize, drawRow * squareSize, squareSize, squareSize);
                }
            }
        }
    }

    public void rotateBoard() {
        isRotated = !isRotated;
        repaint();
    }

    public boolean isRotated() {
        return isRotated;
    }
}