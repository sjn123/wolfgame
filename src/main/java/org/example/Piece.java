package org.example;

public enum Piece {
    WOLF("X", true),
    SHEEP("O", false);

    private final String symbol;
    private final boolean isWolf;

    Piece(String symbol, boolean isWolf) {
        this.symbol = symbol;
        this.isWolf = isWolf;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean isWolf() {
        return isWolf;
    }
}