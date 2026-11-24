package com.regiunic.orders.ui;

import com.regiunic.core.concurrent.AsyncExecutor;
import com.regiunic.core.ui.AppTheme;
import com.regiunic.core.ui.ViewNavigator;
import com.regiunic.core.ui.components.AnimatedButton;
import com.regiunic.orders.model.Order;
import com.regiunic.orders.service.OrderService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistoryView extends JPanel {
    private final ViewNavigator navigator;
    private final OrderService service;
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Cliente", "Producto", "Cantidad", "Total", "Estado", "Fecha"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public HistoryView(ViewNavigator navigator, OrderService service) {
        this.navigator = navigator;
        this.service = service;
        setLayout(new BorderLayout(10,10));
        setBackground(AppTheme.WINDOW_BACKGROUND);
        initUI();
    }

    private void initUI() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(AppTheme.PANEL_BACKGROUND);
        top.setBorder(BorderFactory.createEmptyBorder(10,15,10,15));
        AnimatedButton back = AppTheme.primaryButton("← Menú");
        back.addActionListener(e -> navigator.showMainMenu(navigator.getCurrentUser()));
        JLabel title = new JLabel("Historial de Ventas", SwingConstants.CENTER);
        title.setFont(AppTheme.SECTION);
        title.setForeground(AppTheme.NAVY);
        top.add(back, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        table.setFont(AppTheme.BODY);
        table.setRowHeight(29);
        table.getTableHeader().setFont(AppTheme.LABEL);
        table.getTableHeader().setBackground(AppTheme.NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0,16,0,16));
        ViewNavigator.configureScrollPane(scroll, table.getBackground(), false);
        add(scroll, BorderLayout.CENTER);

        AnimatedButton refresh = AppTheme.secondaryButton("Actualizar historial");
        refresh.addActionListener(e -> loadData());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER,10,12));
        bottom.setBackground(AppTheme.WINDOW_BACKGROUND);
        bottom.add(refresh);
        add(bottom, BorderLayout.SOUTH);
    }

    public void loadData() {
        AsyncExecutor.supplyAsync(
                () -> {
                    try { return service.listOrders(); }
                    catch (SQLException e) { throw new RuntimeException(e); }
                },
                orders -> {
                    model.setRowCount(0);
                    for (Order o : orders) {
                        model.addRow(new Object[]{o.client(), o.product(), o.quantity(),
                                String.format("$ %,.2f", o.total()), o.status(),
                                o.createdAt() == null ? "" : o.createdAt().format(formatter)});
                    }
                },
                error -> JOptionPane.showMessageDialog(this, "No se pudo cargar el historial: " + error.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE));
    }
}
