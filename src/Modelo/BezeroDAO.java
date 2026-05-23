package Modelo;

import Conexion.ConexionDB;

import java.sql.*;

public class BezeroDAO {
    private ConexionDB conexionDB;

    public BezeroDAO() {
        this.conexionDB = new ConexionDB();
    }

    public Usuario validarLogin(String username, String password) {
        Connection con = conexionDB.conectar();
        Usuario usuario = null;

        if (con != null) {
            try {
                // 1. Buscar en VENDEDORES
                String sqlSaltzaile = "SELECT s.ID, s.ERABILTZAILEA, l.IZENA, l.ABIZENA " +
                        "FROM SALTZAILE s JOIN LANGILE l ON s.ID = l.ID " +
                        "WHERE s.ERABILTZAILEA = ? AND s.PASAHITZA = ?";
                PreparedStatement ps1 = con.prepareStatement(sqlSaltzaile);
                ps1.setString(1, username);
                ps1.setString(2, password);
                ResultSet rs1 = ps1.executeQuery();

                if (rs1.next()) {
                    usuario = new Usuario(
                            rs1.getInt("ID"),
                            rs1.getString("ERABILTZAILEA"),
                            password,
                            "Saltzailea",
                            rs1.getString("IZENA"),
                            rs1.getString("ABIZENA"),
                            "",
                            ""
                    );
                }
                rs1.close();
                ps1.close();

                // 2. Si no es vendedor, buscar en CLIENTES
                if (usuario == null) {
                    String sqlBezero = "SELECT ID, EMAILA, IZENA, ABIZENA, HELBIDEA FROM BEZERO WHERE EMAILA = ? AND PASAHITZA = ?";
                    PreparedStatement ps2 = con.prepareStatement(sqlBezero);
                    ps2.setString(1, username);
                    ps2.setString(2, password);
                    ResultSet rs2 = ps2.executeQuery();

                    if (rs2.next()) {
                        usuario = new Usuario(
                                rs2.getInt("ID"),
                                rs2.getString("EMAILA"),
                                password,
                                "Bezeroa",
                                rs2.getString("IZENA"),
                                rs2.getString("ABIZENA"),
                                rs2.getString("HELBIDEA"),
                                ""
                        );
                    }
                    rs2.close();
                    ps2.close();
                }

            } catch (SQLException e) {
                System.out.println("Error en validarLogin: " + e.getMessage());
            } finally {
                conexionDB.desconectar(con);
            }
        }
        return usuario;
    }

    // --- ESTO ES LO QUE FALTABA PARA TU VENTANA PRINCIPAL ---
    public boolean eguneratuDatuak(int id, String izena, String abizena, String emaila, String helbidea) {
        Connection con = conexionDB.conectar();
        if (con != null) {
            try {
                String sql = "UPDATE BEZERO SET IZENA = ?, ABIZENA = ?, EMAILA = ?, HELBIDEA = ? WHERE ID = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, izena);
                ps.setString(2, abizena);
                ps.setString(3, emaila);
                ps.setString(4, helbidea);
                ps.setInt(5, id);

                int rows = ps.executeUpdate();
                ps.close();
                return rows > 0;
            } catch (SQLException e) {
                System.out.println("Errorea datuak eguneratzean: " + e.getMessage());
                return false;
            } finally {
                conexionDB.desconectar(con);
            }
        }
        return false;
    }
    /**
     * Administratzaile berri bat (Saltzailea) sortzen du.
     */
    public boolean gehituAdministratzailea(String izena, String abizena, String emaila, String user, String pass) {
        Connection con = conexionDB.conectar();
        if (con == null) return false;

        try {
            con.setAutoCommit(false);

            // 1. Hurrengo ID-a lortu LANGILE taularako
            String sqlId = "SELECT NVL(MAX(ID), 0) + 1 FROM LANGILE";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sqlId);
            int berriaId = 1;
            if (rs.next()) berriaId = rs.getInt(1);

            // 2. Insert en LANGILE
            String sqlLangile = "INSERT INTO LANGILE (ID, IZENA, ABIZENA, EMAILA, KONTRATAZIO_DATA) VALUES (?, ?, ?, ?, CURRENT_DATE)";
            PreparedStatement psL = con.prepareStatement(sqlLangile);
            psL.setInt(1, berriaId);
            psL.setString(2, izena);
            psL.setString(3, abizena);
            psL.setString(4, emaila);
            psL.executeUpdate();

            // 3. Insert en SALTZAILE
            String sqlSaltzaile = "INSERT INTO SALTZAILE (ID, ERABILTZAILEA, PASAHITZA) VALUES (?, ?, ?)";
            PreparedStatement psS = con.prepareStatement(sqlSaltzaile);
            psS.setInt(1, berriaId);
            psS.setString(2, user);
            psS.setString(3, pass);
            psS.executeUpdate();

            con.commit();
            return true;
        } catch (SQLException e) {
            try { con.rollback(); } catch (SQLException ex) {}
            System.out.println("Errorea saltzailea sortzean: " + e.getMessage());
            return false;
        } finally {
            conexionDB.desconectar(con);
        }
    }
    /**
     * Bezero berri bat erregistratzen du sisteman.
     */
    public boolean erregistratuBezeroa(String izena, String abizena, String emaila, String pass, String helbidea) {
        Connection con = conexionDB.conectar();
        if (con == null) return false;

        try {
            // 1. Hurrengo ID-a lortu
            String sqlId = "SELECT NVL(MAX(ID), 0) + 1 FROM BEZERO";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sqlId);
            int berriaId = 1;
            if (rs.next()) berriaId = rs.getInt(1);

            // 2. Insert-a egin
            String sqlInsert = "INSERT INTO BEZERO (ID, IZENA, ABIZENA, EMAILA, PASAHITZA, HELBIDEA) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sqlInsert);
            ps.setInt(1, berriaId);
            ps.setString(2, izena);
            ps.setString(3, abizena);
            ps.setString(4, emaila);
            ps.setString(5, pass);
            ps.setString(6, helbidea);

            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("Errorea bezeroa erregistratzean: " + e.getMessage());
            return false;
        } finally {
            conexionDB.desconectar(con);
        }
    }
}