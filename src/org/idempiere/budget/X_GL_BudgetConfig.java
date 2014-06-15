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
	private static final long serialVersionUID = 20140613L;

    /** Standard Constructor */
    public X_GL_BudgetConfig (Properties ctx, int GL_BudgetConfig_ID, String trxName)
    {
      super (ctx, GL_BudgetConfig_ID, trxName);
      /** if (GL_BudgetConfig_ID == 0)
        {
			setGL_BudgetConfig_ID (0);
			setIsValid (false);
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

	/** Set Account.
		@param Account_ID 
		Account used
	  */
	public void setAccount_ID (int Account_ID)
	{
		if (Account_ID < 1) 
			set_Value (COLUMNNAME_Account_ID, null);
		else 
			set_Value (COLUMNNAME_Account_ID, Integer.valueOf(Account_ID));
	}

	/** Get Account.
		@return Account used
	  */
	public int getAccount_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Account_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** BudgetTrend AD_Reference_ID=200081 */
	public static final int BUDGETTREND_AD_Reference_ID=200081;
	/** Average = A */
	public static final String BUDGETTREND_Average = "A";
	/** Accumulative = C */
	public static final String BUDGETTREND_Accumulative = "C";
	/** Average+Last = L */
	public static final String BUDGETTREND_AveragePlusLast = "L";
	/** Progressive = P */
	public static final String BUDGETTREND_Progressive = "P";
	/** To-date = T */
	public static final String BUDGETTREND_To_Date = "T";
	/** Set BudgetTrend.
		@param BudgetTrend BudgetTrend	  */
	public void setBudgetTrend (String BudgetTrend)
	{

		set_Value (COLUMNNAME_BudgetTrend, BudgetTrend);
	}

	/** Get BudgetTrend.
		@return BudgetTrend	  */
	public String getBudgetTrend () 
	{
		return (String)get_Value(COLUMNNAME_BudgetTrend);
	}

	/** Set DebugMode.
		@param DebugMode DebugMode	  */
	public void setDebugMode (boolean DebugMode)
	{
		set_Value (COLUMNNAME_DebugMode, Boolean.valueOf(DebugMode));
	}

	/** Get DebugMode.
		@return DebugMode	  */
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

	/** Set GL_BudgetConfig.
		@param GL_BudgetConfig_ID GL_BudgetConfig	  */
	public void setGL_BudgetConfig_ID (int GL_BudgetConfig_ID)
	{
		if (GL_BudgetConfig_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_GL_BudgetConfig_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_GL_BudgetConfig_ID, Integer.valueOf(GL_BudgetConfig_ID));
	}

	/** Get GL_BudgetConfig.
		@return GL_BudgetConfig	  */
	public int getGL_BudgetConfig_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_GL_BudgetConfig_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
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
			set_Value (COLUMNNAME_GL_Budget_ID, null);
		else 
			set_Value (COLUMNNAME_GL_Budget_ID, Integer.valueOf(GL_Budget_ID));
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

	/** Set GL_Budget_Previous_Month.
		@param GL_Budget_Previous_Month GL_Budget_Previous_Month	  */
	public void setGL_Budget_Previous_Month (BigDecimal GL_Budget_Previous_Month)
	{
		set_Value (COLUMNNAME_GL_Budget_Previous_Month, GL_Budget_Previous_Month);
	}

	/** Get GL_Budget_Previous_Month.
		@return GL_Budget_Previous_Month	  */
	public BigDecimal getGL_Budget_Previous_Month () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_GL_Budget_Previous_Month);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set GL_Budget_Previous_Year.
		@param GL_Budget_Previous_Year GL_Budget_Previous_Year	  */
	public void setGL_Budget_Previous_Year (BigDecimal GL_Budget_Previous_Year)
	{
		set_Value (COLUMNNAME_GL_Budget_Previous_Year, GL_Budget_Previous_Year);
	}

	/** Get GL_Budget_Previous_Year.
		@return GL_Budget_Previous_Year	  */
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
		@param IsInvoiceToo IsInvoiceToo	  */
	public void setIsInvoiceToo (boolean IsInvoiceToo)
	{
		set_Value (COLUMNNAME_IsInvoiceToo, Boolean.valueOf(IsInvoiceToo));
	}

	/** Get IsInvoiceToo.
		@return IsInvoiceToo	  */
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
		@param IsPaymentToo IsPaymentToo	  */
	public void setIsPaymentToo (boolean IsPaymentToo)
	{
		set_Value (COLUMNNAME_IsPaymentToo, Boolean.valueOf(IsPaymentToo));
	}

	/** Get IsPaymentToo.
		@return IsPaymentToo	  */
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

	/** Set MonthToMonth.
		@param MonthToMonth MonthToMonth	  */
	public void setMonthToMonth (boolean MonthToMonth)
	{
		set_Value (COLUMNNAME_MonthToMonth, Boolean.valueOf(MonthToMonth));
	}

	/** Get MonthToMonth.
		@return MonthToMonth	  */
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
		@param Prorata Prorata	  */
	public void setProrata (boolean Prorata)
	{
		set_Value (COLUMNNAME_Prorata, Boolean.valueOf(Prorata));
	}

	/** Get Prorata.
		@return Prorata	  */
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

	public org.compiere.model.I_C_ElementValue getUser1() throws RuntimeException
    {
		return (org.compiere.model.I_C_ElementValue)MTable.get(getCtx(), org.compiere.model.I_C_ElementValue.Table_Name)
			.getPO(getUser1_ID(), get_TrxName());	}

	/** Set User List 1.
		@param User1_ID 
		User defined list element #1
	  */
	public void setUser1_ID (int User1_ID)
	{
		if (User1_ID < 1) 
			set_Value (COLUMNNAME_User1_ID, null);
		else 
			set_Value (COLUMNNAME_User1_ID, Integer.valueOf(User1_ID));
	}

	/** Get User List 1.
		@return User defined list element #1
	  */
	public int getUser1_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_User1_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set gl_budgetconfig_uu.
		@param gl_budgetconfig_uu gl_budgetconfig_uu	  */
	public void setgl_budgetconfig_uu (String gl_budgetconfig_uu)
	{
		set_Value (COLUMNNAME_gl_budgetconfig_uu, gl_budgetconfig_uu);
	}

	/** Get gl_budgetconfig_uu.
		@return gl_budgetconfig_uu	  */
	public String getgl_budgetconfig_uu () 
	{
		return (String)get_Value(COLUMNNAME_gl_budgetconfig_uu);
	}
}