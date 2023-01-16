package com.junsi.reggie.dto;

import com.junsi.reggie.entity.OrderDetail;
import com.junsi.reggie.entity.Orders;
import lombok.Data;
import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;

    private int sumNum; // 商品总数
	
}
