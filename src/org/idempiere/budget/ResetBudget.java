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

import java.util.logging.Level;

import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;

public class ResetBudget extends SvrProcess {

	public ResetBudget() { 
	}

	private String p_ResetBudget; 
	public static String p_AlignPrevious;  
    
    protected void prepare() { 
        ProcessInfoParameter[] para = getParameter();
        for (int i = 0; i < para.length; i++) {
            String name = para[i].getParameterName();
            if (para[i].getParameter() == null);
 
            else if (name.equals("IsActive")) { //BORROW FOR PARAM
            	p_ResetBudget = ((String) para[i].getParameter());
            }  
            else if (name.equals("AlignPreviousBudget")) {
            	p_AlignPrevious = ((String) para[i].getParameter());
            } 
            else {
                log.log(Level.SEVERE, "Unknown Parameter: " + name);
            }
        }
    }

    protected String doIt() { 
    	if (p_ResetBudget.equals("Y")){
    		BudgetUtils.budgetCONFIGinstance=null;
    		return "BUDGET CONFIG INSTANCE RESET";
    	}
		return "End";
    }
}
