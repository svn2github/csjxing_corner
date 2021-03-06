package com.doucome.corner.biz.core.service.taobao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.doucome.corner.biz.common.utils.ReflectUtils;
import com.doucome.corner.biz.core.constant.LogConstant;
import com.doucome.corner.biz.core.exception.TaobaoRemoteException;
import com.doucome.corner.biz.core.model.page.Pagination;
import com.doucome.corner.biz.core.model.page.QueryResult;
import com.doucome.corner.biz.core.taobao.dto.TaobaokeItemDTO;
import com.doucome.corner.biz.core.taobao.dto.TaobaokeReportMemberDTO;
import com.doucome.corner.biz.core.taobao.dto.TaobaokeShopDTO;
import com.doucome.corner.biz.core.taobao.model.TaobaokeDate;
import com.doucome.corner.biz.core.taobao.model.TaokeCaturlCondition;
import com.doucome.corner.biz.core.taobao.model.TaokeItemSearchCondition;
import com.doucome.corner.biz.core.utils.ArrayStringUtils;
import com.taobao.api.ApiException;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.TaobaokeItem;
import com.taobao.api.domain.TaobaokeReport;
import com.taobao.api.domain.TaobaokeReportMember;
import com.taobao.api.domain.TaobaokeShop;
import com.taobao.api.request.TaobaokeCaturlGetRequest;
import com.taobao.api.request.TaobaokeItemsConvertRequest;
import com.taobao.api.request.TaobaokeItemsGetRequest;
import com.taobao.api.request.TaobaokeListurlGetRequest;
import com.taobao.api.request.TaobaokeReportGetRequest;
import com.taobao.api.request.TaobaokeShopsConvertRequest;
import com.taobao.api.request.TaobaokeWidgetItemsConvertRequest;
import com.taobao.api.response.TaobaokeCaturlGetResponse;
import com.taobao.api.response.TaobaokeItemsConvertResponse;
import com.taobao.api.response.TaobaokeItemsGetResponse;
import com.taobao.api.response.TaobaokeListurlGetResponse;
import com.taobao.api.response.TaobaokeReportGetResponse;
import com.taobao.api.response.TaobaokeShopsConvertResponse;
import com.taobao.api.response.TaobaokeWidgetItemsConvertResponse;

/**
 * 淘宝开放平台接口封装
 * @author langben 2012-2-24
 *
 */
public class TaobaokeServiceDecoratorImpl extends AbstractTaobaoService implements TaobaokeServiceDecorator  {
	
	private static final Log taobaoLog = LogFactory.getLog(LogConstant.taobao_log) ;
	
	@Override
	public List<TaobaokeShopDTO> conventShops(String[] shorpIds , String outCode , String[] fields) throws TaobaoRemoteException  {
		
		TaobaokeShopsConvertRequest req = new TaobaokeShopsConvertRequest();
		req.setFields(ArrayStringUtils.toString(fields)) ;
		req.setSids(ArrayStringUtils.toString(shorpIds));
		req.setNick(nickname);
		req.setOuterCode(outCode) ;
		//req.setPid(taokeId) ;
		
		try{
			TaobaoClient taobaoClient = taobaoClientWrapper.newClient() ;
			TaobaokeShopsConvertResponse response = taobaoClient.execute(req);
			boolean isSuccess = response.isSuccess() ;
			if(isSuccess){
				List<TaobaokeShopDTO> itemDTOs = new ArrayList<TaobaokeShopDTO>() ;
				List<TaobaokeShop> items =  response.getTaobaokeShops() ;
				if(!CollectionUtils.isEmpty(items)){
					for(TaobaokeShop item : items){
						TaobaokeShopDTO dto = new TaobaokeShopDTO(item) ;
						itemDTOs.add(dto) ;
					}
				}
				return itemDTOs ;
			}
			if(taobaoLog.isErrorEnabled()){
				taobaoLog.error("input [" + ArrayStringUtils.toString(shorpIds) + "|" + outCode +  "]  response : " + response.getBody()) ;
			} 
			throwTaobaoErrorResponse(response) ;
			return null ;
		}catch(ApiException e){
			throw new TaobaoRemoteException(e.getErrMsg() , e , e.getErrCode()) ;
		}
	}

	@Override
	public List<TaobaokeItemDTO> conventItems(String[] itemIds , String outCode ,  String[] fields) throws TaobaoRemoteException  {
		

		TaobaokeItemsConvertRequest req = new TaobaokeItemsConvertRequest();
		req.setFields(ArrayStringUtils.toString(fields)) ;
		req.setNick(nickname);
		if(StringUtils.isNotBlank(outCode)){
			req.setOuterCode(outCode);
		}
		req.setNumIids(ArrayStringUtils.toString(itemIds));
		//req.setPid(ni);
		//req.setIsMobile(true);
		try {
			TaobaoClient taobaoClient = taobaoClientWrapper.newClient() ;
			TaobaokeItemsConvertResponse response = taobaoClient.execute(req);
			boolean isSuccess = response.isSuccess() ;
			if(!isSuccess){
								
				if(taobaoLog.isErrorEnabled()){
					taobaoLog.error("input [" + ArrayStringUtils.toString(itemIds) + "|" + outCode +  "]  response : " + response.getBody()) ;
				} 
				throwTaobaoErrorResponse(response) ;
				return null ;
			}
						
			List<TaobaokeItemDTO> itemDTOs = new ArrayList<TaobaokeItemDTO>() ;
			List<TaobaokeItem> items =  response.getTaobaokeItems() ;
			if(!CollectionUtils.isEmpty(items)){
				for(TaobaokeItem item : items){
					TaobaokeItemDTO dto = new TaobaokeItemDTO(item) ;
					itemDTOs.add(dto) ;
				}
			}
			return itemDTOs ;
		} catch (ApiException e) {
			throw new TaobaoRemoteException(e.getErrMsg() , e , e.getErrCode()) ;
		}
		
	}

	@Override
	public TaobaokeItemDTO conventItem(String itemId,  String outCode , String[] fields) throws TaobaoRemoteException  {
		List<TaobaokeItemDTO> list = conventItems(new String[]{itemId}, outCode , fields) ;
		if(CollectionUtils.isEmpty(list)){
			return null ;
		}
		return list.iterator().next() ;
	}
	
	@Override
	public List<TaobaokeItemDTO> widgetConventItems(String[] itemIds,
			String outCode, Boolean isMobile, String[] fields)
			throws TaobaoRemoteException {
		TaobaokeWidgetItemsConvertRequest req = new TaobaokeWidgetItemsConvertRequest() ;
		req.setFields(ArrayStringUtils.toString(fields)) ;
		req.setOuterCode(outCode) ;
		if(isMobile != null){
			req.setIsMobile(isMobile) ;
		}
		if(StringUtils.isNotBlank(outCode)){
			req.setOuterCode(outCode);
		}
		req.setNumIids(ArrayStringUtils.toString(itemIds));
		try {
			TaobaoClient taobaoClient = taobaoClientWrapper.newWidgetClient() ;
			TaobaokeWidgetItemsConvertResponse response = taobaoClient.execute(req);
			boolean isSuccess = response.isSuccess() ;
			if(!isSuccess){
								
				if(taobaoLog.isErrorEnabled()){
					taobaoLog.error("input [" + ArrayStringUtils.toString(itemIds) + "|" + outCode +  "]  response : " + response.getBody()) ;
				} 
				throwTaobaoErrorResponse(response) ;
				return null ;
			}
			
			List<TaobaokeItemDTO> itemDTOs = new ArrayList<TaobaokeItemDTO>() ;
			List<TaobaokeItem> items =  response.getTaobaokeItems() ;
			if(!CollectionUtils.isEmpty(items)){
				for(TaobaokeItem item : items){
					TaobaokeItemDTO dto = new TaobaokeItemDTO(item) ;
					itemDTOs.add(dto) ;
				}
			} else {
				taobaoLog.error("response is empty, response body: " + response.getBody());
			}
			
			return itemDTOs ;
			
		} catch (ApiException e) {
			throw new TaobaoRemoteException(e.getErrMsg() , e , e.getErrCode()) ;
		}
	}
	
	@Override
	public List<TaobaokeItemDTO> widgetConventItems(String[] itemIds, String outCode,String[] fields) throws TaobaoRemoteException {
		return this.widgetConventItems(itemIds, outCode, null, fields) ;
	}
	
	@Override
	public TaobaokeItemDTO widgetConventItem(String itemId ,  String outCode , String[] fields) throws TaobaoRemoteException {
		return this.widgetConventItem(itemId, outCode, null, fields) ;
	}
	
	@Override
	public TaobaokeItemDTO widgetConventItem(String itemId, String outCode,Boolean isMobile, String[] fields) throws TaobaoRemoteException {
		List<TaobaokeItemDTO> list = widgetConventItems(new String[]{itemId}, outCode , isMobile , fields) ;
		if(CollectionUtils.isEmpty(list)){
			return null ;
		}
		return list.iterator().next() ;
	}

	


	@Override
	public TaobaokeShopDTO conventShop(String shopId, String outCode, String[] fields) throws TaobaoRemoteException {
		List<TaobaokeShopDTO> list = conventShops(new String[]{shopId}, outCode , fields) ;
		if(CollectionUtils.isEmpty(list)){
			return null ;
		}
		return list.iterator().next() ;
	}

	@Override
	public QueryResult<TaobaokeReportMemberDTO> getReport(TaobaokeDate date, String[] fields ,Pagination pagination) {
		TaobaokeReportGetRequest req = new TaobaokeReportGetRequest();
		req.setFields(ArrayStringUtils.toString(fields));
		req.setDate(date.getDateFormat());
		req.setPageNo(Long.valueOf(pagination.getPage()));
		req.setPageSize(Long.valueOf(pagination.getSize()));
		
		TaobaoClient taobaoClient = taobaoClientWrapper.newDefaultClient() ;
		try{
			TaobaokeReportGetResponse response = taobaoClient.execute(req , taobaoClientWrapper.defaultSession());
			boolean isSuccess = response.isSuccess() ;
			if(isSuccess){
				
				TaobaokeReport report = response.getTaobaokeReport() ;
				List<TaobaokeReportMember> items = report.getTaobaokeReportMembers() ;
				List<TaobaokeReportMemberDTO> itemDTOs = new ArrayList<TaobaokeReportMemberDTO>() ;
				long totalResults = report.getTotalResults() ;
				if(!CollectionUtils.isEmpty(items)){
					for(TaobaokeReportMember item : items){
						TaobaokeReportMemberDTO dto = new TaobaokeReportMemberDTO(item) ;
						itemDTOs.add(dto) ;
					}
				}
				return new QueryResult<TaobaokeReportMemberDTO>(itemDTOs , pagination , totalResults) ;
			}
			if(taobaoLog.isErrorEnabled()){
				taobaoLog.error("input [" + date.getDateFormat() + "] response : " + response.getBody()) ;
			} 
			throwTaobaoErrorResponse(response) ;
			return null ;
		}catch(ApiException e){
			throw new TaobaoRemoteException(e.getErrMsg() , e , e.getErrCode()) ;
		}
	}

	@Override
	public QueryResult<TaobaokeItemDTO> getItems(TaokeItemSearchCondition condition , String[] fields, Pagination pagination) throws TaobaoRemoteException {

		TaobaokeItemsGetRequest req = new TaobaokeItemsGetRequest();
		ReflectUtils.reflectTo(condition , req) ;
		req.setFields(ArrayStringUtils.toString(fields)) ;
		req.setNick(nickname);
		req.setPageNo(Long.valueOf(pagination.getPage()));
		req.setPageSize(Long.valueOf(pagination.getSize()));
		TaobaoClient taobaoClient = taobaoClientWrapper.newClient() ;
		try {
			TaobaokeItemsGetResponse response = taobaoClient.execute(req) ;
			boolean isSuccess = response.isSuccess() ;
			if(isSuccess){
				List<TaobaokeItem> items = response.getTaobaokeItems() ;
				
				List<TaobaokeItemDTO> itemDTOs = new ArrayList<TaobaokeItemDTO>() ;
				Long totalResults = response.getTotalResults() ;
				if(totalResults == null){
					totalResults = 0L ;
				}
				if(!CollectionUtils.isEmpty(items)){
					for(TaobaokeItem item : items){
						TaobaokeItemDTO dto = new TaobaokeItemDTO(item) ;
						dto.setTitleHighlight(dto.getTitle()) ;
						String title = StringUtils.replace(dto.getTitle(), "<span class=H>", "") ;
						title = StringUtils.replace(title, "</span>", "") ;
						dto.setTitle(title) ;
						itemDTOs.add(dto) ;
					}
				}
				return new QueryResult<TaobaokeItemDTO>(itemDTOs , pagination ,totalResults) ;
			}
			if(taobaoLog.isErrorEnabled()){
				taobaoLog.error("input [" + condition + "] response : " + response.getBody()) ;
			} 
			throwTaobaoErrorResponse(response) ;
			return null ;
		} catch (ApiException e) {
			throw new TaobaoRemoteException(e.getErrMsg() , e , e.getErrCode()) ;
		}
	
	}

	@Override
	public String getCaturl(TaokeCaturlCondition condition) {
		TaobaokeCaturlGetRequest req = new TaobaokeCaturlGetRequest();
		ReflectUtils.reflectTo(condition, req) ;
		req.setNick(nickname);
		TaobaoClient taobaoClient = taobaoClientWrapper.newClient() ;
		try {
			TaobaokeCaturlGetResponse response = taobaoClient.execute(req);
			
			boolean isSuccess = response.isSuccess() ;
			if(isSuccess){
				TaobaokeItem item = response.getTaobaokeItem() ;
				if(item == null){
					return null ;
				}
				return item.getTaobaokeCatClickUrl() ;
			}
			if(taobaoLog.isErrorEnabled()){
				taobaoLog.error("input [" + condition + "] response : " + response.getBody()) ;
			} 
			throwTaobaoErrorResponse(response) ;
			return null ;
			
		} catch (ApiException e) {
			throw new TaobaoRemoteException(e.getErrMsg() , e , e.getErrCode()) ;
		}
	}

	@Override
	public String getListurl(String keyword, String outCode) {
		
		TaobaokeListurlGetRequest req = new TaobaokeListurlGetRequest();
		req.setNick(nickname) ;
		TaobaoClient taobaoClient = taobaoClientWrapper.newClient() ;
		req.setQ(keyword);
		req.setNick(nickname);
		if(StringUtils.isNotBlank(outCode)){
			req.setOuterCode(outCode);
		}
		
		try {
			TaobaokeListurlGetResponse response = taobaoClient.execute(req);
			
			boolean isSuccess = response.isSuccess() ;
			if(isSuccess){
				TaobaokeItem item = response.getTaobaokeItem() ;
				if(item == null){
					return null ;
				}
				return item.getKeywordClickUrl() ;
			}
			if(taobaoLog.isErrorEnabled()){
				taobaoLog.error("input [" + keyword + "] response : " + response.getBody()) ;
			} 
			throwTaobaoErrorResponse(response) ;
			return null ;
			
		} catch (ApiException e) {
			throw new TaobaoRemoteException(e.getErrMsg() , e , e.getErrCode()) ;
		}
		
	}

	
	
	
	
	
}
