package com.haoyou.spring.cloud.alibaba.commons.message;


import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import lombok.Data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
public class MapBody<K,V> extends BaseMessage{

    private static final long serialVersionUID = -3999396492949499427L;


    public static MapBody beSuccess(){
        MapBody mapBody = new MapBody();
        mapBody.setState(ResponseMsg.MSG_SUCCESS);
        return mapBody;
    }
    public static MapBody beErr(){
        MapBody mapBody = new MapBody();
        mapBody.setState(ResponseMsg.MSG_ERR);
        return mapBody;
    }










    private Map<K,V> msg;

    public MapBody() {
        this.msg = new HashMap<>();
    }

    public void put(K k,V v){
        this.msg.put(k,v);
    }

    public V  get(K k){
        return this.msg.get(k);
    }

    public V remove(K k){
        return this.msg.remove(k);
    }

    public Collection<V> values(){
        return this.msg.values();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapBody<?, ?> mapBody = (MapBody<?, ?>) o;
        return Objects.equals(msg, mapBody.msg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(msg);
    }
}
