package org.idempiere.budget;

import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import org.compiere.model.Query;

public class MBudgetYear extends X_B_BudgetYear {

	private static final long serialVersionUID = 1L;

	private MBudgetPeriod[] m_periods = null;

	public MBudgetYear(Properties ctx, int B_BudgetYear_ID, String trxName) {
		super(ctx, B_BudgetYear_ID, trxName);
	}

	public MBudgetYear(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

	/**
	 * Get Periods of Year
	 * 
	 * @return Periods
	 */
	public MBudgetPeriod[] getPeriods(boolean isPeriod) {
		List<MBudgetPeriod> list = null;
		String condition = isPeriod ? " NOT NULL" : " NULL";
		list = new Query(getCtx(), I_B_BudgetPeriod.Table_Name, "B_BudgetYear_ID=? AND C_Period_ID IS" + condition,
				get_TrxName()).setParameters(getB_BudgetYear_ID()).setOrderBy(MBudgetPeriod.COLUMNNAME_B_BudgetPeriod_ID).setClient_ID()
				.list();
		m_periods = new MBudgetPeriod[list.size()];
		list.toArray(m_periods);
		return m_periods;
	} // getBudgetYearPeriods

}
