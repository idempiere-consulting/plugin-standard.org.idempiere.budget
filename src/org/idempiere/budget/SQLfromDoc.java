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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.compiere.model.I_C_Order;
import org.compiere.model.I_GL_Journal;
import org.compiere.model.I_GL_JournalLine;
import org.compiere.model.MAccount;
import org.compiere.model.MJournal;
import org.compiere.model.MJournalLine;
import org.compiere.model.MOrder;
import org.compiere.model.MPeriod;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.KeyNamePair;

public class SQLfromDoc {

	private List<KeyNamePair> whereMatches =  new ArrayList<KeyNamePair>();
	private StringBuffer whereMatchesSQL = new StringBuffer();
	private List<Object> whereMatchesIDs = new ArrayList<Object>(); 
	PO po = null;
	private int previousMonths;
	
	public SQLfromDoc(PO po, int previousMonths){
		this.po = po; 
		this.previousMonths = previousMonths; 
		whereMatches = new ArrayList<KeyNamePair>();
		whereMatchesSQL = new StringBuffer();
		whereMatchesIDs = new ArrayList<Object>();
	}
	public SQLfromDoc() {
		
	}
	
	private static CLogger log = CLogger.getCLogger(SQLfromDoc.class);

	public void setWhereMatches(){
		whereMatches = matchesFromDoc();
		whereMatchesSQL = whereClauseMatches(whereMatches);
		whereMatchesIDs = matchesToIDs(whereMatches);
	}	
	public List <KeyNamePair>  getWhereMatches() {		
		return whereMatches;
	}
	public StringBuffer getWhereMatchesSQL(){
		return whereMatchesSQL;
	}
	public List<Object> getWhereMatchesIDs(){
		return whereMatchesIDs;
	}
	/**
	 * using whereClauseExistMatches
	 */
	public void setSQLMatching(){
		whereMatches = matchesFromDoc();
		whereMatchesSQL = whereClauseExistMatches(whereMatches);
		whereMatchesIDs = matchesToIDs(whereMatches);
	}
	
	/** Where Clause for Existing Matches '=? AND"
	 *  this method after <matchesFromDoc(po)>
	 *  Zero value not included
	 */
	public StringBuffer whereClauseExistMatches(List<KeyNamePair>matches){
		StringBuffer whereClause = new StringBuffer(" ");
		boolean past = false;
		for (int i=0; i<matches.size(); i++){
			KeyNamePair match = matches.get(i);
			if (past) whereClause.append("AND ");
			if (match.getKey()>0)
				whereClause.append(match.getName()+"=? ");
			else
				whereClause.append(match.getName()+" is null ");
			past = true;
			
		}
		return whereClause;
	}
	
	/**
	 * parse sql clause
	 * count = 1 if journalline as only first match is or, purchase has first two or
	 * @param matches
	 * @param po
	 * @return whereclause
	 */
	private StringBuffer whereClauseMatches(List<KeyNamePair> matches) {
		log.info("StringBuffer whereClauseMatches(List<KeyNamePair> matches, PO po)");
		int orCount = 3; //for purchasing doctypes
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
			this.whereMatches.remove(2);
		}
		String matchRemoved = whereClause.toString().replace(removematch, "");
		whereClause = new StringBuffer(matchRemoved);
		removematch = "AD_OrgDoc_ID is null OR "; //patch for GenerateBudget process Purchase rule
		if (whereClause.toString().contains(removematch)){
			this.whereMatches.remove(1);
		}
		matchRemoved = whereClause.toString().replace(removematch, "");
		whereClause = new StringBuffer(matchRemoved);	
		log.finer("WHERE CLAUSE MATCHES: "+whereClause.toString());
		return whereClause;
	}

	/**
	 * extract matching elements according to document object
	 * arrange in keynamepair array for reuse
	 * purchase - period or partner / account/project/activity/campaign
	 * facts - period or account/project/activity/campaign
	 * @param po
	 * @return matches
	 */
	private List<KeyNamePair> matchesFromDoc () {
		log.fine("List<KeyNamePair> matchesFromDoc (PO po)");
		List<KeyNamePair> matches = new ArrayList<KeyNamePair>();
		if (po instanceof MBudgetPlanLine) {
			MBudgetPlanLine budgetLine = (MBudgetPlanLine)po;	
			//get Period ID  - remove for rematch
			if (budgetLine.getC_Period_ID()>0)
				matches.add(new KeyNamePair(budgetLine.getC_Period_ID(), I_GL_Journal.COLUMNNAME_C_Period_ID));
			//AD_Org_ID handling same as Period ID in MOrder
			if (budgetLine.getAD_OrgDoc_ID()>0)
				matches.add(new KeyNamePair(po.getAD_Org_ID(),"AD_Org_ID"));
			//Account ID is mandatory in GL Journal
			if (budgetLine.getAccount_ID()>0)
				matches.add(new KeyNamePair(budgetLine.getAccount_ID(), "Account_ID"));
			if (budgetLine.getC_Project_ID()>0) 
				matches.add(new KeyNamePair(budgetLine.getC_Project_ID(), I_GL_JournalLine.COLUMNNAME_C_Project_ID));
			else matches.add(new KeyNamePair(0,I_GL_JournalLine.COLUMNNAME_C_Project_ID));
			if (budgetLine.getC_Activity_ID()>0) 
				matches.add(new KeyNamePair(budgetLine.getC_Activity_ID(), I_GL_JournalLine.COLUMNNAME_C_Activity_ID));
			else matches.add(new KeyNamePair(0,I_GL_JournalLine.COLUMNNAME_C_Activity_ID)); 
			if (budgetLine.getC_Campaign_ID()>0) 
				matches.add(new KeyNamePair(budgetLine.getC_Campaign_ID(), I_GL_JournalLine.COLUMNNAME_C_Campaign_ID));
			else matches.add(new KeyNamePair(0,I_GL_JournalLine.COLUMNNAME_C_Campaign_ID));
			if (budgetLine.getC_BPartner_ID()>0)
				matches.add(new KeyNamePair(budgetLine.getC_BPartner_ID(), I_GL_JournalLine.COLUMNNAME_C_BPartner_ID));
		}
		else if (po instanceof MJournalLine) {
			MJournalLine journalLine = (MJournalLine)po;	
			//get Period ID from parent Journal - remove for rematch
			MJournal journal = new Query(po.getCtx(), MJournal.Table_Name, I_GL_Journal.COLUMNNAME_GL_Journal_ID+"=?", po.get_TrxName())
			.setParameters(journalLine.getGL_Journal_ID()).firstOnly();
			matches.add(new KeyNamePair(journal.getC_Period_ID(), I_GL_Journal.COLUMNNAME_C_Period_ID));
			//AD_Org_ID handling same as Period ID in MOrder
			matches.add(new KeyNamePair(MAccount.get(po.getCtx(), journalLine.getC_ValidCombination_ID()).getAD_Org_ID(),"AD_OrgDoc_ID"));
			//Account ID is mandatory in GL Journal
			matches.add(new KeyNamePair(MAccount.get(po.getCtx(), journalLine.getC_ValidCombination_ID()).getAccount_ID(), "Account_ID"));
			if (MAccount.get(po.getCtx(), journalLine.getC_ValidCombination_ID()).getC_Project_ID()>0) 
				matches.add(new KeyNamePair(MAccount.get(po.getCtx(), journalLine.getC_ValidCombination_ID()).getC_Project_ID(), I_GL_JournalLine.COLUMNNAME_C_Project_ID));
			else matches.add(new KeyNamePair(0,I_GL_JournalLine.COLUMNNAME_C_Project_ID));
			if (journalLine.getC_Activity_ID()>0) 
				matches.add(new KeyNamePair(journalLine.getC_Activity_ID(), I_GL_JournalLine.COLUMNNAME_C_Activity_ID));
			else matches.add(new KeyNamePair(0,I_GL_JournalLine.COLUMNNAME_C_Activity_ID)); 
			if (journalLine.getC_Campaign_ID()>0) 
				matches.add(new KeyNamePair(journalLine.getC_Campaign_ID(), I_GL_JournalLine.COLUMNNAME_C_Campaign_ID));
			else matches.add(new KeyNamePair(0,I_GL_JournalLine.COLUMNNAME_C_Campaign_ID));
			if (MAccount.get(po.getCtx(), journalLine.getC_ValidCombination_ID()).getC_BPartner_ID()>0)
				matches.add(new KeyNamePair(MAccount.get(po.getCtx(), journalLine.getC_ValidCombination_ID()).getC_BPartner_ID(), I_GL_JournalLine.COLUMNNAME_C_BPartner_ID));
			else matches.add(new KeyNamePair(0,I_GL_JournalLine.COLUMNNAME_C_BPartner_ID));
			if (MAccount.get(po.getCtx(), journalLine.getC_ValidCombination_ID()).getM_Product_ID()>0)
				matches.add(new KeyNamePair(MAccount.get(po.getCtx(), journalLine.getC_ValidCombination_ID()).getM_Product_ID(), I_GL_JournalLine.COLUMNNAME_M_Product_ID));
			else matches.add(new KeyNamePair(0,I_GL_JournalLine.COLUMNNAME_M_Product_ID));
		}
		else {//for all DocType - Purchase, Invoice and Payment
			//period id for present month. removed for rematch in budget rules
			int Period_ID = (MPeriod.get(po.getCtx(), po.getCreated(),po.getAD_Org_ID())).getC_Period_ID();
			matches.add(new KeyNamePair(Period_ID,"C_Period_ID"));
			//AD_Org_ID handling same as Period ID
			matches.add(new KeyNamePair(po.getAD_Org_ID(),"AD_OrgDoc_ID"));
			//bpartner is permanent but remove for rematch in budget rules
			matches.add(new KeyNamePair(po.get_ValueAsInt(I_C_Order.COLUMNNAME_C_BPartner_ID), I_C_Order.COLUMNNAME_C_BPartner_ID));
			if (po.get_ValueAsInt(I_C_Order.COLUMNNAME_C_Project_ID)>0) 
				matches.add(new KeyNamePair(po.get_ValueAsInt(I_C_Order.COLUMNNAME_C_Project_ID), I_C_Order.COLUMNNAME_C_Project_ID));	
			else matches.add(new KeyNamePair(0,I_C_Order.COLUMNNAME_C_Project_ID));
			if (po.get_ValueAsInt(I_C_Order.COLUMNNAME_C_Activity_ID)>0) 
				matches.add(new KeyNamePair(po.get_ValueAsInt(I_C_Order.COLUMNNAME_C_Activity_ID), I_C_Order.COLUMNNAME_C_Activity_ID));
			else matches.add(new KeyNamePair(0,I_C_Order.COLUMNNAME_C_Activity_ID));
			if (po.get_ValueAsInt(I_C_Order.COLUMNNAME_C_Campaign_ID)>0)  
				matches.add(new KeyNamePair(po.get_ValueAsInt(I_C_Order.COLUMNNAME_C_Campaign_ID), I_C_Order.COLUMNNAME_C_Campaign_ID));		
			else matches.add(new KeyNamePair(0,I_C_Order.COLUMNNAME_C_Campaign_ID));
			matches.add(new KeyNamePair(0, "Account_ID")); // null for non-journal (Purchase etc).
			}

		log.finer("MATCHES FROM DOC: "+matches.toString());
		return matches;
	}
	/**
	 * remove leading arbitrary params for sql query (for purchasing)
	 * @param matchedMJournalLine
	 * @param sDoc 
	 */
	public void paramTrimming(MBudgetPlanLine matchedMJournalLine, PO runtimepo) {
		log.fine("paramTrimming(MJournalLine matchedMJournalLine)");
		String removematch="";
		String matchRemoved="";
		int c = 0; //count how many non matches deleted for aligning wherematches array pointer
		//remove first param - C_PERIOD_ID=? OR (do not remove for Journal, for PO later filter out non periods 
		if (whereMatches.get(0).getName().equals(MBudgetPlanLine.COLUMNNAME_C_Period_ID)) {
			if (matchedMJournalLine.getC_Period_ID()==0) {
				removematch = "("+whereMatches.get(0).getName()+"=? OR ";
				matchRemoved = whereMatchesSQL.toString().replace(removematch, "");
				whereMatchesSQL = new StringBuffer(matchRemoved);
				whereMatches.remove(0);
				whereMatchesIDs.remove(0);
			}
			else {
				if (!(po instanceof MJournalLine)) {
					changePeriodToTimestamp(matchedMJournalLine.getC_Period_ID());
					removematch = "("+MBudgetPlanLine.COLUMNNAME_C_Period_ID+"=? OR ";
	 				matchRemoved = whereMatchesSQL.toString().replace(removematch, "");
					whereMatchesSQL = new StringBuffer(matchRemoved);	
					whereMatches.remove(0);
					whereMatchesIDs.remove(0);
				}
				else{
					c++;
					removematch = "("+MBudgetPlanLine.COLUMNNAME_C_Period_ID+"=? OR ";
	 				matchRemoved = whereMatchesSQL.toString().replace(removematch, MBudgetPlanLine.COLUMNNAME_C_Period_ID+"=? AND ");
					whereMatchesSQL = new StringBuffer(matchRemoved); 
				}
			}
		}
		//remove 2nd param - AD_OrgDoc_ID=? OR 
		if (whereMatches.get(c).getName().equals(MBudgetPlanLine.COLUMNNAME_AD_OrgDoc_ID)) {
			if (matchedMJournalLine.getAD_OrgDoc_ID()==0) {
				removematch = whereMatches.get(c).getName()+"=? OR ";
				matchRemoved = whereMatchesSQL.toString().replace(removematch, "");
				whereMatchesSQL = new StringBuffer(matchRemoved);		
				whereMatches.remove(c);		
				whereMatchesIDs.remove(c);
			} else {
				c++;
				removematch = MBudgetPlanLine.COLUMNNAME_AD_OrgDoc_ID+"=? OR ";
				//SWAP AD_OrgDoc_ID = AD_Org_ID as trimming is for MOrder and GL Journal
				matchRemoved = whereMatchesSQL.toString().replace(removematch, MOrder.COLUMNNAME_AD_Org_ID+"=? AND ");
				whereMatchesSQL = new StringBuffer(matchRemoved);		
			}
		}		
		//check if need to remove second param as first - (C_BPartner_ID=? OR)
		if (whereMatches.get(c).getName().equals(I_GL_JournalLine.COLUMNNAME_C_BPartner_ID)) {
			if (matchedMJournalLine.get_ValueAsInt(I_C_Order.COLUMNNAME_C_BPartner_ID)==0) {
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
		//remove last Account_ID null for non Journal
		if (!(runtimepo instanceof MJournalLine)){
			int size = whereMatches.size() - 1; 
			if (size>0)
				removematch = " AND ";
			if (whereMatches.get(size).getName().equals("Account_ID")){
				removematch = removematch + I_GL_JournalLine.COLUMNNAME_Account_ID+" is null";
				matchRemoved = whereMatchesSQL.toString().replace(removematch, "");
				whereMatchesSQL = new StringBuffer(matchRemoved);
			}
				
		}
		
		 
		log.finer("PARAM TRIMMING = SELECT FROM FACT_ACCT WHERE "+whereMatchesSQL+" PARAMS?: "+whereMatchesIDs.toString());
	}
	/**
	 * timestamp handling append matchessql and params
	 * where dateordered < firstDate(periodID) && > lastDate(periodID)
	 * OR previousMonths
	 */
	public StringBuffer changePeriodToTimestamp(int periodValue){ 
		log.fine("changePeriodToTimestamp(int periodValue)");
		//previous repeat due to MonthToMonth loops - go back
		if (whereMatchesSQL.toString().contains("DateOrdered >= ?")) 
			return whereMatchesSQL;
		
		Calendar cal = Calendar.getInstance();	
		if (periodValue==previousMonths){
			cal.add(Calendar.MONTH, -previousMonths);
			Timestamp whereMonth = new Timestamp(cal.getTimeInMillis());
			whereMatchesSQL.append(new StringBuffer(" AND DateOrdered >= ?"));
			whereMatchesIDs.add(whereMonth);
		}
		else if (periodValue > 100){
			MPeriod period = new MPeriod(po.getCtx(), periodValue, po.get_TrxName());
			if (period.isStandardPeriod()) {
			Timestamp start = period.getStartDate();
			Timestamp end = period.getEndDate();
			if (whereMatchesSQL.length()>0)
				whereMatchesSQL.append(" AND ");
			whereMatchesSQL.append(new StringBuffer("DateOrdered >= ? AND DateOrdered <= ?"));
			whereMatchesIDs.add(start);
			whereMatchesIDs.add(end);
			}
			else
				log.warning("**NOT STANDARD PERIOD**  START/END DateOrdered NOT SET IN SQL. PERIOD ID: "+periodValue);
		}
		log.finer("WHEREMATCHESSQL: "+whereMatchesSQL+" WHEREMATCHS DATES (IF ANY APPENDED): "+whereMatches+ " PERIOD VALUE: "+periodValue);
		return whereMatchesSQL;
	} 
	/** GET IDS FROM MATCHES FOR SETTING PARAM VALUES IN SQL QUERY 
	 * @param matches
	 * @return IDs
	 */
	private ArrayList<Object> matchesToIDs(List<KeyNamePair> matches) {
		log.fine("ArrayList<Object> matchesToIDs(List<KeyNamePair> matches)");
		ArrayList<Object> params = new ArrayList<Object>();
		for (KeyNamePair match:matches) {
			if (match.getKey()>0)
				params.add((new Integer(match.getID())).intValue());
		}
		if (params.isEmpty()) return params;
		log.finer("MATCHES TO IDS - PARAMS: "+params.toString());
		return params;
	}
	
	/**
	 * Check that 'C_Period_ID=? AND' string exists before calling this method.
	 */
	public void stripPeriod() {
		String removematch = "";
		if (whereMatchesIDs.size()>0){
			removematch = MBudgetPlanLine.COLUMNNAME_C_Period_ID+"=? AND ";
			String matchRemoved = whereMatchesSQL.toString().replace(removematch, "");
			whereMatchesSQL = new StringBuffer(matchRemoved);	
			whereMatches.remove(0);
			whereMatchesIDs.remove(0); 
		}
	}

}
