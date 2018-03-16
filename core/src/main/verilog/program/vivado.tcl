set hard_device_id [lindex $argv 0]
set bit_file_path [lindex $argv 1]

set hard_device "*/xilinx_tcf/Digilent/"
append hard_device $hard_device_id

open_hw
connect_hw_server -url localhost:3121
current_hw_target [get_hw_targets $hard_device]
set_property PARAM.FREQUENCY 15000000 [get_hw_targets $hard_device]
open_hw_target
set_property PROGRAM.FILE "$bit_file_path" [lindex [get_hw_devices] 0]
current_hw_device [lindex [get_hw_devices] 0]
refresh_hw_device -update_hw_probes false [lindex [get_hw_devices] 0]
set_property PROBES.FILE {} [lindex [get_hw_devices] 0]
set_property PROGRAM.FILE "$bit_file_path" [lindex [get_hw_devices] 0]
program_hw_devices [lindex [get_hw_devices] 0]
refresh_hw_device [lindex [get_hw_devices] 0]
close_hw
exit
