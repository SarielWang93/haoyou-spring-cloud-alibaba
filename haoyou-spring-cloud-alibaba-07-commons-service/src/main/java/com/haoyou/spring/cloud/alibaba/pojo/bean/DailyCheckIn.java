package com.haoyou.spring.cloud.alibaba.pojo.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.entity.Award;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author wanghui
 * @version 1.0
 */

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class DailyCheckIn implements Serializable {

    private List<Award> awards;

    public boolean allUsed(){

        for(Award award:this.awards){
            if(!award.isUsed()){
                return false;
            }
        }
        return true;
    }

}
