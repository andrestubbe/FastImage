package fastXXX.example;

import fastXXX.FastXXX;

/**
 * Demo class showcasing FastXXX functionality.
 * 
 * Run with: mvn compile exec:java
 */
public class Demo {
    
    public static void main(String[] args) {
        printBanner();
        
        // Demo code here
        System.out.println(FastXXX.hello());
        
        System.out.println("\n✅ Demo complete!");
    }
    
    private static void printBanner() {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║         FastXXX Demo v" + FastXXX.VERSION + "              ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();
    }
}
