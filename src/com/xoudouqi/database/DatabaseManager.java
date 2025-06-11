package com.xoudouqi.database;

import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatabaseManager {
	private static final String DB_URL = "jdbc:sqlite:lib/xoudouqi.db";
    private static DatabaseManager instance;
    private Connection connection;
    
    private DatabaseManager() {}
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    public void initDatabase() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
        createTables();
    }
    
    private void createTables() throws SQLException {
        String createPlayersTable = """
            CREATE TABLE IF NOT EXISTS players (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(100) NOT NULL,
                wins INTEGER DEFAULT 0,
                losses INTEGER DEFAULT 0,
                draws INTEGER DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        
        String createGamesTable = """
            CREATE TABLE IF NOT EXISTS games (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                player1_id INTEGER NOT NULL,
                player2_id INTEGER NOT NULL,
                winner_id INTEGER,
                game_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                moves_count INTEGER DEFAULT 0,
                FOREIGN KEY (player1_id) REFERENCES players(id),
                FOREIGN KEY (player2_id) REFERENCES players(id),
                FOREIGN KEY (winner_id) REFERENCES players(id)
            )
            """;
        
        String createMovesTable = """
            CREATE TABLE IF NOT EXISTS game_moves (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                game_id INTEGER NOT NULL,
                player_id INTEGER NOT NULL,
                move_number INTEGER NOT NULL,
                from_position VARCHAR(2) NOT NULL,
                to_position VARCHAR(2) NOT NULL,
                piece_moved VARCHAR(10) NOT NULL,
                piece_captured VARCHAR(10),
                move_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (game_id) REFERENCES games(id),
                FOREIGN KEY (player_id) REFERENCES players(id)
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createPlayersTable);
            stmt.execute(createGamesTable);
            stmt.execute(createMovesTable);
        }
    }
    
    /**
     * Crée un nouveau compte joueur
     */
    public boolean createPlayer(String username, String password) throws SQLException {
        String hashedPassword = hashPassword(password);
        String sql = "INSERT INTO players (username, password) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                return false; // Nom d'utilisateur déjà existant
            }
            throw e;
        }
    }
    
    /**
     * Authentifie un joueur
     */
    public Integer authenticatePlayer(String username, String password) throws SQLException {
        String hashedPassword = hashPassword(password);
        String sql = "SELECT id FROM players WHERE username = ? AND password = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return null;
    }
    
    /**
     * Récupère les informations d'un joueur
     */
    public PlayerInfo getPlayerInfo(int playerId) throws SQLException {
        String sql = "SELECT username, wins, losses, draws FROM players WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, playerId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new PlayerInfo(
                        rs.getString("username"),
                        rs.getInt("wins"),
                        rs.getInt("losses"),
                        rs.getInt("draws")
                    );
                }
            }
        }
        return null;
    }
    
    /**
     * Save a new game
     */
    public int createGame(int player1Id, int player2Id) throws SQLException {
        String sql = "INSERT INTO games (player1_id, player2_id) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, player1Id);
            pstmt.setInt(2, player2Id);
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Échec de création de la partie");
    }
    
    /**
     * Updates the result of a game
     */
    public void finishGame(int gameId, Integer winnerId, int movesCount) throws SQLException {
        String sql = "UPDATE games SET winner_id = ?, moves_count = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            if (winnerId != null) {
                pstmt.setInt(1, winnerId);
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setInt(2, movesCount);
            pstmt.setInt(3, gameId);
            pstmt.executeUpdate();
        }
        
        // Update player statistics
        updatePlayerStats(gameId, winnerId);
    }
    
    private void updatePlayerStats(int gameId, Integer winnerId) throws SQLException {
        String getPlayersSQL = "SELECT player1_id, player2_id FROM games WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(getPlayersSQL)) {
            pstmt.setInt(1, gameId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int player1Id = rs.getInt("player1_id");
                    int player2Id = rs.getInt("player2_id");
                    
                    if (winnerId == null) {
                        // Match nul
                        updatePlayerStat(player1Id, "draws");
                        updatePlayerStat(player2Id, "draws");
                    } else if (winnerId == player1Id) {
                        updatePlayerStat(player1Id, "wins");
                        updatePlayerStat(player2Id, "losses");
                    } else {
                        updatePlayerStat(player2Id, "wins");
                        updatePlayerStat(player1Id, "losses");
                    }
                }
            }
        }
    }
    
    private void updatePlayerStat(int playerId, String statType) throws SQLException {
        String sql = "UPDATE players SET " + statType + " = " + statType + " + 1 WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, playerId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Record a movement
     */
    public void recordMove(int gameId, int playerId, int moveNumber, String fromPos, String toPos, 
                          String pieceMoved, String pieceCaptured) throws SQLException {
        String sql = """
            INSERT INTO game_moves (game_id, player_id, move_number, from_position, to_position, 
                                   piece_moved, piece_captured) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, gameId);
            pstmt.setInt(2, playerId);
            pstmt.setInt(3, moveNumber);
            pstmt.setString(4, fromPos);
            pstmt.setString(5, toPos);
            pstmt.setString(6, pieceMoved);
            pstmt.setString(7, pieceCaptured);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Retrieves a player's game history
     */
    public void showPlayerHistory(int playerId) throws SQLException {
        String sql = """
            SELECT g.id, p1.username as player1, p2.username as player2, 
                   pw.username as winner, g.game_date, g.moves_count
            FROM games g
            JOIN players p1 ON g.player1_id = p1.id
            JOIN players p2 ON g.player2_id = p2.id
            LEFT JOIN players pw ON g.winner_id = pw.id
            WHERE g.player1_id = ? OR g.player2_id = ?
            ORDER BY g.game_date DESC
            LIMIT 10
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, playerId);
            pstmt.setInt(2, playerId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n=== HISTORIQUE DES PARTIES ===");
                System.out.println("ID   | Joueur 1 vs Joueur 2        | Gagnant      | Date       | Coups");
                System.out.println("-----|------------------------------|--------------|------------|-------");
                
                while (rs.next()) {
                    int gameId = rs.getInt("id");
                    String player1 = rs.getString("player1");
                    String player2 = rs.getString("player2");
                    String winner = rs.getString("winner");
                    String date = rs.getString("game_date").substring(0, 10);
                    int moves = rs.getInt("moves_count");
                    
                    System.out.printf("%-4d | %-15s vs %-10s | %-12s | %-10s | %-5d\n",
                        gameId, player1, player2, 
                        winner != null ? winner : "Match nul", 
                        date, moves);
                }
            }
        }
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur de hachage du mot de passe", e);
        }
    }
    
    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    
    /**
     * Classe pour stocker les informations d'un joueur
     */
    public static class PlayerInfo {
        private final String username;
        private final int wins;
        private final int losses;
        private final int draws;
        
        public PlayerInfo(String username, int wins, int losses, int draws) {
            this.username = username;
            this.wins = wins;
            this.losses = losses;
            this.draws = draws;
        }
        
        public String getUsername() { return username; }
        public int getWins() { return wins; }
        public int getLosses() { return losses; }
        public int getDraws() { return draws; }
        public int getTotalGames() { return wins + losses + draws; }
        
        public void display() {
            System.out.println("\n=== STATISTIQUES DE " + username.toUpperCase() + " ===");
            System.out.println("Victoires: " + wins);
            System.out.println("Défaites: " + losses);
            System.out.println("Matchs nuls: " + draws);
            System.out.println("Total parties: " + getTotalGames());
            if (getTotalGames() > 0) {
                double winRate = (double) wins / getTotalGames() * 100;
                System.out.printf("Taux de victoire: %.1f%%\n", winRate);
            }
        }
    }

}
