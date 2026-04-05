import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Zap, Database, Terminal, Send } from 'lucide-react';

export default function AgentInspector({ isOpen, onClose, agentId, data }) {
  const [command, setCommand] = useState('');

  const handleSendCommand = () => {
    // TODOS
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          <motion.div 
            initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
            onClick={onClose}
            className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50"
          />
          
          <motion.div 
            initial={{ x: '100%' }} animate={{ x: 0 }} exit={{ x: '100%' }}
            transition={{ type: 'spring', damping: 25, stiffness: 200 }}
            className="fixed right-0 top-0 h-full w-full max-w-lg bg-[#161b22] border-l border-[#30363d] shadow-2xl z-50 flex flex-col"
          >
            {/* Header */}
            <div className="p-6 border-b border-[#30363d] flex justify-between items-center bg-[#0d1117]">
              <div>
                <h2 className="text-xl font-mono font-bold text-[#58a6ff]">{agentId}</h2>
                <p className="text-[10px] text-gray-500 uppercase tracking-widest">Agent Inspector Session</p>
              </div>
              <button onClick={onClose} className="p-2 hover:bg-[#30363d] rounded-full transition-colors">
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Metrics Ribbon */}
            <div className="grid grid-cols-3 gap-0 border-b border-[#30363d]">
              <div className="p-4 border-r border-[#30363d] text-center">
                <p className="text-[9px] text-gray-500 uppercase mb-1">Latency</p>
                <p className="text-sm font-bold text-yellow-400 flex items-center justify-center gap-1">
                  <Zap className="w-3 h-3" /> 0.12ms
                </p>
              </div>
              <div className="p-4 border-r border-[#30363d] text-center">
                <p className="text-[9px] text-gray-500 uppercase mb-1">RAM</p>
                <p className="text-sm font-bold text-[#58a6ff] flex items-center justify-center gap-1">
                  <Database className="w-3 h-3" /> 1.4KB
                </p>
              </div>
              <div className="p-4 text-center">
                <p className="text-[9px] text-gray-500 uppercase mb-1">TTL</p>
                <p className="text-sm font-bold text-green-500">42s</p>
              </div>
            </div>

            {/* JSON Data Viewer */}
            <div className="flex-1 overflow-y-auto p-6 bg-[#0d1117]/50">
              <h3 className="text-[10px] font-bold text-gray-500 uppercase mb-4 flex items-center gap-2">
                <Terminal className="w-3 h-3" /> Raw State Map
              </h3>
              <div className="rounded-lg bg-[#0d1117] border border-[#30363d] p-4 font-mono text-xs leading-relaxed text-gray-300">
                <pre>{JSON.stringify(data, null, 2)}</pre>
              </div>
            </div>

            {/* Mini Console Input */}
            <div className="p-6 border-t border-[#30363d] bg-[#161b22]">
              <div className="relative">
                <input 
                  type="text"
                  value={command}
                  onChange={(e) => setCommand(e.target.value)}
                  placeholder="Type command (e.g. PUSH message...)"
                  className="w-full bg-[#0d1117] border border-[#30363d] rounded-lg py-3 px-4 text-sm font-mono focus:outline-none focus:border-[#58a6ff] transition-all"
                />
                <button 
                  onClick={handleSendCommand}
                  className="absolute right-2 top-2 p-1.5 bg-[#58a6ff] text-dark rounded-md hover:bg-[#79c0ff] transition-colors"
                >
                  <Send className="w-4 h-4" />
                </button>
              </div>
              <p className="mt-3 text-[10px] text-gray-600 italic text-center">
                Commands sent here bypass the TCP layer and interact directly with the Core Engine.
              </p>
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
}