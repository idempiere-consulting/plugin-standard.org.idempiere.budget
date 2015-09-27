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

package org.budget.component;

import java.sql.ResultSet;

import org.adempiere.base.IModelFactory;
import org.compiere.model.PO;
import org.compiere.util.Env;
import org.idempiere.budget.MBudgetConfig;
import org.idempiere.budget.MBudgetLine;
import org.idempiere.budget.MBudgetPeriod;
import org.idempiere.budget.MBudgetPlan;
import org.idempiere.budget.MBudgetPlanLine;
import org.idempiere.budget.MBudgetReference;
import org.idempiere.budget.MBudgetYear;
import org.idempiere.budget.MIBudget;

public class BudgetModelFactory implements IModelFactory {

	@Override
	public Class<?> getClass(String tableName) {
		 if (tableName.equals(MBudgetPlan.Table_Name)){
			 return MBudgetPlan.class;
		 } 		 
		 if (tableName.equals(MBudgetConfig.Table_Name)){
			 return MBudgetConfig.class;
		 } 
		 if (tableName.equals(MBudgetPlanLine.Table_Name)){
			 return MBudgetPlanLine.class;
		 } 
		 if (tableName.equals(MBudgetReference.Table_Name)){
			 return MBudgetReference.class;
		 } 
		 if (tableName.equals(MIBudget.Table_Name)){
			 return MIBudget.class;
		 } 
		 if (tableName.equals(MBudgetYear.Table_Name)){
			 return MBudgetYear.class;
		 } 
		 if (tableName.equals(MBudgetPeriod.Table_Name)){
			 return MBudgetPeriod.class;
		 } 
		 if (tableName.equals(MBudgetLine.Table_Name)){
			 return MBudgetLine.class;
		 } 
		return null;
	}

	@Override
	public PO getPO(String tableName, int Record_ID, String trxName) {
		 if (tableName.equals(MBudgetPlan.Table_Name)) {
		     return new MBudgetPlan(Env.getCtx(), Record_ID, trxName);
		 } 		 
		 if (tableName.equals(MBudgetConfig.Table_Name)) {
		     return new MBudgetConfig(Env.getCtx(), Record_ID, trxName);
		 } 
		 if (tableName.equals(MBudgetPlanLine.Table_Name)) {
		     return new MBudgetPlanLine(Env.getCtx(), Record_ID, trxName);
		 } 
		 if (tableName.equals(MBudgetReference.Table_Name)) {
		     return new MBudgetReference(Env.getCtx(), Record_ID, trxName);
		 } 
		 if (tableName.equals(MIBudget.Table_Name)) {
		     return new MIBudget(Env.getCtx(), Record_ID, trxName);
		 }
		 if (tableName.equals(MBudgetYear.Table_Name)) {
		     return new MBudgetYear(Env.getCtx(), Record_ID, trxName);
		 }
		 if (tableName.equals(MBudgetPeriod.Table_Name)) {
		     return new MBudgetPeriod(Env.getCtx(), Record_ID, trxName);
		 }
		 if (tableName.equals(MBudgetLine.Table_Name)) {
		     return new MBudgetLine(Env.getCtx(), Record_ID, trxName);
		 }
		 return null;
	}

	@Override
	public PO getPO(String tableName, ResultSet rs, String trxName) {
		 if (tableName.equals(MBudgetPlan.Table_Name)) {
		     return new MBudgetPlan(Env.getCtx(), rs, trxName);		 			     
		   }
		 if (tableName.equals(MBudgetConfig.Table_Name)) {
		     return new MBudgetConfig(Env.getCtx(), rs, trxName);		 			     
		   }
		 if (tableName.equals(MBudgetPlanLine.Table_Name)) {
		     return new MBudgetPlanLine(Env.getCtx(), rs, trxName);		 			     
		   }
		 if (tableName.equals(MBudgetReference.Table_Name)) {
		     return new MBudgetReference(Env.getCtx(), rs, trxName);		 			     
		   }
		 if (tableName.equals(MIBudget.Table_Name)) {
		     return new MIBudget(Env.getCtx(), rs, trxName);		 			     
		   }
		 if (tableName.equals(MBudgetYear.Table_Name)) {
		     return new MBudgetYear(Env.getCtx(), rs, trxName);		 			     
		   }
		 if (tableName.equals(MBudgetPeriod.Table_Name)) {
		     return new MBudgetPeriod(Env.getCtx(), rs, trxName);		 			     
		   }
		 if (tableName.equals(MBudgetLine.Table_Name)) {
		     return new MBudgetLine(Env.getCtx(), rs, trxName);		 			     
		   }
		 return null;
	}

}
