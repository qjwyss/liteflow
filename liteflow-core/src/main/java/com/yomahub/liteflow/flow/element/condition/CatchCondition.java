package com.yomahub.liteflow.flow.element.condition;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.yomahub.liteflow.enums.ConditionTypeEnum;
import com.yomahub.liteflow.exception.CatchErrorException;
import com.yomahub.liteflow.flow.element.Condition;
import com.yomahub.liteflow.flow.element.Executable;
import com.yomahub.liteflow.slot.DataBus;
import com.yomahub.liteflow.slot.Slot;

/**
 * Catch Condition
 *
 * @author Bryan.Zhang
 * @since 2.10.0
 */
public class CatchCondition extends Condition {

	@Override
	public void executeCondition(Integer slotIndex) throws Exception {
		Slot slot = DataBus.getSlot(slotIndex);
		try {
			Executable catchExecutable = this.getCatchItem();
			if (ObjectUtil.isNull(catchExecutable)) {
				String errorInfo = StrUtil.format("[{}]:no catch item find", slot.getRequestId());
				throw new CatchErrorException(errorInfo);
			}
			catchExecutable.setCurrChainId(this.getCurrChainId());
			catchExecutable.execute(slotIndex);
		}
		catch (Exception e) {
			Executable doExecutable = this.getDoItem();
			if (ObjectUtil.isNotNull(doExecutable)) {
				doExecutable.setCurrChainId(this.getCurrChainId());
				doExecutable.execute(slotIndex);
			}
			// catch之后需要把exception给清除掉
			// 正如同java的catch一样，异常自己处理了，属于正常流程了，整个流程状态应该是成功的
			DataBus.getSlot(slotIndex).removeException();
		}
	}

	@Override
	public ConditionTypeEnum getConditionType() {
		return ConditionTypeEnum.TYPE_CATCH;
	}

	public Executable getCatchItem() {
		return this.getExecutableOne(ConditionKey.CATCH_KEY);
	}

	public void setCatchItem(Executable executable) {
		this.addExecutable(ConditionKey.CATCH_KEY, executable);
	}

	public Executable getDoItem() {
		return this.getExecutableOne(ConditionKey.DO_KEY);
	}

	public void setDoItem(Executable executable) {
		this.addExecutable(ConditionKey.DO_KEY, executable);
	}

}
