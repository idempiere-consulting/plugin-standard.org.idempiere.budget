package org.idempiere.budget;

import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import org.compiere.model.Query;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class MBudgetReference extends X_B_BudgetReference {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MBudgetReference(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName); 
	}
	public MBudgetReference(Properties ctx, int id, String trxName){
		super(ctx, id, trxName);
	}
	
	public static List<MBudgetReference> get(MBudgetPlanLine line){
		List<MBudgetReference> refs = new Query(Env.getCtx(), MBudgetReference.Table_Name, MBudgetReference.COLUMNNAME_B_BudgetPlanLine_ID
				+"=?", line.get_TrxName())
		.setParameters(line.getB_BudgetPlanLine_ID())
		.setOnlyActiveRecords(true)
		.setOrderBy(COLUMNNAME_Line)
		.list();
		return refs;
	}
	protected boolean beforeSave(boolean newRecord){
		if (getLine() == 0)
		{
			String sql = "SELECT COALESCE(MAX(Line),0)+10 FROM B_BudgetReference WHERE B_BudgetPlanLine_ID=?";
			int ii = DB.getSQLValue (get_TrxName(), sql, getB_BudgetPlanLine_ID());
			setLine (ii);
		}
		return true;
	}
}
