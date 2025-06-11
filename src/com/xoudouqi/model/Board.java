package com.xoudouqi.model;

import java.util.HashMap;
import java.util.Map;

public class Board {
	
	public static final int ROWS = 9;
    public static final int COLS = 7;
    
    // Special areas
    private static final Position[] RIVER_POSITIONS = {
        new Position(3, 1), new Position(3, 2), new Position(4, 1), new Position(4, 2), new Position(5, 1), new Position(5, 2),
        new Position(3, 4), new Position(3, 5), new Position(4, 4), new Position(4, 5), new Position(5, 4), new Position(5, 5)
    };
    
    private static final Position[] TRAP_POSITIONS_PLAYER1 = {
        new Position(0, 2), new Position(0, 4), new Position(1, 3)
    };
    
    private static final Position[] TRAP_POSITIONS_PLAYER2 = {
        new Position(8, 2), new Position(8, 4), new Position(7, 3)
    };
    
    private static final Position SANCTUARY_PLAYER1 = new Position(0, 3);
    private static final Position SANCTUARY_PLAYER2 = new Position(8, 3);
    
    // Tray with pieces
    private final Map<Position, Piece> pieces;
    
    public Board() {
        this.pieces = new HashMap<>();
        initializeBoard();
    }
    
    /**
     * Initializes the board with the pieces in their starting positions
     */
    private void initializeBoard() {
        // Player 1's pieces (top)
        pieces.put(new Position(0, 0), new Piece(Animal.LION, 1));
        pieces.put(new Position(0, 6), new Piece(Animal.TIGER, 1));
        pieces.put(new Position(1, 1), new Piece(Animal.DOG, 1));
        pieces.put(new Position(1, 5), new Piece(Animal.CAT, 1));
        pieces.put(new Position(2, 0), new Piece(Animal.RAT, 1));
        pieces.put(new Position(2, 2), new Piece(Animal.PANTHER, 1));
        pieces.put(new Position(2, 4), new Piece(Animal.WOLF, 1));
        pieces.put(new Position(2, 6), new Piece(Animal.ELEPHANT, 1));
        
        // Player 2's pieces (bottom)
        pieces.put(new Position(8, 0), new Piece(Animal.TIGER, 2));
        pieces.put(new Position(8, 6), new Piece(Animal.LION, 2));
        pieces.put(new Position(7, 1), new Piece(Animal.CAT, 2));
        pieces.put(new Position(7, 5), new Piece(Animal.DOG, 2));
        pieces.put(new Position(6, 0), new Piece(Animal.ELEPHANT, 2));
        pieces.put(new Position(6, 2), new Piece(Animal.WOLF, 2));
        pieces.put(new Position(6, 4), new Piece(Animal.PANTHER, 2));
        pieces.put(new Position(6, 6), new Piece(Animal.RAT, 2));
    }
    
    public Piece getPiece(Position position) {
        return pieces.get(position);
    }
    
    public void setPiece(Position position, Piece piece) {
        if (piece == null) {
            pieces.remove(position);
        } else {
            pieces.put(position, piece);
        }
    }
    
    public boolean isEmpty(Position position) {
        return !pieces.containsKey(position);
    }
    
    public boolean isRiver(Position position) {
        for (Position riverPos : RIVER_POSITIONS) {
            if (riverPos.equals(position)) return true;
        }
        return false;
    }
    
    public boolean isTrap(Position position, int player) {
        Position[] trapPositions = (player == 1) ? TRAP_POSITIONS_PLAYER2 : TRAP_POSITIONS_PLAYER1;
        for (Position trapPos : trapPositions) {
            if (trapPos.equals(position)) return true;
        }
        return false;
    }
    
    public boolean isSanctuary(Position position, int player) {
        return (player == 1 && position.equals(SANCTUARY_PLAYER2)) ||
               (player == 2 && position.equals(SANCTUARY_PLAYER1));
    }
    
    public boolean isOwnSanctuary(Position position, int player) {
        return (player == 1 && position.equals(SANCTUARY_PLAYER1)) ||
               (player == 2 && position.equals(SANCTUARY_PLAYER2));
    }
    
    /**
     * Displays the board in console mode
     */
    public void display() {
        System.out.println("  A B C D E F G");
        System.out.println("  =============");
        
        for (int row = 0; row < ROWS; row++) {
            System.out.print((row + 1) + "|");
            for (int col = 0; col < COLS; col++) {
                Position pos = new Position(row, col);
                Piece piece = getPiece(pos);
                
                if (piece != null) {
                    String symbol = piece.getAnimal().getSymbol();
                    symbol += piece.getPlayer();
                    System.out.print(symbol);
                } else if (isRiver(pos)) {
                    System.out.print("~~");
                } else if (pos.equals(SANCTUARY_PLAYER1) || pos.equals(SANCTUARY_PLAYER2)) {
                    System.out.print("##");
                } else if (isTrap(pos, 1) || isTrap(pos, 2)) {
                    System.out.print("XX");
                } else {
                    System.out.print("  ");
                }
                
                if (col < COLS - 1) System.out.print(" ");
            }
            System.out.println("|");
        }
        
        System.out.println("  =============");
        System.out.println("Légende: ## = Sanctuaire, XX = Piège, ~~ = Rivière");
        System.out.println("Animaux: E=Éléphant, L=Lion, T=Tigre, P=Panthère, D=Chien, W=Loup, C=Chat, R=Rat");
    }
    
    /**
     * Inner class to represent a part
     */
    public static class Piece {
        private final Animal animal;
        private final int player;
        
        public Piece(Animal animal, int player) {
            this.animal = animal;
            this.player = player;
        }
        
        public Animal getAnimal() { return animal; }
        public int getPlayer() { return player; }
        
        @Override
        public String toString() {
            return animal.getSymbol() + player;
        }
    }

}
