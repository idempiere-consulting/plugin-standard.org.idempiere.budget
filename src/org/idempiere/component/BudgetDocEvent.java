/******************************************************************************
 * Product: iDempiere Free ERP Project based on Compiere (2006)               *
 * Copyright (C) 2014 Redhuan D. Oon All Rights Reserved.                     *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *  FOR NON-COMMERCIAL DEVELOPER USE ONLY                                     *
 *  @author Redhuan D. Oon  - red1@red1.org  www.red1.org  www.sysnova.com    *
 *****************************************************************************/

package org.idempiere.component;

import org.adempiere.base.event.AbstractEventHandler;
import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MJournal;
import org.compiere.model.MOrder;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.idempiere.budget.BudgetUtils;
import org.idempiere.budget.MBudgetConfig;
import org.osgi.service.event.Event;

public class BudgetDocEvent extends AbstractEventHandler{
	private static CLogger log = CLogger.getCLogger(BudgetDocEvent.class);
	private String trxName = "";
	private PO po = null;
	private String m_processMsg = ""; 
	protected Event event;
	protected static MBudgetConfig budgetCONFIGinstance;

	@Override
	protected void initialize() { 
	//register EventTopics and TableNames
		registerTableEvent(IEventTopics.DOC_BEFORE_COMPLETE, MOrder.Table_Name); 
		registerTableEvent(IEventTopics.DOC_BEFORE_COMPLETE, MJournal.Table_Name); 
		budgetCONFIGinstance = null;
		log.info("<<BUDGET>> PLUGIN INITIALIZED");
		}

	/*
	 * @see BUSINESS RULES ON http://wiki.idempiere.org/en/Plugin:_Budgetary_Control#Budget_Configurator 
	 * @see org.adempiere.base.event.AbstractEventHandler#doHandleEvent(org.osgi.service.event.Event)
	 */
	@Override
	protected void doHandleEvent(Event event) {
		String type = event.getTopic();
		this.event = event;
		//testing that it works at login
		setPo(getPO(event));
		setTrxName(po.get_TrxName());
		
		//USING UTILS FOR REUSE BY ADEMPIERE 361 MODELVALIDATOR
		BudgetUtils utils = new BudgetUtils(po);
		if (budgetCONFIGinstance == null) {
			log.info("<<BUDGET>> RULES ONE-TIME SETTING STARTED");
			String error = utils.oneTimeSetupRevenue();
			if (error!=null)
				handleError(error);
			else
				log.info("<<BUDGET>> RULES ONE-TIME SETTING SUCCESSFUL");
			}
 
		//ORDER DOCUMENT VALIDATION BEFORE COMPLETE
		if (po instanceof MOrder && IEventTopics.DOC_BEFORE_COMPLETE == type){ 
			log.info(" topic="+event.getTopic()+" po="+po);
			//SET VARIABLES FOR MATCHED BUDGETLINE PERCENT OR AMOUNT
			String error = utils.checkPurchaseBudget();			
			if (error != null)
				handleError(error);
			}
			
		//JOURNAL DOCUMENT VALIDATION BEFORE COMPLETE
		//BUDGET CONTROL OVER ACCOUNTING ELEMENT TO EITHER PERCENT OR AMOUNT
		//ACCESS GL BUDGET LINES FOR MATCHING TO JOURNAL-LINES CRITERIA
		else if (po instanceof MJournal && IEventTopics.DOC_BEFORE_COMPLETE == type){
			log.info(" topic="+event.getTopic()+" po="+po);
			//SET VARIABLES FOR MATCHED BUDGETLINE PERCENT OR AMOUNT
			String error = utils.checkAccountsBudget();
			if (error != null)
				handleError(error);
		}
	}

	/*
	 * ALLOW FOR OPTION TO CONTINUE OPERATIONS
	 */
	private void handleError(String error) {
		if (budgetCONFIGinstance.isValid())
			throw new AdempiereException(error);
		else
			log.warning(error);
	}

	private void setPo(PO eventPO) {
		 po = eventPO;
	}

	private void setTrxName(String get_TrxName) {
		trxName = get_TrxName;		
	}
}
