/*
 * POS_EMU
 * This is a Pos Of Sale emulator 
 * TCPServer : TCP/IP listener
 */
package pos_emu;

class JsonCommandList
{
    private String cmd;
    private String version;
    private String msg;
    private String color;
    private String x_pos;
    private String y_pos;
    private String size;
        
    public String GetCommand()
    {
        return cmd;
    }
    public String GetVersion()
    {
        return version;
    }
    public String GetMessageToDisplay()
    {
        return msg;
    }
    public String GetColor()
    {
        return color;
    }
    public String GetXPos()
    {
        return x_pos;
    }
    public String GetYPos()
    {
        return y_pos;
    }    
    public String GetTextSize()
    {
        return size;
    }
}
