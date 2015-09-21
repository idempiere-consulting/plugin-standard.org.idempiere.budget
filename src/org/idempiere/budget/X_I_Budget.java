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

/** Generated Model for I_Budget
 *  @author iDempiere (generated) 
 *  @version Release 2.1 - $Id$ */
public class X_I_Budget extends PO implements I_I_Budget, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20150921L;

    /** Standard Constructor */
    public X_I_Budget (Properties ctx, int I_Budget_ID, String trxName)
    {
      super (ctx, I_Budget_ID, trxName);
      /** if (I_Budget_ID == 0)
        {
			setI_Budget_ID (0);
        } */
    }

    /** Load Constructor */
    public X_I_Budget (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 2 - Client 
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
      StringBuffer sb = new StringBuffer ("X_I_Budget[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set AD_OrgDoc.
		@param AD_OrgDoc AD_OrgDoc	  */
	public void setAD_OrgDoc (String AD_OrgDoc)
	{
		set_Value (COLUMNNAME_AD_OrgDoc, AD_OrgDoc);
	}

	/** Get AD_OrgDoc.
		@return AD_OrgDoc	  */
	public String getAD_OrgDoc () 
	{
		return (String)get_Value(COLUMNNAME_AD_OrgDoc);
	}

	/** Set Account.
		@param Account Account	  */
	public void setAccount (String Account)
	{
		set_Value (COLUMNNAME_Account, Account);
	}

	/** Get Account.
		@return Account	  */
	public String getAccount () 
	{
		return (String)get_Value(COLUMNNAME_Account);
	}

	/** Set Accounted Credit.
		@param AmtAcctCr 
		Accounted Credit Amount
	  */
	public void setAmtAcctCr (BigDecimal AmtAcctCr)
	{
		set_Value (COLUMNNAME_AmtAcctCr, AmtAcctCr);
	}

	/** Get Accounted Credit.
		@return Accounted Credit Amount
	  */
	public BigDecimal getAmtAcctCr () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_AmtAcctCr);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Accounted Debit.
		@param AmtAcctDr 
		Accounted Debit Amount
	  */
	public void setAmtAcctDr (BigDecimal AmtAcctDr)
	{
		set_Value (COLUMNNAME_AmtAcctDr, AmtAcctDr);
	}

	/** Get Accounted Debit.
		@return Accounted Debit Amount
	  */
	public BigDecimal getAmtAcctDr () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_AmtAcctDr);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set C_Activity.
		@param C_Activity C_Activity	  */
	public void setC_Activity (String C_Activity)
	{
		set_Value (COLUMNNAME_C_Activity, C_Activity);
	}

	/** Get C_Activity.
		@return C_Activity	  */
	public String getC_Activity () 
	{
		return (String)get_Value(COLUMNNAME_C_Activity);
	}

	/** Set C_BPartner.
		@param C_BPartner C_BPartner	  */
	public void setC_BPartner (String C_BPartner)
	{
		set_Value (COLUMNNAME_C_BPartner, C_BPartner);
	}

	/** Get C_BPartner.
		@return C_BPartner	  */
	public String getC_BPartner () 
	{
		return (String)get_Value(COLUMNNAME_C_BPartner);
	}

	/** Set C_Campaign.
		@param C_Campaign C_Campaign	  */
	public void setC_Campaign (String C_Campaign)
	{
		set_Value (COLUMNNAME_C_Campaign, C_Campaign);
	}

	/** Get C_Campaign.
		@return C_Campaign	  */
	public String getC_Campaign () 
	{
		return (String)get_Value(COLUMNNAME_C_Campaign);
	}

	/** Set C_Period.
		@param C_Period C_Period	  */
	public void setC_Period (String C_Period)
	{
		set_Value (COLUMNNAME_C_Period, C_Period);
	}

	/** Get C_Period.
		@return C_Period	  */
	public String getC_Period () 
	{
		return (String)get_Value(COLUMNNAME_C_Period);
	}

	/** Set C_Project.
		@param C_Project C_Project	  */
	public void setC_Project (String C_Project)
	{
		set_Value (COLUMNNAME_C_Project, C_Project);
	}

	/** Get C_Project.
		@return C_Project	  */
	public String getC_Project () 
	{
		return (String)get_Value(COLUMNNAME_C_Project);
	}

	/** Set Document No.
		@param DocumentNo 
		Document sequence number of the document
	  */
	public void setDocumentNo (String DocumentNo)
	{
		set_Value (COLUMNNAME_DocumentNo, DocumentNo);
	}

	/** Get Document No.
		@return Document sequence number of the document
	  */
	public String getDocumentNo () 
	{
		return (String)get_Value(COLUMNNAME_DocumentNo);
	}

	/** Set I_Budget.
		@param I_Budget_ID I_Budget	  */
	public void setI_Budget_ID (int I_Budget_ID)
	{
		if (I_Budget_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_I_Budget_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_I_Budget_ID, Integer.valueOf(I_Budget_ID));
	}

	/** Get I_Budget.
		@return I_Budget	  */
	public int getI_Budget_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_I_Budget_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set I_Budget_UU.
		@param I_Budget_UU I_Budget_UU	  */
	public void setI_Budget_UU (String I_Budget_UU)
	{
		set_Value (COLUMNNAME_I_Budget_UU, I_Budget_UU);
	}

	/** Get I_Budget_UU.
		@return I_Budget_UU	  */
	public String getI_Budget_UU () 
	{
		return (String)get_Value(COLUMNNAME_I_Budget_UU);
	}

	/** Set Import Error Message.
		@param I_ErrorMsg 
		Messages generated from import process
	  */
	public void setI_ErrorMsg (String I_ErrorMsg)
	{
		set_Value (COLUMNNAME_I_ErrorMsg, I_ErrorMsg);
	}

	/** Get Import Error Message.
		@return Messages generated from import process
	  */
	public String getI_ErrorMsg () 
	{
		return (String)get_Value(COLUMNNAME_I_ErrorMsg);
	}

	/** Set Imported.
		@param I_IsImported 
		Has this import been processed
	  */
	public void setI_IsImported (boolean I_IsImported)
	{
		set_Value (COLUMNNAME_I_IsImported, Boolean.valueOf(I_IsImported));
	}

	/** Get Imported.
		@return Has this import been processed
	  */
	public boolean isI_IsImported () 
	{
		Object oo = get_Value(COLUMNNAME_I_IsImported);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Sales Transaction.
		@param IsSOTrx 
		This is a Sales Transaction
	  */
	public void setIsSOTrx (boolean IsSOTrx)
	{
		set_Value (COLUMNNAME_IsSOTrx, Boolean.valueOf(IsSOTrx));
	}

	/** Get Sales Transaction.
		@return This is a Sales Transaction
	  */
	public boolean isSOTrx () 
	{
		Object oo = get_Value(COLUMNNAME_IsSOTrx);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set M_Product.
		@param M_Product M_Product	  */
	public void setM_Product (String M_Product)
	{
		set_Value (COLUMNNAME_M_Product, M_Product);
	}

	/** Get M_Product.
		@return M_Product	  */
	public String getM_Product () 
	{
		return (String)get_Value(COLUMNNAME_M_Product);
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

	/** Set Percent.
		@param Percent 
		Percentage
	  */
	public void setPercent (BigDecimal Percent)
	{
		set_Value (COLUMNNAME_Percent, Percent);
	}

	/** Get Percent.
		@return Percentage
	  */
	public BigDecimal getPercent () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Percent);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set PercentageBase.
		@param PercentageBase 
		Alternative percentage lookup
	  */
	public void setPercentageBase (String PercentageBase)
	{
		set_Value (COLUMNNAME_PercentageBase, PercentageBase);
	}

	/** Get PercentageBase.
		@return Alternative percentage lookup
	  */
	public String getPercentageBase () 
	{
		return (String)get_Value(COLUMNNAME_PercentageBase);
	}

	/** Set PeriodFrom.
		@param PeriodFrom 
		Starting Period of Fact Accts selected
	  */
	public void setPeriodFrom (String PeriodFrom)
	{
		set_Value (COLUMNNAME_PeriodFrom, PeriodFrom);
	}

	/** Get PeriodFrom.
		@return Starting Period of Fact Accts selected
	  */
	public String getPeriodFrom () 
	{
		return (String)get_Value(COLUMNNAME_PeriodFrom);
	}

	/** Set PeriodTo.
		@param PeriodTo 
		End Period in Fact Accts selected
	  */
	public void setPeriodTo (String PeriodTo)
	{
		set_Value (COLUMNNAME_PeriodTo, PeriodTo);
	}

	/** Get PeriodTo.
		@return End Period in Fact Accts selected
	  */
	public String getPeriodTo () 
	{
		return (String)get_Value(COLUMNNAME_PeriodTo);
	}

	/** Set Processed.
		@param Processed 
		The document has been processed
	  */
	public void setProcessed (boolean Processed)
	{
		set_Value (COLUMNNAME_Processed, Boolean.valueOf(Processed));
	}

	/** Get Processed.
		@return The document has been processed
	  */
	public boolean isProcessed () 
	{
		Object oo = get_Value(COLUMNNAME_Processed);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Process Now.
		@param Processing Process Now	  */
	public void setProcessing (boolean Processing)
	{
		set_Value (COLUMNNAME_Processing, Boolean.valueOf(Processing));
	}

	/** Get Process Now.
		@return Process Now	  */
	public boolean isProcessing () 
	{
		Object oo = get_Value(COLUMNNAME_Processing);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
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

	/** Set SubAccount.
		@param SubAccount SubAccount	  */
	public void setSubAccount (String SubAccount)
	{
		set_Value (COLUMNNAME_SubAccount, SubAccount);
	}

	/** Get SubAccount.
		@return SubAccount	  */
	public String getSubAccount () 
	{
		return (String)get_Value(COLUMNNAME_SubAccount);
	}

	/** Set SubAmount.
		@param SubAmount SubAmount	  */
	public void setSubAmount (BigDecimal SubAmount)
	{
		set_Value (COLUMNNAME_SubAmount, SubAmount);
	}

	/** Get SubAmount.
		@return SubAmount	  */
	public BigDecimal getSubAmount () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_SubAmount);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}
}