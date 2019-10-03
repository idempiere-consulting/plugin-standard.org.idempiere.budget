/******************************************************************************
 * Product: iDempiere Free ERP Project based on Compiere (2006)               *
 * Copyright (C) 2014 Redhuan D. Oon All Rights Reserved.                     *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *  FOR NON-COMMERCIAL DEVELOPER USE ONLY                                     *
 *  @author Redhuan D. Oon  - red1@red1.org  www.red1.org  www.sysnova.com    *
 *****************************************************************************/

package org.idempiere.budget;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MConversionRate;
import org.compiere.model.MElementValue;
import org.compiere.model.MFactAcct;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MJournal;
import org.compiere.model.MJournalLine;
import org.compiere.model.MMessage;
import org.compiere.model.MNote;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPayment;
import org.compiere.model.MPeriod;
import org.compiere.model.MYear;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;

public class BudgetUtils{
	public BudgetUtils(){
	}
	//static variables - require Reset Config if changed 
	private static CLogger log = CLogger.getCLogger(BudgetUtils.class);
	public static MBudgetConfig budgetCONFIGinstance;
	public static MBudgetPlan targetBudget = null;
	public static BigDecimal RevenueEstimate; 
	public static final String MORE_EQUAL = ">=";
	public static final String EQUAL = "=";
	public static String revenueKey;
	private static final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");	
	public static int previousYears; 
	private static String presentYear;
	public static String startYear;
	private static String pastYear;
	public static int previousMonths;
	private static int presentMonth;
	private static int pastMonth;
	private static List<Integer>mom = new ArrayList<Integer>(); 
	public static boolean isMonthToMonth; //use only during GenerateBudget
	public static int startMonth;	
	private static int firstPeriodOfYear;

	public PO runtimePO;
	private int nowPeriodNo;
	
	public boolean isProrata = false; 
	private SQLfromDoc sDoc = new SQLfromDoc();	
	private BigDecimal budgetPercent;
	private BigDecimal budgetAmount;
	public boolean matchedIsCreditAmt = true;
	public boolean isPurchasing;
	private String isSOTrx = "";
	public String referenceBase;
	public boolean revenueFlag = false;
	
	private boolean hasQty = false;
	public boolean hasProduct = false;
	private List<MBudgetPlanLine> matchedProdQtyArray = new ArrayList<MBudgetPlanLine>();
	private BigDecimal figure = Env.ZERO;
	private boolean isJournal;
	public BigDecimal originalBase; 
	
	public void setSQLfromDoc(SQLfromDoc sDoc){
		this.sDoc = sDoc;
	}
	
	/**
	 * check properties of budgetconfiginstance
	 * obtain start/past year values according to previous years
	 * convert previous-months to period ids
	 */
	public void setupCalendar(PO po) {
		log.fine("setupCalendar(PO po)");
		previousYears = budgetCONFIGinstance.getGL_Budget_Previous_Year().intValue();
		previousMonths = budgetCONFIGinstance.getGL_Budget_Previous_Month().intValue();
		isMonthToMonth = budgetCONFIGinstance.isMonthToMonth();
		//present calendar
		Calendar cal = Calendar.getInstance();
		//present year
		presentYear = yearFormat.format(cal.getTime());		
		//present month 
		presentMonth = MPeriod.getC_Period_ID(po.getCtx(), new Timestamp(cal.getTimeInMillis()), po.getAD_Org_ID());			
		//last year
		cal.add(Calendar.YEAR, -1);
		pastYear = yearFormat.format(cal.getTime());
		
		if (previousMonths > 0){
			previousYears = 0;
			startYear = presentYear;
			pastYear = ""; 
		}
		if (previousYears > 0){
			//starting year
			cal = Calendar.getInstance();
			cal.add(Calendar.YEAR, -previousYears);
			startYear = yearFormat.format(cal.getTime());
		}
				
		//month periods ids assumed to be consecutive.
		//last month
		pastMonth = presentMonth - 1;
		//starting month
		startMonth = presentMonth - previousMonths;
		firstPeriodOfYear = (MPeriod.getFirstInYear(Env.getCtx(), setTimeToZero(po.getCreated()), po.getAD_Org_ID())).getC_Period_ID();
		nowPeriodNo = presentMonth - firstPeriodOfYear; //Difference of IDs = exact PeriodNo (this month subtract first month).

		log.finer("START YEAR: "+startYear+" START MONTH: "+startMonth+" PRESENT YEAR: "+presentYear+" PRESENT MONTH: "+presentMonth+" PAST YEAR: "+pastYear+" PAST MONTH: "+pastMonth);
	}

	/**
	 * @param Date
	 * @return
	 */
	private Timestamp setTimeToZero(Timestamp Date) {

		Date.setHours(0);
		Date.setMinutes(0);
		Date.setSeconds(0);
		Date.setNanos(0);
		return Date;
	} // setTimeToZero

	/**
	 * load config settings :
	 * - one-time setup update of revenue total
	 * @param po
	 */
	public void initBudgetConfig(PO po){
 		log.fine("public void initBudgetConfig(");
 		mom = new ArrayList<Integer>();;//reset for MonthToMonth
		budgetCONFIGinstance = new Query(po.getCtx(), MBudgetConfig.Table_Name,
				"", po.get_TrxName()).setOnlyActiveRecords(true).setClient_ID()
				.firstOnly(); // select only active record for current client -- Logilite

		//if budget module not active
		if (budgetCONFIGinstance==null) throw new AdempiereException("NULL BUDGETCONFIG - YOU CAN STOP BUDGET MODULE");
		if (!budgetCONFIGinstance.isActive()) 
			throw new AdempiereException("NOT ACTIVE BUDGETCONFIG - YOU CAN STOP BUDGET MODULE");
		//static instance of single target budget
		targetBudget = new Query(po.getCtx(), MBudgetPlan.Table_Name, "", po.get_TrxName())
			.setOnlyActiveRecords(true)
			//select only record for current client -- Logilite
			.setClient_ID()	
 			.first();	 
		//get revenue account element value
		int revenueID = budgetCONFIGinstance.getAccount_ID();
		//revenueKey =  
		MElementValue element = new Query (po.getCtx(), MElementValue.Table_Name, MElementValue.COLUMNNAME_C_ElementValue_ID+"=?", po.get_TrxName())
			.setParameters(revenueID).first();
		revenueKey = element.getValue(); 
		//check BudgetPlan existence
		MBudgetPlan plan = new Query(Env.getCtx(), MBudgetPlan.Table_Name, "",po.get_TrxName())
			.setClient_ID()
			.first();
		if (plan==null)
			throw new AdempiereException("Error - No Budget Plan - Please Set Up First.");
		
 		log.finer("REVENUE ID: "+revenueID+" Budget Plan: "+plan.getDescription());
	}
	
	/**
	 * Setters for generate budget process - taking from params there
	 * @param previousYears
	 * @param MonthToMonth
	 * @param PreviousMonths 
	 * @param Prorata
	 */
	public void setInstance(BigDecimal PreviousYears,String MonthToMonth,BigDecimal PreviousMonths,String Prorata) {
		log.fine("setInstance(...)");
		if (PreviousYears.intValue()>0)
			budgetCONFIGinstance.setGL_Budget_Previous_Year(PreviousYears);
		if (PreviousMonths.intValue()>0)
			budgetCONFIGinstance.setGL_Budget_Previous_Month(PreviousMonths);
		if (new Boolean(MonthToMonth))
			budgetCONFIGinstance.setMonthToMonth(new Boolean(MonthToMonth));
		if (new Boolean(Prorata))
			budgetCONFIGinstance.setProrata(new Boolean(Prorata));
		previousYears = PreviousYears.intValue();
		previousMonths = PreviousMonths.intValue();
		log.finer("SET INSTANCE Previous years:"+previousYears+" MonthToMonth: "+MonthToMonth+" PreviousMonths: "+PreviousMonths+" Prorata: "+Prorata);
	}
	/**
	 * one time setup revenue over years range or months range, apply budget trend
	 * @return revenue estimate
	 */
	public BigDecimal revenueEstimate() {
		log.fine("revenueEstimate()");
		//previous years
		if (previousYears==0)  //NO ACTION
				RevenueEstimate = Env.ZERO;					 
		else if (previousYears<100)
			//for previousYears == 1 startYear is effectively pastYear
			RevenueEstimate = selectAccountingFacts(null, MORE_EQUAL, startYear, 0, revenueKey);
		else 
			RevenueEstimate = new BigDecimal(previousYears);
		//previous months
		if (previousMonths<99 && previousMonths>0)
			//for previousMonths == 1 starting month is effectively pastMonth
			RevenueEstimate = selectAccountingFacts(null, MORE_EQUAL, null, startMonth, revenueKey);
		
		log.finer("REVENUE ESTIMATE: "+RevenueEstimate.toString());	
		RevenueEstimate = RevenueEstimate.setScale(2, RoundingMode.HALF_EVEN); 
		return RevenueEstimate;
	}
	
	/**
	 *  apply budget trend list: average, average+last, progressive, accumulative,
	 * 	year-to-date, and apply prorata /12.
	 * @param line
	 * @param 
	 * @return revenue amount
	 */
	public BigDecimal budgetTrend(MBudgetPlanLine line, BigDecimal returnAmount, String baseAcct) {		
		log.fine("BigDecimal budgetTrend(MJournalLine line, BigDecimal returnAmount, int Period_ID)");
		//returnAmount is default Accumulative Amount
		//previous Months/Years = 1 , solved by design in RevenueEstimate as past or previous 1.
		if (previousYears>99)
			return returnAmount;
		BigDecimal average = Env.ZERO;
		if (previousMonths>0 && previousYears>0) throw new AdempiereException("BUDGET TREND ERROR: MONTHS/YEARS CONFLICT");
	
		if (previousMonths>1){
			average = returnAmount.divide(new BigDecimal(previousMonths),2);
		}
		else if (previousYears>1)
			//amount average across range of years
			average = returnAmount.divide(new BigDecimal(previousYears),2);
			
			//year to-date
		else if (budgetCONFIGinstance.getBudgetTrend().equals("T")) {
			//previousYears = 1; //for GenerateBudget selectPurchases
			if (runtimePO instanceof MJournalLine || revenueFlag)
				returnAmount = selectAccountingFacts(line, EQUAL, presentYear, 0, baseAcct);
			else 
				returnAmount = selectPurchasesSales(line, EQUAL, presentYear, 0);
			}		
		
		//trend average
		if (budgetCONFIGinstance.getBudgetTrend().equals("A"))
			returnAmount = average;
		
		//average+last = add average to previous month
		else if (budgetCONFIGinstance.getBudgetTrend().equals("L")){
			BigDecimal lastAmt = Env.ZERO;
			if (runtimePO instanceof MJournalLine || revenueFlag)
				lastAmt = selectAccountingFacts(line, EQUAL, pastYear, pastMonth, baseAcct);
			else
				lastAmt = selectPurchasesSales(line, EQUAL, pastYear, pastMonth);
			returnAmount = lastAmt.add(average);
			}			
		//apply rate of change over range to last month's 
		else if (budgetCONFIGinstance.getBudgetTrend().equals("P")) { 
			returnAmount = rateIncreaseFormula(line, sDoc, baseAcct);
		}
		return returnAmount;
	}

	/**
	 * set source amount to credit or debit
	 */
	public MBudgetPlanLine setAmtSource(PO line, BigDecimal amt){
		log.fine("MJournalLine setAmtSource(MJournalLine line, BigDecimal amt)");
		if (matchedIsCreditAmt)
			line.set_ValueOfColumn(MBudgetPlanLine.COLUMNNAME_AmtAcctCr, amt);
		else line.set_ValueOfColumn(MBudgetPlanLine.COLUMNNAME_AmtAcctDr, amt);
		return (MBudgetPlanLine) line;
	}
 
	/**
	 * Purchasing Documents Event Validation
	 * use matches/whereclause construct from period or account-id,project,activity,campaign,bpartner
	 * apply budget-config rules to budgetamount compare to totalpurchases for the year.
	 * @param po
	 * @return if error string
	 */
	public String eventPurchasesSales(PO po) {
		log.fine("String processPurchaseOrder(PO po)");
		referenceBase = Msg.translate(Env.getCtx(), "Revenue");
		runtimePO = po;   
 		isJournal = false;
 		figure = Env.ZERO;
		if (po instanceof MOrder) {
			MOrder purchase = (MOrder)po; 
			isPurchasing = !((MOrder) po).isSOTrx();
			po = purchase;
		}
		else if (po instanceof MInvoice){
			MInvoice invoice = (MInvoice)po;
			if (invoice.getC_Order_ID()>0 || invoice.getC_Payment_ID()>0)
				return null; 
				isPurchasing = !((MInvoice) po).isSOTrx();
			po = invoice;
		}
		else if (po instanceof MPayment){
			MPayment payment = (MPayment)po;	
			if (payment.getC_Order_ID()>0 || payment.getC_Invoice_ID()>0)
				return null;
			if (payment.getC_DocType().isSOTrx())
				isPurchasing = false;
			else isPurchasing = true;
			po = payment;
		}
		hasQty = hasProduct = false;
		matchedProdQtyArray.clear();
		MBudgetPlanLine matchedBudgetLine = matchingBudget(po);		
		if (matchedProdQtyArray.size()>0){ //hasProduct case - produced by matchingBudget 
			MBudgetPlanLine matchedBgtLPQ = matchedProductQty(po);//handle multiple matchedProductQty
				if (matchedBgtLPQ!=null){
					matchedBudgetLine = matchedBgtLPQ;	
				}
			} 
		if (matchedBudgetLine!=null){ 
			checkCreditOrDebit(matchedBudgetLine);
			checkProrata(matchedBudgetLine); 
			sDoc.paramTrimming(matchedBudgetLine, runtimePO);	
			BigDecimal totalPurchasesSales = selectPurchasesSales(matchedBudgetLine, EQUAL, presentYear, previousMonths);
			if (!hasProduct) //hasProduct case already added in matchedProductQty(po)
				totalPurchasesSales = totalPurchasesSales.add(getGrandTotal(po)); 
			//if prorata take only present month(s) purchases
			return budgetAgainstToDate(totalPurchasesSales, matchedBudgetLine, sDoc);
		}
		log.finer("ISPURCHASING: "+isPurchasing+" RUNTIME PO: "+runtimePO);
		return null;//no error
	}

	private BigDecimal getGrandTotal(PO po) {
		BigDecimal total = Env.ZERO;
		if (po instanceof MOrder){
			MOrderLine[]lines = ((MOrder) po).getLines();
			for (MOrderLine line:lines){
				total = total.add(convert(po,
						line.getLineNetAmt(), ((MOrder) po).getC_Currency_ID())); 
				}
			}
		else if (po instanceof MInvoice){
			MInvoiceLine[]lines = ((MInvoice) po).getLines();
			for (MInvoiceLine line:lines){ 
				total = total.add(convert(po,
						line.getLineNetAmt(), ((MInvoice) po).getC_Currency_ID())); 
			}
		}
		return total;
	}

	/*return matched line that has same product id as sales/purhase line
	 *should not have more than one product budgeted in same document 
	 *hasQty will add line Qty to figure holder else LineNetAmt
	 */
	private MBudgetPlanLine matchedProductQty(PO po) {
		log.fine("matchedProductQty..");
		for (MBudgetPlanLine matched:matchedProdQtyArray){
			if (po instanceof MOrder){
				MOrderLine[]lines = ((MOrder) po).getLines();
				for (MOrderLine line:lines){
				if (matched.getM_Product_ID()==line.getM_Product_ID()){
					if (hasQty && matched.getQty().compareTo(Env.ZERO)>0)
						figure = figure.add(line.getQtyOrdered());
					else figure = figure.add(convert (po,
							line.getLineNetAmt(), ((MOrder) po).getC_Currency_ID()));
					return matched;
					}
				}
			}
			else if (po instanceof MInvoice){
				MInvoiceLine[]lines = ((MInvoice) po).getLines();
				for (MInvoiceLine line:lines){
					if (matched.getM_Product_ID()==line.getM_Product_ID()){
						if (hasQty)
							figure = figure.add(line.getQtyInvoiced());
						else figure = figure.add(convert (po,
								line.getLineNetAmt(), ((MInvoice) po).getC_Currency_ID()));
						return matched;
					}
				}
			}
		}
		hasProduct = hasQty = false;
		return null;
	}

	/**set variables for matched mjournalline percent or amount
	 * only accounting facts
	 * use matches/whereclause construct from period,bpartner,<flag>account-id,project,activity,campaign
	 * apply budget-config rules to budgetamount compare to totalpurchases for the year.
	 * @param po of gl journalline posting
	 * @return if error string
	 */
	public String eventGLJournal(MJournal po) {
		log.fine("String eventGLJournal(PO po)"); 
		referenceBase = Msg.translate(Env.getCtx(), "Revenue");
		//before posting journal lines		
	    final String whereClause = "GL_Journal_ID=? AND IsActive='Y'";
	    List<MJournalLine> journalLines = new Query(po.getCtx(), MJournalLine.Table_Name, whereClause, po.get_TrxName())
	      .setParameters(po.getGL_Journal_ID())
	      .list();
		 //FETCHING VALIDATING DOCUMENT JOURNAL LINES
		isJournal = true;
		//iterating journal lines
		for (MJournalLine journalLine:journalLines) {
			runtimePO = journalLine; 
			MBudgetPlanLine matchedBudgetLine = matchingBudget(journalLine);
			if (matchedBudgetLine==null) return null;
			checkCreditOrDebit(matchedBudgetLine);
			checkProrata(matchedBudgetLine);  
			sDoc.paramTrimming(matchedBudgetLine, runtimePO);
			//get total of all related accounting facts for the year <with rules applied> 
			BigDecimal totFactAmt = selectAccountingFacts(matchedBudgetLine, previousMonths>0? MORE_EQUAL:EQUAL, 
					presentYear,startMonth, revenueKey);
			totFactAmt = totFactAmt.add(getAmtSource(null, journalLine));
			return budgetAgainstToDate(totFactAmt, matchedBudgetLine, sDoc);
		}
		log.finer("PROCESS GL JOURNAL - NO ERROR STRING RETURNED");
		return null;
	}

	/**
	 * access purchase orders according to calendar and matches
	 * to include invoice (vendors)
	 * @param po
	 * @param operand
	 * @param year
	 * @return
	 */
	public BigDecimal selectPurchasesSales(MBudgetPlanLine po, String operand, String year, int periodValue) {
		log.fine("BigDecimal selectPurchaseOrders(MBudgetPlanLine po, String operand, String year)");
		//sql execution
		referenceBase = Msg.translate(Env.getCtx(), "Revenue");
		BigDecimal totalAmt = Env.ZERO;  
		StringBuffer whereMatchesSQL = new StringBuffer("DocStatus = 'CO' AND " + sDoc.getWhereMatchesSQL().toString()); 
		StringBuffer yearClause = new StringBuffer(" AND (SELECT EXTRACT(ISOYEAR FROM DateOrdered)) "+operand+"'"+year+"'");

		if (previousYears>0 && !isMonthToMonth)
			whereMatchesSQL.append(yearClause); 
		else if (periodValue>0){
			whereMatchesSQL = sDoc.changePeriodToTimestamp(periodValue);
			if (previousMonths>0)
				operand = EQUAL;
			}		
		
		//Check for Sales or Purchasing for SQL set
		if (isPurchasing)  
			isSOTrx = "isSOTrx='N' AND ";
		else
			isSOTrx = "isSOTrx='Y' AND ";
		
		whereMatchesSQL = cleanSQL(whereMatchesSQL);
		List<MOrder> allPurchases = new Query(po.getCtx(), MOrder.Table_Name, isSOTrx + whereMatchesSQL.toString(), po.get_TrxName())
		.setParameters(sDoc.getWhereMatchesIDs())
		.list();
		for (MOrder purchase:allPurchases){
			//if hasProduct getLines check hasQty or else totalAmt only Product in lines
			if (hasProduct) {
				MOrderLine[] lines = purchase.getLines();
				for (MOrderLine line:lines){
					if (po.getM_Product_ID()==line.getM_Product_ID())
						figure = figure.add(getLineProdQtyAmt(line, purchase.getC_Currency_ID()));
				} 
			}
			else
				totalAmt = totalAmt.add(convert (po,
					purchase.getTotalLines(), purchase.getC_Currency_ID()));
		}
		//handle other documents that does not overlap across other documents
		if (budgetCONFIGinstance.isInvoiceToo())
		{
			String whereSQL = whereMatchesSQL.toString().replace("DateOrdered", "DateAcct");
			List<MInvoice> allInvoices = new Query(po.getCtx(), MInvoice.Table_Name, 
				 isSOTrx + whereSQL.toString(), po.get_TrxName())
			.setParameters(sDoc.getWhereMatchesIDs())
			.list();
			for (MInvoice invoice:allInvoices){
				if (invoice.getC_Order_ID()>0 || invoice.getC_Payment_ID()>0) continue;
				//if hasProduct getLines check hasQty or else totalAmt only Product in lines
				if (hasProduct) {
					MInvoiceLine[] lines = invoice.getLines();
					for (MInvoiceLine line:lines){
						if (po.getM_Product_ID()==line.getM_Product_ID())
							figure = figure.add(getLineProdQtyAmt(line,invoice.getC_Currency_ID()));
					} 
				}else
					totalAmt = totalAmt.add(convert (po,
						invoice.getTotalLines(), invoice.getC_Currency_ID()));
			}
		}
		if (hasProduct)
			return figure;//return now, no need to take Payments since they have no product / qty info
		if (budgetCONFIGinstance.isPaymentToo())
		{
			String whereSQL = whereMatchesSQL.toString().replace("DateOrdered", "DateAcct"); 
			List<MPayment> allPayments = new Query(po.getCtx(), MPayment.Table_Name, 
					whereSQL, po.get_TrxName())
			.setParameters(sDoc.getWhereMatchesIDs())
			.list();
			for (MPayment payment:allPayments){
				if (payment.getC_Invoice_ID()>0 || payment.getC_Order_ID()>0) continue;
				if (isPurchasing == payment.getC_DocType().isSOTrx()) continue;
					totalAmt = totalAmt.add(convert (po,
						payment.getPayAmt(), payment.getC_Currency_ID()));
			}
		}
		log.finer("TOTAL AMOUNT: "+totalAmt+" PO: "+po+" OPERAND: "+operand+" YEAR: "+year+" PERIOD VALUE: "+periodValue);
		return totalAmt;
	}
	/** retrieve Prod Qty or just its Amt at Line detail level.
	 * @param po
	 * @return
	 */
	private BigDecimal getLineProdQtyAmt(PO po, int po_currency_id) {
		log.fine("getLineProdQty");
		log.finer("HAS QTY: "+hasQty);
		if (hasQty){
			return (BigDecimal) po.get_Value(MInvoiceLine.COLUMNNAME_QtyEntered);

		}
		else
			return convert (po,
					(BigDecimal) po.get_Value(MOrderLine.COLUMNNAME_LineNetAmt), po_currency_id);
	}

	/**
	 * @param po
	 * @param amount
	 * @param currencyId
	 * @return
	 */
	private BigDecimal convert(PO po, BigDecimal amount, int currencyId) {
		if(currencyId != targetBudget.getC_Currency_ID())
			amount = MConversionRate.convert(po.getCtx(), amount, currencyId,
					targetBudget.getC_Currency_ID(), null, 0, po.getAD_Client_ID(),
					po.getAD_Org_ID());
		return amount;
	} //convert

	/**get total amount for account element from factacct table 
	 * revenueflag if on is for percentagebase either default revenue or specified in budget plan line
	 * according to number of previous-years
	 * according to number of previous-months (beware of alot of swapping around by flags)
	 * @param line
	 * @param operand
	 * @param yearValue
	 * @return total amount
	 */
	public BigDecimal selectAccountingFacts(MBudgetPlanLine line, String operand, String yearValue, int periodValue, String baseAcct) {
		log.fine("BigDecimal selectAccountingFacts(MJournalLine, operand, yearValue, Period_ID)");
		StringBuffer whereClause = new StringBuffer();
		PO po = null;
		ArrayList<Object> params = new ArrayList<Object>();
		referenceBase = Msg.translate(Env.getCtx(), "Revenue");
		if (revenueFlag) { //revenue estimate
			matchedIsCreditAmt = true;
			po = budgetCONFIGinstance;
			whereClause = new StringBuffer("POSTINGTYPE = 'A' AND Account_ID IN (Select C_ElementValue_ID FROM C_ElementValue WHERE Value Like '"+baseAcct+"%')"); 
		}
		else {//swap year revenue logic with accounting element logic
			po = line;
			whereClause = new StringBuffer("POSTINGTYPE = 'A' AND "); 
			if (sDoc.getWhereMatchesSQL()!=null){
				whereClause.append(sDoc.getWhereMatchesSQL());
				params.addAll(sDoc.getWhereMatchesIDs());
			}
		}		
		if (previousYears>0 && !yearValue.isEmpty()){ // periodValue==0
			whereClause.append(" AND C_PERIOD_ID IN (SELECT C_PERIOD_ID FROM C_PERIOD WHERE C_YEAR_ID IN (SELECT C_YEAR_ID FROM C_YEAR WHERE FISCALYEAR "+operand+" ?");
			params.add(yearValue); // 
			//exclude present year
			if (operand.equals(MORE_EQUAL)){
				whereClause.append(" AND FISCALYEAR < ?");  
				params.add(presentYear);
			}
			//closing
			whereClause.append("))");
		}
		else if (previousMonths>0){ //assume Period_IDs sequential till present as last ID.
			if (line!=null && line.getC_Period_ID()>0){
				operand = EQUAL;
				periodValue = line.getC_Period_ID();
			}
			whereClause.append(" AND C_Period_ID "+operand+"?");
			params.add(periodValue);
		}
		else { //Juxtapose PeriodID in C_Period_ID place holder already present in params(WhereMatchesSQL)
			params.set(0, new Integer(periodValue));
		}
		List<MFactAcct> facts = new Query(po.getCtx(), MFactAcct.Table_Name, whereClause.toString(), po.get_TrxName())
		.setParameters(params)
		.list();
		BigDecimal returnAmount = Env.ZERO;
		if (facts.isEmpty()) return Env.ZERO;
		
		for (MFactAcct fact:facts) {
			returnAmount = returnAmount.add(getAmtSource(fact, null));
		}
		if (returnAmount.equals(Env.ZERO)) return Env.ZERO; 
		log.finer(" CALCULATED AMOUNT FROM ACCOUNTING FACTS = "+returnAmount+" PO: "+po+" YEAR VALUE: "+yearValue+ " PERIOD ID: "+periodValue);
		return returnAmount;
	}
 
	/** lookup in budget line
	 *  apply matches to where clause in search of matching budget lines 
	 * @param po
	 * @param matches
	 * @param whereClause
	 * @param whereMatchesIDs 
	 * @return matchedLine
	 */
	private  MBudgetPlanLine lookupBudgetRule(PO po, List<KeyNamePair>matches, StringBuffer whereClause, List<Object> whereMatchesIDs) {
		log.fine("MJournalLine lookupBudgetRule(.");
		//fetch only exact mjournalline that has document's criteria (efficient)
		//where clause from document's main criteria
		//Check Sales or Purchasing for SQL set
		if (isPurchasing)  
			isSOTrx = "isSOTrx='N' AND ";
		else
			isSOTrx = "isSOTrx='Y' AND ";
		//avoid GL journal
		String isSOTrxSQL;
		if (isJournal)
			isSOTrxSQL = "";
		else 
			isSOTrxSQL = isSOTrx;
		List<MBudgetPlanLine> matchedLines = new Query(po.getCtx(), MBudgetPlanLine.Table_Name, isSOTrxSQL + whereClause.toString() 
		+" AND "+MBudgetPlanLine.COLUMNNAME_B_BudgetPlan_ID+"="+targetBudget.getB_BudgetPlan_ID(), po.get_TrxName())
		.setParameters(whereMatchesIDs)
		.setOnlyActiveRecords(true)
		.list();
		//with added criteria first c_period_id, ad_orgdoc_id, c_bpartner_id
		//iterate possible matches but rule out period, org and partner (for purchase) until exact match or null
		MBudgetPlanLine matchedLine = null;
		int counter = 0; //if counter incremented it means extra ambiguous Budget Rules for same match.
		//need to fine tune at order lines level for Qty and Product
		for (MBudgetPlanLine line:matchedLines) { 
			if ((matches.get(0).getKey()!=line.getC_Period_ID()) && (line.getC_Period_ID()!=0)) continue;
			if ((matches.get(1).getKey()!=line.getAD_OrgDoc_ID()) && (line.getAD_OrgDoc_ID()!=0)) continue;
			
			if (!(po instanceof MJournalLine)) { 
				if (matches.get(2).getKey()!=line.getC_BPartner_ID() && (line.getC_BPartner_ID()!=0)) 
					continue;
				if (line.getM_Product_ID()>0) {//GL JournalLine not necessary
					hasProduct = true; 
					if (line.getQty().compareTo(Env.ZERO)>0) //doing as last check to avoid mis-taking matchings
						hasQty = true; //for fine tune matching - need to iterate for Sales/Purchases
					matchedProdQtyArray.add(line);//build special array for any of these two if conditions
 				continue;
				} 
			}
			matchedLine = line;
			counter++;
		}
 		//ambigous due to params within brackets having more records with implicit rules
		if (counter>1) {
			String notice = counter+" AMBIGUOUS RULES = "+whereClause;
			log.warning(notice);
			MMessage msg = MMessage.get(po.getCtx(), "BudgetEvent");
			MNote note = new MNote(po.getCtx(),
					msg.getAD_Message_ID(),
					po.get_ValueAsInt("CreatedBy"),
					po.get_Table_ID(), po.get_ID(),
					"MODEL EVENT:",
					notice,
					po.get_TrxName());
			note.setAD_Org_ID(po.getAD_Org_ID());
			note.saveEx();
		}
		
		//SET matchedIsCreditAmt FLAG - USED IN getAmtSource(..,..)
		if (matchedLine==null)
			return null;

		log.finer("matchedMJournalLine Array: "+matchedLine.toString()+" ISSOTRX: "+isSOTrx+" matchedProdQty Array: "+matchedProdQtyArray);
		return matchedLine;
	}

	/**
	 * set prorata if matched line has period id, i.e. monthly base
	 * @param matchedLine
	 */
	public void checkProrata(MBudgetPlanLine matchedLine) {
		log.fine("checkProrata- before: "+isProrata);
		isProrata = budgetCONFIGinstance.isProrata();
		 if (matchedLine.getC_Period_ID()>0)
			 isProrata = true;
		log.finer("after: "+isProrata+" matchedLine.PeriodID: "+matchedLine.getC_Period_ID()); 
	}

	/**
	 * helper method to return amount either credit or debit side; 
	 * for fact, journalline or mjournalline
	 * @param fact
	 * @param jline
	 * @return debit or credit amount
	 */
	private BigDecimal getAmtSource(MFactAcct fact, PO jline) {
		log.fine("BigDecimal getAmtSource(MFactAcct fact, MJournalLine jline)");
		log.finer("FACT: "+fact+" CREDIT? "+(matchedIsCreditAmt? "Y":"N")+"JournalLINE: "+jline);
		if (fact!=null) {
			if (matchedIsCreditAmt)
				return fact.getAmtAcctCr();
			else 
				return fact.getAmtAcctDr();
		}
		else { 
			if (matchedIsCreditAmt)
				return (BigDecimal) jline.get_Value(MBudgetPlanLine.COLUMNNAME_AmtAcctCr);
			else 
				return (BigDecimal) jline.get_Value(MBudgetPlanLine.COLUMNNAME_AmtAcctDr);
		}
	}

	/**this returns matched budget amt or % with variables for matches
	 *	matched is credit or debit amt,
	 * @param poLine
	 * @return matchedMJournalLine
	 */
	private MBudgetPlanLine matchingBudget(PO poLine) {
		log.fine("MBudgetPlanLine matchingProcess(PO poLine)"); 
		sDoc = new SQLfromDoc(runtimePO, previousMonths);
		sDoc.setWhereMatches();
		MBudgetPlanLine matchedMJournalLine = lookupBudgetRule(poLine, sDoc.getWhereMatches(), sDoc.getWhereMatchesSQL(), sDoc.getWhereMatchesIDs());
		return matchedMJournalLine;
	}
	
	/** difference between last and start, divided by start, multiply last, add to last.
	 *  last amt not zero
	 * @param line
	 * @return
	 */
	private BigDecimal rateIncreaseFormula(MBudgetPlanLine line, SQLfromDoc sDoc, String baseAcct) {
		log.fine("rateIncreaseFormula");
		BigDecimal returnAmount;
		BigDecimal startAmt = Env.ZERO;
		BigDecimal lastAmt = Env.ZERO;
		if (runtimePO instanceof MJournalLine || revenueFlag){
			startAmt = selectAccountingFacts(line, EQUAL, startYear, startMonth, baseAcct);
			lastAmt = selectAccountingFacts(line, EQUAL, pastYear, pastMonth, baseAcct);
		}
		else {
			startAmt = selectPurchasesSales(line, MORE_EQUAL, startYear, startMonth);
			lastAmt = selectPurchasesSales(line, MORE_EQUAL, pastYear, pastMonth);
		}
		try{
			if (lastAmt.equals(Env.ZERO)) 
				throw new AdempiereException("LAST YEAR/MONTH HAS NO AMOUNT");
			} catch(AdempiereException e){
				log.warning(e.toString());
				lastAmt = startAmt = Env.ONE;
			}
		BigDecimal buffer = lastAmt.subtract(startAmt);
		BigDecimal rate = buffer.divide(startAmt,2,RoundingMode.HALF_EVEN);
		returnAmount = rate.multiply(lastAmt).add(lastAmt);
		log.finer("RATE: "+returnAmount);
		return returnAmount;
	}
	
	/** set budget percent or budget amount or budget Qty
	 * @param matchedMJournalLine
	 */
	private void matchedType(MBudgetPlanLine matchedMJournalLine) {
		log.fine("matchedResult(MBudgetPlanLine matchedMJournalLine)");
		//
		budgetPercent = matchedMJournalLine.getPercent();
		if (budgetPercent.compareTo(Env.ZERO)==0){
			budgetAmount = getAmtSource(null, matchedMJournalLine);			
		}
		if (hasQty)
			budgetAmount = matchedMJournalLine.getQty();
		log.finer("BUDGETAMOUNT: "+budgetAmount);
	}

	/**
	 * set iscreditordebit
	 */
	public void checkCreditOrDebit(MBudgetPlanLine line){
		log.fine("checkCredirOrDebit");
		if (line.getPercent().compareTo(Env.ZERO)==0){
			if (line.getAmtAcctDr().compareTo(Env.ZERO)>0)
				matchedIsCreditAmt = false;
			else 	
				matchedIsCreditAmt = true;
		}
		log.finer("JournalLine: "+line+(matchedIsCreditAmt?" is CREDIT":" is DEBIT"));
	}
	
	/**
	 * check budget's percent against revenue or amount against total-amount to-date
	 * @param todateAmount
	 * @return
	 */
	private String budgetAgainstToDate(BigDecimal todateAmount, MBudgetPlanLine matchedBudgetLine, SQLfromDoc sDoc) {
		log.info("budgetAgainstToDate(BigDecimal todateAmount.. matchedBudgetLine.. SQLfromDoc)");
		BigDecimal baseAmt = RevenueEstimate;
		if (matchedBudgetLine!=null)  
			matchedType(matchedBudgetLine);
		//reference base checking - baseamt get alternative revenue

		if (matchedBudgetLine.getPercent().compareTo(Env.ZERO)>0 && matchedBudgetLine.getPercentageBase_ID()>0){
			//Take from alternative AccountID, later Reference if any
			baseAmt = percentageBase(matchedBudgetLine, sDoc);  
		}
		if (hasQty && budgetPercent.compareTo(Env.ZERO)==0)
			baseAmt = matchedBudgetLine.getQty();
		//prorata rule here - for matchedline with period id - monthly base
		if (isProrata) {
			baseAmt = baseAmt.divide(new BigDecimal(12),2);
			if (budgetAmount!=null)
				budgetAmount = budgetAmount.divide(new BigDecimal(12),2);
		}
		
		if (budgetPercent.compareTo(Env.ZERO)>0) {
			if (baseAmt.multiply(budgetPercent).divide(Env.ONEHUNDRED,2).compareTo(todateAmount)<0 
					|| budgetCONFIGinstance.isDebugMode()) {
				BigDecimal diff = baseAmt.multiply(budgetPercent).divide(Env.ONEHUNDRED,2).subtract(todateAmount);				
				return outputMessage(matchedBudgetLine,budgetPercent,Env.ZERO,diff,todateAmount,baseAmt);
			}
		}
		else if (budgetAmount.compareTo(Env.ZERO)>0) {
			if (budgetAmount.compareTo(todateAmount)<0 || budgetCONFIGinstance.isDebugMode()) {
				BigDecimal diff = budgetAmount.subtract(todateAmount);
				return outputMessage(matchedBudgetLine,Env.ZERO,budgetAmount,diff,todateAmount,baseAmt);
			}  
		}
		else //if both Percent and Amount are ZERO
			return outputMessage(matchedBudgetLine, baseAmt, baseAmt, Env.ZERO, todateAmount, baseAmt);
		log.finer("WITHIN BUDGET - TODATE AMT: "+todateAmount+" BASEAMT: "+baseAmt+" ISPRORATA: "+(isProrata? "TRUE ":"FALSE "+" BUDGETAMOUNT: ")+budgetAmount+" BUDGETPERCENT: "+budgetPercent);
		return null;//no error
	}
	/**
	 * Format message string for pop up or Notice record
	 * @param matchedBudgetLine
	 * @param budgetPercent
	 * @param budgetAmount
	 * @param diff
	 * @param todateAmount
	 * @param baseAmt
	 * @return
	 */
	private String outputMessage(MBudgetPlanLine matchedBudgetLine, BigDecimal budgetPercent, BigDecimal budgetAmount, 
			BigDecimal diff, BigDecimal todateAmount, BigDecimal baseAmt) {
		diff = diff.setScale(2, RoundingMode.UP);
		baseAmt = baseAmt.setScale(2, RoundingMode.UP);
		budgetPercent = budgetPercent.setScale(2, RoundingMode.UP);
		budgetAmount = budgetAmount.setScale(2, RoundingMode.UP);
		todateAmount = todateAmount.setScale(2, RoundingMode.UP);
		String sign = " "+Msg.translate(Env.getCtx(), diff.compareTo(Env.ZERO)>0? "Balance":"Short")+" ";
		String mode = Msg.translate(Env.getCtx(), budgetCONFIGinstance.isDebugMode()? "DebugLog":"Live")+" ";
		String percentAmt = "";
		String productDesc="";
		String description="";
		if (matchedBudgetLine.getDescription()!=null)
			description = matchedBudgetLine.getDescription();
		if (hasProduct)
			productDesc = " "+Msg.translate(Env.getCtx(), "Product")+": "+matchedBudgetLine.getM_Product().getName();
		if (budgetPercent.compareTo(Env.ZERO)>0)
			percentAmt = " ("+budgetPercent+"% OF "+baseAmt+" "+referenceBase+") ";
		else if (hasQty)
			percentAmt = " (Budget Qty: "+budgetAmount+") ";
		else percentAmt = " (Budget Amount: "+budgetAmount+") ";
		String salesPurchase = "";
		if (!isJournal)
			salesPurchase = " "+Msg.translate(Env.getCtx(), matchedBudgetLine.isSOTrx()? "Sales":"Purchase")+" ";
		
		return mode+salesPurchase+description+" "+Msg.translate(Env.getCtx(), "To-Date")+": "+todateAmount
				+sign+diff+percentAmt+(isProrata? Msg.translate(Env.getCtx(),"Prorata")+" - ":"")
				+Msg.translate(Env.getCtx(), "Trend")+" "+budgetCONFIGinstance.getBudgetTrend()+productDesc;
	}

	/**
	 * alternative from revenue - reference base for each budget plan line percentage
	 * if planLineReference - apply byReference (more formula)
	 * @param matchedBudgetLine
	 * @return baseAmt
	 */
	public BigDecimal percentageBase(MBudgetPlanLine matchedBudgetLine, SQLfromDoc sDoc) {
		log.fine("percentageBase(MBudgetPlanLine matchedBudgetLine, SQLfromDoc sDoc)");
		if (previousYears>99) //hard set revenue - must be below 100 to use alternative
			return RevenueEstimate;
		revenueFlag = true; //turn on
		BigDecimal baseAmt;
		MElementValue account = new MElementValue(matchedBudgetLine.getCtx(),matchedBudgetLine.getPercentageBase_ID(),matchedBudgetLine.get_TrxName());
		String baseAcct = account.getValue();
		originalBase = selectAccountingFacts(matchedBudgetLine, MORE_EQUAL, startYear, startMonth, baseAcct);
		originalBase = originalBase.setScale(2,RoundingMode.HALF_EVEN);
		baseAmt = byReference(matchedBudgetLine, originalBase);
		baseAmt = budgetTrend(matchedBudgetLine,baseAmt,baseAcct);
		revenueFlag = false;//turn off
		referenceBase =  account.getName().toUpperCase();
		log.finer("BASE AMT from PercentageBase: "+baseAmt);
		return baseAmt;
	}
	
	private BigDecimal byReference(MBudgetPlanLine matchedBudgetLine,
			BigDecimal baseAmt) {
		log.fine("BigDecimal byReference(MBudgetPlanLine baseAmt)");
		if (previousYears>99) //hard set revenue - must be below 100 to use alternative
			return RevenueEstimate;

		List<MBudgetReference> refs = MBudgetReference.get(matchedBudgetLine);
		if (refs==null) return baseAmt;
		//baseAmt = iterateRefSet(matchedBudgetLine, baseAmt);
		for (MBudgetReference ref:refs){
			baseAmt = iterateRefSet(ref, baseAmt);
		}
		log.finer("BASE AMT from REFERENCE: "+baseAmt);
		return baseAmt; 
	}

	private BigDecimal iterateRefSet(MBudgetReference ref, BigDecimal baseAmt) {
		if (!ref.isActive()) return baseAmt;
		String operator = ref.getOperator();
		if (operator==null) operator = "";
		int account = ref.getAccount_ID();
		int from = ref.getPeriodFrom_ID();
		int to = ref.getPeriodTo_ID();
		int C_Project_ID = ref.getC_Project_ID();
		StringBuffer whereClause = new StringBuffer();
		ArrayList<Object> params = new ArrayList<Object>();
		if (account>0){
			whereClause.append("Account_ID=?");
			params.add(account);
		}
		
		if (C_Project_ID>0){
			if (whereClause.length()>0) 
				whereClause.append(" AND ");
			whereClause.append("C_Project_ID =?");
			params.add(C_Project_ID);
		}
		if (from>0){
			if (whereClause.length()>0) 
				whereClause.append(" AND ");
				
			whereClause.append("C_Period_ID >=?");
			params.add(from);
		}	
		if (to>0){
			if (whereClause.length()>0) 
				whereClause.append(" AND ");
			whereClause.append("C_Period_ID <=?");
			params.add(to);
		}	
		
		BigDecimal amount = Env.ZERO;
		if (account>0){
			List<MFactAcct> facts = new Query(Env.getCtx(), MFactAcct.Table_Name,whereClause.toString(),ref.get_TrxName())
			.setParameters(params)
			.list();
			for (MFactAcct fact:facts){
				amount = amount.add(getAmtSource(fact, null));
			}
		}		
		if (amount.compareTo(Env.ZERO)==0)
			amount = ref.getAmount();
		if (operator.equals(MBudgetReference.OPERATOR_Plus))
			baseAmt = baseAmt.add(amount);
		else if (operator.equals(MBudgetReference.OPERATOR_Minus))
			baseAmt = baseAmt.subtract(amount);
		else if (operator.equals(MBudgetReference.OPERATOR_Divide))
			baseAmt = baseAmt.divide(amount,2).setScale(2);
		else if (operator.equals(MBudgetReference.OPERATOR_Multiply))
			baseAmt = baseAmt.multiply(amount);
		else baseAmt = amount; //no operator reset the baseAmt to new amount
		
		ref.setAmount(amount);
		ref.setResult(baseAmt);
		ref.saveEx(ref.get_TrxName());
		log.finer("BASE AMT from Iterate Reference Array: "+baseAmt);		
		return baseAmt;
	}

	/**
	 * month on month list created as array of period ids across previous years
	 * FOR processMonthToMonth(MJournalLine)
	 * @param line
	 * @return false if query null
	 */
	private int createPeriodPointers(MBudgetPlanLine line){
		log.fine("int createMonthToMonthList(MBudgetPlanLine line)");
		firstPeriodOfYear = (MPeriod.getFirstInYear(line.getCtx(), setTimeToZero(line.getCreated()), line.getAD_Org_ID())).getC_Period_ID();
		//if previousYears = 0, startYear will be presentYear
		List<MYear> years = new Query(line.getCtx(),MYear.Table_Name,MYear.COLUMNNAME_FiscalYear+" >= ?",line.get_TrxName())
		.setParameters(startYear)
		.list();
		if (years==null) return 0;
		nowPeriodNo = presentMonth - firstPeriodOfYear; //Difference of IDs = exact PeriodNo (this month subtract first month).
		for (MYear year:years){			 
			//MAIN
			if (year.getFiscalYear().equals(presentYear) && previousMonths==0)
				continue;
			List<MPeriod> periodsMOM = new Query(line.getCtx(), MPeriod.Table_Name, MPeriod.COLUMNNAME_C_Year_ID+"=?", line.get_TrxName())
			.setParameters(year.getC_Year_ID())
			//.setOrderBy(MPeriod.COLUMNNAME_C_Period_ID)
			.list();
			if (periodsMOM==null) throw new AdempiereException("periodsMOM==null in CREATE MONTH TO MONTH LIST");
			if (periodsMOM.size()!=12) log.warning("PERIOD.SIZE() IS NOT 12 !?");
 
			// array of period IDs, accessed by logical order of sets of 12. To access i.e. Nth month of the year, loop in *12
 			for (MPeriod period:periodsMOM) {
				mom.add(period.getC_Period_ID());
				//if (year.getFiscalYear().equals(presentYear))
					//presentPeriods.add(period.getC_Period_ID());//for use in writing new target budget lines
			}
 			if (previousMonths>0) { //from present month backwards for previousMonths
 				for (int a=0; a < nowPeriodNo-previousMonths; a++){ //Month backwards previousMonths is spot before which not needed.
 					mom.remove(0); //keep removing from top of stack until previousMonths
 				}
 			}
		}
		log.finest("MonthToMonth Array: "+mom.toString()+" FIRST PERIOD OF YEAR: "+firstPeriodOfYear);
		return firstPeriodOfYear;
	}
	 
	/**
	 *take history into account - average or upper or lower
	 * @param line
	 * @param totalAmt
	 * @return
	 */
	private BigDecimal alignPreviousFigure(BigDecimal newAmt, BigDecimal oldAmt) { 
		log.fine("BigDecimal alignPreviousFigure(BigDecimal newAmt, BigDecimal oldAmt)");
	 	if (ResetBudget.p_AlignPrevious==null) 
			return newAmt;
 		if (ResetBudget.p_AlignPrevious.equals("A")) 
			return newAmt.add(oldAmt).divide(new BigDecimal(2),2);
		if (ResetBudget.p_AlignPrevious.equals("H"))
			return newAmt.max(oldAmt);
		if (ResetBudget.p_AlignPrevious.equals("L"))
			return newAmt.min(oldAmt);	
		return newAmt;
	}
	/**
	 * Removing AND AND doubles
	 * @param whereMatchesSQL
	 * @return
	 */
	private StringBuffer cleanSQL(StringBuffer whereMatchesSQL) {
		String matchRemoved = whereMatchesSQL.toString().replace("AND   AND", "AND");
		return new StringBuffer(matchRemoved);	 
	}
	/**
	 * YearOnYear init of MonthOnMonth Array for PeriodID reference pointers
	 * @param period_ID
	 * @param model
	 * @return
	 */
	public BigDecimal yearOnYear(int period_ID, MBudgetPlanLine model){
		BigDecimal value = Env.ZERO;
		if (period_ID==0) return value; 
		if (mom==null || mom.isEmpty() || firstPeriodOfYear==0) {		
			firstPeriodOfYear = createPeriodPointers(model);
		} 
		return getYOYmth(model);
	}
	/**
	 * Get YearOnYear for the selected month (planPeriodNo) over range of PreviousYears
	 * From GL or Sales/Purchases history
	 * @param model
	 * @return
	 */
	private BigDecimal getYOYmth(MBudgetPlanLine model) {
		int planPeriodNo = getPeriodNo(model)-1;//Planning Month of the year used for same months in previous years
		BigDecimal value = Env.ZERO;
		int y=12;
		if (previousMonths>0)
			y=1;
		for (int i=planPeriodNo;i<mom.size();i+=y){
			//getMthNo detail
			int periodID = mom.get(i);
			if (model.getAccount_ID()>0)
				value = value.add(selectAccountingFacts(model, EQUAL, "", periodID, ""));
			else if (!(runtimePO instanceof MJournalLine)) {
				value = selectPurchasesSales(model, EQUAL, "", periodID);
			}
			else
				throw new AdempiereException("runtimePO error - contact developer");
		}
		return value;
	}

	private int getPeriodNo(MBudgetPlanLine model) {
		//
		MPeriod period = new MPeriod(Env.getCtx(),model.getC_Period_ID(), model.get_TrxName());
		return period.getPeriodNo();
	}

}
