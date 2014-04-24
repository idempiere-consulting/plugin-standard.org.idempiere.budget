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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map; 

import org.adempiere.exceptions.AdempiereException; 
import org.compiere.model.I_C_Order;
import org.compiere.model.I_GL_Journal;
import org.compiere.model.MFactAcct;
import org.compiere.model.MJournal;
import org.compiere.model.MJournalLine;
import org.compiere.model.MOrder;
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
	private static final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
 
	private static int forecastYears; 
	private static String startYear;
	private static String pastYear;
	private static String presentYear;
	private static BigDecimal RevenueEstimate;  
	
	private static int forecastMonths;
	private static int startMonth;
	private static int pastMonth;
	private static int presentMonth;
	
	private static boolean isMonthToMonth;
	private static boolean isProrata = false;
	private static String PRORATA=""; 
	private static List<Integer>mom = new ArrayList<Integer>();
	private static Map<String,Integer> yearPeriod = new HashMap<String,Integer>();
	private static List<Integer> presentPeriods = new ArrayList<Integer>();
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
	 * @param forecastYears
	 * @param MonthToMonth
	 * @param ForecastMonths
	 * @param BudgetTrend
	 * @param Prorata
	 */
	public static void setInstance(BigDecimal forecastYears,String MonthToMonth,BigDecimal ForecastMonths,String BudgetTrend,String Prorata) {
		log.fine("public static void setInstance(...)");
		budgetCONFIGinstance.setGL_Budget_Forecast_Year(forecastYears);
		budgetCONFIGinstance.setMonthToMonth(new Boolean(MonthToMonth));
		budgetCONFIGinstance.setGL_Budget_Forecast_Month(ForecastMonths);
		budgetCONFIGinstance.setBudgetTrend(BudgetTrend);
		budgetCONFIGinstance.setProrata(new Boolean(Prorata));
		log.finer("SET INSTANCE Forecast years:"+forecastYears+" MonthToMonth: "+MonthToMonth+" ForecastMonths: "+ForecastMonths+" BudgetTrend: "+BudgetTrend+" Prorata: "+Prorata);
	}
	/**
	 * RESOLVE CONFLICTING PROPERTIES OF BUDGETCONFIGINSTANCE
	 * OBTAIN START/PAST YEAR VALUES ACCORDING TO FORECAST YEARS
	 * CONVERT FORECAST-MONTHS TO PERIOD IDS
	 */
	public static void setupCalendar(PO po) {
		log.fine("public static void setupCalendar(PO po)");
		forecastYears = budgetCONFIGinstance.getGL_Budget_Forecast_Year().intValue();
		forecastMonths = budgetCONFIGinstance.getGL_Budget_Forecast_Month().intValue();
		
		//present year
		Calendar cal = Calendar.getInstance();
		presentYear = yearFormat.format(cal.getTime());
		//last year
		cal.add(Calendar.YEAR, -1);
		pastYear = yearFormat.format(cal.getTime());
		//starting year
		cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -forecastYears);
		startYear = yearFormat.format(cal.getTime());

		log.finer("SETUP CALENDAR - CAL : "+cal.toString()+" START YEAR: "+startYear+" PAST YEAR: "+pastYear+" YEARS RANGE: "+forecastYears);

		//Month PERIODS IDS assumed to be consecutive.
		//present month
		presentMonth = MPeriod.getC_Period_ID(po.getCtx(), po.getCreated(), po.getAD_Org_ID());		
		//last month
		pastMonth = presentMonth - 1;
		//starting month
		startMonth = presentMonth - forecastMonths;
		
		//IF PRORATA THEN CONFIG MONTHS VOID
		if (budgetCONFIGinstance.isProrata()) {
			isProrata = true;
			isMonthToMonth = false;
			forecastMonths = 0;
		}
		//IF MONTH ON MONTH
		else if (budgetCONFIGinstance.isMonthToMonth()) {
			isMonthToMonth = true; 
			//CANNOT PRORATA
			isProrata = false;
		}
		log.finer("ISPRORATA:"+isProrata+" inMonthToMonth:"+isMonthToMonth);
	}

	/**
	 * ONE TIME SETUP REVENUE OVER YEARS RANGE, APPLY BUDGET TREND
	 * @return YEAR REVENUE
	 */
	public static BigDecimal revenueEstimate() {
		log.fine("public static BigDecimal revenueEstimate()");
		if (forecastYears==0) { //NO ACTION
				RevenueEstimate = Env.ZERO;					 
			}
		else if (forecastYears<100){
			RevenueEstimate = selectAccountingFacts(null, MORE_EQUAL, startYear, 0);
			RevenueEstimate = budgetTrend(null, RevenueEstimate, 0);//OBTAIN REVENUE 4XXX AMOUNT
		}
		//FORECAST MONTHS
		if (forecastMonths>0){
			//set present month, last month, starting month
		}
		log.finer("REVENUE ESTIMATE: "+RevenueEstimate.toString());				
		return RevenueEstimate; 
	}
	
	/**
	 * ONLY PURCHASE ORDERS
	 * USE MATCHES/WHERECLAUSE CONSTRUCT FROM PERIOD OR ACCOUNT-ID,PROJECT,ACTIVITY,CAMPAIGN,BPARTNER
	 * APPLY BUDGET-CONFIG RULES TO BUDGETAMOUNT COMPARE TO TOTALPURCHASES FOR THE YEAR.
	 * @param po
	 * @return IF ERROR STRING
	 */
	public static String processPurchaseOrder(PO po) {
		log.fine("public static String processPurchaseOrder(PO po)");
		runtimePO = po; 
		//MORDER Document Event Validation
		MOrder purchase = new MOrder(Env.getCtx(), po.get_ID(), po.get_TrxName());	
		
		//ONLY PURCHASE ORDER   
		if (!purchase.isSOTrx()) { 
			
			BudgetLine matchedBudgetLine = matchingProcess(purchase);
			if (matchedBudgetLine!=null){
				BigDecimal totalPurchases = Env.ZERO;
				String YEAR = yearFormat.format(Calendar.getInstance().getTime());
				paramTrimming(matchedBudgetLine);
				
				//SQL EXECUTION
				String thisClause = "IsSOTrx = 'N' AND DocStatus = 'CO' AND " + whereMatchesSQL.toString()
					+" AND (SELECT EXTRACT(ISOYEAR FROM DateOrdered)) = '"+YEAR+"'"; //match with criteria in Order
				List<MOrder> allPurchases = new Query(po.getCtx(), MOrder.Table_Name, thisClause, po.get_TrxName())
				.setParameters(whereMatchesIDs)
				.list();
				for (MOrder pastPurchase:allPurchases) {
					totalPurchases = totalPurchases.add(pastPurchase.getGrandTotal());
				}
				totalPurchases = totalPurchases.add(purchase.getGrandTotal());
				//IF PRORATA TAKE ONLY PRESENT MONTH(S) PURCHASES
				//IF ISMONTHONMONTH TAKE SAME PERIODS FROM PREVUOUS YEARS AND APPLY AVERAGE/PRGRESSIVE
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
	public static String processGLJournal(PO po) {
		log.fine("public static String processGLJournal(PO po)");
		runtimePO = po; 
		//BEFORE POSTING JOURNAL LINES
		List<MJournalLine> journalLines = BudgetLine.getJournalLines((MJournal)po); //FETCHING VALIDATING DOCUMENT JOURNAL LINES
			
		//ITERATING JOURNAL LINES
		for (MJournalLine journalLine:journalLines) {
			BudgetLine matchedBudgetLine = matchingProcess(journalLine);
			if (matchedBudgetLine!=null){
				paramTrimming(matchedBudgetLine);
				//GET TOTAL OF ALL RELATED ACCOUNTING FACTS FOR THE YEAR <WITH RULES APPLIED>
				String YEAR = yearFormat.format(Calendar.getInstance().getTime());
				BigDecimal totFactAmt = selectAccountingFacts(matchedBudgetLine, EQUAL, YEAR,0);
				totFactAmt = totFactAmt.add(getAmtSource(null, journalLine));
				return budgetAgainstToDate(totFactAmt);
			}
		}
		log.finer("PROCESS GL JOURNAL - NO ERROR STRING RETURNED");
		return null;
	}

	/**GET TOTAL AMOUNT FOR ACCOUNT ELEMENT FROM FACTACCT TABLE 
	 * REVENUE FOR % OR OTHER ELEMENTS FOR GL. SET LINE TO NULL TO GET REVENUE ACCOUNTS (4***)
	 * ACCORDING TO NUMBER OF FORECAST-YEARS
	 * ACCORDING TO NUMBER OF FORECAST-MONTHS (BEWARE OF ALOT OF SWAPPING AROUND BY FLAGS)
	 * @param line
	 * @param operand
	 * @param yearValue
	 * @return TOTAL AMOUNT
	 */
	private static BigDecimal selectAccountingFacts(BudgetLine line, String operand, String yearValue, int Period_ID) {
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
		if (Period_ID==0){
			whereClause.append(" AND C_PERIOD_ID IN (SELECT C_PERIOD_ID FROM C_PERIOD WHERE C_YEAR_ID IN (SELECT C_YEAR_ID FROM C_YEAR WHERE FISCALYEAR "+operand+" ?))");
			params.add(yearValue);
		}
		else {
			//Juxtapose PeriodID in whereMatchesIDs AND no FiscalYear param needed
			//C_Period_ID place holder already present in params(WhereMatchesSQL)
			params.set(0, new Integer(Period_ID));
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
	
		log.finer(" CALCULATED AMOUNT FROM ACCOUNTING FACTS = "+returnAmount+" PO: "+po+" YEAR VALUE: "+yearValue+ " PERIOD ID: "+Period_ID);
		return returnAmount;
	}
	/**THIS RETURNS MATCHED BUDGET AMT OR % WITH STATIC VARIABLES FOR MATCHES
	 *	MATCHED IS CREDIT OR DEBIT AMT,
	 * @param poLine
	 * @return matchedBudgetLine
	 */
	private static BudgetLine matchingProcess(PO poLine) {
		log.fine("public static BudgetLine matchingProcess(PO poLine)");
		setWhereMatches(poLine);
		BudgetLine matchedBudgetLine = lookupBudgetRule(poLine, whereMatches, whereMatchesSQL);					
		matchedResult(matchedBudgetLine);
		return matchedBudgetLine;
	}
	
	/**
	 * 	GET MATCHES FROM LINE, SET IN WHERECLAUSE WITH PARAMS IDS
	 * @param poLine
	 */
	public static void setWhereMatches(PO poLine){
		log.fine("public static void setWhereMatches(PO poLine)");
		clearWhereMatches();
		whereMatches = matchesFromDoc(poLine);	 
		whereMatchesSQL = whereClauseMatches(whereMatches, poLine);	//ONLY FIRST WILL BE 'OR'
		whereMatchesIDs = matchesToIDs(whereMatches);
	}
	private static void clearWhereMatches(){
		log.fine("private static void clearWhereMatches()");
		whereMatches =  new ArrayList<KeyNamePair>();
		whereMatchesSQL = new StringBuffer();
		whereMatchesIDs = new ArrayList<Object>();
	}
	
	/**
	 *  APPLY BUDGET TREND LIST: AVERAGE, AVERAGE+LAST, PROGRESSIVE, ACCUMULATIVE,
	 * 	YEAR-TO-DATE, AND APPLY PRORATA /12.
	 * @param line
	 * @param 
	 * @return REVENUE AMOUNT
	 */
	private static BigDecimal budgetTrend(BudgetLine line, BigDecimal returnAmount, int Period_ID) {		
		log.fine("	private static BigDecimal budgetTrend(BudgetLine line, BigDecimal returnAmount, int Period_ID)");
		//TODO forecast months
		if (forecastMonths>1){
			
		}
		//IF YEARS-RANGE = 1 i.e. ONLY LAST YEAR NOT SUBJECTED TO FURTHER TREND
		if (forecastYears==1) {
			returnAmount = selectAccountingFacts(line, EQUAL, pastYear, Period_ID);
			return returnAmount;
		}
		//forecast Months = 1 i.e. Period_ID = LastMonth (todo in setupCalendar.
		if (forecastMonths==1) returnAmount = selectAccountingFacts(line,EQUAL,null,Period_ID);
		
		//AMOUNT AVERAGE ACROSS RANGE OF YEARS
 		BigDecimal sumAmt= returnAmount; //FOR LATER FORMULA USE
		BigDecimal average = returnAmount.divide(new BigDecimal(forecastYears),2);
		
		//RETURN AVERAGE
		if (budgetCONFIGinstance.getBudgetTrend().equals("A"))
			return average;
 			
		//AVERAGE + LAST = ADD AVERAGE AMOUNT IN RANGE TO LAST YEAR'S  
		if (budgetCONFIGinstance.getBudgetTrend().equals("L")) {
			BigDecimal lastYearAmt = selectAccountingFacts(line, EQUAL, pastYear, Period_ID);
			returnAmount = lastYearAmt.add(average);
			return returnAmount;
		}
		//APPLY RATE OF CHANGE OVER RANGE TO LAST YEAR'S 
		else if (budgetCONFIGinstance.getBudgetTrend().equals("P")) { 
			BigDecimal startYearAmt = selectAccountingFacts(line, EQUAL, startYear, Period_ID);
			BigDecimal lastYearAmt = selectAccountingFacts(line, EQUAL, pastYear, Period_ID);
			if (lastYearAmt.equals(Env.ZERO)) 
				throw new AdempiereException("LAST YEAR = "+pastYear+" HAS NO AMOUNT");
			else
			{
				BigDecimal rate = startYearAmt.divide(lastYearAmt,2).multiply(Env.ONEHUNDRED);
				returnAmount = rate.multiply(lastYearAmt).add(lastYearAmt);
			}
		}
		//ACCUMULATIVE OVER RANGE OF YEARS
		else if (budgetCONFIGinstance.getBudgetTrend().equals("C")) {
				returnAmount = sumAmt;
		}		
		//YEAR TO-DATE
		else if (budgetCONFIGinstance.getBudgetTrend().equals("T")) {
			returnAmount = selectAccountingFacts(null, EQUAL, presentYear,Period_ID);
		}		
		if (isProrata) {
			returnAmount = returnAmount.divide(new BigDecimal(12),2);
			PRORATA = "(PRORATA) ";
		}
		return returnAmount;
	}
	
	/** SET BUDGET PERCENT OR BUDGET AMOUNT AND DO PRORATA IF TRUE
	 * @param matchedBudgetLine
	 */
	private static void matchedResult(BudgetLine matchedBudgetLine) {
		log.fine("private static void matchedResult(BudgetLine matchedBudgetLine)");
		//
		budgetPercent = matchedBudgetLine.getPercent();

		if (budgetPercent.compareTo(Env.ZERO)==0){
			budgetAmount = getAmtSource(null, matchedBudgetLine);			
			if (budgetAmount.equals(Env.ZERO)) {
				//IF PRORATA, DIVIDE BY 12 MONTHS
				if (isProrata && matchedBudgetLine.getC_Period_ID()==0) {//BUT NOT FOR PERIOD SPECIFIC BUDGET
					budgetAmount = budgetAmount.divide(new BigDecimal(12),2);			//NOR FOR PERCENTAGE
					PRORATA = " (PRORATA) ";
				}
			}
		}
	}
	
	/**
	 * REMOVE LEADING ARBITRARY PARAMS FOR SQL QUERY (FOR PURCHASING)
	 * @param matchedBudgetLine
	 */
	private static void paramTrimming(BudgetLine matchedBudgetLine) {
		log.fine("private static void paramTrimming(BudgetLine matchedBudgetLine)");
		String removematch="";
		String matchRemoved="";
		int c = 0; //COUNT HOW MANY NON MATCHES DELETED FOR ALIGNING WHEREMATCHES ARRAY POINTER
		//REMOVE FIRST PARAM - C_PERIOD_ID=? OR (do not remove for Journal, for PO later filter out non periods TODO
 
		if (whereMatches.get(0).getName().equals(I_GL_JournalLine.COLUMNNAME_C_Period_ID)) {
			if (matchedBudgetLine.getC_Period_ID()==0 || runtimePO instanceof MOrder) {
				removematch = "("+whereMatches.get(0).getName()+"=? OR";
				matchRemoved = whereMatchesSQL.toString().replace(removematch, "");
				whereMatchesSQL = new StringBuffer(matchRemoved);
				whereMatches.remove(0);
			}
			else {
				c++;
				removematch = "("+I_GL_JournalLine.COLUMNNAME_C_Period_ID+"=? OR";
 				matchRemoved = whereMatchesSQL.toString().replace(removematch, I_GL_JournalLine.COLUMNNAME_C_Period_ID+"=? AND");
				whereMatchesSQL = new StringBuffer(matchRemoved);		
			}
		}
	
		//REMOVE 2ND PARAM - AD_OrgDoc_ID=? OR 
		if (whereMatches.get(c).getName().equals(I_GL_JournalLine.COLUMNNAME_AD_OrgDoc_ID)) {
			if (matchedBudgetLine.getAD_OrgDoc_ID()==0) {
				removematch = whereMatches.get(c).getName()+"=? OR";
				matchRemoved = whereMatchesSQL.toString().replace(removematch, "");
				whereMatchesSQL = new StringBuffer(matchRemoved);		
				whereMatches.remove(c);
			} else {
				c++;
				removematch = I_GL_JournalLine.COLUMNNAME_AD_OrgDoc_ID+"=? OR";
				//SWAP AD_OrgDoc_ID = AD_Org_ID as trimming is for MOrder and GL Journal
				matchRemoved = whereMatchesSQL.toString().replace(removematch, MOrder.COLUMNNAME_AD_Org_ID+"=? AND");
				whereMatchesSQL = new StringBuffer(matchRemoved);		
			}
		}
		
		//CHECK IF NEED TO REMOVE SECOND PARAM AS FIRST - (C_BPartner_ID=? OR)
		if (whereMatches.get(c).getName().equals(I_GL_JournalLine.COLUMNNAME_C_BPartner_ID)) {
			if (matchedBudgetLine.getC_BPartner_ID()==0) {
				removematch =  whereMatches.get(c).getName()+"=? OR";
				matchRemoved = whereMatchesSQL.toString().replace(removematch, "");
				whereMatchesSQL = new StringBuffer(matchRemoved);
				whereMatches.remove(c);
			} else {
				c++;
				removematch = I_GL_JournalLine.COLUMNNAME_C_BPartner_ID+"=? OR";
 				matchRemoved = whereMatchesSQL.toString().replace(removematch, MOrder.COLUMNNAME_C_BPartner_ID+"=? AND");
				whereMatchesSQL = new StringBuffer(matchRemoved);	
			}
		}
		
		removematch = " 1=1) AND";
		matchRemoved = whereMatchesSQL.toString().replace(removematch, "");
		whereMatchesSQL = new StringBuffer(matchRemoved);
		//FINAL
		if (runtimePO instanceof MOrder){ //remove "AND Account_ID=?"
			removematch = " Account_ID=? AND";
			matchRemoved = whereMatchesSQL.toString().replace(removematch, "");
			whereMatchesSQL = new StringBuffer(matchRemoved);
			whereMatches.remove(c);
		}			
		whereMatchesIDs = matchesToIDs(whereMatches);
		log.finer("PARAM TRIMMING = SELECT FROM FACT_ACCT WHERE "+whereMatchesSQL+" PARAMS?: "+whereMatchesIDs.toString());
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
	
	/**
	 * SET SOURCE AMOUNT TO CREDIT OR DEBIT
	 */
	public static BudgetLine setAmtSource(BudgetLine line, BigDecimal amt){
		log.fine("public static BudgetLine setAmtSource(BudgetLine line, BigDecimal amt)");
		if (matchedIsCreditAmt)
			line.setAmtSourceCr(amt);
		else line.setAmtSourceDr(amt);
		return line;
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
		if (budgetPercent.compareTo(Env.ZERO)>0) 
			if (RevenueEstimate.multiply(budgetPercent).divide(Env.ONEHUNDRED,2).compareTo(todateAmount)<0) {
				BigDecimal diff = RevenueEstimate.multiply(budgetPercent).divide(Env.ONEHUNDRED,2).subtract(todateAmount);
				return throwBudgetExceedMessage(diff.setScale(2,BigDecimal.ROUND_UP).toString()+", "+budgetPercent.setScale(2,BigDecimal.ROUND_UP).toString()+"% OF "
				+RevenueEstimate.setScale(2,BigDecimal.ROUND_UP).toString()+" REVENUE,  TO-DATE ",todateAmount, whereMatches);			 
			} else log.finest(budgetPercent+"%PERCENT WITHIN BUDGET OF "+RevenueEstimate);

		if (budgetAmount.compareTo(Env.ZERO)>0) {
			if (budgetAmount.compareTo(todateAmount)<0) {
				BigDecimal diff = budgetAmount.subtract(todateAmount);
				return throwBudgetExceedMessage(diff.setScale(2, BigDecimal.ROUND_UP).toString()+", "
				+budgetAmount.setScale(2,BigDecimal.ROUND_UP).toString()+" BUDGET,  TO-DATE ", todateAmount, whereMatches);
			} else log.finest(budgetAmount+" AMOUNT WITHIN BUDGET OF "+RevenueEstimate);
		}
		return null;//no error
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
		if (po instanceof MOrder) {
			MOrder order = (MOrder)po;	
			//PERIOD ID FOR PRESENT MONTH. REMOVED FOR REMATCH IN BUDGET RULES
			int Period_ID = (MPeriod.get(po.getCtx(), order.getCreated(),po.getAD_Org_ID())).getC_Period_ID();
			matches.add(new KeyNamePair(Period_ID,"C_Period_ID"));
			//AD_Org_ID handling same as Period ID
			matches.add(new KeyNamePair(po.getAD_Org_ID(),"AD_OrgDoc_ID"));
			//BPARTNER IS PERMANENT BUT REMOVE FOR REMATCH in BUDGET RULES
			matches.add(new KeyNamePair(order.getC_BPartner_ID(), I_C_Order.COLUMNNAME_C_BPartner_ID));
			//ACCOUNT ID SET TO DUMMY (11100-CHECKING-ACCOUNT) TO ENSURE NO MIXUP WITH JOURNAL MATCHING
			matches.add(new KeyNamePair(508,I_GL_JournalLine.COLUMNNAME_Account_ID));
			if (order.getC_Project_ID()>0) 
				matches.add(new KeyNamePair(order.getC_Project_ID(), I_C_Order.COLUMNNAME_C_Project_ID));	
			else matches.add(new KeyNamePair(0,I_C_Order.COLUMNNAME_C_Project_ID));
			if (order.getC_Activity_ID()>0) 
				matches.add(new KeyNamePair(order.getC_Activity_ID(), I_C_Order.COLUMNNAME_C_Activity_ID));
			else matches.add(new KeyNamePair(0,I_C_Order.COLUMNNAME_C_Activity_ID));
			if (order.getC_Campaign_ID()>0)  
				matches.add(new KeyNamePair(order.getC_Campaign_ID(), I_C_Order.COLUMNNAME_C_Campaign_ID));		
			else matches.add(new KeyNamePair(0,I_C_Order.COLUMNNAME_C_Campaign_ID));
	 		}
		else if (po instanceof MJournalLine) {
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
		log.finer("MATCHES FROM DOC: "+matches.toString());
		return matches;
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
		int orCount = 2; //FOR JOURNALLINE
		if (po instanceof MOrder) orCount = 3;
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
  		.list();
 		//WITH ADDED CRITERIA FIRST C_PERIOD_ID, AD_ORGDOC_ID, C_BPARTNER_ID
		//ITERATE POSSIBLE MATCHES BUT RULE OUT PERIOD, ORG AND PARTNER (FOR PURCHASE) UNTIL EXACT MATCH OR NULL
  		BudgetLine matchedLine = null;

  		int counter = 0; //if counter incremented twice it means extra ambiguous Budget Rules for same match.
  		
  		for (BudgetLine line:matchedLines) {
  			if ((matches.get(0).getKey()!=line.getC_Period_ID()) && (line.getC_Period_ID()!=0)) continue;
  			if ((matches.get(1).getKey()!=line.getAD_OrgDoc_ID()) && (line.getAD_OrgDoc_ID()!=0)) continue;
  			if ((po instanceof MOrder) && (matches.get(2).getKey()!=line.getC_BPartner_ID()) && (line.getC_BPartner_ID()!=0)) continue;
  			matchedLine = line;
  			counter++;
  		}
  		//AMBIGOUS DUE TO PARAMS WITHIN BRACKETS HAVING MORE RECORDS WITH IMPLICIT RULES
  		if (counter>1) throw new AdempiereException(counter+" AMBIGUOUS BUDGET RULES FOUND = "+whereClause);
  		
  		//SET matchedIsCreditAmt FLAG - USED IN getAmtSource(..,..)
  		checkCreditOrDebit(matchedLine);
  		
  		log.finer("LOOKUP BUDGET RULE - matchedBudgetLine: "+matchedLine.toString());
		return matchedLine;
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
	
	/**
	 * @param description - FORMATED RESPONSE ACCORDING TO ITEMS EARLIER
	 * @param totalAmt
	 * @param matches -- OPTIONAL, NOT USED YET
	 * @return STRING RESONSE FOR ERROR HANDLING BY MAIN CALLING CLASS
	 */
	private static String throwBudgetExceedMessage(String description, BigDecimal totalAmt, List<KeyNamePair> matches) {
		log.fine("private static String throwBudgetExceedMessage(..");
		totalAmt = totalAmt.setScale(2);
		log.finer("EXCEED BY "+description+" TOTAL+THIS: " + totalAmt+", TREND: "+PRORATA+budgetCONFIGinstance.getBudgetTrend());
		return "EXCEED > "+description+" TOTAL+THIS: " + totalAmt+", TREND: "+PRORATA+budgetCONFIGinstance.getBudgetTrend();		
	}
	
	/**
	 * MONTH ON MONTH LIST CREATED AS ARRAY OF PERIOD IDS ACROSS FORECAST YEARS
	 * FOR processMonthToMonth(BudgetLine)
	 * @param line
	 * @return false if query null
	 */
	private static List<Integer> createMonthToMonthList(BudgetLine line){
		log.fine("private static boolean createMonthToMonthList(BudgetLine line)");
 		int cnt = 0;
		List<MYear> years = new Query(line.getCtx(),MYear.Table_Name,MYear.COLUMNNAME_FiscalYear+" > ?",line.get_TrxName())
		.setParameters(startYear)
		.list();
		if (years==null) return null;
		List<Integer> returnPeriods = null;
		for (MYear year:years){
			if (year.getFiscalYear().equals(presentYear)){
				//create present year periods
				List<MPeriod> presentPeriods = new Query(line.getCtx(), MPeriod.Table_Name, MPeriod.COLUMNNAME_C_Year_ID+"=?", line.get_TrxName())
				.setParameters(year.getC_Year_ID())
				.setOrderBy(MPeriod.COLUMNNAME_PeriodNo)
				.list();
				returnPeriods = new ArrayList<Integer>();
				for (MPeriod period:presentPeriods){
					returnPeriods.add(period.getC_Period_ID());
				}
			}
			//end - create present year periods
			//MAIN
			List<MPeriod> periodsMOM = new Query(line.getCtx(), MPeriod.Table_Name, MPeriod.COLUMNNAME_C_Year_ID+"=?", line.get_TrxName())
			.setParameters(year.getC_Year_ID())
			//.setOrderBy(MPeriod.COLUMNNAME_C_Period_ID)
			.list();
			if (periodsMOM==null) return null;
			if (periodsMOM.size()!=12) log.warning("PERIOD.SIZE() IS NOT 12 !?");
			//
			// array of pweiod IDs, accessed by logical order of sets of 12. To access i.e. Nth month of the year, loop in *12
 			for (MPeriod period:periodsMOM) {
				mom.add(period.getC_Period_ID());
				cnt++;
			}
		}

		log.finest("MonthToMonth Array: "+mom.toString()+" START YEAR: "+startYear);
		return returnPeriods;
	}
	
	/** ARRAY OF PERIOD IDS FROM createMonthToMonthList
	 * 	FOR MONTH ON MONTH ACROSS THE FORECAST YEARS
	 */
	public static boolean processMonthToMonth(BudgetLine line, MJournal newbudget){	
		log.fine("public static BigDecimal processMonthToMonth(BudgetLine line)");
		paramTrimming(line);
		checkCreditOrDebit(line);
 		if (mom==null || mom.isEmpty() || presentPeriods.isEmpty()) {
 			presentPeriods = createMonthToMonthList(line);
 		}			
 		if (presentPeriods==null) throw new AdempiereException("CANNOT CREATE MONTH-ON-MONTH LIST");
		
		BigDecimal totalAmt = Env.ZERO;
		BigDecimal amtPeriod = Env.ZERO;
		// get Period ID from API

		for (int cnt=0;cnt<12;cnt++){			
			for (int periodCnt=cnt;periodCnt<mom.size();periodCnt+=12){ //increments by 12 to jump into yearly loops
				int Period_ID = mom.get(periodCnt);
				amtPeriod = amtPeriod.add(selectAccountingFacts(line, MORE_EQUAL, null, Period_ID)); //passing period id not year value				
			}
			if (!amtPeriod.equals(Env.ZERO)) {
      	 		//if percent, then calculate amt as percent of revenue..
				totalAmt = amtPeriod.divide(new BigDecimal(forecastYears),2); //temporal .. passing to budgetTrend
				//write to new line rule of new budget (for 12 periods) -- getPeriodNo from Period_ID or deduce!
				//totalAmt = budgetTrend(line, totalAmt, Period_ID); //set isCrDr flag
       	 		BudgetLine newline = new BudgetLine(newbudget);
       	 		newline.setGL_Journal_ID(newbudget.getGL_Journal_ID());
       	 		newline.setAccount_ID(line.getAccount_ID());
       	 		if (line.getPercent().equals(Env.ZERO) && GenerateBudget.p_Percent.equals("N"))
       	 			newline = setAmtSource(newline, totalAmt);
       	 		else {
       	 			//set as percent
       	 			BigDecimal percentage = totalAmt.divide(RevenueEstimate).multiply(Env.ONEHUNDRED);
       	 			percentage.setScale(2);
       	 			newline.setPercent(percentage);
       	 			
       	 		}
        	 	newline.setC_Period_ID(presentPeriods.get(cnt));
       	 		newline.saveEx(line.get_TrxName());
       	 		totalAmt = Env.ZERO; //init for next period
			}
 
		}
		log.finer("PROCESS MONTH ON MONTH - totalAmt: "+totalAmt+" FOR BUDGET LINE: "+line);
		return true;
	}
}
