package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import com.haoyou.spring.cloud.alibaba.commons.util.MapperUtils;
import com.haoyou.spring.cloud.alibaba.commons.util.ZIP;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class Award extends BaseMessage implements Serializable {
    private static final long serialVersionUID = 6681165408113017374L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String type;

    /**
     * 金币
     */
    private Integer coin;

    /**
     * 钻石
     */
    private Integer diamond;

    /**
     * 玩家升级经验
     */
    private Long exp;

    /**
     * 宠物升级经验
     */
    @Column(name = "pet_exp")
    private Long petExp;

    /**
     * 道具（json存储）
     */
    private byte[] props;

    @Transient
    private boolean used;
    @Transient
    private Date upAwardDate;

    @Transient
    private List<Prop> propsList;


    public Award() {
    }

    public Award init(int coin, int diamond, long exp, long petExp, List<Prop> props) {
        this.coin = coin;
        this.diamond = diamond;
        this.exp = exp;
        this.petExp = petExp;
        this.propsList = props;
        this.propList(props);
        return this;
    }

    public void propList(){
        List<Prop> props = null;
        if (this.props!=null) {
            try {
                props = MapperUtils.json2list(new String(ZIP.unGZip(this.props), "UTF-8"), Prop.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            props = new ArrayList<>();
        }
        this.propsList = props;
    }
    public void propList(List<Prop> props){
        try {
            this.props = ZIP.gZip(MapperUtils.obj2jsonIgnoreNull(props).getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public Award notToLong(){
        if(this.props != null && this.propsList == null){
            this.propList();
        }
        this.props = null;
        return this;
    }
}
