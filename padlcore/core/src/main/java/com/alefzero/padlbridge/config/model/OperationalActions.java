package com.alefzero.padlbridge.config.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public enum OperationalActions {
	UNSET(0), EXISTS(1), DELETE(2), ADD(3), UPDATE(4), DO_NOTHING(5), REPLACE(6);

	private Integer operationValue = 0;
	private static Map<Integer, OperationalActions> actions = new HashMap<Integer, OperationalActions>();

	static {
		for (OperationalActions item : OperationalActions.values()) {
			actions.put(item.getOperationalValue(), item);
		}
	}

	OperationalActions(int operationValue) {
		this.operationValue = operationValue;
	}

	public Integer getOperationalValue() {
		return this.operationValue;
	}

	public static OperationalActions fromOperationalValue(Integer value) {
		return Objects.requireNonNull(actions.get(value), "Value " + value + " does not map to a valid action.");
	}

}
