package com.yomahub.liteflow.flow.element.condition;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.yomahub.liteflow.enums.ConditionTypeEnum;
import com.yomahub.liteflow.exception.NoForNodeException;
import com.yomahub.liteflow.flow.element.Executable;
import com.yomahub.liteflow.flow.element.Node;
import com.yomahub.liteflow.slot.DataBus;
import com.yomahub.liteflow.slot.Slot;
import com.yomahub.liteflow.util.LiteFlowProxyUtil;

/**
 * 循环次数Condition
 *
 * @author Bryan.Zhang
 * @since 2.9.0
 */
public class ForCondition extends LoopCondition {

	@Override
	public void executeCondition(Integer slotIndex) throws Exception {
		Slot slot = DataBus.getSlot(slotIndex);
		Node forNode = this.getForNode();
		if (ObjectUtil.isNull(forNode)) {
			String errorInfo = StrUtil.format("[{}]:no for-node found", slot.getRequestId());
			throw new NoForNodeException(errorInfo);
		}

		// 先去判断isAccess方法，如果isAccess方法都返回false，整个FOR表达式不执行
		if (!this.getForNode().isAccess(slotIndex)) {
			return;
		}

		// 执行forCount组件
		forNode.setCurrChainId(this.getCurrChainId());
		forNode.execute(slotIndex);

		// 获得循环次数
		int forCount = forNode.getItemResultMetaValue(slotIndex);

		// 获得要循环的可执行对象
		Executable executableItem = this.getDoExecutor();

		// 获取Break节点
		Executable breakItem = this.getBreakItem();

		// 循环执行
		for (int i = 0; i < forCount; i++) {
			executableItem.setCurrChainId(this.getCurrChainId());
			// 设置循环index
			setLoopIndex(executableItem, i);
			executableItem.execute(slotIndex);
			// 如果break组件不为空，则去执行
			if (ObjectUtil.isNotNull(breakItem)) {
				breakItem.setCurrChainId(this.getCurrChainId());
				setLoopIndex(breakItem, i);
				breakItem.execute(slotIndex);
				boolean isBreak = breakItem.getItemResultMetaValue(slotIndex);
				if (isBreak) {
					break;
				}
			}
		}
	}

	@Override
	public ConditionTypeEnum getConditionType() {
		return ConditionTypeEnum.TYPE_FOR;
	}

	public Node getForNode() {
		return (Node) this.getExecutableOne(ConditionKey.FOR_KEY);
	}

	public void setForNode(Node forNode) {
		this.addExecutable(ConditionKey.FOR_KEY, forNode);
	}

}
