package org.idempiere.budget;

import java.sql.ResultSet;
import java.util.Properties;


public class MBudgetConfig extends X_GL_BudgetConfig {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6313694637129668748L;

	public MBudgetConfig(Properties ctx, int GL_BudgetConfig_ID, String trxName) {
		super(ctx, GL_BudgetConfig_ID, trxName); 
	}

	public MBudgetConfig(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

}
