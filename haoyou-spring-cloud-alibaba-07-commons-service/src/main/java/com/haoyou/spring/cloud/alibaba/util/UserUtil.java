package com.haoyou.spring.cloud.alibaba.util;

import cn.hutool.core.util.IdUtil;
import com.haoyou.spring.cloud.alibaba.commons.entity.Prop;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.ZIP;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wanghui
 * @version 1.0
 * @date 2019/7/29 11:50
 */
@Service
public class UserUtil {


    /**
     * 添加道具
     * @param user
     * @param prop
     */
    public static void addProp(User user,Prop prop) {
        List<Prop> list = new ArrayList<>();
        list.add(prop);
        addProps(user,list);
    }

    public static void addProps(User user, List<Prop> propList){
        try {
            List<Prop> propsThis = user.propList();
            List<Prop> propsOver = new ArrayList<>();

            for (Prop prop : propList) {

                int count = 1;
                if (prop.getCount() != 0) {
                    count = prop.getCount();
                }
                int i = 0;
                if ((i = propsThis.indexOf(prop)) != -1) {
                    propsThis.get(i).setCount(propsThis.get(i).getCount() + count);
                } else {
                    if (propsThis.size() < user.getCurrency().getPropMax()) {
                        prop.setPropInstenceUid(IdUtil.simpleUUID());
                        prop.setCount(count);
                        propsThis.add(prop);
                    }else{
                        propsOver.add(prop);
                    }
                }
                user.getCurrency().setProps(ZIP.gZip(MapperUtils.obj2jsonIgnoreNull(propsThis).getBytes("UTF-8")));
            }

            if(!propsOver.isEmpty()){
                propsEmail(user,propsOver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 超出的道具发送邮箱
     * @param user
     * @param propsOver
     */
    public static void propsEmail(User user, List<Prop> propsOver){
            //TODO 超出的道具发送邮箱
    }
}
