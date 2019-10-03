package org.budget.callout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Properties;

import org.adempiere.base.IColumnCallout;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.MJournalLine;
import org.compiere.model.MOrder;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.idempiere.budget.BudgetUtils;
import org.idempiere.budget.MBudgetPlanLine;
import org.idempiere.budget.SQLfromDoc;
import org.idempiere.budget.X_B_BudgetPlanLine;

public class LookupBase implements IColumnCallout {

	private String operand = "";

	public LookupBase() {
		 	}

    private BudgetUtils bg = new BudgetUtils();
    private SQLfromDoc sDoc = new SQLfromDoc();
	private int project_ID=0;
	private int campaign_ID=0;
	private int activity_ID=0;
	private int product_ID=0;
	private int bpartner_ID=0;
	private int period_ID=0;
	private int org_ID=0;
	private int qty=0;
	private boolean isSOTrx=false;
	private int account_ID=0;
	private String text;
	private BigDecimal percent=Env.ZERO;
	private int percentageBaseID=0;
	private BigDecimal amtCredit;
	private BigDecimal amtDebit; 
	private BigDecimal value = Env.ZERO;
	private StringBuffer desc = new StringBuffer();
	private int planLine_ID = 0;
	private static StringBuffer descX = new StringBuffer();

	@Override
	public String start(Properties ctx, int WindowNo, GridTab mTab,
			GridField mField, Object value, Object oldValue) {
		if (value==null && oldValue==null) return null;
		if (value==oldValue) return null;
		//pull everything for analysis to get percent or fixed amt against %base/revenue
		if (mTab.getValue(MBudgetPlanLine.COLUMNNAME_B_BudgetPlanLine_ID)!=null)
			planLine_ID = (Integer)mTab.getValue(MBudgetPlanLine.COLUMNNAME_B_BudgetPlanLine_ID);
		if (mTab.getValue(MBudgetPlanLine.COLUMNNAME_Account_ID)!=null)
			account_ID = (Integer)mTab.getValue(MBudgetPlanLine.COLUMNNAME_Account_ID);
		if (mTab.getValue(MBudgetPlanLine.COLUMNNAME_PercentageBase_ID)!=null)
			percentageBaseID = (Integer)mTab.getValue(MBudgetPlanLine.COLUMNNAME_PercentageBase_ID);
		if (mTab.getValueAsBoolean(MBudgetPlanLine.COLUMNNAME_Percent));
			percent = (BigDecimal) mTab.getValue(MBudgetPlanLine.COLUMNNAME_Percent);
		if (mTab.getValue(MBudgetPlanLine.COLUMNNAME_C_Project_ID)!=null)
			project_ID = (Integer) mTab.getValue(MBudgetPlanLine.COLUMNNAME_C_Project_ID);
		if (mTab.getValue(MBudgetPlanLine.COLUMNNAME_C_Campaign_ID)!=null)
			campaign_ID = (Integer)mTab.getValue(MBudgetPlanLine.COLUMNNAME_C_Campaign_ID);
		if (mTab.getValue(MBudgetPlanLine.COLUMNNAME_C_Activity_ID)!=null)
			activity_ID = (Integer)mTab.getValue(MBudgetPlanLine.COLUMNNAME_C_Activity_ID);
		if (mTab.getValue(MBudgetPlanLine.COLUMNNAME_M_Product_ID)!=null)	
			product_ID = (Integer)mTab.getValue(MBudgetPlanLine.COLUMNNAME_M_Product_ID);
		if (mTab.getValue(MBudgetPlanLine.COLUMNNAME_C_BPartner_ID)!=null)	
			bpartner_ID = (Integer)mTab.getValue(MBudgetPlanLine.COLUMNNAME_C_BPartner_ID);
		if (mTab.getValue(MBudgetPlanLine.COLUMNNAME_C_Period_ID)!=null)	
			period_ID = (Integer)mTab.getValue(MBudgetPlanLine.COLUMNNAME_C_Period_ID);
		if (mTab.getValue(MBudgetPlanLine.COLUMNNAME_AD_OrgDoc_ID)!=null)	
			org_ID = (Integer)mTab.getValue(MBudgetPlanLine.COLUMNNAME_AD_OrgDoc_ID);
		if (mTab.getValue(MBudgetPlanLine.COLUMNNAME_Qty)!=null)	
			qty = ((BigDecimal)mTab.getValue(MBudgetPlanLine.COLUMNNAME_Qty)).intValue();
		if (mTab.getValue(MBudgetPlanLine.COLUMNNAME_IsSOTrx)!=null)	
			isSOTrx = mTab.getValueAsBoolean(MBudgetPlanLine.COLUMNNAME_IsSOTrx);
		if (mTab.getValue(MBudgetPlanLine.COLUMNNAME_AmtAcctCr)!=null)	
			amtCredit = (BigDecimal)mTab.getValue(MBudgetPlanLine.COLUMNNAME_AmtAcctCr);	
		if (mTab.getValue(MBudgetPlanLine.COLUMNNAME_AmtAcctDr)!=null)	
			amtDebit = (BigDecimal)mTab.getValue(MBudgetPlanLine.COLUMNNAME_AmtAcctDr);
		//if (percent.add(amtCredit).add(amtDebit).compareTo(Env.ZERO)==0) return null;							
		//form BudgetPlanLine model
		MBudgetPlanLine model = new MBudgetPlanLine(Env.getCtx(),0, null);
		model.setB_BudgetPlanLine_ID(planLine_ID);
		model.setC_Project_ID(project_ID);
		model.setC_Campaign_ID(campaign_ID);
		model.setC_Activity_ID(activity_ID);
		model.setM_Product_ID(product_ID);
		model.setC_BPartner_ID(bpartner_ID);
		model.setC_Period_ID(period_ID);
		model.setAD_OrgDoc_ID(org_ID);
		model.setQty(new BigDecimal(qty));
		model.setPercent(percent);
		model.setAccount_ID(account_ID);
		model.setPercentageBase_ID(percentageBaseID);
		model.setAmtAcctCr(amtCredit);
		model.setAmtAcctDr(amtDebit);

		//get previous months / years from BudgetConfigurator
		if (BudgetUtils.budgetCONFIGinstance==null){
			//init and reset budget config details
			bg.revenueFlag = true;
		    bg.initBudgetConfig(model);			    	
			bg.setupCalendar(model); 
			BigDecimal returnAmount = bg.revenueEstimate();
			bg.budgetTrend(model, returnAmount, BudgetUtils.revenueKey);
			bg.revenueFlag = false;
		}
		bg.isPurchasing = isSOTrx;
		int previousMonths = BudgetUtils.previousMonths;
		int previousYears = BudgetUtils.previousYears;
				
		//get matches SQLfromDoc
		sDoc = new SQLfromDoc(model, previousMonths);
		sDoc.setSQLMatching();

		text = lookupBase(model, sDoc);
			
		mTab.setValue(X_B_BudgetPlanLine.COLUMNNAME_Description, text);
		
		if (desc.toString().equals(descX.toString())) //to persist result value upon double click
			generatedDoubleClick(model, mTab);
			
		descX = desc; //double click effect
		
		return null;
	}
	/**
	 * isGenerated field checked twice
	 * @param model
	 * @param mTab
	 */
	private void generatedDoubleClick(MBudgetPlanLine model, GridTab mTab) {
		if (model.getPercent().compareTo(Env.ZERO)==0){
			if (bg.matchedIsCreditAmt)
				mTab.setValue(X_B_BudgetPlanLine.COLUMNNAME_AmtAcctCr, value);
			else
				mTab.setValue(X_B_BudgetPlanLine.COLUMNNAME_AmtAcctDr, value);
		}
		else
			mTab.setValue(X_B_BudgetPlanLine.COLUMNNAME_Percent, value);
	}
	/**
	 * Lookup Base with YearOnYear feature
	 * BaseAmt and Value in Desc format
	 * @param model
	 * @param sDoc
	 * @return
	 */
	private String lookupBase(MBudgetPlanLine model, SQLfromDoc sDoc) {
		operand = BudgetUtils.MORE_EQUAL;
		bg.setSQLfromDoc(sDoc);
		bg.checkCreditOrDebit(model);
		bg.checkProrata(model);   //show when period selected -> baseAmt/12
		
		//init desc sub strings
		String isSalesTrx = "";
		String prorata = "";
		String yoy = "";
		
		if (model.getAccount_ID()>0) {
			MJournalLine j = new MJournalLine(Env.getCtx(),0, model.get_TrxName());
			bg.runtimePO = j;
		} else {
			MOrder o = new MOrder(Env.getCtx(),0,model.get_TrxName());
			bg.runtimePO = o;
		}
			
		//delegate to YearOnYear for same month no lookup
		if (BudgetUtils.isMonthToMonth && model.getC_Period_ID()>0) {
			if (model.getAccount_ID()==0)
				sDoc.stripPeriod(); //get Period ID off match-set cos this is for Sales/Purchases
			yoy = "(YOY) ";
			value = bg.yearOnYear(period_ID, model);
			if (isSOTrx){
				bg.isPurchasing = false;
				isSalesTrx = "Sales";
			}
			else {
				bg.isPurchasing = true;
				isSalesTrx = "Purchasing";
			}
		}
		else{
			//normal LookUp
			if (account_ID>0)
				value = bg.selectAccountingFacts(model, operand, BudgetUtils.startYear, BudgetUtils.startMonth, BudgetUtils.revenueKey);
			else {
				if (isSOTrx){
					bg.isPurchasing = false;
					isSalesTrx = "Sales";
				}
				else {
					bg.isPurchasing = true;
					isSalesTrx = "Purchasing";
				}
				if (period_ID>0)
					sDoc.stripPeriod();
				if (model.getM_Product_ID()>0)
					bg.hasProduct=true;
				value = bg.selectPurchasesSales(model, operand, BudgetUtils.startYear, BudgetUtils.startMonth);
			}
		}

		value = bg.budgetTrend(model, value, BudgetUtils.revenueKey);
		
		//check if PercentageBase
		BigDecimal baseAmt = BudgetUtils.RevenueEstimate;
		if (percentageBaseID>0){
			baseAmt = bg.percentageBase(model, sDoc);
		}
		else
			baseAmt = bg.budgetTrend(model, baseAmt, BudgetUtils.revenueKey);
		//format Year/Month desc
		String yearDesc = Msg.translate(Env.getCtx(),"Year");
		String previousValue = BudgetUtils.startYear;
		if (BudgetUtils.previousMonths>0){
			yearDesc = Msg.translate(Env.getCtx(),"Months");
			previousValue = ((Integer)BudgetUtils.previousMonths).toString();
		}
		//Calculate result
		if (bg.isProrata){
			prorata = Msg.translate(Env.getCtx(), "Prorata");
			baseAmt = baseAmt.divide(new BigDecimal(12),2);
		}
		String result = Msg.translate(Env.getCtx(),"Result");
		if (percent.compareTo(Env.ZERO)>0 && baseAmt.compareTo(Env.ZERO)>0) {
			value = value.divide(baseAmt,2).multiply(Env.ONEHUNDRED); 
			result = Msg.translate(Env.getCtx(), "Percent");
		}
		else if (percentageBaseID>0) //Percent OFF, percentageBase ON
			value = baseAmt; //option to use reference value as Budget Amount to set
		//final values
		value = value.setScale(2, RoundingMode.HALF_EVEN);
		String trend = Msg.translate(Env.getCtx(),"Trend");
		baseAmt = baseAmt.setScale(2,RoundingMode.HALF_EVEN);

		//full description string for output
		String original = "";
		if (bg.originalBase!=null)
			original = bg.originalBase.compareTo(baseAmt)==0? "":"("+bg.originalBase+")";
		desc = desc.append(prorata.isEmpty()? "":prorata).append(bg.referenceBase+":").append(original.isEmpty()? "":original).append(" "+baseAmt+" ")
				.append(yearDesc+": ").append(previousValue+" ").append(isSalesTrx+" ")
				.append(result+" Value: ").append(value+" ").append(yoy+trend+": "+BudgetUtils.budgetCONFIGinstance.getBudgetTrend());
		
		return desc.toString();
	}
}
