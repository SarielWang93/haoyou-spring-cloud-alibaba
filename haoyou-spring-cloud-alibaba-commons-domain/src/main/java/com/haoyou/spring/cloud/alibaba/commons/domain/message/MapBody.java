package com.haoyou.spring.cloud.alibaba.commons.domain.message;


import lombok.Data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Data
public class MapBody<K,V> extends BaseMessage{

    private static final long serialVersionUID = -3999396492949499427L;
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

    public Collection<V> values(){
        return this.msg.values();
    }
}