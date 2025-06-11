package com.xoudouqi.game;

import com.xoudouqi.model.*;
import com.xoudouqi.database.DatabaseManager;
import java.sql.SQLException;

public class GameEngine {
	
	private Board board;
    private int currentPlayer;
    private int gameId;
    private int player1Id, player2Id;
    private int moveCount;
    private boolean gameEnded;
    private Integer winnerId;
    
    public GameEngine(int player1Id, int player2Id) throws SQLException {
        this.board = new Board();
        this.currentPlayer = 1;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.moveCount = 0;
        this.gameEnded = false;
        this.winnerId = null;
        
        // Create the party entry in the database
        this.gameId = DatabaseManager.getInstance().createGame(player1Id, player2Id);
    }
    
    public Board getBoard() { return board; }
    public int getCurrentPlayer() { return currentPlayer; }
    public boolean isGameEnded() { return gameEnded; }
    public Integer getWinnerId() { return winnerId; }
    public int getMoveCount() { return moveCount; }
    
    /**
     * Perform a movement
     */
    public boolean makeMove(String fromNotation, String toNotation) {
        try {
            Position from = Position.fromChessNotation(fromNotation.toUpperCase());
            Position to = Position.fromChessNotation(toNotation.toUpperCase());
            
            // Validate the movement
            GameValidator validator = new GameValidator(board);
            if (!validator.isValidMove(from, to, currentPlayer)) {
                return false;
            }
            
            // Perform the movement
            Board.Piece movingPiece = board.getPiece(from);
            Board.Piece capturedPiece = board.getPiece(to);
            
            board.setPiece(to, movingPiece);
            board.setPiece(from, null);
            
            // Record the movement in the database
            try {
                DatabaseManager.getInstance().recordMove(
                    gameId, 
                    currentPlayer == 1 ? player1Id : player2Id,
                    ++moveCount,
                    fromNotation.toUpperCase(),
                    toNotation.toUpperCase(),
                    movingPiece.toString(),
                    capturedPiece != null ? capturedPiece.toString() : null
                );
            } catch (SQLException e) {
                System.err.println("Erreur lors de l'enregistrement du mouvement: " + e.getMessage());
            }
            
            // Check victory conditions
            checkWinConditions();
            
            // Change player
            if (!gameEnded) {
                currentPlayer = (currentPlayer == 1) ? 2 : 1;
            }
            
            return true;
            
        } catch (IllegalArgumentException e) {
            System.err.println("Position invalide: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check the victory conditions
     */
    private void checkWinConditions() {
        // Check if a player has reached the opposing sanctuary
        for (Position pos : getAllPiecePositions()) {
            Board.Piece piece = board.getPiece(pos);
            if (piece != null && board.isSanctuary(pos, piece.getPlayer())) {
                endGame(piece.getPlayer());
                return;
            }
        }
        
        // Other victory conditions can be added here
        // (for example, if a player has no more pieces)
    }
    
    /**
     * Finish the game
     */
    private void endGame(int winner) {
        this.gameEnded = true;
        this.winnerId = winner;
        
        try {
            DatabaseManager.getInstance().finishGame(
                gameId, 
                winner == 1 ? player1Id : player2Id, 
                moveCount
            );
        } catch (SQLException e) {
            System.err.println("Erreur lors de la finalisation de la partie: " + e.getMessage());
        }
    }
    
    /**
     * Recovers all occupied positions on the board
     */
    private Position[] getAllPiecePositions() {
        java.util.List<Position> positions = new java.util.ArrayList<>();
        
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                Position pos = new Position(row, col);
                if (!board.isEmpty(pos)) {
                    positions.add(pos);
                }
            }
        }
        
        return positions.toArray(new Position[0]);
    }
    
    /**
     * Displays the current state of the game
     */
    public void displayGameState() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("XOU DOU QI - Tour du joueur " + currentPlayer);
        System.out.println("Nombre de coups: " + moveCount);
        System.out.println("=".repeat(50));
        
        board.display();
        
        if (gameEnded) {
            System.out.println("\n PARTIE TERMINÉE ! ");
            if (winnerId != null) {
                System.out.println("Gagnant: Joueur " + (winnerId.equals(player1Id) ? 1 : 2));
            } else {
                System.out.println("Match nul !");
            }
        } else {
            System.out.println("\nC'est au tour du joueur " + currentPlayer);
            System.out.println("Tapez votre mouvement (ex: A1 B1) ou 'help' pour l'aide");
        }
    }
    
    /**
     * Shows game help
     */
    public void displayHelp() {
        System.out.println("\n=== AIDE - XOU DOU QI ===");
        System.out.println("Commandes disponibles:");
        System.out.println("• MOVE [from] [to] - Déplacer une pièce (ex: MOVE A1 B1)");
        System.out.println("• [from] [to] - Raccourci pour MOVE (ex: A1 B1)");
        System.out.println("• HELP - Afficher cette aide");
        System.out.println("• HISTORY - Voir l'historique des parties");
        System.out.println("• STATS - Voir vos statistiques");
        System.out.println("• QUIT - Quitter le jeu");
        
        System.out.println("\nRègles du jeu:");
        System.out.println("• Objectif: Atteindre le sanctuaire adverse (##)");
        System.out.println("• Les pièces se déplacent d'une case (horizontal/vertical)");
        System.out.println("• Hiérarchie: Éléphant > Lion > Tigre > Panthère > Chien > Loup > Chat > Rat");
        System.out.println("• Exception: Le Rat peut capturer l'Éléphant");
        System.out.println("• Lion/Tigre peuvent sauter par-dessus les rivières");
        System.out.println("• Seul le Rat peut aller dans l'eau (~~)");
        System.out.println("• Les pièges (XX) affaiblissent les pièces ennemies");
        
        System.out.println("\nPositions: A-G (colonnes), 1-9 (lignes)");
        System.out.println("Exemple: A1 = coin supérieur gauche");
    }

}
