package Modelo;

import Conexion.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProduktuDAO {
    private ConexionDB conexionDB;

    public ProduktuDAO() {
        this.conexionDB = new ConexionDB();
    }

    /**
     * Datu baseko produktu guztiak lortzen ditu euren stock totalarekin.
     * (Obtiene la lista completa de productos unidos con su stock total).
     */
    public List<Produktu> obtenerTodos() {
        List<Produktu> lista = new ArrayList<>();
        Connection con = conexionDB.conectar();

        if (con != null) {
            try {
                // Consulta PRO: Junta el producto con la suma de su stock en todos los almacenes
                String sql = "SELECT p.ID, p.IZENA, p.DESKRIBAPENA, p.SALNEURRIA, p.ID_KATEGORIA, " +
                        "NVL(SUM(i.KOPURUA), 0) AS STOCK_TOTAL " +
                        "FROM PRODUKTU p " +
                        "LEFT JOIN INBENTARIO i ON p.ID = i.ID_PRODUKTU " +
                        "GROUP BY p.ID, p.IZENA, p.DESKRIBAPENA, p.SALNEURRIA, p.ID_KATEGORIA " +
                        "ORDER BY p.ID ASC";

                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql);

                while (rs.next()) {
                    Produktu p = new Produktu(
                            rs.getInt("ID"),
                            rs.getString("IZENA"),
                            rs.getString("DESKRIBAPENA"),
                            rs.getDouble("SALNEURRIA"),
                            rs.getInt("ID_KATEGORIA"),
                            rs.getInt("STOCK_TOTAL")
                    );
                    lista.add(p);
                }
                rs.close();
                st.close();

            } catch (SQLException e) {
                System.out.println("Error al obtener productos: " + e.getMessage());
            } finally {
                conexionDB.desconectar(con);
            }
        }
        return lista;
    }

    /**
     * Datu basean produktu baten stocka eguneratzen du.
     * (Actualiza el stock de un producto en la tabla INBENTARIO).
     */
    public boolean eguneratuStock(int idProduktu, int stockBerria) {
        Connection con = conexionDB.conectar();
        if (con != null) {
            try {
                // Lehenik, Inbentarioan eguneratzen saiatuko gara (Primero intentamos actualizar)
                String sqlUpdate = "UPDATE INBENTARIO SET KOPURUA = ? WHERE ID_PRODUKTU = ?";
                PreparedStatement psUpdate = con.prepareStatement(sqlUpdate);
                psUpdate.setInt(1, stockBerria);
                psUpdate.setInt(2, idProduktu);

                int rows = psUpdate.executeUpdate();

                // 0 lerro aldatu badira, produktu honek ez du inbentariorik oraindik. Berria sortuko dugu.
                // (Si devuelve 0, significa que no existía registro, hacemos un INSERT)
                if (rows == 0) {
                    // HEMEN DAGO ALDAKETA: ID_BILTEGI gehitu dugu (AQUÍ ESTÁ EL CAMBIO: Añadimos ID_BILTEGI)
                    // Asumimos que el stock va al almacén principal (ID 1). Si tu almacén tiene otro ID, cambia el '1' abajo.
                    String sqlInsert = "INSERT INTO INBENTARIO (ID_PRODUKTU, KOPURUA, ID_BILTEGI) VALUES (?, ?, ?)";
                    PreparedStatement psInsert = con.prepareStatement(sqlInsert);
                    psInsert.setInt(1, idProduktu);
                    psInsert.setInt(2, stockBerria);
                    psInsert.setInt(3, 1); // <--- ID_BILTEGI (1. biltegia / Almacén 1)

                    psInsert.executeUpdate();
                    psInsert.close();
                }

                psUpdate.close();
                return true; // Dena ondo joan da

            } catch (SQLException e) {
                System.out.println("Errorea stocka eguneratzean (Error al actualizar stock): " + e.getMessage());
                return false;
            } finally {
                conexionDB.desconectar(con);
            }
        }
        return false;
    }
    /**
     * Datu baseko produktu bat ezabatzen du (Borra un producto de la base de datos).
     * Kontuz: Lehenik inbentariotik ezabatu behar da gako atzerritarrengatik (claves foráneas).
     * * @param idProduktu Ezabatu nahi den produktuaren ID-a.
     * @return true ondo ezabatu bada, false bestela.
     */
    /**
     * Datu baseko produktu bat ezabatzen du (Borra un producto de la base de datos).
     * Kontuz: Lehenik inbentariotik eta eskaeretatik ezabatu behar da gako atzerritarrengatik (claves foráneas).
     * * @param idProduktu Ezabatu nahi den produktuaren ID-a.
     * @return true ondo ezabatu bada, false bestela.
     */
    public boolean ezabatuProduktua(int idProduktu) {
        Connection con = conexionDB.conectar();
        if (con != null) {
            try {
                // 1. Lehenik, eskaera/faktura lerroetatik ezabatu gako atzerritarra (FK) ez apurtzeko
                String sqlLerroa = "DELETE FROM ESKARI_LERRO WHERE ID_PRODUKTU = ?";
                PreparedStatement psLerroa = con.prepareStatement(sqlLerroa);
                psLerroa.setInt(1, idProduktu);
                psLerroa.executeUpdate();
                psLerroa.close();

                // 2. Gero inbentariotik ezabatu (Luego borramos del inventario)
                String sqlInbentario = "DELETE FROM INBENTARIO WHERE ID_PRODUKTU = ?";
                PreparedStatement psInb = con.prepareStatement(sqlInbentario);
                psInb.setInt(1, idProduktu);
                psInb.executeUpdate();
                psInb.close();

                // 3. Azkenik, produktua bera ezabatu dezakegu (Finalmente borramos el producto)
                String sqlProduktu = "DELETE FROM PRODUKTU WHERE ID = ?";
                PreparedStatement psProd = con.prepareStatement(sqlProduktu);
                psProd.setInt(1, idProduktu);
                int rows = psProd.executeUpdate();
                psProd.close();

                return rows > 0;

            } catch (SQLException e) {
                System.out.println("Errorea produktua ezabatzean (Error al borrar): " + e.getMessage());
                return false;
            } finally {
                conexionDB.desconectar(con);
            }
        }
        return false;
    }
    /**
     * Datu basean produktu berri bat gehitzen du (Añade un nuevo producto).
     * @return true ondo gehitu bada, false bestela.
     */
    public boolean gehituProduktua(String izena, String deskribapena, double balioa, double salneurria, int idKategoria) {
        Connection con = conexionDB.conectar();
        if (con != null) {
            try {
                // 1. Lortu hurrengo ID-a (Obtener el siguiente ID disponible)
                String sqlId = "SELECT NVL(MAX(ID), 0) + 1 AS NEXT_ID FROM PRODUKTU";
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sqlId);
                int hurrengoId = 1;
                if (rs.next()) {
                    hurrengoId = rs.getInt("NEXT_ID");
                }
                rs.close();
                st.close();

                // 2. Produktua txertatu (Insertar el producto)
                String sqlInsert = "INSERT INTO PRODUKTU (ID, IZENA, DESKRIBAPENA, BALIOA, SALNEURRIA, ID_KATEGORIA) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement psInsert = con.prepareStatement(sqlInsert);
                psInsert.setInt(1, hurrengoId);
                psInsert.setString(2, izena);
                psInsert.setString(3, deskribapena);
                psInsert.setDouble(4, balioa);
                psInsert.setDouble(5, salneurria);
                psInsert.setInt(6, idKategoria);

                int rows = psInsert.executeUpdate();
                psInsert.close();

                // 3. Hasierako stock-a 0 jarri inbentarioan (Preparar su inventario a 0 en el almacén 1)
                if (rows > 0) {
                    String sqlStock = "INSERT INTO INBENTARIO (ID_PRODUKTU, ID_BILTEGI, KOPURUA) VALUES (?, 1, 0)";
                    PreparedStatement psStock = con.prepareStatement(sqlStock);
                    psStock.setInt(1, hurrengoId);
                    psStock.executeUpdate();
                    psStock.close();
                }

                return rows > 0;
            } catch (SQLException e) {
                System.out.println("Errorea produktua gehitzean (Error al añadir): " + e.getMessage());
                return false;
            } finally {
                conexionDB.desconectar(con);
            }
        }
        return false;
    }
    /**
     * Datu baseko produktu baten datuak eguneratzen ditu (Modifica los datos de un producto).
     * @return true ondo eguneratu bada, false bestela.
     */
    public boolean aldatuProduktua(int id, String izena, String deskribapena, double salneurria) {
        Connection con = conexionDB.conectar();
        if (con != null) {
            try {
                String sql = "UPDATE PRODUKTU SET IZENA = ?, DESKRIBAPENA = ?, SALNEURRIA = ? WHERE ID = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, izena);
                ps.setString(2, deskribapena);
                ps.setDouble(3, salneurria);
                ps.setInt(4, id);

                int rows = ps.executeUpdate();
                ps.close();

                return rows > 0;
            } catch (SQLException e) {
                System.out.println("Errorea produktua aldatzean (Error al modificar): " + e.getMessage());
                return false;
            } finally {
                conexionDB.desconectar(con);
            }
        }
        return false;
    }
}
