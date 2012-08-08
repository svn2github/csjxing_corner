package com.doucome.corner.biz.dal.condition;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.doucome.corner.biz.dal.model.AbstractModel;

public class TaokeReportSearchCondition extends AbstractModel {

    private Date     gmtPaidStart;

    private Date     gmtPaidEnd;

    private String   settleStatus;

    private String[] settleStatusList;

    private String   settleUid;

    private String   settleTaobaoNick;

    private String   settleAlipay;

    private Date     gmtSettledStart;

    private Date     gmtSettledEnd;
    
    private Date 	 gmtCreateStart ;
    
    private Date 	 gmtCreateEnd ;

    private String   payBatchno;

    private Long     settleId;
    
    public Map<String,Object> toMap(){
    	Map<String,Object> condition = new HashMap<String,Object>() ;
    	condition.put("gmtPaidStart", gmtPaidStart) ;
    	condition.put("gmtPaidEnd", gmtPaidEnd) ;
    	condition.put("settleStatus", settleStatus) ;
    	condition.put("settleStatusList", settleStatusList) ;
    	condition.put("settleUid", settleUid) ;
    	condition.put("settleTaobaoNick", settleTaobaoNick) ;
    	condition.put("settleAlipay", settleAlipay) ;
    	condition.put("gmtSettledStart", gmtSettledStart) ;
    	condition.put("gmtSettledEnd", gmtSettledEnd) ;
    	condition.put("gmtCreateStart", gmtCreateStart) ;
    	condition.put("gmtCreateEnd", gmtCreateEnd) ;
    	condition.put("payBatchno", payBatchno) ;
    	condition.put("settleId", settleId) ;
    	
    	return condition ;
    }

    public Long getSettleId() {
        return settleId;
    }

    public void setSettleId(Long settleId) {
        this.settleId = settleId;
    }

    public String getPayBatchno() {
        return payBatchno;
    }

    public void setPayBatchno(String payBatchno) {
        this.payBatchno = StringUtils.trim(payBatchno);
    }

    public String getSettleUid() {
        return settleUid;
    }

    public void setSettleUid(String settleUid) {
        this.settleUid = StringUtils.trim(settleUid);
    }

    public String getSettleAlipay() {
        return settleAlipay;
    }

    public void setSettleAlipay(String settleAlipay) {
        this.settleAlipay = StringUtils.trim(settleAlipay);
    }

    public Date getGmtPaidStart() {
        return gmtPaidStart;
    }

    public void setGmtPaidStart(Date gmtPaidStart) {
        this.gmtPaidStart = gmtPaidStart;
    }

    public Date getGmtPaidEnd() {
        return gmtPaidEnd;
    }

    public void setGmtPaidEnd(Date gmtPaidEnd) {
        this.gmtPaidEnd = gmtPaidEnd;
    }

    public String[] getSettleStatusList() {
        return settleStatusList;
    }

    public void setSettleStatusList(String[] settleStatusList) {
        this.settleStatusList = settleStatusList;
    }

    public String getSettleStatus() {
        return settleStatus;
    }

    public void setSettleStatus(String settleStatus) {
        this.settleStatus = StringUtils.trim(settleStatus);
    }

    public Date getGmtSettledStart() {
        return gmtSettledStart;
    }

    public void setGmtSettledStart(Date gmtSettledStart) {
        this.gmtSettledStart = gmtSettledStart;
    }

    public Date getGmtSettledEnd() {
        return gmtSettledEnd;
    }

    public void setGmtSettledEnd(Date gmtSettledEnd) {
        this.gmtSettledEnd = gmtSettledEnd;
    }

    public String getSettleTaobaoNick() {
        return settleTaobaoNick;
    }

    public void setSettleTaobaoNick(String settleTaobaoNick) {
        this.settleTaobaoNick = StringUtils.trim(settleTaobaoNick);
    }

	public Date getGmtCreateStart() {
		return gmtCreateStart;
	}

	public void setGmtCreateStart(Date gmtCreateStart) {
		this.gmtCreateStart = gmtCreateStart;
	}

	public Date getGmtCreateEnd() {
		return gmtCreateEnd;
	}

	public void setGmtCreateEnd(Date gmtCreateEnd) {
		this.gmtCreateEnd = gmtCreateEnd;
	}
    
}