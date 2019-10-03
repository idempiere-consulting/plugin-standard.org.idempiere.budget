/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.idempiere.budget;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;

import org.compiere.model.MActivity;
import org.compiere.model.MBPartner;
import org.compiere.model.MCampaign;
import org.compiere.model.MElementValue;
import org.compiere.model.MOrg;
import org.compiere.model.MPeriod;
import org.compiere.model.MProduct;
import org.compiere.model.MProject;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;

/**
 *	Import Order from I_Order
 *  @author Oscar Gomez
 * 			<li>BF [ 2936629 ] Error when creating bpartner in the importation order
 * 			<li>https://sourceforge.net/tracker/?func=detail&aid=2936629&group_id=176962&atid=879332
 * 	@author 	Jorg Janke
 * 	@version 	$Id: ImportOrder.java,v 1.2 2006/07/30 00:51:02 jjanke Exp $
 */
public class ImportBudget extends SvrProcess
{
	/**	Client to be imported to		*/
	private int				m_AD_Client_ID = 0;
	/**	Organization to be imported to		*/
	private int				m_AD_Org_ID = 0;
	/**	Delete old Imported				*/
	private boolean			m_deleteOldImported = false;
	/**	Document Action					*/ 
	boolean mainrule = false;
	/** Effective						*/
	private Timestamp		m_DateValue = null;

	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (name.equals("AD_Client_ID"))
				m_AD_Client_ID = ((BigDecimal)para[i].getParameter()).intValue();
			else if (name.equals("AD_Org_ID"))
				m_AD_Org_ID = ((BigDecimal)para[i].getParameter()).intValue();
			else if (name.equals("DeleteOldImported"))
				m_deleteOldImported = "Y".equals(para[i].getParameter());
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
		if (m_DateValue == null)
			m_DateValue = new Timestamp (System.currentTimeMillis());
	}	//	prepare

	/**
	 *  Perform process.
	 *  @return Message
	 *  @throws Exception
	 */
	protected String doIt() throws java.lang.Exception
	{
		StringBuffer sql = null;
		int no = 0;
		String clientCheck = " AND AD_Client_ID=" + m_AD_Client_ID;

		//	****	Prepare	****

		//	Delete Old Imported
		if (m_deleteOldImported)
		{
			sql = new StringBuffer ("DELETE I_Budget "
				  + "WHERE I_IsImported='Y'").append (clientCheck);
			no = DB.executeUpdate(sql.toString(), get_TrxName());
			log.fine("Delete Old Impored =" + no);
		}

		//	Set Client, Org, IsActive, Created/Updated
		sql = new StringBuffer ("UPDATE I_Budget "
			  + "SET AD_Client_ID = COALESCE (AD_Client_ID,").append (m_AD_Client_ID).append ("),"
			  + " AD_Org_ID = COALESCE (AD_Org_ID,").append (m_AD_Org_ID).append ("),"
			  + " IsActive = COALESCE (IsActive, 'Y'),"
			  + " Created = COALESCE (Created, SysDate),"
			  + " CreatedBy = COALESCE (CreatedBy, 0),"
			  + " Updated = COALESCE (Updated, SysDate),"
			  + " UpdatedBy = COALESCE (UpdatedBy, 0),"
			  + " I_ErrorMsg = ' ',"
			  + " I_IsImported = 'N' "
			  + "WHERE I_IsImported<>'Y' OR I_IsImported IS NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.info ("Reset=" + no);

		sql = new StringBuffer ("UPDATE I_Budget o "
			+ "SET I_IsImported='E', I_ErrorMsg=I_ErrorMsg||'ERR=Invalid Org, '"
			+ "WHERE (AD_Org_ID IS NULL OR AD_Org_ID=0"
			+ " OR EXISTS (SELECT * FROM AD_Org oo WHERE o.AD_Org_ID=oo.AD_Org_ID AND (oo.IsSummary='Y' OR oo.IsActive='N')))"
			+ " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning ("Invalid Org=" + no);

		commitEx();
		
		//	-- New Budget ---------------------------------------------------
		int noInsert = 0;
		int noInsertLine = 0;

		//	Go through Budget Records not imported yet 
		List<MIBudget> imports = new Query(Env.getCtx(), MIBudget.Table_Name, MIBudget.COLUMNNAME_I_IsImported+"=?", get_TrxName())
		.setParameters("N")
		.setOrderBy("Created")
		.list(); 
		int budgetLine = 0; //hold FK for sub reference record
		MBudgetPlan budget = null;
		if (imports.size()>0) {
			//create new Budget header
			budget = new MBudgetPlan(Env.getCtx(),0,get_TrxName());
			budget.setC_AcctSchema_ID(101);
			budget.setC_Currency_ID(100);
			budget.setC_DocType_ID(115);
			budget.setDateDoc(m_DateValue);
			budget.saveEx(get_TrxName());
		}
			
		for (MIBudget imp:imports){
			String errors = "";
			mainrule = false; //flag if false at the end, then create sub detail reference record		
			//lookup value for BudgetPlanLine
			int orgid = getDocOrgID(imp.getAD_OrgDoc());
			if (orgid==-1)
				errors = errors+"Org* ";
			int bpartner = getBPartnerID(imp.getC_BPartner());
			if (bpartner==-1)
				errors = errors+"BPartner* ";		
			int acctid = getAcctID(imp.getAccount());
			if (acctid>0) mainrule = true;
			if (acctid == -1 )
				errors = errors+"Account* ";
			int periodid = getPeriodID(imp.getC_Period());
			if (periodid>0) mainrule = true;
			if (periodid==-1)
				errors = errors+"Period* ";
			int productid = getProductID(imp.getM_Product());
			if (productid==-1)
				errors = errors+"Product* ";
			int projectID = getProjectID(imp.getC_Project());
			if (projectID==-1)
				errors = errors + "Project* ";
			int campaignid = getCampaignID(imp.getC_Campaign());
			if (campaignid==-1)
				errors = errors+"Campaign* ";
			int activityID = getActivity(imp.getC_Activity());
			if (activityID==-1)
				errors = errors+"Activity* ";
			int percentagebase = getAcctID(imp.getPercentageBase());
			if (percentagebase==-1)
				errors = errors+"PercentageBase* ";
			int subaccount = getAcctID(imp.getSubAccount());
			if(subaccount==-1)
				errors = errors+"SubAccount* ";
			int periodfrom = getPeriodID(imp.getPeriodFrom());
			if (periodfrom==-1)
				errors = errors+"PeriodFrom* ";			
			int periodto = getPeriodID(imp.getPeriodTo());
				if (periodto==-1)
					errors = errors+"PeriodTo* ";	
				
			//build B_BudgetPlanLine
			if (mainrule) { //if its elements have values
				MBudgetPlanLine plan = new MBudgetPlanLine(Env.getCtx(),0,get_TrxName());
				if (bpartner>0)
					plan.setC_BPartner_ID(bpartner);
				if (orgid>0)
					plan.setAD_OrgDoc_ID(orgid);
				if (acctid>0)
					plan.setAccount_ID(acctid);
				if (periodid>0)
					plan.setC_Period_ID(periodid);
				plan.setIsSOTrx(imp.isSOTrx());
				plan.setPercent(imp.getPercent());
				plan.setAmtAcctCr(imp.getAmtAcctCr());
				plan.setAmtAcctDr(imp.getAmtAcctDr());
				if (productid>0)
					plan.setM_Product_ID(productid);
				plan.setQty(imp.getQty());
				if (projectID>0)
					plan.setC_Project_ID(projectID);
				if (campaignid>0)
					plan.setC_Campaign_ID(campaignid);
				if (activityID>0)
					plan.setC_Activity_ID(activityID);
				if (percentagebase>0)
					plan.setPercentageBase_ID(percentagebase);
				plan.setB_BudgetPlan_ID(budget.getB_BudgetPlan_ID());
				if (errors.isEmpty()) {
					plan.setLine(++noInsert);
					plan.saveEx(get_TrxName());
					budgetLine = plan.getB_BudgetPlanLine_ID();
				}
			}			
			//create BudgetReference
			if (errors.isEmpty() && budgetLine>0) {
				if (imp.getOperator()!=null || subaccount>0 || imp.getSubAmount()!=null || periodfrom>0 || periodto>0) 
				{
					MBudgetReference subref = new MBudgetReference(Env.getCtx(),0, get_TrxName());
					subref.setOperator(imp.getOperator());
					if (periodfrom>0)
						subref.setPeriodFrom_ID(periodfrom);
					if (periodto>0)
						subref.setPeriodTo_ID(periodto);
					if (subaccount>0)
						subref.setAccount_ID(subaccount);
					subref.setAmount(imp.getSubAmount());
					subref.setB_BudgetPlanLine_ID(budgetLine);
					subref.saveEx(get_TrxName());
					noInsertLine++;
				}
				imp.setI_IsImported(true);
				imp.setProcessed(true);
			}
			else	
				imp.setI_ErrorMsg(errors);
			
			imp.saveEx(get_TrxName());
		}
		addLog (0, null, new BigDecimal (noInsert), "@B_BudgetPlaneLine_ID@: @Inserted@");
		addLog (0, null, new BigDecimal (noInsertLine), "@B_BudgetReference_ID@: @Inserted@");
		return "#" + noInsert + "/" + noInsertLine;
	}	//	doIt
	
	private int getActivity(String c_Activity) {
		if (c_Activity==null) return 0;
		MActivity activity = new Query(Env.getCtx(),MActivity.Table_Name,MActivity.COLUMNNAME_Value+"=?",get_TrxName())
		.setParameters(c_Activity)
		.first();
		if (activity==null) return -1;
		mainrule = true;
		return activity.get_ID();
	}
	private int getCampaignID(String c_Campaign) {
		if (c_Campaign==null) return 0;
		MCampaign campaign = new Query(Env.getCtx(),MCampaign.Table_Name,MCampaign.COLUMNNAME_Value+"=?",get_TrxName())
		.setParameters(c_Campaign)
		.first();
		if (campaign==null) return -1;
		mainrule = true;
		return campaign.get_ID();
	}
	private int getProjectID(String c_Project) { 
		if (c_Project==null) return 0;
		MProject project = new Query(Env.getCtx(), MProject.Table_Name,MProject.COLUMNNAME_Value+"=?",get_TrxName())
		.setParameters(c_Project)
		.first();
		if (project==null) return -1;
		mainrule = true;
		return project.get_ID();
	}
	private int getProductID(String m_Product) {
		if (m_Product==null) return 0;
		MProduct product = new Query(Env.getCtx(),MProduct.Table_Name,MProduct.COLUMNNAME_Value+"=?",get_TrxName())
		.setParameters(m_Product)
		.first();
		if (product==null) return -1;
		mainrule = true;
		return product.get_ID();
	}
	private int getPeriodID(String c_Period) {
		if (c_Period==null) return 0;
		MPeriod period = new Query(Env.getCtx(),MPeriod.Table_Name, MPeriod.COLUMNNAME_Name+"=?", get_TrxName())
		.setParameters(c_Period)
		.first();
		if (period==null) return -1;
		return period.get_ID();
	}
	private int getBPartnerID(String c_BPartner) { 
		if (c_BPartner==null) return 0;
		MBPartner partner = new Query(Env.getCtx(),MBPartner.Table_Name, MBPartner.COLUMNNAME_Value+"=?", get_TrxName())
		.setParameters(c_BPartner)
		.first();
		if (partner==null) return -1;
		mainrule = true;
		return partner.get_ID();
	}
	private int getAcctID(String account) {
		if (account==null) return 0;
		MElementValue element = new Query (Env.getCtx(), MElementValue.Table_Name, MElementValue.COLUMNNAME_Value+"=?", get_TrxName())
		.setParameters(account)
		.first();
		if (element==null) return -1; 
		return element.get_ID();
	}
	private int getDocOrgID(String ad_OrgDoc) {
		if (ad_OrgDoc==null) return 0;
		MOrg org = new Query(Env.getCtx(),MOrg.Table_Name, MOrg.COLUMNNAME_Name+"=?", get_TrxName())
		.setParameters(ad_OrgDoc)
		.first();
		if (org==null) return -1;
		mainrule = true;
		return org.get_ID();
	}
}	//	ImportOrder
