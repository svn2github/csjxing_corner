package com.doucome.corner.task.zhe.syncReport;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.doucome.corner.biz.core.constant.DecimalConstant;
import com.doucome.corner.biz.core.constant.LogConstant;
import com.doucome.corner.biz.core.enums.OutCodeEnums;
import com.doucome.corner.biz.core.enums.SettleStatusEnums;
import com.doucome.corner.biz.core.enums.TrueFalseEnums;
import com.doucome.corner.biz.core.model.page.Pagination;
import com.doucome.corner.biz.core.model.page.QueryResult;
import com.doucome.corner.biz.core.service.taobao.TaobaoServiceDecorator;
import com.doucome.corner.biz.core.service.taobao.TaobaokeServiceDecorator;
import com.doucome.corner.biz.core.taobao.constant.TaobaoItemConst;
import com.doucome.corner.biz.core.taobao.dto.TaobaoItemDTO;
import com.doucome.corner.biz.core.taobao.dto.TaobaokeReportMemberDTO;
import com.doucome.corner.biz.core.taobao.fields.TaobaokeFields;
import com.doucome.corner.biz.core.taobao.model.TaobaokeDate;
import com.doucome.corner.biz.core.utils.DecimalUtils;
import com.doucome.corner.biz.core.utils.OutCodeUtils;
import com.doucome.corner.biz.core.utils.OutCodeUtils.OutCode;
import com.doucome.corner.biz.dal.condition.TaokeReportSearchCondition;
import com.doucome.corner.biz.dal.dataobject.AlipayItemDO;
import com.doucome.corner.biz.dal.dataobject.DdzAccountDO;
import com.doucome.corner.biz.dal.dataobject.DdzSyncReportTaskDO;
import com.doucome.corner.biz.dal.dataobject.DdzTaokeReportDO;
import com.doucome.corner.biz.dal.dataobject.DdzTaokeReportSettleDO;
import com.doucome.corner.biz.dcome.utils.DcPromotionOutCodeUtils;
import com.doucome.corner.biz.dcome.utils.DcPromotionOutCodeUtils.OutCodeData;
import com.doucome.corner.biz.zhe.rule.DdzEatDiscountRule;
import com.doucome.corner.biz.zhe.rule.DdzEatDiscountRule.InternalCommission;
import com.doucome.corner.biz.zhe.service.DdzAccountService;
import com.doucome.corner.biz.zhe.service.DdzSyncReportTaskService;
import com.doucome.corner.biz.zhe.service.DdzTaokeReportService;
import com.doucome.corner.biz.zhe.service.DdzTaokeReportSettleService;
import com.doucome.corner.biz.zhe.utils.DdzJfbConvertUtils;
import com.doucome.corner.task.zhe.TaskService;
import com.doucome.corner.task.zhe.model.SyncReportCodeEnums;
import com.doucome.corner.task.zhe.model.SyncReportRunResult;
import com.doucome.corner.task.zhe.model.SyncReportRunResult.Page;
import com.doucome.corner.task.zhe.model.SyncReportRunResult.SettleReportResult;
import com.doucome.corner.task.zhe.syncReport.handler.Handler;
import com.doucome.corner.task.zhe.utils.TaskUtils;

/**
 * 同步淘宝客报表
 *
 */
public class SyncReportTaskService implements TaskService {

	private static final Log syncReportLog = LogFactory.getLog(LogConstant.task_syncReport_log) ;
	
	private static final Log log = LogFactory.getLog(SyncReportTaskService.class) ;
	
	private List<Handler> handlerList = new ArrayList<Handler>() ;
	
	@Autowired
	private DdzTaokeReportService ddzTaokeReportService ;
	
	@Autowired
	private TaobaokeServiceDecorator taobaokeServiceDecorator ;
	
	@Autowired
	private TaobaoServiceDecorator taobaoServiceDecorator ;
	 
	@Autowired
	private DdzSyncReportTaskService ddzSyncReportTaskService ;
	
	@Autowired
	private DdzAccountService ddzAccountService ;
		
	@Autowired
	private DdzEatDiscountRule ddzEatDiscountRule ;
	
	@Autowired
	private DdzTaokeReportSettleService ddzTaokeReportSettleService;
	
	/**
	 * task interval
	 */
	@Override
	public String executeInternal(){
		
		/**
		 * 跑前2天的数据
		 */
		Date date = TaskUtils.getToSyncDate() ;
		
		TaobaokeDate taobaokeDate = new TaobaokeDate(date) ; 
		
		return syncDailyReport(taobaokeDate) ;
	}
	
	@Transactional(rollbackFor=Throwable.class)
	public String syncDailyReport(TaobaokeDate date){
		
		SyncReportRunResult returnObject = new SyncReportRunResult() ; //return Value 
		
		String taskId = date.getDateFormat() ;
		
		DdzSyncReportTaskDO ddzSyncReportTaskDO = ddzSyncReportTaskService.getByTaskId(taskId) ;

		//任务已经执行过
		if(ddzSyncReportTaskDO != null){
			syncReportLog.info("taskId : " + taskId + " has already runed , skip .") ;
			returnObject.setCode(SyncReportCodeEnums.ASSIGN_ERROR) ;
			returnObject.setDetailMsg("taskId [" + taskId + "] has been runed , skip .") ;
			return returnObject.toString() ;
		}
		
		DdzSyncReportTaskDO reportTask = new DdzSyncReportTaskDO() ;
		reportTask.setTaskId(taskId) ;
				
		try{
			//分配任务
			int createId = ddzSyncReportTaskService.createReportTask(reportTask) ; //分配taskId
			
		}catch(Exception e) {
			log.error(e.getMessage() , e) ;
			syncReportLog.error("error create report task record . " + e.getMessage()) ;
			returnObject.setCode(SyncReportCodeEnums.ASSIGN_ERROR) ;
			returnObject.setDetailMsg("create task record error , msg : " + e.getMessage()) ;
			return returnObject.toString();
		}
		
		//从淘宝API获取报表
		List<TaobaokeReportMemberDTO> taobaoReportList = getAllReport(date, returnObject) ;
		if(!CollectionUtils.isEmpty(taobaoReportList)){
			
			String batchformat = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) ;
			//同步报表到数据库
			int successCount = syncEveryReport(taobaoReportList , batchformat) ;
			returnObject.setSuccessCount(successCount) ;
			
			///一次从数据库取出所有的刚生成的数据，交给不同的handler处理
			TaokeReportSearchCondition searchCondition = new TaokeReportSearchCondition() ;
			searchCondition.setInsertBatch(batchformat) ;
			List<DdzTaokeReportDO> handleReportList = ddzTaokeReportService.getReports(searchCondition) ;
			
			//处理点点折报表结算
			//处理豆蔻活动增积分
			if(!CollectionUtils.isEmpty(handlerList)){
				for(Handler handler : handlerList){
					try {
						handler.handleReport(handleReportList) ;
					}catch(Exception e){
						log.error(e.getMessage() , e) ;
					}
				}
			}
			
			//生成结算
			//SettleReportResult settleReportResult = createSettleReport();
			//returnObject.setSettleReportResult(settleReportResult);
		}
		
		if(!CollectionUtils.isEmpty(returnObject.getFailPages())){
			returnObject.setCode(SyncReportCodeEnums.PARTIAL_SUCCESS) ;
		}
		
		reportTask.setIsSuccess(returnObject.isSuccess() ? TrueFalseEnums.TRUE.getValue() : TrueFalseEnums.FALSE.getValue()) ;
		reportTask.setReportMembCount(returnObject.getTotalCount()) ;
		reportTask.setSuccessCount(returnObject.getSuccessCount()) ;
		reportTask.setRunoutData(returnObject.toString()) ;
		reportTask.setGmtReport(date.getDate()) ;
		try {
			ddzSyncReportTaskService.updateByTaskId(reportTask) ;
		}catch(Exception e){
			log.error(e.getMessage() , e) ;
			returnObject.setCode(SyncReportCodeEnums.UPDATE_TASK_ERROR) ;
			returnObject.setDetailMsg("update task record error , msg : "  + e.getMessage()) ;
		}
		return returnObject.toString() ;
	}
		
	/**
	 * 从阿里妈妈获取当天所有的报表
	 * @param date
	 * @param runout
	 * @return
	 */
	private List<TaobaokeReportMemberDTO> getAllReport(TaobaokeDate date , SyncReportRunResult runout){
				
		List<TaobaokeReportMemberDTO> reportList = new ArrayList<TaobaokeReportMemberDTO>() ;
		List<Page> failPageList = new ArrayList<Page>() ;
		QueryResult<TaobaokeReportMemberDTO> result = null ;
		Integer totalPages = null ;
		Long totalRecords = null ;
		int page = 1  ;
		while(true){
			Pagination pagination = new Pagination(page, 20) ;
			try {
				
				result = taobaokeServiceDecorator.getReport(date, TaobaokeFields.taoke_report_memb_fields , pagination) ;
				
				List<TaobaokeReportMemberDTO> subReportList = result.getItems() ;
				if(!CollectionUtils.isEmpty(subReportList)){
					String[] numIids = parseNumiids(subReportList) ;
					//批量获取宝贝图片
					List<TaobaoItemDTO> itemList = null ;
					try {
						itemList = taobaoServiceDecorator.getListItems(numIids, new String[]{TaobaoItemConst.pic_url,TaobaoItemConst.num_iid}) ;
					}catch(Exception e){
						log.error(e.getMessage() , e) ;
					}
					//matchPicUrl(itemList , subReportList) ;
					reportList.addAll(subReportList) ;
				}
				
			}catch(Exception e){
				log.error(e.getMessage() , e) ;
				syncReportLog.error("error get report . " + e.getMessage()) ;
				failPageList.add(new Page(pagination.getPage(), pagination.getStart(), pagination.getSize())) ; //添加失败记录
			}
						
			if(totalPages == null){
				if(result == null){ //取记录出错
					break ;
				}
				totalPages = pagination.getTotalPages() ;
				totalRecords = result.getTotalRecords() ;
			}
				
			
			page += 1  ; // 下一页
			try {
				Thread.sleep(300) ;
			} catch (InterruptedException e) {
				
			}
			if( page > totalPages ){
				break ;
			}		
		}
		
		//runout.setSuccessCount(successCount) ;
		
		if(totalRecords != null){
			runout.setTotalCount(Integer.valueOf(String.valueOf(totalRecords))) ;
		}
		if(!CollectionUtils.isEmpty(failPageList)){
			runout.setFailPages(failPageList) ;
		}
		return reportList ;
		
	}
	
	private String[] parseNumiids(List<TaobaokeReportMemberDTO> reportList){
		String[] numIids = new String[reportList.size()] ;
		for(int i=0 ;i<reportList.size();i++){
			numIids[i] = String.valueOf(reportList.get(i).getNumIid()) ;
		}
		return numIids ;
	}
	
//	private void matchPicUrl(List<TaobaoItemDTO> itemList , List<TaobaokeReportMemberDTO> toReportList){
//		if(CollectionUtils.isEmpty(itemList) || CollectionUtils.isEmpty(toReportList)){
//			return ;
//		}
//		if(itemList.size() != toReportList.size()){
//			//挨个比对
//			for(TaobaoItemDTO item : itemList){
//				Long numIid = item.getNumIid() ;
//				for(TaobaokeReportMemberDTO report : toReportList){
//					if(numIid.equals(report.getNumIid())){
//						report.setPicUrl(item.getPicUrl()) ;
//						break ;
//					}
//				}
//			}
//			return ;
//		}
//		for(int i=0;i<toReportList.size() ;i++){
//			TaobaokeReportMemberDTO report = toReportList.get(i) ;
//			TaobaoItemDTO item = itemList.get(i) ;
//			
//			if(report.getNumIid().equals(item.getNumIid())){
//				report.setPicUrl(item.getPicUrl()) ;
//			}
// 		}
//	}
	
	/**
	 * 同步每一个报表
	 * @param itemList
	 * @return successCount 
	 */
	private int syncEveryReport(List<TaobaokeReportMemberDTO> itemList  , String insertBatch){
		if(CollectionUtils.isEmpty(itemList)) {
			return 0 ;
		}
		int successCount = 0 ;
		for(TaobaokeReportMemberDTO item : itemList){
			
			if(item == null){
				continue ;
			}
			DdzTaokeReportDO report = new DdzTaokeReportDO() ;
			report.setCategoryId(item.getCategoryId()) ;
			report.setCommission(item.getCommission()) ;
			report.setCommissionRate(item.getCommissionRate()) ;
			report.setGmtPaid(item.getPayTime()) ;
			report.setSettleStatus(SettleStatusEnums.UNSETTLE.toString());
			report.setItemNum(item.getItemNum()) ;
			report.setItemTitle(item.getItemTitle()) ;
			report.setNumIid(item.getNumIid()) ;
			report.setOutCode(item.getOuterCode()) ;
			report.setPayPrice(item.getPayPrice()) ;
			report.setRealPayFee(item.getRealPayFee()) ;
			report.setSellerNick(item.getSellerNick()) ;
			report.setTradeId(item.getTradeId()) ;
			//report.setPicUrl(item.getPicUrl()) ;
			String outCode = report.getOutCode() ;
			OutCode o = OutCodeUtils.decodeOutCode(outCode) ;
			String outcodeType = o.getType().getName() ;
			report.setOutcodeType(outcodeType) ;
			report.setInsertBatch(insertBatch) ;
			
			try {
				
				BigDecimal newCommissionRate = DecimalUtils.multiply(item.getCommissionRate(),DecimalConstant.HUNDRED) ;
				InternalCommission userCommission = DdzEatDiscountRule.calcUserCommissions(ddzEatDiscountRule, item.getCommission() , newCommissionRate , item.getRealPayFee()) ;
				report.setUserCommission(userCommission.getCommission()) ;
				report.setUserCommissionRate(DecimalUtils.divide(userCommission.getCommissionRate(),DecimalConstant.HUNDRED)) ;
				//计算结算金额
				report.setSettleFee(report.getUserCommission());
				//计算集分宝
				report.setSettleJfb(DdzJfbConvertUtils.convertMoney2Jfb(report.getSettleFee())) ; 
				report.setUserJfbRate(DdzJfbConvertUtils.convertJfbCommissionRate(report.getSettleJfb() , report.getRealPayFee())) ;
				
				if(o.getType() == OutCodeEnums.DDZ_ACCOUNT_ID || o.getType() == OutCodeEnums.DDZ_ACCOUNT_ID_MANUAL || o.getType() == OutCodeEnums.DDZ_ACCOUNT_ID_JFB){
					String accountId = o.getOutCode() ;
					DdzAccountDO acc = ddzAccountService.queryAccountByAccountId(accountId) ;
					if(acc != null){ //账号存在
						report.setSettleAlipay(acc.getAlipayId()) ;
					} else {
						syncReportLog.error("cant find account from outCode : " + outCode) ;
					}
				} else if(o.getType() == OutCodeEnums.DOUCOME_PROMOTION){
					try {
						OutCodeData data = DcPromotionOutCodeUtils.decodeOutCode(o.getOutCode()) ;
						if(data != null){
							report.setDcItemId(data.getItemId()) ;
							report.setDcUserId(data.getUserId()) ;
						}
					} catch (Exception e){
						log.error(e.getMessage() , e) ;
					}
				} else if(o.getType() == OutCodeEnums.DOUCOME_USER_ID) {
					try {
						String userId = o.getOutCode() ;
						if(StringUtils.isNotBlank(userId) && StringUtils.isNumeric(userId)) {
							report.setDcUserId(Long.valueOf(userId)) ;
						}
					} catch (Exception e){
						log.error(e.getMessage() , e) ;
					}
				}
				
			} catch(Exception e){
				log.error(e.getMessage() , e) ;
				
			}
			try {
				ddzTaokeReportService.createReport(report) ;
			} catch(Exception e){
				log.error(e.getMessage() , e) ;
				syncReportLog.error(e.getMessage() , e) ;
			}
			successCount ++ ;
			
		}
		return successCount ;
	}
	
	/**
	 * 生成taoke报表结算记录
	 * @return .
	 */
	public SettleReportResult createSettleReport() {
		SettleReportResult result = new SettleReportResult();
		result.setTotalCount(0);
		result.setSuccCount(0);
//		while(true) {
		Pagination pagination = new Pagination(1, 10000);
		List<AlipayItemDO> payItemDOs = ddzTaokeReportService.getUnMergedReportSettleInfo(pagination);
		if (payItemDOs == null) {
			syncReportLog.error("----fetch the taoke report failed.");
			result.setTotalCount(-1);
			result.setSuccCount(-1);
			return result;
		}
		if (payItemDOs.size() == 0) {
		    return result;
		}
		result.setTotalCount(payItemDOs.size());
		int succCount = 0;
		for (AlipayItemDO payItemDO: payItemDOs) {
			syncReportLog.error("----create settle report: " + payItemDO.toString());
			Long settleId = -1L;
			try {
				settleId = insertSettleReport(payItemDO);
			} catch (Exception e) {
				syncReportLog.error(e);
				continue;
			}
			syncReportLog.error(String.format("----set settle report [%d] report [%s] settle_id succ",
					              settleId, payItemDO.getIds()));
			succCount++; 
		}
//		}
		result.setSuccCount(succCount);
		syncReportLog.error(String.format("succ create [%s] settle report", result.toString()));
		return result;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW,
			isolation = Isolation.DEFAULT,
			rollbackFor = Exception.class)
	public Long insertSettleReport(AlipayItemDO payItemDO) throws Exception {
		List<Long> reportIds = convert(payItemDO.getIds());
		if (reportIds == null) {
			syncReportLog.error("----unexcepted report id: " + payItemDO.getIds());
			return -1L;
		}
		DdzTaokeReportSettleDO settleDO = new DdzTaokeReportSettleDO();
		settleDO.setSettleAlipay(payItemDO.getAccount());
		settleDO.setSettleFee(payItemDO.getAmount());
		settleDO.setSettleStatus(SettleStatusEnums.UNSETTLE.getValue());
		Long settleId = ddzTaokeReportSettleService.insertSettleReport(settleDO);
		if (settleId == -1) {
			syncReportLog.error("----insert merged taoke settle report failed: " + payItemDO);
			return settleId;
		}
		
		int count = ddzTaokeReportService.updateTaokeReportSettleId(reportIds, settleId);
		if (count == -1) {
			String temp = String.format("----set report [%s] settle_id failed, delete related settle report[%d]",
		                    payItemDO.getIds(), settleId);
			syncReportLog.error(temp);
			//rollback transaction
			throw new Exception(temp);
		}
		if (count != reportIds.size()) {
			String temp = String.format("----set report [%s] settle_id, but actual update [%d] report." +
		              " reset the report settle_id to null", payItemDO.getIds(), count);
			syncReportLog.error(temp);
			//rollback transaction
			throw new Exception(temp);
		}
		return settleId;
	}
	
	private List<Long> convert(String reportIds) {
		List<Long> result = new ArrayList<Long>();
		if (StringUtils.isEmpty(reportIds)) {
			return result;
		}
		String[] temps = reportIds.split(",");
		for (String temp: temps) {
			try {
				result.add(Long.valueOf(temp.trim()));
			} catch (Exception e) {
				log.error(e);
				return null;
			}
		}
		return result;
	}

	public void setHandlerList(List<Handler> handlerList) {
		this.handlerList = handlerList;
	}
	
	
}
