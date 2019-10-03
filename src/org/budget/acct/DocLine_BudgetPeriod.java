package org.budget.acct;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.compiere.acct.Doc;
import org.compiere.acct.DocLine;
import org.compiere.model.MAccount;
import org.compiere.util.Env;
import org.idempiere.budget.MBudgetLine;

public class DocLine_BudgetPeriod extends DocLine {
	
	/*private int 		m_C_BPartner_ID;
	private Timestamp 		m_DateAcct;
	private int 		m_C_Period_ID;
	private MAccount 		m_account;
	private BigDecimal 		m_amount;
	private BigDecimal 		m_qty;*/

	public DocLine_BudgetPeriod(MBudgetLine line, Doc doc) {
		super(line, doc);
		setC_BPartner_ID(MAccount.get(Env.getCtx(), line.getC_ValidCombination_ID()).getC_BPartner_ID());
		setDateAcct(line.getDateAcct());
		setC_Period_ID(line.getB_BudgetPeriod().getC_Period_ID());
		setAccount(MAccount.get(Env.getCtx(), line.getC_ValidCombination_ID()));
		setAmount(line.getAmount());
		setQty(line.getQty(), false);
	}

	/**
	 * @return Returns the c_Order_ID.
	 *//*
	public int getC_BPartner_ID ()
	{
		return m_C_BPartner_ID;
	}
	*//**
	 * @return Returns the c_Order_ID.
	 *//*
	public Timestamp getDateAcct ()
	{
		return m_DateAcct;
	}
	*//**
	 * @return Returns the c_Order_ID.
	 *//*
	public int getC_Period_ID ()
	{
		return m_C_Period_ID;
	}
	*//**
	 * @return Returns the c_Order_ID.
	 *//*
	public MAccount getAccount ()
	{
		return m_account;
	}
	*//**
	 * @return Returns the c_Order_ID.
	 *//*
	public BigDecimal getAmount ()
	{
		return m_amount;
	}
	*//**
	 * @return Returns the c_Order_ID.
	 *//*
	public BigDecimal getQty ()
	{
		return m_qty;
	} */
}
