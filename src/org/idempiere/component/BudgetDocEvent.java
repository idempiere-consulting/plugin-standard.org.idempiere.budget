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

package org.idempiere.component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat; 
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import org.adempiere.base.event.AbstractEventHandler;
import org.adempiere.base.event.IEventTopics; 
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.POWrapper;
import org.compiere.model.I_C_Order;
import org.compiere.model.I_GL_Journal;
import org.idempiere.budget.I_GL_JournalLine;
import org.compiere.model.MFactAcct; 
import org.compiere.model.MJournal;
import org.compiere.model.MJournalLine;
import org.compiere.model.MOrder;
import org.compiere.model.MPeriod;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.idempiere.budget.MBudgetConfig;
import org.osgi.service.event.Event;
import org.idempiere.budget.BudgetLine;

public class BudgetDocEvent extends AbstractEventHandler{
	private static CLogger log = CLogger.getCLogger(BudgetDocEvent.class);
	private String trxName = "";
	private PO po = null;
	private String m_processMsg = ""; 
	private MBudgetConfig budgetCONFIGinstance;
	private BigDecimal yearRevenue = Env.ZERO;  
	private Event event;
	private String yearValue = "";
	private int forecastMonths;
	private boolean isMonthOnMonth = false;
	private boolean isProrata = false;
	private SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
	private Calendar cal = Calendar.getInstance();
	private String prorata = "";
	private static String MORE_EQUAL = ">=";
	private static String EQUAL = "=";
	@Override
	protected void initialize() { 
	//register EventTopics and TableNames
		registerTableEvent(IEventTopics.DOC_BEFORE_COMPLETE, MOrder.Table_Name); 
		registerTableEvent(IEventTopics.DOC_BEFORE_COMPLETE, MJournal.Table_Name); 
		log.info("<<BUDGET>> PLUGIN INITIALIZED");
		}

	/*
	 * @see BUSINESS RULES ON http://wiki.idempiere.org/en/Plugin:_Budgetary_Control#Budget_Configurator 
	 * @see org.adempiere.base.event.AbstractEventHandler#doHandleEvent(org.osgi.service.event.Event)
	 */
	@Override
	protected void doHandleEvent(Event event) {
		String type = event.getTopic();
		this.event = event;
		//testing that it works at login
		setPo(getPO(event));
		setTrxName(po.get_TrxName());
		if (budgetCONFIGinstance == null) {
			log.info("<<BUDGET>> RULES ONE-TIME SETTING STARTED");
			oneTimeSetupRevenue(event);
			log.info("<<BUDGET>> RULES ONE-TIME SETTING DONE");
			}
 
		//ORDER DOCUMENT VALIDATION BEFORE COMPLETE
		if (po instanceof MOrder && IEventTopics.DOC_BEFORE_COMPLETE == type){ 
			log.info(" topic="+event.getTopic()+" po="+po);
			//SET VARIABLES FOR MATCHED BUDGETLINE PERCENT OR AMOUNT
			checkPurchaseBudget();				
			}
			
		//JOURNAL DOCUMENT VALIDATION BEFORE COMPLETE
		//BUDGET CONTROL OVER ACCOUNTING ELEMENT TO EITHER PERCENT OR AMOUNT
		//ACCESS GL BUDGET LINES FOR MATCHING TO JOURNAL-LINES CRITERIA
		if (po instanceof MJournal && IEventTopics.DOC_BEFORE_COMPLETE == type){
			log.info(" topic="+event.getTopic()+" po="+po);
			//SET VARIABLES FOR MATCHED BUDGETLINE PERCENT OR AMOUNT
			checkAccountsBudget();
		}
	}

	/*
	 * PERSIST BUDGET-CONFIG RULES
	 * ONE TIME SETUP REVENUE OVER YEARS RANGE
	 */
	private void oneTimeSetupRevenue(Event event) {
		Calendar cal = Calendar.getInstance();
		//One-time update during login of Revenue total
		//Check BudgetConfig settings
		budgetCONFIGinstance = new Query(po.getCtx(), MBudgetConfig.Table_Name, "", po.get_TrxName())
		.firstOnly();
		//IF MONTH ON MONTH
		if (budgetCONFIGinstance.isMonthOnMonth()) {
			isMonthOnMonth = true;
			//CANNOT PRORATA
			budgetCONFIGinstance.setProrata(false);
		}
		//IF PRORATA THEN CONFIG MONTHS VOID
		if (budgetCONFIGinstance.isProrata()) {
			isProrata = true;
			isMonthOnMonth = false;
			forecastMonths = 0;
		}
		//Throw exception and stop ops if there is no BudgetConfig record for reference.
		if (budgetCONFIGinstance==null) addErrorMessage(event,"NULL BUDGETCONFIG - You Should Stop This Plugin - org.idempiere.budget");
			if (!budgetCONFIGinstance.isActive()) 
				addErrorMessage(event,"NOT ACTIVE BUDGETCONFIG - You Should Stop This Plugin - org.idempiere.budget");
			BigDecimal budgetYear = budgetCONFIGinstance.getGL_Budget_Forecast_Year();
			int yearsRange = budgetYear.intValue();	
			if (budgetYear.intValue()>100) { //HARD SET REVENUE.
				yearRevenue = budgetYear;				 
			}
			else if (yearsRange==0) { //NO ACTION
				yearRevenue = Env.ZERO;					 
			}
			else {
				yearRevenue = revenueConfig(yearsRange); //DEPENDS ON AVERAGE, LAST, 
				if (isProrata) {
					yearRevenue = yearRevenue.divide(new BigDecimal(12),2);
					prorata = " (PRORATA) ";
				}
			}
		}
	
	/*
	 * RETURN TOTAL AMOUNT FOR REVENUE (4XXXX) ACCOUNT ELEMENTS FROM FACTACCT TABLE
	 * OVER NUMBER OF YEARS IN RANGE, AVERAGE(A), AVERAGE+LAST(L), PROGRESSIVE(P)
	 */
	private BigDecimal revenueConfig(int yearsRange) {
		//PERIOD IDS FROM CALENDAR YEARS
		//GET PRESENT YEAR AND SET BACK BY BUDGETCONFIG.FORECASTYEARS
		String presentYear = yearFormat.format(cal.getTime());
		cal.add(Calendar.YEAR, -1);
		String pastYear = yearFormat.format(cal.getTime());
		cal.add(Calendar.YEAR, +1-yearsRange);
		String startYear = yearFormat.format(cal.getTime());
		BigDecimal baseAmt = getBaseAmtFromFacts(0, true, MORE_EQUAL, startYear);
		
		//REVENUE AVERAGE ACROSS RANGE OF YEARS
//		if (budgetCONFIGinstance.getBudgetTrend().equals("A") && yearsRange < 100) { -- DEFAULT VALUE
		BigDecimal sumAmt= baseAmt; //FOR LATER FORMULA USE
		BigDecimal average = baseAmt.divide(new BigDecimal(yearsRange),2);
			baseAmt = average;
//		}	RETAIN AS DEFAULT AND REUSE FOR PROGRESSIVE CALCULATION
			
		//AVERAGE + LAST = ADD AVERAGE AMOUNT IN RANGE TO LAST YEAR'S  
		if (budgetCONFIGinstance.getBudgetTrend().equals("L")) {
			BigDecimal lastYearAmt = getBaseAmtFromFacts(0, true, EQUAL, pastYear);
			baseAmt = lastYearAmt.add(average);
			return baseAmt;
		}
		//APPLY RATE OF CHANGE OVER RANGE TO LAST YEAR'S 
		else if (budgetCONFIGinstance.getBudgetTrend().equals("P")) { 
			BigDecimal startYearAmt = getBaseAmtFromFacts(0, true, EQUAL, startYear);
			BigDecimal lastYearAmt = getBaseAmtFromFacts(0, true, EQUAL, pastYear);
			if (lastYearAmt.equals(Env.ZERO)) 
				throw new AdempiereException("LAST YEAR = "+pastYear+" HAS NO REVENUE");
			else
			{
				BigDecimal rate = startYearAmt.divide(lastYearAmt,2).multiply(Env.ONEHUNDRED);
				baseAmt = rate.multiply(lastYearAmt).add(lastYearAmt);
			}
		}
		//ACCUMULATIVE OVER RANGE OF YEARS
		else if (budgetCONFIGinstance.getBudgetTrend().equals("C")) {
				baseAmt = sumAmt;
		}		
		//YEAR TO-DATE
		else if (budgetCONFIGinstance.getBudgetTrend().equals("T")) {
			baseAmt = getBaseAmtFromFacts(0, true, EQUAL, presentYear);
		}		
		
		return baseAmt;
	}

	/*
	 * GET TOTAL AMOUNT FOR ACCOUNT ELEMENT FROM FACTACCT TABLE
	 * OVER NUMBER OF YEARS IN RANGE
	 * APPLY RULES, IF MONTHS THEN PERIOD IN (<REPLACE WITH PERIODS ASCERTAINED FOR MONTH2MONTH>(EXTERNAL PROCESS)
	 */
	private BigDecimal getBaseAmtFromFacts(int account_ID, boolean revenueOrFacts, String operand, String yearValue) {
		StringBuffer whereClause = new StringBuffer();
				
		Object[] params = {yearValue};
		if (revenueOrFacts) 
			whereClause = new StringBuffer("Account_ID IN (Select C_ElementValue_ID FROM C_ElementValue WHERE Value Like '4%') "); 
		else {//SWAP YEAR REVENUE LOGIC WITH ACCOUNTING ELEMENT LOGIC
			whereClause = new StringBuffer("ACCOUNT_ID = ? AND POSTINGTYPE = 'A' ");
			params = new Object[]{account_ID,yearValue};
		}
		whereClause.append("AND C_PERIOD_ID IN (SELECT C_PERIOD_ID FROM C_PERIOD WHERE C_YEAR_ID IN (SELECT C_YEAR_ID FROM C_YEAR WHERE FISCALYEAR "+operand+" ?))");
		
		List<MFactAcct> facts = new Query(po.getCtx(), MFactAcct.Table_Name, whereClause.toString(), po.get_TrxName())
		.setParameters(params)
		.list();
		BigDecimal baseAmt = Env.ZERO;
		if (facts.isEmpty()) return Env.ZERO;
		
		for (MFactAcct fact:facts) {
			baseAmt = baseAmt.add(fact.getAmtSourceCr());
		}
		if (baseAmt.equals(Env.ZERO)) return Env.ZERO;
		
		return baseAmt;
 	}
	
	/*
	 * SET VARIABLES FOR MATCHED BUDGETLINE PERCENT OR AMOUNT
	 * ONLY PURCHASE ORDERS
	 * USE MATCHES/WHERECLAUSE CONSTRUCT FROM PERIOD OR ACCOUNT-ID,PROJECT,ACTIVITY,CAMPAIGN,BPARTNER
	 * APPLY BUDGET-CONFIG RULES TO BUDGETAMOUNT COMPARE TO TOTALPURCHASES FOR THE YEAR.
	 */
	private void checkPurchaseBudget() {
		BigDecimal percent = Env.ZERO;
		BigDecimal budgetAmount = Env.ZERO;
		
		//MORDER Document Event Validation
		MOrder purchase = new MOrder(po.getCtx(), po.get_ID(), po.get_TrxName());	
		
		//ONLY PURCHASE ORDER   
		if (!purchase.isSOTrx()) { 
			
			//CONSTRUCT MATCHES ARRAY OF DOCUMENT's BPARTNER/PROJECT/ACTIVITY/CAMPAIGN
			List <KeyNamePair> matches = getMatchesFromDoc(purchase);
			
			//CONSTRUCT SQL WHERE CLAUSE FROM MATCHES
			StringBuffer whereClause = matchesWhereClause(matches, purchase);	//FIRST TWO WILL BE 'OR'
			
			/* FETCH MATCHING BUDGET LINE RULE
			 */
			BudgetLine matchedBudgetLine = BudgetLine.matchingBudgetRule(purchase, matches, whereClause);
			
			//MATCHED BUDGETLINE FITTING THE PURCHASE CRITERIA
			if (matchedBudgetLine!=null) {
				if (matchedBudgetLine.getAmtSourceCr().compareTo(Env.ZERO)>0) {
					budgetAmount = matchedBudgetLine.getAmtSourceCr();
					//IF PRORATA, DIVIDE BY 12 MONTHS
					if (isProrata && matchedBudgetLine.getC_Period_ID()==0) {//PRORATA NOT FOR SPECIFIC PERIOD  
						budgetAmount.divide(new BigDecimal(12),2);			//NOR FOR PERCENTAGE
						prorata = " (PRORATA) ";
					}
						
				}			
				else 
  					percent = matchedBudgetLine.getPercent();
				
				/*
				 * GET TOTAL FROM ALL PREPARED/COMPLETED PURCHASES FOR THE YEAR FOR BUDGET ONCE IN THIS DOCUMENT   
				 * PERFORM REMOVAL OF UNRELATED PARAMS 
				 */
				BigDecimal totalPurchases = Env.ZERO;
				cal = Calendar.getInstance();
				yearValue = yearFormat.format(cal.getTime());
						
				//REMOVE THIRD PARAM - AND Account_ID=?
				String removematch = " AND "+matches.get(2).getName()+"=?";
				String matchRemoved = whereClause.toString().replace(removematch, "");
				whereClause = new StringBuffer(matchRemoved);
				matches.remove(2);
			
				//REMOVE FIRST PARAM - C_PERIOD_ID=? OR
				//TODO FUTURE FINE-TUNE TO EXTRACT MONTH FOR PERIOD OF PURCHASES
				//		if (matchedBudgetLine.getC_Period_ID()==0) {
				removematch = matches.get(0).getName()+"=? OR ";
				matchRemoved = whereClause.toString().replace(removematch, "");
				whereClause = new StringBuffer(matchRemoved);
				matches.remove(0);
								
				//CHECK IF NEED TO REMOVE SECOND PARAM AS FIRST - (C_BPartner_ID=? OR 1=1)
				if (matchedBudgetLine.getC_BPartner_ID()==0) {
					removematch = "("+matches.get(0).getName()+"=? OR 1=1) AND ";
					matchRemoved = whereClause.toString().replace(removematch, "");
					whereClause = new StringBuffer(matchRemoved);
					matches.remove(0);
				}
				//SQL EXECUTION
				String thisClause = "IsSOTrx = 'N' AND DocStatus IN ('IP','CO') AND " + whereClause.toString()
					+" AND (SELECT EXTRACT(ISOYEAR FROM DateOrdered)) = '"+yearValue+"'"; //match with criteria in Order
				List<MOrder> allPurchases = new Query(po.getCtx(), MOrder.Table_Name, thisClause, po.get_TrxName())
				.setParameters(BudgetLine.matchesToIDs(matches))
				.list();
				for (MOrder pastPurchase:allPurchases) {
					totalPurchases = totalPurchases.add(pastPurchase.getGrandTotal());
				}
				totalPurchases = totalPurchases.add(purchase.getGrandTotal());
				//IF PRORATA TAKE ONLY PRESENT MONTH(S) PURCHASES
				//IF ISMONTHONMONTH TAKE SAME PERIODS FROM PREVUOUS YEARS AND APPLY AVERAGE/PRGRESSIVE
				if (!percent.equals(Env.ZERO)) 
					if (yearRevenue.multiply(percent).divide(Env.ONEHUNDRED,2).compareTo(totalPurchases)<0) {
						BigDecimal diff = yearRevenue.multiply(percent).divide(Env.ONEHUNDRED,2).subtract(totalPurchases);
						throwBudgetExceedMessage(diff.setScale(2,BigDecimal.ROUND_UP).toString()+", "+percent.setScale(2,BigDecimal.ROUND_UP).toString()+"% OF "+yearRevenue.setScale(2,BigDecimal.ROUND_UP).toString()+" REVENUE, PURCHASES ",totalPurchases, matches);			 
					} else log.fine("PERCENT WITHIN BUDGET "+event);
			
				if (!budgetAmount.equals(Env.ZERO)) {
					if (budgetAmount.compareTo(totalPurchases)<0) {
						BigDecimal diff = budgetAmount.subtract(totalPurchases);
						throwBudgetExceedMessage(diff.setScale(2, BigDecimal.ROUND_UP).toString()+", "+budgetAmount.setScale(2,BigDecimal.ROUND_UP).toString()+" BUDGET, PURCHASES ", totalPurchases, matches);
					} else log.fine("AMOUNT WITHIN BUDGET "+event);
				}
			}
		}
	}
	/*
	 * SET VARIABLES FOR MATCHED BUDGETLINE PERCENT OR AMOUNT
	 * ONLY ACCOUNTING FACTS
	 * USE MATCHES/WHERECLAUSE CONSTRUCT FROM PERIOD,BPARTNER,<FLAG>ACCOUNT-ID,PROJECT,ACTIVITY,CAMPAIGN
	 * APPLY BUDGET-CONFIG RULES TO BUDGETAMOUNT COMPARE TO TOTALPURCHASES FOR THE YEAR.
	 */
	private void checkAccountsBudget() {
		BigDecimal percent = Env.ZERO;
		BigDecimal budgetAmount = Env.ZERO;
		//FETCHING JOURNAL LINES
		List<MJournalLine> journalLines = BudgetLine.getJournalLines((MJournal)po); //FETCHING VALIDATING DOCUMENT JOURNAL LINES

		//FETCHING BUDGET LINES
		MJournal budget = new Query(po.getCtx(), MJournal.Table_Name, "PostingType = 'B'", po.get_TrxName())
			.setOnlyActiveRecords(true)
			.firstOnly();
			
		//ITERATING JOURNAL LINES
		for (MJournalLine journalLine:journalLines) {
			List <KeyNamePair> matches = getMatchesFromDoc(journalLine);	
			//CONSTRUCT SQL WHERE CLAUSE FROM MATCHES
			int orCount = 1;
			StringBuffer whereClause = matchesWhereClause(matches, journalLine);	//ONLY FIRST WILL BE 'OR'			
			/*
			 * FETCH MATCHING BUDGET LINE RULE
			 */
			BudgetLine matchedBudgetLine = BudgetLine.matchingBudgetRule(journalLine, matches, whereClause);					
			
			//MATCHED BUDGETLINE FITTING THE JOURNALLINE CRITERIA (MUST HAVE ACCOUNTING ELEMENT VALUE)
			// MUST CHECK PERIOD RETURNED IS SAME OR IS ZERO
 
			if (matchedBudgetLine.getAmtSourceCr().compareTo(Env.ZERO)>0){
				budgetAmount = matchedBudgetLine.getAmtSourceCr();	//IF PRORATA, DIVIDE BY 12 MONTHS
				if (isProrata && matchedBudgetLine.getC_Period_ID()==0) {//BUT NOT FOR PERIOD SPECIFIC BUDGET
					budgetAmount.divide(new BigDecimal(12),2);			//NOR FOR PERCENTAGE
					prorata = " (PRORATA) ";
				}
			}
			else {
				percent = matchedBudgetLine.getPercent();
			}
			
			//GET TOTAL OF ALL RELATED ACCOUNTING FACTS FOR THE YEAR <WITH RULES APPLIED>
			cal = Calendar.getInstance();
			yearValue = yearFormat.format(cal.getTime());
				BigDecimal totFactAmt = getBaseAmtFromFacts(matchedBudgetLine.getAccount_ID(), false, MORE_EQUAL, yearValue);
				totFactAmt = totFactAmt.add(journalLine.getAmtSourceCr());
				if (!percent.equals(Env.ZERO)) 
					if (yearRevenue.multiply(percent).divide(Env.ONEHUNDRED,2).compareTo(totFactAmt)<0) {
						BigDecimal diff = yearRevenue.multiply(percent).divide(Env.ONEHUNDRED,2).subtract(totFactAmt);
						throwBudgetExceedMessage(diff.setScale(2, BigDecimal.ROUND_UP).toString()+", "+percent.setScale(2,BigDecimal.ROUND_UP).toString()+"% OF "+yearRevenue.setScale(2,BigDecimal.ROUND_UP)+prorata+" REVENUE FOR ACCOUNT "
								+journalLine.getAccountElementValue().toString()+", FACTS ", totFactAmt, matches);			 
					} else log.fine("PERCENT WITHIN BUDGET "+event);
				if (!budgetAmount.equals(Env.ZERO)) {
					if (budgetAmount.compareTo(totFactAmt)<0) {
						BigDecimal diff = budgetAmount.subtract(totFactAmt);
						throwBudgetExceedMessage(diff.setScale(2, BigDecimal.ROUND_UP).toString()+", "+budgetAmount.setScale(2,BigDecimal.ROUND_UP).toString()+" BUDGET, FACTS ", totFactAmt, matches);
					} else log.fine("AMOUNT WITHIN BUDGET "+event);
				}
		}
	}

	/*
	 * EXTRACT MATCHING ELEMENTS ACCORDING TO DOCUMENT OBJECT
	 * ARRANGE IN KEYNAMEPAIR ARRAY FOR REUSE
	 * PURCHASE - PERIOD OR PARTNER / ACCOUNT/PROJECT/ACTIVITY/CAMPAIGN
	 * FACTS - PERIOD OR ACCOUNT/PROJECT/ACTIVITY/CAMPAIGN
	 */
	private List<KeyNamePair> getMatchesFromDoc (PO po) {
 		List<KeyNamePair> matches = new ArrayList<KeyNamePair>();
		if (po instanceof MOrder) {
			MOrder order = (MOrder)po;	
			//PERIOD ID FOR PRESENT MONTH. REMOVED FOR REMATCH IN BUDGET RULES 
			int Period_ID = MPeriod.getC_Period_ID(po.getCtx(), order.getCreated());
			matches.add(new KeyNamePair(Period_ID,"C_Period_ID"));
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
		return matches;
	}
	/*
	 * MATCH BETWEEN DOCUMENT CRITERIA AND BUDGET RULES
	 * COUNT = 1 IF JOURNALLINE AS ONLY FIRST MATCH IS OR, PURCHASE HAS FIRST TWO OR
	 */
	private StringBuffer matchesWhereClause(List<KeyNamePair> matches, PO po) {
		int orCount = 1; //FOR JOURNALLINE
		if (po instanceof MOrder) orCount = 2;
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
		return whereClause;
	}
	
	// UTILS
	private void throwBudgetExceedMessage(String description, BigDecimal totalAmt, List<KeyNamePair> matches) {
		addErrorMessage(event,"EXCEED BY "+description+" TOTAL+THIS: " + totalAmt+prorata);		
	}

	private void setPo(PO eventPO) {
		 po = eventPO;
	}

	private void setTrxName(String get_TrxName) {
		trxName = get_TrxName;		
	}
}
