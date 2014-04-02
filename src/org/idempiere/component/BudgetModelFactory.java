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

package org.idempiere.component;

import java.sql.ResultSet;
import org.adempiere.base.IModelFactory;
import org.compiere.model.MJournalLine;
import org.compiere.model.PO;
import org.compiere.util.Env;
import org.compiere.model.MJournal;
import org.idempiere.budget.BudgetLine;
import org.idempiere.budget.MBudgetConfig;

public class BudgetModelFactory implements IModelFactory {

	@Override
	public Class<?> getClass(String tableName) {
		 if (tableName.equals(MJournal.Table_Name)){
			 return MJournal.class;
		 } 		 
		 if (tableName.equals(MBudgetConfig.Table_Name)){
			 return MBudgetConfig.class;
		 } 
		 if (tableName.equals(BudgetLine.Table_Name)){
			 return BudgetLine.class;
		 } 
		return null;
	}

	@Override
	public PO getPO(String tableName, int Record_ID, String trxName) {
		 if (tableName.equals(MJournal.Table_Name)) {
		     return new MJournal(Env.getCtx(), Record_ID, trxName);
		 } 		 
		 if (tableName.equals(MBudgetConfig.Table_Name)) {
		     return new MBudgetConfig(Env.getCtx(), Record_ID, trxName);
		 } 
		 if (tableName.equals(BudgetLine.Table_Name)) {
		     return new BudgetLine(Env.getCtx(), Record_ID, trxName);
		 } 
		return null;
	}

	@Override
	public PO getPO(String tableName, ResultSet rs, String trxName) {
		 if (tableName.equals(MJournal.Table_Name)) {
		     return new MJournal(Env.getCtx(), rs, trxName);		 			     
		   }
		 if (tableName.equals(MBudgetConfig.Table_Name)) {
		     return new MBudgetConfig(Env.getCtx(), rs, trxName);		 			     
		   }
		 if (tableName.equals(BudgetLine.Table_Name)) {
		     return new BudgetLine(Env.getCtx(), rs, trxName);		 			     
		   }
		 return null;
	}

}
