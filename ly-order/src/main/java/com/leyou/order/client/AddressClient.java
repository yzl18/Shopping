package com.leyou.order.client;

import com.leyou.order.dto.AddressDTO;

import java.util.ArrayList;
import java.util.List;

public abstract class AddressClient {
    public static final List<AddressDTO> addressList = new ArrayList<AddressDTO>(){
        {
            AddressDTO address = new AddressDTO();
            address.setId(1L);
            address.setAddress("长寿路800号苏提春晓");
            address.setState("上海省");
            address.setCity("上海市");
            address.setDistrict("普陀区");
            address.setName("柴启晨");
            address.setPhone("15179500147");
            address.setZipCode("331100");
            address.setIsDefault(true);
            add(address);

            AddressDTO address2 = new AddressDTO();
            address2.setId(2L);
            address2.setAddress("井冈山大学");
            address2.setDistrict("青原区");
            address2.setCity("吉安市");
            address2.setState("江西省");
            address2.setName("杨泽林");
            address2.setPhone("15179500147");
            address2.setZipCode("331100");
            address2.setIsDefault(false);
            add(address2);
        }

    };

    public static AddressDTO findById(Long id){
        for (AddressDTO addressDTO : addressList) {
            if (addressDTO.getId().equals(id))
                return addressDTO;
        }
        return null;
    }
}
