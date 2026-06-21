import React, { useState, useEffect } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { Menu, X, Link2, LayoutDashboard, LogOut, LogIn, UserPlus } from "lucide-react";
import { useStoreContext } from "../contextApi/ContextApi";
import { cn } from "../lib/utils";
import { Button } from "./ui/button";

const Navbar = () => {
  const navigate = useNavigate();
  const { token, setToken } = useStoreContext();
  const path = useLocation().pathname;
  const [navbarOpen, setNavbarOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 20);
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  const onLogOutHandler = () => {
    setToken(null);
    localStorage.removeItem("JWT_TOKEN");
    navigate("/login");
  };

  const navLinks = [
    { to: "/", label: "Home" },
    { to: "/about", label: "About" },
    ...(token ? [{ to: "/dashboard", label: "Dashboard", icon: LayoutDashboard }] : []),
  ];

  return (
    <nav
      className={cn(
        "fixed top-0 left-0 right-0 z-50 transition-all duration-300",
        scrolled
          ? "glass border-b border-white/8 shadow-lg shadow-black/20"
          : "bg-transparent"
      )}
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2.5 group">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-violet-500 to-fuchsia-600 flex items-center justify-center shadow-lg shadow-violet-500/30 group-hover:shadow-violet-500/50 transition-all duration-300 group-hover:scale-105">
              <Link2 className="w-4 h-4 text-white" strokeWidth={2.5} />
            </div>
            <span className="text-xl font-bold text-white tracking-tight">
              Link<span className="text-gradient-purple">ora</span>
            </span>
          </Link>

          {/* Desktop Nav */}
          <div className="hidden sm:flex items-center gap-1">
            {navLinks.map(({ to, label }) => (
              <Link
                key={to}
                to={to}
                className={cn(
                  "px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200",
                  path === to
                    ? "text-white bg-white/10"
                    : "text-white/60 hover:text-white hover:bg-white/8"
                )}
              >
                {label}
              </Link>
            ))}
          </div>

          {/* CTA Buttons */}
          <div className="hidden sm:flex items-center gap-2">
            {!token ? (
              <>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => navigate("/login")}
                  className="text-white/70 hover:text-white"
                >
                  <LogIn className="w-4 h-4 mr-1.5" />
                  Login
                </Button>
                <Button
                  variant="gradient"
                  size="sm"
                  onClick={() => navigate("/register")}
                >
                  <UserPlus className="w-4 h-4 mr-1.5" />
                  Sign Up
                </Button>
              </>
            ) : (
              <Button
                variant="outline"
                size="sm"
                onClick={onLogOutHandler}
                className="text-white/70 hover:text-red-400 border-white/15 hover:border-red-500/30"
              >
                <LogOut className="w-4 h-4 mr-1.5" />
                Logout
              </Button>
            )}
          </div>

          {/* Mobile Menu Button */}
          <button
            onClick={() => setNavbarOpen(!navbarOpen)}
            className="sm:hidden p-2 rounded-lg text-white/70 hover:text-white hover:bg-white/10 transition-colors"
          >
            {navbarOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>

        {/* Mobile Menu */}
        <div
          className={cn(
            "sm:hidden transition-all duration-300 overflow-hidden",
            navbarOpen ? "max-h-80 opacity-100 pb-4" : "max-h-0 opacity-0"
          )}
        >
          <div className="flex flex-col gap-1 pt-2 border-t border-white/10">
            {navLinks.map(({ to, label }) => (
              <Link
                key={to}
                to={to}
                onClick={() => setNavbarOpen(false)}
                className={cn(
                  "px-4 py-2.5 rounded-lg text-sm font-medium transition-all duration-200",
                  path === to
                    ? "text-white bg-white/10"
                    : "text-white/60 hover:text-white hover:bg-white/8"
                )}
              >
                {label}
              </Link>
            ))}
            <div className="flex gap-2 mt-2 pt-2 border-t border-white/10">
              {!token ? (
                <>
                  <Button
                    variant="ghost"
                    size="sm"
                    className="flex-1 text-white/70"
                    onClick={() => { navigate("/login"); setNavbarOpen(false); }}
                  >
                    Login
                  </Button>
                  <Button
                    variant="gradient"
                    size="sm"
                    className="flex-1"
                    onClick={() => { navigate("/register"); setNavbarOpen(false); }}
                  >
                    Sign Up
                  </Button>
                </>
              ) : (
                <Button
                  variant="outline"
                  size="sm"
                  className="flex-1 text-red-400 border-red-500/30"
                  onClick={onLogOutHandler}
                >
                  <LogOut className="w-4 h-4 mr-1.5" />
                  Logout
                </Button>
              )}
            </div>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;