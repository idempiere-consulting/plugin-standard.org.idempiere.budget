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

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;

import org.compiere.model.I_C_ValidCombination;
import org.compiere.model.I_Persistent;
import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.model.POInfo;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;

/** Generated Model for B_BudgetLine
 *  @author Adempiere (generated) 
 *  @version Release 3.7.0LTS - $Id$ */
public class X_B_BudgetLine extends PO implements I_B_BudgetLine, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20141007L;

    /** Standard Constructor */
    public X_B_BudgetLine (Properties ctx, int B_BudgetLine_ID, String trxName)
    {
      super (ctx, B_BudgetLine_ID, trxName);
      /** if (B_BudgetLine_ID == 0)
        {
			setC_ValidCombination_ID (0);
			setDateAcct (new Timestamp( System.currentTimeMillis() ));
// @DateAcct@
			setDocumentNo (null);
        } */
    }

    /** Load Constructor */
    public X_B_BudgetLine (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_B_BudgetLine[")
        .append(get_ID()).append("]");
      return sb.toString();
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

	/** Set Budget Line.
		@param B_BudgetLine_ID Budget Line	  */
	public void setB_BudgetLine_ID (int B_BudgetLine_ID)
	{
		if (B_BudgetLine_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_B_BudgetLine_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_B_BudgetLine_ID, Integer.valueOf(B_BudgetLine_ID));
	}

	/** Get Budget Line.
		@return Budget Line	  */
	public int getB_BudgetLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_B_BudgetLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_B_BudgetPeriod getB_BudgetPeriod() throws RuntimeException
    {
		return (I_B_BudgetPeriod)MTable.get(getCtx(), I_B_BudgetPeriod.Table_Name)
			.getPO(getB_BudgetPeriod_ID(), get_TrxName());	}

	/** Set Budget Period.
		@param B_BudgetPeriod_ID Budget Period	  */
	public void setB_BudgetPeriod_ID (int B_BudgetPeriod_ID)
	{
		if (B_BudgetPeriod_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_B_BudgetPeriod_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_B_BudgetPeriod_ID, Integer.valueOf(B_BudgetPeriod_ID));
	}

	/** Get Budget Period.
		@return Budget Period	  */
	public int getB_BudgetPeriod_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_B_BudgetPeriod_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_C_ValidCombination getC_ValidCombination() throws RuntimeException
    {
		return (I_C_ValidCombination)MTable.get(getCtx(), I_C_ValidCombination.Table_Name)
			.getPO(getC_ValidCombination_ID(), get_TrxName());	}

	/** Set Combination.
		@param C_ValidCombination_ID 
		Valid Account Combination
	  */
	public void setC_ValidCombination_ID (int C_ValidCombination_ID)
	{
		if (C_ValidCombination_ID < 1) 
			set_Value (COLUMNNAME_C_ValidCombination_ID, null);
		else 
			set_Value (COLUMNNAME_C_ValidCombination_ID, Integer.valueOf(C_ValidCombination_ID));
	}

	/** Get Combination.
		@return Valid Account Combination
	  */
	public int getC_ValidCombination_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_ValidCombination_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Account Date.
		@param DateAcct 
		Accounting Date
	  */
	public void setDateAcct (Timestamp DateAcct)
	{
		set_Value (COLUMNNAME_DateAcct, DateAcct);
	}

	/** Get Account Date.
		@return Accounting Date
	  */
	public Timestamp getDateAcct () 
	{
		return (Timestamp)get_Value(COLUMNNAME_DateAcct);
	}

	/** Set Document No.
		@param DocumentNo 
		Document sequence number of the document
	  */
	public void setDocumentNo (String DocumentNo)
	{
		set_ValueNoCheck (COLUMNNAME_DocumentNo, DocumentNo);
	}

	/** Get Document No.
		@return Document sequence number of the document
	  */
	public String getDocumentNo () 
	{
		return (String)get_Value(COLUMNNAME_DocumentNo);
	}

    /** Get Record ID/ColumnName
        @return ID/ColumnName pair
      */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), getDocumentNo());
    }

	/** Set Quantity.
		@param Qty 
		Quantity
	  */
	public void setQty (BigDecimal Qty)
	{
		set_Value (COLUMNNAME_Qty, Qty);
	}

	/** Get Quantity.
		@return Quantity
	  */
	public BigDecimal getQty () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Qty);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}
}