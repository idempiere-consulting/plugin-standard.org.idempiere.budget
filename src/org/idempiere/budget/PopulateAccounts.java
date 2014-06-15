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

package org.idempiere.budget;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Calendar;

import org.compiere.model.MCalendar;
import org.compiere.model.MFactAcct;
import org.compiere.model.MPeriod;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.Env;

public class PopulateAccounts extends SvrProcess{

	private BigDecimal p_previousYears;
	private BigDecimal p_InitialAmount;
	private BigDecimal p_Increment;
	private BigDecimal p_Account_ID;
	private boolean p_reset;

	@Override
	protected void prepare() {
	      ProcessInfoParameter[] para = getParameter();
	        for (int i = 0; i < para.length; i++) {
	            String name = para[i].getParameterName();
	            if (para[i].getParameter() == null);
	            else if (name.equals("PreviousYears")) {
	                p_previousYears = (BigDecimal) para[i].getParameter();
	            }else if (name.equals("InitialAmount")) {
	                p_InitialAmount = (BigDecimal) para[i].getParameter();
	            }else if (name.equals("Increment")) {
	                p_Increment = (BigDecimal) para[i].getParameter();
	            }else if (name.equals("Account_ID")) {
	                p_Account_ID = (BigDecimal) para[i].getParameter();
	            }else if (name.equals("IsActive")) {
	                p_reset = (Boolean) para[i].getParameter();
	            }   
	        }
		
	}

	@Override
	protected String doIt() throws Exception { 
		MCalendar calendar = MCalendar.getDefault(Env.getCtx());
		//starting year 
		Calendar cal = Calendar.getInstance();
		Timestamp nowTime = new Timestamp(cal.getTimeInMillis());
		cal.add(Calendar.YEAR, -p_previousYears.intValue()); 
		int month = cal.get(Calendar.MONTH);
		cal.add(Calendar.MONTH, -month-1);
		Timestamp date = new Timestamp(cal.getTimeInMillis());
		BigDecimal amount = p_InitialAmount; 
		int cnt = 0; 
		int stop = (p_previousYears.intValue()*12)+month;
		for (int i=0; i<stop; i++){
			cal.add(Calendar.MONTH, 1);
			date = new Timestamp(cal.getTimeInMillis());
			if (date.after(nowTime))
				break;
			MPeriod prd = MPeriod.findByCalendar(Env.getCtx(), date, calendar.getC_Calendar_ID(), get_TrxName());
			int period_ID = prd.getC_Period_ID();
			if (period_ID==0) continue;
			MFactAcct fact = new MFactAcct(Env.getCtx(),0, get_TrxName());
			fact.setDateAcct(date);
			fact.setDateTrx(date); 
			fact.setAccount_ID(p_Account_ID.intValue());
			fact.setC_Period_ID(period_ID);
			fact.setAmtSourceCr(amount);
			fact.setAmtAcctCr(amount);
			fact.setC_AcctSchema_ID(101);
			fact.setAD_Table_ID(407);//CASH JOURNAL
			fact.setRecord_ID(100);
			fact.setPostingType("A");
			fact.setC_Currency_ID(100);
			cnt++;
			if (!p_reset) //if IsActive then save, else its just a test run to reset budgetCONFIGinstance
				fact.saveEx(get_TrxName());
			amount = amount.add(p_Increment);
		}
	       BudgetUtils.budgetCONFIGinstance=null; //backdoor reset for any BudgetConfig change before start normal business		
		return "NO OF RECORDS CREATED: "+cnt;
	}

}
