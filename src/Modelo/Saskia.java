package Modelo;

import java.util.ArrayList;
import java.util.List;

public class Saskia {
    // Lista de productos elegidos y sus cantidades
    private List<Produktu> produktuak;
    private List<Integer> kopuruak;

    public Saskia() {
        this.produktuak = new ArrayList<>();
        this.kopuruak = new ArrayList<>();
    }

    public void gehituProduktua(Produktu p, int kopurua) {
        // Si el producto ya está en el carrito, sumamos la cantidad
        for (int i = 0; i < produktuak.size(); i++) {
            if (produktuak.get(i).getId() == p.getId()) {
                kopuruak.set(i, kopuruak.get(i) + kopurua);
                return;
            }
        }
        // Si no está, lo añadimos nuevo
        produktuak.add(p);
        kopuruak.add(kopurua);
    }

    public List<Produktu> getProduktuak() { return produktuak; }
    public List<Integer> getKopuruak() { return kopuruak; }

    public double kalkulatuGuztira() {
        double guztira = 0;
        for (int i = 0; i < produktuak.size(); i++) {
            guztira += produktuak.get(i).getSalneurria() * kopuruak.get(i);
        }
        return guztira;
    }

    public void garbitu() {
        produktuak.clear();
        kopuruak.clear();
    }
}