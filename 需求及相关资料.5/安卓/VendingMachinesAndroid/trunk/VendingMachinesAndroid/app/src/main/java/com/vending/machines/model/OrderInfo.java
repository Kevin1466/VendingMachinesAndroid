package com.vending.machines.model;


public class OrderInfo {
    private boolean success = false; //是否能取货
    private String orderNum;
    private OrderStatus orderStatus;
    private Long goodsId;
    private String goodsPrice;
    private Integer goodsNum;
    private String totalPrice;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsPrice() {
        return goodsPrice;
    }

    public void setGoodsPrice(String goodsPrice) {
        this.goodsPrice = goodsPrice;
    }

    public Integer getGoodsNum() {
        return goodsNum;
    }

    public void setGoodsNum(Integer goodsNum) {
        this.goodsNum = goodsNum;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return "OrderInfo{" +
                "success=" + success +
                ", orderNum='" + orderNum + '\'' +
                ", orderStatus=" + orderStatus +
                ", goodsId=" + goodsId +
                ", goodsPrice='" + goodsPrice + '\'' +
                ", goodsNum=" + goodsNum +
                ", totalPrice='" + totalPrice + '\'' +
                '}';
    }
}
