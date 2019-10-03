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

/** Generated Interface for B_BudgetReference
 *  @author iDempiere (generated) 
 *  @version Release 2.1
 */
@SuppressWarnings("all")
public interface I_B_BudgetReference 
{

    /** TableName=B_BudgetReference */
    public static final String Table_Name = "B_BudgetReference";

    /** AD_Table_ID=200138 */
    public static final int Table_ID = 200138;

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 3 - Client - Org 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(3);

    /** Load Meta Data */

    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/** Get Client.
	  * Client/Tenant for this installation.
	  */
	public int getAD_Client_ID();

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

    /** Column name Account_ID */
    public static final String COLUMNNAME_Account_ID = "Account_ID";

	/** Set Account.
	  * Account used
	  */
	public void setAccount_ID (int Account_ID);

	/** Get Account.
	  * Account used
	  */
	public int getAccount_ID();

	public org.compiere.model.I_C_ElementValue getAccount() throws RuntimeException;

    /** Column name Amount */
    public static final String COLUMNNAME_Amount = "Amount";

	/** Set Amount.
	  * Amount in a defined currency
	  */
	public void setAmount (BigDecimal Amount);

	/** Get Amount.
	  * Amount in a defined currency
	  */
	public BigDecimal getAmount();

    /** Column name B_BudgetPlanLine_ID */
    public static final String COLUMNNAME_B_BudgetPlanLine_ID = "B_BudgetPlanLine_ID";

	/** Set B_BudgetPlanLine	  */
	public void setB_BudgetPlanLine_ID (int B_BudgetPlanLine_ID);

	/** Get B_BudgetPlanLine	  */
	public int getB_BudgetPlanLine_ID();

	public I_B_BudgetPlanLine getB_BudgetPlanLine() throws RuntimeException;

    /** Column name B_BudgetReference_ID */
    public static final String COLUMNNAME_B_BudgetReference_ID = "B_BudgetReference_ID";

	/** Set Budget Reference	  */
	public void setB_BudgetReference_ID (int B_BudgetReference_ID);

	/** Get Budget Reference	  */
	public int getB_BudgetReference_ID();

    /** Column name B_BudgetReference_UU */
    public static final String COLUMNNAME_B_BudgetReference_UU = "B_BudgetReference_UU";

	/** Set B_BudgetReference_UU	  */
	public void setB_BudgetReference_UU (String B_BudgetReference_UU);

	/** Get B_BudgetReference_UU	  */
	public String getB_BudgetReference_UU();

    /** Column name C_Project_ID */
    public static final String COLUMNNAME_C_Project_ID = "C_Project_ID";

	/** Set Project.
	  * Financial Project
	  */
	public void setC_Project_ID (int C_Project_ID);

	/** Get Project.
	  * Financial Project
	  */
	public int getC_Project_ID();

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

    /** Column name Help */
    public static final String COLUMNNAME_Help = "Help";

	/** Set Comment/Help.
	  * Comment or Hint
	  */
	public void setHelp (String Help);

	/** Get Comment/Help.
	  * Comment or Hint
	  */
	public String getHelp();

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

    /** Column name IsValid */
    public static final String COLUMNNAME_IsValid = "IsValid";

	/** Set Valid.
	  * Element is valid
	  */
	public void setIsValid (boolean IsValid);

	/** Get Valid.
	  * Element is valid
	  */
	public boolean isValid();

    /** Column name Line */
    public static final String COLUMNNAME_Line = "Line";

	/** Set Line No.
	  * Unique line for this document
	  */
	public void setLine (int Line);

	/** Get Line No.
	  * Unique line for this document
	  */
	public int getLine();

    /** Column name Name */
    public static final String COLUMNNAME_Name = "Name";

	/** Set Name.
	  * Alphanumeric identifier of the entity
	  */
	public void setName (String Name);

	/** Get Name.
	  * Alphanumeric identifier of the entity
	  */
	public String getName();

    /** Column name Operator */
    public static final String COLUMNNAME_Operator = "Operator";

	/** Set Operator	  */
	public void setOperator (String Operator);

	/** Get Operator	  */
	public String getOperator();

    /** Column name PeriodFrom_ID */
    public static final String COLUMNNAME_PeriodFrom_ID = "PeriodFrom_ID";

	/** Set PeriodFrom_ID.
	  * Starting Period of Fact Accts selected
	  */
	public void setPeriodFrom_ID (int PeriodFrom_ID);

	/** Get PeriodFrom_ID.
	  * Starting Period of Fact Accts selected
	  */
	public int getPeriodFrom_ID();

	public org.compiere.model.I_C_Period getPeriodFrom() throws RuntimeException;

    /** Column name PeriodTo_ID */
    public static final String COLUMNNAME_PeriodTo_ID = "PeriodTo_ID";

	/** Set PeriodTo_ID.
	  * End Period in Fact Accts selected
	  */
	public void setPeriodTo_ID (int PeriodTo_ID);

	/** Get PeriodTo_ID.
	  * End Period in Fact Accts selected
	  */
	public int getPeriodTo_ID();

	public org.compiere.model.I_C_Period getPeriodTo() throws RuntimeException;

    /** Column name Result */
    public static final String COLUMNNAME_Result = "Result";

	/** Set Result.
	  * Result of the action taken
	  */
	public void setResult (BigDecimal Result);

	/** Get Result.
	  * Result of the action taken
	  */
	public BigDecimal getResult();

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
