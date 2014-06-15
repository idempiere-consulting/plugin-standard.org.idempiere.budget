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
 
import java.sql.ResultSet;
import java.util.Properties;

public class MBudgetPlanLine extends X_B_BudgetPlanLine {

	private static final long serialVersionUID = 1L;  
	
	public MBudgetPlanLine(Properties ctx, int B_BudgetLine_ID, String trxName) {
		super(ctx, B_BudgetLine_ID, trxName); 
	}

	public MBudgetPlanLine(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}
	/**
	 * 	Parent Constructor
	 *	@param parent journal
	 */
	public MBudgetPlanLine (MBudgetPlan parent)
	{
		this (parent.getCtx(), 0, parent.get_TrxName());
		setClientOrg(parent);
		setB_BudgetPlan_ID(parent.getB_BudgetPlan_ID());
		setC_Currency_ID(parent.getC_Currency_ID());		
	}
    /**
     * 	Get active MGLBudgetPlanDetails of GL Budget
     *	@param parent GL BudgetPlan
     *	@return array of allocations
     */
 
}
