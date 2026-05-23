package Modelo;

import Conexion.ConexionDB;
import java.sql.*;
import java.util.ArrayList; // Gehitu dugu zerrendak erabiltzeko
import java.util.List;

public class EskariDAO {
    private ConexionDB conexionDB = new ConexionDB();

    /**
     * Erosketa prozesua burutzen du: Eskaria sortu, lerroak sartu eta stocka eguneratu.
     */
    public boolean erosi(int idBezero, Saskia saskia) {
        Connection con = conexionDB.conectar();
        if (con == null) return false;

        try {
            con.setAutoCommit(false); // Transakzioa hasi (Empezar transacción)

            // 1. Eskariaren ID berria lortu
            String sqlId = "SELECT NVL(MAX(ID), 0) + 1 AS NEXT_ID FROM ESKARI";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sqlId);
            int eskariId = 1;
            if (rs.next()) eskariId = rs.getInt("NEXT_ID");

            // 2. Insert ESKARI
            String sqlEskari = "INSERT INTO ESKARI (ID, ID_BEZERO, ID_EGOERA, ESKAERA_DATA) VALUES (?, ?, 1, CURRENT_DATE)";
            PreparedStatement psEskari = con.prepareStatement(sqlEskari);
            psEskari.setInt(1, eskariId);
            psEskari.setInt(2, idBezero);
            psEskari.executeUpdate();

            // 3. Insert ESKARI_LERROAK eta Stock-a eguneratu
            List<Produktu> produktuak = saskia.getProduktuak();
            List<Integer> kopuruak = saskia.getKopuruak();

            for (int i = 0; i < produktuak.size(); i++) {
                // Lerroa sartu
                String sqlLerro = "INSERT INTO ESKARI_LERRO (ID_ESKARI, ID_LERRO, ID_PRODUKTU, KOPURUA, SALNEURRIA) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement psL = con.prepareStatement(sqlLerro);
                psL.setInt(1, eskariId);
                psL.setInt(2, i + 1);
                psL.setInt(3, produktuak.get(i).getId());
                psL.setInt(4, kopuruak.get(i));
                psL.setDouble(5, produktuak.get(i).getSalneurria());
                psL.executeUpdate();

                // Stock-a kendu (Restar stock del almacén 1)
                String sqlStock = "UPDATE INBENTARIO SET KOPURUA = KOPURUA - ? WHERE ID_PRODUKTU = ? AND ID_BILTEGI = 1";
                PreparedStatement psS = con.prepareStatement(sqlStock);
                psS.setInt(1, kopuruak.get(i));
                psS.setInt(2, produktuak.get(i).getId());
                psS.executeUpdate();
            }

            con.commit(); // Dena ondo, gorde aldaketak
            return true;

        } catch (SQLException e) {
            try { con.rollback(); } catch (SQLException ex) {}
            System.out.println("Errorea erosterakoan: " + e.getMessage());
            return false;
        } finally {
            conexionDB.desconectar(con);
        }
    }

    /**
     * Bezero baten erosketen historial osoa lortzen du.
     */
    public List<String> lortuErosketak(int idBezero) {
        List<String> lista = new ArrayList<>();
        Connection con = conexionDB.conectar();
        if (con != null) {
            try {
                // JOIN para ver la fecha del pedido y los nombres de los productos
                String sql = "SELECT e.ID, e.ESKAERA_DATA, p.IZENA, l.KOPURUA, l.SALNEURRIA " +
                        "FROM ESKARI e " +
                        "JOIN ESKARI_LERRO l ON e.ID = l.ID_ESKARI " +
                        "JOIN PRODUKTU p ON l.ID_PRODUKTU = p.ID " +
                        "WHERE e.ID_BEZERO = ? " +
                        "ORDER BY e.ESKAERA_DATA DESC";

                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, idBezero);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    String linea = "Data: " + rs.getDate("ESKAERA_DATA") +
                            " | " + rs.getString("IZENA") +
                            " (x" + rs.getInt("KOPURUA") + ") " +
                            " - " + rs.getDouble("SALNEURRIA") + "€";
                    lista.add(linea);
                }
                rs.close();
                ps.close();
            } catch (SQLException e) {
                System.out.println("Errorea historialea lortzean: " + e.getMessage());
            } finally {
                conexionDB.desconectar(con);
            }
        }
        return lista;
    }
    /**
     * Administratzailearentzat: Eskaera guztiak lortzen ditu bezeroaren izenarekin.
     */
    public List<String> lortuEskariGuztiak() {
        List<String> lista = new ArrayList<>();
        Connection con = conexionDB.conectar();
        if (con != null) {
            try {
                String sql = "SELECT e.ID, b.IZENA AS BEZERO_IZENA, b.ABIZENA, e.ESKAERA_DATA, p.IZENA AS PROD_IZENA, l.KOPURUA, l.SALNEURRIA " +
                        "FROM ESKARI e " +
                        "JOIN BEZERO b ON e.ID_BEZERO = b.ID " +
                        "JOIN ESKARI_LERRO l ON e.ID = l.ID_ESKARI " +
                        "JOIN PRODUKTU p ON l.ID_PRODUKTU = p.ID " +
                        "ORDER BY e.ESKAERA_DATA DESC, e.ID DESC";

                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    String linea = "ID: " + rs.getInt("ID") +
                            " | Bezeroa: " + rs.getString("BEZERO_IZENA") + " " + rs.getString("ABIZENA") +
                            " | Data: " + rs.getDate("ESKAERA_DATA") +
                            " | " + rs.getString("PROD_IZENA") +
                            " (x" + rs.getInt("KOPURUA") + ") " +
                            " - " + rs.getDouble("SALNEURRIA") + "€";
                    lista.add(linea);
                }
                rs.close();
                ps.close();
            } catch (SQLException e) {
                System.out.println("Errorea eskaera guztiak lortzean: " + e.getMessage());
            } finally {
                conexionDB.desconectar(con);
            }
        }
        return lista;
    }
}