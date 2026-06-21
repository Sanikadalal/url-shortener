import React from "react";
import { Link2, MessageCircle, Code2, AtSign } from "lucide-react";
import { Link } from "react-router-dom";

const Footer = () => {
  return (
    <footer className="relative border-t border-white/8 bg-surface-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="flex flex-col lg:flex-row justify-between gap-10">
          {/* Brand */}
          <div className="max-w-xs">
            <Link to="/" className="flex items-center gap-2.5 mb-4">
              <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-violet-500 to-fuchsia-600 flex items-center justify-center shadow-lg shadow-violet-500/30">
                <Link2 className="w-4 h-4 text-white" strokeWidth={2.5} />
              </div>
              <span className="text-lg font-bold text-white">
                Link<span className="text-gradient-purple">ora</span>
              </span>
            </Link>
            <p className="text-white/40 text-sm leading-relaxed">
              The fastest way to shorten, share, and track your links. Built for developers and marketers.
            </p>
          </div>

          {/* Links */}
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-8 text-sm">
            <div>
              <h3 className="text-white font-semibold mb-3">Product</h3>
              <ul className="space-y-2 text-white/40">
                <li><Link to="/" className="hover:text-white/70 transition-colors">Home</Link></li>
                <li><Link to="/about" className="hover:text-white/70 transition-colors">About</Link></li>
                <li><Link to="/dashboard" className="hover:text-white/70 transition-colors">Dashboard</Link></li>
              </ul>
            </div>
            <div>
              <h3 className="text-white font-semibold mb-3">Features</h3>
              <ul className="space-y-2 text-white/40">
                <li className="hover:text-white/70 transition-colors cursor-default">Analytics</li>
                <li className="hover:text-white/70 transition-colors cursor-default">Security</li>
                <li className="hover:text-white/70 transition-colors cursor-default">API Access</li>
              </ul>
            </div>
            <div>
              <h3 className="text-white font-semibold mb-3">Account</h3>
              <ul className="space-y-2 text-white/40">
                <li><Link to="/login" className="hover:text-white/70 transition-colors">Login</Link></li>
                <li><Link to="/register" className="hover:text-white/70 transition-colors">Sign Up</Link></li>
              </ul>
            </div>
          </div>
        </div>

        <div className="mt-10 pt-8 border-t border-white/8 flex flex-col sm:flex-row justify-between items-center gap-4">
          <p className="text-white/30 text-sm">© 2025 Linkora. All rights reserved.</p>
          <div className="flex items-center gap-4">
            <a href="#" className="text-white/30 hover:text-white/70 transition-colors">
              <MessageCircle className="w-4 h-4" />
            </a>
            <a href="#" className="text-white/30 hover:text-white/70 transition-colors">
              <Code2 className="w-4 h-4" />
            </a>
            <a href="#" className="text-white/30 hover:text-white/70 transition-colors">
              <AtSign className="w-4 h-4" />
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;