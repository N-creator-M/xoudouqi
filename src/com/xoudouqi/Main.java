package com.xoudouqi;

import com.xoudouqi.database.DatabaseManager;
import com.xoudouqi.ui.ConsoleInterface;

public class Main {
	public static void main(String[] args) {
        try {
            // Initialize the database
            DatabaseManager.getInstance().initDatabase();
            
            // Lancer l'interface console
            ConsoleInterface console = new ConsoleInterface();
            console.start();
            
        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage : " + e.getMessage());
            e.printStackTrace();
        }
    }

}
