package org.idempiere.budget;

import java.math.BigDecimal; 
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.adempiere.exceptions.AdempiereException; 
import org.compiere.model.I_C_Order;
import org.compiere.model.I_GL_Journal;
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
import org.idempiere.component.BudgetDocEvent; 

public class BudgetUtils extends BudgetDocEvent{
	public BudgetUtils(PO po){
		this.po = po;
	}
	
	private static PO po;
	private static CLogger log = CLogger.getCLogger(BudgetUtils.class);
	private static BigDecimal yearRevenue = Env.ZERO;  
	private String yearValue = "";
	private static int forecastMonths;
	private static boolean isMonthOnMonth = false;
	private static boolean isProrata = false;
	private SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
	private Calendar cal = Calendar.getInstance();
	private String prorata = "";
	private String MORE_EQUAL = ">=";
	private String EQUAL = "=";
	private static boolean matchedIsCreditAmt = true;
	/*
	 * PERSIST BUDGET-CONFIG RULES
	 * ONE TIME SETUP REVENUE OVER YEARS RANGE
	 */
	public String oneTimeSetupRevenue() {
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
		if (budgetCONFIGinstance==null) return "NULL BUDGETCONFIG - You Should Stop This Plugin - org.idempiere.budget";
		if (!budgetCONFIGinstance.isActive()) 
			return "NOT ACTIVE BUDGETCONFIG - You Should Stop This Plugin - org.idempiere.budget";
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
		
		return null; //NO ERROR
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
		BigDecimal baseAmt = getFactsAmts(null, MORE_EQUAL, startYear);
		log.fine("CAL : "+cal.toString());
		log.info("Revenue Setting - YearsRange "+yearsRange+", SumAmt = "+baseAmt);
		//REVENUE AVERAGE ACROSS RANGE OF YEARS
//		if (budgetCONFIGinstance.getBudgetTrend().equals("A") && yearsRange < 100) { -- DEFAULT VALUE
		BigDecimal sumAmt= baseAmt; //FOR LATER FORMULA USE
		BigDecimal average = baseAmt.divide(new BigDecimal(yearsRange),2);
			baseAmt = average;
//		}	RETAIN AS DEFAULT AND REUSE FOR PROGRESSIVE CALCULATION
			
		//AVERAGE + LAST = ADD AVERAGE AMOUNT IN RANGE TO LAST YEAR'S  
		if (budgetCONFIGinstance.getBudgetTrend().equals("L")) {
			BigDecimal lastYearAmt = getFactsAmts(null, EQUAL, pastYear);
			baseAmt = lastYearAmt.add(average);
			return baseAmt;
		}
		//APPLY RATE OF CHANGE OVER RANGE TO LAST YEAR'S 
		else if (budgetCONFIGinstance.getBudgetTrend().equals("P")) { 
			BigDecimal startYearAmt = getFactsAmts(null, EQUAL, startYear);
			BigDecimal lastYearAmt = getFactsAmts(null, EQUAL, pastYear);
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
			baseAmt = getFactsAmts(null, EQUAL, presentYear);
		}		
		
		return baseAmt;
	}
	/*
	 * GET TOTAL AMOUNT FOR ACCOUNT ELEMENT FROM FACTACCT TABLE
	 * OVER NUMBER OF YEARS IN RANGE
	 * APPLY RULES, IF MONTHS THEN PERIOD IN (<REPLACE WITH PERIODS ASCERTAINED FOR MONTH2MONTH>(EXTERNAL PROCESS)
	 */
	private BigDecimal getFactsAmts(BudgetLine line, String operand, String yearValue) {
		StringBuffer whereClause = new StringBuffer();
				
		Object[] params = {yearValue};
		if (line==null) 
			whereClause = new StringBuffer("Account_ID IN (Select C_ElementValue_ID FROM C_ElementValue WHERE Value Like '4%') "); 
		else {//SWAP YEAR REVENUE LOGIC WITH ACCOUNTING ELEMENT LOGIC
			whereClause = new StringBuffer("ACCOUNT_ID = ? AND POSTINGTYPE = 'A' ");
			params = new Object[]{line.getAccount_ID(),yearValue};
		}
		whereClause.append("AND C_PERIOD_ID IN (SELECT C_PERIOD_ID FROM C_PERIOD WHERE C_YEAR_ID IN (SELECT C_YEAR_ID FROM C_YEAR WHERE FISCALYEAR "+operand+" ?))");
		
		List<MFactAcct> facts = new Query(po.getCtx(), MFactAcct.Table_Name, whereClause.toString(), po.get_TrxName())
		.setParameters(params)
		.list();
		BigDecimal baseAmt = Env.ZERO;
		if (facts.isEmpty()) return Env.ZERO;
		
		for (MFactAcct fact:facts) {
			baseAmt = baseAmt.add(getAmtSource(fact, null));
		}
		if (baseAmt.equals(Env.ZERO)) return Env.ZERO;
		
		return baseAmt;
 	}
	/*
	 * HELPER METHOD TO RETURN AMOUNT EITHER CREDIT OR DEBIT SIDE; FACT, JOURNALLINE OR BUDGETLINE
	 */
	private BigDecimal getAmtSource(MFactAcct fact, MJournalLine jline) {
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
	
	/*
	 * SET VARIABLES FOR MATCHED BUDGETLINE PERCENT OR AMOUNT
	 * ONLY PURCHASE ORDERS
	 * USE MATCHES/WHERECLAUSE CONSTRUCT FROM PERIOD OR ACCOUNT-ID,PROJECT,ACTIVITY,CAMPAIGN,BPARTNER
	 * APPLY BUDGET-CONFIG RULES TO BUDGETAMOUNT COMPARE TO TOTALPURCHASES FOR THE YEAR.
	 */
	public String checkPurchaseBudget() {
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
			BudgetLine matchedBudgetLine = matchingBudgetRule(purchase, matches, whereClause);
			
			//MATCHED BUDGETLINE FITTING THE PURCHASE CRITERIA
			if (matchedBudgetLine!=null) {
				percent = matchedBudgetLine.getPercent();	
				if (percent.compareTo(Env.ZERO)==0){
 					budgetAmount = getAmtSource(null, matchedBudgetLine);
 					if (budgetAmount.compareTo(Env.ZERO)>0){
					//IF PRORATA, DIVIDE BY 12 MONTHS
 						if (isProrata && matchedBudgetLine.getC_Period_ID()==0) {//PRORATA NOT FOR SPECIFIC PERIOD  
 							budgetAmount = budgetAmount.divide(new BigDecimal(12),2);			//NOR FOR PERCENTAGE
 							prorata = " (PRORATA) ";
 						}	
 					}
				}					
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
				String thisClause = "IsSOTrx = 'N' AND DocStatus = 'CO' AND " + whereClause.toString()
					+" AND (SELECT EXTRACT(ISOYEAR FROM DateOrdered)) = '"+yearValue+"'"; //match with criteria in Order
				List<MOrder> allPurchases = new Query(po.getCtx(), MOrder.Table_Name, thisClause, po.get_TrxName())
				.setParameters(matchesToIDs(matches))
				.list();
				for (MOrder pastPurchase:allPurchases) {
					totalPurchases = totalPurchases.add(pastPurchase.getGrandTotal());
				}
				totalPurchases = totalPurchases.add(purchase.getGrandTotal());
				//IF PRORATA TAKE ONLY PRESENT MONTH(S) PURCHASES
				//IF ISMONTHONMONTH TAKE SAME PERIODS FROM PREVUOUS YEARS AND APPLY AVERAGE/PRGRESSIVE
				if (percent.compareTo(Env.ZERO)>0) 
					if (yearRevenue.multiply(percent).divide(Env.ONEHUNDRED,2).compareTo(totalPurchases)<0) {
						BigDecimal diff = yearRevenue.multiply(percent).divide(Env.ONEHUNDRED,2).subtract(totalPurchases);
						return throwBudgetExceedMessage(diff.setScale(2,BigDecimal.ROUND_UP).toString()+", "+percent.setScale(2,BigDecimal.ROUND_UP).toString()+"% OF "+yearRevenue.setScale(2,BigDecimal.ROUND_UP).toString()+" REVENUE, PURCHASES ",totalPurchases, matches);			 
					} else log.fine("PERCENT WITHIN BUDGET "+event);
			
				if (budgetAmount.compareTo(Env.ZERO)>0) {
					if (budgetAmount.compareTo(totalPurchases)<0) {
						BigDecimal diff = budgetAmount.subtract(totalPurchases);
						return throwBudgetExceedMessage(diff.setScale(2, BigDecimal.ROUND_UP).toString()+", "+budgetAmount.setScale(2,BigDecimal.ROUND_UP).toString()+" BUDGET, PURCHASES ", totalPurchases, matches);
					} else log.fine("AMOUNT WITHIN BUDGET "+event);
				}
			}
		}
		return null;
	}
	/*
	 * SET VARIABLES FOR MATCHED BUDGETLINE PERCENT OR AMOUNT
	 * ONLY ACCOUNTING FACTS
	 * USE MATCHES/WHERECLAUSE CONSTRUCT FROM PERIOD,BPARTNER,<FLAG>ACCOUNT-ID,PROJECT,ACTIVITY,CAMPAIGN
	 * APPLY BUDGET-CONFIG RULES TO BUDGETAMOUNT COMPARE TO TOTALPURCHASES FOR THE YEAR.
	 */
	public String checkAccountsBudget() {
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
			BudgetLine matchedBudgetLine = matchingBudgetRule(journalLine, matches, whereClause);					
			
			//MATCHED BUDGETLINE FITTING THE JOURNALLINE CRITERIA (MUST HAVE ACCOUNTING ELEMENT VALUE)
			// MUST CHECK PERIOD RETURNED IS SAME OR IS ZERO
			percent = matchedBudgetLine.getPercent();

			if (percent.compareTo(Env.ZERO)==0){
				budgetAmount = getAmtSource(null, matchedBudgetLine);			
				if (budgetAmount.equals(Env.ZERO)) {
					//IF PRORATA, DIVIDE BY 12 MONTHS
					if (isProrata && matchedBudgetLine.getC_Period_ID()==0) {//BUT NOT FOR PERIOD SPECIFIC BUDGET
						budgetAmount = budgetAmount.divide(new BigDecimal(12),2);			//NOR FOR PERCENTAGE
						prorata = " (PRORATA) ";
					}
				}
			}
			
			//GET TOTAL OF ALL RELATED ACCOUNTING FACTS FOR THE YEAR <WITH RULES APPLIED>
			cal = Calendar.getInstance();
			yearValue = yearFormat.format(cal.getTime());
			BigDecimal totFactAmt = getFactsAmts(matchedBudgetLine, EQUAL, yearValue);
			totFactAmt = totFactAmt.add(getAmtSource(null, journalLine));
			if (percent.compareTo(Env.ZERO)>0) 
				if (yearRevenue.multiply(percent).divide(Env.ONEHUNDRED,2).compareTo(totFactAmt)<0) {
					BigDecimal diff = yearRevenue.multiply(percent).divide(Env.ONEHUNDRED,2).subtract(totFactAmt);
					return throwBudgetExceedMessage(diff.setScale(2, BigDecimal.ROUND_UP).toString()+", "+percent.setScale(2,BigDecimal.ROUND_UP).toString()+"% OF "+yearRevenue.setScale(2,BigDecimal.ROUND_UP)+prorata+" REVENUE FOR ACCOUNT "
							+journalLine.getAccountElementValue().toString()+", FACTS ", totFactAmt, matches);			 
				} else log.fine("PERCENT WITHIN BUDGET "+event);
			if (budgetAmount.compareTo(Env.ZERO)>0) {
				if (budgetAmount.compareTo(totFactAmt)<0) {
					BigDecimal diff = budgetAmount.subtract(totFactAmt);
					return throwBudgetExceedMessage(diff.setScale(2, BigDecimal.ROUND_UP).toString()+", "+budgetAmount.setScale(2,BigDecimal.ROUND_UP).toString()+" BUDGET, FACTS ", totFactAmt, matches);
				} else log.fine("AMOUNT WITHIN BUDGET "+event);
			}
		}
		return null;
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
			//TODO GET PERIOD ACCORDING TO ORG CALENDAR
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
	private String throwBudgetExceedMessage(String description, BigDecimal totalAmt, List<KeyNamePair> matches) {
		return "EXCEED BY "+description+" TOTAL+THIS: " + totalAmt+prorata+", TREND:"+budgetCONFIGinstance.getBudgetTrend();		
	}
	/*
	 * APPLY MATCHES TO WHERE CLAUSE IN SEARCH OF BUDGET LINES
	 */
	public static  BudgetLine matchingBudgetRule(PO po, List<KeyNamePair>matches, StringBuffer whereClause) {
		//FETCH ONLY EXACT BUDGETLINE THAT HAS DOCUMENT'S CRITERIA (EFFICIENT)
		//WHERE CLAUSE FROM DOCUMENT'S MAIN CRITERIA
 		MJournal budget = new Query(po.getCtx(), MJournal.Table_Name, "PostingType='B'", po.get_TrxName())
		.setOnlyActiveRecords(true)
		.first();	 
		//WITH ADDED CRITERIA FIRST C_PERIOD_ID, C_BPARTNER_ID
		Object[] params = matchesToIDs(matches);
		if (params==null) return null;
		
		//ITERATE POSSIBLE MATCHES BUT RULE OUT PERIOD AND PARTNER (FOR PURCHASE) UNTIL EXACT MATCH OR NULL
  		BudgetLine matchedLine = null;
  		int counter = 0; //if counter incremented twice it means extra ambiguous Budget Rules for same match.

  		List<BudgetLine> matchedLines = new Query(po.getCtx(), BudgetLine.Table_Name, whereClause.toString() 
  		+" AND "+I_GL_Journal.COLUMNNAME_GL_Journal_ID+"="+budget.getGL_Journal_ID(), po.get_TrxName())
  		.setParameters(params)
  		.list();
  		
  		for (BudgetLine line:matchedLines) {
  			if ((matches.get(0).getKey()!=line.getC_Period_ID()) && (line.getC_Period_ID()!=0)) continue;
  			if ((po instanceof MOrder) && (matches.get(1).getKey()!=line.getC_BPartner_ID()) && (line.getC_BPartner_ID()!=0)) continue;
  			matchedLine = line;
  			counter++;
  		}
  		if (counter>1) throw new AdempiereException(counter+" AMBIGUOUS BUDGET RULES FOUND = "+whereClause);
  		
  		//SET matchedIsCreditAmt FLAG - USED IN getAmtSource(..,..)
  		if (matchedLine.getAmtSourceCr().compareTo(Env.ZERO)>0)
  				matchedIsCreditAmt = true;
  		else 	matchedIsCreditAmt = false;
  		
		return matchedLine;
	}
 
	public static Object[] matchesToIDs(List<KeyNamePair> matches) {
		ArrayList<Object> params = new ArrayList<Object>();
		for (KeyNamePair match:matches) {
			if (match.getKey()>0)
				params.add((new Integer(match.getID())).intValue());
		}
		if (params.isEmpty()) return null;
		return params.toArray();
	}
}
