package Modelo;

public class Usuario {
    private int id;
    private String username; // Será EMAILA (Bezero) o ERABILTZAILEA (Saltzaile)
    private String password;
    private String rol;      // "Bezeroa" o "Saltzailea"

    // Datos extra para el perfil del Bezero (Cliente)
    private String izena;
    private String abizena;
    private String helbidea;
    private String telefono;

    // Constructor para Login general
    public Usuario(int id, String username, String password, String rol) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.rol = rol;
    }

    // Constructor completo para el Perfil del Cliente
    public Usuario(int id, String username, String password, String rol, String izena, String abizena, String helbidea, String telefono) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.izena = izena;
        this.abizena = abizena;
        this.helbidea = helbidea;
        this.telefono = telefono;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRol() { return rol; }
    public String getIzena() { return izena; }
    public String getAbizena() { return abizena; }
    public String getHelbidea() { return helbidea; }
    public String getTelefono() { return telefono; }
    // Añade estos métodos al final de la clase Usuario
    public void setIzena(String izena) { this.izena = izena; }
    public void setAbizena(String abizena) { this.abizena = abizena; }
    public void setUsername(String username) { this.username = username; }
    public void setHelbidea(String helbidea) { this.helbidea = helbidea; }
}