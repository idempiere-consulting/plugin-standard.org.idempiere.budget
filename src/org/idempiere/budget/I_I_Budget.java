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
package org.idempiere.budget;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.compiere.model.*;
import org.compiere.util.KeyNamePair;

/** Generated Interface for I_Budget
 *  @author iDempiere (generated) 
 *  @version Release 2.1
 */
@SuppressWarnings("all")
public interface I_I_Budget 
{

    /** TableName=I_Budget */
    public static final String Table_Name = "I_Budget";

    /** AD_Table_ID=200139 */
    public static final int Table_ID = 200139;

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 2 - Client 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(2);

    /** Load Meta Data */

    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/** Get Client.
	  * Client/Tenant for this installation.
	  */
	public int getAD_Client_ID();

    /** Column name AD_OrgDoc */
    public static final String COLUMNNAME_AD_OrgDoc = "AD_OrgDoc";

	/** Set AD_OrgDoc	  */
	public void setAD_OrgDoc (String AD_OrgDoc);

	/** Get AD_OrgDoc	  */
	public String getAD_OrgDoc();

    /** Column name AD_Org_ID */
    public static final String COLUMNNAME_AD_Org_ID = "AD_Org_ID";

	/** Set Organization.
	  * Organizational entity within client
	  */
	public void setAD_Org_ID (int AD_Org_ID);

	/** Get Organization.
	  * Organizational entity within client
	  */
	public int getAD_Org_ID();

    /** Column name Account */
    public static final String COLUMNNAME_Account = "Account";

	/** Set Account	  */
	public void setAccount (String Account);

	/** Get Account	  */
	public String getAccount();

    /** Column name AmtAcctCr */
    public static final String COLUMNNAME_AmtAcctCr = "AmtAcctCr";

	/** Set Accounted Credit.
	  * Accounted Credit Amount
	  */
	public void setAmtAcctCr (BigDecimal AmtAcctCr);

	/** Get Accounted Credit.
	  * Accounted Credit Amount
	  */
	public BigDecimal getAmtAcctCr();

    /** Column name AmtAcctDr */
    public static final String COLUMNNAME_AmtAcctDr = "AmtAcctDr";

	/** Set Accounted Debit.
	  * Accounted Debit Amount
	  */
	public void setAmtAcctDr (BigDecimal AmtAcctDr);

	/** Get Accounted Debit.
	  * Accounted Debit Amount
	  */
	public BigDecimal getAmtAcctDr();

    /** Column name C_Activity */
    public static final String COLUMNNAME_C_Activity = "C_Activity";

	/** Set C_Activity	  */
	public void setC_Activity (String C_Activity);

	/** Get C_Activity	  */
	public String getC_Activity();

    /** Column name C_BPartner */
    public static final String COLUMNNAME_C_BPartner = "C_BPartner";

	/** Set C_BPartner	  */
	public void setC_BPartner (String C_BPartner);

	/** Get C_BPartner	  */
	public String getC_BPartner();

    /** Column name C_Campaign */
    public static final String COLUMNNAME_C_Campaign = "C_Campaign";

	/** Set C_Campaign	  */
	public void setC_Campaign (String C_Campaign);

	/** Get C_Campaign	  */
	public String getC_Campaign();

    /** Column name C_Period */
    public static final String COLUMNNAME_C_Period = "C_Period";

	/** Set C_Period	  */
	public void setC_Period (String C_Period);

	/** Get C_Period	  */
	public String getC_Period();

    /** Column name C_Project */
    public static final String COLUMNNAME_C_Project = "C_Project";

	/** Set C_Project	  */
	public void setC_Project (String C_Project);

	/** Get C_Project	  */
	public String getC_Project();

    /** Column name Created */
    public static final String COLUMNNAME_Created = "Created";

	/** Get Created.
	  * Date this record was created
	  */
	public Timestamp getCreated();

    /** Column name CreatedBy */
    public static final String COLUMNNAME_CreatedBy = "CreatedBy";

	/** Get Created By.
	  * User who created this records
	  */
	public int getCreatedBy();

    /** Column name DocumentNo */
    public static final String COLUMNNAME_DocumentNo = "DocumentNo";

	/** Set Document No.
	  * Document sequence number of the document
	  */
	public void setDocumentNo (String DocumentNo);

	/** Get Document No.
	  * Document sequence number of the document
	  */
	public String getDocumentNo();

    /** Column name I_Budget_ID */
    public static final String COLUMNNAME_I_Budget_ID = "I_Budget_ID";

	/** Set I_Budget	  */
	public void setI_Budget_ID (int I_Budget_ID);

	/** Get I_Budget	  */
	public int getI_Budget_ID();

    /** Column name I_Budget_UU */
    public static final String COLUMNNAME_I_Budget_UU = "I_Budget_UU";

	/** Set I_Budget_UU	  */
	public void setI_Budget_UU (String I_Budget_UU);

	/** Get I_Budget_UU	  */
	public String getI_Budget_UU();

    /** Column name I_ErrorMsg */
    public static final String COLUMNNAME_I_ErrorMsg = "I_ErrorMsg";

	/** Set Import Error Message.
	  * Messages generated from import process
	  */
	public void setI_ErrorMsg (String I_ErrorMsg);

	/** Get Import Error Message.
	  * Messages generated from import process
	  */
	public String getI_ErrorMsg();

    /** Column name I_IsImported */
    public static final String COLUMNNAME_I_IsImported = "I_IsImported";

	/** Set Imported.
	  * Has this import been processed
	  */
	public void setI_IsImported (boolean I_IsImported);

	/** Get Imported.
	  * Has this import been processed
	  */
	public boolean isI_IsImported();

    /** Column name IsActive */
    public static final String COLUMNNAME_IsActive = "IsActive";

	/** Set Active.
	  * The record is active in the system
	  */
	public void setIsActive (boolean IsActive);

	/** Get Active.
	  * The record is active in the system
	  */
	public boolean isActive();

    /** Column name IsSOTrx */
    public static final String COLUMNNAME_IsSOTrx = "IsSOTrx";

	/** Set Sales Transaction.
	  * This is a Sales Transaction
	  */
	public void setIsSOTrx (boolean IsSOTrx);

	/** Get Sales Transaction.
	  * This is a Sales Transaction
	  */
	public boolean isSOTrx();

    /** Column name M_Product */
    public static final String COLUMNNAME_M_Product = "M_Product";

	/** Set M_Product	  */
	public void setM_Product (String M_Product);

	/** Get M_Product	  */
	public String getM_Product();

    /** Column name Operator */
    public static final String COLUMNNAME_Operator = "Operator";

	/** Set Operator	  */
	public void setOperator (String Operator);

	/** Get Operator	  */
	public String getOperator();

    /** Column name Percent */
    public static final String COLUMNNAME_Percent = "Percent";

	/** Set Percent.
	  * Percentage
	  */
	public void setPercent (BigDecimal Percent);

	/** Get Percent.
	  * Percentage
	  */
	public BigDecimal getPercent();

    /** Column name PercentageBase */
    public static final String COLUMNNAME_PercentageBase = "PercentageBase";

	/** Set PercentageBase.
	  * Alternative percentage lookup
	  */
	public void setPercentageBase (String PercentageBase);

	/** Get PercentageBase.
	  * Alternative percentage lookup
	  */
	public String getPercentageBase();

    /** Column name PeriodFrom */
    public static final String COLUMNNAME_PeriodFrom = "PeriodFrom";

	/** Set PeriodFrom.
	  * Starting Period of Fact Accts selected
	  */
	public void setPeriodFrom (String PeriodFrom);

	/** Get PeriodFrom.
	  * Starting Period of Fact Accts selected
	  */
	public String getPeriodFrom();

    /** Column name PeriodTo */
    public static final String COLUMNNAME_PeriodTo = "PeriodTo";

	/** Set PeriodTo.
	  * End Period in Fact Accts selected
	  */
	public void setPeriodTo (String PeriodTo);

	/** Get PeriodTo.
	  * End Period in Fact Accts selected
	  */
	public String getPeriodTo();

    /** Column name Processed */
    public static final String COLUMNNAME_Processed = "Processed";

	/** Set Processed.
	  * The document has been processed
	  */
	public void setProcessed (boolean Processed);

	/** Get Processed.
	  * The document has been processed
	  */
	public boolean isProcessed();

    /** Column name Processing */
    public static final String COLUMNNAME_Processing = "Processing";

	/** Set Process Now	  */
	public void setProcessing (boolean Processing);

	/** Get Process Now	  */
	public boolean isProcessing();

    /** Column name Qty */
    public static final String COLUMNNAME_Qty = "Qty";

	/** Set Quantity.
	  * Quantity
	  */
	public void setQty (BigDecimal Qty);

	/** Get Quantity.
	  * Quantity
	  */
	public BigDecimal getQty();

    /** Column name SubAccount */
    public static final String COLUMNNAME_SubAccount = "SubAccount";

	/** Set SubAccount	  */
	public void setSubAccount (String SubAccount);

	/** Get SubAccount	  */
	public String getSubAccount();

    /** Column name SubAmount */
    public static final String COLUMNNAME_SubAmount = "SubAmount";

	/** Set SubAmount	  */
	public void setSubAmount (BigDecimal SubAmount);

	/** Get SubAmount	  */
	public BigDecimal getSubAmount();

    /** Column name Updated */
    public static final String COLUMNNAME_Updated = "Updated";

	/** Get Updated.
	  * Date this record was updated
	  */
	public Timestamp getUpdated();

    /** Column name UpdatedBy */
    public static final String COLUMNNAME_UpdatedBy = "UpdatedBy";

	/** Get Updated By.
	  * User who updated this records
	  */
	public int getUpdatedBy();
}
