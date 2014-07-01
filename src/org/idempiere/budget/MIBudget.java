package org.idempiere.budget;

import java.sql.ResultSet;
import java.util.Properties;

public class MIBudget extends X_I_Budget {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6578537922838036193L;

	public MIBudget(Properties ctx, int I_Budget_ID, String trxName) {
		super(ctx, I_Budget_ID, trxName);
	}

	public MIBudget(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

}
