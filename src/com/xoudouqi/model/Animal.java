package com.xoudouqi.model;

public enum Animal {
	ELEPHANT(1, "E", "Éléphant"),
    LION(2, "L", "Lion"),
    TIGER(3, "T", "Tigre"),
    PANTHER(4, "P", "Panthère"),
    DOG(5, "D", "Chien"),
    WOLF(6, "W", "Loup"),
    CAT(7, "C", "Chat"),
    RAT(8, "R", "Rat");
    
    private final int rank;
    private final String symbol;
    private final String name;
    
    Animal(int rank, String symbol, String name) {
        this.rank = rank;
        this.symbol = symbol;
        this.name = name;
    }
    
    public int getRank() { return rank; }
    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    
    /**
     * Check if this animal can capture the target animal
     */
    public boolean canCapture(Animal target, boolean targetInTrap) {
        // In a trap, all pieces can be captured
        if (targetInTrap) return true;
        
        // The rat can capture the elephant (special exception)
        if (this == RAT && target == ELEPHANT) return true;
        
        // The elephant cannot be caught by the rat if the rat is not in the water
        if (this == ELEPHANT && target == RAT) return false;
        
        // General rule: can capture if rank lower or equal
        return this.rank <= target.rank;
    }
    
    /**
     * Check if the animal can jump over rivers
     */
    public boolean canJumpOverRiver() {
        return this == LION || this == TIGER;
    }
    
    /**
     * Check if the animal can move in the water
     */
    public boolean canSwim() {
        return this == RAT;
    }

}
