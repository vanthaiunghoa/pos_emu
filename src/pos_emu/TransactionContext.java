/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pos_emu;

/**
 *
 * @author alacahan
 */
public class TransactionContext {

    String m_trxPan;
    String m_trxAid;
    String m_trxAmount;
    boolean m_trxStatus;
    
    TransactionContext() {
        m_trxPan = "---pan---";
        m_trxAid = "---aid---";
        m_trxAmount = "---amount---";
        m_trxStatus = true;
    }    
    
    public void SetPan(String pp) {
        m_trxPan = pp;
    }
    public String GetPan() {
        return m_trxPan;
    }

    public void SetAid(String aa) {
        m_trxAid = aa;
    }
    public String GetAid() {
        return m_trxAid;
    }

    public void SetAmount(String pp) {
        m_trxAmount = pp;
    }
    public String GetAmount() {
        return m_trxAmount;
    }

    public void SetTrxStatus(boolean status) {
        m_trxStatus = status; 
    }
    public boolean GetTrxStatus() {
        return m_trxStatus;
    }
}
