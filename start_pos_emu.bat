mkdir dist
copy param.json dist
mkdir dist\src\pos_emu\config
copy src\pos_emu\config\param_pos_emu.json dist\src\pos_emu\config
cd dist
java -jar pos_emu.jar
