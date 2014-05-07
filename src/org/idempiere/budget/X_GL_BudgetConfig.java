/******************************************************************************
 * Product: iDempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2012 ComPiere, Inc. All Rights Reserved.                *
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

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.model.*;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;

/** Generated Model for GL_BudgetConfig
 *  @author iDempiere (generated) 
 *  @version Release 2.0 - $Id$ */
public class X_GL_BudgetConfig extends PO implements I_GL_BudgetConfig, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20140506L;

    /** Standard Constructor */
    public X_GL_BudgetConfig (Properties ctx, int GL_BudgetConfig_ID, String trxName)
    {
      super (ctx, GL_BudgetConfig_ID, trxName);
      /** if (GL_BudgetConfig_ID == 0)
        {
			setGL_BudgetConfig_ID (0);
			setIsValid (true);
// Y
			setName (null);
        } */
    }

    /** Load Constructor */
    public X_GL_BudgetConfig (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_GL_BudgetConfig[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Average = A */
	public static final String BUDGETTREND_Average = "A";
	/** Progressive = P */
	public static final String BUDGETTREND_Progressive = "P";
	/** Average+LastYear = L */
	public static final String BUDGETTREND_AveragePlusLastYear = "L";
	/** Accumulative = C */
	public static final String BUDGETTREND_Accumulative = "C";
	/** Year-to-date = T */
	public static final String BUDGETTREND_Year_To_Date = "T";
	/** Set Budget Trend.
		@param BudgetTrend 
		Budget Trend to use in revenue estimate
	  */
	public void setBudgetTrend (String BudgetTrend)
	{

		set_Value (COLUMNNAME_BudgetTrend, BudgetTrend);
	}

	/** Get Budget Trend.
		@return Budget Trend to use in revenue estimate
	  */
	public String getBudgetTrend () 
	{
		return (String)get_Value(COLUMNNAME_BudgetTrend);
	}

	/** Set Debug Mode.
		@param DebugMode 
		Debug mode logging
	  */
	public void setDebugMode (boolean DebugMode)
	{
		set_Value (COLUMNNAME_DebugMode, Boolean.valueOf(DebugMode));
	}

	/** Get Debug Mode.
		@return Debug mode logging
	  */
	public boolean isDebugMode () 
	{
		Object oo = get_Value(COLUMNNAME_DebugMode);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Description.
		@param Description 
		Optional short description of the record
	  */
	public void setDescription (String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Description.
		@return Optional short description of the record
	  */
	public String getDescription () 
	{
		return (String)get_Value(COLUMNNAME_Description);
	}

	/** Set Budget Configurator.
		@param GL_BudgetConfig_ID Budget Configurator	  */
	public void setGL_BudgetConfig_ID (int GL_BudgetConfig_ID)
	{
		if (GL_BudgetConfig_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_GL_BudgetConfig_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_GL_BudgetConfig_ID, Integer.valueOf(GL_BudgetConfig_ID));
	}

	/** Get Budget Configurator.
		@return Budget Configurator	  */
	public int getGL_BudgetConfig_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_GL_BudgetConfig_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set GL_BudgetConfig_UU.
		@param GL_BudgetConfig_UU GL_BudgetConfig_UU	  */
	public void setGL_BudgetConfig_UU (String GL_BudgetConfig_UU)
	{
		set_Value (COLUMNNAME_GL_BudgetConfig_UU, GL_BudgetConfig_UU);
	}

	/** Get GL_BudgetConfig_UU.
		@return GL_BudgetConfig_UU	  */
	public String getGL_BudgetConfig_UU () 
	{
		return (String)get_Value(COLUMNNAME_GL_BudgetConfig_UU);
	}

	public org.compiere.model.I_GL_Budget getGL_Budget() throws RuntimeException
    {
		return (org.compiere.model.I_GL_Budget)MTable.get(getCtx(), org.compiere.model.I_GL_Budget.Table_Name)
			.getPO(getGL_Budget_ID(), get_TrxName());	}

	/** Set Budget.
		@param GL_Budget_ID 
		General Ledger Budget
	  */
	public void setGL_Budget_ID (int GL_Budget_ID)
	{
		if (GL_Budget_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_GL_Budget_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_GL_Budget_ID, Integer.valueOf(GL_Budget_ID));
	}

	/** Get Budget.
		@return General Ledger Budget
	  */
	public int getGL_Budget_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_GL_Budget_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Budget Previous Month.
		@param GL_Budget_Previous_Month 
		No. of months to base revenue and other calculations
	  */
	public void setGL_Budget_Previous_Month (BigDecimal GL_Budget_Previous_Month)
	{
		set_Value (COLUMNNAME_GL_Budget_Previous_Month, GL_Budget_Previous_Month);
	}

	/** Get Budget Previous Month.
		@return No. of months to base revenue and other calculations
	  */
	public BigDecimal getGL_Budget_Previous_Month () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_GL_Budget_Previous_Month);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Budget Previous Year.
		@param GL_Budget_Previous_Year 
		No of years back to forecast or this year or estimate
	  */
	public void setGL_Budget_Previous_Year (BigDecimal GL_Budget_Previous_Year)
	{
		set_Value (COLUMNNAME_GL_Budget_Previous_Year, GL_Budget_Previous_Year);
	}

	/** Get Budget Previous Year.
		@return No of years back to forecast or this year or estimate
	  */
	public BigDecimal getGL_Budget_Previous_Year () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_GL_Budget_Previous_Year);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Comment/Help.
		@param Help 
		Comment or Hint
	  */
	public void setHelp (String Help)
	{
		set_Value (COLUMNNAME_Help, Help);
	}

	/** Get Comment/Help.
		@return Comment or Hint
	  */
	public String getHelp () 
	{
		return (String)get_Value(COLUMNNAME_Help);
	}

	/** Set IsInvoiceToo.
		@param IsInvoiceToo 
		Includes Invoice for Purchasing Budget Checking
	  */
	public void setIsInvoiceToo (boolean IsInvoiceToo)
	{
		set_Value (COLUMNNAME_IsInvoiceToo, Boolean.valueOf(IsInvoiceToo));
	}

	/** Get IsInvoiceToo.
		@return Includes Invoice for Purchasing Budget Checking
	  */
	public boolean isInvoiceToo () 
	{
		Object oo = get_Value(COLUMNNAME_IsInvoiceToo);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set IsPaymentToo.
		@param IsPaymentToo 
		Includes Payment for Purchasing Budget Checking
	  */
	public void setIsPaymentToo (boolean IsPaymentToo)
	{
		set_Value (COLUMNNAME_IsPaymentToo, Boolean.valueOf(IsPaymentToo));
	}

	/** Get IsPaymentToo.
		@return Includes Payment for Purchasing Budget Checking
	  */
	public boolean isPaymentToo () 
	{
		Object oo = get_Value(COLUMNNAME_IsPaymentToo);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Valid.
		@param IsValid 
		Element is valid
	  */
	public void setIsValid (boolean IsValid)
	{
		set_Value (COLUMNNAME_IsValid, Boolean.valueOf(IsValid));
	}

	/** Get Valid.
		@return Element is valid
	  */
	public boolean isValid () 
	{
		Object oo = get_Value(COLUMNNAME_IsValid);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Month to month.
		@param MonthToMonth 
		Same months of previous years (Year On Year)
	  */
	public void setMonthToMonth (boolean MonthToMonth)
	{
		set_Value (COLUMNNAME_MonthToMonth, Boolean.valueOf(MonthToMonth));
	}

	/** Get Month to month.
		@return Same months of previous years (Year On Year)
	  */
	public boolean isMonthToMonth () 
	{
		Object oo = get_Value(COLUMNNAME_MonthToMonth);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Name.
		@param Name 
		Alphanumeric identifier of the entity
	  */
	public void setName (String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Alphanumeric identifier of the entity
	  */
	public String getName () 
	{
		return (String)get_Value(COLUMNNAME_Name);
	}

    /** Get Record ID/ColumnName
        @return ID/ColumnName pair
      */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), getName());
    }

	/** Set Prorata.
		@param Prorata 
		Prorata over number of periods
	  */
	public void setProrata (boolean Prorata)
	{
		set_Value (COLUMNNAME_Prorata, Boolean.valueOf(Prorata));
	}

	/** Get Prorata.
		@return Prorata over number of periods
	  */
	public boolean isProrata () 
	{
		Object oo = get_Value(COLUMNNAME_Prorata);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}
}