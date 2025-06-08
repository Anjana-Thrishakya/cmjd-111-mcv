/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.ijse.mvc.model;

import edu.ijse.mvc.db.DBConnection;
import edu.ijse.mvc.dto.OrderDetailDto;
import edu.ijse.mvc.dto.OrderDto;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 *
 * @author Anjana
 */
public class OrderModel {
    
    public String placeOrder(OrderDto orderDto, ArrayList<OrderDetailDto> orderDetailDtos) throws  Exception{
        Connection connection = DBConnection.getInstance().getConnection();
        
        try {
            connection.setAutoCommit(false);
            
            String orderSql = "INSERT INTO Orders VALUES(?,?,?)";
            PreparedStatement orderStatement = connection.prepareStatement(orderSql);
            orderStatement.setString(1, orderDto.getOrderId());
            orderStatement.setString(2, orderDto.getDate());
            orderStatement.setString(3, orderDto.getCustId());
            
            boolean isOrderSaved = orderStatement.executeUpdate() > 0;
            
            if(isOrderSaved){
                boolean isOrderDetailSaved = true;
                String orderDetailSql = "INSERT INTO Orderdetail VALUES(?,?,?,?)";
                for (OrderDetailDto orderDetailDto : orderDetailDtos) {
                    PreparedStatement orderDetailStatemet = connection.prepareStatement(orderDetailSql);
                    orderDetailStatemet.setString(1, orderDto.getOrderId());
                    orderDetailStatemet.setString(2, orderDetailDto.getItemCode());
                    orderDetailStatemet.setInt(3, orderDetailDto.getQty());
                    orderDetailStatemet.setInt(4, orderDetailDto.getDiscount());
                    
                    if(!(orderDetailStatemet.executeUpdate() > 0)){
                        isOrderDetailSaved = false;
                    }  
                }
                
                if(isOrderDetailSaved){
                    boolean isItemUpdate = true;
                    String itemUpdateSql = "UPDATE Item SET QtyOnHand = QtyOnHand - ? WHERE ItemCode = ?";
                    for (OrderDetailDto orderDetailDto : orderDetailDtos) {
                        PreparedStatement itemStatement = connection.prepareStatement(itemUpdateSql);
                        itemStatement.setInt(1, orderDetailDto.getQty());
                        itemStatement.setString(2, orderDetailDto.getItemCode());
                        
                        if(!(itemStatement.executeUpdate() > 0)){
                            isItemUpdate = false;
                        }
                    }
                    
                    if(isItemUpdate){
                        connection.commit();
                        return "Success";
                    } else {
                        connection.rollback();
                        return "Item Update Error";
                    }
                } else {
                    connection.rollback();
                    return "Order Detail Save Error";
                }
            } else {
                connection.rollback();
                return "Order Save Error";
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
            connection.rollback();
            return "Order Save Error";
        } finally {
            connection.setAutoCommit(true);
        }
    }
    
}
