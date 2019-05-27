package com.haoyou.spring.cloud.alibaba.fighting.info;

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

    //房间uid（已淘汰）
    //private String fightingRoomUid;

    //行动宠物位置
    private Integer currentPetId;
    //消除快的数量
    private Integer blockCount;
    //消除块的种类
    private Integer blockType;
    //上一步，步骤
    private Integer step;
    //消除操作信息
    private List<BlockInfo> destroyInfos;

    //新生成的块
    private List<BlockInfo> newInfos;

    public FightingReq() {
        this.currentPetId=0;
        this.blockType=0;
        this.blockCount=0;
    }

}
