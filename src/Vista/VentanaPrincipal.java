package Vista;

import Modelo.Produktu;
import Modelo.ProduktuDAO;
import Modelo.Saskia;
import Modelo.Usuario;
import Modelo.EskariDAO;
import Modelo.BezeroDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    private Usuario usuarioActual;
    private JTabbedPane pestanas;
    // para el carrito:
    private Saskia nireSaskia = new Saskia();
    private JButton btnHeaderSaskia; // Header-eko botoia eguneratzeko
    private DefaultTableModel modeloTabla; // Taula errazago freskatzeko
    private List<Produktu> listaProductos; // Produktuen zerrenda orokorra

    // GameStop kolore korporatiboak (Colores corporativos)
    private Color gsRed = new Color(225, 6, 19);
    private Color gsBlack = new Color(30, 30, 30);
    private Color gsWhite = Color.WHITE;

    public VentanaPrincipal(Usuario usuarioActivo) {
        this.usuarioActual = usuarioActivo;

        // Leihoaren oinarrizko konfigurazioa (Configuración básica)
        setTitle("GameStop ERP - " + usuarioActual.getIzena() + " [" + usuarioActual.getRol() + "]");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. HEADER (Goiko panel beltza / Panel superior negro)
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(gsBlack);
        panelHeader.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblBienvenida = new JLabel("Ongi etorri, " + usuarioActual.getIzena() + " " + usuarioActual.getAbizena());
        lblBienvenida.setForeground(gsWhite);
        lblBienvenida.setFont(new Font("Arial", Font.BOLD, 18));

        // Botoien panela (Dretxan jartzeko / Para poner a la derecha)
        JPanel panelHeaderButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelHeaderButtons.setOpaque(false);

        // --- SASKIA BOTOIA (Header-ean, bakarrik bezeroentzat) ---
        if (usuarioActual.getRol().equals("Bezeroa")) {
            btnHeaderSaskia = new JButton("Saskia (0)");
            btnHeaderSaskia.setBackground(gsWhite);
            btnHeaderSaskia.setForeground(gsBlack);
            btnHeaderSaskia.setFont(new Font("Arial", Font.BOLD, 12));
            btnHeaderSaskia.addActionListener(e -> checkoutAction());
            panelHeaderButtons.add(btnHeaderSaskia);
        }

        JButton btnLogout = new JButton("Saioa Itxi");
        btnLogout.setBackground(gsWhite);
        btnLogout.setForeground(gsBlack);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            new VentanaLogin().setVisible(true);
            this.dispose();
        });

        panelHeaderButtons.add(btnLogout);

        panelHeader.add(lblBienvenida, BorderLayout.WEST);
        panelHeader.add(panelHeaderButtons, BorderLayout.EAST);
        add(panelHeader, BorderLayout.NORTH);

        // 2. FITXAK ("Pantaila gutxiago" filosofiaren muina / El núcleo de las pestañas)
        pestanas = new JTabbedPane();
        pestanas.setFont(new Font("Arial", Font.BOLD, 14));

        if (usuarioActual.getRol().equals("Bezeroa")) {
            // BEZEROARENTZAKO fitxak (Pestañas para cliente)
            pestanas.addTab("Jokoen Katalogoa", crearPanelCatalogo(false));
            pestanas.addTab("Nire Erosketak", crearPanelHistorial());
            pestanas.addTab("Nire Datuak", crearPanelMisDatos());
        } else {
            // ADMIN / SALTZAILEARENTZAKO fitxak (Pestañas para administrador)
            pestanas.addTab("Inbentarioaren Kudeaketa", crearPanelCatalogo(true));
            pestanas.addTab("Eskaera Guztiak", crearPanelEskaeraGuztiak()); // BERRIA

            // Administratzaile "Supremoa" bada (adibidez ID 1 daukana), langileak kudeatu ditzake
            if (usuarioActual.getId() == 1 || usuarioActual.getIzena().contains("Admin")) {
                pestanas.addTab("Admin Berriak Sortu", crearPanelGestionAdmins()); // BERRIA
            }
        }

        add(pestanas, BorderLayout.CENTER);
    }

    /**
     * Produktuen taularen panela sortzen du.
     * isAdmin true bada, CRUD botoiak erakusten ditu. False bada, erosteko botoia.
     */
    private JPanel crearPanelCatalogo(boolean isAdmin) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(gsWhite);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Taula sortu (Crear tabla)
        String[] columnas = {"ID", "Izena", "Deskribapena", "Prezioa (€)", "Stock Totala"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tabla = new JTable(modeloTabla);
        tabla.setRowHeight(25);
        tabla.getTableHeader().setBackground(Color.LIGHT_GRAY);
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // Datuak Oracle-tik kargatu (Se cargan a través del método de refresco)
        refrescarTabla();

        // ADMINA EZ bada (bezeroa da), ID zutabea ezkutatuko dugu estetika zaintzeko
        if (!isAdmin) {
            tabla.getColumnModel().removeColumn(tabla.getColumnModel().getColumn(0));
        }

        JScrollPane scrollPane = new JScrollPane(tabla);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Beheko botoien panela (Panel de botones inferior)
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelBotones.setBackground(gsWhite);

        if (isAdmin) {
            JButton btnNuevo = new JButton("Produktua Gehitu");
            JButton btnModificar = new JButton("Produktua Aldatu");
            JButton btnBorrar = new JButton("Produktua Ezabatu");
            JButton btnReponerStock = new JButton("Stocka Berritu");

            // --- Lógica: Añadir Producto (Create) ---
            btnNuevo.addActionListener(e -> {
                // Formularioaren eremuak sortu (Crear campos del formulario)
                JTextField txtIzena = new JTextField();
                JTextField txtDeskribapena = new JTextField();
                JTextField txtBalioa = new JTextField();
                JTextField txtSalneurria = new JTextField();

                // Kategoriak zure Datu Basearen arabera (Categorías según tu BD)
                String[] kategoriak = {"1 - CPU", "2 - Video Card", "3 - RAM", "4 - Mother Board", "5 - Storage"};
                JComboBox<String> cbKategoria = new JComboBox<>(kategoriak);

                // Formularioa muntatu (Montar el formulario visual)
                Object[] message = {
                        "Izena (Nombre):", txtIzena,
                        "Deskribapena (Descripción):", txtDeskribapena,
                        "Balioa / Coste (€) [10 eta 10000 artean]:", txtBalioa,
                        "Salneurria / Precio Venta (€):", txtSalneurria,
                        "Kategoria:", cbKategoria
                };

                int option = JOptionPane.showConfirmDialog(panel, message, "Produktua Gehitu", JOptionPane.OK_CANCEL_OPTION);

                // Erabiltzaileak OK sakatzen badu (Si el usuario pulsa OK)
                if (option == JOptionPane.OK_OPTION) {
                    try {
                        String izena = txtIzena.getText();
                        String deskribapena = txtDeskribapena.getText();

                        // Validar campos vacíos
                        if (izena.isEmpty() || txtBalioa.getText().isEmpty() || txtSalneurria.getText().isEmpty()) {
                            JOptionPane.showMessageDialog(panel, "Eremu guztiak bete behar dituzu (Debes rellenar todos los campos).", "Errorea", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // Komak puntuetara aldatu SQL-k ondo irakurtzeko (Cambiar comas por puntos para los decimales)
                        double balioa = Double.parseDouble(txtBalioa.getText().replace(",", "."));
                        double salneurria = Double.parseDouble(txtSalneurria.getText().replace(",", "."));

                        // Kategoriaren ID-a atera (Extraer solo el número de la categoría)
                        int idKategoria = Integer.parseInt(cbKategoria.getSelectedItem().toString().substring(0, 1));

                        // Balioaren murrizketa frogatu (Validar restricción CHK_BALIOA)
                        if (balioa <= 10 || balioa >= 10000) {
                            JOptionPane.showMessageDialog(panel,
                                    "Datu Basearen Araua: Balioak 10 eta 10000 artean egon behar du!\n(El coste debe estar entre 10 y 10000)",
                                    "Errorea", JOptionPane.ERROR_MESSAGE);
                            return; // Hemen gelditu (Detener aquí)
                        }

                        // DAO-ri deitu (Llamar al DAO)
                        ProduktuDAO daoGehitu = new ProduktuDAO();
                        boolean exito = daoGehitu.gehituProduktua(izena, deskribapena, balioa, salneurria, idKategoria);

                        if (exito) {
                            // Cargar de nuevo toda la tabla para tener el ID real que se le asignó en BD
                            refrescarTabla();

                            JOptionPane.showMessageDialog(panel,
                                    "Produktua ondo gehitu da! Hasierako stocka 0 da.",
                                    "Arrakasta", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(panel,
                                    "Errorea egon da datu basean gordetzean.",
                                    "Errorea", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(panel,
                                "Mesedez, sartu zenbaki egokiak prezioetan.",
                                "Errorea", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            // --- Lógica: Modificar Producto (Update) ---
            btnModificar.addActionListener(e -> {
                int filaSeleccionada = tabla.getSelectedRow();
                if (filaSeleccionada >= 0) {
                    // Lortu uneko datuak taulatik (Obtener datos actuales de la tabla)
                    int idProducto = Integer.parseInt(tabla.getValueAt(filaSeleccionada, 0).toString());
                    String izenaActual = tabla.getValueAt(filaSeleccionada, 1).toString();
                    // Puede que la descripción sea null, la controlamos
                    String descActual = tabla.getValueAt(filaSeleccionada, 2) != null ? tabla.getValueAt(filaSeleccionada, 2).toString() : "";
                    String precioActual = tabla.getValueAt(filaSeleccionada, 3).toString();

                    // Formularioaren eremuak sortu eta bete (Crear y rellenar campos)
                    JTextField txtIzena = new JTextField(izenaActual);
                    JTextField txtDeskribapena = new JTextField(descActual);
                    JTextField txtSalneurria = new JTextField(precioActual);

                    Object[] message = {
                            "Izena (Nombre):", txtIzena,
                            "Deskribapena (Descripción):", txtDeskribapena,
                            "Salneurria / Precio Venta (€):", txtSalneurria
                    };

                    int option = JOptionPane.showConfirmDialog(panel, message, "Produktua Aldatu", JOptionPane.OK_CANCEL_OPTION);

                    if (option == JOptionPane.OK_OPTION) {
                        try {
                            String izena = txtIzena.getText();
                            String deskribapena = txtDeskribapena.getText();
                            double salneurria = Double.parseDouble(txtSalneurria.getText().replace(",", "."));

                            if (izena.isEmpty() || txtSalneurria.getText().isEmpty()) {
                                JOptionPane.showMessageDialog(panel, "Izena eta Salneurria ezin dira hutsik egon.", "Errorea", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            // DAO-ri deitu (Llamar al DAO)
                            ProduktuDAO daoAldatu = new ProduktuDAO();
                            boolean exito = daoAldatu.aldatuProduktua(idProducto, izena, deskribapena, salneurria);

                            if (exito) {
                                // Taulan bisualki eguneratu (Actualizar tabla visualmente)
                                refrescarTabla();

                                JOptionPane.showMessageDialog(panel, "Produktua ondo eguneratu da!", "Arrakasta", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(panel, "Errorea datu basean gordetzean.", "Errorea", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(panel, "Mesedez, sartu zenbaki egokiak prezioetan.", "Errorea", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(panel, "Mesedez, aukeratu taulako produktu bat lehenik.", "Oharra", JOptionPane.WARNING_MESSAGE);
                }
            });

            // --- Lógica: Borrar Producto ---
            btnBorrar.addActionListener(e -> {
                int filaSeleccionada = tabla.getSelectedRow();
                if (filaSeleccionada >= 0) {
                    // Konfirmazioa eskatu (Pedir confirmación)
                    int confirmacion = JOptionPane.showConfirmDialog(panel,
                            "Ziur zaude produktu hau ezabatu nahi duzula? (Ezin da desegin)",
                            "Produktua Ezabatu",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (confirmacion == JOptionPane.YES_OPTION) {
                        // ID-a lortu (Obtener el ID de la columna 0)
                        int idProducto = Integer.parseInt(tabla.getValueAt(filaSeleccionada, 0).toString());

                        // DAO-ri deitu ezabatzeko (Llamar al DAO)
                        ProduktuDAO daoEzabatu = new ProduktuDAO();
                        boolean exito = daoEzabatu.ezabatuProduktua(idProducto);

                        if (exito) {
                            // Taulatik bisualki kendu (Quitar la fila visualmente)
                            refrescarTabla();

                            JOptionPane.showMessageDialog(panel,
                                    "Produktua ondo ezabatu da.",
                                    "Arrakasta",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(panel,
                                    "Errorea egon da datu basean ezabatzean.",
                                    "Errorea",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(panel,
                            "Mesedez, aukeratu taulako produktu bat lehenik.",
                            "Oharra",
                            JOptionPane.WARNING_MESSAGE);
                }
            });

            // --- Lógica: Reponer Stock ---
            btnReponerStock.addActionListener(e -> {
                int filaSeleccionada = tabla.getSelectedRow();
                if (filaSeleccionada >= 0) {
                    String input = JOptionPane.showInputDialog(panel,
                            "Zenbat unitate gehitu nahi dizkiozu stockari?",
                            "Stocka Berritu",
                            JOptionPane.QUESTION_MESSAGE);

                    if (input != null && !input.isEmpty()) {
                        try {
                            int cantidadAAnadir = Integer.parseInt(input);
                            if (cantidadAAnadir > 0) {
                                // ID-a taularen 0. zutabean dago (El ID está en la columna 0)
                                int idProducto = Integer.parseInt(tabla.getValueAt(filaSeleccionada, 0).toString());

                                int columnaStock = 4; // Stock zutabea
                                int stockActual = Integer.parseInt(tabla.getValueAt(filaSeleccionada, columnaStock).toString());
                                int nuevoStock = stockActual + cantidadAAnadir;

                                // DAO deitu datu basean eguneratzeko (Llamar al DAO para DB)
                                ProduktuDAO daoEguneratu = new ProduktuDAO();
                                boolean exito = daoEguneratu.eguneratuStock(idProducto, nuevoStock);

                                if (exito) {
                                    // Taulan bisualki eguneratu bakarrik DB-an ondo joan bada
                                    refrescarTabla();

                                    JOptionPane.showMessageDialog(panel,
                                            "Stocka ondo eguneratu da " + nuevoStock + " unitatera.",
                                            "Arrakasta",
                                            JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(panel,
                                            "Errorea egon da datu basean gordetzean.",
                                            "Errorea",
                                            JOptionPane.ERROR_MESSAGE);
                                }

                            } else {
                                JOptionPane.showMessageDialog(panel,
                                        "Kopuruak 0 baino handiagoa izan behar du.",
                                        "Errorea",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(panel,
                                    "Mesedez, sartu baliozko zenbaki oso bat.",
                                    "Errorea",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(panel,
                            "Mesedez, aukeratu taulako produktu bat lehenik.",
                            "Oharra",
                            JOptionPane.WARNING_MESSAGE);
                }
            });

            panelBotones.add(btnNuevo);
            panelBotones.add(btnModificar);
            panelBotones.add(btnBorrar);
            panelBotones.add(btnReponerStock);
        } else {
            // --- BEZEROARENTZAKO BOTOIAK (Saskia + Erosketa Azkarra) ---
            JButton btnComprar = new JButton("Nire Saskira Gehitu");
            btnComprar.setBackground(gsBlack);
            btnComprar.setForeground(gsWhite);

            JButton btnQuickBuy = new JButton("Erosketa Azkarra");
            btnQuickBuy.setBackground(gsRed);
            btnQuickBuy.setForeground(gsWhite);

            btnComprar.addActionListener(e -> {
                int fila = tabla.getSelectedRow();
                if (fila >= 0) {
                    Produktu p = listaProductos.get(fila);
                    int stock = p.getStockTotal();
                    String input = JOptionPane.showInputDialog(panel, p.getIzena() + " - Zenbat unitate?", "Saskira Gehitu", JOptionPane.QUESTION_MESSAGE);
                    if (input != null && !input.isEmpty()) {
                        try {
                            int kop = Integer.parseInt(input);
                            if (kop > 0 && kop <= stock) {
                                nireSaskia.gehituProduktua(p, kop);
                                btnHeaderSaskia.setText("Saskia (" + nireSaskia.getProduktuak().size() + ")");
                                JOptionPane.showMessageDialog(panel, "Saskira gehituta!");
                            } else {
                                JOptionPane.showMessageDialog(panel, "Ez dago stock nahikorik!", "Errorea", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) { JOptionPane.showMessageDialog(panel, "Sartu zenbaki bat."); }
                    }
                } else { JOptionPane.showMessageDialog(panel, "Hautatu joko bat lehenik."); }
            });

            // --- Lógica: Erosketa Azkarra (Compra Directa) ---
            btnQuickBuy.addActionListener(e -> {
                int fila = tabla.getSelectedRow();
                if (fila >= 0) {
                    Produktu p = listaProductos.get(fila);
                    int stock = p.getStockTotal();
                    String input = JOptionPane.showInputDialog(panel, "EROSKETA AZKARRA: " + p.getIzena() + "\nZenbat unitate erosi nahi dituzu?", "Erosketa Azkarra", JOptionPane.WARNING_MESSAGE);

                    if (input != null && !input.isEmpty()) {
                        try {
                            int kop = Integer.parseInt(input);
                            if (kop > 0 && kop <= stock) {
                                // Crear un carrito temporal solo para esta compra
                                Saskia saskiaTxikia = new Saskia();
                                saskiaTxikia.gehituProduktua(p, kop);

                                EskariDAO edao = new EskariDAO();
                                if (edao.erosi(usuarioActual.getId(), saskiaTxikia)) {
                                    JOptionPane.showMessageDialog(panel, "Erosketa azkarra ondo burutu da!");
                                    refrescarTabla(); // ACTUALIZAR TABLA AL INSTANTE
                                }
                            } else {
                                JOptionPane.showMessageDialog(panel, "Stock nahikorik ez.");
                            }
                        } catch (Exception ex) { JOptionPane.showMessageDialog(panel, "Sartu zenbaki bat."); }
                    }
                } else { JOptionPane.showMessageDialog(panel, "Hautatu joko bat lehenik."); }
            });

            panelBotones.add(btnComprar);
            panelBotones.add(btnQuickBuy);
        }

        panel.add(panelBotones, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Datu baseko informazioa berriz kargatzen du taulan stocka eguneratzeko.
     */
    private void refrescarTabla() {
        ProduktuDAO dao = new ProduktuDAO();
        this.listaProductos = dao.obtenerTodos(); // Lista orokorra eguneratu
        modeloTabla.setRowCount(0); // Taula garbitu
        for (Produktu p : this.listaProductos) {
            modeloTabla.addRow(new Object[]{
                    p.getId(), p.getIzena(), p.getDeskribapena(), p.getSalneurria(), p.getStockTotal()
            });
        }
    }

    /**
     * Administratzailearentzako: Eskaera guztien zerrenda ikusteko fitxa.
     */
    private JPanel crearPanelEskaeraGuztiak() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(gsWhite);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel("Dendako Eskaera Guztien Zerrenda");
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titulo, BorderLayout.NORTH);

        DefaultListModel<String> modeloLista = new DefaultListModel<>();
        JList<String> listaUI = new JList<>(modeloLista);
        listaUI.setFont(new Font("Monospaced", Font.PLAIN, 12));

        EskariDAO edao = new EskariDAO();

        JButton btnRefrescar = new JButton("Eguneratu Zerrenda");
        btnRefrescar.addActionListener(e -> {
            modeloLista.clear();
            List<String> guztiak = edao.lortuEskariGuztiak();
            for (String h : guztiak) modeloLista.addElement(h);
        });

        panel.add(new JScrollPane(listaUI), BorderLayout.CENTER);
        panel.add(btnRefrescar, BorderLayout.SOUTH);

        // Karga hasierako datuak
        List<String> guztiak = edao.lortuEskariGuztiak();
        for (String h : guztiak) modeloLista.addElement(h);

        return panel;
    }

    /**
     * Admin Supremoarentzako: Administratzaile berriak sortzeko fitxa.
     */
    private JPanel crearPanelGestionAdmins() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(gsWhite);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitulo = new JLabel("Administratzaile Berria Sortu");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));

        JTextField txtNom = new JTextField(20);
        JTextField txtAbi = new JTextField(20);
        JTextField txtEma = new JTextField(20);
        JTextField txtUser = new JTextField(20);
        JPasswordField txtPass = new JPasswordField(20);

        JButton btnSortu = new JButton("Gorde Saltzaile Berria");
        btnSortu.setBackground(gsBlack);
        btnSortu.setForeground(gsWhite);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; panel.add(lblTitulo, gbc);
        gbc.gridwidth = 1; gbc.gridy = 1; panel.add(new JLabel("Izena:"), gbc);
        gbc.gridx = 1; panel.add(txtNom, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Abizenak:"), gbc);
        gbc.gridx = 1; panel.add(txtAbi, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Emaila:"), gbc);
        gbc.gridx = 1; panel.add(txtEma, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Erabiltzaile izena:"), gbc);
        gbc.gridx = 1; panel.add(txtUser, gbc);
        gbc.gridx = 0; gbc.gridy = 5; panel.add(new JLabel("Pasahitza:"), gbc);
        gbc.gridx = 1; panel.add(txtPass, gbc);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; panel.add(btnSortu, gbc);

        btnSortu.addActionListener(e -> {
            String pass = new String(txtPass.getPassword());
            if (txtNom.getText().isEmpty() || txtUser.getText().isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Eremu guztiak bete behar dituzu.");
                return;
            }
            BezeroDAO bdao = new BezeroDAO();
            if (bdao.gehituAdministratzailea(txtNom.getText(), txtAbi.getText(), txtEma.getText(), txtUser.getText(), pass)) {
                JOptionPane.showMessageDialog(this, "Saltzaile berria ondo sortu da!");
                txtNom.setText(""); txtAbi.setText(""); txtEma.setText(""); txtUser.setText(""); txtPass.setText("");
            }
        });

        return panel;
    }

    /**
     * Bezeroak bere erosketen historial osoa ikusteko panela.
     */
    private JPanel crearPanelHistorial() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(gsWhite);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel("Zure Erosketen Historiala");
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titulo, BorderLayout.NORTH);

        DefaultListModel<String> modeloLista = new DefaultListModel<>();
        JList<String> listaUI = new JList<>(modeloLista);
        listaUI.setFont(new Font("Monospaced", Font.PLAIN, 12));

        EskariDAO edao = new EskariDAO();
        List<String> historial = edao.lortuErosketak(usuarioActual.getId());

        if (historial.isEmpty()) {
            modeloLista.addElement("Oraindik ez duzu erosketarik egin.");
        } else {
            for (String h : historial) {
                modeloLista.addElement(h);
            }
        }

        panel.add(new JScrollPane(listaUI), BorderLayout.CENTER);

        JButton btnRefrescar = new JButton("Eguneratu Historiala");
        btnRefrescar.addActionListener(e -> {
            modeloLista.clear();
            List<String> berritu = edao.lortuErosketak(usuarioActual.getId());
            for (String h : berritu) modeloLista.addElement(h);
        });
        panel.add(btnRefrescar, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Bezeroak bere profila ikusi eta editatzeko panela sortzen du.
     */
    private JPanel crearPanelMisDatos() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(gsWhite);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitulo = new JLabel("Nire Profilaren Konfigurazioa");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));

        JTextField txtNombre = new JTextField(usuarioActual.getIzena(), 20);
        JTextField txtApellido = new JTextField(usuarioActual.getAbizena(), 20);
        JTextField txtEmail = new JTextField(usuarioActual.getUsername(), 20);
        JTextField txtDireccion = new JTextField(usuarioActual.getHelbidea(), 20);

        JButton btnGuardar = new JButton("Datuak Eguneratu");
        btnGuardar.setBackground(gsRed);
        btnGuardar.setForeground(gsWhite);

        // Maketazioa GridBagLayout erabiliz
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; panel.add(lblTitulo, gbc);
        gbc.gridwidth = 1; gbc.gridy = 1; panel.add(new JLabel("Izena:"), gbc);
        gbc.gridx = 1; panel.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Abizenak:"), gbc);
        gbc.gridx = 1; panel.add(txtApellido, gbc);

        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Emaila:"), gbc);
        gbc.gridx = 1; panel.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Helbidea:"), gbc);
        gbc.gridx = 1; panel.add(txtDireccion, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; panel.add(btnGuardar, gbc);

        // Gordetzeko logika (DAO-ra konektatuta)
        btnGuardar.addActionListener(e -> {
            String nuevoNom = txtNombre.getText();
            String nuevoAbi = txtApellido.getText();
            String nuevoEma = txtEmail.getText();
            String nuevoHel = txtDireccion.getText();

            if (nuevoNom.isEmpty() || nuevoAbi.isEmpty() || nuevoEma.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Izena, abizena eta emaila beharrezkoak dira.", "Errorea", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BezeroDAO bdao = new BezeroDAO();
            if (bdao.eguneratuDatuak(usuarioActual.getId(), nuevoNom, nuevoAbi, nuevoEma, nuevoHel)) {
                usuarioActual.setIzena(nuevoNom);
                usuarioActual.setAbizena(nuevoAbi);
                usuarioActual.setUsername(nuevoEma);
                usuarioActual.setHelbidea(nuevoHel);

                JOptionPane.showMessageDialog(this, "Datuak ondo eguneratu dira.");
            } else {
                JOptionPane.showMessageDialog(this, "Errorea datuak gordetzean.", "Errorea", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    /**
     * Header-eko saskia botoian klik egitean gertatzen dena.
     */
    private void checkoutAction() {
        if (nireSaskia.getProduktuak().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Saskia hutsik dago!");
            return;
        }

        String resumen = "Zure erosketa:\n";
        for (int i = 0; i < nireSaskia.getProduktuak().size(); i++) {
            resumen += "- " + nireSaskia.getProduktuak().get(i).getIzena() + " x" + nireSaskia.getKopuruak().get(i) + "\n";
        }
        resumen += "\nGUZTIRA: " + String.format("%.2f", nireSaskia.kalkulatuGuztira()) + " €";

        int confirm = JOptionPane.showConfirmDialog(this, resumen + "\n\nErosketa prozesatu nahi duzu?", "Erosketa Bukatu", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            EskariDAO edao = new EskariDAO();
            if (edao.erosi(usuarioActual.getId(), nireSaskia)) {
                JOptionPane.showMessageDialog(this, "Erosketa ondo burutu da! Eskerrik asko.");
                nireSaskia.garbitu();
                btnHeaderSaskia.setText("Saskia (0)");
                refrescarTabla();
            } else {
                JOptionPane.showMessageDialog(this, "Errorea erosketa egiterakoan.", "Errorea", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}