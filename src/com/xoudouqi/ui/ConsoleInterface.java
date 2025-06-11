package com.xoudouqi.ui;

import com.xoudouqi.database.DatabaseManager;
import com.xoudouqi.game.GameEngine;
import java.sql.SQLException;
import java.util.Scanner;

public class ConsoleInterface {
	private Scanner scanner;
    private DatabaseManager dbManager;
    private Integer currentPlayerId;
    private String currentPlayerName;
    
    public ConsoleInterface() {
        this.scanner = new Scanner(System.in);
        this.dbManager = DatabaseManager.getInstance();
    }
    
    public void start() {
        displayWelcome();
        
        try {
            // Authentication
            if (!authenticateUser()) {
                System.out.println("Échec de l'authentification. Au revoir !");
                return;
            }
            
            // Menu principal
            mainMenu();
            
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
    
    private void displayWelcome() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║          XOU DOU QI - JUNGLE         ║");
        System.out.println("║         Jeu des Échecs Chinois       ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();
    }
    
    private boolean authenticateUser() throws SQLException {
        while (true) {
            System.out.println("=== AUTHENTIFICATION ===");
            System.out.println("1. Se connecter");
            System.out.println("2. Créer un compte");
            System.out.println("3. Quitter");
            System.out.print("Votre choix: ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    if (loginUser()) return true;
                    break;
                case "2":
                    if (registerUser()) return true;
                    break;
                case "3":
                    return false;
                default:
                    System.out.println("Choix invalide !");
            }
        }
    }
    
    private boolean loginUser() throws SQLException {
        System.out.print("Nom d'utilisateur: ");
        String username = scanner.nextLine().trim();
        
        System.out.print("Mot de passe: ");
        String password = scanner.nextLine().trim();
        
        Integer playerId = dbManager.authenticatePlayer(username, password);
        if (playerId != null) {
            this.currentPlayerId = playerId;
            this.currentPlayerName = username;
            System.out.println("Connexion réussie ! Bienvenue " + username);
            return true;
        } else {
            System.out.println("Nom d'utilisateur ou mot de passe incorrect !");
            return false;
        }
    }
    
    private boolean registerUser() throws SQLException {
        System.out.print("Nouveau nom d'utilisateur: ");
        String username = scanner.nextLine().trim();
        
        if (username.length() < 3) {
            System.out.println("Le nom d'utilisateur doit contenir au moins 3 caractères !");
            return false;
        }
        
        System.out.print("Mot de passe: ");
        String password = scanner.nextLine().trim();
        
        if (password.length() < 4) {
            System.out.println("Le mot de passe doit contenir au moins 4 caractères !");
            return false;
        }
        
        if (dbManager.createPlayer(username, password)) {
            System.out.println("Compte créé avec succès !");
            // Connect automatically
            Integer playerId = dbManager.authenticatePlayer(username, password);
            this.currentPlayerId = playerId;
            this.currentPlayerName = username;
            return true;
        } else {
            System.out.println("Ce nom d'utilisateur existe déjà !");
            return false;
        }
    }
    
    private void mainMenu() throws SQLException {
        while (true) {
            System.out.println("\n=== MENU PRINCIPAL ===");
            System.out.println("Connecté en tant que: " + currentPlayerName);
            System.out.println("1. Nouvelle partie");
            System.out.println("2. Voir mes statistiques");
            System.out.println("3. Historique des parties");
            System.out.println("4. Règles du jeu");
            System.out.println("5. Quitter");
            System.out.print("Votre choix: ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    startNewGame();
                    break;
                case "2":
                    showPlayerStats();
                    break;
                case "3":
                    showGameHistory();
                    break;
                case "4":
                    showGameRules();
                    break;
                case "5":
                    System.out.println("Au revoir " + currentPlayerName + " !");
                    return;
                default:
                    System.out.println("Choix invalide !");
            }
        }
    }
    
    private void startNewGame() throws SQLException {
        System.out.println("\n=== NOUVELLE PARTIE ===");
        System.out.println("Mode de jeu: Deux joueurs sur la même machine");
        
        // Authenticate the second player
        System.out.println("Authentification du Joueur 2:");
        Integer player2Id = authenticateSecondPlayer();
        
        if (player2Id == null) {
            System.out.println("Impossible de démarrer la partie sans le joueur 2");
            return;
        }
        
        // Start the game engine
        GameEngine game = new GameEngine(currentPlayerId, player2Id);
        playGame(game);
    }
    
    private Integer authenticateSecondPlayer() throws SQLException {
        while (true) {
            System.out.print("Nom d'utilisateur du Joueur 2 (ou 'annuler'): ");
            String username = scanner.nextLine().trim();
            
            if (username.equalsIgnoreCase("annuler")) {
                return null;
            }
            
            if (username.equals(currentPlayerName)) {
                System.out.println("Le Joueur 2 doit être différent du Joueur 1 !");
                continue;
            }
            
            System.out.print("Mot de passe: ");
            String password = scanner.nextLine().trim();
            
            Integer playerId = dbManager.authenticatePlayer(username, password);
            if (playerId != null) {
                System.out.println("Joueur 2 connecté: " + username);
                return playerId;
            } else {
                System.out.println("Authentification échouée !");
            }
        }
    }
    
    private void playGame(GameEngine game) {
        System.out.println("\n🎮 DÉBUT DE LA PARTIE ! 🎮");
        game.displayHelp();
        
        while (!game.isGameEnded()) {
            game.displayGameState();
            
            System.out.print("\nCommande: ");
            String input = scanner.nextLine().trim().toUpperCase();
            
            if (input.isEmpty()) continue;
            
            // Process orders
            if (input.equals("QUIT")) {
                System.out.println("Partie abandonnée !");
                break;
            } else if (input.equals("HELP")) {
                game.displayHelp();
            } else if (input.equals("HISTORY")) {
                try {
                    showGameHistory();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de l'affichage de l'historique: " + e.getMessage());
                }
            } else if (input.equals("STATS")) {
                try {
                    showPlayerStats();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de l'affichage des stats: " + e.getMessage());
                }
            } else {
                // Treat movements
                handleMoveCommand(game, input);
            }
        }
        
        if (game.isGameEnded()) {
            game.displayGameState();
            System.out.println("\nMerci d'avoir joué ! Retour au menu principal...");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private void handleMoveCommand(GameEngine game, String input) {
        String[] parts;
        
        // Support for "MOVE A1 B1" and "A1 B1"
        if (input.startsWith("MOVE ")) {
            parts = input.substring(5).split("\\s+");
        } else {
            parts = input.split("\\s+");
        }
        
        if (parts.length != 2) {
            System.out.println("Format invalide ! Utilisez: A1 B1 ou MOVE A1 B1");
            return;
        }
        
        String from = parts[0];
        String to = parts[1];
        
        if (!game.makeMove(from, to)) {
            System.out.println("Mouvement invalide ! Réessayez.");
        }
    }
    
    private void showPlayerStats() throws SQLException {
        DatabaseManager.PlayerInfo playerInfo = dbManager.getPlayerInfo(currentPlayerId);
        if (playerInfo != null) {
            playerInfo.display();
        } else {
            System.out.println("Erreur lors du chargement des statistiques");
        }
    }
    
    private void showGameHistory() throws SQLException {
        dbManager.showPlayerHistory(currentPlayerId);
    }
    
    private void showGameRules() {
    	System.out.println("\n" + "=".repeat(60));
        System.out.println("RÈGLES DU JEU XOU DOU QI (JUNGLE)");
        System.out.println("=".repeat(60));
        System.out.println("\nLe Jungle ou Xiangqi est un jeu d'échecs chinois avec des pièces animales.");
        System.out.println("Chaque animal a des capacités différentes et se déplace selon des règles spécifiques.");
        System.out.println("\nRègles de base:");
        System.out.println("- Le but est de capturer le lion adverse ou d'atteindre la tanière ennemie");
        System.out.println("- Les pièces se déplacent d'une case à la fois (sauf exceptions)");
        System.out.println("- Les pièces ne peuvent pas sauter par-dessus d'autres pièces");
        System.out.println("- Les animaux plus forts capturent les plus faibles");
        System.out.println("\nHiérarchie des animaux (du plus fort au plus faible):");
        System.out.println("1. Éléphant");
        System.out.println("2. Lion");
        System.out.println("3. Tigre");
        System.out.println("4. Léopard");
        System.out.println("5. Chien");
        System.out.println("6. Loup");
        System.out.println("7. Chat");
        System.out.println("8. Rat");
        System.out.println("\nCommandes spéciales:");
        System.out.println("- HELP: Affiche l'aide");
        System.out.println("- HISTORY: Affiche l'historique des parties");
        System.out.println("- STATS: Affiche vos statistiques");
        System.out.println("- QUIT: Quitte la partie en cours");
        System.out.println("\nPour déplacer une pièce, entrez les coordonnées (ex: A1 B1)");
        System.out.println("=".repeat(60));
        System.out.println("Appuyez sur Entrée pour retourner au menu...");
        scanner.nextLine();
    }

}
