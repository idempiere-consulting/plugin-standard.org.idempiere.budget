package org.idempiere.budget;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import org.compiere.model.MAccount;
import org.compiere.model.MPeriod;
import org.compiere.model.Query;
import org.compiere.process.SvrProcess;
import org.compiere.util.Env;

public class CalculateBudget extends SvrProcess {

	MBudgetYear budgetYear = null;
	Timestamp startDateOfYear = null;

	@Override
	protected void prepare() {
	}

	@Override
	protected String doIt() throws Exception {

		budgetYear = new MBudgetYear(getCtx(), getRecord_ID(), get_TrxName());
		MPeriod period = new Query(getCtx(), MPeriod.Table_Name, "C_Year_ID=?", get_TrxName())
				.setParameters(budgetYear.getC_Year_ID()).setOrderBy(MPeriod.COLUMNNAME_PeriodNo).setClient_ID()
				.first();
		startDateOfYear = period.getStartDate();
		List<MBudgetPlanLine> budgetPlanRules = new Query(getCtx(), MBudgetPlanLine.Table_Name, "B_BudgetPlan_ID=?",
				get_TrxName()).setParameters(budgetYear.getB_BudgetPlan_ID()).setOnlyActiveRecords(true).setClient_ID()
				.list();
		MBudgetPlanLine budgetPlanRule = null;
		int periodCount = 0;
		int yearCount = 0;

		if (budgetPlanRules != null && budgetPlanRules.size() != 0) {
			for (int i = 0; i < budgetPlanRules.size(); i++) {
				budgetPlanRule = budgetPlanRules.get(i);
				if (budgetPlanRule.getC_Period_ID() != 0) {
					if (periodCount == 0) {
						createBudgetPeriods(true);
						periodCount++;
					}
					createBudgetLines(budgetYear.getPeriods(true), budgetPlanRule, true);
				} else {
					if (yearCount == 0) {
						createBudgetPeriods(false);
						yearCount++;
					}
					createBudgetLines(budgetYear.getPeriods(false), budgetPlanRule, false);
				}
			}
		}
		return "Budget Calculation done";
	}

	/**
	 * @param budgetPeriods
	 * @param budgetPlanLine
	 * @param isPeriod
	 */
	private void createBudgetLines(MBudgetPeriod[] budgetPeriods, MBudgetPlanLine budgetPlanLine, Boolean isPeriod) {

		BigDecimal zero = new BigDecimal(0);
		BudgetUtils bg = new BudgetUtils();
		SQLfromDoc sDoc = new SQLfromDoc();

		for (int i = 0; i < budgetPeriods.length; i++) {
			if (budgetPeriods[i].getDocStatus().equals("DR")) {
				MBudgetLine budgetLine = new MBudgetLine(getCtx(), 0, budgetPeriods[i].getB_BudgetPeriod_ID(),
						get_TrxName());
				budgetLine.set_ValueOfColumn("B_BudgetPlanLine_ID", budgetPlanLine.getB_BudgetPlanLine_ID());
				budgetLine.setAD_Org_ID(budgetPlanLine.getAD_OrgDoc_ID());
				budgetLine.setC_ValidCombination_ID(getValidCombination(budgetPlanLine));
				if (!isPeriod) {
					budgetLine.setDateAcct(startDateOfYear);
				} else
					budgetLine.setDateAcct(budgetPeriods[i].getDateAcct());
				if (budgetPlanLine.getM_Product_ID() != 0 && budgetPlanLine.getQty().compareTo(zero) != 0) { // set
																												// Quantity
					budgetLine.setQty(budgetPlanLine.getQty());
					if (isPeriod)
						budgetLine.setQty(budgetPlanLine.getQty().divide(new BigDecimal(12), 2));
					budgetLine.setAmount(zero);
				} else { // set Amount
					if (budgetPlanLine.getPercent().compareTo(zero) == 0) {
						if (budgetPlanLine.getAmtAcctDr().compareTo(zero) != 0)
							budgetLine.setAmount(budgetPlanLine.getAmtAcctDr());
						else
							budgetLine.setAmount(budgetPlanLine.getAmtAcctCr());
					} else if (budgetPlanLine.getPercentageBase_ID() == 0) {
						BudgetUtils.budgetCONFIGinstance = null;
						bg.initBudgetConfig(budgetPlanLine);
						bg.setupCalendar(budgetPlanLine);
						bg.revenueFlag = true;
						budgetLine.setAmount(bg.revenueEstimate().multiply(budgetPlanLine.getPercent())
								.divide(Env.ONEHUNDRED, 2));
					} else {
						sDoc = new SQLfromDoc(budgetPlanLine, BudgetUtils.previousMonths);
						sDoc.setSQLMatching();
						BudgetUtils.budgetCONFIGinstance = null;
						bg.initBudgetConfig(budgetPlanLine);
						bg.setupCalendar(budgetPlanLine);
						budgetLine.setAmount(bg.percentageBase(budgetPlanLine, sDoc)
								.multiply(budgetPlanLine.getPercent()).divide(Env.ONEHUNDRED, 2));
					}
					if (isPeriod)
						budgetLine.setAmount(budgetLine.getAmount().divide(new BigDecimal(12), 2));
					budgetLine.setQty(zero);
				}
				budgetLine.save();
			}
		}
	} // createBudgetLines

	/**
	 * @param budgetPlanLine
	 * @return
	 */
	private int getValidCombination(MBudgetPlanLine budgetPlanLine) {

		MBudgetPlan budgetPlan = new MBudgetPlan(getCtx(), budgetPlanLine.getB_BudgetPlan_ID(), get_TrxName());
		MAccount account = MAccount.get(getCtx(), budgetPlanLine.getAD_Client_ID(), budgetPlanLine.getAD_Org_ID(),
				budgetPlan.getC_AcctSchema_ID(), budgetPlanLine.getAccount_ID(), 0, budgetPlanLine.getM_Product_ID(),
				budgetPlanLine.getC_BPartner_ID(), 0, 0, 0, budgetPlanLine.getC_SalesRegion_ID(),
				budgetPlanLine.getC_Project_ID(), budgetPlanLine.getC_Campaign_ID(), budgetPlanLine.getC_Activity_ID(),
				0, 0, 0, 0);
		return account.getC_ValidCombination_ID();
	} // getValidCombination

	/**
	 * @param isPeriod
	 */
	private void createBudgetPeriods(Boolean isPeriod) {

		MBudgetPeriod budgetPeriods[] = budgetYear.getPeriods(isPeriod);
		if (budgetPeriods != null && budgetPeriods.length != 0) {
			createPeriodsOrDeleteLines(budgetPeriods, isPeriod);
		} else {
			if (isPeriod) {
				List<MPeriod> periods = new Query(getCtx(), MPeriod.Table_Name, MPeriod.COLUMNNAME_C_Year_ID + "=?",
						get_TrxName()).setClient_ID().setParameters(budgetYear.getC_Year_ID()).list();
				if (periods.size() != 12)
					log.warning("PERIOD.SIZE() IS NOT 12 !?");
				for (MPeriod period : periods) {
					MBudgetPeriod budgetPeriod = new MBudgetPeriod(getCtx(), 0, budgetYear.getB_BudgetYear_ID(),
							get_TrxName());
					budgetPeriod.setC_Period_ID(period.getC_Period_ID());
					budgetPeriod.setC_DocType_ID(budgetYear.getC_DocType_ID());
					budgetPeriod.setDateAcct(period.getStartDate());
					budgetPeriod.save();
				}
			} else {
				MBudgetPeriod budgetPeriod = new MBudgetPeriod(getCtx(), 0, budgetYear.getB_BudgetYear_ID(),
						get_TrxName());
				budgetPeriod.setDateAcct(startDateOfYear);
				budgetPeriod.setC_DocType_ID(budgetYear.getC_DocType_ID());
				budgetPeriod.save();
			}
		}
	} // createPeriods

	/**
	 * @param budgetPeriods
	 * @param isPeriod
	 */
	private void createPeriodsOrDeleteLines(MBudgetPeriod[] budgetPeriods, Boolean isPeriod) {
		String condition = null;
		for (int i = 0; i < budgetPeriods.length; i++) {
			if (budgetPeriods[i].getDocStatus().equals("VO")) {
				if (!isPeriod)
					condition = " IS NULL";
				else
					condition = "=" + budgetPeriods[i].getC_Period_ID();
				List<MBudgetPeriod> list = new Query(getCtx(), MBudgetPeriod.Table_Name, "C_Period_ID" + condition
						+ " AND B_BudgetYear_ID=? AND AD_Client_ID=? AND (DocStatus=? OR DocStatus=? OR DocStatus=?)",
						get_TrxName())
						.setParameters(budgetYear.getB_BudgetYear_ID(), budgetYear.getAD_Client_ID(), "DR", "CO", "CL")
						.setOnlyActiveRecords(true).setClient_ID().list();
				if (list.size() == 0) {
					MBudgetPeriod budgetPeriod = new MBudgetPeriod(getCtx(), 0, budgetYear.getB_BudgetYear_ID(),
							get_TrxName());
					budgetPeriod.setC_DocType_ID(budgetYear.getC_DocType_ID());
					budgetPeriod.setDateAcct(startDateOfYear);
					if (isPeriod) {
						budgetPeriod.setC_Period_ID(budgetPeriods[i].getC_Period_ID());
						budgetPeriod.setDateAcct(MPeriod.get(getCtx(), budgetPeriods[i].getC_Period_ID())
								.getStartDate());
					}
					budgetPeriod.save();
				}
			} else if (budgetPeriods[i].getDocStatus().equals("DR")) {
				MBudgetPeriod.deleteLines(budgetPeriods[i]);
			}
		}
	} // createPeriodsOrDeleteLines

}
