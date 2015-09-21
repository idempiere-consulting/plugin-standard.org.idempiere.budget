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

/** Generated Model for B_BudgetReference
 *  @author iDempiere (generated) 
 *  @version Release 2.1 - $Id$ */
public class X_B_BudgetReference extends PO implements I_B_BudgetReference, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20150921L;

    /** Standard Constructor */
    public X_B_BudgetReference (Properties ctx, int B_BudgetReference_ID, String trxName)
    {
      super (ctx, B_BudgetReference_ID, trxName);
      /** if (B_BudgetReference_ID == 0)
        {
			setB_BudgetPlanLine_ID (0);
			setB_BudgetReference_ID (0);
			setIsValid (true);
// Y
        } */
    }

    /** Load Constructor */
    public X_B_BudgetReference (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_B_BudgetReference[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public org.compiere.model.I_C_ElementValue getAccount() throws RuntimeException
    {
		return (org.compiere.model.I_C_ElementValue)MTable.get(getCtx(), org.compiere.model.I_C_ElementValue.Table_Name)
			.getPO(getAccount_ID(), get_TrxName());	}

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

	/** Set Amount.
		@param Amount 
		Amount in a defined currency
	  */
	public void setAmount (BigDecimal Amount)
	{
		set_Value (COLUMNNAME_Amount, Amount);
	}

	/** Get Amount.
		@return Amount in a defined currency
	  */
	public BigDecimal getAmount () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Amount);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	public I_B_BudgetPlanLine getB_BudgetPlanLine() throws RuntimeException
    {
		return (I_B_BudgetPlanLine)MTable.get(getCtx(), I_B_BudgetPlanLine.Table_Name)
			.getPO(getB_BudgetPlanLine_ID(), get_TrxName());	}

	/** Set B_BudgetPlanLine.
		@param B_BudgetPlanLine_ID B_BudgetPlanLine	  */
	public void setB_BudgetPlanLine_ID (int B_BudgetPlanLine_ID)
	{
		if (B_BudgetPlanLine_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_B_BudgetPlanLine_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_B_BudgetPlanLine_ID, Integer.valueOf(B_BudgetPlanLine_ID));
	}

	/** Get B_BudgetPlanLine.
		@return B_BudgetPlanLine	  */
	public int getB_BudgetPlanLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_B_BudgetPlanLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Budget Reference.
		@param B_BudgetReference_ID Budget Reference	  */
	public void setB_BudgetReference_ID (int B_BudgetReference_ID)
	{
		if (B_BudgetReference_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_B_BudgetReference_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_B_BudgetReference_ID, Integer.valueOf(B_BudgetReference_ID));
	}

	/** Get Budget Reference.
		@return Budget Reference	  */
	public int getB_BudgetReference_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_B_BudgetReference_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set B_BudgetReference_UU.
		@param B_BudgetReference_UU B_BudgetReference_UU	  */
	public void setB_BudgetReference_UU (String B_BudgetReference_UU)
	{
		set_Value (COLUMNNAME_B_BudgetReference_UU, B_BudgetReference_UU);
	}

	/** Get B_BudgetReference_UU.
		@return B_BudgetReference_UU	  */
	public String getB_BudgetReference_UU () 
	{
		return (String)get_Value(COLUMNNAME_B_BudgetReference_UU);
	}

	/** Set Project.
		@param C_Project_ID 
		Financial Project
	  */
	public void setC_Project_ID (int C_Project_ID)
	{
		if (C_Project_ID < 1) 
			set_Value (COLUMNNAME_C_Project_ID, null);
		else 
			set_Value (COLUMNNAME_C_Project_ID, Integer.valueOf(C_Project_ID));
	}

	/** Get Project.
		@return Financial Project
	  */
	public int getC_Project_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Project_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
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

	/** Set Line No.
		@param Line 
		Unique line for this document
	  */
	public void setLine (int Line)
	{
		set_Value (COLUMNNAME_Line, Integer.valueOf(Line));
	}

	/** Get Line No.
		@return Unique line for this document
	  */
	public int getLine () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_Line);
		if (ii == null)
			 return 0;
		return ii.intValue();
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

	/** Operator AD_Reference_ID=200088 */
	public static final int OPERATOR_AD_Reference_ID=200088;
	/** * = * */
	public static final String OPERATOR_Multiply = "*";
	/** + = + */
	public static final String OPERATOR_Plus = "+";
	/** - = - */
	public static final String OPERATOR_Minus = "-";
	/** / = / */
	public static final String OPERATOR_Divide = "/";
	/** Set Operator.
		@param Operator Operator	  */
	public void setOperator (String Operator)
	{

		set_Value (COLUMNNAME_Operator, Operator);
	}

	/** Get Operator.
		@return Operator	  */
	public String getOperator () 
	{
		return (String)get_Value(COLUMNNAME_Operator);
	}

	public org.compiere.model.I_C_Period getPeriodFrom() throws RuntimeException
    {
		return (org.compiere.model.I_C_Period)MTable.get(getCtx(), org.compiere.model.I_C_Period.Table_Name)
			.getPO(getPeriodFrom_ID(), get_TrxName());	}

	/** Set PeriodFrom_ID.
		@param PeriodFrom_ID 
		Starting Period of Fact Accts selected
	  */
	public void setPeriodFrom_ID (int PeriodFrom_ID)
	{
		if (PeriodFrom_ID < 1) 
			set_Value (COLUMNNAME_PeriodFrom_ID, null);
		else 
			set_Value (COLUMNNAME_PeriodFrom_ID, Integer.valueOf(PeriodFrom_ID));
	}

	/** Get PeriodFrom_ID.
		@return Starting Period of Fact Accts selected
	  */
	public int getPeriodFrom_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_PeriodFrom_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_C_Period getPeriodTo() throws RuntimeException
    {
		return (org.compiere.model.I_C_Period)MTable.get(getCtx(), org.compiere.model.I_C_Period.Table_Name)
			.getPO(getPeriodTo_ID(), get_TrxName());	}

	/** Set PeriodTo_ID.
		@param PeriodTo_ID 
		End Period in Fact Accts selected
	  */
	public void setPeriodTo_ID (int PeriodTo_ID)
	{
		if (PeriodTo_ID < 1) 
			set_Value (COLUMNNAME_PeriodTo_ID, null);
		else 
			set_Value (COLUMNNAME_PeriodTo_ID, Integer.valueOf(PeriodTo_ID));
	}

	/** Get PeriodTo_ID.
		@return End Period in Fact Accts selected
	  */
	public int getPeriodTo_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_PeriodTo_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Result.
		@param Result 
		Result of the action taken
	  */
	public void setResult (BigDecimal Result)
	{
		set_Value (COLUMNNAME_Result, Result);
	}

	/** Get Result.
		@return Result of the action taken
	  */
	public BigDecimal getResult () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Result);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}
}