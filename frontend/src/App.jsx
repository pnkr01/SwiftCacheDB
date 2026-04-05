import React, { useState, useEffect, useRef } from 'react';
import { useSystemState } from './hooks/useSystemState';
import MetricCard from './components/MetricCard';
import AgentCard from './components/AgentCard';
import AgentInspector from './components/AgentInspector';
import { AreaChart, Area, ResponsiveContainer, YAxis, Tooltip, XAxis } from 'recharts';
import { 
  Zap, Search, LayoutGrid, Database, Activity, 
  Cpu, Server, ShieldCheck, Terminal, Layers, Settings, ChevronRight, Command
} from 'lucide-react';

function App() {
  const { systemData, error } = useSystemState();
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedAgent, setSelectedAgent] = useState(null);
  const [history, setHistory] = useState([]);

  const lastCount = useRef(0);
  const lastTime = useRef(Date.now());

  useEffect(() => {
    if (!systemData.metrics) return;
    const currentTotal = systemData.metrics.totalCommands || 0;
    const now = Date.now();
    const ops = ((currentTotal - lastCount.current) / ((now - lastTime.current) / 1000)).toFixed(2);
    setHistory(prev => [...prev, { time: new Date().toLocaleTimeString().split(' ')[0], val: parseFloat(ops) }].slice(-40));
    lastCount.current = currentTotal; lastTime.current = now;
  }, [systemData]);

  const metrics = systemData.metrics || { cpuUsage: '0%', usedHeap: '0MB', threads: '0' };
  const filtered = Object.entries(systemData.memorySnapshot || {}).filter(([id]) => id.toLowerCase().includes(searchTerm.toLowerCase()));

  return (
    <div className="flex h-screen w-screen overflow-hidden selection:bg-blue-500/30">
      
      {/* 1. STUDIO SIDEBAR */}
      <aside className="w-20 xl:w-64 h-full glass border-r border-white/5 flex flex-col items-center xl:items-stretch py-8 z-50">
        <div className="flex items-center gap-3 px-8 mb-12">
          <div className="bg-blue-600 p-2.5 rounded-2xl shadow-[0_0_25px_rgba(37,99,235,0.4)]">
            <Zap className="w-5 h-5 text-white fill-white" />
          </div>
          <h1 className="text-xl font-black tracking-tighter text-white hidden xl:block">SWIFT<span className="text-blue-500">CACHE</span></h1>
        </div>

        <nav className="flex-1 px-4 space-y-2">
          {[{ icon: LayoutGrid, label: 'Dashboard' }, { icon: Activity, label: 'Analytics' }, { icon: Database, label: 'Storage' }, { icon: Settings, label: 'Settings' }].map((item, i) => (
            <div key={i} className={`flex items-center gap-4 px-4 py-3 rounded-2xl transition-all cursor-pointer ${i === 0 ? 'bg-white/5 text-blue-400 border border-white/5' : 'text-slate-500 hover:text-slate-200'}`}>
              <item.icon size={20} />
              <span className="text-sm font-bold hidden xl:block">{item.label}</span>
            </div>
          ))}
        </nav>

        <div className="px-8 mt-auto">
           <div className="p-4 rounded-2xl bg-emerald-500/5 border border-emerald-500/10 hidden xl:block text-center">
              <p className="text-[9px] font-black text-emerald-500 uppercase tracking-widest">System Online</p>
           </div>
        </div>
      </aside>
      <main className="flex-1 h-full overflow-y-auto px-10 py-10 relative scroll-smooth">
        <div className="flex items-center justify-between mb-12 border-b border-white/5 pb-8">
           <div>
              <h2 className="text-sm font-bold text-slate-500 uppercase tracking-[0.2em] mb-1">Infrastructure</h2>
              <p className="text-2xl font-black text-white italic tracking-tighter">Cluster Control</p>
           </div>
           
           <div className="flex items-center gap-8">
              <div className="hidden lg:flex gap-10 font-mono text-[10px] text-slate-500 border-x border-white/5 px-10">
                 <div className="flex flex-col"><span>CPU_USAGE</span><span className="text-emerald-400 font-black">{metrics.cpuUsage}</span></div>
                 <div className="flex flex-col"><span>HEAP_MEM</span><span className="text-blue-400 font-black">{metrics.usedHeap}</span></div>
                 <div className="flex flex-col"><span>THREADS</span><span className="text-purple-400 font-black">{metrics.threads}</span></div>
              </div>
              <div className={`flex items-center gap-3 px-5 py-2.5 rounded-2xl text-[10px] font-black border tracking-widest ${error ? 'border-red-500/20 text-red-500 bg-red-500/5' : 'border-emerald-500/20 text-emerald-500 bg-emerald-500/5'}`}>
                 <div className={`w-2 h-2 rounded-full animate-pulse ${error ? 'bg-red-500 shadow-[0_0_10px_red]' : 'bg-emerald-500 shadow-[0_0_10px_#10b981]'}`} />
                 {error ? 'OFFLINE' : 'STABLE'}
              </div>
           </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-4 xl:grid-cols-6 gap-8 mb-16">
          
          <div className="lg:col-span-3 xl:col-span-4 glass p-10 rounded-[2.5rem] relative overflow-hidden group">
             <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-transparent via-blue-500/40 to-transparent" />
             <div className="flex justify-between items-start mb-12 relative z-10">
                <div>
                  <h2 className="text-[10px] font-black text-slate-500 uppercase tracking-[0.4em] mb-2 flex items-center gap-2">
                    <Terminal size={14} className="text-blue-500" /> Throughput Monitoring
                  </h2>
                  <p className="text-5xl font-black text-white italic tracking-tighter">Live Traffic</p>
                </div>
                <div className="p-6 bg-black/30 rounded-3xl border border-white/5 text-right backdrop-blur-md">
                  <span className="text-[10px] text-slate-500 block font-bold uppercase tracking-widest mb-1">Average OPS</span>
                  <span className="text-3xl font-mono text-blue-500 font-black drop-shadow-[0_0_15px_rgba(59,130,246,0.3)]">
                    {history[history.length-1]?.val || 0}
                  </span>
                </div>
             </div>
             <div className="h-64 w-full relative z-10">
                <ResponsiveContainer width="100%" height="100%">
                   <AreaChart data={history}>
                    <defs>
                      <linearGradient id="studioGlow" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stopColor="#3b82f6" stopOpacity={0.4}/>
                        <stop offset="100%" stopColor="#3b82f6" stopOpacity={0}/>
                      </linearGradient>
                    </defs>
                    <XAxis dataKey="time" hide />
                    <YAxis hide domain={['auto', 'auto']} />
                    <Tooltip contentStyle={{ backgroundColor: '#0f172a', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '12px' }} />
                    <Area type="monotone" dataKey="val" stroke="#3b82f6" strokeWidth={4} fill="url(#studioGlow)" isAnimationActive={false} />
                   </AreaChart>
                </ResponsiveContainer>
             </div>
          </div>

          <div className="lg:col-span-1 xl:col-span-2 flex flex-col gap-8">
            <div className="grid grid-cols-2 gap-6">
              <MetricCard title="Agents Active" value={filtered.length} icon={Layers} colorClass="text-blue-500" />
              <MetricCard title="Total Shards" value={systemData.totalKeys} icon={Database} colorClass="text-purple-400" />
            </div>
            
            <div className="flex-1 glass rounded-[2.5rem] p-8 flex flex-col justify-center relative overflow-hidden group shadow-2xl transition-all hover:bg-white/[0.02]">
               <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-transparent via-blue-500/20 to-transparent" />
               <div className="flex items-center gap-2 mb-6">
                  <Command size={14} className="text-blue-500" />
                  <p className="text-[10px] font-black text-slate-500 uppercase tracking-[0.3em]">Query Engine</p>
               </div>
               
               <div className="relative group/search">
                  <Search className="absolute left-4 top-4 text-slate-600 group-focus-within/search:text-blue-500 transition-colors" size={18} />
                  <input 
                    type="text" 
                    placeholder="Search Memory ID..." 
                    className="w-full bg-black/40 border border-white/5 rounded-2xl py-4 pl-12 pr-6 text-sm font-mono focus:outline-none focus:border-blue-500/30 transition-all placeholder:text-slate-700 shadow-inner"
                    value={searchTerm} 
                    onChange={e => setSearchTerm(e.target.value)} 
                  />
               </div>

               <div className="mt-6 flex items-center justify-between px-2">
                  <div className="flex items-center gap-2">
                     <div className="w-1.5 h-1.5 rounded-full bg-slate-700" />
                     <span className="text-[9px] text-slate-600 font-mono">IDLE_TIMEOUT: 60s</span>
                  </div>
                  <span className="text-[9px] text-slate-700 font-mono uppercase">Results: {filtered.length}</span>
               </div>
            </div>
          </div>
        </div>

        <div className="mb-20">
           <div className="flex items-center justify-between mb-10 pb-6 border-b border-white/5">
              <div className="flex items-center gap-3">
                 <div className="w-1 h-6 bg-blue-600 rounded-full" />
                 <h2 className="text-2xl font-black text-white italic tracking-tighter uppercase">Active Fragments</h2>
              </div>
              <div className="flex gap-1.5">
                 {[1,2,3].map(i => <div key={i} className="w-1 h-1 rounded-full bg-slate-800" />)}
              </div>
           </div>
           
           <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 2xl:grid-cols-6 gap-8">
              {filtered.map(([key, data]) => (
                <div key={key} onClick={() => setSelectedAgent({id: key, data})}>
                  <AgentCard id={key} data={data} />
                </div>
              ))}

              {filtered.length === 0 && (
                <div className="col-span-full py-32 border-2 border-dashed border-white/5 rounded-[3rem] flex flex-col items-center justify-center opacity-20 grayscale">
                  <ShieldCheck size={64} className="mb-4 text-slate-500" />
                  <p className="font-mono text-xs italic">BUFFER_EMPTY: NO_MATCHES</p>
                </div>
              )}
           </div>
        </div>
      </main>

      <AgentInspector 
        isOpen={!!selectedAgent} 
        onClose={() => setSelectedAgent(null)} 
        agentId={selectedAgent?.id} 
        data={selectedAgent?.data} 
      />
    </div>
  );
}

export default App;