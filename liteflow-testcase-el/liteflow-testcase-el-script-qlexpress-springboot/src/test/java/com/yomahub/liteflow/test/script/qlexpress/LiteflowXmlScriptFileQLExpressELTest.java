package com.yomahub.liteflow.test.script.qlexpress;

import cn.hutool.core.io.resource.ResourceUtil;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import com.yomahub.liteflow.enums.FlowParserTypeEnum;
import com.yomahub.liteflow.flow.FlowBus;
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

/**
 * 测试springboot下的脚本组件
 *
 * @author Bryan.Zhang
 * @since 2.6.0
 */
@RunWith(SpringRunner.class)
@TestPropertySource(value = "classpath:/xml-script-file/application.properties")
@SpringBootTest(classes = LiteflowXmlScriptFileQLExpressELTest.class)
@EnableAutoConfiguration
@ComponentScan({ "com.yomahub.liteflow.test.script.qlexpress.cmp" })
public class LiteflowXmlScriptFileQLExpressELTest extends BaseTest {

	@Resource
	private FlowExecutor flowExecutor;

	// 测试普通脚本节点
	@Test
	public void testScript1() {
		LiteflowResponse response = flowExecutor.execute2Resp("chain1", "arg");
		DefaultContext context = response.getFirstContextBean();
		Assert.assertTrue(response.isSuccess());
		Assert.assertEquals(Integer.valueOf(6), context.getData("s1"));
	}

	// 测试条件脚本节点
	@Test
	public void testScript2() {
		LiteflowResponse response = flowExecutor.execute2Resp("chain2", "arg");
		Assert.assertTrue(response.isSuccess());
		Assert.assertEquals("d==>s2[条件脚本]==>b", response.getExecuteStepStr());
	}

	@Test
	public void testScript3() throws Exception {
		// 根据配置，加载的应该是flow.xml，执行原来的规则
		LiteflowResponse responseOld = flowExecutor.execute2Resp("chain2", "arg");
		Assert.assertTrue(responseOld.isSuccess());
		Assert.assertEquals("d==>s2[条件脚本]==>b", responseOld.getExecuteStepStr());
		// 更改规则，重新加载，更改的规则内容从flow_update.xml里读取，这里只是为了模拟下获取新的内容。不一定是从文件中读取
		String newContent = ResourceUtil.readUtf8Str("classpath: /xml-script-file/flow_update.el.xml");
		// 进行刷新
		FlowBus.refreshFlowMetaData(FlowParserTypeEnum.TYPE_EL_XML, newContent);

		// 重新执行chain2这个链路，结果会变
		LiteflowResponse responseNew = flowExecutor.execute2Resp("chain2", "arg");
		Assert.assertTrue(responseNew.isSuccess());
		Assert.assertEquals("d==>s2[条件脚本_改]==>a==>s3[普通脚本_新增]", responseNew.getExecuteStepStr());
	}

	// 测试脚本&规则平滑重载刷新
	@Test
	public void testScript4() throws Exception {
		new Thread(() -> {
			try {
				Thread.sleep(2000L);
				// 更改规则，重新加载，更改的规则内容从flow_update.xml里读取，这里只是为了模拟下获取新的内容。不一定是从文件中读取
				String newContent = ResourceUtil.readUtf8Str("classpath: /xml-script-file/flow_update.el.xml");
				// 进行刷新
				FlowBus.refreshFlowMetaData(FlowParserTypeEnum.TYPE_EL_XML, newContent);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}).start();

		for (int i = 0; i < 300; i++) {
			LiteflowResponse responseNew = flowExecutor.execute2Resp("chain2", "arg");
			Assert.assertTrue(responseNew.isSuccess());
			Thread.sleep(10L);
		}
	}

}
