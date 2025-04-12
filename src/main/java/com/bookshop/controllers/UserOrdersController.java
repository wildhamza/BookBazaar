package com.bookshop.controllers;

import com.bookshop.models.Order;
import com.bookshop.models.User;
import com.bookshop.services.OrderService;
import com.bookshop.utils.AlertHelper;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

public class UserOrdersController {
    @FXML
    private TableView<Order> ordersTableView;
    
    @FXML
    private TableColumn<Order, Integer> orderIdColumn;
    
    @FXML
    private TableColumn<Order, String> orderDateColumn;
    
    @FXML
    private TableColumn<Order, Double> totalAmountColumn;
    
    @FXML
    private TableColumn<Order, String> statusColumn;
    
    private OrderService orderService;
    
    @FXML
    public void initialize() {
        orderService = new OrderService();
        initializeColumns();
    }
    
    private void initializeColumns() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }
    
    public void loadUserOrders(User user) {
        try {
            List<Order> orders = orderService.getOrdersByUser(user.getId());
            ordersTableView.getItems().clear();
            ordersTableView.getItems().addAll(orders);
        } catch (SQLException e) {
            AlertHelper.showError("Error", "Failed to load orders", e.getMessage());
        }
    }
} 