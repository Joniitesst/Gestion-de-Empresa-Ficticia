package Vista;

import Modelo.BezeroDAO;
import Modelo.Usuario;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class VentanaLogin extends JFrame {

    private JTextField txtUser;
    private JPasswordField txtPass;

    // GameStop koloreak (Estilo propioa)
    private Color gsRed = new Color(225, 6, 19);
    private Color gsBlack = new Color(30, 30, 30);
    private Color gsWhite = Color.WHITE;

    public VentanaLogin() {
        setTitle("GameStop - Saioa Hasi");
        setSize(380, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // Panel nagusia (Zuria eta garbia)
        JPanel panelCentral = new JPanel(new GridBagLayout());
        panelCentral.setBackground(gsWhite);
        panelCentral.setBorder(new EmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 0, 12, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. Titulua (GameStop estiloan)
        JLabel lblTitulo = new JLabel("GameStop", SwingConstants.CENTER);
        lblTitulo.setForeground(gsRed);
        lblTitulo.setFont(new Font("Arial Black", Font.BOLD, 28));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panelCentral.add(lblTitulo, gbc);

        // 2. Erabiltzailea testua
        JLabel lblUser = new JLabel("ERABILTZAILEA (EMAILA)");
        lblUser.setForeground(gsBlack);
        lblUser.setFont(new Font("Arial", Font.BOLD, 11));
        gbc.gridy = 1;
        panelCentral.add(lblUser, gbc);

        txtUser = new JTextField();
        txtUser.setPreferredSize(new Dimension(0, 35));
        txtUser.setFont(new Font("Arial", Font.PLAIN, 14));
        txtUser.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        gbc.gridy = 2;
        panelCentral.add(txtUser, gbc);

        // 3. Pasahitza testua
        JLabel lblPass = new JLabel("PASAHITZA");
        lblPass.setForeground(gsBlack);
        lblPass.setFont(new Font("Arial", Font.BOLD, 11));
        gbc.gridy = 3;
        panelCentral.add(lblPass, gbc);

        txtPass = new JPasswordField();
        txtPass.setPreferredSize(new Dimension(0, 35));
        txtPass.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPass.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        gbc.gridy = 4;
        panelCentral.add(txtPass, gbc);

        // 4. SARTU BOTOIA (Gure estilo propioa, ez Windows-ena)
        JButton btnLogin = new JButton("SARTU");
        btnLogin.setBackground(gsRed);
        btnLogin.setForeground(gsWhite);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 16));
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setPreferredSize(new Dimension(0, 45));

        // Estiloa behartu (Forzar estilo)
        btnLogin.setOpaque(true);
        btnLogin.setBorder(new LineBorder(gsRed, 1));

        gbc.gridy = 5;
        gbc.insets = new Insets(25, 0, 10, 0);
        panelCentral.add(btnLogin, gbc);

        // 5. ERREGISTRO BOTOIA (Link estiloa)
        JButton btnRegister = new JButton("Ez daukazu konturik? Erregistratu hemen");
        btnRegister.setContentAreaFilled(false);
        btnRegister.setBorderPainted(false);
        btnRegister.setForeground(Color.DARK_GRAY);
        btnRegister.setFont(new Font("Arial", Font.PLAIN, 12));
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        panelCentral.add(btnRegister, gbc);

        add(panelCentral, BorderLayout.CENTER);

        // --- Logika ---
        btnLogin.addActionListener(e -> {
            String user = txtUser.getText();
            String pass = new String(txtPass.getPassword());
            BezeroDAO bdao = new BezeroDAO();
            Usuario logueado = bdao.validarLogin(user, pass);
            if (logueado != null) {
                new VentanaPrincipal(logueado).setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Erabiltzaile edo pasahitz okerra.", "Errorea", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRegister.addActionListener(e -> abrirFormularioRegistro());
    }

    private void abrirFormularioRegistro() {
        JTextField regNom = new JTextField();
        JTextField regAbi = new JTextField();
        JTextField regEma = new JTextField();
        JPasswordField regPas = new JPasswordField();
        JTextField regHel = new JTextField();

        Object[] message = {
                "Izena:", regNom,
                "Abizenak:", regAbi,
                "Emaila (Erabiltzailea):", regEma,
                "Pasahitza:", regPas,
                "Helbidea:", regHel
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Bezero Berria Erregistratu", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String pass = new String(regPas.getPassword());
            if (regNom.getText().isEmpty() || regEma.getText().isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Izena, Emaila eta Pasahitza beharrezkoak dira.", "Errorea", JOptionPane.WARNING_MESSAGE);
                return;
            }
            BezeroDAO dao = new BezeroDAO();
            if (dao.erregistratuBezeroa(regNom.getText(), regAbi.getText(), regEma.getText(), pass, regHel.getText())) {
                JOptionPane.showMessageDialog(this, "Erregistroa ondo burutu da! Orain saioa hasi dezakezu.", "Arrakasta", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Errorea egon da datu basean gordetzean.", "Errorea", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        // Estilo propioa mantentzeko ez dugu UIManager-en sistemako look-and-feel-a kargatuko
        new VentanaLogin().setVisible(true);
    }
}