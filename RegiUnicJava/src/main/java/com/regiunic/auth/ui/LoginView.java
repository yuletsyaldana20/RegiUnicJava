package com.regiunic.auth.ui;

import com.regiunic.auth.service.AuthService;
import com.regiunic.core.concurrent.AsyncExecutor;
import com.regiunic.core.ui.AppTheme;
import com.regiunic.core.ui.ViewNavigator;
import com.regiunic.core.ui.components.AnimatedButton;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Pantalla de inicio de sesión de RegiUnic.
 * Implementada como un JPanel desacoplado para navegación en ventana única.
 */
public class LoginView extends JPanel {
    private final ViewNavigator navigator;
    private final AuthService authService;

    private final JTextField txtUsuario = new JTextField(15);
    private final JPasswordField txtPassword = new JPasswordField(15);
    private final AnimatedButton btnLogin = AppTheme.primaryButton("Iniciar Sesión");
    private final AnimatedButton btnRegistrarse = AppTheme.secondaryButton("Registrarse");

    public LoginView(ViewNavigator navigator, AuthService authService) {
        this.navigator = navigator;
        this.authService = authService;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(AppTheme.WINDOW_BACKGROUND);

        initUI();
    }

    private void initUI() {
        JPanel centeringPanel = new JPanel(new GridBagLayout());
        centeringPanel.setBackground(AppTheme.WINDOW_BACKGROUND);

        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(20, 55, 105), 2),
                BorderFactory.createEmptyBorder(35, 45, 35, 45)
        ));

        // Logo institucional
        JLabel lblLogo = new JLabel(AppTheme.loadScaledIcon("assets/Logo.png", 300, 300));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(lblLogo);
        tarjeta.add(Box.createRigidArea(new Dimension(0, 10)));

        // Subtítulo
        JLabel lblSubtitulo = new JLabel("Control de Ventas e Inventario");
        lblSubtitulo.setFont(AppTheme.SECTION);
        lblSubtitulo.setForeground(AppTheme.TEXT_MUTED);
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(lblSubtitulo);
        tarjeta.add(Box.createRigidArea(new Dimension(0, 20)));

        // Campo Usuario
        JLabel lblUsuario = new JLabel("Usuario");
        lblUsuario.setFont(AppTheme.FORM_HEADING);
        lblUsuario.setForeground(AppTheme.NAVY);
        lblUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(lblUsuario);
        tarjeta.add(Box.createRigidArea(new Dimension(0, 5)));

        txtUsuario.setMaximumSize(new Dimension(260, 36));
        txtUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(txtUsuario);
        tarjeta.add(Box.createRigidArea(new Dimension(0, 15)));

        // Campo Contraseña
        JLabel lblPassword = new JLabel("Contraseña");
        lblPassword.setFont(AppTheme.FORM_HEADING);
        lblPassword.setForeground(AppTheme.NAVY);
        lblPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(lblPassword);
        tarjeta.add(Box.createRigidArea(new Dimension(0, 5)));

        txtPassword.setMaximumSize(new Dimension(260, 36));
        txtPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(txtPassword);
        tarjeta.add(Box.createRigidArea(new Dimension(0, 8)));

        // Checkbox Mostrar Contraseña
        JCheckBox chkMostrar = new JCheckBox("Mostrar contraseña");
        chkMostrar.setFont(AppTheme.BODY);
        chkMostrar.setForeground(AppTheme.TEXT_MUTED);
        chkMostrar.setBackground(Color.WHITE);
        chkMostrar.setFocusPainted(false);
        chkMostrar.setAlignmentX(Component.CENTER_ALIGNMENT);
        chkMostrar.addActionListener(e -> {
            txtPassword.setEchoChar(chkMostrar.isSelected() ? (char) 0 : '•');
        });
        tarjeta.add(chkMostrar);
        tarjeta.add(Box.createRigidArea(new Dimension(0, 20)));

        // Botones de Acción
        btnLogin.setFont(AppTheme.ACTION);
        btnLogin.setPreferredSize(new Dimension(180, 46));
        btnLogin.addActionListener(e -> ejecutarLogin());

        btnRegistrarse.setPreferredSize(new Dimension(160, 46));
        btnRegistrarse.addActionListener(e -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            new RegisterDialog(parentFrame, authService).setVisible(true);
        });

        // Enter key shortcut para submit
        txtPassword.addActionListener(e -> ejecutarLogin());
        txtUsuario.addActionListener(e -> txtPassword.requestFocusInWindow());

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelBotones.setBackground(Color.WHITE);
        panelBotones.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelBotones.add(btnLogin);
        panelBotones.add(btnRegistrarse);
        tarjeta.add(panelBotones);

        centeringPanel.add(tarjeta);

        JScrollPane scrollPane = new JScrollPane(centeringPanel);
        scrollPane.setBorder(null);
        ViewNavigator.configureScrollPane(scrollPane, AppTheme.WINDOW_BACKGROUND, false);
        add(scrollPane);
    }

    private void ejecutarLogin() {
        String usuario = txtUsuario.getText().trim();
        char[] password = txtPassword.getPassword();

        if (usuario.isEmpty() || password.length == 0) {
            Arrays.fill(password, '\0');
            JOptionPane.showMessageDialog(
                    this,
                    "Por favor, ingrese el usuario y la contraseña.",
                    "Campos incompletos",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        setControlesHabilitados(false);
        btnLogin.setText("Verificando...");

        AsyncExecutor.supplyAsync(
                () -> {
                    try {
                        return authService.authenticate(usuario, password);
                    } catch (SQLException ex) {
                        throw new RuntimeException("Error al conectar con la base de datos SQLite: " + ex.getMessage(), ex);
                    } finally {
                        Arrays.fill(password, '\0');
                    }
                },
                autenticado -> {
                    setControlesHabilitados(true);
                    btnLogin.setText("Iniciar Sesión");
                    if (autenticado) {
                        txtPassword.setText("");
                        navigator.showMainMenu(usuario);
                    } else {
                        JOptionPane.showMessageDialog(
                                this,
                                "El usuario o la contraseña no son correctos.",
                                "No se pudo iniciar sesión",
                                JOptionPane.WARNING_MESSAGE
                        );
                    }
                },
                error -> {
                    setControlesHabilitados(true);
                    btnLogin.setText("Iniciar Sesión");
                    JOptionPane.showMessageDialog(
                            this,
                            "Error durante la autenticación: " + error.getMessage(),
                            "Error de Acceso",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
        );
    }

    private void setControlesHabilitados(boolean habilitar) {
        btnLogin.setEnabled(habilitar);
        btnRegistrarse.setEnabled(habilitar);
        txtUsuario.setEnabled(habilitar);
        txtPassword.setEnabled(habilitar);
    }

    public void resetForm() {
        txtUsuario.setText("");
        txtPassword.setText("");
        setControlesHabilitados(true);
        btnLogin.setText("Iniciar Sesión");
    }
}
