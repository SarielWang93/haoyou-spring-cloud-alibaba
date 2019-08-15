package com.haoyou.spring.cloud.alibaba.fighting.info;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.entity.LevelDesign;
import com.haoyou.spring.cloud.alibaba.util.SendMsgUtil;
import com.haoyou.spring.cloud.alibaba.commons.domain.SendType;
import com.haoyou.spring.cloud.alibaba.commons.message.MapBody;
import com.haoyou.spring.cloud.alibaba.commons.entity.Skill;
import com.haoyou.spring.cloud.alibaba.commons.entity.User;

import com.haoyou.spring.cloud.alibaba.fighting.info.fightingstate.FightingState;
import lombok.Data;


import java.io.Serializable;
import java.util.*;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class FightingRoom implements Serializable {
    private static final long serialVersionUID = 3878881711527519063L;


    //房间uid
    private String uid;

    //战斗种类（PVE,PVP）
    private String fightingType;
    //闯关模式难度
    private int difficult;
    //关卡设计
    private LevelDesign levelDesign;

    //创建时间
    private Date creatTime;
    //结束时间
    private Date overTime;


    //详细步骤
    private int step;
    //详细记录，整个房间的所有操作
    private TreeMap<Integer, FightingStep> steps;
    //当前操作的步骤信息
    private transient TreeMap<Integer, FightingStep> nowSteps;

    //出手数
    private int shotNum;
    //出手操作记录
    private TreeMap<Integer, FightingReq> shots;

    //阵营 key：useruid
    private Map<String, FightingCamp> fightingCamps;

    //棋盘对象
    private FightingBoard fightingBoard;

    //当前行动阵营
    private String campNow;

    //当前行动宠物
    private Integer petNow;


    public FightingRoom() {
    }
    public FightingRoom(String fightingType) {
        this.fightingType = fightingType;
        intFightingRoom();
    }
    public FightingRoom(LevelDesign levelDesign,int difficult,String fightingType) {

        this.fightingType = fightingType;
        this.levelDesign = levelDesign;
        this.difficult = difficult;
        intFightingRoom();
    }
    private void intFightingRoom(){
        this.step = 0;
        this.shotNum = 0;

        this.uid = IdUtil.simpleUUID();

        this.steps = new TreeMap<>();

        this.nowSteps = new TreeMap<>();

        this.shots = new TreeMap<>();
    }

    /**
     * 开启新回合
     *
     * @param fightingPet 此回合行动宠物
     */
    public void startRount(FightingPet fightingPet) {
        this.campNow = fightingPet.getFightingCamp().getUser().getUid();
        this.petNow = fightingPet.getPet().getIswork();
        this.creatTime = new Date();
        /**
         * 刷新回合
         */
        fightingPet.reflashRound();

    }

    /**
     * 添加出手
     *
     * @param fightingReq
     */
    public void addShot(FightingReq fightingReq) {
        this.shotNum++;
        this.shots.put(this.shotNum, fightingReq);
    }

    /**
     * 添加步骤
     *
     * @param fightingStep
     */
    public void addStep(FightingStep fightingStep) {
        this.step++;
        this.steps.put(this.step, fightingStep);
        this.nowSteps.put(this.step, fightingStep);
    }

    /**
     * 初始化并发送信息结构
     *
     * @return
     */
    public void sendMsgInit(String userUid, SendMsgUtil sendMsgUtil) {
        List<String> userUids = new ArrayList<>();
        userUids.add(userUid);
        sendMsgInit(userUids, sendMsgUtil);
    }

    public void sendMsgInit(Collection<String> userUids, SendMsgUtil sendMsgUtil) {
        sendRoomMsg(userUids, sendMsgUtil);
        sendPetMsg(userUids, sendMsgUtil);
    }

    public void sendRoomMsg(Collection<String> userUids, SendMsgUtil sendMsgUtil) {
        sendMsgUtil.sendMsgList(userUids, SendType.FIGHTING_INIT, this.getRoomMSG());
    }

    public void sendPetMsg(Collection<String> userUids, SendMsgUtil sendMsgUtil) {
        Map<Integer, Map> petsMSG = this.getPetsMSG();
        for (Map pet : petsMSG.values()) {
            MapBody send = new MapBody();
            send.put("pet", pet);
            sendMsgUtil.sendMsgList(userUids, SendType.FIGHTING_INITPET, send);
        }
    }

    public void sendMsgResp(String userUid, SendMsgUtil sendMsgUtil) {
        List<String> userUids = new ArrayList<>();
        userUids.add(userUid);
        sendMsgResp(userUids, sendMsgUtil);
    }

    public void sendMsgResp(Collection<String> userUids, SendMsgUtil sendMsgUtil) {

        MapBody<String, Object> room = getRoomMSG();

        room.put("steps", this.nowSteps);
        room.put("shotNum", this.shotNum);
        room.remove("maps");
        Map<Integer, Map> pets = getPetsMSG();
        Map<String, Object> playerLeft = (Map<String, Object>) room.get("playerLeft");
        Map<String, Object> playerRight = (Map<String, Object>) room.get("playerRight");

        Map<String, Object>[] petsL = new Map[3];
        Map<String, Object>[] petsR = new Map[3];

        for (Map.Entry<Integer, Map> entry : pets.entrySet()) {
            Map pet = entry.getValue();
            Integer iswork = entry.getKey();
            pet.remove("skillInfos");

            if (iswork<3) {
                petsL[iswork] = pet;
            }else{
                petsR[iswork-3] = pet;
            }


        }

        playerLeft.put("pets", petsL);
        playerRight.put("pets", petsR);


        sendMsgUtil.sendMsgList(userUids, SendType.FIGHTING_RESP, room);

    }

    private MapBody<String, Object> getRoomMSG() {
        MapBody<String, Object> room = new MapBody<>();
        room.put("roomid", this.uid);
        room.put("fightingType",this.fightingType);
        room.put("maps", this.fightingBoard.getBoard());


        for (FightingCamp fightingCamp : fightingCamps.values()) {

            Map<String, Object> player = new HashMap<>();
            player.put("useruid", fightingCamp.getUser().getUid());
            player.put("name", fightingCamp.getUser().getUsername());
            player.put("isAi", fightingCamp.isAi());
            player.put("energy", fightingCamp.getEnergy());
            player.put("petsCount",fightingCamp.getFightingPets().size());

            if (this.campNow.equals(fightingCamp.getUser().getUid())) {
                player.put("currentPetId", this.petNow - 1);
            } else {
                player.put("currentPetId", -1);
            }


            if (fightingCamp.isLeft()) {
                room.put("playerLeft", player);
            } else {
                room.put("playerRight", player);
            }
        }
        return room;
    }

    private Map<Integer, Map> getPetsMSG() {

        Map<Integer, Map> pets = new HashMap<>();

        for (FightingCamp fightingCamp : fightingCamps.values()) {
            for (FightingPet fightingPet : fightingCamp.getFightingPets().values()) {
                Map<String, Object> pet = new HashMap<>();

                Integer index = fightingPet.getPet().getIswork() - 1;
                if (!fightingCamp.isLeft()) {
                    index += 3;
                }
                pet.put("index", index);

                pet.put("maxHP", fightingPet.getFt_max_hp());
                pet.put("HP", fightingPet.getHp());
                pet.put("shield", fightingPet.getFt_shield());
                pet.put("petName", fightingPet.getNickName());
                pet.put("petTypeId", fightingPet.getPet().getTypeId());
                pet.put("petId", fightingPet.getUid());
                pet.put("ATN", fightingPet.getFt_atn());
                pet.put("DEF", fightingPet.getFt_def());
                pet.put("SPD", fightingPet.getFt_spd());
                pet.put("LUK", fightingPet.getFt_luk());
                pet.put("isLuk", fightingPet.isLuky());
                pet.put("punishValue", fightingPet.getPunishValue());
                pet.put("ActionTime", fightingPet.getAction_time());
                pet.put("starLevel", fightingPet.getPet().getStarClass());

                List skills = new ArrayList();
                /**
                 * 技能
                 */
                for (Skill skill : fightingPet.getSkills()) {
                    Map<String, Object> sk = new HashMap<>();
                    sk.put("skillId", skill.getId());
                    sk.put("skillName", skill.getName());
                    sk.put("skillType", skill.getType());
                    sk.put("skillDescription", skill.getDescribe());
                    skills.add(sk);
                }
                pet.put("skillInfos", skills);
                /**
                 * 状态
                 */
                List buffs = new ArrayList();
                for (FightingState fightingState : fightingPet.getFightingStates()) {
                    Map<String, Object> buff = new HashMap<>();
                    buff.put("buffName", fightingState.getName());
                    buff.put("count", fightingState.getRound());
                    buffs.add(buff);
                }

                pet.put("buffs", buffs);

                pets.put(index, pet);
            }

        }
        return pets;
    }


    @Override
    public String toString() {
        return "FightingRoom{" +
                "uid='" + uid + '\'' +
                ", step=" + step +
                ", steps=" + steps +
                ", nowSteps=" + nowSteps +
                ", shotNum=" + shotNum +
                ", shots=" + shots +
                ", fightingCamps=" + fightingCamps +
                ", fightingBoard=" + fightingBoard +
                ", campNow='" + campNow + '\'' +
                ", petNow=" + petNow +
                '}';
    }
}
