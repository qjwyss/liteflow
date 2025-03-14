package com.yomahub.liteflow.test.subflow;

import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import com.yomahub.liteflow.slot.DefaultContext;
import com.yomahub.liteflow.test.BaseTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

/**
 * 测试隐式调用子流程 单元测试
 *
 * @author justin.xu
 */
@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:/subflow/application-implicit.properties")
@SpringBootTest(classes = ImplicitSubFlowELDeclMultiSpringbootTest.class)
@EnableAutoConfiguration
@ComponentScan({ "com.yomahub.liteflow.test.subflow.cmp2" })
public class ImplicitSubFlowELDeclMultiSpringbootTest extends BaseTest {

	@Resource
	private FlowExecutor flowExecutor;

	public static final Set<String> RUN_TIME_SLOT = new HashSet<>();

	// 这里GCmp中隐式的调用chain4，从而执行了h，m
	@Test
	public void testImplicitSubFlow1() {
		LiteflowResponse response = flowExecutor.execute2Resp("chain3", "it's a request");
		DefaultContext context = response.getFirstContextBean();
		Assert.assertTrue(response.isSuccess());
		Assert.assertEquals("f==>g==>h==>m", response.getExecuteStepStr());

		// 传递了slotIndex，则set的size==1
		Assert.assertEquals(1, RUN_TIME_SLOT.size());
		// set中第一次设置的requestId和response中的requestId一致
		Assert.assertTrue(RUN_TIME_SLOT.contains(response.getSlot().getRequestId()));
		// requestData的取值正确
		Assert.assertEquals("it's implicit subflow.", context.getData("innerRequest"));
	}

	// 在p里多线程调用q 10次，每个q取到的参数都是不同的。
	@Test
	public void testImplicitSubFlow2() {
		LiteflowResponse response = flowExecutor.execute2Resp("c1", "it's a request");
		DefaultContext context = response.getFirstContextBean();
		Assert.assertTrue(response.isSuccess());

		Set<String> set = context.getData("test");

		// requestData的取值正确
		Assert.assertEquals(10, set.size());
	}

}
