package com.leyou.order.mapper;

import com.leyou.order.pojo.OrderStatus;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface OrderStatusMapper extends Mapper<OrderStatus> {

    @Update("UPDATE tb_order_status SET STATUS = #{status}  WHERE order_id = #{orderId}")
    int updateStatus(@Param("orderId")Long orderId,@Param("status")Integer status);
}
