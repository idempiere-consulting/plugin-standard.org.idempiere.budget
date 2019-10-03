package org.idempiere.budget;

import java.sql.ResultSet;
import java.util.Properties;

public class MBudgetLine extends X_B_BudgetLine {

	private static final long serialVersionUID = 1L;

	public MBudgetLine(Properties ctx, int B_BudgetLine_ID, String trxName) {
		super(ctx, B_BudgetLine_ID, trxName);
	}

	public MBudgetLine(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

	public MBudgetLine(Properties ctx, int B_BudgetLine_ID, int B_BudgetPeriod_ID, String trxName) {
		super(ctx, B_BudgetLine_ID, trxName);
		setB_BudgetPeriod_ID(B_BudgetPeriod_ID);
	}
}
