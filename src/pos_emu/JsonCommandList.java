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
    private String image;
        
    public String GetCommand()
    {
        if (cmd == null)
            cmd = "???";
        
        return cmd;
    }
    public String GetVersion()
    {
        if (version == null)
            version = "X.Y.Z";
        
        return version;
    }
    public String GetMessageToDisplay()
    {
        if (msg == null)
            msg = "--EMPTY MESSAGE--";
        
        return msg;
    }
    public String GetColor()
    {
        if (color == null)
            color = "#808080";
        
        return color;
    }
    public String GetXPos()
    {
        if (x_pos == null)
            x_pos = "0";

        return x_pos;
    }
    public String GetYPos()
    {
        if (y_pos == null)
            y_pos = "0";
        
        return y_pos;
    }    
    public String GetTextSize()
    {
        if (size == null)
            size = "16";
        
        return size;
    }
    public String GetImageName()
    {
        if (image == null)
            image = "/pos_emu/resource/logo.png";
        
        return image;
    }    
}
