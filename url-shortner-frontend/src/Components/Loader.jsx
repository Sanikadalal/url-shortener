import React from "react";
import { Loader2 } from "lucide-react";

const Loader = () => {
  return (
    <div className="min-h-screen bg-surface flex items-center justify-center">
      <div className="flex flex-col items-center gap-4">
        <div className="relative">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-violet-500 to-fuchsia-600 flex items-center justify-center shadow-2xl shadow-violet-500/30 animate-glow-pulse">
            <Loader2 className="w-8 h-8 text-white animate-spin" />
          </div>
        </div>
        <p className="text-white/40 text-sm font-medium">Loading...</p>
      </div>
    </div>
  );
};

export default Loader;