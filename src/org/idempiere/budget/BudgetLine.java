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
 *  @author Redhuan D. Oon  - red1@red1.org  www.red1.org                     *
 *****************************************************************************/

package org.idempiere.budget;
 
import java.math.BigDecimal;
import java.sql.ResultSet; 
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
 
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.POWrapper;
import org.compiere.model.I_GL_Journal;
import org.compiere.model.MJournal;  
import org.compiere.model.MJournalLine;
import org.compiere.model.MOrder;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.KeyNamePair;

public class BudgetLine extends MJournalLine {

	/**
	 * USING POWRAPPER TO CREATE EXTENDED JOURNAL LINE MODEL
	 */
	private static final long serialVersionUID = 1L;  
	private I_GL_JournalLine budgetLine = POWrapper.create(this, I_GL_JournalLine.class);

	public BudgetLine(Properties ctx, int GL_JournalLine_ID, String trxName) {
		super(ctx, GL_JournalLine_ID, trxName); 
	}

	public BudgetLine(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

    /**
     * 	Get active MGLBudgetPlanDetails of GL Budget
     *	@param parent GL BudgetPlan
     *	@return array of allocations
     */
    public static List<MJournalLine> getJournalLines(MJournal parent) {
        final String whereClause = "GL_Journal_ID=? AND IsActive='Y'";
        List<MJournalLine> list = new Query(parent.getCtx(), MJournalLine.Table_Name, whereClause, parent.get_TrxName())
        .setParameters(parent.getGL_Journal_ID())
        .list();
        return list;
    } //    
    
    /*
     * from POWrapper extend JournalLine with Percent property
     */
    public BigDecimal getPercent() {
    	return budgetLine.getPercent();
    }
    public void setPercent(BigDecimal percent) {
    	budgetLine.setPercent(percent);
    }
    /*
     * from POWrapper extend JournalLine with Period property
     */
    public int getC_Period_ID() {
    	return budgetLine.getC_Period_ID();
    }
    public void setC_Period_ID(int id) {
    	budgetLine.setC_Period_ID(id);
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
  		if (counter>1) throw new AdempiereException(counter+" AMBIGOUS BUDGET RULES FOUND = "+whereClause);
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
