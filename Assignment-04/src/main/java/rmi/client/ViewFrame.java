package rmi.client;

import rmi.model.Logics;
import rmi.util.Position;
import javax.swing.*;
import java.awt.*;

public class ViewFrame extends JFrame {
    private final GameClientController controller;
    private final CardLayout cardLayout;
    private final JPanel mainContainer;

    private JTextField codeField;

    private JButton[][] boardButtons;
    private String title = "";

    public ViewFrame(GameClientController controller) {
        this.controller = controller;
        this.cardLayout = new CardLayout();
        this.mainContainer = new JPanel(cardLayout);

        refreshTitle("", "");
        this.setSize(400, 400);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        setupLobbyPanel();
        setupGamePanel();

        this.add(mainContainer);
        showLobbyScreen();
    }

    private void setupLobbyPanel() {
        JPanel lobbyPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label = new JLabel("Lobby Code:", SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        lobbyPanel.add(label, gbc);

        codeField = new JTextField(15);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        lobbyPanel.add(codeField, gbc);

        JButton createBtn = new JButton("Create Lobby");
        createBtn.addActionListener(e -> controller.handleCreateLobby(codeField.getText().trim()));
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        lobbyPanel.add(createBtn, gbc);

        JButton joinBtn = new JButton("Join Lobby");
        joinBtn.addActionListener(e -> controller.handleJoinLobby(codeField.getText().trim()));
        gbc.gridx = 1; gbc.gridy = 2;
        lobbyPanel.add(joinBtn, gbc);

        mainContainer.add(lobbyPanel, "LOBBY_SCREEN");
    }

    private void setupGamePanel() {
        JPanel gamePanel = new JPanel(new BorderLayout());
        JPanel gameGridPanel = new JPanel(new GridLayout(Logics.BOARD_SIZE, Logics.BOARD_SIZE));
        boardButtons = new JButton[Logics.BOARD_SIZE][Logics.BOARD_SIZE];

        for (int r = 0; r < Logics.BOARD_SIZE; r++) {
            for (int c = 0; c < Logics.BOARD_SIZE; c++) {
                final int row = r;
                final int col = c;
                JButton btn = new JButton("");
                btn.setFont(new Font("Arial", Font.BOLD, 40));
                btn.addActionListener(e -> controller.handleCellClicked(new Position(row, col)));
                boardButtons[r][c] = btn;
                gameGridPanel.add(btn);
            }
        }
        gamePanel.add(gameGridPanel, BorderLayout.CENTER);
        mainContainer.add(gamePanel, "GAME_SCREEN");
    }

    public void showLobbyScreen() {
        codeField.setText("");
        cardLayout.show(mainContainer, "LOBBY_SCREEN");
    }

    public void showGameScreen() {
        for (int r = 0; r < Logics.BOARD_SIZE; r++) {
            for (int c = 0; c < Logics.BOARD_SIZE; c++) {
                boardButtons[r][c].setText("");
                boardButtons[r][c].setEnabled(true);
            }
        }
        cardLayout.show(mainContainer, "GAME_SCREEN");
    }

    public void setCellText(Position pos, String symbol) {
        boardButtons[pos.x()][pos.y()].setText(symbol);
        boardButtons[pos.x()][pos.y()].setEnabled(false);
    }

    public void setGridEnabled(boolean enabled) {
        for (int r = 0; r < Logics.BOARD_SIZE; r++) {
            for (int c = 0; c < Logics.BOARD_SIZE; c++) {
                if (boardButtons[r][c].getText().isEmpty()) {
                    boardButtons[r][c].setEnabled(enabled);
                }
            }
        }
    }

    public void refreshTitle(String lobbyCode, String playerSymbol) {
        String lobby = lobbyCode.isEmpty() ? "" : "Lobby: "+lobbyCode;
        String symbol = playerSymbol.isEmpty() ? "" : " | Player: "+playerSymbol;
        this.title = "TicTacToe "+ (lobby.isEmpty() && symbol.isEmpty() ? "Client" : "- "+lobby+symbol);
        this.setTitle(this.title);
    }

    public void updateTurnTitle(boolean yourTurn) {
        this.setTitle(this.title + (yourTurn ? " ~ Your Turn": ""));
    }


    public void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }
}