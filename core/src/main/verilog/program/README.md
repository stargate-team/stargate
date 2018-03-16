# Installation & Program Guide

## Install Vivado

---

```bash
# 1. Download Vivado (Version: Vivado_HL_Design_Edition_2015.4)

# 2. Change to downloaded Vivado directory
cd Vivado_HL_Design_Edition_2015.4

# 3. Generate vivado configuration file
./xsetup -b ConfigGen 

# 4. Modify configuration file
vim ~/.Xilinx/install_config.txt
# Add the following modules into install_config.txt
Modules=Vivado:1,Vivado High Level Synthesis:0,Software Development Kit:0,DocNav:0

# 5. Install Vivado
./xsetup --agree XilinxEULA,3rdPartyEULA,WebTalkTerms --batch Install --config ~/.Xilinx/install_config.txt
```

## Install Driver

```bash
# 1. Get root privileges
sudo -s

# 2. Go to <Xilinx install>/bin/[lin|lin64] or common/bin/[lin|lin64] ois an installed area, Copy the install_script directory to /opt.
cp -r ~/data/software/Xilinx/Vivado/2015.4/data/xicom/cable_drivers/lin64/ /opt

# 3. Install driver
cd ./install_script/install_drivers/
./install_drivers  
./setup_pcusb
./install_digilent.sh

# 4. Reboot machine
sudo reboot
```

## Program FPGA
```bash
# sh program.sh $cardId $bitstreamPath
sh program.sh 0 bitstream/Loopback.bin
```
