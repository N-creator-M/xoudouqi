package com.xoudouqi.game;
import com.xoudouqi.model.*;

public class GameValidator {
private final Board board;
    
    public GameValidator(Board board) {
        this.board = board;
    }
    
    /**
     * Checks if a move is valid
     */
    public boolean isValidMove(Position from, Position to, int currentPlayer) {
        // Check that the positions are valid
        if (!from.isValid() || !to.isValid()) {
            System.err.println("Position invalide");
            return false;
        }
        
        // Check that there is a piece at the starting position
        Board.Piece movingPiece = board.getPiece(from);
        if (movingPiece == null) {
            System.err.println("Aucune pièce à la position " + from);
            return false;
        }
        
        // Check that the piece belongs to the current player
        if (movingPiece.getPlayer() != currentPlayer) {
            System.err.println("Cette pièce ne vous appartient pas");
            return false;
        }
        
        // Make sure you don't go into your own sanctuary
        if (board.isOwnSanctuary(to, currentPlayer)) {
            System.err.println("Vous ne pouvez pas entrer dans votre propre sanctuaire");
            return false;
        }
        
        // Check the type of movement
        if (!isValidMovementPattern(from, to, movingPiece)) {
            return false;
        }
        
        // Check the capture rules
        Board.Piece targetPiece = board.getPiece(to);
        if (targetPiece != null) {
            if (!isValidCapture(movingPiece, targetPiece, to)) {
                return false;
            }
        }
        
        // Special checks for certain areas
        if (!checkSpecialZoneRules(from, to, movingPiece)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks the movement pattern (distance, direction)
     */
    private boolean isValidMovementPattern(Position from, Position to, Board.Piece piece) {
        Animal animal = piece.getAnimal();
        
        // Special case: Lion and Tiger can jump over rivers
        if (animal.canJumpOverRiver()) {
            if (canJumpOverRiver(from, to)) {
                return true; // Valid jump over the river
            }
        }
        
        // Normal move: one adjacent square
        if (!from.isAdjacentTo(to)) {
            System.err.println("Les pièces ne peuvent se déplacer que d'une case");
            return false;
        }
        
        // Check for diagonal movements (prohibited)
        int rowDiff = Math.abs(to.getRow() - from.getRow());
        int colDiff = Math.abs(to.getCol() - from.getCol());
        
        if (rowDiff == 1 && colDiff == 1) {
            System.err.println("Les mouvements diagonaux sont interdits");
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if a jump over the river is valid
     */
    private boolean canJumpOverRiver(Position from, Position to) {
        // Check if it is a horizontal or vertical jump
        int rowDiff = to.getRow() - from.getRow();
        int colDiff = to.getCol() - from.getCol();
        
        // Horizontal jump
        if (rowDiff == 0 && Math.abs(colDiff) > 1) {
            return isValidHorizontalJump(from, to);
        }
        
        // Vertical jump
        if (colDiff == 0 && Math.abs(rowDiff) > 1) {
            return isValidVerticalJump(from, to);
        }
        
        return false;
    }
    
    /**
     * Check a horizontal jump over the river
     */
    private boolean isValidHorizontalJump(Position from, Position to) {
        int row = from.getRow();
        int fromCol = from.getCol();
        int toCol = to.getCol();
        
        //Must be in the river area (lines 3-5)
        if (row < 3 || row > 5) {
            return false;
        }
        
        // Check that we jump over the river
        int minCol = Math.min(fromCol, toCol);
        int maxCol = Math.max(fromCol, toCol);
        
        // Check if there is a rat in the water on the path
        for (int col = minCol + 1; col < maxCol; col++) {
            Position riverPos = new Position(row, col);
            if (board.isRiver(riverPos)) {
                Board.Piece pieceInRiver = board.getPiece(riverPos);
                if (pieceInRiver != null && pieceInRiver.getAnimal() == Animal.RAT) {
                    System.err.println("Un rat bloque le saut dans l'eau");
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Check a vertical jump over the river
     */
    private boolean isValidVerticalJump(Position from, Position to) {
        int col = from.getCol();
        int fromRow = from.getRow();
        int toRow = to.getRow();
        
        // Must be in river columns
        if ((col < 1 || col > 2) && (col < 4 || col > 5)) {
            return false;
        }
        
        // Check that we jump over the river
        int minRow = Math.min(fromRow, toRow);
        int maxRow = Math.max(fromRow, toRow);
        
        // Check if there is a rat in the water on the path
        for (int row = minRow + 1; row < maxRow; row++) {
            Position riverPos = new Position(row, col);
            if (board.isRiver(riverPos)) {
                Board.Piece pieceInRiver = board.getPiece(riverPos);
                if (pieceInRiver != null && pieceInRiver.getAnimal() == Animal.RAT) {
                    System.err.println("Un rat bloque le saut dans l'eau");
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Checks if a capture is valid
     */
    private boolean isValidCapture(Board.Piece attacker, Board.Piece target, Position targetPos) {
        // Cannot capture own pieces
        if (attacker.getPlayer() == target.getPlayer()) {
            System.err.println("Vous ne pouvez pas capturer vos propres pièces");
            return false;
        }
        
        // Check if the target is in a trap
        boolean targetInTrap = board.isTrap(targetPos, target.getPlayer());
        
        // Check the capture rules
        if (!attacker.getAnimal().canCapture(target.getAnimal(), targetInTrap)) {
            System.err.println("Cette pièce ne peut pas capturer la pièce cible");
            return false;
        }
        
        return true;
    }
    
    /**
     * Check the special rules of the areas (rivers, traps, etc.)
     */
    private boolean checkSpecialZoneRules(Position from, Position to, Board.Piece piece) {
        // Rules for Rivers
        if (board.isRiver(to)) {
            if (!piece.getAnimal().canSwim()) {
                System.err.println("Seul le rat peut aller dans l'eau");
                return false;
            }
        }
        
        // Special rule: the rat cannot capture while coming out of the water
        if (board.isRiver(from) && piece.getAnimal() == Animal.RAT) {
            Board.Piece targetPiece = board.getPiece(to);
            if (targetPiece != null && !board.isRiver(to)) {
                System.err.println("Le rat ne peut pas capturer en sortant de l'eau");
                return false;
            }
        }
        
        return true;
    }

}
