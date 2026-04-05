import { Activity, Server, Trash2 } from 'lucide-react';

export default function TopBar({ status }) {
  const isOnline = status === 'ONLINE';
  
  return (
    <div className="flex items-center justify-between p-6 mb-8 border-b border-border bg-card/50">
      <div className="flex items-center gap-3">
        <Server className="w-6 h-6 text-primary" />
        <h1 className="text-2xl font-bold text-gray-100">SwiftCache Control Plane</h1>
      </div>
      <div className="flex items-center gap-4">
        <div className={`flex items-center gap-2 px-3 py-1 text-sm font-medium rounded-full ${isOnline ? 'bg-success/20 text-success' : 'bg-red-500/20 text-red-400'}`}>
          <Activity className="w-4 h-4" />
          {status}
        </div>
        <button className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-gray-300 transition-colors border rounded-lg border-border hover:bg-border hover:text-white">
          <Trash2 className="w-4 h-4" />
          Force GC
        </button>
      </div>
    </div>
  );
}