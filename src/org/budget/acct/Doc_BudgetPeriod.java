package org.budget.acct;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Level;

import org.compiere.acct.Doc;
import org.compiere.acct.DocLine;
import org.compiere.acct.Fact;
import org.compiere.acct.FactLine;
import org.compiere.model.MAccount;
import org.compiere.model.MAcctSchema;
import org.compiere.util.Env;
import org.idempiere.budget.MBudgetLine;
import org.idempiere.budget.MBudgetPeriod;

public class Doc_BudgetPeriod extends Doc {

	public Doc_BudgetPeriod(MAcctSchema ass, ResultSet rs, String trxName) {
		super(ass, MBudgetPeriod.class, rs, null, trxName);
	}

	/**
	 * Load Document Details
	 * 
	 * @return error message or null
	 */
	protected String loadDocumentDetails() {

		MBudgetPeriod budgetPeriod = (MBudgetPeriod) getPO();
		setDateDoc(budgetPeriod.getDateAcct());
		p_lines = loadLines(budgetPeriod);
		log.fine("Lines=" + p_lines.length);
		return null;
	} // loadDocumentDetails

	/**
	 * @param budgetPeriod
	 * @return
	 */
	private DocLine[] loadLines(MBudgetPeriod budgetPeriod) {

		ArrayList<DocLine> list = new ArrayList<DocLine>();
		MBudgetLine[] lines = budgetPeriod.getLines();
		for (int i = 0; i < lines.length; i++) {
			MBudgetLine line = lines[i];
			DocLine_BudgetPeriod docLine = new DocLine_BudgetPeriod(line, this);
			/*docLine.setC_BPartner_ID(MAccount.get(getCtx(), line.getC_ValidCombination_ID()).getC_BPartner_ID());
			docLine.setDateAcct(line.getDateAcct());
			docLine.setC_Period_ID(budgetPeriod.getC_Period_ID());
			docLine.setAccount(MAccount.get(getCtx(), line.getC_ValidCombination_ID()));
			docLine.setAmount(line.getAmount());
			docLine.setQty(line.getQty(), false);*/
			log.fine(docLine.toString());
			list.add(docLine);
		}

		// Return Array
		DocLine[] dls = new DocLine[list.size()];
		list.toArray(dls);
		return dls;
	} // loadLines

	@Override
	public BigDecimal getBalance() {
		BigDecimal retValue = Env.ZERO;
		return retValue;
	}

	@Override
	public ArrayList<Fact> createFacts(MAcctSchema as) {

		ArrayList<Fact> facts = new ArrayList<Fact>();
		// create Fact Header
		Fact fact = new Fact(this, as, Fact.POST_Budget);
		setC_Currency_ID(as.getC_Currency_ID());

		// Line pointers
		FactLine dr = null;
		FactLine cr = null;

		for (int i = 0; i < p_lines.length; i++) {
			DocLine_BudgetPeriod line = (DocLine_BudgetPeriod) p_lines[i];
			
			// expense DR
			dr = fact.createLine(line, line.getAccount(), getC_Currency_ID(), line.getAmtSource(), null);
			if (dr == null) {
				p_Error = "FactLine DR not created: " + line;
				log.log(Level.WARNING, p_Error);
				return null;
			}

			// Suspense CR
			MAccount suspenseAcct = as.getSuspenseBalancing_Acct();
			cr = fact.createLine(line, suspenseAcct, getC_Currency_ID(), null, line.getAmtSource());

			if (cr == null) {
				p_Error = "FactLine CR not created: " + line;
				log.log(Level.WARNING, p_Error);
				return null;
			}
		}
		facts.add(fact);
		return facts;
	} // createFacts

}
