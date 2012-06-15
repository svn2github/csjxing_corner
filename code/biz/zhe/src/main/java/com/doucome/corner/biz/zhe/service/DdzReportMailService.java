package com.doucome.corner.biz.zhe.service;

import java.util.List;

import com.doucome.corner.biz.dal.dataobject.DdzTaokeReportSettleDO;

/**
 * 类DdzReportMailService.java的实现描述：报表邮件
 * 
 * @author ib 2012-5-7 下午11:31:08
 */
public interface DdzReportMailService {

    /**
     * 批量发送打款成功邮件
     * 
     * @param settleIdList
     * @return
     */
    public int sendTaokeReportMailsForIds(List<Integer> settleIdList);

    public int sendTaokeReportMails(List<DdzTaokeReportSettleDO> settleList);
    
    /**
     * 发送打款成功邮件
     * 
     * @param settleId
     * @return
     */
    public boolean sendTaokeReportMail(Long settleId);
}
