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
