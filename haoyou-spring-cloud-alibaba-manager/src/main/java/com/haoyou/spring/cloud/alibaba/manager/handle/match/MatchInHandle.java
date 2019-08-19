package com.haoyou.spring.cloud.alibaba.manager.handle.match;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.domain.ResponseMsg;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.fighting.info.FightingPet;
import com.haoyou.spring.cloud.alibaba.manager.handle.ManagerHandle;
import com.haoyou.spring.cloud.alibaba.sofabolt.protocol.MyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 加入匹配处理
 */
@Service
public class MatchInHandle extends ManagerHandle {


    private static final long serialVersionUID = 5371089693863609511L;
    private static final Logger logger = LoggerFactory.getLogger(MatchInHandle.class);
    @Override
    protected void setHandleType() {
        this.handleType = SendType.MATCH_IN;
    }

    @Override
    public BaseMessage handle(MyRequest req) {
        User user = req.getUser();
        MapBody mapBody = MapBody.beSuccess();

        List<FightingPet> fightingPets = FightingPet.getByUser(user, redisObjectUtil);

        int i = 0;
        for (FightingPet fightingPet : fightingPets) {
            Integer iswork = fightingPet.getPet().getIswork();
            if (iswork != null && iswork != 0) {
                i++;
            }
        }
        if (i < 3) {
            mapBody.put("errMsg", "您上阵的宠物不足3只！");
            return mapBody.err();
        }
        //结算最后半小时关闭天梯
        DateTime date = DateUtil.date();
        DateTime dateTime = DateUtil.offsetDay(date, 1);

        int dayOfMonth = dateTime.dayOfMonth();
        if(dayOfMonth == 1){
            int hour = date.hour(true);
            int minute = date.minute();
            if(hour == 23 && minute>30){
                mapBody.put("errMsg", "结算前半个小时关闭天梯！");
                return mapBody.err();
            }
        }


        BaseMessage baseMessage = new BaseMessage();
        if(matchService.putPlayerIntoMatchPool(req.getUser())){
            baseMessage.setState(ResponseMsg.MSG_SUCCESS);
        }else{
            baseMessage.setState(ResponseMsg.MSG_ERR);
        }
        return baseMessage;
    }
}
