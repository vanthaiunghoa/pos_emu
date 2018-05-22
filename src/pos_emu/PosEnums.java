/*
 * PosEmuEngine
 * This is the engine of POS_EMU simulator
 * 
 */
package pos_emu;

class PosEnums
{
    public enum PosKeyCode {
        NO_KEY,
        NUM_0,
        NUM_1,
        NUM_2,
        NUM_3,
        NUM_4,
        NUM_5,
        NUM_6,
        NUM_7,
        NUM_8,
        NUM_9,
        NUM_VAL,
        NUM_CORR,
        NUM_CANCEL,
        NUM_MENU,
        NUM_POINT,
        NUM_F
    }
    
    public enum State
    {
        STATE_NOT_STARTED,
        STATE_IDLE,
        STATE_MENU_SCREEN,
        STATE_AMOUNT,
        STATE_CARD_WAITING,
        STATE_TRANSACTION
    }
    
    public enum PosEvent
    {
        NO_EVENT,
        KEY_PRESSED,
        TOUCHSCREEN,
        COMM_MESSAGE,
        ICC_INSERTED,
        CARD_SWIPED,
        CLESS_CARD
    }
}