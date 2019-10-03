package org.idempiere.budget;

import java.io.File;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MDocType;
import org.compiere.model.MFactAcct;
import org.compiere.model.MTable;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.process.DocumentEngine;
import org.compiere.util.DB;

public class MBudgetPeriod extends X_B_BudgetPeriod implements DocAction {

	private static final long serialVersionUID = 1L;

	/** Process Message */
	private String m_processMsg = null;
	/** Just Prepared Flag */
	private boolean m_justPrepared = false;

	public MBudgetPeriod(Properties ctx, int B_BudgetPeriod_ID, String trxName) {
		super(ctx, B_BudgetPeriod_ID, trxName);
	}

	public MBudgetPeriod(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

	public MBudgetPeriod(Properties ctx, int B_BudgetPeriod_ID, int B_BudgetYear_ID, String trxName) {
		super(ctx, B_BudgetPeriod_ID, trxName);
		setB_BudgetYear_ID(B_BudgetYear_ID);
	}

	/**
	 * Get Lines of BudgetPeriod
	 * 
	 * @param requery
	 *            refresh from db
	 * @return Budget lines
	 */
	public MBudgetLine[] getLines() {

		List<MBudgetLine> list = new Query(getCtx(), I_B_BudgetLine.Table_Name, "B_BudgetPeriod_ID=?", get_TrxName())
				.setParameters(getB_BudgetPeriod_ID()).setClient_ID().setOrderBy(MBudgetLine.COLUMNNAME_B_BudgetLine_ID).list();
		//
		MBudgetLine[] m_lines = new MBudgetLine[list.size()];
		list.toArray(m_lines);
		return m_lines;
	} // getBudgetPeriodLines
	
	public static void deleteLines(MBudgetPeriod budgetPeriod){
		final String sql = "DELETE B_BudgetLine WHERE B_BudgetPeriod_ID=? AND AD_Client_ID=?";
		DB.executeUpdateEx(sql, new Object[]{budgetPeriod.getB_BudgetPeriod_ID(), budgetPeriod.getAD_Client_ID()},budgetPeriod.get_TrxName());
	}

	/**************************************************************************
	 * Process document
	 * 
	 * @param processAction
	 *            document action
	 * @return true if performed
	 */
	public boolean processIt(String processAction) {
		m_processMsg = null;
		DocumentEngine engine = new DocumentEngine(this, getDocStatus());
		return engine.processIt(processAction, getDocAction());
	} // processIt

	/**
	 * Unlock Document.
	 * 
	 * @return true if success
	 */
	public boolean unlockIt() {
		log.info("unlockIt - " + toString());
		setProcessing(false);
		return true;
	} // unlockIt

	/**
	 * Invalidate Document
	 * 
	 * @return true if success
	 */
	public boolean invalidateIt() {
		log.info(toString());
		setDocAction(DOCACTION_Prepare);
		return true;
	} // invalidateIt

	/**************************************************************************
	 * Prepare Document
	 * 
	 * @return new status (In Progress or Invalid)
	 */
	public String prepareIt() {
		log.info(toString());
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_PREPARE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;

		// Lines
		MBudgetLine[] lines = getLines();
		if (lines.length == 0) {
			m_processMsg = "@NoLines@";
			return DocAction.STATUS_Invalid;
		}
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_PREPARE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;

		m_justPrepared = true;
		// if (!DOCACTION_Complete.equals(getDocAction())) don't set for just
		// prepare
		// setDocAction(DOCACTION_Complete);
		return DocAction.STATUS_InProgress;
	}

	@Override
	public boolean approveIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean rejectIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String completeIt() {
		// Just prepare
		if (DOCACTION_Prepare.equals(getDocAction())) {
			setProcessed(false);
			return DocAction.STATUS_InProgress;
		}
		// Re-Check
		if (!m_justPrepared) {
			String status = prepareIt();
			if (!DocAction.STATUS_InProgress.equals(status))
				return status;
		}
		setProcessed(true);
		setDocAction(DOCACTION_Close);
		return DocAction.STATUS_Completed;
	}

	@Override
	public boolean voidIt() {
		log.info(toString());
		
		// Before Void
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_VOID);
		if (m_processMsg != null)
			return false;

		BigDecimal zero = new BigDecimal(0);
		if (DOCSTATUS_Completed.equals(getDocStatus())) {
			setDocumentNo(getDocumentNo() + "|** Voided");
			MBudgetLine lines[] = getLines();
			String qtyOrAmt = null;
			for(int i=0; i <lines.length;i++){
				if(lines[i].getQty().compareTo(zero)!=0){
					qtyOrAmt="Quantity : "+lines[i].getQty();
				}
				else{
					qtyOrAmt="Amount : "+lines[i].getAmount();
				}
				lines[i].setDocumentNo(lines[i].getDocumentNo()+"|** Voided ("+qtyOrAmt+")");
				lines[i].setQty(zero);
				lines[i].setAmount(zero);
				lines[i].saveEx();
			}
			MFactAcct.deleteEx(MTable.getTable_ID(I_B_BudgetPeriod.Table_Name), getB_BudgetPeriod_ID(), get_TrxName());
			setDocStatus(DOCSTATUS_Voided);
			setPosted(false);
			setProcessed(true);
			setDocAction(DOCACTION_None);
		}
		
		// After Void
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_VOID);
		if (m_processMsg != null)
			return false;
		
		return true;
	}

	/**
	 * 	Close Document.
	 * 	@return true if success
	 */
	public boolean closeIt()
	{
		log.info(toString());
		// Before Close
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_CLOSE);
		if (m_processMsg != null)
			return false;

		setProcessed(true);
		setDocAction(DOCACTION_None);

		// After Close
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_CLOSE);
		if (m_processMsg != null)
			return false;
		return true;
	}	//	closeIt

	/**
	 * Reverse Correction - same void
	 * 
	 * @return true if success
	 */
	public boolean reverseCorrectIt() {
		log.info(toString());
		// Before reverseCorrect
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_REVERSECORRECT);
		if (m_processMsg != null)
			return false;

		// After reverseCorrect
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_REVERSECORRECT);
		if (m_processMsg != null)
			return false;

		return voidIt();
	} // reverseCorrectionIt

	/**
	 * Reverse Accrual - none
	 * 
	 * @return false
	 */
	public boolean reverseAccrualIt() {
		log.info(toString());
		// Before reverseAccrual
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_REVERSEACCRUAL);
		if (m_processMsg != null)
			return false;

		// After reverseAccrual
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_REVERSEACCRUAL);
		if (m_processMsg != null)
			return false;

		return false;
	} // reverseAccrualIt

	@Override
	public boolean reActivateIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getSummary() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Get Document Info
	 * 
	 * @return document info (untranslated)
	 */
	public String getDocumentInfo() {
		MDocType dt = MDocType.get(getCtx(), getC_DocType_ID());
		return dt.getName() + " " + getDocumentNo();
	} // getDocumentInfo

	@Override
	public File createPDF() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Get Process Message
	 * 
	 * @return clear text error message
	 */
	public String getProcessMsg() {
		return m_processMsg;
	} // getProcessMsg

	@Override
	public int getDoc_User_ID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getC_Currency_ID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BigDecimal getApprovalAmt() {
		// TODO Auto-generated method stub
		return null;
	}
}
