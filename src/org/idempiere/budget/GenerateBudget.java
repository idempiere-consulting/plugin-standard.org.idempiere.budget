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
import java.util.List;
import java.util.logging.Level;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MFactAcct;
import org.compiere.model.MJournal;
import org.compiere.model.MOrder;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;

public class GenerateBudget extends SvrProcess {

	public GenerateBudget() { 
	}
    private BigDecimal p_previousYears;
	private String p_MonthToMonth;
	private BigDecimal p_previousMonths; 
	public static String p_Percent; //Force all percentages or Amt or follow
	private String p_CreatePurchaseBudget;
	private String p_DeleteOld; //OR just deactivate
	private String p_IsProrata;  
    
    protected void prepare() {
    	//TODO implement parameters capture
        ProcessInfoParameter[] para = getParameter();
        for (int i = 0; i < para.length; i++) {
            String name = para[i].getParameterName();
            if (para[i].getParameter() == null);
            else if (name.equals("GL_Budget_Previous_Year")) {
                p_previousYears = ((BigDecimal) para[i].getParameter());
            } 
            //MONTH ON MONTH 
            else if (name.equals("MonthToMonth")) {
                p_MonthToMonth = ((String) para[i].getParameter());
            }             
            else if (name.equals("GL_Budget_Previous_Month")) {
                p_previousMonths = ((BigDecimal) para[i].getParameter());
            }  
            else if (name.equals("Percent")) {
            	p_Percent = ((String) para[i].getParameter());
            }  
            else if (name.equals("IsActive")) { //BORROW FOR PARAM
            	p_CreatePurchaseBudget = ((String) para[i].getParameter());
            } 
            else if (name.equals("DeleteOld")) {
            	p_IsProrata = ((String) para[i].getParameter());
            }  
            else if (name.equals("Prorata")) {
            	p_DeleteOld = ((String) para[i].getParameter());
            } 
            else {
                log.log(Level.SEVERE, "Unknown Parameter: " + name);
            }
        }
    }

    protected String doIt() {
    	String message = "";
    	int count = 0;
    	//ACCESS PRESENT TARGET BUDGET TO CREATE NEW BUDGET
        MJournal targetBudget = new Query(Env.getCtx(),MJournal.Table_Name, "PostingType='B' AND GL_Budget_ID=?",get_TrxName())
        .setParameters(100) //HARD SET FOR TARGETBUDGET ID
        .firstOnly(); //ONLY ONE SUCH BUDGET SET TO 'TARGET BUDGET' AT ANY SINGLE TIME, FOR ACTIVE USE 

    	//INIT BUDGET CONFIG DETAILS
    	BudgetUtils.initBudgetConfig(targetBudget);
    	BudgetUtils.setInstance(p_previousYears,p_MonthToMonth,p_previousMonths,p_IsProrata);
    	BudgetUtils.setupCalendar(targetBudget);
    	BudgetUtils.clearWhereMatches();
		BudgetUtils.revenueEstimate();
    	MJournal newbudget = createBudget(targetBudget);
    	if (newbudget.getGL_Journal_ID()<1) throw new AdempiereException("CANNOT CREATE NEW BUDGET JOURNAL");
    	//GET YEAR REVENUE ACCORDING TO CONFIG
//    	BigDecimal yearRevenue = budgetUtils.oneTimeSetupRevenue();
     	
    	//DELETE OLD OR DEACTIVATE
    	
    	//ITERATE TARGET BUDGET LINES       
        List<BudgetLine> targetBudgetLines = new Query(newbudget.getCtx(), BudgetLine.Table_Name, BudgetLine.COLUMNNAME_GL_Journal_ID+"=?", newbudget.get_TrxName())
        .setParameters(targetBudget.getGL_Journal_ID())
        .setOnlyActiveRecords(true)
        .list();
         for (BudgetLine tbl:targetBudgetLines){
        	if (tbl.isProcessed()) continue; //marker to stop accidentally generate from generated (highly redundant for M2M)
        	if (tbl.getAccount_ID()<1) continue; //NO ACCOUNTING ELEMENT TO SET BUDGET 
         	if (p_MonthToMonth=="Y" && tbl.getC_Period_ID()>0) continue;//M2M avoids Periodic rules
        	
         	if (tbl.getAccount_ID()==508 && p_CreatePurchaseBudget.equals("Y")){//508 reserved checking account to denote POs       		
            	BudgetUtils.runtimePO = new MOrder(tbl.getCtx(), 0, tbl.get_TrxName());
         	}
        	else if (tbl.getAccount_ID()!=508){
        		//GENERATE BUDGET FOR MATCHING ACCOUNTING ELEMENT 
        		BudgetUtils.runtimePO = tbl;
         	}
           	int c = BudgetUtils.generateBudgetLine(tbl, newbudget); 
           	if (c>0){
           		count=count+c;
           	}
        }
 
		return new Integer(count).toString()+" Budget Lines Created";   	
    }
    /**
     * CREATE NEW BUDGET JOURNAL
     * @return GL_JOURNAL_ID
     */
    private MJournal createBudget(MJournal target) {
    	MJournal budget = new MJournal(target); 
    	budget.setAD_Org_ID(target.getAD_Org_ID());
    	budget.setGL_Budget_ID(0); //- set to target last TODO
    	budget.setDescription("**NEW TARGET**" + budget.getDescription());
    	budget.setControlAmt(Env.ZERO);
    	budget.saveEx(target.get_TrxName());
		return budget;
	}

}
