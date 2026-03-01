package com.fuzhou.pojo.entity;
import lombok.Data;

@Data
public class ShowActor {
    private Long id;
    private Long actorId;            // 演员ID
    private Long showId;             // 演出ID
}
