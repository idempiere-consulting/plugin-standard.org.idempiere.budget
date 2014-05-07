package org.budget.callout;

import java.math.BigDecimal;
import java.util.Properties;

import org.adempiere.base.IColumnCallout;
import org.compiere.model.CalloutEngine;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.util.Env;
import org.idempiere.budget.MBudgetConfig;

public class BudgetConfigCallout extends CalloutEngine implements IColumnCallout {

	public BudgetConfigCallout() {
		 	}

	@Override
	public String start(Properties ctx, int WindowNo, GridTab mTab,
			GridField mField, Object value, Object oldValue) {
		if (mField.getColumnName().equals(MBudgetConfig.COLUMNNAME_GL_Budget_Previous_Year))
			if (((BigDecimal) mTab.getValue(MBudgetConfig.COLUMNNAME_GL_Budget_Previous_Year)).intValue()>0)
				mTab.setValue(MBudgetConfig.COLUMNNAME_GL_Budget_Previous_Month, Env.ZERO);
		if (mField.getColumnName().equals(MBudgetConfig.COLUMNNAME_GL_Budget_Previous_Month))
			if (((BigDecimal) mTab.getValue(MBudgetConfig.COLUMNNAME_GL_Budget_Previous_Month)).intValue()>0)
				mTab.setValue(MBudgetConfig.COLUMNNAME_GL_Budget_Previous_Year, Env.ZERO);
		return null;
	}

}
