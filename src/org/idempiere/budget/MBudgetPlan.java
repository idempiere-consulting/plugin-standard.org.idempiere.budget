package org.idempiere.budget;

import java.sql.ResultSet;
import java.util.Properties;

public class MBudgetPlan extends X_B_BudgetPlan {

	 
	private static final long serialVersionUID = 1L;  
	
	public MBudgetPlan(Properties ctx, int B_BudgetPlan_ID, String trxName) {
		super(ctx, B_BudgetPlan_ID, trxName); 
	}

	public MBudgetPlan(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

	public MBudgetPlan(MBudgetPlan original) {
		this (original.getCtx(), 0, original.get_TrxName());
		setClientOrg(original); 		//
		setC_AcctSchema_ID(original.getC_AcctSchema_ID());
		setGL_Budget_ID(original.getGL_Budget_ID());  
		setDescription(original.getDescription());
		setC_DocType_ID(original.getC_DocType_ID()); 
		//
		setC_Currency_ID(original.getC_Currency_ID());  
	//	setDateDoc(original.getDateDoc());
	//	setDateAcct(original.getDateAcct());
	//	setC_Period_ID(original.getC_Period_ID());
	}	
}
