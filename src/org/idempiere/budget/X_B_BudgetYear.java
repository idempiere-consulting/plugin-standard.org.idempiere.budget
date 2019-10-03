/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2007 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
/** Generated Model - DO NOT CHANGE */
package org.idempiere.budget;

import java.sql.ResultSet;
import java.util.Properties;

import org.compiere.model.I_Persistent;
import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.model.POInfo;

/** Generated Model for B_BudgetYear
 *  @author Adempiere (generated) 
 *  @version Release 3.7.0LTS - $Id$ */
public class X_B_BudgetYear extends PO implements I_B_BudgetYear, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20140927L;

    /** Standard Constructor */
    public X_B_BudgetYear (Properties ctx, int B_BudgetYear_ID, String trxName)
    {
      super (ctx, B_BudgetYear_ID, trxName);
      /** if (B_BudgetYear_ID == 0)
        {
			setB_BudgetPlan_ID (0);
			setC_Year_ID (0);
        } */
    }

    /** Load Constructor */
    public X_B_BudgetYear (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 3 - Client - Org 
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuffer sb = new StringBuffer ("X_B_BudgetYear[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public I_B_BudgetPlan getB_BudgetPlan() throws RuntimeException
    {
		return (I_B_BudgetPlan)MTable.get(getCtx(), I_B_BudgetPlan.Table_Name)
			.getPO(getB_BudgetPlan_ID(), get_TrxName());	}

	/** Set Budget Plan.
		@param B_BudgetPlan_ID Budget Plan	  */
	public void setB_BudgetPlan_ID (int B_BudgetPlan_ID)
	{
		if (B_BudgetPlan_ID < 1) 
			set_Value (COLUMNNAME_B_BudgetPlan_ID, null);
		else 
			set_Value (COLUMNNAME_B_BudgetPlan_ID, Integer.valueOf(B_BudgetPlan_ID));
	}

	/** Get Budget Plan.
		@return Budget Plan	  */
	public int getB_BudgetPlan_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_B_BudgetPlan_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Budget Year.
		@param B_BudgetYear_ID Budget Year	  */
	public void setB_BudgetYear_ID (int B_BudgetYear_ID)
	{
		if (B_BudgetYear_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_B_BudgetYear_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_B_BudgetYear_ID, Integer.valueOf(B_BudgetYear_ID));
	}

	/** Get Budget Year.
		@return Budget Year	  */
	public int getB_BudgetYear_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_B_BudgetYear_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_C_Currency getC_Currency() throws RuntimeException
    {
		return (org.compiere.model.I_C_Currency)MTable.get(getCtx(), org.compiere.model.I_C_Currency.Table_Name)
			.getPO(getC_Currency_ID(), get_TrxName());	}

	/** Set Currency.
		@param C_Currency_ID 
		The Currency for this record
	  */
	public void setC_Currency_ID (int C_Currency_ID)
	{
		if (C_Currency_ID < 1) 
			set_Value (COLUMNNAME_C_Currency_ID, null);
		else 
			set_Value (COLUMNNAME_C_Currency_ID, Integer.valueOf(C_Currency_ID));
	}

	/** Get Currency.
		@return The Currency for this record
	  */
	public int getC_Currency_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Currency_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_C_DocType getC_DocType() throws RuntimeException
    {
		return (org.compiere.model.I_C_DocType)MTable.get(getCtx(), org.compiere.model.I_C_DocType.Table_Name)
			.getPO(getC_DocType_ID(), get_TrxName());	}

	/** Set Document Type.
		@param C_DocType_ID 
		Document type or rules
	  */
	public void setC_DocType_ID (int C_DocType_ID)
	{
		if (C_DocType_ID < 0) 
			set_Value (COLUMNNAME_C_DocType_ID, null);
		else 
			set_Value (COLUMNNAME_C_DocType_ID, Integer.valueOf(C_DocType_ID));
	}

	/** Get Document Type.
		@return Document type or rules
	  */
	public int getC_DocType_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_DocType_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_C_Year getC_Year() throws RuntimeException
    {
		return (org.compiere.model.I_C_Year)MTable.get(getCtx(), org.compiere.model.I_C_Year.Table_Name)
			.getPO(getC_Year_ID(), get_TrxName());	}

	/** Set Year.
		@param C_Year_ID 
		Calendar Year
	  */
	public void setC_Year_ID (int C_Year_ID)
	{
		if (C_Year_ID < 1) 
			set_Value (COLUMNNAME_C_Year_ID, null);
		else 
			set_Value (COLUMNNAME_C_Year_ID, Integer.valueOf(C_Year_ID));
	}

	/** Get Year.
		@return Calendar Year
	  */
	public int getC_Year_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Year_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Calculate Budget.
		@param CalculateBudget Calculate Budget	  */
	public void setCalculateBudget (String CalculateBudget)
	{
		set_Value (COLUMNNAME_CalculateBudget, CalculateBudget);
	}

	/** Get Calculate Budget.
		@return Calculate Budget	  */
	public String getCalculateBudget () 
	{
		return (String)get_Value(COLUMNNAME_CalculateBudget);
	}
}