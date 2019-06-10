package com.haoyou.spring.cloud.alibaba.commons.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name = "version_control")
@Data
@JsonIgnoreProperties(value = {}, ignoreUnknown = true)
public class VersionControl implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String uid;

    @Column(name = "force_game_update")
    private Boolean forceGameUpdate;

    @Column(name = "latest_game_version")
    private String latestGameVersion;

    @Column(name = "internal_game_version")
    private Integer internalGameVersion;

    @Column(name = "internal_resource_version")
    private Integer internalResourceVersion;

    @Column(name = "game_update_url")
    private String gameUpdateUrl;

    @Column(name = "version_list_length")
    private Long versionListLength;

    @Column(name = "version_list_hash_code")
    private Long versionListHashCode;

    @Column(name = "version_list_zip_length")
    private Long versionListZipLength;

    @Column(name = "version_list_zip_hash_code")
    private Long versionListZipHashCode;

    @Column(name = "end_of_json")
    private String endOfJson;

    /**
     * 创建时间
     */
    private Date date;


    /**
     * 描述
     */
    @Column(name = "`describe`")
    private String describe;

    @Column(name = "devic_type")
    private String devicType;
}