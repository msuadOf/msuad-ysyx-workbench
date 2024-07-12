#include <stdio.h>

//log
FILE* log_fp;

//State
#include "utils.h"
NPCState npc_state = { .state = NPC_STOP };

int is_exit_status_bad() {
  int good = (npc_state.state == NPC_END && npc_state.halt_ret == 0) ||
    (npc_state.state == NPC_QUIT);

    const char* state_info = "state error" ;

    if(npc_state.state == NPC_RUNNING){
        state_info = "NPC_RUNNING" ;
    }
    if(npc_state.state == NPC_STOP){
        state_info = "NPC_STOP" ;
    }
    if(npc_state.state == NPC_END){
        state_info = "NPC_END" ;
    }
    if(npc_state.state == NPC_ABORT){
        state_info = "NPC_ABORT" ;
    }
    if(npc_state.state == NPC_END){
        state_info = "NPC_END" ;
    }
    if(npc_state.state == NPC_QUIT){
        state_info = "NPC_QUIT" ;
    }
    
    Log("return %d [%s,halt_ret=%d]",!good,state_info,npc_state.halt_ret);
  return !good;
}