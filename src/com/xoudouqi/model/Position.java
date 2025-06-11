package com.xoudouqi.model;

public class Position {
	private final int row;
    private final int col;
    
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    public int getRow() { return row; }
    public int getCol() { return col; }
    
    /**
     * Converts a position into chess notation (eg: A1, B2)
     */
    public String toChessNotation() {
        char colLetter = (char)('A' + col);
        return colLetter + String.valueOf(row + 1);
    }
    
    /**
     * Create a position from chess notation
     */
    public static Position fromChessNotation(String notation) {
        if (notation.length() != 2) {
            throw new IllegalArgumentException("Format invalide: " + notation);
        }
        
        char colChar = notation.charAt(0);
        char rowChar = notation.charAt(1);
        
        if (colChar < 'A' || colChar > 'G') {
            throw new IllegalArgumentException("Colonne invalide: " + colChar);
        }
        
        if (rowChar < '1' || rowChar > '9') {
            throw new IllegalArgumentException("Ligne invalide: " + rowChar);
        }
        
        int col = colChar - 'A';
        int row = rowChar - '1';
        
        return new Position(row, col);
    }
    
    /**
     * Checks if the position is valid on the 9x7 board
     */
    public boolean isValid() {
        return row >= 0 && row < 9 && col >= 0 && col < 7;
    }
    
    /**
     * Calculates the Manhattan distance between two positions
     */
    public int distanceTo(Position other) {
        return Math.abs(this.row - other.row) + Math.abs(this.col - other.col);
    }
    
    /**
     * Checks if the move is adjacent (one square)
     */
    public boolean isAdjacentTo(Position other) {
        return distanceTo(other) == 1;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return row == position.row && col == position.col;
    }
    
    @Override
    public int hashCode() {
        return row * 7 + col;
    }
    
    @Override
    public String toString() {
        return toChessNotation();
    }

}
