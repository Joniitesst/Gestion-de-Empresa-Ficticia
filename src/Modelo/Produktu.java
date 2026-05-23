package Modelo;

public class Produktu {
    private int id;
    private String izena;
    private String deskribapena;
    private double salneurria;
    private int idKategoria;
    private int stockTotal; // hemen stockaren prezioa kalkulatzen dugu

    public Produktu(int id, String izena, String deskribapena, double salneurria, int idKategoria, int stockTotal) {
        this.id = id;
        this.izena = izena;
        this.deskribapena = deskribapena;
        this.salneurria = salneurria;
        this.idKategoria = idKategoria;
        this.stockTotal = stockTotal;
    }

    public int getId() { return id; }
    public String getIzena() { return izena; }
    public String getDeskribapena() { return deskribapena; }
    public double getSalneurria() { return salneurria; }
    public int getIdKategoria() { return idKategoria; }
    public int getStockTotal() { return stockTotal; }
}