import React from 'react';
import { motion } from 'framer-motion';
import { MessageSquare, Workflow, Cpu, ChevronRight, Activity } from 'lucide-react';

export default function AgentCard({ id, data }) {
  const isGraph = data.type === 'Reasoning Graph';
  const accentColor = isGraph ? 'text-emerald-400' : 'text-blue-400';
  const progress = isGraph ? Math.min(100, (Object.keys(data.tasks || {}).length / 8) * 100) : Math.min(100, (data.messageCount / 15) * 100);

  return (
    <motion.div 
      whileHover={{ scale: 1.01 }}
      className="glass p-5 rounded-[2rem] group cursor-pointer hover:bg-white/[0.03] transition-all"
    >
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-2xl bg-white/5 border border-white/10">
            {isGraph ? <Workflow size={18} className="text-emerald-400" /> : <MessageSquare size={18} className="text-blue-400" />}
          </div>
          <div>
            <h3 className="font-mono font-bold text-sm text-slate-100">{id}</h3>
            <p className="text-[9px] text-slate-500 uppercase font-black tracking-widest">{data.type}</p>
          </div>
        </div>
        <ChevronRight size={16} className="text-slate-700 group-hover:text-white transition-colors" />
      </div>

      <div className="grid grid-cols-2 gap-4 mb-5 px-1">
        <div className="space-y-1">
          <span className="text-[9px] text-slate-500 uppercase font-bold tracking-tighter">Utilization</span>
          <p className={`text-lg font-black font-mono ${accentColor}`}>{Math.round(progress)}%</p>
        </div>
        <div className="space-y-1 text-right">
          <span className="text-[9px] text-slate-500 uppercase font-bold tracking-tighter">Shards</span>
          <p className="text-lg font-black font-mono text-slate-300">{isGraph ? Object.keys(data.tasks).length : data.messageCount}</p>
        </div>
      </div>

      <div className="h-1 w-full bg-white/5 rounded-full overflow-hidden">
        <motion.div 
          initial={{ width: 0 }} animate={{ width: `${progress}%` }}
          className={`h-full rounded-full ${isGraph ? 'bg-emerald-500 shadow-[0_0_12px_#10b981]' : 'bg-blue-500 shadow-[0_0_12px_#3b82f6]'}`}
        />
      </div>
    </motion.div>
  );
}