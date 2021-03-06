package com.doucome.corner.biz.zhe.service.impl.pay;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.doucome.corner.biz.dal.dataobject.AlipayItemDO;
import com.doucome.corner.biz.zhe.service.DdzTaokeReportSettleService;

@ContextConfiguration(locations = {"classpath:biz-zhe-test.xml"})
public class TaokeReporSettleServiceTest extends AbstractJUnit4SpringContextTests {
	@Autowired
	private DdzTaokeReportSettleService taokeReportSettleService;
	
	@Test
	public void testGetAlipayItems() {
		List<AlipayItemDO> payItems = taokeReportSettleService.getAlipayItems(20);
		System.out.println(payItems.size());
	}
}
