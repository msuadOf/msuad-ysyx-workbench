
#ifndef MONITOR_H
#define MONITOR_H

typedef struct monitor_handler
{
    uint32_t snpc;
    uint32_t dnpc;
    uint32_t get_cpu_pc();
}monitor_handler;
extern monitor_handler npc_handler;

void init_monitor(int argc, char *argv[]);
void sdb_mainloop();

#endif // !_MONITOR_H