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
import java.util.List;
import java.util.Properties;

import org.adempiere.model.POWrapper;
import org.compiere.model.MJournal;
import org.compiere.model.MJournalLine;
import org.compiere.model.Query;

public class BudgetLine extends MJournalLine {

	/**
	 * USING POWRAPPER TO CREATE EXTENDED JOURNAL LINE MODEL
	 */
	private static final long serialVersionUID = 1L;  
	private I_GL_JournalLine budgetLine = POWrapper.create(this, I_GL_JournalLine.class);

	public BudgetLine(MJournal journal){
		super(journal);
	}
	
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
    public int getAD_OrgDoc_ID() {
    	return budgetLine.getAD_OrgDoc_ID();
    }
    public void setAD_OrgDoc_ID(int id) {
    	budgetLine.setAD_OrgDoc_ID(id);
    }
    public boolean isSOTrx(){
    	return budgetLine.isSOTrx();
    }
    public void setIsSOTrx(boolean IsSOTrx){
    	budgetLine.setIsSOTrx(IsSOTrx);
    }
}
