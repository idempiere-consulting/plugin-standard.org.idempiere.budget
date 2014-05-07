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

import java.math.BigDecimal;

import org.adempiere.base.event.AbstractEventHandler;
import org.adempiere.base.event.IEventTopics;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MInvoice;
import org.compiere.model.MJournal;
import org.compiere.model.MMessage;
import org.compiere.model.MNote;
import org.compiere.model.MOrder;
import org.compiere.model.MPayment;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.idempiere.budget.BudgetUtils; 
import org.osgi.service.event.Event;

public class BudgetDocEvent extends AbstractEventHandler{
	private static CLogger log = CLogger.getCLogger(BudgetDocEvent.class);
	private String trxName = "";
	private PO po = null;
	private String m_processMsg = ""; 
	private Event event;
	private boolean isSOTrx;
	@Override
	protected void initialize() { 
	//register EventTopics and TableNames
		registerTableEvent(IEventTopics.DOC_BEFORE_COMPLETE, MOrder.Table_Name); 
		registerTableEvent(IEventTopics.DOC_BEFORE_COMPLETE, MJournal.Table_Name); 
		registerTableEvent(IEventTopics.DOC_BEFORE_COMPLETE, MInvoice.Table_Name); 
		registerTableEvent(IEventTopics.DOC_BEFORE_COMPLETE, MPayment.Table_Name);  
		BudgetUtils.budgetCONFIGinstance = null;
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
		if (BudgetUtils.budgetCONFIGinstance == null) {
			log.info("<<BUDGET>> RULES ONE-TIME SETTING STARTED");
			BudgetUtils.initBudgetConfig(po);
			BudgetUtils.setupCalendar(po);
			BudgetUtils.clearWhereMatches();
			BigDecimal revenueEstimate = BudgetUtils.revenueEstimate();
			BudgetUtils.RevenueEstimate = BudgetUtils.budgetTrend(null, revenueEstimate);//OBTAIN REVENUE 4XXX AMOUNT

			log.info("<<BUDGET>> RULES ONE-TIME SETTING SUCCESSFUL");
			}
 
		//ORDER DOCUMENT VALIDATION BEFORE COMPLETE
		if ((po instanceof MOrder || po instanceof MInvoice || po instanceof MPayment) && IEventTopics.DOC_BEFORE_COMPLETE == type){ 
			log.info(" topic="+event.getTopic()+" po="+po);
			//SET VARIABLES FOR MATCHED BUDGETLINE PERCENT OR AMOUNT
			String error = BudgetUtils.eventPurchasesSales(po);			
			if (error != null)
				handleException(error, po);
			}
			
		//JOURNAL DOCUMENT VALIDATION BEFORE COMPLETE
		//BUDGET CONTROL OVER ACCOUNTING ELEMENT TO EITHER PERCENT OR AMOUNT
		//ACCESS GL BUDGET LINES FOR MATCHING TO JOURNAL-LINES CRITERIA
		else if (po instanceof MJournal && IEventTopics.DOC_BEFORE_COMPLETE == type){
			log.info(" topic="+event.getTopic()+" po="+po);
			//SET VARIABLES FOR MATCHED BUDGETLINE PERCENT OR AMOUNT
			String error = BudgetUtils.eventGLJournal(po);
			if (error != null)
				handleException(error, po);
		}
	}

	/**
	 * ALLOW FOR OPTION TO CONTINUE OPERATIONS BUT NOTICE WILL BE ISSUED 
	 * SALES PERFORMANCE TARGET CHECK WITH ISSOTRX
	 * @param notice
	 */
	private void handleException(String notice, PO po) {		
		//DIFFERENTIATE BETWEEN PURCHASING BUDGET EXCESS AND SALES TARGET PERFORMANCE MEASURE
		isSOTrx=false;
		if (po instanceof MPayment){
			MPayment payment = new MPayment(po.getCtx(),po.get_ID(),po.get_TrxName());
			if (payment.getC_DocType().isSOTrx())
				isSOTrx = true;
		}else {
			if (po.get_ValueAsBoolean(MOrder.COLUMNNAME_IsSOTrx))
				isSOTrx = true;
		}			
		if (BudgetUtils.budgetCONFIGinstance.isValid() && !isSOTrx)
			throw new AdempiereException(notice);
		else {
			notice = notice + (isSOTrx? " **SALES TARGET ACHIEVED**":" **BUDGET BREACHED**");
			log.warning(notice);
			MMessage msg = MMessage.get(po.getCtx(), "BudgetError");
			MNote note = new MNote(po.getCtx(),
					msg.getAD_Message_ID(),
					po.get_ValueAsInt("CreatedBy"),
					po.get_Table_ID(), po.get_ID(),
					"MODEL EVENT: "+event.getProperty("event.data").toString(),
					notice,
					po.get_TrxName());
			note.setAD_Org_ID(po.getAD_Org_ID());
			note.saveEx();
			log.fine("BUDGET SYSTEM - NOTICE CREATED");
		}
	}
	/**
	 * 
	 * @param eventPO
	 */
	private void setPo(PO eventPO) {
		 po = eventPO;
	}
	/**
	 * 
	 * @param get_TrxName
	 */
	private void setTrxName(String get_TrxName) {
		trxName = get_TrxName;		
	}
}
