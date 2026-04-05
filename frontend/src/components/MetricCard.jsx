import React from 'react';
import { motion } from 'framer-motion';

export default function MetricCard({ title, value, icon: Icon, colorClass }) {
  return (
    <motion.div 
      whileHover={{ y: -2 }}
      className="glass p-6 rounded-3xl relative overflow-hidden group shadow-2xl"
    >
      <div className="flex items-center justify-between mb-4">
        <span className="text-[10px] font-bold text-slate-500 uppercase tracking-[0.2em]">{title}</span>
        <div className={`p-2 rounded-xl bg-white/5 border border-white/5 group-hover:scale-110 transition-transform`}>
          {Icon && <Icon className={`w-4 h-4 ${colorClass}`} />}
        </div>
      </div>
      <div className="flex items-baseline gap-2">
        <span className="text-4xl font-black tracking-tight text-slate-50 font-mono">{value}</span>
        <div className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse shadow-[0_0_10px_#10b981]" />
      </div>
    </motion.div>
  );
}