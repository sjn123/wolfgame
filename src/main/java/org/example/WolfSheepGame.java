package org.example;

import javax.swing.*;
import java.awt.*;

public class WolfSheepGame extends JFrame {

    private GameBoard gameBoard;
    private JLabel stepLabel, wolfCountLabel, sheepCountLabel;
    private int steps = 0;
    private static final int MAX_STEPS = 150;

    public WolfSheepGame() {
        setTitle("狼吃羊 - 5x5棋盘");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        // 顶部信息栏
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        stepLabel = new JLabel("步数: 0 / 150");
        wolfCountLabel = new JLabel("狼: 3");
        sheepCountLabel = new JLabel("羊: 15");
        topPanel.add(stepLabel);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(wolfCountLabel);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(sheepCountLabel);

        gameBoard = new GameBoard();

        // 刷新 UI 回调
        gameBoard.setOnBoardUpdate(() -> {
            stepLabel.setText("步数: " + steps + " / " + MAX_STEPS);
            wolfCountLabel.setText("狼: " + gameBoard.getWolfCount());
            sheepCountLabel.setText("羊: " + gameBoard.getSheepCount());
        });

        // 游戏结束回调
        gameBoard.setOnGameEnd(() -> {
            gameBoard.checkWinConditions(steps);
        });

        // 底部按钮
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton btnSwitchTurn = new JButton("切换阵营");
        JButton btnReset = new JButton("重新开始");

        btnSwitchTurn.addActionListener(e -> switchTurn());
        btnReset.addActionListener(e -> resetGame());

        bottomPanel.add(btnSwitchTurn);
        bottomPanel.add(btnReset);

        add(topPanel, BorderLayout.NORTH);
        add(gameBoard, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        pack();
    }

    // 切换阵营（同时处理步数增加和自动移动）
    private void switchTurn() {
        if (steps >= MAX_STEPS) {
            gameBoard.checkWinConditions(steps);
            return;
        }

        if (gameBoard.isWolfTurn()) {
            // 狼走完，切换到羊，步数增加
            steps++;
            // 羊自动移动
            SwingUtilities.invokeLater(() -> {
                gameBoard.autoSheepMove();
            });
        } else {
            // 羊走完，切换到狼
            gameBoard.checkWinConditions(steps);
        }
    }

    private void resetGame() {
        steps = 0;
        gameBoard.initGame();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new WolfSheepGame().setVisible(true);
        });
    }
}