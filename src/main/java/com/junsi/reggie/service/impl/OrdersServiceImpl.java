package com.junsi.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.junsi.reggie.common.BaseContext;
import com.junsi.reggie.common.CustomException;
import com.junsi.reggie.entity.*;
import com.junsi.reggie.mapper.OrdersMapper;
import com.junsi.reggie.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户提交订单
     * @param orders
     */
    @Transactional
    @Override
    public void submit(Orders orders) {
        // 获得当前用户id
        Long userId = BaseContext.getCurrentId();

        // 获取当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);

        if (shoppingCarts.size() == 0) {
            throw new CustomException("购物车为空，不能下单");
        }

        // 获取当前用户数据
        User user = userService.getById(userId);

        // 获取当前用户地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);

        if (addressBook == null) {
            throw new CustomException("当前地址为空，不能下单");
        }

        // 存入订单明细表（多条）
        long orderId = IdWorker.getId(); // 生成一个订单号
        AtomicInteger amount = new AtomicInteger(0); // 计算总价
        // 获取一系列订单明细
        List<OrderDetail> orderDetailList = shoppingCarts.stream().map((item) -> {
            // 根据购物车商品对象创建一个订单明细对象
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            // 顺便计算购物车内所有商品总价
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        orderDetailService.saveBatch(orderDetailList);


        // 把下单数据存入订单表（一条）
        orders.setNumber(String.valueOf(orderId)); // 设置订单号
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get())); // 设置总金额
        orders.setUserId(userId);
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(user.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        this.save(orders);


        // 清空购物车数据
        shoppingCartService.remove(queryWrapper);
    }

    /**
     * 再来一单
     * @param orders
     */
    @Transactional
    @Override
    public void again(Orders orders) {
        // 获取当前订单数据
        Orders originalOrder = this.getById(orders);

        // 获取当前订单明细数据
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, originalOrder.getNumber());
        List<OrderDetail> originalOrderDetails = orderDetailService.list(queryWrapper);

        // 新建订单并保存表中
        Orders newOrder = new Orders();
        long orderId = IdWorker.getId(); // 新建订单号
        BeanUtils.copyProperties(originalOrder, newOrder, "id");
        newOrder.setStatus(2);
        newOrder.setCheckoutTime(LocalDateTime.now());
        newOrder.setOrderTime(LocalDateTime.now());
        newOrder.setNumber(String.valueOf(orderId));

        this.save(newOrder);

        // 新增订单明细并保存表中
        for (OrderDetail od: originalOrderDetails) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(od, orderDetail, "id");
            orderDetail.setOrderId(orderId);
            orderDetailService.save(orderDetail);
        }
    }
}
