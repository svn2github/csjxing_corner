package com.doucome.corner.web.zhe.model;

import com.doucome.corner.biz.dal.model.AbstractModel;

/**
 * 淘宝登陆后返回的top参数
 * @author langben 2012-3-30
 *
 */
public class TopCallbackParameter extends AbstractModel {

	private String top_appkey ;
	private String top_parameters ;
	private String top_session ;
	private String top_sign ;
	public String getTop_appkey() {
		return top_appkey;
	}
	public void setTop_appkey(String top_appkey) {
		this.top_appkey = top_appkey;
	}
	public String getTop_parameters() {
		return top_parameters;
	}
	public void setTop_parameters(String top_parameters) {
		this.top_parameters = top_parameters;
	}
	public String getTop_session() {
		return top_session;
	}
	public void setTop_session(String top_session) {
		this.top_session = top_session;
	}
	public String getTop_sign() {
		return top_sign;
	}
	public void setTop_sign(String top_sign) {
		this.top_sign = top_sign;
	}
	
	
}
