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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.I_C_Order;
import org.compiere.model.I_GL_Journal;
import org.compiere.model.MFactAcct;
import org.compiere.model.MInvoice;
import org.compiere.model.MJournal;
import org.compiere.model.MJournalLine;
import org.compiere.model.MOrder;
import org.compiere.model.MPayment;
import org.compiere.model.MPeriod;
import org.compiere.model.MYear;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;

public class BudgetUtils{
	public BudgetUtils(){
	}
	 
	private static CLogger log = CLogger.getCLogger(BudgetUtils.class);
	public static MBudgetConfig budgetCONFIGinstance;
	public static MJournal targetBudget = null;
	public static PO runtimePO;
	public static BigDecimal RevenueEstimate;
	private static final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
 
	private static int previousYears; 
	private static String presentYear;
	private static String startYear;
	private static String pastYear;
	private static int previousMonths;
	private static int presentMonth;
	private static int startMonth;
	private static int pastMonth;
	private static int PeriodNo;
	private static int firstPeriodOfYear;
	
	private static boolean isMonthToMonth;
	private static boolean isProrata = false;
	private static String PRORATA=""; 
	private static List<Integer>mom = new ArrayList<Integer>(); 
	private static final String MORE_EQUAL = ">=";
	private static final String EQUAL = "=";
	
	private static List <KeyNamePair> whereMatches;
	private static StringBuffer whereMatchesSQL;
	private static ArrayList<Object> whereMatchesIDs;
	
	private static BigDecimal budgetPercent;
	private static BigDecimal budgetAmount;
	private static boolean matchedIsCreditAmt = true;
	
	/**
	 * LOAD CONFIG SETTINGS :
	 * - ONE-TIME SETUP UPDATE OF REVENUE TOTAL
	 * @param po
	 */
	public static void initBudgetConfig(PO po){
 		log.fine("public static void initBudgetConfig(");
		budgetCONFIGinstance = new Query(po.getCtx(), MBudgetConfig.Table_Name, "", po.get_TrxName())
		.firstOnly(); 
		//IF BUDGET MODULE NOT ACTIVE
		if (budgetCONFIGinstance==null) throw new AdempiereException("NULL BUDGETCONFIG - YOU CAN STOP BUDGET MODULE");
		if (!budgetCONFIGinstance.isActive()) 
			throw new AdempiereException("NOT ACTIVE BUDGETCONFIG - YOU CAN STOP BUDGET MODULE");
		//STATIC INSTANCE OF SINGLE TARGET BUDGET
 		targetBudget = new Query(po.getCtx(), MJournal.Table_Name, "PostingType='B' AND "+MJournal.COLUMNNAME_GL_Budget_ID+"=?", po.get_TrxName())
		.setParameters(100) //TARGET BUDGET ID HARD CODED
 		.setOnlyActiveRecords(true)
		.first();	 
 		log.finer("INITBUDGETCONFIG - TARGETBUDGET: "+targetBudget);
	}
	
	/**
	 * SETTERS FOR GENERATE BUDGET PROCESS - TAKING FROM PARAMS THERE
	 * @param previousYears
	 * @param MonthToMonth
	 * @param PreviousMonths 
	 * @param Prorata
	 */
	public static void setInstance(BigDecimal previousYears,String MonthToMonth,BigDecimal PreviousMonths,String Prorata) {
		log.fine("public static void setInstance(...)");
		budgetCONFIGinstance.setGL_Budget_Previous_Year(previousYears);
		budgetCONFIGinstance.setMonthToMonth(new Boolean(MonthToMonth));
		budgetCONFIGinstance.setGL_Budget_Previous_Month(PreviousMonths);
		budgetCONFIGinstance.setProrata(new Boolean(Prorata));
		log.finer("SET INSTANCE Previous years:"+previousYears+" MonthToMonth: "+MonthToMonth+" PreviousMonths: "+PreviousMonths+" Prorata: "+Prorata);
	}
	/**
	 * CHECK PROPERTIES OF BUDGETCONFIGINSTANCE
	 * OBTAIN START/PAST YEAR VALUES ACCORDING TO PREVIOUS YEARS
	 * CONVERT PREVIOUS-MONTHS TO PERIOD IDS
	 */
	public static void setupCalendar(PO po) {
		log.fine("public static void setupCalendar(PO po)");
		previousYears = budgetCONFIGinstance.getGL_Budget_Previous_Year().intValue();
		previousMonths = budgetCONFIGinstance.getGL_Budget_Previous_Month().intValue();
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
			isMonthToMonth = false;
		}
		if (previousYears > 0){
			previousMonths = 0;
			startMonth = 0;
			pastMonth = 0; 

			//starting year
			cal = Calendar.getInstance();
			cal.add(Calendar.YEAR, -previousYears);
			startYear = yearFormat.format(cal.getTime());

		}
		
		//IF PRORATA THEN CONFIG MONTHS VOID
		if (budgetCONFIGinstance.isProrata()) {
			isProrata = true;
			isMonthToMonth = false;
			previousMonths = 0;
		}
		//IF MONTH ON MONTH
		else if (budgetCONFIGinstance.isMonthToMonth()) {
			isMonthToMonth = true; 
			//CANNOT PRORATA
			isProrata = false;
		}
 
		//Month PERIODS IDS assumed to be consecutive.
		//last month
		pastMonth = presentMonth - 1;
		//starting month
		startMonth = presentMonth - previousMonths;
		

		log.finer("ISPRORATA:"+isProrata+" isMonthToMonth:"+isMonthToMonth);
	}

	/**
	 * ONE TIME SETUP REVENUE OVER YEARS RANGE OR MONTHS RANGE, APPLY BUDGET TREND
	 * @return REVENUE ESTIMATE
	 */
	public static BigDecimal revenueEstimate() {
		log.fine("public static BigDecimal revenueEstimate()");
		
		//PREVIOUS YEARS
		if (previousYears==0)  //NO ACTION
				RevenueEstimate = Env.ZERO;					 
		else if (previousYears<100)
			//for previousYears == 1 startYear is effectively pastYear
			RevenueEstimate = selectAccountingFacts(null, MORE_EQUAL, startYear, 0);
		
		//PREVIOUS MONTHS
		if (previousMonths<12 && previousMonths>0)
			//for previousMonths == 1 starting month is effectively pastMonth
			RevenueEstimate = selectAccountingFacts(null, MORE_EQUAL, null, startMonth);
		
		log.finer("REVENUE ESTIMATE: "+RevenueEstimate.toString());				
		return RevenueEstimate; 
	}
	
	/**
	 *  APPLY BUDGET TREND LIST: AVERAGE, AVERAGE+LAST, PROGRESSIVE, ACCUMULATIVE,
	 * 	YEAR-TO-DATE, AND APPLY PRORATA /12.
	 * @param line
	 * @param 
	 * @return REVENUE AMOUNT
	 */
	public static BigDecimal budgetTrend(BudgetLine line, BigDecimal returnAmount) {		
		log.fine("	private static BigDecimal budgetTrend(BudgetLine line, BigDecimal returnAmount, int Period_ID)");
		//returnAmount is default Accumulative Amount
		//previous Months/Years = 1 , solved by design in RevenueEstimate as past or previous 1.
		BigDecimal average = Env.ZERO;
		if (previousMonths>0 && previousYears>0) throw new AdempiereException("BUDGET TREND ERROR: MONTHS/YEARS CONFLICT");
	
		if (previousMonths>1){
			average = returnAmount.divide(new BigDecimal(previousMonths),2);
		}
		else if (previousYears>1)
			//AMOUNT AVERAGE ACROSS RANGE OF YEARS
			average = returnAmount.divide(new BigDecimal(previousYears),2);
			
			//YEAR TO-DATE
			else if (budgetCONFIGinstance.getBudgetTrend().equals("T")) {
				previousYears = 1; //for GenerateBudget selectPurchases
				previousMonths = PeriodNo;
				if (runtimePO instanceof MJournal)
					returnAmount = selectAccountingFacts(line, EQUAL, presentYear, 0);
				else 
					returnAmount = selectPurchases(line, MORE_EQUAL, presentYear, 0);
			}		
		
		//TREND AVERAGE
		if (budgetCONFIGinstance.getBudgetTrend().equals("A"))
			returnAmount = average;
		
		//AVERAGE+LAST = ADD AVERAGE TO PREVIOUS MONTH
		else if (budgetCONFIGinstance.getBudgetTrend().equals("L")){
			BigDecimal lastAmt = Env.ZERO;
			if (runtimePO instanceof MJournal)
				lastAmt = selectAccountingFacts(line, EQUAL, pastYear, pastMonth);
			else
				lastAmt = selectPurchases(line, MORE_EQUAL, pastYear, pastMonth);
			returnAmount = lastAmt.add(average);
			}			
		//APPLY RATE OF CHANGE OVER RANGE TO LAST MONTH'S 
		else if (budgetCONFIGinstance.getBudgetTrend().equals("P")) { 
			returnAmount = rateIncreaseFormula(line);
		}
	
		return returnAmount;
	}

	/**
	 * SET SOURCE AMOUNT TO CREDIT OR DEBIT
	 */
	public static BudgetLine setAmtSource(BudgetLine line, BigDecimal amt){
		log.fine("public static BudgetLine setAmtSource(BudgetLine line, BigDecimal amt)");
		if (amt.equals(Env.ZERO))
			line.setAmtAcctDr(Env.ZERO);//is a clear set request from amt arg
		if (matchedIsCreditAmt)
			line.setAmtSourceCr(amt);
		else line.setAmtSourceDr(amt);
		return line;
	}

	public static void clearWhereMatches(){
		log.fine("private static void clearWhereMatches()");
		whereMatches =  new ArrayList<KeyNamePair>();
		whereMatchesSQL = new StringBuffer();
		whereMatchesIDs = new ArrayList<Object>();
	}

	/**
	 * Purchasing Documents Event Validation
	 * USE MATCHES/WHERECLAUSE CONSTRUCT FROM PERIOD OR ACCOUNT-ID,PROJECT,ACTIVITY,CAMPAIGN,BPARTNER
	 * APPLY BUDGET-CONFIG RULES TO BUDGETAMOUNT COMPARE TO TOTALPURCHASES FOR THE YEAR.
	 * @param po
	 * @return IF ERROR STRING
	 */
	public static String eventPurchases(PO po) {
		log.fine("public static String processPurchaseOrder(PO po)");
		runtimePO = po;  
		boolean isPurchasing = true;
		if (po instanceof MOrder) {
			MOrder purchase = new MOrder(Env.getCtx(), po.get_ID(), po.get_TrxName());	
			if (purchase.isSOTrx()) 
				isPurchasing = false;
			po = purchase;
		}
		else if (po instanceof MInvoice){
			MInvoice invoice = new MInvoice(Env.getCtx(), po.get_ID(), po.get_TrxName());
			if (invoice.isSOTrx() || invoice.getC_Order_ID()>0) 
				isPurchasing = false;
			po = invoice;
		}
		else if (po instanceof MPayment){
			MPayment payment = new MPayment(Env.getCtx(), po.get_ID(), po.get_TrxName());	
			if (payment.getC_DocType().isSOTrx() || payment.getC_Order_ID()>0 || payment.getC_Invoice_ID()>0){
				isPurchasing = false;
				po = payment;
			}
		}
		//ONLY PURCHASING DOCTYPES

		if (isPurchasing) { 		
			BudgetLine matchedBudgetLine = matchingBudget(po);
			if (matchedBudgetLine!=null){ 
				paramTrimming(matchedBudgetLine, false);
				
				BigDecimal totalPurchases = selectPurchases(po, EQUAL, presentYear, previousMonths);
				totalPurchases = totalPurchases.add((BigDecimal) po.get_Value(MOrder.COLUMNNAME_GrandTotal));
				//IF PRORATA TAKE ONLY PRESENT MONTH(S) PURCHASES
				//IF ISMONTHONMONTH TAKE SAME PERIODS FROM PREVIOUS YEARS AND APPLY AVERAGE/PRGRESSIVE
				return budgetAgainstToDate(totalPurchases);
			}
		}
		log.finer("PROCESS PURCHASE ORDER - NO ERROR STRING RETURNED");
		return null;//no error
	}

	/**SET VARIABLES FOR MATCHED BUDGETLINE PERCENT OR AMOUNT
	 * ONLY ACCOUNTING FACTS
	 * USE MATCHES/WHERECLAUSE CONSTRUCT FROM PERIOD,BPARTNER,<FLAG>ACCOUNT-ID,PROJECT,ACTIVITY,CAMPAIGN
	 * APPLY BUDGET-CONFIG RULES TO BUDGETAMOUNT COMPARE TO TOTALPURCHASES FOR THE YEAR.
	 * @param po of GL JournalLine posting
	 * @return IF ERROR STRING
	 */
	public static String eventGLJournal(PO po) {
		log.fine("public static String processGLJournal(PO po)");
		runtimePO = po; 
		//BEFORE POSTING JOURNAL LINES
		List<MJournalLine> journalLines = BudgetLine.getJournalLines((MJournal)po); //FETCHING VALIDATING DOCUMENT JOURNAL LINES
			
		//ITERATING JOURNAL LINES
		for (MJournalLine journalLine:journalLines) {
			BudgetLine matchedBudgetLine = matchingBudget(journalLine);
			if (matchedBudgetLine==null) return null;
			paramTrimming(matchedBudgetLine, false);
			//GET TOTAL OF ALL RELATED ACCOUNTING FACTS FOR THE YEAR <WITH RULES APPLIED>
			String YEAR = yearFormat.format(Calendar.getInstance().getTime());
			BigDecimal totFactAmt = selectAccountingFacts(matchedBudgetLine, EQUAL, YEAR,0);
			totFactAmt = totFactAmt.add(getAmtSource(null, journalLine));
			return budgetAgainstToDate(totFactAmt);
		}
		log.finer("PROCESS GL JOURNAL - NO ERROR STRING RETURNED");
		return null;
	}

	/**
	 * GENERATE PURCHASING BUDGET, PREVIOUS YEARS, MONTH TO MONTH, PREVIOUS MONTHS
	 * @param line
	 * @param newbudget
	 * @return true
	 */
	public static int generateBudgetLine(BudgetLine line, MJournal newbudget){
		log.fine("public static boolean generateBudgetLines(BudgetLine line, MJournal newbudget)");
		int count = 0;
		BigDecimal baseAmt = Env.ZERO;
		int periodSerial = firstPeriodOfYear;
		int Period_ID = 0;
		String operand = EQUAL;
		BigDecimal totalAmt = Env.ZERO; 
		int y = 12;
		if (mom==null || mom.isEmpty() || firstPeriodOfYear==0) {
			firstPeriodOfYear = createPeriodPointers(line);
		}
		boolean purchaseGenerate = false;
		if (runtimePO instanceof MOrder) {
			purchaseGenerate = true;
			//get all matching POs during previous range, apply trend if not percent
			PO.copyValues(line, runtimePO);
			if (line.getAD_OrgDoc_ID() > 0)
				runtimePO.setAD_Org_ID(line.getAD_OrgDoc_ID());
		} 
		checkCreditOrDebit(line);
		setWhereMatches(runtimePO); 
		paramTrimming(line, purchaseGenerate);

		int momSize = mom.size();
		if (previousMonths>0) { //no longer year cycle
			y=1;
			momSize = mom.size()+PeriodNo-12; //truncate off as after this month has no records
			Period_ID = previousMonths;
			isProrata = false;
		}
		if (!isMonthToMonth){
			y=1;
			operand = MORE_EQUAL;
			momSize = 0;
		}
		for (int cnt=0;cnt<y;cnt++){			
			for (int periodCnt=cnt;periodCnt<=momSize;periodCnt+=y){ //increments by 12 to jump into yearly loops
				if (Period_ID==0)
					Period_ID = mom.get(periodCnt);		
				if (!(runtimePO instanceof MJournal)) 
					totalAmt = totalAmt.add(selectPurchases(line, operand, startYear, Period_ID));
				else 
					totalAmt = totalAmt.add(selectAccountingFacts(line, operand, startYear, Period_ID)); 		 
			}
			if (!totalAmt.equals(Env.ZERO)) { 
				BudgetLine newline = new BudgetLine(newbudget);
				copyValues(line, newline);
				totalAmt = budgetTrend(newline,totalAmt); 
				if (previousMonths==0) //only for previousYears case, more definite check
					checkProrata(line);
				if (line.getPercent().compareTo(Env.ZERO)==0 && GenerateBudget.p_Percent.equals("N")){
	 	 				totalAmt = historicalCondition(totalAmt, getAmtSource(null, line));
					newline = setAmtSource(newline, totalAmt); 
					newline.setPercent(Env.ZERO);
	      		}
				else {
					//set as percent
					if (isProrata) 
						baseAmt = RevenueEstimate.divide(new BigDecimal(12),2);//PRORATA IF MONTH BASE
					else
						baseAmt = RevenueEstimate;
					BigDecimal percentage = totalAmt.divide(baseAmt,2).multiply(Env.ONEHUNDRED);
	 				percentage = historicalCondition(percentage, line.getPercent());
					newline.setPercent(percentage);  
					newline = setAmtSource(newline, Env.ZERO);
	       	 		}
	
				newline.setC_Period_ID(presentMonth);//a Period base targetBudgetLine !isMonthToMonth
				if (isMonthToMonth) {
					newline.setC_Period_ID(periodSerial);
					periodSerial++;
				}		
				else if (line.getC_Period_ID()==0)
					newline.setC_Period_ID(0); 
 
	       	 	newline.saveEx(newbudget.get_TrxName());
	   		
	       	 	log.finer("GENERATE NEW BUDGETLINE "+newline+" - AMOUNT: "+totalAmt+" OR PERCENT: "+newline.getPercent()); 	
	       	 	totalAmt = Env.ZERO; //init for next period
	       	 	count++;
			}
		}
		return count;
	}

	private static void copyValues(BudgetLine line, BudgetLine newline) {
		newline.setC_BPartner_ID(line.getC_BPartner_ID());
		newline.setAD_OrgDoc_ID(line.getAD_OrgDoc_ID());
		newline.setC_Project_ID(line.getC_Project_ID());
		newline.setC_Activity_ID(line.getC_Activity_ID());
		newline.setC_Campaign_ID(line.getC_Campaign_ID());
		newline.setM_Product_ID(line.getM_Product_ID());
		newline.setAccount_ID(line.getAccount_ID());
		newline.setLine(line.getLine());
	}

	/**
	 * EXTRACT MATCHING ELEMENTS ACCORDING TO DOCUMENT OBJECT
	 * ARRANGE IN KEYNAMEPAIR ARRAY FOR REUSE
	 * PURCHASE - PERIOD OR PARTNER / ACCOUNT/PROJECT/ACTIVITY/CAMPAIGN
	 * FACTS - PERIOD OR ACCOUNT/PROJECT/ACTIVITY/CAMPAIGN
	 * @param po
	 * @return MATCHES
	 */
	private static List<KeyNamePair> matchesFromDoc (PO po) {
		log.fine("private static List<KeyNamePair> matchesFromDoc (PO po)");
		List<KeyNamePair> matches = new ArrayList<KeyNamePair>();
		if (po instanceof MJournalLine) {
			MJournalLine journalLine = (MJournalLine)po;	
			//get Period ID from parent Journal - remove for rematch
			MJournal journal = new Query(po.getCtx(), MJournal.Table_Name, I_GL_Journal.COLUMNNAME_GL_Journal_ID+"=?", po.get_TrxName())
			.setParameters(journalLine.getGL_Journal_ID()).firstOnly();
			matches.add(new KeyNamePair(journal.getC_Period_ID(), I_GL_Journal.COLUMNNAME_C_Period_ID));
			//AD_Org_ID handling same as Period ID in MOrder
			matches.add(new KeyNamePair(po.getAD_Org_ID(),"AD_OrgDoc_ID"));
			//Account ID is mandatory in GL Journal
			matches.add(new KeyNamePair(journalLine.getAccount_ID(), "Account_ID"));
			if (journalLine.getC_Project_ID()>0) 
				matches.add(new KeyNamePair(journalLine.getC_Project_ID(), I_GL_JournalLine.COLUMNNAME_C_Project_ID));
			else matches.add(new KeyNamePair(0,I_GL_JournalLine.COLUMNNAME_C_Project_ID));
			if (journalLine.getC_Activity_ID()>0) 
				matches.add(new KeyNamePair(journalLine.getC_Activity_ID(), I_GL_JournalLine.COLUMNNAME_C_Activity_ID));
			else matches.add(new KeyNamePair(0,I_GL_JournalLine.COLUMNNAME_C_Activity_ID)); 
			if (journalLine.getC_Campaign_ID()>0) 
				matches.add(new KeyNamePair(journalLine.getC_Campaign_ID(), I_GL_JournalLine.COLUMNNAME_C_Campaign_ID));
			else matches.add(new KeyNamePair(0,I_GL_JournalLine.COLUMNNAME_C_Campaign_ID));
			if (journalLine.getC_BPartner_ID()>0)
				matches.add(new KeyNamePair(journalLine.getC_BPartner_ID(), I_GL_JournalLine.COLUMNNAME_C_BPartner_ID));
			else matches.add(new KeyNamePair(0,I_GL_JournalLine.COLUMNNAME_C_BPartner_ID));
			if (journalLine.getM_Product_ID()>0)
				matches.add(new KeyNamePair(journalLine.getM_Product_ID(), I_GL_JournalLine.COLUMNNAME_M_Product_ID));
			else matches.add(new KeyNamePair(0,I_GL_JournalLine.COLUMNNAME_M_Product_ID));
		}
		else {//for all DocType - Purchase, Invoice and Payment
			//PERIOD ID FOR PRESENT MONTH. REMOVED FOR REMATCH IN BUDGET RULES
			int Period_ID = (MPeriod.get(po.getCtx(), po.getCreated(),po.getAD_Org_ID())).getC_Period_ID();
			matches.add(new KeyNamePair(Period_ID,"C_Period_ID"));
			//AD_Org_ID handling same as Period ID
			matches.add(new KeyNamePair(po.getAD_Org_ID(),"AD_OrgDoc_ID"));
			//BPARTNER IS PERMANENT BUT REMOVE FOR REMATCH in BUDGET RULES
			matches.add(new KeyNamePair(po.get_ValueAsInt(I_C_Order.COLUMNNAME_C_BPartner_ID), I_C_Order.COLUMNNAME_C_BPartner_ID));
			//ACCOUNT ID SET TO DUMMY (11100-CHECKING-ACCOUNT) TO ENSURE NO MIXUP WITH JOURNAL MATCHING
			matches.add(new KeyNamePair(508,I_GL_JournalLine.COLUMNNAME_Account_ID));
			if (po.get_ValueAsInt(I_C_Order.COLUMNNAME_C_Project_ID)>0) 
				matches.add(new KeyNamePair(po.get_ValueAsInt(I_C_Order.COLUMNNAME_C_Project_ID), I_C_Order.COLUMNNAME_C_Project_ID));	
			else matches.add(new KeyNamePair(0,I_C_Order.COLUMNNAME_C_Project_ID));
			if (po.get_ValueAsInt(I_C_Order.COLUMNNAME_C_Activity_ID)>0) 
				matches.add(new KeyNamePair(po.get_ValueAsInt(I_C_Order.COLUMNNAME_C_Activity_ID), I_C_Order.COLUMNNAME_C_Activity_ID));
			else matches.add(new KeyNamePair(0,I_C_Order.COLUMNNAME_C_Activity_ID));
			if (po.get_ValueAsInt(I_C_Order.COLUMNNAME_C_Campaign_ID)>0)  
				matches.add(new KeyNamePair(po.get_ValueAsInt(I_C_Order.COLUMNNAME_C_Campaign_ID), I_C_Order.COLUMNNAME_C_Campaign_ID));		
			else matches.add(new KeyNamePair(0,I_C_Order.COLUMNNAME_C_Campaign_ID));
	 		}

		log.finer("MATCHES FROM DOC: "+matches.toString());
		return matches;
	}

	/**
	 * 	GET MATCHES FROM LINE, SET IN WHERECLAUSE WITH PARAMS IDS
	 * @param poLine
	 */
	private static void setWhereMatches(PO poLine){
		log.fine("public static void setWhereMatches(PO poLine)");
		clearWhereMatches();
		whereMatches = matchesFromDoc(poLine);	 
		whereMatchesSQL = whereClauseMatches(whereMatches, poLine);	//ONLY FIRST WILL BE 'OR'
		whereMatchesIDs = matchesToIDs(whereMatches);
	}

	/**
	 * ACCESS PURCHASE ORDERS ACCORDING TO CALENDAR AND MATCHES
	 * TO INCLUDE INVOICE (VENDORS)
	 * @param po
	 * @param operand
	 * @param year
	 * @return
	 */
	private static BigDecimal selectPurchases(PO po, String operand, String year, int periodValue) {
		log.fine("private static BigDecimal selectPurchaseOrders(PO po, String operand, String year)");
		//SQL EXECUTION
		BigDecimal totalAmt = Env.ZERO; 
		whereMatchesSQL = new StringBuffer("IsSOTrx = 'N' AND DocStatus = 'CO' AND " + whereMatchesSQL.toString());//IsSOTrx = 'N' AND DocStatus = 'CO' AND 
		StringBuffer yearClause = new StringBuffer(" AND (SELECT EXTRACT(ISOYEAR FROM DateOrdered)) "+operand+"'"+year+"'");
		if (operand.equals(MORE_EQUAL)){//happens during GenerateBudget only
			if (previousYears>0)
				whereMatchesSQL.append(yearClause); 
			else {
				changePeriodToTimestamp(periodValue);
			}		
		}
	
		List<MOrder> allPurchases = new Query(po.getCtx(), MOrder.Table_Name, whereMatchesSQL.toString(), po.get_TrxName())
		.setParameters(whereMatchesIDs)
		.list();
		for (MOrder purchase:allPurchases){
			totalAmt = totalAmt.add(purchase.getGrandTotal());
		}
		//HANDLE OTHER DOCUMENTS THAT DOES NOT OVERLAP ACROSS OTHER DOCUMENTS
		if (budgetCONFIGinstance.isInvoiceToo())
		{
			String whereSQL = whereMatchesSQL.toString().replace("DateOrdered", "DateAcct");
			List<MInvoice> allInvoices = new Query(po.getCtx(), MInvoice.Table_Name, 
					whereSQL.toString(), po.get_TrxName())
			.setParameters(whereMatchesIDs)
			.list();
			for (MInvoice invoice:allInvoices){
				if (invoice.getC_Order_ID()>0) continue;
				if (invoice.isSOTrx()) continue;
				totalAmt = totalAmt.add(invoice.getGrandTotal());
			}
		}
		if (budgetCONFIGinstance.isPaymentToo())
		{
			String whereSQL = whereMatchesSQL.toString().replace("DateOrdered", "DateAcct");
			whereSQL = whereSQL.toString().replace("IsSOTrx = 'N' AND ", ""); 
			List<MPayment> allPayments = new Query(po.getCtx(), MPayment.Table_Name, 
					whereSQL, po.get_TrxName())
			.setParameters(whereMatchesIDs)
			.list();
			for (MPayment payment:allPayments){
				if (payment.getC_Invoice_ID()>0) continue;
				if (payment.getC_Order_ID()>0) continue;
				if (payment.getC_DocType().isSOTrx()) continue;
				totalAmt = totalAmt.add(payment.getPayAmt());
			}
		}
		
		return totalAmt;
	}

	/**GET TOTAL AMOUNT FOR ACCOUNT ELEMENT FROM FACTACCT TABLE 
	 * REVENUE FOR % OR OTHER ELEMENTS FOR GL. SET LINE TO NULL TO GET REVENUE ACCOUNTS (4***)
	 * ACCORDING TO NUMBER OF PREVIOUS-YEARS
	 * ACCORDING TO NUMBER OF PREVIOUS-MONTHS (BEWARE OF ALOT OF SWAPPING AROUND BY FLAGS)
	 * @param line
	 * @param operand
	 * @param yearValue
	 * @return TOTAL AMOUNT
	 */
	private static BigDecimal selectAccountingFacts(BudgetLine line, String operand, String yearValue, int periodValue) {
		log.fine("private static BigDecimal selectAccountingFacts(BudgetLine, operand, yearValue, Period_ID)");
		StringBuffer whereClause = new StringBuffer();
		PO po = null;
		ArrayList<Object> params = new ArrayList<Object>();
		if (line==null) { //REVENUE ESTIMATE
			matchedIsCreditAmt = true;
			po = budgetCONFIGinstance;
			whereClause = new StringBuffer("Account_ID IN (Select C_ElementValue_ID FROM C_ElementValue WHERE Value Like '4%') "); 
		}
		else {//SWAP YEAR REVENUE LOGIC WITH ACCOUNTING ELEMENT LOGIC
			po = line;
			whereClause = new StringBuffer("POSTINGTYPE = 'A' AND "); 
		}
		if (whereMatchesSQL!=null){
			whereClause.append(whereMatchesSQL);
			params.addAll(whereMatchesIDs);
		}
		if (periodValue==0){ //for years > 0
			whereClause.append(" AND C_PERIOD_ID IN (SELECT C_PERIOD_ID FROM C_PERIOD WHERE C_YEAR_ID IN (SELECT C_YEAR_ID FROM C_YEAR WHERE FISCALYEAR "+operand+" ?))");
			params.add(yearValue);
		}
		else if (previousMonths>0){ //assume Period_IDs sequential till present as last ID.
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

	/**
	 * PARSE SQL CLAUSE
	 * COUNT = 1 IF JOURNALLINE AS ONLY FIRST MATCH IS OR, PURCHASE HAS FIRST TWO OR
	 * @param matches
	 * @param po
	 * @return WHERECLAUSE
	 */
	private static StringBuffer whereClauseMatches(List<KeyNamePair> matches, PO po) {
		log.info("private static StringBuffer whereClauseMatches(List<KeyNamePair> matches, PO po)");
		int orCount = 3; //FOR PURCHASING DOCTYPES
		if (po instanceof MJournalLine) orCount = 2;
		StringBuffer whereClause = new StringBuffer("(");
		int start = 0;
		for (KeyNamePair match:matches) {
			if (whereClause.length()>1) {
				if (start < orCount)  {
					whereClause.append(" OR ");
					start++;
			 		if (start==orCount) {
			 			whereClause.append("1=1) AND ");
			 			}
					}				
				 	else {
				 		whereClause.append(" AND ");
				 	}
			}
			if (match.getKey()==0)
				whereClause.append(match.getName()+" is null");
			else
				whereClause.append(match.getName()+"=?");			
		}	
		String removematch = "C_BPartner_ID is null OR "; //patch for GenerateBudget process Purchase rule
		if (whereClause.toString().contains(removematch)){
			whereMatches.remove(2);
		}
		String matchRemoved = whereClause.toString().replace(removematch, "");
		whereClause = new StringBuffer(matchRemoved);
		removematch = "AD_OrgDoc_ID is null OR "; //patch for GenerateBudget process Purchase rule
		if (whereClause.toString().contains(removematch)){
			whereMatches.remove(1);
		}
		matchRemoved = whereClause.toString().replace(removematch, "");
		whereClause = new StringBuffer(matchRemoved);	
		log.finer("WHERE CLAUSE MATCHES: "+whereClause.toString());
		return whereClause;
	}

	/** LOOKUP IN BUDGET LINE
	 *  APPLY MATCHES TO WHERE CLAUSE IN SEARCH OF MATCHING BUDGET LINES 
	 * @param po
	 * @param matches
	 * @param whereClause
	 * @return matchedLine
	 */
	private static  BudgetLine lookupBudgetRule(PO po, List<KeyNamePair>matches, StringBuffer whereClause) {
		log.fine("private static  BudgetLine lookupBudgetRule(PO po, List<KeyNamePair>matches, StringBuffer whereClause)");
		//FETCH ONLY EXACT BUDGETLINE THAT HAS DOCUMENT'S CRITERIA (EFFICIENT)
		//WHERE CLAUSE FROM DOCUMENT'S MAIN CRITERIA
	
		List<BudgetLine> matchedLines = new Query(po.getCtx(), BudgetLine.Table_Name, whereClause.toString() 
		+" AND "+I_GL_Journal.COLUMNNAME_GL_Journal_ID+"="+targetBudget.getGL_Journal_ID(), po.get_TrxName())
		.setParameters(whereMatchesIDs)
		.setOnlyActiveRecords(true)
		.list();
		//WITH ADDED CRITERIA FIRST C_PERIOD_ID, AD_ORGDOC_ID, C_BPARTNER_ID
		//ITERATE POSSIBLE MATCHES BUT RULE OUT PERIOD, ORG AND PARTNER (FOR PURCHASE) UNTIL EXACT MATCH OR NULL
		BudgetLine matchedLine = null;
	
		int counter = 0; //if counter incremented twice it means extra ambiguous Budget Rules for same match.
		
		for (BudgetLine line:matchedLines) {
			if ((matches.get(0).getKey()!=line.getC_Period_ID()) && (line.getC_Period_ID()!=0)) continue;
			if ((matches.get(1).getKey()!=line.getAD_OrgDoc_ID()) && (line.getAD_OrgDoc_ID()!=0)) continue;
			if (!(po instanceof MJournalLine) && (matches.get(2).getKey()!=line.get_ValueAsInt(I_C_Order.COLUMNNAME_C_BPartner_ID)) && (line.get_ValueAsInt(I_C_Order.COLUMNNAME_C_BPartner_ID)!=0)) continue;
			matchedLine = line;
			counter++;
		}
		if (counter==0) return null;	
		//AMBIGOUS DUE TO PARAMS WITHIN BRACKETS HAVING MORE RECORDS WITH IMPLICIT RULES
		if (counter>1) throw new AdempiereException(counter+" AMBIGUOUS BUDGET RULES FOUND = "+whereClause);
		
		//SET matchedIsCreditAmt FLAG - USED IN getAmtSource(..,..)
		checkCreditOrDebit(matchedLine);
		checkProrata(matchedLine);
		log.finer("LOOKUP BUDGET RULE - matchedBudgetLine: "+matchedLine.toString());
		return matchedLine;
	}

	/*
	 * SET PRORATA IF MATCHED LINE HAS PERIOD ID, I.E. MONTHLY BASE
	 */
	private static void checkProrata(BudgetLine matchedLine) {
		 if (matchedLine.getC_Period_ID()>0)
			 isProrata = true;
	}

	/**
	 * HELPER METHOD TO RETURN AMOUNT EITHER CREDIT OR DEBIT SIDE; 
	 * FOR FACT, JOURNALLINE OR BUDGETLINE
	 * @param fact
	 * @param jline
	 * @return DEBIT OR CREDIT AMOUNT
	 */
	private static BigDecimal getAmtSource(MFactAcct fact, MJournalLine jline) {
		log.fine("private static BigDecimal getAmtSource(MFactAcct fact, MJournalLine jline)");
		if (fact!=null) {
			if (matchedIsCreditAmt)
				return fact.getAmtSourceCr();
			else 
				return fact.getAmtSourceDr();
		}
		else { 
			if (matchedIsCreditAmt)
				return jline.getAmtSourceCr();
			else 
				return jline.getAmtSourceDr();
		}
	}

	/**THIS RETURNS MATCHED BUDGET AMT OR % WITH STATIC VARIABLES FOR MATCHES
	 *	MATCHED IS CREDIT OR DEBIT AMT,
	 * @param poLine
	 * @return matchedBudgetLine
	 */
	private static BudgetLine matchingBudget(PO poLine) {
		log.fine("public static BudgetLine matchingProcess(PO poLine)");
		setWhereMatches(poLine);
		BudgetLine matchedBudgetLine = lookupBudgetRule(poLine, whereMatches, whereMatchesSQL);		
		if (matchedBudgetLine!=null)  
			matchedType(matchedBudgetLine);
		return matchedBudgetLine;
	}
	
	/** GET IDS FROM MATCHES FOR SETTING PARAM VALUES IN SQL QUERY 
	 * @param matches
	 * @return IDs
	 */
	private static ArrayList<Object> matchesToIDs(List<KeyNamePair> matches) {
		log.fine("private static ArrayList<Object> matchesToIDs(List<KeyNamePair> matches)");
		ArrayList<Object> params = new ArrayList<Object>();
		for (KeyNamePair match:matches) {
			if (match.getKey()>0)
				params.add((new Integer(match.getID())).intValue());
		}
		if (params.isEmpty()) return null;
		log.finer("MATCHES TO IDS - PARAMS: "+params.toString());
		return params;
	}

	/** difference between last and start, divided by start, multiply last, add to last.
	 *  LAST AMT NOT ZERO
	 * @param line
	 * @return
	 */
	private static BigDecimal rateIncreaseFormula(BudgetLine line) {
		BigDecimal returnAmount;
		BigDecimal startAmt = Env.ZERO;
		BigDecimal lastAmt = Env.ZERO;
		if (runtimePO instanceof MJournal){
			startAmt = selectAccountingFacts(line, EQUAL, startYear, startMonth);
			lastAmt = selectAccountingFacts(line, EQUAL, pastYear, pastMonth);
		}
		else {
			startAmt = selectPurchases(line, MORE_EQUAL, startYear, startMonth);
			lastAmt = selectPurchases(line, MORE_EQUAL, pastYear, pastMonth);
		}
		if (lastAmt.equals(Env.ZERO)) 
			throw new AdempiereException("LAST YEAR/MONTH HAS NO AMOUNT");

		BigDecimal buffer = lastAmt.subtract(startAmt);
		BigDecimal rate = buffer.divide(startAmt);
		returnAmount = rate.multiply(lastAmt).add(lastAmt);
		return returnAmount;
	}
	
	/** SET BUDGET PERCENT OR BUDGET AMOUNT
	 * @param matchedBudgetLine
	 */
	private static void matchedType(BudgetLine matchedBudgetLine) {
		log.fine("private static void matchedResult(BudgetLine matchedBudgetLine)");
		//
		budgetPercent = matchedBudgetLine.getPercent();

		if (budgetPercent.compareTo(Env.ZERO)==0){
			budgetAmount = getAmtSource(null, matchedBudgetLine);			
		}
	}
	
	/**
	 * REMOVE LEADING ARBITRARY PARAMS FOR SQL QUERY (FOR PURCHASING)
	 * @param matchedBudgetLine
	 */
	private static void paramTrimming(BudgetLine matchedBudgetLine, boolean purchaseGenerate) {
		log.fine("private static void paramTrimming(BudgetLine matchedBudgetLine)");
		String removematch="";
		String matchRemoved="";
		int c = 0; //COUNT HOW MANY NON MATCHES DELETED FOR ALIGNING WHEREMATCHES ARRAY POINTER
		//REMOVE FIRST PARAM - C_PERIOD_ID=? OR (do not remove for Journal, for PO later filter out non periods 
 
		if (whereMatches.get(0).getName().equals(I_GL_JournalLine.COLUMNNAME_C_Period_ID)) {
			if (matchedBudgetLine.getC_Period_ID()==0 || purchaseGenerate) {
				removematch = "("+whereMatches.get(0).getName()+"=? OR ";
				matchRemoved = whereMatchesSQL.toString().replace(removematch, "");
				whereMatchesSQL = new StringBuffer(matchRemoved);
				whereMatches.remove(0);
				whereMatchesIDs.remove(0);
			}
			else {
				if (!(runtimePO instanceof MJournal)) {
					changePeriodToTimestamp(matchedBudgetLine.getC_Period_ID());
					removematch = "("+I_GL_JournalLine.COLUMNNAME_C_Period_ID+"=? OR ";
	 				matchRemoved = whereMatchesSQL.toString().replace(removematch, "");
					whereMatchesSQL = new StringBuffer(matchRemoved);	
					whereMatches.remove(0);
					whereMatchesIDs.remove(0);
				}
				else{
					c++;
					removematch = "("+I_GL_JournalLine.COLUMNNAME_C_Period_ID+"=? OR ";
	 				matchRemoved = whereMatchesSQL.toString().replace(removematch, I_GL_JournalLine.COLUMNNAME_C_Period_ID+"=? AND ");
					whereMatchesSQL = new StringBuffer(matchRemoved);	
				}
			}
		}
	
		//REMOVE 2ND PARAM - AD_OrgDoc_ID=? OR 
		if (whereMatches.get(c).getName().equals(I_GL_JournalLine.COLUMNNAME_AD_OrgDoc_ID)) {
			if (matchedBudgetLine.getAD_OrgDoc_ID()==0) {
				removematch = whereMatches.get(c).getName()+"=? OR ";
				matchRemoved = whereMatchesSQL.toString().replace(removematch, "");
				whereMatchesSQL = new StringBuffer(matchRemoved);		
				whereMatches.remove(c);		
				whereMatchesIDs.remove(c);
			} else {
				c++;
				removematch = I_GL_JournalLine.COLUMNNAME_AD_OrgDoc_ID+"=? OR ";
				//SWAP AD_OrgDoc_ID = AD_Org_ID as trimming is for MOrder and GL Journal
				matchRemoved = whereMatchesSQL.toString().replace(removematch, MOrder.COLUMNNAME_AD_Org_ID+"=? AND ");
				whereMatchesSQL = new StringBuffer(matchRemoved);		
			}
		}
		
		//CHECK IF NEED TO REMOVE SECOND PARAM AS FIRST - (C_BPartner_ID=? OR)
		if (whereMatches.get(c).getName().equals(I_GL_JournalLine.COLUMNNAME_C_BPartner_ID)) {
			if (matchedBudgetLine.get_ValueAsInt(I_C_Order.COLUMNNAME_C_BPartner_ID)==0) {
				removematch =  whereMatches.get(c).getName()+"=? OR ";
				matchRemoved = whereMatchesSQL.toString().replace(removematch, "");
				whereMatchesSQL = new StringBuffer(matchRemoved);		
				whereMatches.remove(c);
				whereMatchesIDs.remove(c);
			} else {
				c++;
				removematch = I_GL_JournalLine.COLUMNNAME_C_BPartner_ID+"=? OR ";
 				matchRemoved = whereMatchesSQL.toString().replace(removematch, MOrder.COLUMNNAME_C_BPartner_ID+"=? AND ");
				whereMatchesSQL = new StringBuffer(matchRemoved);	
			}
		}
		
		removematch = "1=1) AND ";
		matchRemoved = whereMatchesSQL.toString().replace(removematch, "");
		whereMatchesSQL = new StringBuffer(matchRemoved);
		//FINAL
		if (!(runtimePO instanceof MJournal)){ //remove "AND Account_ID=?"
			removematch = "Account_ID=? AND ";
			matchRemoved = whereMatchesSQL.toString().replace(removematch, "");
			whereMatchesSQL = new StringBuffer(matchRemoved);
			whereMatches.remove(c);
			whereMatchesIDs.remove(c);
		}			 
		log.finer("PARAM TRIMMING = SELECT FROM FACT_ACCT WHERE "+whereMatchesSQL+" PARAMS?: "+whereMatchesIDs.toString());
	}
	
	/**
	 * SET ISCREDITORDEBIT
	 */
	private static void checkCreditOrDebit(BudgetLine line){
		if (line.getPercent().equals(Env.ZERO)){
			if (line.getAmtSourceCr().compareTo(Env.ZERO)>0)
				matchedIsCreditAmt = true;
			else 	
				matchedIsCreditAmt = false;
		}
	}
	
	/**
	 * CHECK BUDGET'S PERCENT AGAINST REVENUE OR AMOUNT AGAINST TOTAL-AMOUNT TO-DATE
	 * @param todateAmount
	 * @return
	 */
	private static String budgetAgainstToDate(BigDecimal todateAmount) {
		log.info("private static String budgetAgainstToDate(BigDecimal todateAmount)");
		BigDecimal baseAmt = null;
		
		//PRORATA RULE HERE - FOR MATCHEDLINE WITH PERIOD ID - MONTHLY BASE
		if (isProrata)
			 baseAmt = RevenueEstimate.divide(new BigDecimal(12),2);
		
		if (budgetPercent.compareTo(Env.ZERO)>0) 
			if (baseAmt.multiply(budgetPercent).divide(Env.ONEHUNDRED,2).compareTo(todateAmount)<0 || budgetCONFIGinstance.getDescription().contains("DEBUG")) {
				BigDecimal diff = baseAmt.multiply(budgetPercent).divide(Env.ONEHUNDRED,2).subtract(todateAmount);
				return throwBudgetExceedMessage(diff.setScale(2,BigDecimal.ROUND_UP).toString()+", "+budgetPercent.setScale(2,BigDecimal.ROUND_UP).toString()+"% OF "
				+baseAmt.setScale(2,BigDecimal.ROUND_UP).toString()+" REVENUE, ",todateAmount, whereMatches);			 
			} else log.finest("TO-DATE: "+todateAmount+" WITHIN "+budgetPercent+"% OF "+baseAmt+" BUDGET.");

		if (budgetAmount.compareTo(Env.ZERO)>0) {
			if (budgetAmount.compareTo(todateAmount)<0 || budgetCONFIGinstance.getDescription().contains("DEBUG")) {
				BigDecimal diff = budgetAmount.subtract(todateAmount);
				return throwBudgetExceedMessage(diff.setScale(2, BigDecimal.ROUND_UP).toString()+", "
				+budgetAmount.setScale(2,BigDecimal.ROUND_UP).toString()+" BUDGET, ", todateAmount, whereMatches);
			} else log.finest(todateAmount+" AMOUNT WITHIN BUDGET OF "+budgetAmount);
		}
		return null;//no error
	}

	/**
	 * @param description - FORMATED RESPONSE ACCORDING TO ITEMS EARLIER
	 * @param totalAmt
	 * @param matches -- OPTIONAL, NOT USED YET
	 * @return STRING RESONSE FOR ERROR HANDLING BY MAIN CALLING CLASS
	 */
	private static String throwBudgetExceedMessage(String description, BigDecimal totalAmt, List<KeyNamePair> matches) {
		log.fine("private static String throwBudgetExceedMessage(..");
		totalAmt = totalAmt.setScale(2);
		log.finer("EXCEED BY "+description+" TO-DATE: "+totalAmt+", TREND: "+PRORATA+budgetCONFIGinstance.getBudgetTrend());
		return "EXCEED > "+description+" TO-DATE: " + totalAmt+", TREND: "+PRORATA+budgetCONFIGinstance.getBudgetTrend();		
	}
	
	/**
	 * MONTH ON MONTH LIST CREATED AS ARRAY OF PERIOD IDS ACROSS PREVIOUS YEARS
	 * FOR processMonthToMonth(BudgetLine)
	 * @param line
	 * @return false if query null
	 */
	private static int createPeriodPointers(BudgetLine line){
		log.fine("private static boolean createMonthToMonthList(BudgetLine line)");
		firstPeriodOfYear = (MPeriod.getFirstInYear(line.getCtx(), line.getCreated(), line.getAD_Org_ID())).getC_Period_ID();
		//if previousYears = 0, startYear will be presentYear
		List<MYear> years = new Query(line.getCtx(),MYear.Table_Name,MYear.COLUMNNAME_FiscalYear+" >= ?",line.get_TrxName())
		.setParameters(startYear)
		.list();
		if (years==null) return 0;
		PeriodNo = presentMonth - firstPeriodOfYear; //Difference of IDs = exact PeriodNo (this month subtract first month).
//		List<Integer> presentPeriods = new ArrayList<Integer>(); //for present Year periods
		for (MYear year:years){			 
			//MAIN
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
 				for (int a=0; a < PeriodNo-previousMonths; a++){ //Month backwards previousMonths is spot before which not needed.
 					mom.remove(0); //keep removing from top of stack until previousMonths
 				}
 			}
		}

		log.finest("MonthToMonth Array: "+mom.toString()+" START YEAR: "+startYear+" PREVIOUS MONTHS: "+previousMonths);
		return firstPeriodOfYear;
	}
	 
	/**
	 *TAKE HISTORY INTO ACCOUNT - AVERAGE OR UPPER OR LOWER
	 * @param line
	 * @param totalAmt
	 * @return
	 */
	private static BigDecimal historicalCondition(BigDecimal newAmt, BigDecimal oldAmt) {
		if (budgetCONFIGinstance.getDescription()==null) return newAmt;
 		if (budgetCONFIGinstance.getDescription().contains("GENERATE BETWEEN")) 
			return newAmt.add(oldAmt).divide(new BigDecimal(2),2);
		if (budgetCONFIGinstance.getDescription().equals("GENERATE HIGHER"))
			return newAmt.max(oldAmt);
		if (budgetCONFIGinstance.getDescription().equals("GENERATE LOWER"))
			return newAmt.min(oldAmt);	
		return newAmt;
	}

	/**
	 * TIMESTAMP HANDLING APPEND MATCHESSQL AND PARAMS
	 * where dateordered < firstDate(periodID) && > lastDate(periodID)
	 * OR previousMonths
	 */
	private static void changePeriodToTimestamp(int periodValue){ 
		log.fine("private static void setTimestampToMatches(int param)");
		Calendar cal = Calendar.getInstance();	
		if (periodValue==previousMonths){
			cal.add(Calendar.MONTH, -previousMonths);
			Timestamp whereMonth = new Timestamp(cal.getTimeInMillis());
			whereMatchesSQL.append(new StringBuffer(" AND DateOrdered >= ?"));
			whereMatchesIDs.add(whereMonth);
		}
		else if (periodValue > 100){
			MPeriod period = new MPeriod(runtimePO.getCtx(), periodValue, runtimePO.get_TrxName());
			if (period.isStandardPeriod()) {
			Timestamp start = period.getStartDate();
			Timestamp end = period.getEndDate();
			whereMatchesSQL.append(new StringBuffer(" AND DateOrdered >= ? AND DateOrdered <= ?"));
			whereMatchesIDs.add(start);
			whereMatchesIDs.add(end);
			}
			else
				log.warning("**NOT STANDARD PERIOD**  START/END DateOrdered NOT SET IN SQL. PERIOD ID: "+periodValue);
		}		
	} 
}
