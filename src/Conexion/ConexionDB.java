package Conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    // Datos de conexión para el RETO 3
    private final String URL = "jdbc:oracle:thin:@//localhost:1521/XEPDB1";
    private final String USUARIO = "RETO3";
    private final String PASAHITZA = "RETO3";

    /**
     * conexión con la base de datos Oracle.
     * @return Connection si es exitosa, null si falla.
     */
    public Connection conectar() {
        Connection con = null;
        try {
            // Cargamos el driver de Oracle (el ojdbc11.jar que has añadido)
            Class.forName("oracle.jdbc.driver.OracleDriver");
            // Intentamos conectar
            con = DriverManager.getConnection(URL, USUARIO, PASAHITZA);
        } catch (ClassNotFoundException e) {
            System.out.println("Falta el Driver: Asegúrate de haber añadido ojdbc11.jar como librería.");
        } catch (SQLException e) {
            System.out.println("Error de credenciales o de base de datos: " + e.getMessage());
        }
        return con;
    }

    /**
     * Cierra la conexión con la base de datos de forma segura.
     */
    public void desconectar(Connection con) {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            System.out.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
}
