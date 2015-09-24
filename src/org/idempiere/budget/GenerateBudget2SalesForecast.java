package org.idempiere.budget;

import java.util.List;

import org.compiere.model.MCalendar;
import org.compiere.model.MForecast;
import org.compiere.model.MForecastLine;
import org.compiere.model.MPeriod;
import org.compiere.model.Query;
import org.compiere.process.SvrProcess;
import org.compiere.util.Env; 

/**
 * Generate Purchase Orders in bulk (refer another class that handles InfoWindow process T_selection records to generate)
 * @author red1
 *
 */
public class GenerateBudget2SalesForecast extends SvrProcess{

	@Override
	protected void prepare() { 
	}

	@Override
	protected String doIt() throws Exception {
		String whereClause = "EXISTS (SELECT T_Selection_ID FROM T_Selection WHERE  T_Selection.AD_PInstance_ID=? " +
				"AND T_Selection.T_Selection_ID=B_BudgetPlanLine.B_BudgetPlanLine_ID)";		
		

		List<MBudgetPlanLine> lines = new Query(Env.getCtx(),MBudgetPlanLine.Table_Name,whereClause,get_TrxName())
		.setClient_ID()
		.setParameters(new Object[]{getAD_PInstance_ID()	})
		.list(); 
		if (lines.isEmpty() || lines==null)
			return "no lines";
		
		MCalendar calendar = MCalendar.getDefault(Env.getCtx());
		//create Sales Forecast header with each line included
		MForecast header = new MForecast(Env.getCtx(), 0, null); 
		header.setName("PO Forecast from SalesForecast"); 
		header.setC_Calendar_ID(calendar.getC_Calendar_ID());
		MPeriod period = MPeriod.findByCalendar(Env.getCtx(), header.getCreated(), calendar.getC_Calendar_ID(), get_TrxName());
		header.setC_Year_ID(period.getC_Year_ID());
		header.saveEx(get_TrxName());
		
		for (MBudgetPlanLine line:lines){
			MForecastLine forecastline = new MForecastLine(Env.getCtx(),0,get_TrxName());
			forecastline.setM_Forecast_ID(header.getM_Forecast_ID());
			int cnt = 0;
			//DatePromised remains same throughout even till PO. only lead time (order date) is changed (see further below).
			forecastline.setDatePromised(null);		
			//check qty not to be zero
 			//set Qty with Reserved/Ordered from future orders by DatePromised
			forecastline.setQty(line.getQty()); 
			
			//set product and save
			forecastline.setM_Product_ID(line.getM_Product_ID());
			forecastline.setC_Period_ID(line.getC_Period_ID());
			forecastline.setM_Warehouse_ID(0);
			forecastline.saveEx(get_TrxName());
		}

		
		return "@DocumentNo@ ";
	}

}
