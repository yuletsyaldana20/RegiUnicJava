package com.regiunic.auth.ui;

import com.regiunic.auth.service.AuthService;
import com.regiunic.core.concurrent.AsyncExecutor;
import com.regiunic.core.ui.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Diálogo modal para el registro de nuevos usuarios en RegiUnic.
 */
public class RegisterDialog extends JDialog {
    private final AuthService authService;
    private final JTextField txtUsuario = new JTextField(18);
    private final JPasswordField txtPassword = new JPasswordField(18);
    private final JPasswordField txtConfirmacion = new JPasswordField(18);
    private final JButton btnGuardar = AppTheme.primaryButton("Crear Cuenta");
    private final JButton btnCancelar = AppTheme.secondaryButton("Cancelar");

    public RegisterDialog(JFrame parent, AuthService authService) {
        super(parent, "RegiUnic | Registro de Cuenta", true);
        this.authService = authService;

        setSize(420, 360);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel container = new JPanel(new BorderLayout(15, 15));
        container.setBackground(AppTheme.PANEL_BACKGROUND);
        container.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Registrar Nuevo Usuario");
        title.setFont(AppTheme.SECTION);
        title.setForeground(AppTheme.NAVY);
        container.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.setOpaque(false);

        form.add(new JLabel("Nombre de usuario:"));
        form.add(txtUsuario);
        form.add(new JLabel("Contraseña (mínimo 8 caracteres):"));
        form.add(txtPassword);
        form.add(new JLabel("Confirmar contraseña:"));
        form.add(txtConfirmacion);

        char echoPass = txtPassword.getEchoChar();
        char echoConf = txtConfirmacion.getEchoChar();
        JButton btnVer = AppTheme.secondaryButton("Mostrar contraseñas");
        btnVer.setPreferredSize(new Dimension(180, 32));
        btnVer.addActionListener(e -> {
            boolean mostrar = btnVer.getText().equals("Mostrar contraseñas");
            txtPassword.setEchoChar(mostrar ? (char) 0 : echoPass);
            txtConfirmacion.setEchoChar(mostrar ? (char) 0 : echoConf);
            btnVer.setText(mostrar ? "Ocultar contraseñas" : "Mostrar contraseñas");
        });
        form.add(btnVer);

        container.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        btnGuardar.addActionListener(e -> ejecutarRegistro());
        btnCancelar.addActionListener(e -> dispose());
        btnPanel.add(btnCancelar);
        btnPanel.add(btnGuardar);
        container.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(container);
    }

    private void ejecutarRegistro() {
        String usuario = txtUsuario.getText().trim();
        char[] pass = txtPassword.getPassword();
        char[] conf = txtConfirmacion.getPassword();

        if (usuario.isEmpty() || pass.length == 0 || conf.length == 0) {
            JOptionPane.showMessageDialog(this, "Por favor completa todos los campos.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnGuardar.setEnabled(false);
        btnGuardar.setText("Creando...");

        AsyncExecutor.supplyAsync(
                () -> {
                    try {
                        authService.register(usuario, pass, conf);
                        return true;
                    } catch (SQLException ex) {
                        throw new RuntimeException("Error en la base de datos: " + ex.getMessage(), ex);
                    } finally {
                        Arrays.fill(pass, '\0');
                        Arrays.fill(conf, '\0');
                    }
                },
                success -> {
                    JOptionPane.showMessageDialog(
                            this,
                            "La cuenta se creó correctamente en SQLite. Ya puedes iniciar sesión.",
                            "Registro exitoso",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    dispose();
                },
                error -> {
                    btnGuardar.setEnabled(true);
                    btnGuardar.setText("Crear Cuenta");
                    String msg = error.getCause() != null ? error.getCause().getMessage() : error.getMessage();
                    JOptionPane.showMessageDialog(this, msg, "No se pudo crear la cuenta", JOptionPane.WARNING_MESSAGE);
                }
        );
    }
}
