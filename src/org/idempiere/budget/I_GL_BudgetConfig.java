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

/** Generated Interface for GL_BudgetConfig
 *  @author iDempiere (generated) 
 *  @version Release 2.0
 */
@SuppressWarnings("all")
public interface I_GL_BudgetConfig 
{

    /** TableName=GL_BudgetConfig */
    public static final String Table_Name = "GL_BudgetConfig";

    /** AD_Table_ID=1000000 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

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

    /** Column name BudgetTrend */
    public static final String COLUMNNAME_BudgetTrend = "BudgetTrend";

	/** Set Budget Trend.
	  * Budget Trend to use in revenue estimate
	  */
	public void setBudgetTrend (String BudgetTrend);

	/** Get Budget Trend.
	  * Budget Trend to use in revenue estimate
	  */
	public String getBudgetTrend();

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

    /** Column name Description */
    public static final String COLUMNNAME_Description = "Description";

	/** Set Description.
	  * Optional short description of the record
	  */
	public void setDescription (String Description);

	/** Get Description.
	  * Optional short description of the record
	  */
	public String getDescription();

    /** Column name GL_BudgetConfig_ID */
    public static final String COLUMNNAME_GL_BudgetConfig_ID = "GL_BudgetConfig_ID";

	/** Set Budget Configurator	  */
	public void setGL_BudgetConfig_ID (int GL_BudgetConfig_ID);

	/** Get Budget Configurator	  */
	public int getGL_BudgetConfig_ID();

    /** Column name GL_BudgetConfig_UU */
    public static final String COLUMNNAME_GL_BudgetConfig_UU = "GL_BudgetConfig_UU";

	/** Set GL_BudgetConfig_UU	  */
	public void setGL_BudgetConfig_UU (String GL_BudgetConfig_UU);

	/** Get GL_BudgetConfig_UU	  */
	public String getGL_BudgetConfig_UU();

    /** Column name GL_Budget_Forecast_Month */
    public static final String COLUMNNAME_GL_Budget_Forecast_Month = "GL_Budget_Forecast_Month";

	/** Set Budget Forecast Month.
	  * No. of months to base revenue and other calculations
	  */
	public void setGL_Budget_Forecast_Month (BigDecimal GL_Budget_Forecast_Month);

	/** Get Budget Forecast Month.
	  * No. of months to base revenue and other calculations
	  */
	public BigDecimal getGL_Budget_Forecast_Month();

    /** Column name GL_Budget_Forecast_Year */
    public static final String COLUMNNAME_GL_Budget_Forecast_Year = "GL_Budget_Forecast_Year";

	/** Set Budget Forecast Year.
	  * No of years back to forecast or this year or estimate
	  */
	public void setGL_Budget_Forecast_Year (BigDecimal GL_Budget_Forecast_Year);

	/** Get Budget Forecast Year.
	  * No of years back to forecast or this year or estimate
	  */
	public BigDecimal getGL_Budget_Forecast_Year();

    /** Column name GL_Budget_ID */
    public static final String COLUMNNAME_GL_Budget_ID = "GL_Budget_ID";

	/** Set Budget.
	  * General Ledger Budget
	  */
	public void setGL_Budget_ID (int GL_Budget_ID);

	/** Get Budget.
	  * General Ledger Budget
	  */
	public int getGL_Budget_ID();

	public org.compiere.model.I_GL_Budget getGL_Budget() throws RuntimeException;

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

    /** Column name MonthToMonth */
    public static final String COLUMNNAME_MonthToMonth = "MonthToMonth";

	/** Set Month to month.
	  * Same months of previous years (Year On Year)
	  */
	public void setMonthToMonth (boolean MonthToMonth);

	/** Get Month to month.
	  * Same months of previous years (Year On Year)
	  */
	public boolean isMonthToMonth();

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

    /** Column name Prorata */
    public static final String COLUMNNAME_Prorata = "Prorata";

	/** Set Prorata.
	  * Prorata over number of periods
	  */
	public void setProrata (boolean Prorata);

	/** Get Prorata.
	  * Prorata over number of periods
	  */
	public boolean isProrata();

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
