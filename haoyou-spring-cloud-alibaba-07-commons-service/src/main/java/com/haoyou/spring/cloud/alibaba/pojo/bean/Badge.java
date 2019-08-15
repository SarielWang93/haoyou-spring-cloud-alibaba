package com.haoyou.spring.cloud.alibaba.pojo.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haoyou.spring.cloud.alibaba.commons.message.BaseMessage;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * @author wanghui
 * @version 1.0
 */

@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class Badge implements Serializable {

    //章节
    private String chapterName;
    //关卡编号
    private Integer idNum;
    //难度
    private int difficult;
    //创建时间
    private Date creatTime;

    public Badge() {
    }

    public Badge(String chapterName, Integer idNum, int difficult) {
        this.chapterName = chapterName;
        this.idNum = idNum;
        this.difficult = difficult;
        this.creatTime = new Date();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Badge badge = (Badge) o;
        return difficult == badge.difficult &&
                Objects.equals(chapterName, badge.chapterName) &&
                Objects.equals(idNum, badge.idNum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chapterName, idNum, difficult);
    }
}
