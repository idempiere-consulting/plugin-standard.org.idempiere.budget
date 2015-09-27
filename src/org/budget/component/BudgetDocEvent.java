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

package org.budget.component;

import java.lang.reflect.Method;

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
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.idempiere.budget.BudgetUtils;
import org.idempiere.budget.MBudgetPlanLine;
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
		registerTableEvent(IEventTopics.DOC_BEFORE_PREPARE, MOrder.Table_Name); 
		registerTableEvent(IEventTopics.DOC_BEFORE_COMPLETE, MJournal.Table_Name); 
		registerTableEvent(IEventTopics.DOC_BEFORE_PREPARE, MInvoice.Table_Name); 
		registerTableEvent(IEventTopics.DOC_BEFORE_PREPARE, MPayment.Table_Name);  
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
		BudgetUtils bg = new BudgetUtils();		
		//USING UTILS FOR REUSE BY ADEMPIERE 361 MODELVALIDATOR
		MBudgetPlanLine budgetPlanLine = new Query(po.getCtx(), MBudgetPlanLine.Table_Name, "", po.get_TrxName()).setOnlyActiveRecords(true).setClient_ID().first(); 
		if (budgetPlanLine!=null) {
			if (BudgetUtils.budgetCONFIGinstance == null) {
				log.info("<<BUDGET>> RULES ONE-TIME SETTING STARTED");
				bg.initBudgetConfig(po);
				bg.setupCalendar(po); 	
				bg.revenueFlag=true;
				BudgetUtils.RevenueEstimate = bg.revenueEstimate();
				BudgetUtils.RevenueEstimate = bg.budgetTrend(null, BudgetUtils.RevenueEstimate, BudgetUtils.revenueKey);//OBTAIN REVENUE 4XXX AMOUNT
				bg.revenueFlag=false;
				log.info("<<BUDGET>> RULES ONE-TIME SETTING SUCCESSFUL");
			}
 
			//ORDER DOCUMENT VALIDATION BEFORE COMPLETE
			if ((po instanceof MOrder || po instanceof MInvoice || po instanceof MPayment) && IEventTopics.DOC_BEFORE_PREPARE == type){ 
				log.info(" topic="+event.getTopic()+" po="+po);
				//SET VARIABLES FOR MATCHED BUDGETLINE PERCENT OR AMOUNT
				String response = bg.eventPurchasesSales(po);			
				if (response != null)
					handleResponse(response, po);
				}
			
			//JOURNAL DOCUMENT VALIDATION BEFORE COMPLETE
			//BUDGET CONTROL OVER ACCOUNTING ELEMENT TO EITHER PERCENT OR AMOUNT
			//ACCESS GL BUDGET LINES FOR MATCHING TO JOURNAL-LINES CRITERIA
			else if (po instanceof MJournal && IEventTopics.DOC_BEFORE_COMPLETE == type 
					&& po.get_Value(MJournal.COLUMNNAME_PostingType).equals(MJournal.POSTINGTYPE_Actual)){
				log.info(" topic="+event.getTopic()+" po="+po);
				//SET VARIABLES FOR MATCHED BUDGETLINE PERCENT OR AMOUNT
				String response = bg.eventGLJournal((MJournal)po);
				if (response != null)
					handleResponse(response, po);
			}
		}
	}

	/**
	 * ALLOW FOR OPTION TO CONTINUE OPERATIONS BUT NOTICE WILL BE ISSUED 
	 * SALES PERFORMANCE TARGET CHECK WITH ISSOTRX
	 * @param notice
	 */
	private void handleResponse(String notice, PO po) {		
		//differentiate between purchasing budget excess and sales target performance measure
		isSOTrx=false;
		if (po instanceof MPayment){
			MPayment payment = new MPayment(po.getCtx(),po.get_ID(),po.get_TrxName());
			if (payment.getC_DocType().isSOTrx())
				isSOTrx = true;
		}else {
			if (!(po instanceof MJournal) && po.get_ValueAsBoolean(MOrder.COLUMNNAME_IsSOTrx))
				isSOTrx = true;
		}			
		// make it work with ZK webui -- Logilite
		if (BudgetUtils.budgetCONFIGinstance.isValid()) {
			try {
				ClassLoader loader = Thread.currentThread()
						.getContextClassLoader();
				if (loader == null)
					loader = BudgetDocEvent.class.getClassLoader();
				Class<?> clazz = loader
						.loadClass("org.adempiere.webui.window.FDialog");
				Method m = clazz.getMethod("warn", Integer.TYPE, String.class);
				m.invoke(null, 0, notice);
				if(!isSOTrx && notice.contains("Short"))
					throw new AdempiereException("Could not complete document, it exceeds defined Budget!");
			} catch (Exception e) {
				throw new AdempiereException(e);
			}
			//throw new AdempiereException(notice);
		}
		if(isSOTrx || !BudgetUtils.budgetCONFIGinstance.isValid()){
			log.warning(notice);
			MMessage msg = MMessage.get(po.getCtx(), "BudgetEvent");
			MNote note = new MNote(po.getCtx(),
				msg.getAD_Message_ID(),
				po.get_ValueAsInt("CreatedBy"),
				po.get_Table_ID(), po.get_ID(),
				po.get_ValueAsString(MOrder.COLUMNNAME_DocumentNo), //reference
				notice, //text message
				po.get_TrxName());
			note.setAD_Org_ID(po.getAD_Org_ID());
			note.saveEx(po.get_TrxName());
			log.fine("budget system - notice created");
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
