package com.haoyou.spring.cloud.alibaba.fighting.Info;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.domain.message.BaseMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(value = {},ignoreUnknown = true)
@EqualsAndHashCode(callSuper = false)
public class FightingReq extends BaseMessage implements Serializable {
    private static final long serialVersionUID = 452273190151315559L;
    //房间uid
    private String fightingRoomUid;
    //行动宠物位置
    private Integer currentPetId;
    //消除块的种类
    private Integer blockType;
    //消除快的数量
    private Integer blockCount;
    //消除操作信息
    private List<BlockInfo> destroyInfos;

    //新生成的块
    private List<BlockInfo> newInfos;

    public FightingReq() {
        this.currentPetId=0;
        this.blockType=0;
        this.blockCount=0;
    }


    public boolean check(){
        /**
         * 校验块，并且补充块的种类和数量
         */
        if(destroyInfos!=null&&destroyInfos.size()>0){
            this.blockCount=destroyInfos.size();
            this.blockType=destroyInfos.get(0).getRandomID();
            for(BlockInfo blockInfo:destroyInfos){
                if(blockInfo.getRandomID()!=this.blockType){
                    return false;
                }
            }
        }
        return true;
    }
}
