import React from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import { ArrowRight, Link2, BarChart3, Shield, Zap, Globe, ChevronRight } from "lucide-react";
import { useStoreContext } from "../contextApi/ContextApi";
import { Button } from "./ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "./ui/card";

const features = [
  {
    icon: Link2,
    title: "Instant Shortening",
    description: "Generate short, memorable links in milliseconds with our blazing-fast infrastructure.",
    color: "from-violet-500 to-purple-600",
    glow: "rgba(139,92,246,0.2)",
  },
  {
    icon: BarChart3,
    title: "Deep Analytics",
    description: "Track clicks, measure performance, and understand your audience with real-time data.",
    color: "from-blue-500 to-cyan-600",
    glow: "rgba(59,130,246,0.2)",
  },
  {
    icon: Shield,
    title: "Secure & Reliable",
    description: "All links are protected with enterprise-grade encryption and 99.9% uptime guarantee.",
    color: "from-emerald-500 to-teal-600",
    glow: "rgba(16,185,129,0.2)",
  },
  {
    icon: Zap,
    title: "Lightning Fast",
    description: "Sub-millisecond redirects powered by globally distributed edge nodes.",
    color: "from-amber-500 to-orange-600",
    glow: "rgba(245,158,11,0.2)",
  },
];

const stats = [
  { value: "10M+", label: "Links Created" },
  { value: "500M+", label: "Redirects Served" },
  { value: "99.9%", label: "Uptime" },
  { value: "150+", label: "Countries" },
];

const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: { staggerChildren: 0.1 },
  },
};

const itemVariants = {
  hidden: { opacity: 0, y: 30 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.6, ease: "easeOut" } },
};

const LandingPage = () => {
  const navigate = useNavigate();
  const { token } = useStoreContext();

  const handleDashboard = () => navigate(token ? "/dashboard" : "/login");
  const handleCreate = () => navigate(token ? "/dashboard" : "/register");

  return (
    <div className="min-h-screen bg-surface">
      {/* Grid background */}
      <div className="fixed inset-0 grid-bg opacity-50 pointer-events-none" />

      {/* Hero glow */}
      <div className="fixed top-0 left-1/2 -translate-x-1/2 w-[800px] h-[400px] bg-gradient-to-b from-violet-600/20 to-transparent rounded-full blur-3xl pointer-events-none" />

      <div className="relative pt-24 pb-16 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
        {/* Badge */}
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="flex justify-center mb-8"
        >
          <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full glass border-glow text-sm text-white/70">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
            <span>Trusted by 50,000+ developers worldwide</span>
          </div>
        </motion.div>

        {/* Hero Text */}
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.7, delay: 0.1 }}
          className="text-center mb-8"
        >
          <h1 className="text-5xl sm:text-6xl lg:text-7xl xl:text-8xl font-black tracking-tight leading-[0.9] mb-6">
            <span className="text-white">Shorten.</span>
            <br />
            <span className="text-gradient">Share. Track.</span>
          </h1>
          <p className="text-lg sm:text-xl text-white/50 max-w-2xl mx-auto leading-relaxed font-light">
            Transform long, unwieldy URLs into powerful, trackable short links.
            Built for speed, designed for scale.
          </p>
        </motion.div>

        {/* CTA Buttons */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.3 }}
          className="flex flex-col sm:flex-row gap-4 justify-center mb-16"
        >
          <Button
            size="xl"
            variant="gradient"
            onClick={handleCreate}
            className="group shadow-2xl shadow-violet-500/30"
          >
            Create Short Link
            <ArrowRight className="w-5 h-5 ml-1 group-hover:translate-x-1 transition-transform" />
          </Button>
          <Button
            size="xl"
            variant="outline"
            onClick={handleDashboard}
          >
            {token ? "Go to Dashboard" : "View Dashboard"}
            <ChevronRight className="w-5 h-5 ml-1" />
          </Button>
        </motion.div>

        {/* Stats */}
        <motion.div
          variants={containerVariants}
          initial="hidden"
          animate="visible"
          className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-24"
        >
          {stats.map((stat) => (
            <motion.div key={stat.label} variants={itemVariants}>
              <div className="glass rounded-2xl p-6 text-center border border-white/8 hover:border-violet-500/30 transition-all duration-300 hover:shadow-glow">
                <div className="text-3xl sm:text-4xl font-black text-gradient-purple mb-1">
                  {stat.value}
                </div>
                <div className="text-sm text-white/40 font-medium">{stat.label}</div>
              </div>
            </motion.div>
          ))}
        </motion.div>

        {/* Features */}
        <motion.div
          variants={containerVariants}
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, margin: "-100px" }}
        >
          <motion.div variants={itemVariants} className="text-center mb-12">
            <p className="text-sm font-semibold text-violet-400 uppercase tracking-widest mb-3">
              Why Linkora
            </p>
            <h2 className="text-3xl sm:text-4xl lg:text-5xl font-black text-white">
              Everything you need,
              <br />
              <span className="text-gradient">nothing you don't.</span>
            </h2>
          </motion.div>

          <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-5">
            {features.map((feature) => (
              <motion.div key={feature.title} variants={itemVariants}>
                <Card className="group h-full hover:border-white/20 transition-all duration-300 hover:-translate-y-1 hover:shadow-card">
                  <CardHeader className="pb-2">
                    <div
                      className={`w-12 h-12 rounded-xl bg-gradient-to-br ${feature.color} flex items-center justify-center mb-3 shadow-lg group-hover:scale-110 transition-transform duration-300`}
                      style={{ boxShadow: `0 8px 30px ${feature.glow}` }}
                    >
                      <feature.icon className="w-6 h-6 text-white" />
                    </div>
                    <CardTitle className="text-base">{feature.title}</CardTitle>
                  </CardHeader>
                  <CardContent className="pt-0">
                    <CardDescription>{feature.description}</CardDescription>
                  </CardContent>
                </Card>
              </motion.div>
            ))}
          </div>
        </motion.div>

        {/* CTA Banner */}
        <motion.div
          initial={{ opacity: 0, y: 40 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6 }}
          className="mt-24 relative overflow-hidden rounded-3xl"
        >
          <div className="absolute inset-0 bg-gradient-to-r from-violet-600/30 via-fuchsia-600/20 to-violet-600/30" />
          <div className="absolute inset-0 grid-bg opacity-30" />
          <div className="relative glass border border-violet-500/20 rounded-3xl p-10 sm:p-16 text-center">
            <Globe className="w-12 h-12 text-violet-400 mx-auto mb-6 animate-float" />
            <h2 className="text-3xl sm:text-4xl font-black text-white mb-4">
              Ready to go global?
            </h2>
            <p className="text-white/50 text-lg mb-8 max-w-xl mx-auto">
              Join thousands of developers and marketers who trust Linkora to power their links.
            </p>
            <Button size="lg" variant="gradient" onClick={handleCreate} className="shadow-2xl shadow-violet-500/30">
              Get Started Free
              <ArrowRight className="w-5 h-5 ml-1" />
            </Button>
          </div>
        </motion.div>
      </div>
    </div>
  );
};

export default LandingPage;