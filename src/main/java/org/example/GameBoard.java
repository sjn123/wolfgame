package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

public class GameBoard extends JPanel {

    private static final int ROWS = 5;
    private static final int COLS = 5;
    private static final int CELL_SIZE = 100;
    private static final int BOARD_WIDTH = COLS * CELL_SIZE;
    private static final int BOARD_HEIGHT = ROWS * CELL_SIZE;
    private static final int PADDING = 20;

    private Piece[][] board = new Piece[ROWS][COLS];
    private boolean wolfTurn = true; // 狼先走
    private int wolfCount = 3;
    private int sheepCount = 15;

    private int selectedRow = -1;
    private int selectedCol = -1;

    private Runnable onGameEnd;
    private Runnable onBoardUpdate;
    private boolean gameOver = false;

    public GameBoard() {
        setPreferredSize(new Dimension(BOARD_WIDTH + PADDING * 2, BOARD_HEIGHT + PADDING * 2));
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!gameOver) {
                    handleMouseClick(e);
                }
            }
        });

        initGame();
    }

    public void setOnGameEnd(Runnable onGameEnd) {
        this.onGameEnd = onGameEnd;
    }

    public void setOnBoardUpdate(Runnable onBoardUpdate) {
        this.onBoardUpdate = onBoardUpdate;
    }

    public void initGame() {
        wolfTurn = true;
        wolfCount = 3;
        sheepCount = 15;
        selectedRow = -1;
        selectedCol = -1;
        gameOver = false;

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = null;
            }
        }
        board[4][1] = Piece.WOLF;
        board[4][2] = Piece.WOLF;
        board[4][3] = Piece.WOLF;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = Piece.SHEEP;
            }
        }
        refresh();
    }

    private void handleMouseClick(MouseEvent e) {
        Point cell = getCell(e);
        if (cell == null) return;

        // 如果已选中棋子，尝试移动
        if (selectedRow != -1 && selectedCol != -1) {
            movePiece(selectedRow, selectedCol, cell.x, cell.y);
            selectedRow = -1;
            selectedCol = -1;
            repaint();
            return;
        }

        // 选择己方棋子
        Piece piece = board[cell.x][cell.y];
        if (piece != null && piece.isWolf() == wolfTurn) {
            selectedRow = cell.x;
            selectedCol = cell.y;
        }
        repaint();
    }

    private void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow == toRow && fromCol == toCol) return;

        Piece currentPiece = board[fromRow][fromCol];
        Piece targetPiece = board[toRow][toCol];

        int dRow = Math.abs(toRow - fromRow);
        int dCol = Math.abs(toCol - fromCol);

        if (currentPiece.isWolf()) {
            // 狼的移动逻辑：四周走一步，或隔一个格子吃羊
            if ((dRow <= 1 && dCol <= 1 && (dRow + dCol == 1) && targetPiece == null) ||
                    (dRow + dCol == 2 && targetPiece == Piece.SHEEP)) {

                int midRow = (fromRow + toRow) / 2;
                int midCol = (fromCol + toCol) / 2;

                boolean canMove = targetPiece == null;
                if (targetPiece == Piece.SHEEP && board[midRow][midCol] == null) {
                    canMove = true;
                }

                if (canMove) {
                    board[toRow][toCol] = currentPiece;
                    board[fromRow][fromCol] = null;
                    if (targetPiece == Piece.SHEEP) {
                        sheepCount--;
                    }
                    wolfTurn = false;
                    checkGameEnd();
                    refresh();
                }
            }
        } else {
            // 羊的移动逻辑：只能四周走一步，不能吃
            if (dRow + dCol == 1 && targetPiece == null) {
                board[toRow][toCol] = currentPiece;
                board[fromRow][fromCol] = null;
                wolfTurn = true;
                checkGameEnd();
                refresh();
            }
        }
    }

    // 狼走完一步后，羊随机移动
    public void autoSheepMove() {
        if (gameOver) return;

        // 收集所有羊的位置
        java.util.List<int[]> sheepPositions = new java.util.ArrayList<>();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (board[i][j] == Piece.SHEEP) {
                    sheepPositions.add(new int[]{i, j});
                }
            }
        }

        if (sheepPositions.isEmpty()) return;

        // 随机选一只羊
        Random random = new Random();
        int[] pos = sheepPositions.get(random.nextInt(sheepPositions.size()));
        int row = pos[0], col = pos[1];

        // 找到所有合法移动方向
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        java.util.List<int[]> validMoves = new java.util.ArrayList<>();
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            if (newRow >= 0 && newRow < ROWS && newCol >= 0 && newCol < COLS && board[newRow][newCol] == null) {
                validMoves.add(new int[]{newRow, newCol});
            }
        }

        // 如果有合法移动，随机选择一个
        if (!validMoves.isEmpty()) {
            int[] move = validMoves.get(random.nextInt(validMoves.size()));
            board[move[0]][move[1]] = Piece.SHEEP;
            board[row][col] = null;
            wolfTurn = true;
            refresh();
        }
    }

    private Point getCell(MouseEvent e) {
        int x = e.getX() - PADDING;
        int y = e.getY() - PADDING;
        if (x >= 0 && x < BOARD_WIDTH && y >= 0 && y < BOARD_HEIGHT) {
            return new Point(y / CELL_SIZE, x / CELL_SIZE);
        }
        return null;
    }

    private void checkGameEnd() {
        if (onGameEnd != null) {
            onGameEnd.run();
        }
    }

    public boolean checkWinConditions(int steps) {
        if (sheepCount <= 3) {
            JOptionPane.showMessageDialog(this, "🐺 狼方胜利！吃掉了大部分羊。");
            gameOver = true;
            return true;
        }
        if (steps >= 150) {
            JOptionPane.showMessageDialog(this, "🐺 狼方胜利！步数达到上限。");
            gameOver = true;
            return true;
        }
        return false;
    }

    public boolean isWolfTurn() {
        return wolfTurn;
    }

    public int getWolfCount() {
        return wolfCount;
    }

    public int getSheepCount() {
        return sheepCount;
    }

    private void refresh() {
        if (onBoardUpdate != null) {
            onBoardUpdate.run();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 白色棋盘背景
        g2.setColor(Color.WHITE);
        g2.fillRect(PADDING, PADDING, BOARD_WIDTH, BOARD_HEIGHT);

        // 棋盘边框
        g2.setColor(new Color(200, 200, 200));
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(PADDING, PADDING, BOARD_WIDTH, BOARD_HEIGHT);

        // 格子线
        g2.setColor(new Color(220, 220, 220));
        g2.setStroke(new BasicStroke(1));
        for (int i = 1; i < ROWS; i++) {
            int y = PADDING + i * CELL_SIZE;
            g2.drawLine(PADDING, y, PADDING + BOARD_WIDTH, y);
        }
        for (int i = 1; i < COLS; i++) {
            int x = PADDING + i * CELL_SIZE;
            g2.drawLine(x, PADDING, x, PADDING + BOARD_HEIGHT);
        }

        // 绘制棋子（使用图案代替文字）
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                Piece piece = board[i][j];
                if (piece != null) {
                    // 选中高亮
                    if (i == selectedRow && j == selectedCol) {
                        g2.setColor(new Color(255, 200, 100, 150));
                        g2.fillOval(PADDING + j * CELL_SIZE + 10, PADDING + i * CELL_SIZE + 10,
                                CELL_SIZE - 20, CELL_SIZE - 20);
                    }

                    int x = PADDING + j * CELL_SIZE + CELL_SIZE / 2;
                    int y = PADDING + i * CELL_SIZE + CELL_SIZE / 2;

                    if (piece.isWolf()) {
                        drawWolf(g2, x, y);
                    } else {
                        drawSheep(g2, x, y);
                    }
                }
            }
        }
        g2.dispose();
    }

    private void drawWolf(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(139, 69, 19));
        g2.setStroke(new BasicStroke(3));

        // 狼头（圆形）
        g2.fillOval(x - 20, y - 25, 40, 35);

        // 耳朵
        int[] leftEarX = {x - 18, x - 22, x - 10};
        int[] leftEarY = {y - 20, y - 38, y - 25};
        g2.fillPolygon(leftEarX, leftEarY, 3);

        int[] rightEarX = {x + 18, x + 22, x + 10};
        int[] rightEarY = {y - 20, y - 38, y - 25};
        g2.fillPolygon(rightEarX, rightEarY, 3);

        // 眼睛
        g2.setColor(Color.BLACK);
        g2.fillOval(x - 12, y - 18, 6, 6);
        g2.fillOval(x + 6, y - 18, 6, 6);

        // 鼻子
        g2.fillOval(x - 4, y - 8, 8, 6);

        // 嘴巴
        g2.setColor(Color.BLACK);
        g2.drawArc(x - 10, y - 5, 20, 10, 0, -180);
    }

    private void drawSheep(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(220, 220, 220));
        g2.setStroke(new BasicStroke(2));

        // 羊毛（云朵状）
        g2.fillOval(x - 22, y - 22, 15, 15);
        g2.fillOval(x - 12, y - 28, 18, 18);
        g2.fillOval(x + 5, y - 25, 16, 16);
        g2.fillOval(x + 15, y - 18, 14, 14);
        g2.fillOval(x - 20, y - 12, 40, 28);

        // 头
        g2.setColor(new Color(240, 240, 240));
        g2.fillOval(x - 8, y + 5, 16, 14);

        // 耳朵
        g2.fillOval(x - 15, y + 2, 10, 6);
        g2.fillOval(x + 5, y + 2, 10, 6);

        // 眼睛
        g2.setColor(Color.BLACK);
        g2.fillOval(x - 5, y + 8, 3, 3);
        g2.fillOval(x + 2, y + 8, 3, 3);

        // 边框
        g2.setColor(new Color(150, 150, 150));
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(x - 22, y - 22, 15, 15);
        g2.drawOval(x - 12, y - 28, 18, 18);
        g2.drawOval(x + 5, y - 25, 16, 16);
        g2.drawOval(x + 15, y - 18, 14, 14);
        g2.drawOval(x - 20, y - 12, 40, 28);
        g2.drawOval(x - 8, y + 5, 16, 14);
    }
}