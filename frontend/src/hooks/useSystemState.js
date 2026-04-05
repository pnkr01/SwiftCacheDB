import { useState, useEffect } from 'react';

export function useSystemState() {
  const [systemData, setSystemData] = useState({ status: 'OFFLINE', totalKeys: 0, memorySnapshot: {}, metrics: {} });
  const [error, setError] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await fetch('/api/v1/system');
        const data = await res.json();
        setSystemData(data);
        setError(false);
      } catch (err) {
        setError(true);
        setSystemData(prev => ({ ...prev, status: 'OFFLINE' }));
        console.log(err);
        
      }
    };
    fetchData();
    const interval = setInterval(fetchData, 1500);
    return () => clearInterval(interval);
  }, []);

  return { systemData, error };
}