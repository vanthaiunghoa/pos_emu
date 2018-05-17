/*
 * POS_EMU
 * This is a Pos Of Sale emulator 
 * 
 * This is the class for the parameter file : param.json
 */
package pos_emu;

class ParamConfigFile
{
    private String app_name;
    private String protocol_version;
    private String ip_port;
    private String idle_type;
    private String idle_logo;
    private String idle_msg1;
    private String idle_msg2;
    
    public String GetIdleMsg1()
    {
        if (idle_msg1 == null)
            idle_msg1 = "";
        
        return idle_msg1;
    }
    public String GetIdleMsg2()
    {
        if (idle_msg2 == null)
            idle_msg2 = "";
        
        return idle_msg2;
    }
    public String GetLogo()
    {
        if (idle_logo == null)
            idle_logo = "";
        
        return idle_logo;
    }
    public String GetIdleType()
    {
        // idle_type = 0 => TEXT for IDLE SCREEN
        // idle_type = 1 => LOGO for IDLE SCREEN
        if (idle_type == null)
            idle_type = "0";
        
        return idle_type;
    }
    public String GetPosPort()
    {
        // Default port value
        if (ip_port == null)
            ip_port = "8999";
        
        return ip_port;
    }
    public String GetAppName()
    {
        // Default port value
        if (app_name == null)
            app_name = "pos_emu";
        
        return app_name;
    }    
    public String GetProtocolVersion()
    {
        // Default port value
        if (protocol_version == null)
            protocol_version = "0.1.0";
        
        return protocol_version;
    }    
}
