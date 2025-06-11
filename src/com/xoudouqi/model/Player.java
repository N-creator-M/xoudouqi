package com.xoudouqi.model;

public class Player {
	private int id;
    private String username;
    private String password;
    private int wins;
    private int losses;
    
    public Player() {}
    
    public Player(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }
    
    public double getWinRate() {
        if (wins + losses == 0) return 0;
        return (double) wins / (wins + losses) * 100;
    }
    
    @Override
    public String toString() {
        return username + " - Victoires: " + wins + " | Défaites: " + losses + 
               " | Taux: " + String.format("%.1f", getWinRate()) + "%";
    }

}
