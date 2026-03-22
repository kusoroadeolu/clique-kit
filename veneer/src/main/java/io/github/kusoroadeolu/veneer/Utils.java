package io.github.kusoroadeolu.veneer;

class Utils {
    private Utils(){
    }

    public static String formatNoTo3dp(int lineNo){
      return String.format("%3d | ", lineNo);
    }
}
