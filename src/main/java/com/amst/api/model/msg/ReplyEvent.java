package com.amst.api.model.msg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyEvent implements Serializable {
      
    /**  
     * 用户ID  
     */  
    private String userId;
      
    /**  
     * 博客ID  
     */  
    private String content;

    /**
     * 回复Id
     */
    private Long replyId;
      

      
    /**  
     * 事件发生时间  
     */  
    private LocalDateTime eventTime;
      

}
